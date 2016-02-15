package com.punwire.kat.chart;

import com.punwire.kat.data.*;

import javax.activation.DataSource;
import java.awt.*;
import java.awt.List;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Kanwal on 19/01/16.
 */
public class OaChartData {

    private MongoDb db;
    private Dataset fullData;
    private Dataset visibleData=null;
    Range fullRange;
    Range visibleRange;
    int size;
    int period = -1;
    int last=-1;
    private ChartProperties cp;
    private Stock stock;
    private Interval interval;

    private static DecimalFormat D1 = new DecimalFormat("0.###");
    private static DecimalFormat D2 = new DecimalFormat("0.0");

    public OaChartData(ChartProperties cp1, Stock stock, Interval interval){
        this.cp = cp1;
        this.stock = stock;
        this.interval = interval;
        db = new MongoDb();
    }

    public void fetch()
    {
        fullData = db.getDataSet(stock.getSymbol(), interval.getLengthInSeconds()/60);
        size = fullData.getItemsCount();
        last=-1;
    }

    public void moveVisible(int by)
    {
        //System.out.println("Before Move: Last " + last + " by  " + by   );

        if( by > 0 && (last + by) < fullData.getItemsCount()) last = last + by;
        if( by < 0 && (last + by) > period ) last = last + by;

        //System.out.println("After Move: Last " + last);
    }

    public void calcVisible(Rectangle rect)
    {
        double barWidth = cp.getBarWidth();
        last = last == -1 ? size : last;
        period = (int) (rect.getWidth() / (barWidth + 2));
        if (period == 0)
            period = 150;
        if (period > size)
            period = size;

        //System.out.println("Periods: " + period + "  RecWidth: " + rect.getWidth() + "  barWidth: " + barWidth);

        if (fullData != null) {
            setVisibleData(fullData.getVisibleDataset(period, last));
        }
        calcRange();
    }
    public void calcRange()
    {
        Range range = new Range();
        if (visibleData != null) {
            double min = visibleData.getMinNotZero();
            double max = visibleData.getMaxNotZero();
            range = new Range(min - (max - min) * 0.01, max + (max - min) * 0.01);
        }
        setVisibleRange(range);
    }

    public void setVisibleData(Dataset visibleData) {
        this.visibleData = visibleData;
    }

    public void setVisibleRange(Range visibleRange) {
        this.visibleRange = visibleRange;
    }

    public Dataset getVisibleData() {
        return visibleData;
    }

    public Range getVisibleRange() {
        return visibleRange;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Dataset getFullData() {
        return fullData;
    }

    public void setFullData(Dataset fullData) {
        this.fullData = fullData;
    }

    public double[] getDateValues() {
        if (visibleData != null) {
            int count = visibleData.getItemsCount();
            Interval itrv = getInterval();
            double[] list = new double[getVisibleData().getItemsCount()];

            if (!itrv.isIntraDay()) {
                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.setTimeInMillis(getVisibleData().getTimeAt(0));
                list[0] = 0;

                if (itrv instanceof MonthlyInterval) {
                    int year = cal.get(Calendar.YEAR);
                    for (int i = 2; i < count + 1; i++) {
                        cal.setTimeInMillis(getVisibleData().getTimeAt(i - 1));
                        if (year != cal.get(Calendar.YEAR)) {
                            list[i - 1] = i - 1;
                            year = cal.get(Calendar.YEAR);
                        } else {
                            list[i - 1] = -1;
                        }
                    }
                } else {
                    int month = cal.get(Calendar.MONTH);
                    for (int i = 2; i < count + 1; i++) {
                        cal.setTimeInMillis(getVisibleData().getTimeAt(i - 1));
                        if (month != cal.get(Calendar.MONTH)) {
                            list[i - 1] = i - 1;
                            month = cal.get(Calendar.MONTH);
                        } else {
                            list[i - 1] = -1;
                        }
                    }
                }
            } else {
                int div = count/15;
                if( div < 1 ) div = 1;
                for (int i = 0; i < count; i++) {
                    if (i % div == 0)
                        list[i] = i;
                    else
                        list[i] = -1;
                }
            }

            return list;
        }

        return new double[0];
    }

    public int getBarLocation(int x, Rectangle rect) {
        double r = rect.getMinX();
        int p = period;
        Double value = (((x - rect.getMinX())/rect.getWidth()) * (double) period) - 0.5D  ;
        return value.intValue();
    }

    public double getPriceAtY(int y, Rectangle rect, Range range) {
        return range.getUpperBound() - ((y - rect.getMinY())/rect.getHeight()) * (range.getUpperBound() - range.getLowerBound());
    }


    public double getX(double value, Rectangle rect) {
        double r = rect.getMinX();
        int p = period;
        double r1 = ((value + 0.5D) / (double) period);
        double r2 = r1 * rect.getWidth();
        return rect.getMinX() + (((value + 0.5D) / (double) period) * rect.getWidth());

    }

    public double[] getYValues(Rectangle rectangle, Range range, int fontHeight) {
        int count = 15;
        while (((rectangle.height / count) < (fontHeight + 20)) && (count > -2))
            count--;

        double rangeMin = range.getLowerBound();
        double rangeMax = range.getUpperBound();

        double vRange = rangeMax - rangeMin;
        double rangeUnit = vRange / count;

        int roundedExponent = (int) Math.round(Math.log10(rangeUnit)) - 1;
        double factor = Math.pow(10, -roundedExponent);
        int adjustedValue = (int) (rangeUnit * factor);
        rangeUnit = (double) adjustedValue / factor;

        if (rangeUnit < 0.001) {
            rangeUnit = 0.001d;
        } else if (rangeUnit >= 0.001 && rangeUnit < 0.005) {
            String unitStr = D1.format(rangeUnit);
            try {
                rangeUnit = D1.parse(unitStr.trim()).doubleValue();
            } catch (ParseException ex) {
            }
        } else if (rangeUnit >= 0.005 && rangeUnit < 1) {
            String unitStr = D2.format(rangeUnit);
            try {
                rangeUnit = D2.parse(unitStr.trim()).doubleValue();
            } catch (ParseException ex) {
            }
        }

        rangeMin = (int) (rangeMin / rangeUnit) * rangeUnit;
        count = (int) (vRange / rangeUnit);

        if (count + 2 > 0) {
            double[] result = new double[count + 2];
            for (int i = 0; i < count + 2; i++)
                result[i] = rangeMin + rangeUnit * i;
            return result;
        } else {
            java.util.List<Double> list = getPriceValues(rectangle, range);
            double[] result = new double[list.size()];
            for (int i = 0; i < list.size(); i++)
                result[i] = list.get(i).doubleValue();
            return result;
        }
    }

    public Point2D.Double valueToJava2D(final double xvalue, final double yvalue, Rectangle bounds, Range range, boolean isLog) {
        double px = getX(xvalue, bounds);
        double py = getY(yvalue, bounds, range, isLog);
        Point2D.Double p = new Point2D.Double(px, py);
        return p;
    }

    public Point2D getPoint(double x, double y, Range range, Rectangle rect, boolean isLog) {
        return new Point2D.Double(getX(x, rect), getY(y, rect, range, isLog));
    }

    public java.util.List<Double> getPriceValues(Rectangle rect, Range range) {
        java.util.List<Double> values = new ArrayList<Double>();

        double diff = range.getUpperBound() - range.getLowerBound();
        if (diff > 10) {
            int step = (int) (diff / 10) + 1;
            double low = Math.ceil(range.getUpperBound() - (diff / 10) * 9);

            for (double i = low; i <= range.getUpperBound(); i += step) {
                values.add(new Double(i));
            }
        } else {
            double step = diff / 10;
            for (double i = range.getLowerBound(); i <= range.getUpperBound(); i += step) {
                values.add(new Double(i));
            }
        }

        return values;
    }

    private double getY(double value, Rectangle rect, Range range) {
        return rect.getMinY() + (range.getUpperBound() - value) / (range.getUpperBound() - range.getLowerBound()) * rect.getHeight();
    }

    public double getY(double value, Rectangle rect, Range range, boolean isLog) {
        if (isLog) return getLogY(value, rect, range);
        return getY(value, rect, range);
    }

    private double getLogY(double value, Rectangle rect, Range range) {
        double base = 0;
        if (range.getLowerBound() < 0)
            base = Math.abs(range.getLowerBound()) + 1.0D;
        double scale = (rect.getHeight() /
                (Math.log(range.getUpperBound() + base) -
                        Math.log(range.getLowerBound() + base)));
        return rect.getMinY() +
                Math.round(
                        (Math.log(range.getUpperBound() + base) -
                                Math.log(value + base)) * scale);
    }


    public boolean isFirstWorkingDayOfMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTimeInMillis(time);
        if (getInterval() instanceof WeeklyInterval) {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int week = getFirstWeekMondayOfMonth(
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR));
            if (week == calendar.get(Calendar.WEEK_OF_MONTH)) {
                return day == Calendar.MONDAY;
            }
            return false;
        } else {
            int week = getFirstWorkingWeekOfMonth(
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR));
            if (calendar.get(Calendar.WEEK_OF_MONTH) == week)
                return calendar.get(Calendar.DAY_OF_WEEK) ==
                        getFirstWorkingDayOfMonth(
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.YEAR));
            else
                return false;
        }
    }

    private int getFirstWeekMondayOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONDAY, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            return calendar.get(Calendar.WEEK_OF_MONTH);
        } else {
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            return calendar.get(Calendar.WEEK_OF_MONTH);
        }
    }


    private static int getFirstWorkingWeekOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONDAY, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                return calendar.get(Calendar.WEEK_OF_MONTH);
            case Calendar.SUNDAY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                return calendar.get(Calendar.WEEK_OF_MONTH);
            default:
                return calendar.get(Calendar.WEEK_OF_MONTH);
        }
    }

    private int getFirstWorkingDayOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONDAY, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                return calendar.get(Calendar.DAY_OF_WEEK);
            case Calendar.SUNDAY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                return calendar.get(Calendar.DAY_OF_WEEK);
            default:
                return calendar.get(Calendar.DAY_OF_WEEK);
        }
    }

    public ChartProperties getChartProperties() {
        return cp;
    }
}
