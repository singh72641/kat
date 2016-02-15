package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Bar;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdSymbol;
import com.punwire.kat.zerodha.ZdVolatility;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Kanwal on 13/02/16.
 */
public class OptionClassAction {
    static NseLoader loader = new NseLoader();

    public static ObjectNode sendOptionChain(ObjectNode msg)
    {
        String symbol = msg.get("symbol").asText();
        System.out.println(symbol);

        ObjectNode result = AppConfig.mapper.createObjectNode();
        //Send Option Chain

        //List<ZdOptionChain> options = AppConfig.db.geteOptionChain(symbol);

        List<ZdOptionChain> options = loader.testDirect(symbol);

        Bar bar = AppConfig.db.getEod(symbol);
        ArrayNode data = AppConfig.mapper.createArrayNode();
        ObjectNode optionData = AppConfig.mapper.createObjectNode();
        ObjectNode eventData = AppConfig.mapper.createObjectNode();
        double currPrice= bar.getLast() ;
        double prevStrike=0;
        double optStep=0;
        LocalDate expiryDate = LocalDate.now();
        LocalDate onDate = LocalDate.now();
        System.out.println("CurrPrice to check ITM and OTM " + currPrice);
        for(ZdOptionChain option: options){
            //option.greeks(0.0735,option.asOfDate);
            ObjectNode on = option.toJson();
            Double strike = on.get("strike").asDouble();
            if( prevStrike == 0 ) prevStrike = strike;
            else {
                optStep = strike - prevStrike;
                prevStrike = strike;
            }

            if(  currPrice < strike && currPrice > (strike - optStep) ) {
                on.put("call_atm",1);
            }
            else {
                on.put("call_atm",0);
            }

            if(  currPrice > strike && currPrice < (strike + optStep) )  {
                on.put("put_atm",1);
            }
            else {
                on.put("put_atm", 0);
            }


            data.add( on );
            expiryDate = option.expiryDate;
            onDate = option.asOfDate;
        }
        ZdVolatility vol = AppConfig.db.getVol(symbol, onDate);

        double ivRank = 0.25;
        if( !symbol.equals("NIFTY") && !symbol.equals("BANKNIFTY") )  ivRank =  AppConfig.db.getIvRank(symbol, onDate);
        System.out.println(vol.toString());

        Duration d = Duration.between(LocalDate.now().atStartOfDay(), expiryDate.atStartOfDay());
        ZdSymbol lot = AppConfig.db.getLot(symbol);

        optionData.put("oc",data);
        optionData.put("symbol",symbol);
        optionData.put("name",lot.name);
        optionData.put("lot_size",lot.lotSize);
        optionData.put("ivrank", NumberUtil.round(ivRank, 2));
        optionData.put("date", DateUtil.intDate(bar.getStart()) );
        optionData.put("printdate", NseLoader.printDate.format(expiryDate));
        optionData.put("days", d.toDays());
        optionData.put("vol", vol.yearVol * 100);
        optionData.put("O", bar.getO() );
        optionData.put("H", bar.getH() );
        optionData.put("L", bar.getL() );
        optionData.put("C", bar.getC() );
        optionData.put("last", bar.getLast() );
        optionData.put("prev", bar.getPrev() );
        double change = (bar.getLast() - bar.getPrev());

        optionData.put("change", change );
        optionData.put("V", bar.getV() );
        result.put("data", optionData);
        result.put("event", "oc");
        return result;
    }
}
