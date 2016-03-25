package com.punwire.kat.zerodha;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.data.NseLoader;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by kanwal on 2/26/2016.
 */
public class ZdOptionList {
    public String symbol;
    TreeMap<Integer,TreeMap<Double, ZdOptionChain>> data = new TreeMap<>();
    public LocalDate onDate=LocalDate.now();
    public LocalDate expiryDate=LocalDate.now();
    public double currentPrice=0.0;

    public ZdOptionList(String symbol)
    {
        this.symbol = symbol;
    }

    public TreeMap<Double, ZdOptionChain> get(String expMonth) {
        LocalDate ld = LocalDate.parse(expMonth, NseLoader.ddMMMyyyy);
        System.out.println("ZdOptonList Get" + ld.toString());
        if( ! data.containsKey(DateUtil.intDate(ld)))
        {
            load(expMonth);
        }
        return data.get(DateUtil.intDate(ld));
    }

    public ZdOptionChain get(String expMonth, Double strike) {
        LocalDate ld = LocalDate.parse(expMonth, NseLoader.ddMMMyyyy);
        System.out.println("ZdOptonList Get" + ld.toString());

        if( ! data.containsKey(DateUtil.intDate(ld)))
        {
            load(expMonth);
        }
        return data.get(DateUtil.intDate(ld)).get(strike);
    }

    public double getVolatility()
    {
        TreeMap<Double, ZdOptionChain> oc = data.firstEntry().getValue();
        //For Nifty Get Vol from
        double hStrike = oc.lowerKey(currentPrice);
        double v1 = oc.get(hStrike).call.getVolatility();
        double v2 = oc.get(hStrike).put.getVolatility();
        System.out.println("Getting Vol from strike: " + hStrike + "  Vol: " + ((v1+v2)/2.0));
        return ((v1+v2)/2.0);
    }

    public double getExpectedMove()
    {
        TreeMap<Double, ZdOptionChain> oc = data.firstEntry().getValue();

        //For Nifty Get Vol from
        double hStrike = oc.lowerKey(currentPrice);
        LocalDate expDate = oc.get(hStrike).call.getExpiryDate();

        double v1 = oc.get(hStrike).call.getVolatility();
        double v2 = oc.get(hStrike).put.getVolatility();
        double vol = ((v1+v2)/2.0);

        Duration d = Duration.between(LocalDate.now().atStartOfDay(), expDate.atStartOfDay());

        System.out.println("Expected Move: Vol: " + vol);
        double daysf = d.toDays() / 365.00;
        double expect1 = currentPrice * (vol/100.00 ) * Math.sqrt( daysf );

        return expect1;
    }

    public void load(String expMonth) {

        LocalDate ld = LocalDate.parse(expMonth, NseLoader.ddMMMyyyy);
        System.out.println("ZdOptonList Get" + ld.toString());

        System.out.println("Getting option chain from net");
        TreeMap<Double, ZdOptionChain> options = AppConfig.loader.downloadOptionChain(symbol,expMonth);
        System.out.println("Adding ATM flag to Options");
        double currPrice = options.firstEntry().getValue().underlinePrice;

        double lowerStrike = options.lowerKey(currPrice);
        double upperStrike = options.higherKey(currPrice);

        ZdOptionChain oc = options.get(lowerStrike);
        oc.call.atm = true;
        oc = options.get(upperStrike);
        oc.put.atm = true;
        System.out.println("CurrPrice to check ITM and OTM " + currPrice + "  LowerStrike: " + lowerStrike + "  UpperSTrike: " + upperStrike);
        data.put(DateUtil.intDate(ld),options);
        onDate = oc.asOfDate;
        expiryDate = oc.expiryDate;
        currentPrice = oc.underlinePrice;
    }

    public ArrayNode toJson(String expMonth){
        LocalDate ld = LocalDate.parse(expMonth, NseLoader.ddMMMyyyy);

        if( ! data.containsKey(DateUtil.intDate(ld)) ) load(expMonth);

        TreeMap<Double, ZdOptionChain> ocList = data.get(DateUtil.intDate(ld));
        ArrayNode list = AppConfig.arrayNode();
        for(ZdOptionChain oc: ocList.values())
        {
            list.add(oc.toJson());
        }
        return list;
    }
}
