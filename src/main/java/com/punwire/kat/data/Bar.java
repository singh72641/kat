package com.punwire.kat.data;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by Kanwal on 13/01/16.
 */
public class Bar {
    private String symbol;
    private double O;
    private double H;
    private double L;
    private double C;
    private double last;
    private double prev;
    private long V;
    private long OI;
    private long start;
    private int duration;

    public Bar(String symbol, long start, int duration, double o, double h, double l, double c, long v, long OI) {
        this.symbol = symbol;
        this.start = start;
        this.duration = duration;
        O = o;
        H = h;
        L = l;
        C = c;
        V = v;
        this.OI = OI;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getO() {
        return O;
    }

    public double getH() {
        return H;
    }

    public double getL() {
        return L;
    }

    public double getC() {
        return C;
    }

    public long getV() {
        return V;
    }

    public long getOI() {
        return OI;
    }

    public long getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public double getAverage() {
        return (H + L) / 2;
    }

    public void setO(double o) {
        O = o;
    }

    public void setH(double h) {
        H = h;
    }

    public void setL(double l) {
        L = l;
    }

    public void setC(double c) {
        C = c;
    }

    public void setV(long v) {
        V = v;
    }

    public void setOI(long OI) {
        this.OI = OI;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(start + ",");
        sb.append(O + ",");
        sb.append(H + ",");
        sb.append(L + ",");
        sb.append(C + ",");
        sb.append(V + ",");
        sb.append(OI);
        return sb.toString();
    }

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public double getPrev() {
        return prev;
    }

    public void setPrev(double prev) {
        this.prev = prev;
    }
}
