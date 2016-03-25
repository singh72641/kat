package com.punwire.kat.spark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by kanwal on 22/03/16.
 */
public abstract class Algorithm {
    AlgoContext context;
    Portfolio portfolio;
    TimeSeries result;
    Consumer<TimeSeries> resultListener;
    Consumer<JsonNode> eventListener;
    DataProvider dataProvider = new DataProvider();

    public Algorithm(){
        result = new TimeSeries();
        portfolio = new Portfolio(dataProvider);
        portfolio.addTradeListener(this::onTrade);
        context = new AlgoContext();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public double getDouble(String symbol, Instant instant, String name)
    {
        return dataProvider.get(symbol).getDouble(instant,name);
    }

    public DataHistory getHistory(String symbol, String name)
    {
        return dataProvider.getHistory(symbol,name);
    }

    public abstract void initialize();

    public abstract void onTrade(Trade trade);

    public void onData(Instant point){
        portfolio.update(point);
    }

    public void dump() {
        this.result.dump();
    }

    public void addResultListener(Consumer<TimeSeries> result){
        resultListener = result;
    }

    public void addEventListener(Consumer<JsonNode> event){
        eventListener = event;
    }

    public void run()
    {
        initialize();
        ObjectNode res = AppConfig.objectNode();
        res.put("event","START");
        eventListener.accept(res);

        dataProvider.stream(this::onData);

        res = AppConfig.objectNode();
        res.put("event","END");
        eventListener.accept(res);
    }
}
