package com.punwire.kat.data;


import java.io.Serializable;
import java.util.Calendar;
import com.punwire.kat.core.AppConfig;

public class WeeklyInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public WeeklyInterval()
    {
        super("Weekly");
        timeParam = "w";
    }

    public long startTime()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -4);
        return c.getTimeInMillis();
    }

    public String getTimeParam()
    {
        return timeParam;
    }

    public int getLengthInSeconds()
    {
        return 604800;
    }

}