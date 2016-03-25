package com.punwire.kat.spark;

import com.punwire.kat.core.AppConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by kanwal on 22/03/16.
 */
public class DataProvider {
    HashMap<String, TimeSeries> data = new HashMap<>();
    HashMap<String, HashMap<String, DataHistory>> histories = new HashMap<>();

    public DataProvider(){

    }

    public void addHistory(String name, String col, int length){
        for(String symbol: data.keySet()) {
            if( ! histories.containsKey( symbol)) histories.put(symbol, new HashMap<>());
            HashMap<String, DataHistory> history = histories.get(symbol);
            if( ! history.containsKey(name)) {
                DataHistory historyQueue = new DataHistory(name, col, length);
                history.put(name, historyQueue);
            }
        }
    }
    public DataHistory getHistory(String symbol, String name){
        return histories.get(symbol).get(name);
    }

    public void addSecurity(String symbol){
        TimeSeries ts = AppConfig.db.getTimeSeries(symbol);
        data.put(symbol,ts);
    }

    public TimeSeries get(String symbol){
        return data.get(symbol);
    }

    public void stream(Consumer<Instant> consumer)
    {
        String firstSymbol = data.keySet().iterator().next();
        ArrayList<Instant> p = data.get(firstSymbol).points;
        Collections.sort(p);
        for(Instant point: p)
        {
            //Load the history with data
            for(String symbol: histories.keySet())
            {
                TimeSeries ts = data.get(symbol);
                HashMap<String, DataHistory> history = histories.get(symbol);
                for(DataHistory historyQueue: history.values() ) {
                    historyQueue.add(ts.getDouble(point, "close"));
                }
            }
            consumer.accept(point);
        }
    }
}
