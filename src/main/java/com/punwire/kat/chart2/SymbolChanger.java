package com.punwire.kat.chart2;


import com.punwire.kat.core.AppConfig;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.Serializable;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.AbstractDocument;

public class SymbolChanger  extends JToolBar implements Serializable {
    private static final long serialVersionUID = AppConfig.APPVERSION;
    //private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private ChartFrame chartFrame;
    private JTextField txtSymbol;
    private JButton btnSubmit;
    private JButton btnBack;
    private JButton btnForward;
    private JButton btnBackHistory;
    private JButton btnForwardHistory;
    private String dataProvider;
    private boolean canOpen = true;

    public SymbolChanger(ChartFrame frame)
    {
        super(JToolBar.HORIZONTAL);
        chartFrame = frame;
        setFloatable(false);
        setOpaque(false);
        setDoubleBuffered(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void init()
    {
        Insets margins = new Insets(0, 2, 0, 2);

        // symbol text field
        txtSymbol = new JTextField(6);
        ((AbstractDocument) txtSymbol.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
        txtSymbol.setMargin(margins);
        txtSymbol.setText(chartFrame.getChartData().getStock().getKey());
        Dimension d = new Dimension(50, 20);
        txtSymbol.setPreferredSize(d);
        txtSymbol.setMinimumSize(d);
        txtSymbol.setMaximumSize(d);

        // submit button
//        btnSubmit = new JButton(new ChangeStock());
//        btnSubmit.setText("");
//
//        // back button
//        btnBack = new JButton(new BackAction());
//        btnBack.setText("");
//
//        // forward button
//        btnForward = new JButton(new ForwardAction());
//        btnForward.setText("");
//
//        // back history button
//        btnBackHistory = new JButton(new BackListAction());
//        btnBackHistory.setText("");
//
//        // forward history button
//        btnForwardHistory = new JButton(new ForwardListAction());
//        btnForwardHistory.setText("");
//
//        if (!chartFrame.getHistory().hasBackHistory())
//        {
//            btnBack.setEnabled(false);
//            btnBackHistory.setEnabled(false);
//        }
//        if (!chartFrame.getHistory().hasFwdHistory())
//        {
//            btnForward.setEnabled(false);
//            btnForwardHistory.setEnabled(false);
//        }

        add(txtSymbol);
//        add(btnSubmit);
//        add(btnBack);
//        add(btnForward);
//        add(btnBackHistory);
//        add(btnForwardHistory);

        dataProvider = chartFrame.getChartData().getDataProviderName();
//        final StockAutoCompleter completer = new StockAutoCompleter(txtSymbol);
//        completer.setDataProvider(dataProvider);
    }



    private static abstract class SymbolChangerAction extends AbstractAction
    {

        public SymbolChangerAction(String name, String tooltip, String icon)
        {
            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, tooltip);
            if (icon != null)
            {
                //putValue(SMALL_ICON, ResourcesUtils.getIcon(icon));
                //putValue(LARGE_ICON_KEY, ResourcesUtils.getIcon(icon));
            }
        }
    }

}
