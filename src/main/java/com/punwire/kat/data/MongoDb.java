package com.punwire.kat.data;

import com.mongodb.*;
import com.mongodb.bulk.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.zerodha.ZdHolding;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdSymbol;
import com.punwire.kat.zerodha.ZdVolatility;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.BasicBSONList;

import javax.print.Doc;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

/**
 * Created by Kanwal on 13/01/16.
 */
public class MongoDb {

    MongoClient mongoClient;
    MongoDatabase database;

    public MongoDb() {
        mongoClient = new MongoClient("localhost",27020);
        database = mongoClient.getDatabase("trade");
        mongoClient.setWriteConcern(WriteConcern.JOURNALED);
    }


    public void removeSymbol(String symbol) {
        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        c.drop();
    }

    public boolean stockExists(Stock stock) {
        MongoCollection c = database.getCollection("symbols");
        Document d = new Document();
        d.append("symbol", stock.getSymbol());

        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        try {
            long res = c.count();
            return res > 0;
        } catch (Exception ex)
        {

        }
        return false;
    }

    public long getLastDate(Stock stock) {
        MongoCollection c = database.getCollection(stock.getSymbol().toLowerCase().replace("-", "_"));
        Document sort = new Document();
        sort.append("time", -1);

        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        try {
            MongoCursor<Document> cursor = c.find().sort(sort).limit(1).iterator();
            Document doc = cursor.next();
            if( doc != null) return doc.getLong("time");
            else return 0;
        } catch (Exception ex)
        {
            //ex.printStackTrace();
        }
        return 0;
    }

    public long getFirstDate(Stock stock) {
        MongoCollection c = database.getCollection(stock.getSymbol().toLowerCase().replace("-", "_"));
        Document sort = new Document();
        sort.append("time", 1);

        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        try {
            MongoCursor<Document> cursor = c.find().sort(sort).limit(1).iterator();
            Document doc = cursor.next();
            if( doc != null) return doc.getLong("time");
            else return 0;
        } catch (Exception ex)
        {
            //ex.printStackTrace();
        }
        return 0;
    }

    public void saveStock(Stock stock) {
        if( stockExists(stock)) return;

        MongoCollection c = database.getCollection("symbols");
        Document doc = new Document("symbol",1);
        Document doc1 = new Document("unique",true);
        c.createIndex(doc, new IndexOptions().unique(true));

        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        try {
            Document d = new Document();
            d.append("symbol", stock.getSymbol());
            d.append("exchange", stock.getExchange());
            d.append("root", stock.getSymbolRoot());
            d.append("company", stock.getCompanyName());
            c.insertOne(d);
            ensureIndex(stock.getSymbol());

        } catch (Exception ex)
        {

        }

    }

    public boolean optionDataExists(LocalDate date, String instrument, String symbol){
        MongoCollection col = database.getCollection(instrument.toLowerCase());
        Document d = new Document();
        d.put("symbol",symbol);
        d.put("date", DateUtil.intDate(date));
        long l = col.count(d);
        return l > 0;
    }

    public List<Option>  getOptions(String instrument, String symbol){
        List<Option> optionChain = new ArrayList<>();
        MongoCollection col = database.getCollection(instrument.toLowerCase());
        Document d = new Document();
        d.put("symbol", symbol);


        Document sort = new Document();
        sort.append("date", -1);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMYY");

        MongoCursor<Document> cursor = col.find(d).sort(sort).limit(1).iterator();
        Document doc = cursor.next();
        if( doc != null) {
            //No Option Data Exists
            int lastDate = doc.getInteger("date");
            d.put("date",lastDate);

            sort = new Document();
            sort.append("expiry_date", 1);
            sort.append("strike", 1);

            cursor = col.find(d).sort(sort).iterator();
            DecimalFormat myFormatter = new DecimalFormat("####");
            while(cursor.hasNext())
            {
                Document opt = cursor.next();
                long oi = opt.getLong("oi");
                long contract = opt.getLong("contracts");

                if( oi < 1000  || contract < 1000) continue;
                int exDate = opt.getInteger("expiry_date");
                String opType = opt.getString("opt_type");
                double strike = opt.getDouble("strike");
                LocalDate lDate = DateUtil.toDate(exDate);

                String optSym = symbol + myFormatter.format(strike) + opType + lDate.format(df);


                Option o = new Option(optSym, symbol, opType , lDate, strike);

                o.setLastPrice( opt.getDouble("c"));
                o.setOpenInterest(opt.getLong("oi"));
                o.setOiChange(opt.getLong("oi_change"));
                o.setVolume(opt.getLong("contracts"));
                optionChain.add(o);
            }
        }
        else
        {
            //No Option Data Exists
        }



        return optionChain;
    }

    public void saveOption(LocalDate date, String instrument, String symbol, LocalDate expiry_date, double strike_price,
                           String opt_type, double o ,double h ,double l ,double c ,long contracts, long open_int,
                           long oi_change ) {
        MongoCollection col = database.getCollection(instrument.toLowerCase());

        Document d = new Document();
        d.put("symbol",symbol);
        d.put("date",DateUtil.intDate(date));
        d.put("expiry_date",DateUtil.intDate(expiry_date));
        d.put("strike",strike_price);
        d.put("opt_type",opt_type);
        d.put("o",o);
        d.put("h",h);
        d.put("l",l);
        d.put("c", c);
        d.put("contracts",contracts);
        d.put("oi",open_int);
        d.put("oi_change",oi_change);
        col.insertOne(d);
    }

    public void deleteOptionChain(String underline, LocalDate onDate, double strike) {
        MongoCollection col = database.getCollection("OptionChain");
        Document d = new Document();
        d.put("symbol",underline);
        d.put("on_date",DateUtil.intDate(onDate));
        d.put("strike",strike);
        col.deleteMany(d);
    }

    public Integer geteOptionLastDate(String underline) {
        MongoCollection col = database.getCollection("OptionChain");
        Document doc = new Document("symbol", underline);
        Document sort = new Document("on_date", -1);
        MongoCursor<Document> cursor = col.find(doc).sort(doc).sort(sort).limit(1).iterator();
        Document d = cursor.next();
        return d==null? 0 : d.getInteger("on_date");
    }

    public Integer getLastDateEod(String underline) {
        MongoCollection col = database.getCollection("DailyBar");
        Document doc = new Document("symbol", underline);
        Document sort = new Document("date", -1);
        MongoCursor<Document> cursor = col.find(doc).sort(sort).limit(1).iterator();
        Document d = cursor.next();
        return d==null? 0 : d.getInteger("date");
    }

    public Integer getLastDateVol(String underline) {
        MongoCollection col = database.getCollection("Volatility");
        Document doc = new Document("symbol", underline);
        Document sort = new Document("date", -1);
        MongoCursor<Document> cursor = col.find(doc).sort(sort).limit(1).iterator();
        Document d = cursor.next();
        return d==null? 0 : d.getInteger("date");
    }


    public List<ZdOptionChain> geteOptionChain(String underline) {
        Integer date = geteOptionLastDate(underline);

        MongoCollection col = database.getCollection("OptionChain");
        Document doc = new Document("symbol",underline);
        doc.put("on_date",date);
        Document sort = new Document("strike",1);
        List<ZdOptionChain> optionList = new ArrayList<>();

        MongoCursor<Document> cursor = col.find(doc).sort(sort).iterator();
        try {
            while (cursor.hasNext()) {
                Document row = cursor.next();
                int expDate = row.getInteger("expiry_date");
                int onDate = row.getInteger("on_date");
                LocalDate expiryDate = DateUtil.toDate(expDate);
                LocalDate asOfDate = DateUtil.toDate(onDate);
                long oiCall =  row.getLong("call_oi");
                long oiPut = row.getLong("put_oi");
                long oicCall = row.getLong("call_oic");
                long vCall = row.getLong("call_volume");
                double ivCall = row.getDouble("call_iv");
                double underlinePrice = row.getDouble("underlinePrice");
                double ltpCall = row.getDouble("call_ltp");
                double changeCall = row.getDouble("call_change");
                double bidPriceCall = row.getDouble("call_bid");
                int bidQtyCall = 0;
                double askPriceCall = row.getDouble("call_ask");
                int askQtyCall = 0;
                Double strike = row.getDouble("strike");
                int bidQtyPut =0;
                double bidPricePut = row.getDouble("put_bid");
                double askPricePut = row.getDouble("put_ask");
                int askQtyPut = 0;
                double changePut = row.getDouble("put_change");
                double ltpPut = row.getDouble("put_ltp");
                double ivPut = row.getDouble("put_iv");
                long vPut = row.getLong("put_volume");
                long oicPut = row.getLong("put_oic");
                double interest = row.getDouble("interest");
                double call_delta = row.getDouble("call_delta");
                double call_gamma = row.getDouble("call_gamma");
                double call_rho = row.getDouble("call_rho");
                double call_theta = row.getDouble("call_theta");
                double call_vega = row.getDouble("call_vega");
                double put_delta = row.getDouble("put_delta");
                double put_gamma = row.getDouble("put_gamma");
                double put_rho = row.getDouble("put_rho");
                double put_theta = row.getDouble("put_theta");
                double put_vega = row.getDouble("put_vega");

                String oSymbol = underline + NseLoader.yyMMM.format(expiryDate).toUpperCase() + NseLoader.strikePriceFormatter.format(strike);
                Option call = new Option(oSymbol+"CE",underline,underlinePrice,"CE",expiryDate,strike, ltpCall, changeCall, askPriceCall,askQtyCall,bidPriceCall,bidQtyCall, ivCall, oiCall, oicCall,vCall);
                call.setGreeks(call_delta,call_gamma,call_rho, call_theta, call_vega);
                Option put = new Option(oSymbol+"PE",underline,underlinePrice,"PE",expiryDate,strike,ltpPut, changePut, askPricePut,askQtyPut ,bidPricePut,bidQtyPut, ivPut, oiPut, oicPut,vPut);
                put.setGreeks(put_delta,put_gamma,put_rho, put_theta, put_vega);
                ZdOptionChain oc = new ZdOptionChain(underline, underlinePrice, asOfDate ,expiryDate, strike,call,put,interest);
                optionList.add(oc);
            }
        } finally {
            cursor.close();
        }
        return optionList;
    }

    public ZdOptionChain getOption(String underline, double strike) {
        Integer date = geteOptionLastDate(underline);

        MongoCollection col = database.getCollection("OptionChain");
        Document doc = new Document("symbol",underline);
        doc.put("strike",strike);

        Document sort = new Document("on_date",-1);
        ZdOptionChain oc=null;
        MongoCursor<Document> cursor = col.find(doc).sort(sort).limit(1).iterator();
        try {
            if (cursor.hasNext()) {
                Document row = cursor.next();
                int expDate = row.getInteger("expiry_date");
                int onDate = row.getInteger("on_date");
                LocalDate expiryDate = DateUtil.toDate(expDate);
                LocalDate asOfDate = DateUtil.toDate(onDate);
                long oiCall =  row.getLong("call_oi");
                long oiPut = row.getLong("put_oi");
                long oicCall = row.getLong("call_oic");
                long vCall = row.getLong("call_volume");
                double ivCall = row.getDouble("call_iv");
                double underlinePrice = row.getDouble("underlinePrice");
                double ltpCall = row.getDouble("call_ltp");
                double changeCall = row.getDouble("call_change");
                double bidPriceCall = row.getDouble("call_bid");
                int bidQtyCall = 0;
                double askPriceCall = row.getDouble("call_ask");
                int askQtyCall = 0;
                int bidQtyPut =0;
                double bidPricePut = row.getDouble("put_bid");
                double askPricePut = row.getDouble("put_ask");
                int askQtyPut = 0;
                double changePut = row.getDouble("put_change");
                double ltpPut = row.getDouble("put_ltp");
                double ivPut = row.getDouble("put_iv");
                long vPut = row.getLong("put_volume");
                long oicPut = row.getLong("put_oic");
                double interest = row.getDouble("interest");
                double call_delta = row.getDouble("call_delta");
                double call_gamma = row.getDouble("call_gamma");
                double call_rho = row.getDouble("call_rho");
                double call_theta = row.getDouble("call_theta");
                double call_vega = row.getDouble("call_vega");
                double put_delta = row.getDouble("put_delta");
                double put_gamma = row.getDouble("put_gamma");
                double put_rho = row.getDouble("put_rho");
                double put_theta = row.getDouble("put_theta");
                double put_vega = row.getDouble("put_vega");

                String oSymbol = underline + NseLoader.yyMMM.format(expiryDate).toUpperCase() + NseLoader.strikePriceFormatter.format(strike);
                Option call = new Option(oSymbol+"CE",underline,underlinePrice,"CE",expiryDate,strike, ltpCall, changeCall, askPriceCall,askQtyCall,bidPriceCall,bidQtyCall, ivCall, oiCall, oicCall,vCall);
                call.setGreeks(call_delta,call_gamma,call_rho, call_theta, call_vega);
                Option put = new Option(oSymbol+"PE",underline,underlinePrice,"PE",expiryDate,strike,ltpPut, changePut, askPricePut,askQtyPut ,bidPricePut,bidQtyPut, ivPut, oiPut, oicPut,vPut);
                put.setGreeks(put_delta, put_gamma, put_rho, put_theta, put_vega);
                oc = new ZdOptionChain(underline, underlinePrice, asOfDate ,expiryDate, strike,call,put,interest);
            }
        } finally {
            cursor.close();
        }
        return oc;
    }

    public void saveOptionChain(ZdOptionChain oc) {
        deleteOptionChain(oc.underline, oc.asOfDate, oc.strikePrice);
        MongoCollection col = database.getCollection("OptionChain");
        Document d = new Document();
        d.put("symbol",oc.underline);
        d.put("expiry_date",DateUtil.intDate(oc.expiryDate));
        d.put("on_date",DateUtil.intDate(oc.asOfDate));
        d.put("strike",oc.strikePrice);
        d.put("interest",oc.interest);
        d.put("underlinePrice",oc.underlinePrice);
        d.put("call_oi",oc.call.getOpenInterest());
        d.put("call_oic",oc.call.getOiChange());
        d.put("call_volume",oc.call.getVolume());
        d.put("call_iv",oc.call.getVolatility());
        d.put("call_ltp", oc.call.getLastPrice());
        d.put("call_change",oc.call.getLastChange());
        d.put("call_bid",oc.call.getBidPrice());
        d.put("call_ask",oc.call.getAskPrice());
        d.put("put_oi",oc.put.getOpenInterest());
        d.put("put_oic",oc.put.getOiChange());
        d.put("put_volume",oc.put.getVolume());
        d.put("put_iv",oc.put.getVolatility());
        d.put("put_ltp", oc.put.getLastPrice());
        d.put("put_change",oc.put.getLastChange());
        d.put("put_bid",oc.put.getBidPrice());
        d.put("put_ask",oc.put.getAskPrice());
        d.put("call_delta", oc.callGreeks.delta);
        d.put("call_gamma", oc.callGreeks.gamma);
        d.put("call_rho", oc.callGreeks.rho);
        d.put("call_theta", oc.callGreeks.theta);
        d.put("call_vega", oc.callGreeks.vega);
        d.put("put_delta", oc.putGreeks.delta);
        d.put("put_gamma", oc.putGreeks.gamma);
        d.put("put_rho", oc.putGreeks.rho);
        d.put("put_theta", oc.putGreeks.theta);
        d.put("put_vega", oc.putGreeks.vega);
        col.insertOne(d);

    }


    public void saveOptionList(String type, String symbol) {
        MongoCollection col = database.getCollection("nfo");

        Document d = new Document();
        d.put("type",type);
        d.put("symbol",symbol);
        col.insertOne(d);
    }

    public List<String>  getOptionList(String type) {
        MongoCollection col = database.getCollection("nfo");
        Document doc = new Document();
        if( type != null) doc.put("type", type);

        List<String> options = new ArrayList<>();
        MongoCursor<Document> cursor = col.find(doc).iterator();
        try {
            while (cursor.hasNext()) {
                Document row = cursor.next();
                options.add( row.getString("type")+"," + row.getString("symbol") );
            }
        } finally {
            cursor.close();
        }
        return options;
    }

    public void saveBar(Bar bar) {
        MongoCollection c = database.getCollection(bar.getSymbol().toLowerCase().replace("-", "_"));
        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        Document d = barToDoc(bar);
        c.insertOne(d);

    }
    public void removeDailyIndex(String name, int date) {
        MongoCollection col = database.getCollection("DailyIndex");
        Document d = new Document("name",name);
        d.put("date",date);
        col.deleteOne(d);
    }
    public void saveDailyIndex(String name, String symbol, int date, double o, double h, double l, double c,
                               double change, double changePct, long volume, double turnover, double pe, double pb, double de) {
        removeDailyIndex(name,date);
        MongoCollection col = database.getCollection("DailyIndex");
        Document d = new Document();
        d.put("name",name);
        d.put("symbol",symbol);
        d.put("date",date);
        d.put("o",o);
        d.put("h",h);
        d.put("l",l);
        d.put("c",c);
        d.put("change",change);
        d.put("change_pct",changePct);
        d.put("volumne",volume);
        d.put("turnover",turnover);
        d.put("pe",pe);
        d.put("pb",pb);
        d.put("de",de);
        col.insertOne(d);
    }



    public void deleteVolatility(String symbol, int date) {
        MongoCollection col = database.getCollection("Volatility");
        Document d = new Document();
        d.put("symbol",symbol);
        d.put("date",date);
        col.deleteOne(d);
    }

    public void saveVolatility(String symbol, int date, double price, double prev, double prevVol, double currVol, double yearVol) {
        deleteVolatility(symbol,date);
        MongoCollection col = database.getCollection("Volatility");
        Document d = new Document();
        d.put("symbol",symbol);
        d.put("date",date);
        d.put("price",price);
        d.put("prev",prev);
        d.put("prev_vol",prevVol);
        d.put("curr_vol",currVol);
        d.put("year_vol",yearVol);
        col.insertOne(d);
    }

    public ZdSymbol getLot(String symbol) {
        MongoCollection col = database.getCollection("Symbols");
        Document d = new Document();
        d.put("symbol",symbol);

        MongoCursor<Document> cursor = col.find(d).iterator();

        Document doc = cursor.next();
        return new ZdSymbol(doc.getString("name"),doc.getString("symbol"), doc.getInteger("lot_size"));
    }


    public void saveLot(String name, String symbol, int lotSize) {
        MongoCollection col = database.getCollection("Symbols");
        Document d = new Document();
        d.put("name",name);
        d.put("symbol",symbol);
        d.put("lot_size",lotSize);
        col.insertOne(d);
    }

    public ZdVolatility getVol(String symbol, LocalDate date) {
        MongoCollection col = database.getCollection("Volatility");
        Document d = new Document();
        d.put("symbol",symbol);
        Document sort =  new Document("date",-1);
        System.out.println(d.toJson());
        MongoCursor<Document> cursor = col.find(d).sort(sort).limit(1).iterator();
        try {
            Document row = cursor.next();
            int oDate = row.getInteger("date");
            double price = row.getDouble("price");
            double prev = row.getDouble("prev");
            double prevVol = row.getDouble("prev_vol");
            double currVol = row.getDouble("curr_vol");
            double yearVol = row.getDouble("year_vol");
            ZdVolatility vol = new ZdVolatility(symbol, date, price, prev, prevVol, currVol, yearVol);
            return vol;
        } catch (Exception ex)
        {
            ZdVolatility vol = new ZdVolatility(symbol, date, 0.00, 0.00, 0.00, 0.00, 0.25);
            return vol;
        } finally {
            cursor.close();
        }

    }

    public double getIvRank(String underline,  LocalDate date)
    {
        ZdVolatility currVol = getVol(underline,date);
        Document d = new Document();
        List<Document> aggList = new ArrayList<>();
        aggList.add(new Document("$match", new Document("symbol", underline)));
        Document gDoc = new Document();
        gDoc.append("_id", "$symbol");
        gDoc.append("minVol", new Document("$min", "$year_vol"));
        gDoc.append("maxVol", new Document("$max", "$year_vol"));

        Document g = new Document("$group",gDoc);

        aggList.add(g);

        System.out.println(g.toJson());
        AggregateIterable<Document> iterable = database.getCollection("Volatility").aggregate(aggList);
        MongoCursor<Document> cursor = iterable.iterator();
        Document doc = cursor.next();
        double minVol = doc.getDouble("minVol");
        double maxVol = doc.getDouble("maxVol");
        System.out.println(minVol);
        System.out.println(minVol);
        double ivRank = ((currVol.yearVol - minVol) / (maxVol - minVol));
        System.out.println(ivRank);
        return ivRank;
    }

    public void removeEod(String name, int date) {
        MongoCollection col = database.getCollection("DailyBar");
        Document d = new Document("name",name);
        d.put("date",date);
        col.deleteOne(d);
    }

    public void saveEod(String symbol, int date, double o, double h, double l, double c,  double last, double prev, long vol) {
        removeEod(symbol,date);
        MongoCollection col = database.getCollection("DailyBar");
        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        Document d = new Document();
        d.put("symbol",symbol);
        d.put("date",date);
        d.put("o",o);
        d.put("h",h);
        d.put("l",l);
        d.put("c",c);
        d.put("last",last);
        d.put("v",vol);
        d.put("prev",prev);
        col.insertOne(d);
    }

    public Bar getEod(String symbol) {
        MongoCollection col = database.getCollection("DailyBar");
        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        Document d = new Document();
        d.put("symbol",symbol);
        Document sort = new Document("date",-1);

        MongoCursor<Document> cursor = col.find(d).sort(sort).limit(1).iterator();
        try {
            Document row = cursor.next();
            double O = row.getDouble("o");
            double H = row.getDouble("h");
            double L = row.getDouble("l");
            double C = row.getDouble("c");
            long V = row.getLong("v");
            int date = row.getInteger("date");
            Bar bar = new Bar(symbol,DateUtil.longDateTime(date,0),36000,O,H,L,C,V,0);
            bar.setLast(row.getDouble("last"));
            bar.setPrev(row.getDouble("prev"));
            return bar;
        } finally {
            cursor.close();
        }

    }


    public Document barToDoc(Bar bar) {
        Document d = new Document();
        d.append("O",bar.getO());
        d.append("H",bar.getH());
        d.append("L",bar.getL());
        d.append("C",bar.getC());
        d.append("V",bar.getV());
        d.append("OI", bar.getOI());
        d.append("time", bar.getStart());
        d.append("duration", bar.getDuration());
        return d;
    }

    public Document holdingToDoc(ZdHolding holding) {
        Document d = new Document();
        d.append("symbol",holding.symbol);
        d.append("underline",holding.underline);
        d.append("type",holding.type);
        d.append("qty",holding.qty);
        d.append("avg_price",holding.avgPrice);
        d.append("curr_price",holding.currentPrice);
        d.append("currPnL", holding.currentPnL);
        return d;
    }

    public void clearHolding() {
        MongoCollection c = database.getCollection("holdings");
        c.drop();
    }

    public void saveHoldings(List<ZdHolding> holdings) {
        if( holdings.size() < 1 )  return;
        MongoCollection c = database.getCollection("holdings");
        InsertManyOptions io = new InsertManyOptions();
        io.ordered(false);

        List<Document> ops = new ArrayList<>();
        for(ZdHolding holding: holdings) {
            ops.add(holdingToDoc(holding));
        }
        c.insertMany(ops, io);
    }


    public void saveBars(String symbol, List<Bar> bars) {
        if( bars.size() < 1 )  return;

        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        //System.out.println("Collection:  " + bar.getSymbol().toLowerCase().replace("-", "_"));
        InsertManyOptions io = new InsertManyOptions();
        io.ordered(false);

        List<Document> ops = new ArrayList<>();
        for(Bar bar: bars)
        {
            ops.add(barToDoc(bar));
        }
        c.insertMany(ops,io);
    }

    public boolean barExists(String symbol, long time ){
        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        Document doc = new Document("time",time);
        long count = c.count(doc);
        return count > 0;
    }

    public void ensureIndex(String symbol){
        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        Document doc = new Document("time",1);
        Document doc1 = new Document("unique",true);
        c.createIndex(doc, new IndexOptions().unique(true));
    }



    public BarList getBars(String symbol)
    {
        return getBars(symbol,5);
    }
    public BarList getBars(String symbol, int interval)
    {
        BarList bars = new BarList(interval);
        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        MongoCursor<Document> cursor = c.find().sort(ascending("time")).iterator();
        try {
            while (cursor.hasNext()) {
                Document row = cursor.next();
                double O = row.getDouble("O");
                double H = row.getDouble("H");
                double L = row.getDouble("L");
                double C = row.getDouble("C");
                long V = row.getLong("V");
                long OI = row.getLong("OI");
                long time = row.getLong("time");
                int duration = row.getInteger("duration");
                Bar bar = new Bar(symbol, time,duration,O,H,L,C,V,OI);
                bars.addBar(bar);
        }
        } finally {
            cursor.close();
        }
        return bars;
    }

    public Dataset getDataSet(String symbol, int interval)
    {
        Dataset set = new Dataset();

        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        MongoCursor<Document> cursor = c.find().sort(ascending("time")).iterator();
        long lastDateTime=0;
        DataItem currentDataItem = null;
        try {
            while (cursor.hasNext()) {
                Document row = cursor.next();
                double O = row.getDouble("O");
                double H = row.getDouble("H");
                double L = row.getDouble("L");
                double C = row.getDouble("C");
                long V = row.getLong("V");
                long OI = row.getLong("OI");
                long time = row.getLong("time");
                int duration = row.getInteger("duration");
                long longTime = DateUtil.toInstant(time).getEpochSecond();

                int t = DateUtil.intTime(time);
                int hm = t/100;
                int hmSince = ( hm - 915) / interval;
                int barTime = 915 + hmSince * interval;
                long barDateTime = DateUtil.longDateTime(DateUtil.intDate(time), barTime * 100);
                Instant ii = DateUtil.toInstant(barDateTime);
                long mergeDateTime = DateUtil.toInstant(barDateTime).toEpochMilli();


                if( lastDateTime != mergeDateTime)
                {
                    currentDataItem = new DataItem(mergeDateTime ,O,H,L,C,V,OI);
                    set.addDataItem(currentDataItem);
                    Date d = currentDataItem.getDate();

                    lastDateTime = mergeDateTime;
                }
                else
                {
                    if( H > currentDataItem.getHigh() ) currentDataItem.setHigh(H);
                    if( L < currentDataItem.getLow() ) currentDataItem.setLow(L);
                    currentDataItem.setClose(C);
                    currentDataItem.setVolume(currentDataItem.getVolume() + V );
                    currentDataItem.setOpenInterest( OI);
                }
            }
        } finally {
            cursor.close();
        }
        return set;
    }

    public DataItem getLastDataItem(String symbol, int interval)
    {
        MongoCollection c = database.getCollection(symbol.toLowerCase().replace("-", "_"));
        MongoCursor<Document> cursor = c.find().sort(ascending("time")).iterator();
        long lastDateTime=0;
        DataItem currentDataItem = null;
        try {
            while (cursor.hasNext()) {
                Document row = cursor.next();
                double O = row.getDouble("O");
                double H = row.getDouble("H");
                double L = row.getDouble("L");
                double C = row.getDouble("C");
                long V = row.getLong("V");
                long OI = row.getLong("OI");
                long time = row.getLong("time");
                int duration = row.getInteger("duration");
                long longTime = DateUtil.toInstant(time).getEpochSecond();

                int t = DateUtil.intTime(time);
                int hm = t/100;
                int hmSince = ( hm - 915) / interval;
                int barTime = 915 + hmSince * interval;
                long barDateTime = DateUtil.longDateTime(DateUtil.intDate(time), barTime * 100);
                long mergeDateTime = DateUtil.toInstant(barDateTime).getEpochSecond();

                if( lastDateTime != mergeDateTime)
                {
                    if( currentDataItem != null ) return currentDataItem;
                    currentDataItem = new DataItem(mergeDateTime * 1000,O,H,L,C,V,OI);
                    Date d = currentDataItem.getDate();
                    lastDateTime = mergeDateTime;
                }
                else
                {
                    if( H > currentDataItem.getHigh() ) currentDataItem.setHigh(H);
                    if( L < currentDataItem.getLow() ) currentDataItem.setLow(L);
                    currentDataItem.setClose(C);
                    currentDataItem.setVolume(currentDataItem.getVolume() + V );
                    currentDataItem.setOpenInterest( OI);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
