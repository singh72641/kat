package com.punwire.kat.chart;

import com.punwire.kat.chart2.CoordCalc;
import com.punwire.kat.chart2.RectangleInsets;
import com.punwire.kat.data.DataItem;
import com.punwire.kat.data.Dataset;
import com.punwire.kat.data.Range;
import com.punwire.kat.data.WeeklyInterval;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.GeneralPath;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by Kanwal on 19/01/16.
 */
public class OaChart extends JPanel implements MouseMotionListener {

    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private ChartProperties cp;
    private OaChartData cd;
    private Point dragStart;
    private boolean inDrag=false;
    private int dragMove=0;
    private Rectangle candleRect;
    Rectangle xAxisRect;
    Rectangle yAxisRect;
    private boolean drawCross=false;
    private Point crossPoint=null;
    BollingerBands bd;

    public OaChart(OaChartData cd) {
        this.cd = cd;
        cp = cd.getChartProperties();

        setPreferredSize(new Dimension(1200,1000));


        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                //dragStart = e.getPoint();
                //inDrag = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inDrag = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                if( ! inDrag ) {
                    inDrag = true;
                    dragStart = e.getPoint();
                    dragMove = 0;
                }

                if( inDrag ) {
                    int move = dragStart.x - e.getX();
                    move = move / 50;

                    if( dragMove != move ) {
                        cd.moveVisible(move - dragMove);
                        //System.out.println("Dragging at X " + e.getX() + "  dragMove: " + dragMove + "  Move: " + move);
                        dragMove = move;
                        revalidate();
                        repaint();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);

                if( candleRect.contains(e.getPoint()))
                {
                    crossPoint = e.getPoint();
                    revalidate();
                    repaint();
                }
                else crossPoint = null;



            }
        });
        addMouseWheelListener(new MouseAdapter() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();
                //System.out.println("Mouse wheel delta " + delta);
                Double barWidth = cp.getBarWidth() + (delta * 50);;
                int b = barWidth.intValue();

                cp.setBarWidth(b);
                revalidate();
                repaint();
            }

        });
    }

    public void addBolinger() {
        bd = new BollingerBands(cd);
        bd.calculate();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = OaGraphicUtil.g2(g);

        g2.setBackground( Color.red );
        g2.fillRect(0, 0, getWidth(), getHeight());


        Rectangle r =  getBounds();
        r.grow(-5, -5);

        //System.out.println(r.height);
        //System.out.println(cp.getBarWidth());
        cd.calcVisible(r);


        xAxisRect =  new Rectangle(r.x, r.height - 40, r.width - 140, 40 );
        yAxisRect =  new Rectangle(r.width - 140, r.y, 140, r.height );
        Rectangle gridRect =  new Rectangle(r.x, r.y, r.width - 140, r.height - 40);
        candleRect = gridRect;

        drawCandle(g2);
        drawGrid(g2, gridRect);
        drawXAxis(g2, xAxisRect);
        drawYAxis(g2, yAxisRect);
        drawCandles(g2, gridRect);
        if( bd != null) bd.paint(g2,candleRect);
        if( crossPoint != null )
        {
            drawCross(g2,candleRect,crossPoint);
        }



    }

    public void priceAxisMarker(Graphics2D g,double value, Color color, double y )
    {
        if (value < 10f)
        {
            df = new DecimalFormat("#,##0.00000");
        }
        RectangleInsets dataOffset = ChartData.dataOffset;
        FontMetrics fm = g.getFontMetrics();

        g.setPaint(color);
        double x = 1;
        double w = 130;
        double h = fm.getHeight() + 6;

        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
        gp.moveTo((float) x, (float) y);
        gp.lineTo((float) (x + 6), (float) (y - h / 2));
        gp.lineTo((float) (x + w + 8), (float) (y - h / 2));
        gp.lineTo((float) (x + w + 8), (float) (y + h / 2));
        gp.lineTo((float) (x + 6), (float) (y + h / 2));
        gp.closePath();
        g.fill(gp);

        g.setPaint(Color.black);
        g.drawString(df.format(value), (float) (x + 6 + 1), (float) (y + fm.getDescent() + 3));
    }

    public void drawCross(Graphics2D g,Rectangle r, Point p)
    {
        g.setColor(new Color(160, 160, 160));
        g.setStroke(cp.getGridVerticalStroke());
        //Horizontal Line
        g.draw(CoordCalc.line(r.x, p.y, r.width, p.y));
        g.draw(CoordCalc.line(p.x, r.y, p.x, r.height));

        Range chartRange = cd.getVisibleRange();
        double price1 = cd.getPriceAtY(p.y,r,chartRange);

        g.translate(yAxisRect.x, yAxisRect.y);

        priceAxisMarker(g, price1, new Color(191, 190, 188), p.y);

        g.translate(-1 * yAxisRect.x, -1 * yAxisRect.y);

        drawDataBox(g,r,p);
    }

    public void drawDataBox(Graphics2D g,Rectangle r, Point p)
    {
        g.translate(p.x, p.y);

        int i = cd.getBarLocation(p.x, candleRect);

        if( i > 0 && i < cd.getVisibleData().getItemsCount())
        {
            DataItem item = cd.getVisibleData().getDataItem(i);
            g.setPaint(new Color(191, 190, 188));
            g.fill(CoordCalc.rectangle(4, 4, 300, 400));
            FontMetrics fm = g.getFontMetrics();
            g.setPaint(Color.black);
            double h = fm.getHeight();

            g.drawString("Date: " + item.getDate().toString(), 10.0f, (float)(10 + h ));
            g.drawString("Open: " + df.format(item.getOpen()), 10.0f, (float)(10 + 2 * h ));
            g.drawString("High: " + df.format(item.getHigh()), 10.0f, (float)(10 + 3 * h ));
            g.drawString("Low: " + df.format(item.getLow()), 10.0f, (float)(10 + 4 * h ));
            g.drawString("Close: " + df.format(item.getClose()), 10.0f, (float)(10 + 5 * h ));
            g.drawString("Volume: " + df.format(item.getVolume()), 10.0f, (float)(10 + 6 * h ));
        }
        g.translate(-1 * p.x, -1 * p.y);
    }

    public void drawCandles(Graphics2D g,Rectangle r)
    {
        g.translate(r.x, r.y);
        boolean isLog = cp.getAxisLogarithmicFlag();
        Range range = cd.getVisibleRange();

        if (cd.getVisibleData() != null)
        {
            Dataset dataset = cd.getVisibleData();
            for(int i = 0; i < dataset.getItemsCount(); i++)
            {
                double open = dataset.getOpenAt(i);
                double close = dataset.getCloseAt(i);
                double high = dataset.getHighAt(i);
                double low = dataset.getLowAt(i);

                double x = cd.getX(i, r);
                double yOpen = cd.getY(open, r, range, isLog);
                double yClose = cd.getY(close, r, range, isLog);
                double yHigh = cd.getY(high, r, range, isLog);
                double yLow = cd.getY(low, r, range, isLog);

                double candleWidth = cp.getBarWidth();
                double candleHeight = Math.abs(yOpen - yClose);

                Color barColor = (open > close ? cp.getBarDownColor() : cp.getBarUpColor());

                if (open > close ? cp.getBarDownVisibility() : cp.getBarUpVisibility())
                {
                    g.setPaint(open > close ? cp.getBarDownColor() : cp.getBarUpColor());
                    g.fill(CoordCalc.rectangle(x - candleWidth/2, (open > close ? yOpen : yClose), candleWidth - 8, candleHeight));
                }

                if (cp.getBarVisibility())
                {
//                    System.out.println(cp.getBarColor().getRed());
//                    System.out.println(cp.getBarColor().getGreen());
//                    System.out.println(cp.getBarColor().getBlue());
                    g.setPaint(barColor);
                    g.setStroke(cp.getBarStroke());
                    g.draw(CoordCalc.line(x, (open > close ? yOpen : yClose), x, yHigh));
                    g.draw(CoordCalc.line(x, (open > close ? yClose : yOpen), x, yLow));
                    g.draw(CoordCalc.rectangle(x - candleWidth/2, (open > close ? yOpen : yClose), candleWidth - 8, candleHeight));
                }
            }
        }

        g.translate(-r.x, -r.y);
    }

    public void drawGrid(Graphics2D g,Rectangle r) {

        Range chartRange = cd.getVisibleRange();
        double x, y;
        // Vertical Grid
        if (cp.getGridVerticalVisibility()) {
            g.setColor(cp.getGridVerticalColor());
            g.setStroke(cp.getGridVerticalStroke());
            double[] list = cd.getDateValues();
            boolean firstFlag = true;

            System.out.println("Vertical Grid: " + list.length);
            for (int i = 0; i < list.length; i++) {
                double value = list[i];
                if (value != -1) {
                    x = cd.getX(value, r);
                    if (firstFlag) {
                        int index = (int) value;
                        long time = cd.getVisibleData().getTimeAt(index);
                        if (cd.isFirstWorkingDayOfMonth(time))
                            g.draw(CoordCalc.line(r.x + x, r.y, r.x + x, r.height));
                        firstFlag = false;
                    } else {
                        g.draw(CoordCalc.line(r.x + x, r.y, r.x + x, r.height));
                    }
                }
            }
        }

        // Horizontal Grid
        if (cp.getGridHorizontalVisibility()) {
            // paint grid for chart2
            g.setColor(cp.getGridHorizontalColor());
            g.setStroke(cp.getGridHorizontalStroke());
            FontMetrics fm = getFontMetrics(cp.getFont());
            double[] list = cd.getYValues(r, chartRange, fm.getHeight());
            for (int i = 0; i < list.length; i++) {
                double value = list[i];
                y = cd.getY(value, r, chartRange, false);
                if (r.contains(r.x + 2, r.y + y)) {
                    g.draw(CoordCalc.line(r.x, y, r.width, y));
                }
            }
        }
    }


    public void drawYAxis(Graphics2D g,Rectangle r)
    {

        boolean isLog = cp.getAxisLogarithmicFlag();

        if (cd.getVisibleData() != null)
        {
            // paint values for chart2
            Range chartRange = cd.getVisibleRange();
            FontMetrics fm = getFontMetrics(cp.getFont());

            g.setFont(cp.getFont());
            g.translate(r.x, r.y);
            g.setPaint(cp.getAxisColor());
            g.setStroke(cp.getAxisStroke());
            g.drawLine(0, 0, 0, r.height);

            r.grow(-2, -2);

            double[] values = cd.getYValues(r, chartRange, fm.getHeight());
            double axisTick = cp.getAxisTick();
            double axisStick = cp.getAxisPriceStick();
            double y;

            g.setFont(cp.getFont());
            LineMetrics lm = cp.getFont().getLineMetrics("0123456789", g.getFontRenderContext());
            DecimalFormat df = new DecimalFormat("#,###.##");

            for (int i = 0; i < values.length; i++)
            {
                double value = values[i];
                y = cd.getY(value, r, chartRange, isLog);
                if (r.contains(r.getCenterX(), y))
                {
                    g.setPaint(cp.getAxisColor());
                    g.draw(CoordCalc.line(0, y, axisTick, y));
                    g.setPaint(cp.getFontColor());
                    g.drawString(df.format(value), (float)(axisTick + axisStick), (float)(y + lm.getDescent()));
                }
            }

            g.translate(-r.x, -r.y);

            // paint chart2 marker
//            double open = cd.getVisible().getLastOpen();
//            double close = cd.getVisible().getLastClose();
//            y = cd.getY(close, chartBounds, chartRange, isLog);
            //PriceAxisMarker.paint(g2, chartFrame, close, open > close ? cp.getBarDownColor() : cp.getBarUpColor(), y);

        }
    }

    public void drawXAxis(Graphics2D g,Rectangle r)
    {

        g.setFont(cp.getFont());
        g.setColor(cp.getAxisColor());
        g.setStroke(cp.getAxisStroke());

        String[] months = cp.getMonths();
        FontRenderContext frc = g.getFontRenderContext();
        LineMetrics lm = cp.getFont().getLineMetrics("0123456789/", g.getFontRenderContext());

        Dataset dataset = cd.getVisibleData();
        Range range = cd.getVisibleRange();

        g.drawLine(r.x, r.y , r.width, r.y);

        Calendar calendar = Calendar.getInstance();
        double[] list = cd.getDateValues();
        if (!cd.getInterval().isIntraDay())
        {
            boolean firstFlag = true;
            for (int j = 0; j < list.length; j++)
            {
                double value = list[j];
                if (value != -1)
                {
                    double x = cd.getX(value, r);

                    calendar.setTimeInMillis(dataset.getTimeAt(j));
                    String string = months[calendar.get(Calendar.MONTH)];
                    if (string.isEmpty())
                    {
                        string = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
                    } else
                    {
                        if (cd.getInterval() instanceof WeeklyInterval)
                            string = string.substring(0, 1);
                    }
                    double h = cp.getFont().getStringBounds(string, frc).getHeight();

                    if (firstFlag)
                    {
                        int index = (int) value;
                        long time = dataset.getTimeAt(index);
                        if (!cd.isFirstWorkingDayOfMonth(time))
                        {
                            double nvalue = 0;
                            for (int k = j + 1; k < list.length; k++)
                                if (list[k] != -1)
                                {
                                    nvalue = list[k];
                                    break;
                                }
                            double nx = cd.getX(nvalue, r);
                            double w = cp.getFont().getStringBounds(string, frc).getWidth();
                            if (nx - x > w + 5)
                            {
                                g.setColor(cp.getFontColor());
                                g.drawString(string, (float)(x + 5), lm.getAscent());
                            }
                        } else
                        {
                            g.setColor(cp.getAxisColor());
                            g.draw(CoordCalc.line(r.x + x, r.y, r.x + x, r.y + h));
                            g.setColor(cp.getFontColor());
                            g.drawString(string, (float)(x + 5) + r.x, r.y  + lm.getAscent());
                        }
                        firstFlag = false;
                    } else
                    {
                        g.setColor(cp.getAxisColor());
                        g.draw(CoordCalc.line(r.x + x, r.y, r.x + x, r.y + h));
                        g.setColor(cp.getFontColor());
                        g.drawString(string, (float)(x + 5) + r.x, r.y  + lm.getAscent());
                    }
                }
            }
        }
        else
        {
            for (int j = 0; j < list.length; j++)
            {
                double value = list[j];
                if (value != -1)
                {
                    double x = cd.getX(value, r);
                    calendar.setTimeInMillis(dataset.getTimeAt(j));
                    StringBuilder sb = new StringBuilder();
                    if (calendar.get(Calendar.HOUR_OF_DAY) < 10)
                        sb.append("0");
                    sb.append(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
                    sb.append(":");
                    if (calendar.get(Calendar.MINUTE) < 10)
                        sb.append("0");
                    sb.append(String.valueOf(calendar.get(Calendar.MINUTE)));
                    String label = sb.toString();

                    double h = cp.getFont().getStringBounds(label, frc).getHeight();
                    g.setColor(cp.getAxisColor());
                    g.draw(CoordCalc.line(r.x + x, r.y, r.x + x, r.y + h));
                    g.setColor(cp.getFontColor());
                    g.drawString(label, (float)(x + 5), r.y  + lm.getAscent());
                }
            }
        }
    }

    public void drawCandle(Graphics2D g)
    {
        Rectangle r =  getBounds();
        r.grow(-10, -10);


    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
