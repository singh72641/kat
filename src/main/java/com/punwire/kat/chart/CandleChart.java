package com.punwire.kat.chart;

import com.punwire.kat.chart2.CoordCalc;
import com.punwire.kat.chart2.GraphicsUtils;
import com.punwire.kat.data.Dataset;
import com.punwire.kat.data.Range;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 17/01/16.
 */
public class CandleChart extends JPanel {

    private ChartFrame chartFrame;
    public CandleChart(ChartFrame chartFrame){
        this.chartFrame = chartFrame;
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g2) {
        System.out.println("Displaying Candle Chart");
        Graphics2D g = GraphicsUtils.prepareGraphics(g2);
        ChartData cd = chartFrame.getChartData();
        ChartProperties cp = chartFrame.getChartProperties();
        boolean isLog = cp.getAxisLogarithmicFlag();
        Rectangle rect = chartFrame.getChartPanel().getBounds();
        rect.grow(-2, -2);
        Range range = cd.getVisibleRange();

        if (!cd.isVisibleNull() && !cd.getVisible().isEmpty())
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
                double candleHeight = Math.abs(yOpen - yClose);

                if (open > close ? cp.getBarDownVisibility() : cp.getBarUpVisibility())
                {
                    g.setPaint(open > close ? cp.getBarDownColor() : cp.getBarUpColor());
                    g.fill(CoordCalc.rectangle(x - candleWidth / 2, (open > close ? yOpen : yClose), candleWidth - 8, candleHeight));
                }

                if (cp.getBarVisibility())
                {
                    g.setPaint(cp.getBarColor());
                    g.setStroke(cp.getBarStroke());
                    g.draw(CoordCalc.line(x, (open > close ? yOpen : yClose), x, yHigh));
                    g.draw(CoordCalc.line(x, (open > close ? yClose : yOpen), x, yLow));
                    g.draw(CoordCalc.rectangle(x - candleWidth/2, (open > close ? yOpen : yClose), candleWidth - 8, candleHeight));
                }
            }
        }
    }
}
