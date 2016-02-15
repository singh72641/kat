package com.punwire.kat.chart;

import com.punwire.kat.chart.*;
import com.punwire.kat.data.FiveMinuteInterval;
import com.punwire.kat.data.Stock;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by Kanwal on 19/01/16.
 */
public class OaMainWindow extends JFrame {

    private OaChartData cd;
    public static ChartProperties cp;

    private		JTabbedPane tabbedPane;
    private		JPanel		chartPanel;
    private		JPanel		optionPanel;
    private		JPanel		portfolioPanel;


    public OaMainWindow() {
        setFontSize();
        initUI();
    }

    private static void setFontSize() {
        int fontSize = 28;
        Hashtable defaults = UIManager.getDefaults();
        Enumeration keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            if ((key instanceof String) && (((String) key).endsWith(".font"))) {
                FontUIResource font = (FontUIResource) UIManager.get(key);
                defaults.put (key, new FontUIResource(font.getFontName(), font.getStyle(), fontSize));
            }
        }
    }
    private void initUI() {
        setBackground(AppStyle.BACKGROUD);
        setTitle( "KTrader" );

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(AppStyle.BACKGROUD);
        tabbedPane.setForeground(AppStyle.TEXT);

        optionPanel = new JPanel();
        optionPanel.setLayout(new BorderLayout());
        tabbedPane.addTab("Options", optionPanel);

        JLabel lab = new JLabel("Options");
        lab.setPreferredSize(new Dimension(200, 40));
        tabbedPane.setTabComponentAt(0, lab);

        OptionTable oTable = new OptionTable();
        optionPanel.add(oTable, BorderLayout.CENTER);



        cp = new ChartProperties();
        cd = new OaChartData(cp, new Stock("NIFTY-I"), new FiveMinuteInterval());

        cd.fetch();
        OaChart oc = new OaChart(cd);
        oc.addBolinger();
        tabbedPane.addTab("Chart", oc);

        getContentPane().add(tabbedPane);

        setPreferredSize(new Dimension(800, 800));

        setTitle("Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
//
//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                System.out.println(info.getName());
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            // If Nimbus is not available, you can set the GUI to another look and feel.
//        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                OaMainWindow ex = new OaMainWindow();
                ex.setVisible(true);
                ex.setExtendedState(ex.MAXIMIZED_BOTH);
            }
        });
    }
}

