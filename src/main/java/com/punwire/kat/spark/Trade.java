package com.punwire.kat.spark;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.NseLoader;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Created by kanwal on 23/03/16.
 */
public class Trade {
    private final Portfolio portfolio;
    private final Instant time;
    private final String symbol;
    private final int qty;
    private final double price;

    public Trade(Portfolio portfolio, Instant time, String symbol, int qty, double price) {
        this.portfolio = portfolio;
        this.time = time;
        this.symbol = symbol;
        this.qty = qty;
        this.price = price;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Instant getTime() {
        return time;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQty() {
        return qty;
    }

    public double getPrice() {
        return price;
    }

    public ObjectNode toJson(){
        ObjectNode result = AppConfig.objectNode();
        result.put("symbol",symbol);
        result.put("date", time.atZone(ZoneId.systemDefault()).format(NseLoader.ddMMyyyy));
        result.put("qty",qty);
        result.put("price",price);
        return result;
    }
}
