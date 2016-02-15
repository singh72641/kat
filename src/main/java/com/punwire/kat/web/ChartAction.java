package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Bar;
import com.punwire.kat.trade.Trade;

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

        double maxStrike = trade.getMaxStrike();
        double minStrike = trade.getMinStrike();

        if( upperLimit > maxStrike )
        {
            System.out.println("Upprt limit add: " + (20* incr));
            upperLimit += 20* incr;
        }
        else {
            System.out.println("Upprt limit add: " + (20* incr));
            upperLimit = (maxStrike + 20*incr);
        }

        System.out.println("CurrPrice: " + currPrice);
        System.out.println("UpperPrice: " + upperLimit);
        System.out.println("MinStrike: " + minStrike);
        System.out.println("MaxStrike: " + maxStrike);
        System.out.println("Incr: " + incr);

        ArrayNode chartData = AppConfig.mapper.createArrayNode();
        for( double pp = minStrike - (20.0*incr);pp< upperLimit; pp=pp+incr){
            double pnl1 = trade.getPnLAtExpiry(pp);
            ObjectNode chartNode = AppConfig.mapper.createObjectNode();
            if( currPrice >= pp && currPrice < pp + incr)
            {
                chartNode.put("bullet","diamond");
                System.out.println("Adding Bullet at " + pp);
            }

            chartNode.put("price",pp);
            chartNode.put("value", NumberUtil.round(pnl1, 2));
            chartNode.put("std",0.4);
            chartData.add(chartNode);
        }

        ObjectNode result = AppConfig.mapper.createObjectNode();
        result.put("data", chartData);
        result.put("event", "chartdata");
        return result;
    }
}
