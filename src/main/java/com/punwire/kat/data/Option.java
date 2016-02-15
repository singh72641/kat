package com.punwire.kat.data;

import com.punwire.kat.options.BlackScholesGreeks;
import com.punwire.kat.options.OptionDetails;
import com.punwire.kat.options.OptionGreeks;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Created by Kanwal on 24/01/16.
 */
public class Option {
    private String symbol;
    private String underline;
    private double underlinePrice;
    private String optionType;
    private LocalDate expiryDate;
    private double strike;
    private double volatility;
    private double lastPrice;
    private double lastChange;
    private long openInterest;
    private long oiChange;
    private long volume;
    private double bidPrice;
    private int bidQty;
    private double askPrice;
    private int askQty;
    public double delta;
    public double gamma;
    public double rho;
    public double theta;
    public double vega;


    public Option(String symbol, String underLine, String optionType, LocalDate expiryDate, double strike){
        this.symbol = symbol;
        this.underline = underLine;
        this.optionType = optionType;
        this.expiryDate = expiryDate;
        this.strike = strike;
    }

    public OptionGreeks greeks(double interest, LocalDate asOnDate) {
        boolean isCall = optionType.equals("CE")?true: false;
        Duration d = Duration.between(asOnDate.atStartOfDay(), expiryDate.atStartOfDay());
        long days = d.toDays();
        double dd = (days / 365.00);
        OptionDetails od = new OptionDetails(isCall,underlinePrice,strike,interest, dd ,volatility);
        OptionGreeks og =  BlackScholesGreeks.calculate( new OptionDetails(isCall,underlinePrice,strike,interest, dd ,volatility/100.00));
        System.out.println(od .toString());
        System.out.println(og.toString());
        return og;
    }

    public boolean isCall()
    {
        return optionType.equals("CE")?true:false;
    }

    public boolean isPut()
    {
        return optionType.equals("PE")?true:false;
    }

    public void setGreeks(double delta, double gamma, double rho, double theta,  double vega){
        this.delta = delta;
        this.gamma = gamma;
        this.rho = rho;
        this.theta = theta;
        this.vega = vega;
    }

    public Option(String symbol, String underLine, double underlinePrice, String optionType, LocalDate expiryDate, double strike, double lastPrice, double lastChange, double askPrice, int askQty,
                  double bidPrice, int bidQty, double volatility, long oi, long oic, long volume){
        this.symbol = symbol;
        this.underlinePrice = underlinePrice;
        this.underline = underLine;
        this.optionType = optionType;
        this.expiryDate = expiryDate;
        this.strike = strike;
        this.lastPrice = lastPrice;
        this.lastChange = lastChange;
        this.volatility = volatility;
        this.askPrice = askPrice;
        this.askQty = askQty;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.openInterest = oi;
        this.oiChange = oic;
        this.volume = volume;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getUnderline() {
        return underline;
    }

    public void setUnderline(String underline) {
        this.underline = underline;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getStrike() {
        return strike;
    }

    public void setStrike(double strike) {
        this.strike = strike;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(long openInterest) {
        this.openInterest = openInterest;
    }

    public long getOiChange() {
        return oiChange;
    }

    public void setOiChange(long oiChange) {
        this.oiChange = oiChange;
    }

    public String getOptionType() {
        return optionType;
    }

    public String getOptionTypeName() {
        return optionType.equals("CE")?"Call":"Put";
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public int getBidQty() {
        return bidQty;
    }

    public void setBidQty(int bidQty) {
        this.bidQty = bidQty;
    }

    public double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(double askPrice) {
        this.askPrice = askPrice;
    }

    public int getAskQty() {
        return askQty;
    }

    public void setAskQty(int askQty) {
        this.askQty = askQty;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public double getLastChange() {
        return lastChange;
    }

    public void setLastChange(double lastChange) {
        this.lastChange = lastChange;
    }

    public int isITM()
    {
        if( optionType.equals("CE"))
        {
            //Call
            return underlinePrice > strike ? 1: 0;
        }
        else {
            //Put
            return underlinePrice < strike ? 1: 0;
        }
    }
}
