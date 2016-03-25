package com.punwire.kat.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.DateUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

/**
 * Created by Kanwal on 13/01/16.
 */
public class BarList {
    TreeMap<Long, Bar> bars = new TreeMap<Long, Bar>();
    int duration;

    private static ObjectMapper mapper = new ObjectMapper();

    public BarList(int duration)
    {
        this.duration = duration;
    }

    public TreeMap<Long, Bar> getBars() {
        return bars;
    }

    public void addBar(Bar bar)
    {
        int time = DateUtil.intTime(bar.getStart());
        int hm = time/100;
        int hmSince = ( hm - 915) / duration;
        int barTime = 915 + hmSince * duration;
        long barDateTime = DateUtil.longDateTime( DateUtil.intDate(bar.getStart()), barTime * 100 );

        if( bars.containsKey(barDateTime))
        {
            //Add to Bar
            Bar b = bars.get(barDateTime);
            if( bar.getH() > b.getH() ) b.setH(bar.getH());
            if( bar.getL() < b.getL() ) b.setL(bar.getL());
            b.setC(bar.getC());
            b.setV(  b.getV() + bar.getV() );
            b.setOI( bar.getOI() );
        }
        else {
            //Create New Bar
            Bar b = new Bar(bar.getSymbol(), barDateTime,duration,bar.getO(),bar.getH(),bar.getL(),bar.getC(),bar.getV(), bar.getOI());
            bars.put(barDateTime,b);
        }
    }

    public void dump()
    {
        for(Bar bar: bars.values())
        {
            System.out.println(bar);
        }
    }

    public ArrayNode toJson()
    {
        ArrayNode result =  mapper.createArrayNode();
        int count=0;
        for(Bar bar: bars.values())
        {
            Instant s = DateUtil.toInstant(bar.getStart());
            DateTimeFormatter f = DateTimeFormatter.ofPattern("YYYYMMddHHmm");
            ObjectNode row =  mapper.createObjectNode();
            LocalDateTime dt = DateUtil.toDateTime(DateUtil.intDate(bar.getStart()), DateUtil.intTime(bar.getStart()));

            row.put("Date", dt.format(f));
            row.put("Open", bar.getO());
            row.put("High", bar.getH());
            row.put("Low", bar.getL());
            row.put("Close", bar.getC());
            row.put("Volume", bar.getV());
            row.put("oi", bar.getOI());
            //if( count++ > 200 ) break;
            result.add(row);
        }
        return result;
    }
}
