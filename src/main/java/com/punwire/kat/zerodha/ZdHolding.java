package com.punwire.kat.zerodha;

/**
 * Created by Kanwal on 02/02/16.
 */
public class ZdHolding {
    public String symbol;
    public String underline;
    public String type;
    public int qty;
    public double avgPrice;
    public double currentPrice;
    public double currentPnL;

    public ZdHolding(String s, String underline, String type, int q, double avgPrice, double currentPrice, double currentPnL)
    {
        this.symbol = s;
        this.underline = underline;
        this.type = type;
        this.qty = q;
        this.avgPrice = avgPrice;
        this.currentPnL = currentPnL;
        this.currentPrice = currentPrice;
    }

    public ZdHolding(String s, String type, int q, double avgPrice, double currentPrice, double currentPnL)
    {
        this.symbol = s;
        this.underline = s;
        this.type = type;
        this.qty = q;
        this.avgPrice = avgPrice;
        this.currentPnL = currentPnL;
        this.currentPrice = currentPrice;
    }

    public String toString()
    {
        return symbol + " (" + type + ") " + qty + "@" + avgPrice + "  ->  " + currentPnL + "@" + currentPrice;
    }
}
