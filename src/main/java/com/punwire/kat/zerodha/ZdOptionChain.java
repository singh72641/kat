package com.punwire.kat.zerodha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.data.Option;
import com.punwire.kat.options.BlackScholesGreeks;
import com.punwire.kat.options.OptionGreeks;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Kanwal on 05/02/16.
 */
public class ZdOptionChain {
    final public double strikePrice;
    final public double underlinePrice;
    final public double interest;
    final public String underline;
    final public Option call;
    final public Option put;
    final public LocalDate expiryDate;
    public LocalDate asOfDate;
    final static ObjectMapper mapper = new ObjectMapper();
    public OptionGreeks callGreeks;
    public OptionGreeks putGreeks;
    public static DecimalFormat greekFormatter = new DecimalFormat("##.##");

    public ZdOptionChain(String underline, double underlinePrice, LocalDate asOfDate, LocalDate expiryDate, double Strike, Option call, Option put, double interest){
        this.interest = interest;
        this.underline = underline;
        this.underlinePrice = underlinePrice;
        this.asOfDate = asOfDate;
        this.expiryDate = expiryDate;
        this.strikePrice = Strike;
        this.call = call;
        this.put = put;
        //greeks(interest,asOfDate);
    }


    public void greeks(double interest, LocalDate asOnDate) {
        callGreeks = call.greeks(interest,asOnDate);
        putGreeks = put.greeks(interest,asOnDate);
    }

    public ObjectNode toJson(){
        ObjectNode d = mapper.createObjectNode();
        d.put("symbol",underline);
        d.put("on_date", DateUtil.intDate(asOfDate));
        d.put("expiry_date", DateUtil.intDate(expiryDate));
        d.put("strike",strikePrice);
        d.put("underlinePrice",underlinePrice);
        d.put("call_itm",call.isITM());
        d.put("call_oi",call.getOpenInterest());
        d.put("call_oic",call.getOiChange());
        d.put("call_volume", call.getVolume());
        d.put("call_iv",call.getVolatility());
        d.put("call_ltp", call.getLastPrice());
        d.put("call_change",call.getLastChange());
        d.put("call_bid",call.getBidPrice());
        d.put("call_ask",call.getAskPrice());
        d.put("call_delta", greekFormatter.format(call.delta));
        d.put("call_gamma", greekFormatter.format(call.gamma));
        d.put("call_rho", greekFormatter.format(call.rho));
        d.put("call_theta", greekFormatter.format(call.theta));
        d.put("call_vega", greekFormatter.format(call.vega));
        d.put("put_oi",put.getOpenInterest());
        d.put("put_oic",put.getOiChange());
        d.put("put_volume",put.getVolume());
        d.put("put_iv",put.getVolatility());
        d.put("put_ltp", put.getLastPrice());
        d.put("put_change",put.getLastChange());
        d.put("put_bid",put.getBidPrice());
        d.put("put_ask",put.getAskPrice());
        d.put("put_itm",put.isITM());
        d.put("put_delta", greekFormatter.format(put.delta));
        d.put("put_gamma", greekFormatter.format(put.gamma));
        d.put("put_rho", greekFormatter.format(put.rho));
        d.put("put_theta", greekFormatter.format(put.theta));
        d.put("put_vega", greekFormatter.format(put.vega));
        return d;
    }

}
