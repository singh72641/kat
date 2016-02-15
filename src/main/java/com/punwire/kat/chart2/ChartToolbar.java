package com.punwire.kat.chart2;

import com.punwire.kat.core.AppConfig;

import java.io.Serializable;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JToolBar;

public class ChartToolbar extends JToolBar implements Serializable, PreferenceChangeListener
{
    private static final long serialVersionUID = AppConfig.APPVERSION;

    private ChartFrame chartFrame;
    private SymbolChanger symbolChanger;
    //private Preferences chatPreferences = NbPreferences.root().node("/org/chartsy/chat");

    public ChartToolbar(ChartFrame frame)
    {
        super("ChartToolbar", JToolBar.HORIZONTAL);
        chartFrame = frame;
        initComponents();
        setFloatable(false);
        setDoubleBuffered(true);
        //setBorder(new BottomBorder());
        //addMouseListener(new ToolbarOptions(this));
        //chatPreferences.addPreferenceChangeListener((PreferenceChangeListener) this);
    }
    private void initComponents()
    {
        // SymbolChanger Toolbar
        symbolChanger = new SymbolChanger(chartFrame);
        add(symbolChanger);

        // ChartToolbar buttons
//        add(zoomInBtn = ToolbarButton.getButton(MainActions.zoomIn(chartFrame)));
//        add(zoomOutBtn = ToolbarButton.getButton(MainActions.zoomOut(chartFrame)));
//        add(intervalsBtn = ToolbarButton.getButton(MainActions.intervalPopup(chartFrame)));
//        add(chartBtn = ToolbarButton.getButton(MainActions.chartPopup(chartFrame)));
//        add(indicatorsBtn = ToolbarButton.getButton(MainActions.openIndicators(chartFrame)));
//        add(overlaysBtn = ToolbarButton.getButton(MainActions.openOverlays(chartFrame)));
//        add(annotationsBtn = ToolbarButton.getButton(MainActions.annotationPopup(chartFrame)));
//        add(markerBtn = ToolbarToggleButton.getButton(MainActions.toggleMarker(chartFrame)));
//        add(exportBtn = ToolbarButton.getButton(MainActions.exportImage(chartFrame)));
//        add(printBtn = ToolbarButton.getButton(MainActions.printChart(chartFrame)));
//        add(propertiesBtn = ToolbarButton.getButton(MainActions.chartProperties(chartFrame)));
//        add(joinConference = ToolbarButton.getButton(MainActions.joinToConference(chartFrame)));
//        add(postFacebook = ToolbarButton.getButton(MainActions.postOnFacebook(chartFrame)));
//        add(postTwitter = ToolbarButton.getButton(MainActions.postOnTwitter(chartFrame)));
//
//        postFacebook.setButtonWidth(50);
//        postTwitter.setButtonWidth(50);
//
//        markerBtn.setSelected(true);
//        joinConference.setVisible(chatPreferences.getBoolean("loggedin", false));
    }
    public void updateToolbar()
    {
        //symbolChanger.updateToolbar();
    }


    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
//        if (evt.getKey().equals("loggedin"))
            //joinConference.setVisible(evt.getNode().getBoolean("loggedin", false));
    }


}
