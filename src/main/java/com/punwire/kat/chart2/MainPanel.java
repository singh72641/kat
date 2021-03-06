package com.punwire.kat.chart2;

import com.punwire.kat.core.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * Created by Kanwal on 16/01/16.
 */
public class MainPanel extends JLayeredPane implements Serializable {

    private static final long serialVersionUID = AppConfig.APPVERSION;

    private ChartFrame chartFrame;
    private ChartSplitPanel sPane;
    private Grid grid;
    private DateAxis dateAxis;
    private PriceAxis priceAxis;

    public MainPanel(ChartFrame frame) {
        chartFrame = frame;
        sPane = new ChartSplitPanel(chartFrame);
        grid = new Grid(chartFrame);
        dateAxis = new DateAxis(chartFrame);
        priceAxis = new PriceAxis(chartFrame);

        setOpaque(true);
        setBackground(chartFrame.getChartProperties().getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(2, 20, 0, 0));
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

                grid.setBounds(insets.left, insets.top, w, h);
                dateAxis.setBounds(insets.left, insets.top + h, w, bottom);
                priceAxis.setBounds(insets.left + insets.right + w, insets.top, right, insets.top + insets.bottom + h);
                sPane.setBounds(insets.left, insets.top, w, h);
            }
        });

        add(sPane);
        add(dateAxis);
        add(priceAxis);
        add(grid);
//
//        putClientProperty("print.printable", Boolean.TRUE);
//        putClientProperty("print.name", "");
    }

    public ChartSplitPanel getSplitPanel() {
        return sPane;
    }

    @Override
    public void paint(Graphics g) {
        chartFrame.getChartData().calculate(chartFrame);
        chartFrame.getChartData().calculateRange(chartFrame, sPane.getChartPanel().getOverlays());
        setBackground(chartFrame.getChartProperties().getBackgroundColor());
        super.paint(g);
    }
}
