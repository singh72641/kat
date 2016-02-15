package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;

import java.io.Serializable;
import java.util.Calendar;
public class ThirtyMinuteInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public ThirtyMinuteInterval()
    {
        super("30 Min", true);
        timeParam = "30";
    }

    public long startTime()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -6);
        return c.getTimeInMillis();
    }

    public String getTimeParam()
    {
        return timeParam;
    }

    public int getLengthInSeconds()
    {
        return 1800;
    }

}
