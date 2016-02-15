package com.punwire.kat.data;

import java.io.Serializable;
import java.util.Calendar;
import com.punwire.kat.core.AppConfig;

/**
 * Created by Kanwal on 16/01/16.
 */
public class MonthlyInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public MonthlyInterval()
    {
        super("Monthly");
        timeParam = "m";
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
        return 2628000;
    }

}
