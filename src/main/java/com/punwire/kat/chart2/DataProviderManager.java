package com.punwire.kat.chart2;

import com.punwire.kat.data.DataProvider;
import com.punwire.kat.data.MongoProvider;
import com.punwire.kat.data.Yahoo;

import java.util.*;

public class DataProviderManager
{

    private static DataProviderManager instance;
    private static List<String> ignored;
    private LinkedHashMap<String, DataProvider> dataProviders;
    private boolean updated = false;

    public static DataProviderManager getDefault()
    {
        if (instance == null)
        {
            instance = new DataProviderManager();
        }
        return instance;
    }

    private DataProviderManager()
    {
        dataProviders = new LinkedHashMap<String, DataProvider>();
        //Collection<? extends DataProvider> list = new ArrayList<>();
        //list.add(Yahoo.class);
        DataProvider dp = new MongoProvider();
//        for (DataProvider dp : list)
//        {
            if (!ignored.contains(dp.getName()))
            {
                dp.initialize();
                dataProviders.put(dp.getName(), dp);
            }
//        }

        sort();
    }

    private void sort()
    {
        List<String> mapKeys = new ArrayList<String>(dataProviders.keySet());
        Collections.sort(mapKeys);

        LinkedHashMap<String, DataProvider> someMap = new LinkedHashMap<String, DataProvider>();
        for (int i = 0; i < mapKeys.size(); i++)
        {
            someMap.put(mapKeys.get(i), dataProviders.get(mapKeys.get(i)));
        }
        dataProviders = someMap;
    }

    public DataProvider getDataProvider(String key)
    {
        return dataProviders.get(key);
    }

    public List<String> getDataProviders()
    {
        List<String> list = new ArrayList<String>(dataProviders.keySet());
        Collections.sort(list);
        return list;
    }

    static
    {
        ignored = new ArrayList<String>();
        //ignored.add("Yahoo");
    }
}
