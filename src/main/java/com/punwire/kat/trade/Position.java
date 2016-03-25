package com.punwire.kat.trade;

import java.time.LocalDate;

/**
 * Created by Kanwal on 12/02/16.
 */
public class Position {
    int id;
    int qty;
    double avgPrice;
    public double currPrice;
    LocalDate openDate;
    String symbol;

    public double getPnLAt(double price)
    {
        return 0.00;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
