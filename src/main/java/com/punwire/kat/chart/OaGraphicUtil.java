package com.punwire.kat.chart;

import java.awt.*;

/**
 * Created by Kanwal on 19/01/16.
 */
public class OaGraphicUtil {

    public static Graphics2D g2(Graphics g){
        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh
                = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHints(rh);
        return g2d;
    }
}
