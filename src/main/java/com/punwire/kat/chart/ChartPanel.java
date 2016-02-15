package com.punwire.kat.chart;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 17/01/16.
 */
public class ChartPanel extends JLayeredPane {

    private ChartFrame chartFrame;

    public ChartPanel(ChartFrame chartFrame) {
        this.chartFrame = chartFrame;
        setOpaque(false);
        setDoubleBuffered(true);
        //CandleChart candleChart = new CandleChart(chartFrame);



//        setLayout(new LayoutManager() {
//            public void addLayoutComponent(String name, Component comp) {
//            }
//
//            public void removeLayoutComponent(Component comp) {
//            }
//
//            public Dimension preferredLayoutSize(Container parent) {
//                return new Dimension(0, 0);
//            }
//
//            public Dimension minimumLayoutSize(Container parent) {
//                return new Dimension(0, 0);
//            }
//
//            public void layoutContainer(Container parent) {
//                int right = (int) ChartData.dataOffset.right;
//                int bottom = (int) ChartData.dataOffset.bottom;
//                Insets insets = parent.getInsets();
//                int w = parent.getWidth() - insets.left - insets.right - right;
//                int h = parent.getHeight() - insets.top - insets.bottom - bottom;
//                candleChart.setBounds(insets.left, insets.top + h, w, bottom);
//            }
//        });

       // add(candleChart, new Integer(5));
        System.out.println("Chart Panel Formed");
    }
}
