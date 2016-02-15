package com.punwire.kat.chart2;

import com.punwire.kat.data.Dataset;
import com.punwire.kat.data.Range;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kanwal on 16/01/16.
 */
public abstract class Overlay extends ChartFrameAdapter implements Serializable {

    protected String datasetKey;
    protected ConcurrentHashMap<String, Dataset> datasets;
    protected boolean active = true;

    public abstract String getName();

    public abstract String getLabel();

    public abstract Overlay newInstance();

    public abstract LinkedHashMap getHTML(ChartFrame cf, int i);

    public abstract void paint(Graphics2D g, ChartFrame cf, Rectangle bounds);

    public abstract void calculate();

    public abstract Color[] getColors();

    public abstract double[] getValues(ChartFrame cf);

    public abstract double[] getValues(ChartFrame cf, int i);

    public abstract boolean getMarkerVisibility();

    public abstract String getPrice();

    /**
     * If an override in the overlay class sets this to false
     * that overlay is not included in the range calculation of the chart2.
     *
     * @return whether to include this overlay in chart2 range
     */
    public boolean isIncludedInRange()
    {
        return true;
    }

    public Dataset getDataset()
    {
        return new Dataset();
        //return DatasetUsage.getInstance().getDatasetFromMemory(datasetKey);
    }

    public void setDatasetKey(String datasetKey)
    {
        this.datasetKey = datasetKey;
    }

    public void clearDatasets()
    {
        datasets.clear();
    }

    public void addDataset(String key, Dataset value)
    {
        datasets.put(key, value);
    }

    public Dataset getDataset(String key)
    {
        return datasets.get(key);
    }

    private boolean datasetExists(String key)
    {
        return datasets.containsKey(key);
    }

    public Dataset visibleDataset(ChartFrame cf, String key)
    {
        if (datasetExists(key))
        {
            Dataset dataset = getDataset(key);
            if (dataset == null)
            {
                return null;
            }

            int period = cf.getChartData().getPeriod();
            int last = cf.getChartData().getLast();
            Dataset visible = dataset.getVisibleDataset(period, last);
            return visible;
        }
        return null;
    }

    public Range getRange(ChartFrame cf, String price)
    {
        Range range = null;
        String[] keys = datasets.keySet().toArray(new String[datasets.size()]);
        for (String key : keys)
        {
            Dataset dataset = visibleDataset(cf, key);
            double min = dataset.getMinNotZero(price);
            double max = dataset.getMaxNotZero(price);
            if (range == null)
            {
                range = new Range(min - (max - min) * 0.01, max + (max - min) * 0.01);
            } else
            {
                range = Range.combine(range, new Range(min - (max - min) * 0.01, max + (max - min) * 0.01));
            }
        }
        return range;
    }

}
