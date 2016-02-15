package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.trade.Trade;
import com.punwire.kat.zerodha.ZdHolding;
import com.punwire.kat.zerodha.ZdOptionMapper;
import org.sikuli.script.App;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Kanwal on 13/02/16.
 */
public class TradeAction {

    public static ObjectNode sendTrade(Trade trade) {
        System.out.println("Sending Trade..");
        ////Trade trade = Trade.shortPutSpread("RELIANCE",840,860,500);
        //Trade trade = Trade.shortPutSpread("NIFTY",6800,6900,150);
        ObjectNode result = AppConfig.mapper.createObjectNode();
        result.put("data", trade.toJson());
        result.put("event", "trade");
        System.out.println(result.toString());
        return result;
    }

    public static Trade fetchTrade(String underline) {
        System.out.println("Fetching Trade for " + underline);
        List<ZdHolding> holdings = AppConfig.db.getHolding("NFO", underline);
        if( holdings.size() == 0 ) return null;
        Trade trade = new Trade(underline, "Zerodha", LocalDate.now());
        for(ZdHolding holding: holdings)
        {
            String optType = holding.symbol.endsWith("CE")? "CE" : "PE";
            String sym = holding.symbol.substring(0, (holding.symbol.length()-2));
            String s = ZdOptionMapper.onlyLastNum(sym);
            Double strikePrice = Double.valueOf(s);
            trade.addPositionQty(underline, strikePrice,optType,holding.qty,holding.avgPrice);
        }
        return trade;
    }

    public static ObjectNode addPosition(ObjectNode msg, Trade trade) {
        ObjectNode option = (ObjectNode)msg.get("option");
        String optType = msg.get("opType").asText();
        double strike = option.get("strike").asDouble();
        double price = 0.0;
        if( optType.equals("CE"))
        {
            price = option.get("call_ltp").asDouble();
        }
        else
        {
            price = option.get("put_ltp").asDouble();
        }

        int lots = msg.get("lots").asInt();
        trade.addPosition(option.get("symbol").asText(), strike, optType, lots, price);
        return sendTrade(trade);
    }

    public static ObjectNode removePosition(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        trade.removePosition(posId);
        return sendTrade(trade);
    }

    public static ObjectNode reversePosition(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        trade.reversePosition(posId);
        return sendTrade(trade);
    }

    public static ObjectNode updateQty(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        Integer qty = positionData.get("qty").asInt();
        trade.updateQty(posId, qty);
        return sendTrade(trade);
    }

    public static ObjectNode updatePrice(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        Double price = positionData.get("price").asDouble();
        trade.updatePrice(posId, price);
        return sendTrade(trade);
    }
}
