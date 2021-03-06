package com.punwire.kat.chart2;

import com.punwire.kat.data.Interval;
import com.punwire.kat.data.Stock;

import java.util.EventListener;

public interface ChartFrameListener extends EventListener
{

    public void stockChanged(Stock newStock);
    public void intervalChanged(Interval newInterval);
    public void chartChanged(Chart newChart);
    public void datasetKeyChanged(String datasetKey);
    public void indicatorAdded(Indicator indicator);
    public void indicatorRemoved(Indicator indicator);
    public void overlayAdded(Overlay overlay);
    public void overlayRemoved(Overlay overlay);
    public double zoomIn(double barWidth);
    public double zoomOut(double barWidth);

}
