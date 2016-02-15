package com.punwire.kat.chart2;

import com.punwire.kat.core.AppConfig;

import java.awt.*;
import java.io.Serializable;

public abstract class Chart
        implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public Chart()
    {
    }

    public abstract String getName();
    public abstract void paint(Graphics2D g, ChartFrame cf);

}
