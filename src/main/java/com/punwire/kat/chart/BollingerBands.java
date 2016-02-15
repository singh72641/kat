package com.punwire.kat.chart;

import com.punwire.kat.chart2.CoordCalc;
import com.punwire.kat.data.DataItem;
import com.punwire.kat.data.Dataset;
import com.punwire.kat.data.Range;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kanwal on 20/01/16.
 */
public class BollingerBands
{
    public static final String FULL_NAME = "Bollinger Bands";
    public static final String UPPER = "upper";
    public static final String MIDDLE = "middle";
    public static final String LOWER = "lower";
    private OaChartData cd;
    private Color uppperColor = Color.red;
    private Color middleColor = Color.gray;
    private Color lowerColor = Color.green;

    protected String datasetKey;
    protected ConcurrentHashMap<String, Dataset> datasets;

    public BollingerBands(OaChartData chartData)
    {
        this.cd = chartData;
        datasets = new ConcurrentHashMap<>();
    }

    public String getName(){ return FULL_NAME; }

    public String getLabel()
    {
        //return properties.getLabel() + " (" + properties.getPrice() + ", " + properties.getStd() + ", " + properties.getPeriod() + ")";
        return "label";
    }

    public void paint(Graphics2D g, Rectangle bounds)
    {
        Dataset middle = visibleDataset(MIDDLE);
        Dataset upper = visibleDataset(UPPER);
        Dataset lower = visibleDataset(LOWER);
        ChartProperties cp = OaMainWindow.cp;

        if (middle != null && upper != null && lower != null)
        {
            String price = "Close";
            Range range = cd.getVisibleRange();

//            if (properties.getInsideVisibility())
//                DefaultPainter.insideFill(g, cf, range, bounds, upper, lower, properties.getInsideTransparentColor(), Dataset.getPrice(price));

            DefaultPainter.line(g, cd, range, bounds, middle, middleColor, cp.getGridVerticalStroke(), Dataset.getPrice(price)); // paint middle line
            DefaultPainter.line(g, cd, range, bounds, upper, uppperColor, cp.getGridVerticalStroke(), Dataset.getPrice(price)); // paint upper line
            DefaultPainter.line(g, cd, range, bounds, lower, lowerColor, cp.getGridVerticalStroke(), Dataset.getPrice(price)); // paint lower line
        }
    }

    public void calculate()
    {
        int period = 15;
        int stddev = 2;
        Dataset initial = cd.getFullData();
        if (initial != null && !initial.isEmpty())
        {
            Dataset middle = Dataset.SMA(initial, period);
            addDataset(MIDDLE, middle);

            Dataset upper = getLowerUpperDataset(initial, middle, period, stddev, UPPER);
            addDataset(UPPER, upper);

            Dataset lower = getLowerUpperDataset(initial, middle, period, stddev, LOWER);
            addDataset(LOWER, lower);
        }
    }

    public void addDataset(String key, Dataset value)
    {
        datasets.put(key, value);
    }


    private Dataset getLowerUpperDataset(final Dataset initial, final Dataset middle, final int period, final int stddev, final String type)
    {
        int count = initial.getItemsCount();
        Dataset d = Dataset.EMPTY(count);

        for (int i = period; i < count; i++)
        {
            double opendev = 0;
            double highdev = 0;
            double lowdev = 0;
            double closedev = 0;

            for (int j = 0; j < period; j++)
            {
                opendev += Math.pow(initial.getOpenAt(i-j) - middle.getOpenAt(i), 2);
                highdev += Math.pow(initial.getHighAt(i-j) - middle.getHighAt(i), 2);
                lowdev += Math.pow(initial.getLowAt(i-j) - middle.getLowAt(i), 2);
                closedev += Math.pow(initial.getCloseAt(i-j) - middle.getCloseAt(i), 2);
            }

            opendev = stddev * Math.sqrt(opendev / period);
            highdev = stddev * Math.sqrt(highdev / period);
            lowdev = stddev * Math.sqrt(lowdev / period);
            closedev = stddev * Math.sqrt(closedev / period);

            if (type.equals(LOWER))
                d.setDataItem(i, new DataItem(middle.getTimeAt(i), middle.getOpenAt(i) - opendev, middle.getHighAt(i) - highdev, middle.getLowAt(i) - lowdev, middle.getCloseAt(i) - closedev, 0));
            else
                d.setDataItem(i, new DataItem(middle.getTimeAt(i), middle.getOpenAt(i) + opendev, middle.getHighAt(i) + highdev, middle.getLowAt(i) + lowdev, middle.getCloseAt(i) + closedev, 0));
        }

        return d;
    }

    public Color[] getColors()
    { return new Color[] {Color.red, Color.gray, Color.green}; }

    public double[] getValues(ChartFrame cf)
    {
        Dataset middle = visibleDataset(MIDDLE);
        Dataset upper = visibleDataset(UPPER);
        Dataset lower = visibleDataset(LOWER);

        int i = middle.getLastIndex();
        double[] values = new double[3];
        values[0] = upper.getDataItem(i) != null ? upper.getCloseAt(i) : 0;
        values[1] = middle.getDataItem(i) != null ? middle.getCloseAt(i) : 0;
        values[2] = lower.getDataItem(i) != null ? lower.getCloseAt(i) : 0;

        return values;
    }

    public double[] getValues(ChartFrame cf, int i)
    {
        Dataset middle = visibleDataset(MIDDLE);
        Dataset upper = visibleDataset(UPPER);
        Dataset lower = visibleDataset(LOWER);

        double[] values = new double[3];
        values[0] = upper.getDataItem(i) != null ? upper.getCloseAt(i) : 0;
        values[1] = middle.getDataItem(i) != null ? middle.getCloseAt(i) : 0;
        values[2] = lower.getDataItem(i) != null ? lower.getCloseAt(i) : 0;

        return values;
    }

    public Dataset visibleDataset(String key)
    {
        if (datasets.containsKey(key))
        {
            Dataset dataset = datasets.get(key);
            if (dataset == null)
            {
                return null;
            }

            int period = cd.period;
            int last = cd.last;
            Dataset visible = dataset.getVisibleDataset(period, last);
            return visible;
        }
        return null;
    }

}
