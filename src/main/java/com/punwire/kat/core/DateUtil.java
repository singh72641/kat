package com.punwire.kat.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by Kanwal on 13/01/16.
 */
public class DateUtil {

    public static LocalDateTime toDateTime(int date, int time)
    {
        int sec = time % 100;
        int hm = time % 10000;
        int hour = (int)((time-hm)/10000);
        int min = (time - (hour*10000))/100;
        if (sec>59) { sec -= 60; min++; }
        if (min > 59) { hour++; min-=60; }
        int year = 1, day = 1, month = 1;
        if (date != 0)
        {
            int ym = (date % 10000);
            year = (int)((date - ym)/10000);
            int mm = ym % 100;
            month = (int)((ym - mm) / 100);
            day = mm;
        }
        return LocalDateTime.of(year,month,day,hour,min);
    }

    public static LocalDate toDate(int date)
    {
        int year = 1, day = 1, month = 1;
        if (date != 0)
        {
            int ym = (date % 10000);
            year = (int)((date - ym)/10000);
            int mm = ym % 100;
            month = (int)((ym - mm) / 100);
            day = mm;
        }
        return LocalDate.of(year,month,day);
    }

    public static Instant toInstant(long datetime)
    {
        LocalDateTime dt = toDateTime( intDate(datetime), intTime(datetime) );
        return dt.toInstant(ZoneOffset.ofHoursMinutes(5, 30));
    }

    public static long longDateTime(int date, int time)
    {
        return (long)(date) * 1000000 +  (long)time;
    }

    public static long longDateTime(Instant instant)
    {
        LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return longDateTime(dt);
    }


    // get long for current date + time
    public static int intDate(long datetime)
    {
        long rem = datetime % 1000000;
        long date = datetime - rem;
        date = date / 1000000;
        return (int)date;
    }

    public static int intTime(long datetime)
    {
        long rem = datetime % 1000000;
        return (int)rem;
    }

    public static int intDate(LocalDateTime dt)
    {
        return (dt.getYear() * 10000) + (dt.getMonthValue() * 100) + dt.getDayOfMonth();
    }

    public static int intDate(LocalDate dt)
    {
        return (dt.getYear() * 10000) + (dt.getMonthValue() * 100) + dt.getDayOfMonth();
    }

    public static int intTime(LocalDateTime dt)
    {
        return (dt.getHour() * 10000) + (dt.getMinute() * 100) + dt.getSecond();
    }

    public static long longDateTime(LocalDateTime dt)
    {
        int d = intDate(dt);
        int t = intTime(dt);
        return longDateTime( d, t);
    }

}
