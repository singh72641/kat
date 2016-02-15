package com.punwire.kat.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.data.MongoDb;
import com.punwire.kat.data.Option;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdSymbol;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kanwal on 12/02/16.
 */
public class Trade {
    HashMap<Integer, OptionPosition> positions = new HashMap<>();
    public static int IDSEQ=0;
    int id;
    Integer positionNum=0;
    String name;
    public final String symbol;
    LocalDate date;
    public double delta=9999.99;
    public Trade(String symbol, String name, LocalDate date)
    {
        this.id = IDSEQ++;
        this.symbol = symbol;
        this.name = name;
        this.date = date;
    }

    public static Trade shortPutSpread(String symbol, double lowerStrike, double uppperStrike, int qty, double lowPremium, double upperPremium)
    {
        MongoDb db = AppConfig.db;
        ZdOptionChain lower = db.getOption(symbol, lowerStrike);
        ZdOptionChain upper = db.getOption(symbol, uppperStrike);
        OptionPosition ot1 = new OptionPosition(upper.put, -1 * qty,upperPremium,LocalDate.now());
        OptionPosition ot2 = new OptionPosition(lower.put, qty, lowPremium,LocalDate.now());
        Trade trade = new Trade(symbol, "Short Put Spread",LocalDate.now());
        trade.addPosition(ot1);
        trade.addPosition(ot2);
        return trade;
    }

    public static Trade shortPutSpread(String symbol, double lowerStrike, double uppperStrike, int qty)
    {
        MongoDb db = AppConfig.db;
        ZdOptionChain lower = db.getOption(symbol, lowerStrike);
        ZdOptionChain upper = db.getOption(symbol, uppperStrike);
        OptionPosition ot1 = new OptionPosition(upper.put, -1 * qty,upper.put.getLastPrice(),LocalDate.now());
        OptionPosition ot2 = new OptionPosition(lower.put, qty, lower.put.getLastPrice(),LocalDate.now());
        Trade trade = new Trade(symbol, "Short Put Spread",LocalDate.now());
        trade.addPosition(ot1);
        trade.addPosition(ot2);
        return trade;
    }

    public void addPosition(String symbol, double strike, String optType, int lots){
        MongoDb db = AppConfig.db;
        ZdOptionChain option = db.getOption(symbol, strike);
        ZdSymbol lot = db.getLot(symbol);
        if( optType.equals("CE")) {
            OptionPosition ot1 = new OptionPosition(option.call, lot.lotSize * lots, option.call.getLastPrice(), LocalDate.now());
            addPosition(ot1);
        }else{
            OptionPosition ot1 = new OptionPosition(option.put, lot.lotSize * lots, option.put.getLastPrice(), LocalDate.now());
            addPosition(ot1);
        }
    }


    public void addPosition(OptionPosition position){
        Integer posId = positionNum++;
        position.setId(posId);
        positions.put(posId,position);
        updateDelta();
    }

    public void reversePosition(Integer id){
        OptionPosition position = positions.get(id);
        position.qty = -1 * position.qty;
        updateDelta();
    }

    public void removePosition(Integer id){
        System.out.println("Removing ID " + id);
        positions.remove(id);
        updateDelta();
    }

    public ObjectNode toJson()
    {
        ObjectNode d = AppConfig.mapper.createObjectNode();
        ArrayNode p = AppConfig.mapper.createArrayNode();
        for(OptionPosition op: positions.values())
        {
            ObjectNode ojson = op.toJson();
            p.add(ojson);
        }
        d.put("id",id);
        d.put("name",name);
        d.put("date", DateUtil.intDate(date));
        d.put("delta", delta);
        d.put("positions", p);
        return d;
    }

    public double getPnLAtExpiry(double price)
    {
        double pnl = 0.0;
        for(OptionPosition op: positions.values())
        {
            double p = op.getPnLAt(price);
            //System.out.println(op.toString() + "     PnL= " + p + "  PriceAt: " + price);
            pnl += p;
        }
        //System.out.println("Total PnL= " + pnl);
        return pnl;
    }

    public double updateDelta()
    {
        double d = 0.00;
        double totalQty=0;
        for(OptionPosition op: positions.values())
        {
            totalQty += Math.abs(op.qty);
            d += op.qty * op.option.delta;
        }
        delta = d/totalQty;
        return delta;
    }

    public double getDelta()
    {
        return delta;
    }

    public double getMinStrike()
    {
        double strike = 999999.00;
        for(OptionPosition op: positions.values())
        {
            if( op.option.getStrike() < strike ) strike = op.option.getStrike();
        }
        return strike;
    }

    public double getMaxStrike()
    {
        double strike = 0.00;
        for(OptionPosition op: positions.values())
        {
            if( op.option.getStrike() > strike ) strike = op.option.getStrike();
        }
        return strike;
    }
}
