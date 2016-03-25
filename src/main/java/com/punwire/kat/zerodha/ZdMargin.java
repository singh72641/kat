package com.punwire.kat.zerodha;

/**
 * Created by kanwal on 2/18/2016.
 */
public class ZdMargin {
    public String exchange;
    public String product;
    public String symbol;
    public String optType;
    public int strike;
    public int qty;
    public String trade="sell";

    public ZdMargin(String exchange, String product, String symbol, String optType, int strike, int qty, String trade) {
        this.exchange = exchange;
        this.product = product;
        this.symbol = symbol;
        this.optType = optType;
        this.strike = strike;
        this.qty = qty;
        this.trade = trade;
    }
}
