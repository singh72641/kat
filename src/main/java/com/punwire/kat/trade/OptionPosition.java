package com.punwire.kat.trade;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Option;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Kanwal on 12/02/16.
 */
public class OptionPosition extends Position{
    Option option;

    public OptionPosition(Option option, int qty, double premium, LocalDate onDate)
    {
        this.option = option;
        this.qty = qty;
        this.avgPrice = premium;
        this.openDate = onDate;
        this.symbol = option.getSymbol();
    }

    public String toString()
    {
        return option.getSymbol() + "  "  + ( qty > 0 ? "BUY":"SELL") +  "   " + qty + "@" + avgPrice;
    }

    public ObjectNode toJson()
    {
        ObjectNode d = AppConfig.mapper.createObjectNode();
        d.put("symbol",option.getSymbol());
        d.put("opt_type",option.getOptionTypeName());
        d.put("id",id);
        d.put("underline",option.getUnderline());
        d.put("qty",qty);
        d.put("side", (qty>0?"Buy":"Sell"));
        d.put("price",avgPrice);
        d.put("strike",option.getStrike());
        d.put("iv",option.getVolatility());
        d.put("delta", NumberUtil.round(option.delta,4));
        d.put("theta",NumberUtil.round(option.theta,4));
        d.put("rho",NumberUtil.round(option.rho,4));
        d.put("gamma",NumberUtil.round(option.gamma,4));
        return d;
    }
    public double getPnLAt(double price)
    {
        double origPnL = -1 * qty * avgPrice;

        if( option.isCall())
        {
            //For Call
            if( qty > 0 ) {
                //We are long call
                if (price > option.getStrike()) {
                    // Call is in the money
                    origPnL += qty * (price - option.getStrike());
                }
            }
            else
            {
                //We are short call
                if (price > option.getStrike()) {
                    // Call is in the money
                    origPnL += qty * (price - option.getStrike());
                }
            }
        }
        else{
            //For Put
            if( qty > 0 ) {
                // We are long put
                if (price < option.getStrike()) {
                    // Put is in the money, profit
                    origPnL += qty * (option.getStrike() - price);
                }
            }
            else
            {
                //We are Short the put
                if (price < option.getStrike()) {
                    // Put is in the money, Loss
                    origPnL += (qty * (price - option.getStrike())) * (-1.0);
                }
            }
        }
        return origPnL;
    }
}
