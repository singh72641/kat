package com.punwire.kat.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.data.MongoDb;
import com.punwire.kat.data.Option;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionMapper;
import com.punwire.kat.zerodha.ZdSymbol;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kanwal on 12/02/16.
 */
public class Trade {
    HashMap<Integer, OptionPosition> positions = new HashMap<>();
    public static int IDSEQ=0;
    int id;
    Integer positionNum=0;
    String name;
    public final String symbol;
    public double underlinePrice=0.0;
    LocalDate date;
    public double delta=9999.99;
    public double totalMargin=9999.99;
    public double pnlAtExpiry=0.0;
    static HttpClient client=null;
    ZdSymbol lot;

    public Trade(String symbol, String name, LocalDate date)
    {
        this.id = IDSEQ++;
        this.symbol = symbol;
        this.name = name;
        this.date = date;
        lot = AppConfig.db.getLot(symbol);
    }

    public static Trade shortPutSpread(String symbol, double lowerStrike, double uppperStrike, int qty, double lowPremium, double upperPremium)
    {
        MongoDb db = AppConfig.db;
        ZdOptionChain lower = db.getOption(symbol, lowerStrike);
        ZdOptionChain upper = db.getOption(symbol, uppperStrike);
        OptionPosition ot1 = new OptionPosition(upper.put, -1 * qty,upperPremium,LocalDate.now());
        OptionPosition ot2 = new OptionPosition(lower.put, qty, lowPremium,LocalDate.now());
        Trade trade = new Trade(symbol, "Short Put Spread",LocalDate.now());
        trade.addPosition(ot1);
        trade.addPosition(ot2);
        return trade;
    }

    public static Trade shortPutSpread(String symbol, double lowerStrike, double uppperStrike, int qty)
    {
        MongoDb db = AppConfig.db;
        ZdOptionChain lower = db.getOption(symbol, lowerStrike);
        ZdOptionChain upper = db.getOption(symbol, uppperStrike);
        OptionPosition ot1 = new OptionPosition(upper.put, -1 * qty,upper.put.getLastPrice(),LocalDate.now());
        OptionPosition ot2 = new OptionPosition(lower.put, qty, lower.put.getLastPrice(),LocalDate.now());
        Trade trade = new Trade(symbol, "Short Put Spread",LocalDate.now());
        trade.addPosition(ot1);
        trade.addPosition(ot2);
        return trade;
    }

    public void addPosition(Option option, int lots){
        String symbol = option.getUnderline();
        ZdSymbol lot = AppConfig.db.getLot(symbol);
        OptionPosition ot1 = new OptionPosition(option, lot.lotSize * lots, option.getLastPrice(), LocalDate.now());
        addPosition(ot1);
    }


    public void addPosition(String symbol, double strike, String optType, int lots){
        MongoDb db = AppConfig.db;
        ZdOptionChain option = db.getOption(symbol, strike);
        ZdSymbol lot = db.getLot(symbol);
        if( optType.equals("CE")) {
            OptionPosition ot1 = new OptionPosition(option.call, lot.lotSize * lots, option.call.getLastPrice(), LocalDate.now());
            addPosition(ot1);
        }else{
            OptionPosition ot1 = new OptionPosition(option.put, lot.lotSize * lots, option.put.getLastPrice(), LocalDate.now());
            addPosition(ot1);
        }
    }

    public void addPosition(String symbol, double strike, String optType, int lots, double price){
        MongoDb db = AppConfig.db;
        ZdOptionChain option = db.getOption(symbol, strike);
        ZdSymbol lot = db.getLot(symbol);
        if( optType.equals("CE")) {
            OptionPosition ot1 = new OptionPosition(option.call, lot.lotSize * lots, price, LocalDate.now());
            addPosition(ot1);
        }else{
            OptionPosition ot1 = new OptionPosition(option.put, lot.lotSize * lots, price, LocalDate.now());
            addPosition(ot1);
        }
    }

    public void addPositionQty(String symbol, double strike, String optType, int qty, double price, String expMonth){
        if( qty == 0 ) return;
        MongoDb db = AppConfig.db;
        ZdOptionChain option = AppConfig.store.get(symbol).get(expMonth,strike);
        if( optType.equals("CE")) {
            OptionPosition ot1 = new OptionPosition(option.call, qty, price, LocalDate.now());
            ot1.currPrice = option.call.getLastPrice();
            addPosition(ot1);
        }else{
            OptionPosition ot1 = new OptionPosition(option.put, qty, price, LocalDate.now());
            ot1.currPrice = option.put.getLastPrice();
            addPosition(ot1);
        }
    }


    public void addPosition(OptionPosition position){
        Integer posId = positionNum++;
        position.setId(posId);
        positions.put(posId,position);
    }

    public void update(){
        System.out.println("Updating Trade at Price: " + underlinePrice);
        updateDelta();
        calculateMargin();
        pnlAtExpiry = getPnLAtExpiry(underlinePrice);
    }

    public void reversePosition(Integer id){
        OptionPosition position = positions.get(id);
        position.qty = -1 * position.qty;
    }

    public void updateQty(Integer id, int qty){
        OptionPosition position = positions.get(id);
        position.qty = qty;
    }

    public void updatePrice(Integer id, double price){
        OptionPosition position = positions.get(id);
        position.avgPrice= price;
    }



    public void removePosition(Integer id){
        System.out.println("Removing ID " + id);
        positions.remove(id);
    }

    public ObjectNode toJson()
    {
        ObjectNode d = AppConfig.mapper.createObjectNode();
        ArrayNode p = AppConfig.mapper.createArrayNode();
        for(OptionPosition op: positions.values())
        {
            ObjectNode ojson = op.toJson();
            p.add(ojson);
        }
        d.put("id",id);
        d.put("name",name);
        d.put("date", DateUtil.intDate(date));
        d.put("delta", delta);
        d.put("margin", totalMargin);
        double roc = 0.0;
        System.out.println("pnlAtExpirty: " + pnlAtExpiry);
        System.out.println("totalMargin: " + totalMargin);
        if( totalMargin > 0 ) roc = (pnlAtExpiry/totalMargin) * 100;
        d.put("pnl", pnlAtExpiry);
        d.put("roc", roc);
        d.put("positions", p);
        return d;
    }

    public double getPnLAtExpiry(double price)
    {
        double pnl = 0.0;
        for(OptionPosition op: positions.values())
        {
            double p = op.getPnLAt(price);
            //System.out.println(op.toString() + "     PnL= " + p + "  PriceAt: " + price);
            pnl += p;
        }
        //System.out.println("Total PnL= " + pnl);
        return pnl - (50 * positions.size());
    }

    public double updateDelta()
    {
        double d = 0.00;
        double totalQty=0;
        System.out.println("Upading Delta...");
        for(OptionPosition op: positions.values())
        {
            totalQty += Math.abs(op.qty);
            //System.out.println("Deta Add Qty: " + Math.abs(op.qty) + "  TotalQty: " + totalQty);
            d += Math.abs(op.qty) *   ( op.qty > 0?1:-1 ) * op.option.delta;
            //System.out.println("Deta Add deta: " +( Math.abs(op.qty) *   ( op.qty > 0?1:-1 ) * op.option.delta) + "  TotalDeta: " + d);
        }
        delta = d/totalQty;
        return delta;
    }

    public double getDelta()
    {
        return delta;
    }

    public double getMinStrike()
    {
        double strike = 999999.00;
        for(OptionPosition op: positions.values())
        {
            if( op.option.getStrike() < strike ) strike = op.option.getStrike();
        }
        return strike;
    }

    public double getMaxStrike()
    {
        double strike = 0.00;
        for(OptionPosition op: positions.values())
        {
            if( op.option.getStrike() > strike ) strike = op.option.getStrike();
        }
        return strike;
    }


    public void calculateMargin() {
        String url = "https://zerodha.com/margin-calculator/SPAN/";
        System.out.println(url);
        try {
            if( client == null ) {
                HttpClientBuilder b = HttpClientBuilder.create();

                // setup a Trust Strategy that allows all certificates.
                //
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();
                b.setSslcontext(sslContext);

                // don't check Hostnames, either.
                //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
                HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

                // here's the special part:
                //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
                //      -- and create a Registry, to register it.
                //
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

                // now, we create connection-manager using our Registry.
                //      -- allows multi-threaded use
                PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                b.setConnectionManager(connMgr);
                client = b.build();
            }

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("action","calculate"));
            totalMargin = 0.0;
            for(OptionPosition op: positions.values())
            {
                if ( op.qty > 0 )
                {
                    totalMargin += op.avgPrice * op.qty;
                }
                urlParameters.add(new BasicNameValuePair("exchange[]","NFO"));
                urlParameters.add(new BasicNameValuePair("product[]","OPT"));
                String symbol = op.option.getSymbol();
                urlParameters.add(new BasicNameValuePair("scrip[]", ZdOptionMapper.getUnderline(symbol)+ZdOptionMapper.getExpMonth(symbol)));
                urlParameters.add(new BasicNameValuePair("option_type[]",op.option.getOptionType()));
                urlParameters.add(new BasicNameValuePair("strike_price[]", ""+ Math.round(op.option.getStrike()) ));
                urlParameters.add(new BasicNameValuePair("qty[]", ""+ Math.abs(op.qty)));
                urlParameters.add(new BasicNameValuePair("trade[]", ( op.qty > 0 ?"buy" : "sell") ));
            }

            System.out.println(urlParameters.toString());


            HttpPost p = new HttpPost(url);
            p.setEntity(new UrlEncodedFormEntity(urlParameters));

            p.addHeader("X-Requested-With","XMLHttpRequest");
            p.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
            p.addHeader("Accept-Encoding","gzip, deflate");
            p.addHeader("Accept-Language","en-US, en; q=0.5");
            p.addHeader("Cache-Control","no-cache");
            p.addHeader("Connection","Keep-Alive");
            p.addHeader("Referer","https://zerodha.com/margin-calculator/SPAN/");
            p.addHeader("Host","zerodha.com");
            p.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");

            //Content-Length: 157
            //Content-Type: ; charset=UTF-8
            //Cookie: PHPSESSID=47snf10eoo6idoisjsaeb2qi67; __utmc=134287610; __utma=134287610.79898486.1455725926.1455775819.1455781993.4; __utmz=134287610.1455775819.3.3.utmcsr=bing|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); _ga=GA1.2.79898486.1455725926; __utmb=134287610.2.10.1455781993; __utmt=1


            HttpResponse r = client.execute(p);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = rd.readLine();
            rd.close();

            System.out.println(line);
            ObjectNode marginObject = (ObjectNode)AppConfig.mapper.readTree(line);
            ObjectNode tm = (ObjectNode)marginObject.get("total");
            totalMargin += tm.get("total").asDouble();
            System.out.println( totalMargin  );
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
