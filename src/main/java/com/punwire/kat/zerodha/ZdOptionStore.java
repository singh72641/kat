package com.punwire.kat.zerodha;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by kanwal on 2/26/2016.
 */
public class ZdOptionStore {
    HashMap<String,ZdOptionList> data = new HashMap<>();
    LocalDate onDate=LocalDate.now();

    public void load(String symbol, String expMonth) {

        if( ! data.containsKey(symbol) )
        {
            ZdOptionList ol = new ZdOptionList(symbol);
            data.put(symbol,ol);
        }

        ZdOptionList ol = data.get(symbol);
        ol.load(expMonth);
        onDate = ol.onDate;
    }

    public void load(String symbol) {

        if( ! data.containsKey(symbol) )
        {
            ZdOptionList ol = new ZdOptionList(symbol);
            data.put(symbol,ol);
        }

        ZdOptionList ol = data.get(symbol);
        onDate = ol.onDate;
    }

    public ZdOptionList  get(String symbol) {
        if( ! data.containsKey(symbol) ) load(symbol);
        return data.get(symbol);

    }
}
