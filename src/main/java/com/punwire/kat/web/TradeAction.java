package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.trade.Trade;

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

    public static ObjectNode addPosition(ObjectNode msg, Trade trade) {
        ObjectNode option = (ObjectNode)msg.get("option");
        String optType = msg.get("opType").asText();
        double strike = option.get("strike").asDouble();
        int lots = msg.get("lots").asInt();
        trade.addPosition(option.get("symbol").asText(), strike, optType, lots);
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

}
