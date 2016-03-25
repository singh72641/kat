package com.punwire.kat.spark;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.punwire.kat.core.AppConfig;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Created by kanwal on 23/03/16.
 */
public class Portfolio {
    TreeMap<String, Position> positions = new TreeMap<String, Position>();
    TreeMap<String, ArrayList<Trade>> trades = new TreeMap<>();
    private double cash=1000000.00;
    private double currentValue=0.00;
    private DataProvider data;
    private Consumer<Trade> tradeListener;

    public Portfolio(DataProvider data)
    {
        this.data = data;
        currentValue = cash;
    }

    public int getOpenQty(String symbol)
    {
        if( ! positions.containsKey(symbol)) return 0;
        return positions.get(symbol).getQuantity();
    }

    public void addTradeListener(Consumer<Trade> tradeListener){
        this.tradeListener = tradeListener;
    }

    public void addTrade(Trade trade)
    {
        if( ! trades.containsKey(trade.getSymbol())) trades.put(trade.getSymbol(),new ArrayList<>());
        trades.get(trade.getSymbol()).add(trade);
        cash -= trade.getQty() * trade.getPrice();
        tradeListener.accept(trade);
        //dump();
    }

    public void update(Instant point){
        currentValue = cash;
        for(Position position: positions.values())
        {
            TimeSeries ts = data.get(position.getSymbol());
            double price = ts.getDouble(point,"close");
            position.update(price);
            currentValue += position.getMarketValue();
        }
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void orderTarget(String symbol, Instant instant, int qty)
    {
        TimeSeries ts = data.get(symbol);
        double price = ts.getDouble(instant,"close");
        if( positions.containsKey(symbol))
        {
            //Already have a position
            Position pos = positions.get(symbol);
            if( pos.getQuantity() != qty )
            {
                //We have to close some positions
                Trade trade = new Trade(this, instant,symbol,(qty - pos.getQuantity()), price);
                pos.addTrade(trade);
                addTrade(trade);
            }
        }
        else {
            Position pos = new Position(symbol,this);
            positions.put(symbol,pos);
            Trade trade = new Trade(this, instant,symbol,(qty - pos.getQuantity()), price);
            pos.addTrade(trade);
            addTrade(trade);

        }
    }

    public void dump()
    {
        System.out.println("Portfolio");
        System.out.println("=====================================");
        System.out.println("Cash:   " + cash);
        for(Position position: positions.values())
        {
            position.dump();
        }
    }

    public static void main(String[] args){

        try {
            MongoCollection col = AppConfig.db.database.getCollection("Eod");
            MongoCursor<Document> cursor = col.distinct("symbol", String.class).iterator();
            while(cursor.hasNext())
            {
                Document row = cursor.next();
                System.out.println(row.getString("symbol"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
