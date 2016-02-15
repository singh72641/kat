package com.punwire.kat.chart2;

import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.Dataset;
import com.punwire.kat.data.Range;

import java.awt.*;

/**
 * Created by Kanwal on 16/01/16.
 */
public class OHLC extends Chart{
    private static final long serialVersionUID = AppConfig.APPVERSION;

    @Override
    public String getName() {
        return "OHLC Bars";
    }

    public void paint(Graphics2D g, ChartFrame cf)
    {
        ChartData cd = cf.getChartData();
        ChartProperties cp = cf.getChartProperties();
        boolean isLog = cp.getAxisLogarithmicFlag();
        Rectangle rect = cf.getSplitPanel().getChartPanel().getBounds();
        rect.grow(-2, -2);
        Range range = cf.getSplitPanel().getChartPanel().getRange();

        if (!cd.isVisibleNull())
        {
            Dataset dataset = cd.getVisible();
            for(int i = 0; i < dataset.getItemsCount(); i++)
            {
                double open = dataset.getOpenAt(i);
                double close = dataset.getCloseAt(i);
                double high = dataset.getHighAt(i);
                double low = dataset.getLowAt(i);

                double x = cd.getX(i, rect);
                double yOpen = cd.getY(open, rect, range, isLog);
                double yClose = cd.getY(close, rect, range, isLog);
                double yHigh = cd.getY(high, rect, range, isLog);
                double yLow = cd.getY(low, rect, range, isLog);

                double candleWidth = cp.getBarWidth();

                g.setPaint(open > close ? cp.getBarDownColor() : cp.getBarUpColor());
                g.draw(CoordCalc.line(x, yLow, x, yHigh));
                g.draw(CoordCalc.line(x, yOpen, x - candleWidth/2, yOpen));
                g.draw(CoordCalc.line(x, yClose, x + candleWidth/2, yClose));
            }
        }
    }
}
