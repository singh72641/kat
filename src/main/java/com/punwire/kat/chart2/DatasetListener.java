package com.punwire.kat.chart2;


import java.util.EventListener;

public interface DatasetListener
        extends EventListener
{

    public void datasetChanged(DatasetEvent evt);

}
