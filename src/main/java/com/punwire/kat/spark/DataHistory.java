package com.punwire.kat.spark;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.HashMap;

/**
 * Created by kanwal on 22/03/16.
 */
public class DataHistory {
    private final String name;
    private final String colName;
    private final int length;
    private LimitedQueue<Double> queue ;
    public DataHistory(String name, String colName, int length) {
        this.name = name;
        this.colName = colName;
        this.length = length;
        queue = new LimitedQueue<Double>(length);
    }

    public void add(double value)
    {
        queue.add(value);
    }

    public String getColName() {
        return colName;
    }

    public Double[] get(){
        Double[] values = new Double[length];
        queue.toArray(values);
        return values;
    }

    public double[] convert(Double[] in){
        double[] out = new double[in.length];
        for(int i=0;i<in.length;i++)
        {
            out[i]=in[i];
        }
        return out;
    }

    public boolean isReady(){
        return queue.isFull();
    }

    public double sma()
    {
        Core c = new Core();
        double[] out = new double[length];
        MInteger outBegin = new MInteger();
        MInteger lengthBegin = new MInteger();
        double[] in = convert(get());
        RetCode retCode = c.sma(0, queue.size()- 1, in,20, outBegin, lengthBegin, out);
        return out[0];
    }
}
