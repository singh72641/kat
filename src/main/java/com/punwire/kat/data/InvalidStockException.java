package com.punwire.kat.data;

public class InvalidStockException extends Exception
{

    public InvalidStockException()
    {
        super("The symbol is invalid. Please enter a valid symbol.");
    }

    @Override public String toString()
    {
        return "The symbol is invalid. Please enter a valid symbol.";
    }

}
