package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;

import java.io.Serializable;
import java.util.Calendar;

public class SixtyMinuteInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public SixtyMinuteInterval()
    {
        super("60 Min", true);
        timeParam = "60";
    }

    public long startTime()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        return c.getTimeInMillis();
    }

    public String getTimeParam()
    {
        return timeParam;
    }

    public int getLengthInSeconds()
    {
        return 3600;
    }

}
