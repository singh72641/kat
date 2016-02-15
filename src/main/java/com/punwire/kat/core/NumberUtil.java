package com.punwire.kat.core;

/**
 * Created by Kanwal on 12/02/16.
 */
public class NumberUtil {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
