package com.punwire.kat.chart;

import com.punwire.kat.chart.*;
import com.punwire.kat.chart2.CoordCalc;
import com.punwire.kat.chart2.GraphicsUtils;
import com.punwire.kat.data.Range;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 17/01/16.
 */
public class GridPanel extends JPanel {

    private ChartFrame chartFrame;

    public GridPanel(ChartFrame chartFrame) {
        this.chartFrame = chartFrame;
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = GraphicsUtils.prepareGraphics(g);
        ChartProperties cp = chartFrame.getChartProperties();
        ChartData cd = chartFrame.getChartData();
        Range chartRange = cd.getVisibleRange();
        Rectangle chartBounds = chartFrame.getChartPanel().getBounds();
        chartBounds.grow(-2, -2);
        System.out.println(cd.getPeriod());
        double x, y;
        // Vertical Grid
        if (cp.getGridVerticalVisibility()) {
            g2.setColor(cp.getGridVerticalColor());
            g2.setStroke(cp.getGridVerticalStroke());
            double[] list = cd.getDateValues();
            boolean firstFlag = true;


            for (int i = 0; i < list.length; i++) {
                double value = list[i];
                if (value != -1) {
                    x = cd.getX(value, chartBounds);
                    if (firstFlag) {
                        int index = (int) value;
                        long time = cd.getVisible().getTimeAt(index);
                        if (cd.isFirstWorkingDayOfMonth(time))
                            g2.draw(CoordCalc.line(x, 0, x, getHeight()));
                        firstFlag = false;
                    } else {
                        g2.draw(CoordCalc.line(x, 0, x, getHeight()));
                    }
                }
            }
        }

        // Horizontal Grid
        if (cp.getGridHorizontalVisibility()) {
            // paint grid for chart2
            g2.setColor(cp.getGridHorizontalColor());
            g2.setStroke(cp.getGridHorizontalStroke());
            FontMetrics fm = getFontMetrics(chartFrame.getChartProperties().getFont());
            double[] list = cd.getYValues(chartBounds, chartRange, fm.getHeight());
            for (int i = 0; i < list.length; i++) {
                double value = list[i];
                y = cd.getY(value, chartBounds, chartRange, false);
                if (chartBounds.contains(2, y)) {
                    g2.draw(CoordCalc.line(0, y, getWidth(), y));
                }
            }


        }

    }

}
