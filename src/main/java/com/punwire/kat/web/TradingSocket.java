package com.punwire.kat.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.core.NumberUtil;
import com.punwire.kat.data.Bar;
import com.punwire.kat.data.BarList;
import com.punwire.kat.data.MongoDb;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.options.BlackScholesGreeks;
import com.punwire.kat.options.OptionGreeks;
import com.punwire.kat.trade.OptionPosition;
import com.punwire.kat.trade.Trade;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdVolatility;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;


/**
 * Created by Kanwal on 14/01/16.
 */
public class TradingSocket extends WebSocketAdapter {

    private static final Logger LOG = Log.getLogger(TradingSocket.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static MongoDb db = new MongoDb();
    private Trade trade=null;
    double underlinePrice=0.0;

    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        LOG.info("WebSocket Close: {} - {}", statusCode, reason);
    }

    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        getSession().setIdleTimeout(1000000L);
        LOG.info("WebSocket Connect: {}",session);
        getRemote().sendStringByFuture("You are now connected to " + this.getClass().getName());
    }

    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error",cause);
    }

    public void onWebSocketText(String message)
    {
        if (isConnected()) {
            LOG.info("Echoing back text message [{}]", message);


            try {
                ObjectNode msg = (ObjectNode) mapper.readTree(message);
                String event = msg.get("event").asText();
                System.out.println("Event: " + event);
                ObjectNode result = mapper.createObjectNode();

                if( event.equals("oc")) {
                    String symbol = msg.get("symbol").asText();
                    result = OptionClassAction.sendOptionChain(msg);

                    ObjectNode optionData = (ObjectNode)result.get("data");
                    underlinePrice = optionData.get("last").asDouble();
                    getRemote().sendStringByFuture(result.toString());

                    //Fetch holdings
                    trade = TradeAction.fetchTrade(symbol);
                    if( trade != null ) {
                        trade.underlinePrice = underlinePrice;
                        result = TradeAction.sendTrade(trade);
                        getRemote().sendStringByFuture(result.toString());
                        // Send the Chart As well
                        result = ChartAction.sendChart(trade);
                        getRemote().sendStringByFuture(result.toString());
                    }

                }
                else if( event.equals("NewTrade")) {
                    String tradeType = msg.get("tradeType").asText();
                    String symbol = msg.get("symbol").asText();
                    trade = new Trade(symbol, tradeType, LocalDate.now());
                    trade.underlinePrice = underlinePrice;
                    result = TradeAction.sendTrade(trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Sending New Trade");
                }
                else if( event.equals("AddPos")) {
                    result = TradeAction.addPosition(msg, trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Added Position to trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    System.out.println(result.toString());
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("reversePos")) {
                    result = TradeAction.reversePosition(msg,trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Reversed Position to trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("updateQty")) {
                    result = TradeAction.updateQty(msg, trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Updated Qty on trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("updatePrice")) {
                    result = TradeAction.updatePrice(msg,trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Updated Qty on trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("refreshChart")) {
                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("removePos")) {
                    result = TradeAction.removePosition(msg,trade);
                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }
    }



    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2)
    {
        /* ignore */
    }
}
