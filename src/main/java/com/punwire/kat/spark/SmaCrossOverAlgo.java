package com.punwire.kat.spark;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.NumberUtil;

import java.time.Instant;

/**
 * Created by kanwal on 22/03/16.
 */
public class SmaCrossOverAlgo extends Algorithm {
    private String symbol = "AXISBANK";
    public SmaCrossOverAlgo() {
        super();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public void initialize() {
        dataProvider.addSecurity(symbol);
        dataProvider.addHistory("MA1", "close", 25);
        dataProvider.addHistory("MA2", "close", 100);
    }

    @Override
    public void onTrade(Trade trade) {
        //Add Trade to the result event
        result.addLong("Trade",trade.getTime(),Long.valueOf(trade.getQty()) );
        ObjectNode res = AppConfig.objectNode();
        res.put("event", "AlgoTrade");
        res.put("data", trade.toJson());
        eventListener.accept(res);
    }

    @Override
    public void onData(Instant point) {
        super.onData(point);
        if( (! getHistory(symbol, "MA1").isReady()) || (! getHistory(symbol,"MA2").isReady()) ) return;
        double price = getDouble(symbol, point, "close");
        double ma1 = getHistory(symbol, "MA1").sma();
        double ma2 = getHistory(symbol, "MA2").sma();
        result.addDouble("MA1",point, NumberUtil.round(ma1,2));
        result.addDouble("MA2",point,NumberUtil.round(ma2,2));
        result.addDouble("Price",point,NumberUtil.round(price,2));
        if( ma1 > ma2 )
        {
            if( portfolio.getOpenQty(symbol) < 0 ) {
                //Open the position if we are not in one
                portfolio.orderTarget(symbol, point, 0);
            }
            else  if( portfolio.getOpenQty(symbol) == 0 ) {
               if( (ma1 - ma2) > 4) portfolio.orderTarget(symbol, point, 100);
            }
        }
        else if( ma1 < ma2 ) {
            if( portfolio.getOpenQty(symbol) > 0) {
                //Open the position if we are not in one
                portfolio.orderTarget(symbol, point, 0);
            }
            else  if( portfolio.getOpenQty(symbol) == 0 ) {
                if( (ma2 - ma1) > 4) portfolio.orderTarget(symbol, point, -100);
            }
        }
        result.addDouble("Equity",point,NumberUtil.round(portfolio.getCurrentValue(),2));
        if( resultListener != null ) resultListener.accept(result);
        //System.out.println("At " + point.toString() +  "  " + ts.getDouble(point, "close") + "Fast SMA:" + ma1  + " Slow SMA:" + ma2 );
    }
}
