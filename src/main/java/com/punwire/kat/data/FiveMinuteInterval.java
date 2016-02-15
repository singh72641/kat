package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;
import java.io.Serializable;
import java.util.Calendar;
/**
 * Created by Kanwal on 16/01/16.
 */
public class FiveMinuteInterval extends Interval implements Serializable
{

    private static final long serialVersionUID = AppConfig.APPVERSION;

    public FiveMinuteInterval()
    {
        super("5 Min", true);
        timeParam = "5";
    }

    public long startTime()
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.MONTH, -1);
        return c.getTimeInMillis();
    }

    public String getTimeParam()
    {
        return timeParam;
    }

    public int getLengthInSeconds()
    {
        return 300;
    }

}
