package com.punwire.kat.spark;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.NseLoader;
import com.tictactec.ta.lib.Core;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by kanwal on 22/03/16.
 */
public class TimeSeries {
    HashMap<String, TreeMap<Instant, Double> > dataDouble = new HashMap<>();
    HashMap<String, TreeMap<Instant, Long> > dataLong = new HashMap<>();
    HashMap<String, TreeMap<Instant, String> > dataString = new HashMap<>();
    HashMap<String, TreeMap<Instant, LocalDateTime> > dataDate = new HashMap<>();
    ArrayList<String> columns = new ArrayList<>();
    ArrayList<Instant> points = new ArrayList<>();
    private int currentIndex=0;
    public void add(Instant point, double o, double h, double l, double c, long vol, long oi)
    {
        addDouble("open",point,o);
        addDouble("high",point,h);
        addDouble("low",point,l);
        addDouble("close",point,c);
        addLong("volume", point, vol);
        addLong("oi", point, oi);
    }

    public Instant getLastPoint(){
        return points.get(points.size()-1);
    }

    public ArrayNode toJson(String col){
        ArrayNode list = AppConfig.arrayNode();
        for(Instant point: points) {
            ObjectNode result = AppConfig.objectNode();
            result.put("point", point.atZone(ZoneId.systemDefault()).format(NseLoader.yyyy_MM_dd));
            if (dataDouble.containsKey(col)) {
                result.put(col, dataDouble.get(col).get(point));
            }
            else if (dataLong.containsKey(col)) {
                result.put(col, dataLong.get(col).get(point));
            }
            else if (dataString.containsKey(col)) {
                result.put(col, dataString.get(col).get(point));
            }
            else if (dataDate.containsKey(col)) {
                result.put(col, dataDate.get(col).get(point).toString());
            }
            list.add(result);
        }
        return list;
    }

    public ArrayNode toJson(){
        ArrayNode list = AppConfig.arrayNode();
        for(Instant point: points) {
            ObjectNode result = toJson(point);
            list.add(result);
        }
        return list;
    }

    public ObjectNode toJson(Instant point){
        ObjectNode result = AppConfig.objectNode();
        result.put("point", point.atZone(ZoneId.systemDefault()).format(NseLoader.yyyy_MM_dd));
        for(String col: dataDouble.keySet())
        {
            result.put(col,dataDouble.get(col).get(point));
        }
        for(String col: dataLong.keySet())
        {
            result.put(col, dataLong.get(col).get(point));
        }
        for(String col: dataString.keySet())
        {
            result.put(col, dataString.get(col).get(point));
        }
        for(String col: dataDate.keySet())
        {
            result.put(col, dataDate.get(col).get(point).toString());
        }
        return result;
    }

    public void getSma(String col, int periods)
    {
        TreeMap<Instant,Double> column = dataDouble.get(col);
        Core c = new Core();

    }

    public ArrayList<Instant> getPoints() {
        return points;
    }

    //    public double[] getColumn(String col)
//    {
//        TreeMap<Instant,Double> column = dataDouble.get(col);
//        Double[] val ={};
//        return column.values().toArray(val);
//
//    }

    public double getDouble(Instant instant, String name){
        if( dataDouble.containsKey(name)) return dataDouble.get(name).get(instant);
        throw new IllegalStateException("Data not found at " + instant.toString());
    }

    public String getString(Instant instant, String name){
        if( dataString.containsKey(name)) return dataString.get(name).get(instant);
        throw new IllegalStateException("Data not found at " + instant.toString());
    }

    public long getLong(Instant instant, String name){
        if( dataLong.containsKey(name)) return dataLong.get(name).get(instant);
        throw new IllegalStateException("Data not found at " + instant.toString());
    }

    public void dump()
    {
        for(String col: dataDouble.keySet())
        {
            System.out.print(col + ", ");
        }
        for(String col: dataLong.keySet())
        {
            System.out.print(col + ", ");
        }
        for(String col: dataString.keySet())
        {
            System.out.print(col + ", ");
        }
        for(String col: dataDate.keySet())
        {
            System.out.print(col + ", ");
        }
        System.out.println("\n");

        Collections.sort(points);
        int count=0;
        for(Instant point: points)
        {
            count++;
            if( count == 20 ) System.out.println(".....................");
            if( ! ( count <  20 || count > points.size() - 20  ) ) continue;
            System.out.print(point.toString() + ", ");
            for(String col: dataDouble.keySet())
            {
                System.out.print( String.format("%12s", dataDouble.get(col).get(point)) + ", ");
            }
            for(String col: dataLong.keySet())
            {
                System.out.print(dataLong.get(col).get(point) + ", ");
            }
            for(String col: dataString.keySet())
            {
                System.out.print(dataString.get(col).get(point) + ", ");
            }
            for(String col: dataDate.keySet())
            {
                System.out.print(dataDate.get(col).get(point) + ", ");
            }
            System.out.print("\n");
        }
    }

    public void addDouble(String name, Instant point, Double value){
       if( ! dataDouble.containsKey( name))
       {
           dataDouble.put(name, new TreeMap<>());
           if(!columns.contains(name)) columns.add(name);
       }
       if(! points.contains(point)) points.add(point);
       TreeMap<Instant, Double> series = dataDouble.get(name);
       series.put(point,value);
    }

    public void addLong(String name, Instant point, Long value){
        if( ! dataLong.containsKey( name))
        {
            dataLong.put(name, new TreeMap<>());
            if(!columns.contains(name)) columns.add(name);
        }
        if(! points.contains(point)) points.add(point);
        TreeMap<Instant, Long> series = dataLong.get(name);
        series.put(point,value);
    }

    public void addString(String name, Instant point, String value){
        if( ! dataString.containsKey( name))
        {
            dataString.put(name, new TreeMap<>());
            if(!columns.contains(name)) columns.add(name);
        }
        if(! points.contains(point)) points.add(point);
        TreeMap<Instant, String> series = dataString.get(name);
        series.put(point,value);
    }
}
