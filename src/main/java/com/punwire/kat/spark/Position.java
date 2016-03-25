package com.punwire.kat.spark;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Created by kanwal on 22/03/16.
 */
public class Position {
    private String id;
    private Long orderId;
    private final Portfolio portfolio;
    private String symbol;
    private int quantity = 0;
    private double averagePrice = 0.0D;
    private double marketValue = 0.0D;
    private double marketPrice = 0.0D;
    private double unrealizedPnL = 0.0D;
    private double realizedPnL = 0.0D;
    private Instant openTime;
    private Instant closeTime;

    public Position(String sym, Portfolio portfolio)
    {
        this.portfolio = portfolio;
        this.symbol = sym;
    }

    public void update(double price)
    {
        this.marketPrice = price;
        this.marketValue = this.quantity * this.marketPrice;
        this.unrealizedPnL = this.quantity * ( this.marketPrice - this.averagePrice);
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isOpen() {
        return quantity != 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public double getUnrealizedPnL() {
        return unrealizedPnL;
    }

    public double getRealizedPnL() {
        return realizedPnL;
    }

    public void addTrade(Trade trade)
    {
        if( quantity != 0 )
        {
            if( quantity + trade.getQty() == 0)
            {
                //Closing Trade
                this.closeTime = trade.getTime();
                this.marketValue = 0.0;
                this.marketPrice = trade.getPrice();
                this.unrealizedPnL = 0.0;
                this.realizedPnL += this.getQuantity() * (trade.getPrice() - this.averagePrice);
                this.quantity = quantity + trade.getQty();
            }
            else {
                //Add a trade, which does not
                if( ( quantity > 0 && trade.getQty() > 0) || ( quantity < 0 && trade.getQty() < 0 ) ) {
                    // adding to it
                    double avg = (( this.quantity * this.averagePrice) + ( trade.getQty() * trade.getPrice() ))/(this.quantity + trade.getQty());
                    this.averagePrice = avg;
                    this.quantity += trade.getQty();
                    this.marketValue = this.quantity * trade.getPrice();
                    this.marketPrice = trade.getPrice();
                    this.unrealizedPnL = this.quantity * ( this.marketPrice - this.averagePrice );
                }
                else if( ( quantity > 0 && trade.getQty() < 0 ) || ( quantity < 0 && trade.getQty() > 0 ) ) {
                    // We were long and we are selling
                    if( Math.abs(quantity) > Math.abs(trade.getQty()))
                    {
                        //Partiall Close
                        this.quantity -= trade.getQty();
                        this.marketValue = this.quantity * trade.getPrice();
                        this.marketPrice = trade.getPrice();
                        this.unrealizedPnL = this.quantity * ( this.marketPrice - this.averagePrice );
                        this.realizedPnL += trade.getQty() * ( trade.getPrice() - this.averagePrice);
                    }
                    else
                    {
                        //Full Close and reverse
                        int closeQty = this.quantity;
                        this.realizedPnL += closeQty * ( trade.getPrice() - this.averagePrice);
                        this.quantity = trade.getQty() + this.quantity;
                        this.averagePrice = trade.getPrice();
                        this.marketValue = this.quantity * trade.getPrice();
                        this.marketPrice = trade.getPrice();
                        this.unrealizedPnL = this.quantity * ( this.marketPrice - this.averagePrice );

                    }
                }
            }
        }
        else
        {
            this.quantity = trade.getQty();
            this.averagePrice = trade.getPrice();
            this.openTime = trade.getTime();
            this.marketValue = this.getQuantity() * this.averagePrice;
            this.marketPrice = trade.getPrice();
            this.unrealizedPnL = 0.0;
            this.realizedPnL = 0.0;
        }
    }

    public void dump()
    {
        System.out.println("Position");
        System.out.println("=================================");
        System.out.println("Symbol: " + symbol);
        System.out.println("Qty:    " + quantity);
        System.out.println("AvgPrice: " + averagePrice);
        System.out.println("RealizedPnl: " + realizedPnL);
        System.out.println("UnRealizedPnl: " + unrealizedPnL);
    }
}
