package com.punwire.kat.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.*;
import com.punwire.kat.spark.SmaCrossOverAlgo;
import com.punwire.kat.spark.TimeSeries;
import com.punwire.kat.trade.Trade;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdOptionList;
import com.punwire.kat.zerodha.ZdSymbol;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;


/**
 * Created by Kanwal on 14/01/16.
 */
public class TradingSocket extends WebSocketAdapter {

    private static final Logger LOG = Log.getLogger(TradingSocket.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static MongoDb db = new MongoDb();
    private Trade trade=null;
    double underlinePrice=0.0;
    TradeAction tradeAction = new TradeAction();
    List<ZdSymbol> symbols=null;
    String currExpMonth;
    String symbol;
    SmaCrossOverAlgo algo;
    HashMap<String, Consumer<ObjectNode>> actions = new HashMap<>();
    Session session;
    int algoCount=0;
    ArrayNode algoOutput = AppConfig.arrayNode();
    public TradingSocket(){
        super();
        actions.put("SymbolSearch", this::eventSymbolSearch);
        actions.put("Analyze", this::eventAnalyze);
        actions.put("OptionChain", this::eventOptionChain);
        actions.put("AddPos", this::eventAddPos);
        actions.put("Positions", this::eventPositions);
        actions.put("Algo", this::eventAlgo);
        actions.put("Eod", this::eventEod);
        actions.put("Eod", this::eventEod);
        actions.put("Fii", this::eventFii);
        //actions.put("OptionChain", this::eventOptionChain);
    }

    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        LOG.info("WebSocket Close: {} - {}", statusCode, reason);
    }

    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        this.session = session;
        getSession().setIdleTimeout(1000000L);
        LOG.info("WebSocket Connect: {}",session);
        getRemote().sendStringByFuture("You are now connected to " + this.getClass().getName());
    }

    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error",cause);
    }

    //Nothing needed in msg
    public void eventSymbolSearch(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        if( symbols == null ) symbols = AppConfig.db.getSymbols();
        ArrayNode list = AppConfig.arrayNode();
        for(ZdSymbol symbol: symbols)
        {
            list.add(symbol.toJson());
        }
        result.put("event", "SymbolSearch");
        result.put("data", list);
        getRemote().sendStringByFuture(result.toString());
    }

    //Nothing needed in msg
    public void eventSendDates(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        result = OptionClassAction.sendDates();
        getRemote().sendStringByFuture(result.toString());
        ArrayNode data1 = (ArrayNode)result.get("data");
        currExpMonth =  data1.get(0).asText();
    }

    //Nothing needed in msg
    public void eventAddPos(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        result = tradeAction.addPosition(msg, trade);
        getRemote().sendStringByFuture(result.toString());
        System.out.println("Added Position to trade");

        //Send Chart As Well
        eventSendTrade(msg);
        eventChart(msg);
    }

    //Nothing needed in msg
    public void eventChart(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        //Send Chart As Well
        result = ChartAction.sendChart(trade);
        System.out.println(result.toString());
        getRemote().sendStringByFuture(result.toString());

        //Send Oi Chart
        //result = ChartAction.sendOiChart();
        //getRemote().sendStringByFuture(result.toString());


    }

    //Nothing needed in msg
    public void eventPositions(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        //Send Chart As Well
        result = PositionAction.sendPositions();
        getRemote().sendStringByFuture(result.toString());
    }

    //Nothing needed in msg
    public void eventSendTrade(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        //Send Chart As Well
        if( trade != null ) {
            System.out.println("Seeting Trade Underline Price " + underlinePrice );
            trade.underlinePrice = underlinePrice;
            trade.update();

            result = tradeAction.sendTrade(trade);
            getRemote().sendStringByFuture(result.toString());
            // Send the Chart As well
            eventChart(msg);
        }
    }


    public void eventFii(ObjectNode msg) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File("D:\\Zero\\kat\\charts\\"+"fii.json");
            ObjectNode o = (ObjectNode) AppConfig.mapper.readTree(file);
            ObjectNode result = AppConfig.objectNode();
            result.put("event","FiiChart");
            result.put("data",o);
            getRemote().sendStringByFuture(result.toString());

            //Send Fii Data
            TimeSeries ts = AppConfig.db.getFiiTS();
            result = AppConfig.objectNode();
            result.put("event","Fii");
            result.put("data",ts.toJson());
            System.out.println(result.toString());
            getRemote().sendStringByFuture(result.toString());

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void eventEodChart(ObjectNode msg) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File("D:\\Zero\\kat\\charts\\"+"algo.json");
            ObjectNode o = (ObjectNode) AppConfig.mapper.readTree(file);
            ObjectNode result = AppConfig.objectNode();
            result.put("event","EodChart");
            result.put("data",o);
            getRemote().sendStringByFuture(result.toString());
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //Nothing needed in msg
    public void eventEod(ObjectNode msg)
    {
        //File classpathRoot = new File( getClass().getClassLoader().getResource("").getPath());
        //System.out.println(classpathRoot);
        ObjectNode result = AppConfig.objectNode();
        ObjectNode data = (ObjectNode)msg.get("data");
        String symbol = data.get("symbol").asText();
        System.out.println("Event EOD for " + symbol);
        String startDate = data.get("startDate").asText();
        String endDate = data.get("endDate").asText();
        LocalDate sDate = LocalDate.parse(startDate,NseLoader.yyyy_MM_dd);
        int page = data.get("page").asInt();
        System.out.println("Page: " + page + "  start: " + sDate.toString());
        LocalDate fDate = sDate.plusDays((page-1) * 100);
        TimeSeries ts = AppConfig.db.getTimeSeries(symbol, fDate, 140);
        ArrayNode d = AppConfig.arrayNode();
        int count=0;
        for( Instant point: ts.getPoints())
        {
            d.add(ts.toJson(point));
            count++;
        }
        result.put("event", "Eod");
        result.put("data", d);
        result.put("page", 1);
        System.out.println(result);
        getRemote().sendStringByFuture(result.toString());
        if( page == 1 ) eventEodChart(msg);
    }

    //data.symbol, data.month
    public void eventOptionChain(ObjectNode msg)
    {
        ObjectNode result = AppConfig.objectNode();
        ObjectNode data = (ObjectNode)msg.get("data");
        symbol = data.get("symbol").asText();
        String refreshChart = data.get("chart").asText();


        JsonNode expMonthN = data.get("month");
        String expMonth="";
        if(expMonthN != null ) expMonth = expMonthN.asText();
        System.out.println("+++++++++++ Month: " + expMonth);

        result = OptionClassAction.sendOptionChain(symbol, expMonth);

        ObjectNode optionData = (ObjectNode)result.get("data");
        underlinePrice = optionData.get("last").asDouble();
        getRemote().sendStringByFuture(result.toString());

        if( refreshChart != null && refreshChart.equals("Y"))
        {
            eventChart(msg);
        }
    }


    //data.symbol
    public void eventAlgo(ObjectNode msg)
    {
        ObjectNode data = (ObjectNode)msg.get("data");
        String symbol = data.get("symbol").asText();
        System.out.println("Algo for : " + symbol);
        algo = new SmaCrossOverAlgo();
        algo.setSymbol(symbol);
        algo.addResultListener(this::listenAlgoResult);
        algo.addEventListener(this::listenAlgoEvent);
        algo.run();
        //algo.dump();
    }

    public void listenAlgoEvent(JsonNode event){
        if( event.get("event").equals("END")){
            //Send the Last one
            ObjectNode result = AppConfig.objectNode();
            result.put("event", "AlgoChart");
            result.put("data", algoOutput);
            //System.out.println("Sending algo result" + result.toString());
            session.getRemote().sendStringByFuture(result.toString());
        }
        else {
            session.getRemote().sendStringByFuture(event.toString());
        }
    }

    public void listenAlgoResult(TimeSeries algoResult){
        algoCount++;
        algoOutput.add(algoResult.toJson(algoResult.getLastPoint()));

        if( algoCount > 50 ) {
            ObjectNode result = AppConfig.objectNode();
            result.put("event", "AlgoChart");
            result.put("data", algoOutput);
            //System.out.println("Sending algo result" + result.toString());
            session.getRemote().sendStringByFuture(result.toString());
            try {
                Thread.sleep(300L);
            } catch (Exception ex) {

            }
            algoCount = 0;
            algoOutput.removeAll();
        }
    }

    //data.symbol
    public void eventAnalyze(ObjectNode msg)
    {
        System.out.println("In the event eventAnalyze " + msg.toString());
        ObjectNode result = AppConfig.objectNode();
        ObjectNode data = (ObjectNode)msg.get("data");

        String s = data.get("symbol").asText();
        String tradeType = data.get("tradeType").asText();

        //Get the dates to find current Month
        String cm =  AppConfig.db.getCurrExpMonth();

        if( tradeType.equals("VERTICALSC"))
        {
            trade = buildVerticalCall(symbol,true);
        }
        else if( tradeType.equals("VERTICALLC"))
        {
            trade = buildVerticalCall(symbol,false);
        }
        else if( tradeType.equals("VERTICALSP"))
        {
            trade = buildVerticalPut(symbol,true);
        }
        else if( tradeType.equals("VERTICALLP"))
        {
            trade = buildVerticalPut(symbol,false);
        }

        eventSendTrade(msg);

        eventChart(msg);
    }

    public Trade buildVerticalCall(String symbol, boolean isShort)
    {
        trade = new Trade(symbol, "VERTICAL - Short Call", LocalDate.now());
        double currPrice=0.00;
        Option firstCall=null;
        Option secondCall=null;
        ZdOptionList list = AppConfig.store.get(symbol);
        TreeMap<Double,ZdOptionChain> oc = list.get(currExpMonth);
        Double currKey= oc.firstKey();
        for(int i=0;i<oc.size();i++)
        {
            Option o = oc.get(currKey).call;
            if( firstCall == null && o.getStrike() > oc.get(currKey).underlinePrice ) firstCall = o;
            else if (firstCall != null){
                secondCall = o;
                break;
            }
            currKey = oc.higherKey(currKey);
        }

        trade.addPosition(firstCall, (  isShort ? -1: 1));
        trade.addPosition(secondCall,(  isShort ? 1: -1));
        trade.underlinePrice = currPrice;
        return trade;
    }


    public Trade buildVerticalPut(String symbol, boolean isShort)
    {
        trade = new Trade(symbol, "VERTICAL - Short Call", LocalDate.now());
        double currPrice=0.00;
        Option firstPut=null;
        Option secondPut=null;
        ZdOptionList list = AppConfig.store.get(symbol);
        TreeMap<Double,ZdOptionChain> oc = list.get(currExpMonth);

        Double currKey= oc.lastKey();
        for(int i=0;i<oc.size();i++)
        {
            Option o = oc.get(currKey).put;
            if( firstPut == null && o.getStrike() < oc.get(currKey).underlinePrice ) firstPut = o;
            else if (firstPut != null){
                secondPut = o;
                break;
            }
            currKey = oc.lowerKey(currKey);
        }

        trade.addPosition(firstPut,(  isShort ? -1: 1));
        trade.addPosition(secondPut,(  isShort ? 1: -1));
        trade.underlinePrice = currPrice;
        return trade;
    }


    public void onWebSocketText(String message)
    {
        if (isConnected()) {
            LOG.info("Echoing back text message [{}]", message);
            ObjectNode result = AppConfig.objectNode();

            try {
                ObjectNode msg = (ObjectNode) mapper.readTree(message);
                String event = msg.get("event").asText();
                System.out.println("Event: " + event);


                if( actions.containsKey(event))
                {
                    actions.get(event).accept(msg);
                }
                else if( event.equals("oc")) {
                    String symbol = msg.get("symbol").asText();
                    JsonNode expMonthN = msg.get("month");
                    currExpMonth=AppConfig.db.getCurrExpMonth();
                    if(expMonthN != null ) currExpMonth = expMonthN.asText();
                    System.out.println("+++++++++++ Month: " + currExpMonth);


                    ZdOptionList list = AppConfig.store.get(symbol);
                    TreeMap<Double,ZdOptionChain> oc = list.get(currExpMonth);
                    result = OptionClassAction.sendOptionChain(symbol, currExpMonth);

                    ObjectNode optionData = (ObjectNode)result.get("data");
                    underlinePrice = optionData.get("last").asDouble();
                    getRemote().sendStringByFuture(result.toString());

                    //Send Dates
                    result = OptionClassAction.sendDates();
                    getRemote().sendStringByFuture(result.toString());

                    //Send Oi Chart
                    result = ChartAction.sendOiChart(symbol,currExpMonth);
                    getRemote().sendStringByFuture(result.toString());

                    //Fetch holdings
                    trade = tradeAction.fetchTrade(symbol);
                    if( trade != null ) {
                        System.out.println("Seeting Trade Underline Price " + underlinePrice );
                        trade.underlinePrice = underlinePrice;
                        trade.update();

                        result = tradeAction.sendTrade(trade);
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
                    result = tradeAction.sendTrade(trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Sending New Trade");
                }
                else if( event.equals("reversePos")) {
                    result = tradeAction.reversePosition(msg, trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Reversed Position to trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("updateQty")) {
                    result = tradeAction.updateQty(msg, trade);
                    getRemote().sendStringByFuture(result.toString());
                    System.out.println("Updated Qty on trade");

                    //Send Chart As Well
                    result = ChartAction.sendChart(trade);
                    getRemote().sendStringByFuture(result.toString());
                }
                else if( event.equals("updatePrice")) {
                    result = tradeAction.updatePrice(msg, trade);
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
                    result = tradeAction.removePosition(msg,trade);
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
