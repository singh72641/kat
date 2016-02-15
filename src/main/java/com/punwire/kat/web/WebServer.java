package com.punwire.kat.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.eclipse.jetty.websocket.server.pathmap.ServletPathSpec;

import java.net.URL;
import java.util.Objects;

/**
 * Created by Kanwal on 14/01/16.
 */
public class WebServer {

    public static void main(String[] args)
    {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);


        // Add websocket servlet
        ServletHolder wsHolder = new ServletHolder("echo",new TradingSocketServlet());
        context.addServlet(wsHolder, "/trading");


        // Add default servlet (to serve the html/css/js)
        URL urlStatics = Thread.currentThread().getContextClassLoader().getResource("index.html");
        Objects.requireNonNull(urlStatics, "Unable to find index.html in classpath");
        String urlBase = urlStatics.toExternalForm().replaceFirst("/[^/]*$","/");
        ServletHolder defHolder = new ServletHolder("default",new DefaultServlet());
        defHolder.setInitParameter("resourceBase",urlBase);
        defHolder.setInitParameter("dirAllowed","true");
        defHolder.setInitParameter("cacheControl", "max-age=0,public");
        context.addServlet(defHolder,"/");

        try
        {
            server.start();
            server.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
