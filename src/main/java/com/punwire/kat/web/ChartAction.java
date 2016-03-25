package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Bar;
import com.punwire.kat.trade.Trade;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionList;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Kanwal on 13/02/16.
 */
public class ChartAction {


    public static ObjectNode sendChart(Trade trade) {
//        double currPrice1 = bar.getC();
//        double stdVal1 = ((currPrice1 * vol.yearVol) * Math.sqrt(d.toDays()))/(Math.sqrt(365));
//        System.out.println(vol.yearVol);
//        System.out.println("Standard Deviation1: " + (currPrice1 - stdVal1));
////        System.out.println("Standard Deviation1: " + (currPrice1 + stdVal1));

        Bar bar = AppConfig.db.getEod(trade.symbol);
        double currPrice = trade.underlinePrice;
        double upperLimit = currPrice;
        double incr = 0.5;
        if( currPrice > 1000 ) incr = 1;
        if( currPrice > 5000 ) incr = 5;

        ZdOptionList optoinList = AppConfig.store.get(trade.symbol);
        double expectedMove = optoinList.getExpectedMove();



//        double daysf = d.toDays() / 365.00;
//        double expect1 = currPrice * (vol.yearVol ) * Math.sqrt( daysf );

        double maxStrike = trade.getMaxStrike();
        double minStrike = trade.getMinStrike();

        if( upperLimit > maxStrike )
        {
            System.out.println("Upprt limit add: " + (15* incr));
            upperLimit += 15* incr;
        }
        else {
            System.out.println("Upprt limit add: " + (15* incr));
            upperLimit = (maxStrike + 15*incr);
        }

        System.out.println("CurrPrice: " + currPrice);
        System.out.println("UpperPrice: " + upperLimit);
        System.out.println("MinStrike: " + minStrike);
        System.out.println("MaxStrike: " + maxStrike);
        System.out.println("Incr: " + incr);


        double lowerBound = currPrice - expectedMove;
        double uppperBound = currPrice + expectedMove;
        ArrayNode chartData = AppConfig.mapper.createArrayNode();
        System.out.println("Expected Move: " + expectedMove);
        System.out.println("Lower Bound: " + lowerBound);
        System.out.println("Upper Bound: " + uppperBound);
        ObjectNode chart = AppConfig.objectNode();
        double maxValue=0.0;
        double minValue=minStrike - (15.0*incr);
        for( double pp = minValue;pp< upperLimit; pp=pp+incr){
            double pnl1 = trade.getPnLAtExpiry(pp);
            maxValue = pp;
            ObjectNode chartNode = AppConfig.mapper.createObjectNode();
            if( currPrice >= pp && currPrice < pp + incr)
            {
                chartNode.put("bullet","diamond");
                System.out.println("Adding Bullet at " + pp);
            }

            if( lowerBound >= pp && lowerBound < pp + incr)
            {
                chart.put("guide1", pp);
                System.out.println("Adding Bullet at " + lowerBound);
            }

            if( uppperBound >= pp && uppperBound < pp + incr)
            {
                chart.put("guide2", pp);
                System.out.println("Adding Bullet at " + uppperBound);
            }

            chartNode.put("price",pp);
            chartNode.put("value", NumberUtil.round(pnl1, 2));
            chartNode.put("std",0.4);
            chartData.add(chartNode);
        }


        chart.put("chartData",chartData);
        chart.put("minValue",minValue);
        chart.put("maxValue",maxValue);
        ObjectNode result = AppConfig.mapper.createObjectNode();
        result.put("data", chart);
        result.put("event", "chartdata");

        System.out.println(result);

        return result;
    }

    public static ObjectNode sendOiChart(String symbol, String expMonth) {

        ZdOptionList optionList = AppConfig.store.get(symbol);
        TreeMap<Double,ZdOptionChain> oc = optionList.get(expMonth);

        ArrayNode chartData = AppConfig.mapper.createArrayNode();
        for(Double strike: oc.keySet())
        {
            ZdOptionChain optionChain = oc.get(strike);
            ObjectNode chartNode = AppConfig.mapper.createObjectNode();

            chartNode.put("strike",strike);
            chartNode.put("oi_call", NumberUtil.round(optionChain.call.getOpenInterest()/1000, 2));
            chartNode.put("oi_put", NumberUtil.round(optionChain.put.getOpenInterest()/1000, 2));
            chartNode.put("oic_call", NumberUtil.round(optionChain.call.getOiChange()/1000, 2));
            chartNode.put("oic_put", NumberUtil.round(optionChain.put.getOiChange()/1000, 2));
            chartData.add(chartNode);
        }
        ObjectNode result = AppConfig.mapper.createObjectNode();
        result.put("data", chartData);
        result.put("event", "oidata");
        return result;
    }

}
