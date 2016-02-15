package com.punwire.kat.data;

public class StockNotFoundException extends Exception
{

    public StockNotFoundException()
    {
        super("Stock not found.");
    }


    @Override public String toString()
    {
        return "Stock not found.";
    }

}
