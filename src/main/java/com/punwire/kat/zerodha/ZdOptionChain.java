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
        d.put("call_symbol",call.getSymbol());
        d.put("call_itm",call.isITM());
        d.put("call_oi",call.getOpenInterest());
        d.put("call_oic",call.getOiChange());
        d.put("call_volume", call.getVolume());
        d.put("call_iv",call.getVolatility());
        d.put("call_ltp", call.getLastPrice());
        d.put("call_change",call.getLastChange());
        d.put("call_bid",call.getBidPrice());
        d.put("call_ask",call.getAskPrice());
        d.put("call_atm",(call.atm?1:0));
        d.put("put_atm",(put.atm?1:0));
        double callTimeVal = ( underlinePrice > call.getStrike()?  call.getLastPrice() - (underlinePrice - call.getStrike()): call.getLastPrice() );
        double putTimeVal = ( underlinePrice < put.getStrike()?  put.getLastPrice() - (put.getStrike() - underlinePrice) : put.getLastPrice() );
        d.put("call_tval", greekFormatter.format(callTimeVal));
        d.put("put_tval", greekFormatter.format(putTimeVal));
        d.put("call_delta", greekFormatter.format(call.delta));
        d.put("call_gamma", greekFormatter.format(call.gamma));
        d.put("call_rho", greekFormatter.format(call.rho));
        d.put("call_theta", greekFormatter.format(call.theta));
        d.put("call_vega", greekFormatter.format(call.vega));
        d.put("put_symbol",put.getSymbol());
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

    public static ZdOptionChain fromJson(ObjectNode d){
        String underline = d.get("symbol").asText();
        int onDate = d.get("on_date").asInt();
        int expDate = d.get("expiry_date").asInt();
        double strikePrice = d.get("strike").asDouble();
        double underlinePrice = d.get("underlinePrice").asDouble();
        String call_symbol = d.get("call_symbol").asText();
        String put_symbol = d.get("put_symbol").asText();
        long callOi = d.get("call_oi").asLong();
        long callOic = d.get("call_oic").asLong();
        long callVolume = d.get("call_volume").asLong();
        double callIv = d.get("call_iv").asDouble();
        double callLtp = d.get("call_ltp").asDouble();
        double callChange = d.get("call_change").asDouble();
        double callBid = d.get("call_bid").asDouble();
        double callAsk = d.get("call_ask").asDouble();
        double callDelta = d.get("call_delta").asDouble();
        double callGamma = d.get("call_gamma").asDouble();
        double callRho = d.get("call_rho").asDouble();
        double callTheta = d.get("call_theta").asDouble();
        double callVega = d.get("call_vega").asDouble();

        Option call = new Option(call_symbol,underline, underlinePrice,"CE",DateUtil.toDate(expDate),strikePrice,callLtp,callChange,callAsk,0,callBid,0,callIv,callOi,callOic,callVolume);
        call.delta = callDelta;
        call.gamma = callGamma;
        call.theta = callTheta;
        call.rho = callRho;
        call.vega = callVega;

        long putOi = d.get("put_oi").asLong();
        long putOic= d.get("put_oic").asLong();
        long putVolume = d.get("put_volume").asLong();
        double putIv = d.get("put_iv").asDouble();
        double putLtp = d.get("put_ltp").asDouble();
        double putChange = d.get("put_change").asDouble();
        double putBid = d.get("put_bid").asDouble();
        double putAsk = d.get("put_ask").asDouble();
        double putDelta = d.get("put_delta").asDouble();
        double putGamma = d.get("put_gamma").asDouble();
        double putRho = d.get("put_rho").asDouble();
        double putTheta = d.get("put_theta").asDouble();
        double putVega = d.get("put_vega").asDouble();

        Option put = new Option(put_symbol,underline, underlinePrice,"PE",DateUtil.toDate(expDate),strikePrice,putLtp,putChange,putAsk,0,putBid,0,putIv,putOi,putOic,putVolume);
        put.delta = putDelta;
        put.gamma = putGamma;
        put.rho = putRho;
        put.theta = putTheta;
        put.vega = putVega;

        ZdOptionChain oc = new ZdOptionChain(underline,underlinePrice,DateUtil.toDate(onDate),DateUtil.toDate(expDate),strikePrice,call,put,0.10);
        return oc;
    }

}
