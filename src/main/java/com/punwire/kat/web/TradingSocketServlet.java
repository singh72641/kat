package com.punwire.kat.web;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by Kanwal on 14/01/16.
 */

@SuppressWarnings("serial")
public class TradingSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(TradingSocket.class);
    }
}