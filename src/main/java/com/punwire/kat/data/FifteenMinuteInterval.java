package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;

import java.io.Serializable;
import java.util.Calendar;

public class FifteenMinuteInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public FifteenMinuteInterval()
    {
        super("15 Min", true);
        timeParam = "15";
    }

    public long startTime()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, -3);
        return c.getTimeInMillis();
    }

    public String getTimeParam()
    {
        return timeParam;
    }

    public int getLengthInSeconds()
    {
        return 900;
    }

}
