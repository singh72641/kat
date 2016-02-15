
package com.punwire.kat.core;

import com.jacob.com.ComThread;
import com.punwire.kat.ami.JAmiBroker;
import com.punwire.kat.data.*;
import com.punwire.kat.zerodha.ZdOptionMapper;
import org.bson.Document;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Created by Kanwal on 13/01/16.
 */
public class KTest {

    public void uiDefault() {
        Hashtable defaults = UIManager.getDefaults();
        Enumeration keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            System.out.println("key: " + key + "    >    "   +   UIManager.get(key) );
        }
    }

    public void getAmi()
    {
        try {
            MongoDb db = new MongoDb();

            ComThread.InitMTA(); // Initialize the current java thread to be part of the Multi-threaded COM Apartment

            // Initialize AmiBroker
            JAmiBroker ab = JAmiBroker.getInstance();
            if(ab.getVisible() > 0) {
                System.out.println("AmiBroker is not running");
            };

            System.out.println( ab.getDatabasePath());
            // Load our database
            //boolean result = ab.loadDatabase("C:/Program Files (x86)/AmiBroker/BestRT");
            //if(! result) {
            //    System.out.println("Unable to open AmiBroker database");
            //};

            //ab.getAnalysisDocs();

//            JAmiBroker.Documents docs = ab.getDocuments();
//            JAmiBroker.Documents.Document doc = docs.getActiveDocument();
//            docs.open("NIFTY-I");
//            System.out.println("Name: " + doc.getName());
//
            JAmiBroker.Stocks stocks = ab.getStocks();
            String[] s = stocks.getTickerList(1);
            for(int s1=0; s1<s.length; s1++) {
                String symbol = s[s1];
                System.out.println("Processing Symbol " + symbol);
                JAmiBroker.Stocks.Stock stock = stocks.item(symbol);
                Stock oaStock = new Stock(symbol,"NSE");

//                long lastTime = db.getLastDate(oaStock);
//                if( lastTime > 0 ) continue;
                if( ! db.stockExists(oaStock)) {
                    //db.removeSymbol(symbol);
                    db.saveStock(oaStock);
                }
                else continue;

                long firstDate = db.getFirstDate(oaStock);
                long lastDate = db.getLastDate(oaStock);


                JAmiBroker.Stocks.Stock.Quotations quotes = stock.getQuotations();


                System.out.println("+++++++ Preparing Bulk Request for Symbol " + symbol);


                //front fill
                List<Bar> bars = new ArrayList<>();
                int ld = 0;
//                for(int i=(quotes.count() - 1);i >= 0 ;i--)
//                {
//                    JAmiBroker.Stocks.Stock.Quotations.Quotation q = quotes.item(i);
//                    //System.out.println(q.getDate().toString());
//                    LocalDateTime dt = LocalDateTime.ofInstant(q.getDate().toInstant(), ZoneOffset.ofHoursMinutes(5, 30));
//                    //System.out.println(dt.toString());
//                    Float v = q.getVolume();
//                    Float oi = q.getVolume();
//                    long cTime = DateUtil.longDateTime(dt);
//                    int cLastDate = DateUtil.intDate(dt);
//
////                    if( ld != cLastDate )
////                    {
////                        ld = cLastDate;
////                        if( bars.size() > 2000) break;
////                        System.out.println("Loading date " + cLastDate);
////                    }
//
//                    if ( cTime <= lastDate ) break;
//                    //System.out.println(symbol + "  " + cTime);
//                    Bar bar = new Bar(symbol, DateUtil.longDateTime(dt),1,q.getOpen(),q.getHigh(),q.getLow(),q.getClose(),v.longValue(),oi.longValue());
//                    bars.add(bar);
//                }

                //back fill
                ld = 0;
                for(int i=0;i < quotes.count() ;i++)
                {
                    JAmiBroker.Stocks.Stock.Quotations.Quotation q = quotes.item(i);
                    //System.out.println(q.getDate().toString());
                    LocalDateTime dt = LocalDateTime.ofInstant(q.getDate().toInstant(), ZoneOffset.ofHoursMinutes(5, 30));
                    //System.out.println(dt.toString());
                    Float v = q.getVolume();
                    Float oi = q.getVolume();
                    int cLastDate = DateUtil.intDate(dt);
                    long cTime = DateUtil.longDateTime(dt);
                    //if ( cTime <= lastDate ) continue;

                    if( ld != cLastDate )
                    {
                        ld = cLastDate;
                        if( bars.size() > 2000) break;
                        System.out.println("Loading date " + cLastDate);
                    }

                    //System.out.println(symbol + "  " + cTime);
                    Bar bar = new Bar(symbol, DateUtil.longDateTime(dt),1,q.getOpen(),q.getHigh(),q.getLow(),q.getClose(),v.longValue(),oi.longValue());
                    bars.add(bar);
                }

                System.out.println("++++++ Done Bulk Request for Symbol " + symbol + " size: " + bars.size());
                db.saveBars(symbol,bars);
                System.out.println("++++++ Done Bulk Request for Symbol " + symbol);
            }





            //String[] s = stocks.getTickerList();

            //JAmiBroker.AnalysisDocs.AnalysisDoc doc = docs.add();

            //JAmiBroker.Stocks stocks = ab.getStocks();



            //NewA = ab.AnalysisDocs.Open( "C:\\analysis1.apx" );

            // Import quotes
//            int res = ab.importFile(0, "d:\\tickdata\\WIG.mst", "mst.format"); // import quotes
//            if(res != 0) {
//                System.out.println("FAILED");
//            } else {
//                System.out.println("DONE");
//            }

            // Save Amibroker database
            ab.refreshAll();
//            ab.saveDatabase();
        } finally {
            ComThread.Release(); // release this java thread from COM
        }
    }

    public static void main(String[] args){

        try {
//            CsvLoader loader = new CsvLoader();
//            loader.load(true);
              NseLoader loader = new NseLoader();
//              loader.load(LocalDate.of(2016,1,20));
              //loader.processFile(LocalDate.of(2016, 1, 22));
//              loader.processFile();
//                MongoDb db = new MongoDb();
//             List<Option> options = db.getOptions("OPTSTK","RELIANCE");
//            System.out.println(options.size());

//            KTest t = new KTest();
//            t.getAmi();
            //t.uiDefault();
//            System.out.println("Fetched");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
