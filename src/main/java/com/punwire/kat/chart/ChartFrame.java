package com.punwire.kat.chart;

import com.punwire.kat.chart.*;
import com.punwire.kat.data.FiveMinuteInterval;
import com.punwire.kat.data.Stock;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 17/01/16.
 */
public class ChartFrame extends JLayeredPane {

    private ChartPanel chartPanel;
    private GridPanel gridPanel;
    private DateAxis dateAxis;
    private ChartData chartData;
    private ChartProperties chartProperties;
    private CandleChart candleChart;

    public ChartFrame(){
        setOpaque(true);
        chartProperties = new ChartProperties();
        chartPanel = new ChartPanel(this);
        gridPanel = new GridPanel(this);
        dateAxis = new DateAxis(this);
        candleChart = new CandleChart(this);
        setBackground(new Color(50, 50, 50));
        setLayout(new LayoutManager() {
            public void addLayoutComponent(String name, Component comp) {
            }

            public void removeLayoutComponent(Component comp) {
            }

            public Dimension preferredLayoutSize(Container parent) {
                return new Dimension(0, 0);
            }

            public Dimension minimumLayoutSize(Container parent) {
                return new Dimension(0, 0);
            }

            public void layoutContainer(Container parent) {
                int right = (int) ChartData.dataOffset.right;
                int bottom = (int) ChartData.dataOffset.bottom;
                Insets insets = parent.getInsets();
                int w = parent.getWidth() - insets.left - insets.right - right;
                int h = parent.getHeight() - insets.top - insets.bottom - bottom;
                gridPanel.setBounds(insets.left, insets.top, w, h);
                candleChart.setBounds(insets.left, insets.top, w, h);
                chartPanel.setBounds(insets.left, insets.top, w, h);
                dateAxis.setBounds(insets.left, insets.top + h, w, bottom);
            }
        });


        add(gridPanel, new Integer(1));
        add(dateAxis, new Integer(2));
        add(chartPanel, new Integer(3));
        add(candleChart, new Integer(3));
        chartData = new ChartData();
        Stock nifty = new Stock("NIFTY-I");
        FiveMinuteInterval interval = new FiveMinuteInterval();
        chartData.setStock(nifty);
        chartData.setInterval(interval);
        chartData.fetch();
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public ChartProperties getChartProperties() {
        return chartProperties;
    }

    public ChartData getChartData() {
        return chartData;
    }

    @Override
    public void paint(Graphics g) {
        chartData.calculate(this);
        chartData.calculateRange(this);
        super.paint(g);
    }
}
