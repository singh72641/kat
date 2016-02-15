package com.punwire.kat.zerodha;

import java.time.LocalDate;

/**
 * Created by Kanwal on 07/02/16.
 */
public class ZdVolatility {

    public final String symbol;
    public final LocalDate onDate;
    public final double price;
    public final double prevPrice;
    public final double currVol;
    public final double prevVol;
    public final double yearVol;

    public ZdVolatility(String symbol, LocalDate onDate, double price, double prevPrice, double currVol, double prevVol, double yearVol) {
        this.symbol = symbol;
        this.onDate = onDate;
        this.price = price;
        this.prevPrice = prevPrice;
        this.currVol = currVol;
        this.prevVol = prevVol;
        this.yearVol = yearVol;
    }

    public String toString()
    {
        return "VOL: " + symbol + " YearVol: " + yearVol +  " CurrVol: " + currVol;
    }
}
