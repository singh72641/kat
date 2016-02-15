package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;
import java.io.Serializable;
import java.util.Calendar;

public class DailyInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public DailyInterval()
    {
        super("Daily");
        timeParam = "d";
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
        return 86400;
    }

}
