package com.punwire.kat.data;

import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.ResBundle;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by Kanwal on 17/01/16.
 */
public class MongoProvider extends DataProvider {

    private static final long serialVersionUID = AppConfig.APPVERSION;
    private static MongoDb db = new MongoDb();

    public MongoProvider()
    {
        super(ResBundle.bundle);
        this.name = "MongoProvider";
    }


    @Override
    protected String fetchCompanyName(String symbol) throws InvalidStockException, StockNotFoundException, RegistrationException, IOException {
        return symbol + " name";
    }

    @Override
    protected Dataset fetchDataForFavorites(Stock stock) throws IOException, ParseException {
        return fetchData(stock, new FiveMinuteInterval());
    }

    @Override
    protected Dataset fetchData(Stock stock, Interval interval) throws IOException, ParseException {
        return db.getDataSet(stock.getSymbol(), interval.getLengthInSeconds()/60);
    }

    @Override
    protected DataItem fetchLastDataItem(Stock stock, Interval interval) throws IOException, ParseException {
        return db.getLastDataItem(stock.getSymbol(), interval.getLengthInSeconds()/60);
    }

    @Override
    public List<Stock> fetchAutocomplete(String text) throws IOException {
        return null;
    }

    @Override
    public int getRefreshInterval() {
        return 5;
    }
}
