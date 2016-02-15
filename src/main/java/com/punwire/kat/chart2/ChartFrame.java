package com.punwire.kat.chart2;

import com.punwire.kat.core.ResBundle;
import com.punwire.kat.data.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.EventListenerList;
/**
 * Created by Kanwal on 15/01/16.
 */
public class ChartFrame extends JComponent implements AdjustmentListener, MouseWheelListener, DataProviderListener {

    public static Color BACKCOLOR = new Color(43, 42, 42);
    private static AtomicInteger ID;
    private static String PREFERRED_ID;
    public static final Logger LOG = Logger.getLogger(ChartFrame.class.getName());
    //private static final RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);

    private ChartProperties chartProperties;
    private ChartData chartData;
    private Template template;
    //private History history;
    //private transient AbstractNode node;

    private ChartToolbar chartToolbar;
    private MainPanel mainPanel;
    private JScrollBar scrollBar;
    private JPopupMenu popupMenu;

    private boolean initialized = false;
    private boolean restored = false;
    private boolean focus = true;
    boolean loadingError = false;

    private Stock oldStock = null;
    private Interval oldInterval = null;
    //private transient RequestProcessor.Task task;
    private transient EventListenerList chartFrameListeners;

    public ChartFrame()
    {
        PREFERRED_ID = "1";
        setLayout(new BorderLayout());
        setName("CTL_ChartFrameEmpty");
        setToolTipText("TOOL_ChartFrameEmpty");

        chartProperties = new ChartProperties();
        //history = new History();
    }

    public ChartFrame(String id)
    {
        PREFERRED_ID = id;
        setLayout(new BorderLayout());
        setName("CTL_ChartFrameEmpty");
        setToolTipText("TOOL_ChartFrameEmpty");

    }

    public void init()
    {
        setOpaque(false);
        setDoubleBuffered(true);

        chartToolbar = new ChartToolbar(this);
        mainPanel = new MainPanel(this);
        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        scrollBar.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);

        add(chartToolbar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(scrollBar, BorderLayout.SOUTH);

        validate();

        if (restored)
        {
            chartProperties.setMarkerVisibility(true);

//            for (Overlay overlay : chartData.getSavedOverlays())
//                overlayAdded(overlay);
//            for (Indicator indicator : chartData.getSavedIndicators())
//                indicatorAdded(indicator);
//            restoreAnnotations();

            revalidate();
            repaint();
            getSplitPanel().getIndicatorsPanel().calculateHeight();
            revalidate();
            repaint();

            chartData.clearSavedIndicators();
            chartData.clearSavedOverlays();
            chartData.clearAnnotations();
            chartData.clearAnnotationsCount();

            setRestored(false);
        } else
        {
//            HistoryItem historyItem = new HistoryItem(
//                    chartData.getStock(),
//                    chartData.getInterval());
//            history.setCurrent(historyItem);

//            if (template != null)
//            {
//                List<Overlay> overlays = template.getOverlays();
//                for (int i = 0; i < overlays.size(); i++)
//                    overlayAdded(overlays.get(i));
//                List<Indicator> indicators = template.getIndicators();
//                for (int i = 0; i < indicators.size(); i++)
//                    indicatorAdded(indicators.get(i));
//            }
        }

        DatasetUsage.getInstance().addDataProviderListener(this);
        addMouseWheelListener((MouseWheelListener) this);
        scrollBar.addAdjustmentListener((AdjustmentListener) this);

        initialized = true;
    }

    public boolean getRestored()
    {
        return restored;
    }

    public void setRestored(boolean b)
    {
        restored = b;
    }

    public boolean getFocus()
    {
        return focus;
    }

    public void setFocus(boolean b)
    {
        focus = b;
    }


    public ChartProperties getChartProperties()
    {
        return chartProperties;
    }

    public void setChartProperties(ChartProperties cp)
    {
        chartProperties = cp;
    }

    public ChartData getChartData()
    {
        return chartData;
    }

    public void setChartData(ChartData data)
    {
        if (data == null)
            throw new IllegalArgumentException("ChartData shouldn't be null");
        chartData = data;
        addChartFrameListener(data);
        Stock stock = chartData.getStock();
        setName("CTL_ChartFrame" + stock.getKey());
        setToolTipText("TOOL_ChartFrame" + stock.getCompanyName());
    }

    public MainPanel getMainPanel()
    {
        return mainPanel;
    }

    public ChartSplitPanel getSplitPanel()
    {
        if (mainPanel != null)
            return mainPanel.getSplitPanel();
        return null;
    }

    private EventListenerList listenerList()
    {
        if (chartFrameListeners == null)
            chartFrameListeners = new EventListenerList();
        return chartFrameListeners;
    }

    public void addChartFrameListener(ChartFrameListener listener)
    {
        listenerList().add(ChartFrameListener.class, listener);
    }

    public void removeChartFrameListener(ChartFrameListener listener)
    {
        listenerList().remove(ChartFrameListener.class, listener);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (!getChartData().isDatasetNull())
        {
            int items = getChartData().getPeriod();
            int itemsCount = getChartData().getDataset().getItemsCount();
            if (itemsCount > items)
            {
                int last = getChartData().getLast() - e.getWheelRotation();
                last = last > itemsCount ? itemsCount : (last < items ? items : last);

                if (getChartData().getLast() != last)
                {
                    getChartData().setLast(last);
                    getChartData().calculate(this);
                }
            }
        }
    }

    public void updateHorizontalScrollBar() {
        int last = getChartData().getLast();
        int items = getChartData().getPeriod();
        int itemsCount = getChartData().getDataset().getItemsCount();

        boolean updated = false;

        if (scrollBar.getModel().getExtent() != items)
        {
            scrollBar.getModel().setExtent(items);
            updated = true;
        }
        if (scrollBar.getModel().getMinimum() != 0)
        {
            scrollBar.getModel().setMinimum(0);
            updated = true;
        }
        if (scrollBar.getModel().getMaximum() != itemsCount)
        {
            scrollBar.getModel().setMaximum(itemsCount);
            updated = true;
        }
        if (scrollBar.getModel().getValue() != (last - items))
        {
            scrollBar.getModel().setValue(last - items);
            updated = true;
        }

        if (updated)
        {
            repaint();
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        int items = getChartData().getPeriod();
        int itemsCount = getChartData().getDataset().getItemsCount();
        int end = e.getValue() + items;

        end = end > itemsCount ? itemsCount : (end < items ? items : end);

        if (getChartData().getLast() != end)
        {
            getChartData().setLast(end);
            getChartData().calculate(this);
        }
        repaint();
    }




    public BufferedImage getBufferedImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = GraphicsUtils.prepareGraphics(image.createGraphics());
        g.setColor(chartProperties.getBackgroundColor());
        g.fillRect(0, 0, width, height);
        mainPanel.paintComponents(g);
        g.dispose();
        return image;
    }

    public void loading(final Stock stock, final Interval interval, final boolean newChart)
    {
        if (!newChart)
        {
            oldStock = chartData.getStock();
            chartData.setStock(stock);
            oldInterval = chartData.getInterval();
            chartData.setInterval(interval);
            chartToolbar.setVisible(false);
            mainPanel.setVisible(false);
            scrollBar.setVisible(false);
            revalidate();
            repaint();
        }

        final DataProvider dataProvider = getChartData().getDataProvider();
        final String key = dataProvider.getDatasetKey(stock, interval);
        final JLabel loading = getLoadingLabel(stock);
        add(loading, BorderLayout.CENTER);
        revalidate();
        repaint();

//        final ProgressHandle handle = ProgressHandleFactory.createHandle(loading.getText());
//        handle.start();
//        handle.switchToIndeterminate();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                loadingError = false;
                try
                {
                    if (!DatasetUsage.getInstance().isDatasetInMemory(key))
                    {
                        dataProvider.fetchDataset(stock, interval);
                    } else
                    {
                        Dataset dataset = DatasetUsage.getInstance().getDatasetFromMemory(key);
                        if (dataset.getItemsCount() < 3)
                            dataProvider.fetchDataset(stock, interval);
                    }
                } catch (Exception ex)
                {
                    loadingError = true;
                }

                //handle.finish();
                if (!loadingError)
                {
                    DatasetUsage.getInstance().fetchDataset(key);
                    DatasetUsage.getInstance().addDatasetUpdater(dataProvider.getName(), stock, interval);
                    datasetKeyChanged(key);
                    remove(loading);
                    if (!newChart)
                    {
                        setName(ResBundle.getMessage("CTL_ChartFrame", stock.getKey()));
                        setToolTipText(ResBundle.getMessage("TOOL_ChartFrame", stock.getCompanyName()));
                        //HistoryItem item = new HistoryItem(stock, interval);
                        //history.setCurrent(item);
                        DatasetUsage.getInstance().chartClosed(dataProvider.getDatasetKey(oldStock, oldInterval));
                        resetHorizontalScrollBar();
                        chartToolbar.setVisible(true);
                        chartToolbar.updateToolbar();
                        mainPanel.setVisible(true);
                        scrollBar.setVisible(true);
                    } else
                    {
                        init();
                    }
                } else
                {
                    if (stock.hasCompanyName())
                    {
                        loading.setText(ResBundle.getMessage("LBL_LoadingNoDataNew", stock.getCompanyName()));
                    } else
                    {
                        loading.setText(ResBundle.getMessage("LBL_LoadingNoDataNew", stock.getKey()));
                    }
                    if (!newChart)
                        System.out.println("Showing conformation");
                }
                revalidate();
                repaint();
            }
        };

        runnable.run();
    }

    public void resetHorizontalScrollBar()
    {
        chartData.setPeriod(-1);
        chartData.setLast(-1);
        chartData.calculate(this);
        int last = getChartData().getLast();
        int items = getChartData().getPeriod();
        scrollBar.getModel().setExtent(items);
        scrollBar.getModel().setMinimum(0);
        scrollBar.getModel().setMaximum(last);
        scrollBar.getModel().setValue(last - items);
    }

    public void datasetKeyChanged(String datasetKey)
    {
        ChartFrameListener[] listeners = listenerList().getListeners(ChartFrameListener.class);
        for (ChartFrameListener listener : listeners)
            listener.datasetKeyChanged(datasetKey);
    }

    private JLabel getLoadingLabel(Stock stock)
    {
        String text;
        text = ResBundle.getMessage("LBL_Loading", stock.getKey());
        //ImageIcon logo = ResourcesUtils.getLogo();
        JLabel loading = new JLabel(text);
        loading.setOpaque(true);
        loading.setBackground(Color.WHITE);
        loading.setVerticalTextPosition(SwingConstants.BOTTOM);
        loading.setHorizontalTextPosition(SwingConstants.CENTER);
        return loading;
    }

    @Override
    public void triggerDataProviderListener(DataProviderEvent evt)
    {
        String key = chartData.getDatasetKey();
        if (key.equals((String) evt.getSource()))
        {
            int itemsAdded = evt.getItemsAdded();
            int last = chartData.getLast();
            datasetKeyChanged(key);
            int count = chartData.getDataset().getItemsCount();
            if (last == count - itemsAdded)
                resetHorizontalScrollBar();
            revalidate();
            repaint();
        }
    }
}
