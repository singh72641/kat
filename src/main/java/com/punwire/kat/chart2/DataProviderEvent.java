package com.punwire.kat.chart2;

import java.util.EventObject;

/**
 * Created by Kanwal on 16/01/16.
 */
public class DataProviderEvent extends EventObject
{

    private int itemsAdded;

    public DataProviderEvent(Object source, int itemsAdded)
    {
        super(source);
        this.itemsAdded = itemsAdded;
    }

    public int getItemsAdded()
    {
        return this.itemsAdded;
    }

}
