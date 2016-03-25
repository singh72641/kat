package com.punwire.kat.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Bar;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionList;
import com.punwire.kat.zerodha.ZdSymbol;
import com.punwire.kat.zerodha.ZdVolatility;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Kanwal on 13/02/16.
 */
public class OptionClassAction {
    static NseLoader loader = new NseLoader();

    public static ObjectNode sendDates()
    {
        TreeMap<String,LocalDate> dates =  AppConfig.db.getExpDates();
        ArrayNode aNode = AppConfig.arrayNode();
        for(LocalDate date: dates.values())
        {
            aNode.add( date.format(NseLoader.ddMMMyyyy).toUpperCase() );
        }
        ObjectNode result = AppConfig.objectNode();
        System.out.println("Defautl Date: " + aNode.get(0).asText());
        result.put("data", aNode);
        result.put("event", "ExpDates");
        return result;
    }
    public static ObjectNode sendOptionChain(String symbol, String expMonth)
    {
        System.out.println("+++++++++++ Month: " + expMonth);
        System.out.println(symbol);

        ObjectNode result = AppConfig.mapper.createObjectNode();

        Bar bar = AppConfig.db.getEod(symbol);
        ArrayNode data = AppConfig.mapper.createArrayNode();
        ObjectNode optionData = AppConfig.mapper.createObjectNode();
        ObjectNode eventData = AppConfig.mapper.createObjectNode();

        ZdOptionList optionList = AppConfig.store.get(symbol);
        data = optionList.toJson(expMonth);

        double currPrice= bar.getLast() ;
        ZdVolatility vol = AppConfig.db.getVol(symbol, optionList.onDate);

        double ivRank = 0.19;
        if( !symbol.equals("NIFTY") && !symbol.equals("BANKNIFTY") )  ivRank =  AppConfig.db.getIvRank(symbol, optionList.onDate);
        System.out.println(vol.toString());
        double yearVol = optionList.getVolatility();
        Duration d = Duration.between(LocalDate.now().atStartOfDay(), optionList.expiryDate.atStartOfDay());
        ZdSymbol lot = AppConfig.db.getLot(symbol);

        optionData.put("oc",data);
        optionData.put("symbol",symbol);
        optionData.put("name",lot.name);
        optionData.put("lot_size",lot.lotSize);
        optionData.put("ivrank", NumberUtil.round(ivRank, 2));
        optionData.put("date", DateUtil.intDate(bar.getStart()) );
        optionData.put("printdate", NseLoader.printDate.format(optionList.expiryDate));
        optionData.put("days", d.toDays());

        //(Stock price) x (Annualized Implied Volatility) x (Square Root of [days to expiration / 365]) = 1
        double daysf = d.toDays() / 365.00;
        double expect1 = currPrice * (vol.yearVol ) * Math.sqrt( daysf );
        optionData.put("vol", NumberUtil.round(yearVol ,2));
        optionData.put("O", bar.getO() );
        optionData.put("H", bar.getH() );
        optionData.put("L", bar.getL() );
        optionData.put("C", currPrice );
        optionData.put("last", currPrice );
        optionData.put("expected1",  NumberUtil.round(expect1,2) );
        optionData.put("prev", bar.getPrev() );
        double change = (bar.getLast() - bar.getPrev());

        optionData.put("change", change );
        optionData.put("V", bar.getV() );
        result.put("data", optionData);
        result.put("event", "oc");

        System.out.println(result.toString());
        return result;
    }
}
