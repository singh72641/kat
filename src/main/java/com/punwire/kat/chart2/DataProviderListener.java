package com.punwire.kat.chart2;

import java.util.EventListener;

/**
 * Created by Kanwal on 16/01/16.
 */
public interface DataProviderListener extends EventListener
{

    public void triggerDataProviderListener(DataProviderEvent evt);

}
