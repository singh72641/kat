package com.punwire.kat.zerodha;

/**
 * Created by Kanwal on 13/02/16.
 */
public class ZdSymbol {
    public String name;
    public String symbol;
    public int lotSize;

    public ZdSymbol(String name, String symbol, int lotSize)
    {
        this.name = name;
        this.lotSize = lotSize;
        this.symbol = symbol;
    }

}
