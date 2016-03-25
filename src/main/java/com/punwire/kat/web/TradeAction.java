package com.punwire.kat.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.trade.Trade;
import com.punwire.kat.zerodha.ZdHolding;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionMapper;
import org.sikuli.script.App;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Kanwal on 13/02/16.
 */
public class TradeAction {

    public TradeAction(){
    }

    public ObjectNode sendTrade(Trade trade) {
        System.out.println("Sending Trade..");
        ////Trade trade = Trade.shortPutSpread("RELIANCE",840,860,500);
        //Trade trade = Trade.shortPutSpread("NIFTY",6800,6900,150);
        ObjectNode result = AppConfig.mapper.createObjectNode();
        result.put("data", trade.toJson());
        result.put("event", "trade");
        System.out.println(result.toString());
        return result;
    }

    public Trade fetchTrade(String underline) {
        System.out.println("Fetching Trade for " + underline);
        List<ZdHolding> holdings = AppConfig.db.getHolding("NFO", underline);
        if( holdings.size() == 0 ) return null;
        Trade trade = new Trade(underline, "Zerodha", LocalDate.now());
        for(ZdHolding holding: holdings)
        {
            String optType = holding.symbol.endsWith("CE")? "CE" : "PE";
            String sym = holding.symbol.substring(0, (holding.symbol.length()-2));
            String expMonth = ZdOptionMapper.getExpDate(sym);

            String s = ZdOptionMapper.onlyLastNum(sym);
            Double strikePrice = Double.valueOf(s);

            if( optType.equals("CE"))
            {
                trade.addPositionQty(underline, strikePrice,optType,holding.qty,holding.avgPrice, expMonth);
            }
            else {
                trade.addPositionQty(underline, strikePrice,optType,holding.qty,holding.avgPrice, expMonth);
            }

        }
        trade.update();
        return trade;
    }

    public ObjectNode addPosition(ObjectNode msg, Trade trade) {
        ObjectNode option = (ObjectNode)msg.get("option");
        ZdOptionChain optionChain = ZdOptionChain.fromJson(option);
        String optType = msg.get("opType").asText();
        int lots = msg.get("lots").asInt();
        double strike = option.get("strike").asDouble();
        int expDate = option.get("expiry_date").asInt();
        double price = 0.0;
        if( optType.equals("CE"))
        {
            trade.addPosition(optionChain.call, lots);
        }
        else
        {
            trade.addPosition(optionChain.put, lots);
        }
        trade.update();
        return sendTrade(trade);
    }

    public ObjectNode removePosition(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        trade.removePosition(posId);
        trade.update();
        return sendTrade(trade);
    }

    public ObjectNode reversePosition(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        trade.reversePosition(posId);
        trade.update();
        return sendTrade(trade);
    }

    public ObjectNode updateQty(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        Integer qty = positionData.get("qty").asInt();
        trade.updateQty(posId, qty);
        trade.update();
        return sendTrade(trade);
    }

    public ObjectNode updatePrice(ObjectNode msg, Trade trade) {
        ObjectNode positionData = (ObjectNode)msg.get("data");
        Integer posId = positionData.get("id").asInt();
        Double price = positionData.get("price").asDouble();
        trade.updatePrice(posId, price);
        trade.update();
        return sendTrade(trade);
    }
}
