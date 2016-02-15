package com.punwire.kat.chart2;

import com.punwire.kat.data.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 15/01/16.
 */
public class MainWindow extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final Interval defaultInterval = new DailyInterval();

    Yahoo y = new Yahoo();


    public MainWindow() {
        this.setBackground(new Color(43, 42, 42));
        ChartFrame chartFrame = new ChartFrame();
//        desktopPane.add(chartFrame);
//        chartFrame.pack();
//        chartFrame.setVisible(true);
//        try {
//            chartFrame.setMaximum(true);
//        } catch(Exception ex)
//        {
//            ex.printStackTrace();
//        }
        desktopPane.setBackground(new Color(43, 42, 42));


        MongoProvider mProvider = new MongoProvider();

        ChartFrame cf = new ChartFrame();
        ChartData cd = new ChartData();
        try {
            mProvider.fetchStock("NIFTY-I");
        } catch (Exception e) {
            e.printStackTrace();
        }

        cd.setInterval(defaultInterval);
        cd.setDataProviderName("MongoProvider");
        Stock nifty = new Stock("NIFTY-I");
        cd.setStock(nifty);

        cf.setChartData(cd);

        CandleStick cs = new CandleStick();

        OHLC ohlc = new OHLC();
        cd.setChart(cs);

        cf.loading(nifty, new FiveMinuteInterval(), true);
        //desktopPane.add(mp);
        this.add(cf);
        cf.init();

        //this.add(desktopPane, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(300, 300));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            MainWindow frame = new MainWindow();
            frame.pack();
            frame.setVisible(true);
            frame.setExtendedState(frame.MAXIMIZED_BOTH);
        });
    }
}
