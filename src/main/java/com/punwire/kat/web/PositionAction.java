package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.zerodha.ZdHolding;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by kanwal on 2/19/2016.
 */
public class PositionAction {

    public static ObjectNode sendPositions()
    {
        ArrayNode finalList = AppConfig.arrayNode();
        List<String> list = AppConfig.db.getHoldingUnderlines("NFO");
        TreeMap<String, LocalDate> dates = AppConfig.db.getExpDates();
        for(String underline: list)
        {
            ObjectNode uNode = AppConfig.objectNode();
            uNode.put("symbol",underline);
            //Find Trades for the underline
            List<ZdHolding> holdings = AppConfig.db.getHolding("NFO", underline);
            ArrayNode posList = AppConfig.arrayNode();

            for(ZdHolding holding: holdings){
                String expMonth = ZdOptionMapper.getExpMonth(holding.symbol);
                Double strike = Double.valueOf(ZdOptionMapper.getStrike(holding.symbol));
                String optType = ZdOptionMapper.getOptType(holding.symbol);

                LocalDate lDate = AppConfig.db.getExpDates(expMonth);
                String expDay = lDate.format(NseLoader.yyMMM);
                ObjectNode o = holding.toJson();
                Duration dd = Duration.between(LocalDate.now().atStartOfDay(), lDate.atStartOfDay());
                o.put("expMonth", expMonth);
                o.put("expDay", expDay);
                o.put("days", dd.toDays());
                posList.add(o);
            }
            uNode.put("positions",posList);
            uNode.put("pcount",posList.size());
            finalList.add(uNode);
        }

        ObjectNode result = AppConfig.objectNode();
        result.put("data", finalList);
        result.put("event", "Positions");
        System.out.println(result.toString());
        return result;
    }
}
