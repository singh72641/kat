package com.punwire.kat.chart2;

import com.punwire.kat.core.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * Created by Kanwal on 16/01/16.
 */
public class ChartSplitPanel extends JLayeredPane implements Serializable{
    private static final long serialVersionUID = AppConfig.APPVERSION;

    private ChartFrame chartFrame;
    private ChartPanel chartPanel;
    private IndicatorsPanel indicatorsPanel;
    //private MarkerLabel label;

    private int index = -1;
    private static int width = 200;
    private static int height;
    private int lines = 5;

    private Color lineColor = new Color(0xef2929);
    private Color color = new Color(0x1C2331);
    private Color backgroundColor = ColorGenerator.getTransparentColor(color, 100);
    private Color fontColor = new Color(0xffffff);

    private static Font font;

    static
    {
        font = new Font("Dialog", Font.PLAIN, 10);
        height = 14;
    }

    public ChartSplitPanel(ChartFrame frame)
    {
        chartFrame = frame;
        chartPanel = new ChartPanel(chartFrame);
        indicatorsPanel = new IndicatorsPanel(chartFrame);
        //label = new MarkerLabel();

        setOpaque(false);
        setDoubleBuffered(true);

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int w = parent.getWidth() - insets.left - insets.right;
                int h = parent.getHeight() - insets.top - insets.bottom;
                int indicatorsHeight = indicatorsPanel.getPanelHeight();
                int chartHeight = h - indicatorsHeight;

                indicatorsPanel.setBounds(x, y + chartHeight, w, indicatorsHeight);
                chartPanel.setBounds(x, y, w, chartHeight);

                Point dp = new Point(0, 50);
//                Point p = label.getLocation();
//                if (!dp.equals(p))
//                    label.setBounds(p.x, p.y, width + 2, height * lines + 2);
//                else
//                    label.setBounds(dp.x, dp.y, width + 2, height * lines + 2);
            }
        });

        //add(label);
        add(indicatorsPanel);
        add(chartPanel);
        //label.setLocation(0, 50);
    }


    public ChartFrame getChartFrame()
    { return chartFrame; }

    public void setChartFrame(ChartFrame frame)
    { chartFrame = frame; }

    public ChartPanel getChartPanel()
    { return chartPanel; }

    public void setChartPanel(ChartPanel panel)
    { chartPanel = panel; }

    public IndicatorsPanel getIndicatorsPanel()
    { return indicatorsPanel; }

    public void setIndicatorsPanel(IndicatorsPanel panel)
    { indicatorsPanel = panel; }

    public void setIndex(int i)
    { index = i; }

    public int getIndex()
    { return index; }

}
