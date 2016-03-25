package com.punwire.kat.zerodha;

import java.time.LocalDate;
import java.util.Date;

/**
 * Created by kanwal on 25/03/16.
 */
public class ZdSplit {
    public String symbol;
    public LocalDate date;
    public double ratio;

    public ZdSplit(String symbol, LocalDate date, double ratio) {
        this.symbol = symbol;
        this.date = date;
        this.ratio = ratio;
    }
}
