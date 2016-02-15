package com.punwire.kat.chart;

import com.punwire.kat.chart2.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Kanwal on 17/01/16.
 */
public class MainWindow extends JFrame{

    public MainWindow() {
        this.setBackground(new Color(43, 42, 42));
        ChartFrame chartFrame = new ChartFrame();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(chartFrame, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
