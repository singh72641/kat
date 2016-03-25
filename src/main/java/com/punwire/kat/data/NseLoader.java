package com.punwire.kat.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.punwire.kat.core.AppConfig;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.zerodha.ZdOptionChain;
import com.punwire.kat.zerodha.ZdSplit;
import com.punwire.kat.zerodha.ZdSymbol;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.*;

import javax.net.ssl.SSLContext;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by Kanwal on 24/01/16.
 */
public class NseLoader {

    static WebDriver driver;
    static Wait<WebDriver> wait;
    static String downloadPath = "d:\\savedata\\";
    static MongoDb db ;
    static DateTimeFormatter dd_mm_yyyy = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static DateTimeFormatter printDate = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static DateTimeFormatter ddMMMyyyy = new DateTimeFormatterBuilder()
                                                .parseCaseInsensitive()
                                                .appendPattern("ddMMMyyyy")
                                                .toFormatter(Locale.ENGLISH);
    public static DateTimeFormatter yyMMM = DateTimeFormatter.ofPattern("yyMMM");
    public static DateTimeFormatter yyMM = DateTimeFormatter.ofPattern("yyMM");
    public static DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy");
    public static DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter dd_mmm_yyyy = new DateTimeFormatterBuilder()
                                                    .parseCaseInsensitive()
                                                    .appendPattern("dd-MMM-yyyy")
                                                    .toFormatter(Locale.ENGLISH);

    public static DateTimeFormatter dd_mmm_yy = new DateTimeFormatterBuilder()
                                                    .parseCaseInsensitive()
                                                    .appendPattern("dd-MMM-yy")
                                                    .toFormatter(Locale.ENGLISH);
    public static DecimalFormat strikePriceFormatter = new DecimalFormat("#####");

    public NseLoader(){
        db = AppConfig.db;
    }

    public void startBrowser()
    {
        if( driver == null ) {
            FirefoxProfile profile = FirefoxDriverProfile();
            driver = new FirefoxDriver(profile);
            wait = new WebDriverWait(driver, 5);
            driver.manage().window().maximize();
        }
    }

    public void loadOptList() throws Exception {
        File f = new File("d:\\savedata\\optlist.csv");
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line=null;
        line = br.readLine();
        while((line = br.readLine()) != null)
        {
            String[] parts = line.split(",");
            db.saveOptionList(parts[0],parts[1]);
            System.out.println(line);
        }
    }

    public void processFile(LocalDate date) throws Exception
    {
        String dateString = date.format(dd_mm_yyyy);
        String monString = date.format(ddMMMyyyy);
        String dayString = date.format(ddMMyyyy);
        String bhavfileName = "fo"+monString.toUpperCase()+"bhav.csv.zip";
        String mktfileName = "fo" + dayString + ".zip";



        File f = new File("d:\\savedata\\"+bhavfileName);
        final ZipFile zf = new ZipFile(f);

        final Enumeration<? extends ZipEntry> entries = zf.entries();
        ZipInputStream zipInput = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        while (entries.hasMoreElements()) {
            final ZipEntry zipEntry = entries.nextElement();
            final String fileName = zipEntry.getName();
            System.out.println("File in zip " + fileName);
            InputStream inputs=zf.getInputStream(zipEntry);
            //  final RandomAccessFile br = new RandomAccessFile(fileName, "r");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputs));
            String line=null;
            line = br.readLine();
            while((line = br.readLine()) != null)
            {
                System.out.println(line);
                String[] parts = line.split(",");
                String instrument = parts[0];
                String symbol = parts[1];

                LocalDate expiry_date = LocalDate.parse(parts[2], df)  ;
                double strike_price =  Double.valueOf(parts[3]);
                String opt_type = parts[4];
                double o = Double.valueOf(parts[5]);
                double h = Double.valueOf(parts[6]);
                double l = Double.valueOf(parts[7]);
                double c = Double.valueOf(parts[8]);
                long contracts = Long.valueOf(parts[10]);
                long open_int = Long.valueOf(parts[12]);
                long oi_change = Long.valueOf(parts[13]);
                String onDate = parts[14].substring(0, 3) + WordUtils.capitalize(parts[14].substring(3).toLowerCase());
                //System.out.println(onDate);
                //System.out.println(onDate.length());
                LocalDate asOfDate = LocalDate.parse( onDate , df);

                db.saveOption(asOfDate,instrument,symbol,expiry_date,strike_price,opt_type,o,h,l,c,contracts,open_int,oi_change);
                System.out.println(symbol);

            }
        }
    }
    public static FirefoxProfile FirefoxDriverProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.dir", "d:\\savedata");
        profile.setPreference("browser.helperApps.neverAsk.openFile",
                "application/csv,text/csv,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/csv,application/zip,text/csv,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        profile.setPreference("browser.download.manager.focusWhenStarting", false);
        profile.setPreference("browser.download.manager.useWindow", false);
        profile.setPreference("browser.download.manager.showAlertOnComplete", false);
        profile.setPreference("browser.download.manager.closeWhenDone", false);
        return profile;
    }

    public void load(LocalDate date) throws Exception {

        startBrowser();

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            String dateString = date.format(dd_mm_yyyy);
            String monString = date.format(ddMMMyyyy);
            String dayString = date.format(ddMMyyyy);
            //String dateString = "21-01-2016";
            String dailyPath = "d:\\savedata\\"+dateString.replaceAll("-","");
            //Create Directory
            File f = new File(dailyPath);
            if(!f.exists()) f.mkdirs();
            String bhavfileName = "fo"+monString.toUpperCase()+"bhav.csv.zip";
            String mktfileName = "fo" + dayString + ".zip";

            //downloadFile("Bhavcopy", "fo21JAN2016bhav.csv.zip", dateString);
            //downloadFile("Market Activity Report","fo21012016.zip", dateString);

            downloadFile("Bhavcopy", bhavfileName, dateString);
            downloadFile("Market Activity Report",mktfileName, dateString);
            //downloadFile("Bhavcopy","fo21JAN2016bhav.csv.zip", dateString);

        } finally {
        }

    }

    public void processEqFile(LocalDate date) throws Exception
    {
        String dayString = date.format(ddMMyyyy);
        String dateString = date.format(dd_mm_yyyy);
        String monString = date.format(ddMMMyyyy);
        String bhavfileName = "cm"+monString.toUpperCase()+"bhav.csv.zip";
        File f = new File("d:\\savedata\\"+bhavfileName);

        if( ! f.exists() ) return;

        System.out.println("Processing " + bhavfileName);
        final ZipFile zf = new ZipFile(f);

        final Enumeration<? extends ZipEntry> entries = zf.entries();
        ZipInputStream zipInput = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        boolean success=true;
        while (entries.hasMoreElements()) {
            final ZipEntry zipEntry = entries.nextElement();
            final String fileName = zipEntry.getName();
            System.out.println("File in zip " + fileName);
            InputStream inputs=zf.getInputStream(zipEntry);
            //  final RandomAccessFile br = new RandomAccessFile(fileName, "r");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputs));
            String line=null;
            line = br.readLine();
            while((line = br.readLine()) != null)
            {
                System.out.println(line);
                String[] parts = line.split(",");
                String symbol = parts[0];
                String type = parts[1];
                double o = Double.valueOf(parts[2]);
                double h = Double.valueOf(parts[3]);
                double l = Double.valueOf(parts[4]);
                double c = Double.valueOf(parts[5]);
                double last = Double.valueOf(parts[6]);
                double prev = Double.valueOf(parts[7]);
                long volume = Long.valueOf(parts[8]);

                db.saveEod(symbol, DateUtil.intDate(date),o,h,l,c,last,prev,volume);
            }
            br.close();

        }
        zf.close();
        if(success){
            f.delete();
        }

        System.out.println("Done Processing " + bhavfileName);
    }

    public void processEqVol(LocalDate date) throws Exception
    {
        String dateString = date.format(dd_mm_yyyy);
        String volDateString = date.format(ddMMyyyy);
        String monString = date.format(ddMMMyyyy);
        String volfileName = "CMVOLT_"+volDateString+".CSV";
        File f = new File("d:\\savedata\\"+volfileName);
        if( ! f.exists() ) return;
        System.out.println("Processing " + volfileName);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line=null;
        line = br.readLine();
        boolean success=true;
        while((line = br.readLine()) != null)
        {
            //System.out.println(line);
            String[] parts = line.split(",");
            String volDate = parts[0];
            try {
                LocalDate onDate = volDate.length()>9? LocalDate.parse(volDate, dd_mmm_yyyy): LocalDate.parse(volDate, dd_mmm_yy);
                String symbol = parts[1];
                if( parts[2].equals("-") || parts[3].equals("-") || parts[4].equals("-") ) continue;
                double closePrice = Double.valueOf(parts[2]);
                double prevPrice = Double.valueOf(parts[3]);
                double prevVol = Double.valueOf(parts[5]);
                double currVol = Double.valueOf(parts[6]);
                double yearVol = Double.valueOf(parts[7]);

                db.saveVolatility(symbol, DateUtil.intDate(onDate), closePrice, prevPrice, prevVol,currVol, yearVol);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                success=false;
                break;
            }
        }
        br.close();
        if(success){
            f.delete();
        }

    }

    public void processLotFile() throws Exception
    {
        String lotfile = "fo_mktlots.csv";
        File f = new File("d:\\savedata\\"+lotfile);
        if( ! f.exists() ) return;
        System.out.println("Processing " + lotfile);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line=null;
        line = br.readLine();
        boolean success=true;
        while((line = br.readLine()) != null)
        {
            String[] parts = line.split(",");
            try {
                String name = parts[0].trim();
                String symbol = parts[1].trim();
                int lotSize = Integer.valueOf(parts[2].trim());
                db.saveLot(name, symbol, lotSize);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                success=false;
                break;
            }
        }
        br.close();
        if(success){
            f.delete();
        }

    }

    public void downloadEqBhav(LocalDate date)
    {
        try {
            String dateString = date.format(dd_mm_yyyy);
            String volDateString = date.format(ddMMyyyy);
            String monString = date.format(ddMMMyyyy);
            String bhavfileName = "cm"+monString.toUpperCase()+"bhav.csv.zip";
            String volfileName = "CMVOLT_"+volDateString+".CSV";
            File bhavFile = new File(downloadPath+bhavfileName);
            File volFile = new File(downloadPath+volfileName);

            if( bhavFile.exists() && volFile.exists() ) return;


            startBrowser();
            driver.get("http://www.nseindia.com/products/content/equities/equities/archieve_eq.htm");
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    System.out.println("Searching ...");
                    return webDriver.findElement(By.id("h_filetype")) != null;
                }
            });

            Select fileSelect = new Select(driver.findElement(By.id("h_filetype")));
            WebElement getButton = driver.findElement(By.className("getdata-button"));
            if(! bhavFile.exists())
            {
                System.out.println("Downloading Bhavcopy " + date.toString());
                fileSelect.selectByVisibleText("Bhavcopy");
                WebElement dateElement = driver.findElement(By.id("date"));
                dateElement.clear();
                dateElement.sendKeys(dateString);
                //dateElement.sendKeys(Keys.TAB);
                //dateElement.sendKeys(Keys.TAB);
                dateElement.sendKeys(Keys.ENTER);
                Thread.sleep(1000L);
                //wait.until(ExpectedConditions.elementToBeClickable(By.className("getdata-button")));
                getButton = driver.findElement(By.className("getdata-button"));
                getButton.click();
                System.out.println("After get data button");

                try {
                    wait.until(new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver webDriver) {
                            System.out.println("Searching ...");
                            return webDriver.findElement(By.linkText(bhavfileName)) != null;
                        }
                    });
                } catch (TimeoutException ex) {
                    //No Such file exists go back;
                    return;
                }

                WebElement getlink = driver.findElement(By.linkText(bhavfileName));
                getlink.click();
                do {
                    Thread.sleep(1000);
                } while (!bhavFile.exists());
                System.out.println(bhavfileName + " File length: " + bhavFile.length());
            }
            //Download Volatility File

            if( ! volFile.exists()) {
                System.out.println("Downloading Volatility " + date.toString());

                fileSelect.selectByVisibleText("Daily Volatility Files");
                WebElement dateElement = driver.findElement(By.id("date"));
                dateElement.clear();
                dateElement.sendKeys(dateString);
                //dateElement.sendKeys(Keys.TAB);
                //dateElement.sendKeys(Keys.TAB);
                dateElement.sendKeys(Keys.ENTER);
                Thread.sleep(1000L);
                getButton = driver.findElement(By.className("getdata-button"));
                getButton.click();
                try {
                    wait.until(new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver webDriver) {
                            System.out.println("Searching ...");
                            return webDriver.findElement(By.linkText(volfileName)) != null;
                        }
                    });
                } catch (TimeoutException ex) {
                    //No Such file exists go back;
                    return;
                }

                WebElement getlink = driver.findElement(By.linkText(volfileName));
                getlink.click();
                do {
                    Thread.sleep(1000);
                } while (!volFile.exists());
                System.out.println(volfileName + " File length: " + volFile.length());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void processIndexFile(LocalDate date) throws Exception
    {
        String dateString = date.format(dd_mm_yyyy);
        String volDateString = date.format(ddMMyyyy);
        String monString = date.format(ddMMMyyyy);
        //ind_close_all_12022016.csv
        String bhavfileName = "ind_close_all_"+volDateString+".csv";
        File bhavFile = new File(downloadPath+bhavfileName);

        if( ! bhavFile.exists()) return;
        System.out.println("Processing " + bhavfileName);

        BufferedReader br = new BufferedReader(new FileReader(bhavFile));
        String line=null;
        line = br.readLine();
        boolean success=true;
        while((line = br.readLine()) != null)
        {
            //System.out.println(line);
            String[] parts = line.split(",");

            try {
                String name = parts[0];
                String symbol = parts[0];
                if( name.equals("Nifty 50"))
                {
                    symbol = "NIFTY";
                    //db.saveEod(symbol, DateUtil.intDate(date),o,h,l,c,last,prev,volume);
                }
                else if( name.equals("Nifty Bank"))
                {
                    symbol = "BANKNIFTY";
                }
                String dd = parts[1];
                LocalDate onDate = dd.length()>9? LocalDate.parse(dd, dd_mm_yyyy): LocalDate.parse(dd, dd_mmm_yy);
                if( parts[3].equals("-") || parts[3].equals("-") || parts[3].equals("-") ) continue;
                double o = Double.valueOf(parts[2]);
                double h = Double.valueOf(parts[3]);
                double l = Double.valueOf(parts[4]);
                double c = Double.valueOf(parts[5]);
                double change = Double.valueOf(parts[6]);
                double changePct = Double.valueOf(parts[7]);
                long volume = parts[8].equals("-")? 0: Long.valueOf(parts[8]);
                double turnover = parts[9].equals("-")? 0.00: Double.valueOf(parts[9]);
                double pe = parts[10].equals("-")? 0.00: Double.valueOf(parts[10]);
                double pb = parts[11].equals("-")? 0.00: Double.valueOf(parts[11]);
                double de = parts[12].equals("-")? 0.00: Double.valueOf(parts[12]);

                db.saveDailyIndex(name, symbol, DateUtil.intDate(onDate), o,h,l,c,change,changePct,volume,turnover,pe,pb,de);

                if( symbol.equals("NIFTY") || symbol.equals("BANKNIFTY") )
                {
                    db.saveEod(symbol, DateUtil.intDate(onDate),o,h,l,c,c,c-change,volume);
                }

            } catch (Exception ex)
            {
                System.out.println(line);
                ex.printStackTrace();
                success=false;
                break;
            }
        }
        br.close();
        if(success){
            bhavFile.delete();
        }

    }


    public void downloadIndexFile(LocalDate date)
    {
        try {
            String dateString = date.format(dd_mm_yyyy);
            String volDateString = date.format(ddMMyyyy);
            String monString = date.format(ddMMMyyyy);
            //ind_close_all_12022016.csv
            String bhavfileName = "ind_close_all_"+volDateString+".csv";
            File bhavFile = new File(downloadPath+bhavfileName);

            if( bhavFile.exists()) return;

            startBrowser();
            driver.get("https://www1.nseindia.com/products/content/equities/indices/archieve_indices.htm");
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    System.out.println("Searching ...");
                    return webDriver.findElement(By.id("h_filetype")) != null;
                }
            });

            Select fileSelect = new Select(driver.findElement(By.id("h_filetype")));
            WebElement getButton = driver.findElement(By.className("getdata-button"));
            if(! bhavFile.exists())
            {
                System.out.println("Downloading Bhavcopy " + date.toString());
                fileSelect.selectByVisibleText("Daily Snapshot");
                WebElement dateElement = driver.findElement(By.id("date"));
                dateElement.clear();
                dateElement.sendKeys(dateString);
                //dateElement.sendKeys(Keys.TAB);
                //dateElement.sendKeys(Keys.TAB);
                dateElement.sendKeys(Keys.ENTER);
                Thread.sleep(1000L);
                //wait.until(ExpectedConditions.elementToBeClickable(By.className("getdata-button")));
                getButton = driver.findElement(By.className("getdata-button"));

                getButton.click();
                System.out.println("After get data button");

                try {
                    wait.until(new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver webDriver) {
                            System.out.println("Searching ...");
                            return webDriver.findElement(By.linkText(bhavfileName)) != null;
                        }
                    });
                } catch (TimeoutException ex) {
                    //No Such file exists go back;
                    return;
                }

                WebElement getlink = driver.findElement(By.linkText(bhavfileName));
                getlink.click();
                do {
                    Thread.sleep(1000);
                } while (!bhavFile.exists());
                System.out.println(bhavfileName + " File length: " + bhavFile.length());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void downloadFile(String option, String fileName, String dateString)
    {
        try {
            System.out.println("Downloading File " + option);
            System.out.println("Downloading File " + fileName);
            driver.get("http://www.nseindia.com/products/content/derivatives/equities/archieve_fo.htm");
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    System.out.println("Searching ...");
                    return webDriver.findElement(By.id("h_filetype")) != null;
                }
            });

            System.out.println("Downloading File setting filet type" + option);

            new Select(driver.findElement(By.id("h_filetype"))).selectByVisibleText(option);
            WebElement dateElement = driver.findElement(By.id("date"));
            dateElement.clear();
            dateElement.sendKeys(dateString);
            dateElement.sendKeys(Keys.ENTER);
            //dateElement.sendKeys(Keys.TAB);
            //dateElement.sendKeys(Keys.ENTER);
            Thread.sleep(1000L);
            WebElement getButton = driver.findElement(By.className("getdata-button"));
            getButton.click();
            File f = new File(downloadPath+fileName);
            if(f.exists())f.delete();
            System.out.println("After get data button");

            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    System.out.println("Searching ...");
                    return webDriver.findElement(By.linkText(fileName)) != null;
                }
            });

            WebElement getlink = driver.findElement(By.linkText(fileName));
            getlink.click();
            do {
                Thread.sleep(1000);
            } while (!f.exists());
            System.out.println( fileName + " File length: " + f.length());

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void downloadOptionChain()
    {
        startBrowser();
        List<String> options = db.getOptionList("OPTSTK");
        for(String option: options)
        {

            String[] parts = option.split(",");
            String symbol = parts[1].trim();
            String type = parts[0].trim();
            saveOptionChain(symbol);
        }
        saveOptionChain("NIFTY");
        saveOptionChain("BANKNIFTY");
        saveOptionChain("NIFTYIT");
    }

    public void saveOptionChain(String symbol) {
        System.out.println("Downloading Option Chain For " + symbol);
        TreeMap<Double, ZdOptionChain> list = downloadOptionChain(symbol);

        System.out.println("Saving Option Chain For " + symbol);
        for(ZdOptionChain oc: list.values() ){
            db.saveOptionChain(oc);
        }
    }

//    public HashMap<Double, ZdOptionChain> downloadOptionChain(String underline)
//    {
//        startBrowser();
//        HashMap<Double, ZdOptionChain> ocList = new HashMap<>();
//        LocalDate asOfDate = LocalDate.now();
//
//        try {
//            System.out.println("Downloading Option Chain for " + underline);
//            driver.get("http://nseindia.com/live_market/dynaContent/live_watch/option_chain/optionKeys.jsp?symbol=" + URLEncoder.encode(underline) + "&instrument=-&date=-");
//
//            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
//
//            WebElement ocForm =  driver.findElement(By.id("ocForm"));
//            WebElement hdoctable =  driver.findElement(By.id("wrapper_btm"));
//            List<WebElement> tables = hdoctable.findElements(By.tagName("table"));
//            System.out.println(tables.size());
//            double underlinePrice=0.0;
//            LocalDateTime dt = LocalDateTime.now();
//            for(WebElement table: tables)
//            {
//                int rowCount = table.findElements(By.tagName("tr")).size();
//                if(rowCount > 1) continue;
//                System.out.println(table.getAttribute("innerHTML"));
//                String currPriceString = table.findElement(By.tagName("b")).getText();
//                String[] parts1 = currPriceString.split(" ");
//                underlinePrice = Double.valueOf(parts1[1]);
//                String currTimeString = table.findElements(By.tagName("span")).get(1).getText();
//                currTimeString  = currTimeString.replaceAll("As on ", "");
//                currTimeString  = currTimeString.replaceAll("IST","").trim();
//                DateTimeFormatter fff = DateTimeFormatter.ofPattern("MMM dd, yyyy kk:mm:ss");
//                dt = LocalDateTime.parse(currTimeString,fff);
//                //As on Feb 05, 2016 15:30:45 IST
//                System.out.println(table.findElement(By.tagName("b")).getText());
//                System.out.println(currTimeString.replaceAll("As on ",""));
//                break;
//            }
//
//            asOfDate = dt.toLocalDate();
//            Select dateSelect = new Select(ocForm.findElement(By.id("date")));
//            String selectedDate = dateSelect.getFirstSelectedOption().getText();
//            LocalDate expiryDate = LocalDate.parse(selectedDate, ddMMMyyyy);
//            WebElement ocTable =  driver.findElement(By.id("octable"));
//
//            List<WebElement> trs = ocTable.findElements(By.tagName("tr"));
//            for(int r=2;r < trs.size();r++){
//                WebElement tr = trs.get(r);
//                List<WebElement> tds = tr.findElements(By.tagName("td"));
//                if( tds.size() < 8 ) continue;
//                long oiCall =  tds.get(1).getText().equals("-")? 0 : Long.valueOf(tds.get(1).getText().replaceAll(",",""));
//                long oiPut = tds.get(21).getText().equals("-")? 0 : Long.valueOf(tds.get(21).getText().replaceAll(",", ""));
//                if( oiCall == 0 && oiPut == 0 ) continue;
//                long oicCall = tds.get(2).getText().equals("-")? 0 : Long.valueOf(tds.get(2).getText().replaceAll(",", ""));
//                long vCall = tds.get(3).getText().equals("-")? 0 : Long.valueOf(tds.get(3).getText().replaceAll(",", ""));
//                double ivCall = tds.get(4).getText().equals("-")? 0.0 : Double.valueOf(tds.get(4).getText().replaceAll(",", ""));
//                double ltpCall = tds.get(5).getText().equals("-")? 0.0 : Double.valueOf(tds.get(5).getText().replaceAll(",", ""));
//                double changeCall = tds.get(6).getText().equals("-")? 0.0 : Double.valueOf(tds.get(6).getText().replaceAll(",", ""));
//                int bidQtyCall =  tds.get(7).getText().equals("-")? 0 : Integer.valueOf(tds.get(7).getText().replaceAll(",", ""));
//                double bidPriceCall = tds.get(8).getText().equals("-")? 0.0 : Double.valueOf(tds.get(8).getText().replaceAll(",", ""));
//                double askPriceCall = tds.get(9).getText().equals("-")? 0.0 : Double.valueOf(tds.get(9).getText().replaceAll(",", ""));
//                int askQtyCall = tds.get(10).getText().equals("-")? 0 : Integer.valueOf(tds.get(10).getText().replaceAll(",", ""));
//                Double strike = tds.get(11).getText().equals("-")? 0.0 : Double.valueOf(tds.get(11).getText().replaceAll(",", ""));
//                int bidQtyPut = tds.get(12).getText().equals("-")? 0 : Integer.valueOf(tds.get(12).getText().replaceAll(",", ""));
//                double bidPricePut = tds.get(13).getText().equals("-")? 0.0 : Double.valueOf(tds.get(13).getText().replaceAll(",", ""));
//                double askPricePut = tds.get(14).getText().equals("-")? 0.0 : Double.valueOf(tds.get(14).getText().replaceAll(",", ""));
//                int askQtyPut = tds.get(15).getText().equals("-")? 0 : Integer.valueOf(tds.get(15).getText().replaceAll(",", ""));
//                double changePut = tds.get(16).getText().equals("-")? 0.0 : Double.valueOf(tds.get(16).getText().replaceAll(",", ""));
//                double ltpPut = tds.get(17).getText().equals("-")? 0.0 : Double.valueOf(tds.get(17).getText().replaceAll(",", ""));
//                double ivPut = tds.get(18).getText().equals("-")? 0.0 : Double.valueOf(tds.get(18).getText().replaceAll(",", ""));
//                long vPut = tds.get(19).getText().equals("-")? 0 : Long.valueOf(tds.get(19).getText().replaceAll(",", ""));
//                long oicPut = tds.get(20).getText().equals("-")? 0 : Long.valueOf(tds.get(20).getText().replaceAll(",", ""));
//                String oSymbol = underline + yyMMM.format(expiryDate).toUpperCase() + strikePriceFormatter.format(strike);
//                Option call = new Option(oSymbol+"CE",underline, underlinePrice, "CE",expiryDate,strike, ltpCall, changeCall, askPriceCall,askQtyCall,bidPriceCall,bidQtyCall, ivCall, oiCall, oicCall,vCall);
//                Option put = new Option(oSymbol+"PE",underline, underlinePrice, "PE",expiryDate,strike,ltpPut, changePut, askPricePut,askQtyPut ,bidPricePut,bidQtyPut, ivPut, oiPut, oicPut,vPut);
//                ZdOptionChain oc = new ZdOptionChain(underline, underlinePrice, asOfDate, expiryDate, strike,call,put,0.0735);
//                oc.greeks(0.0735,asOfDate);
//                ocList.put(strike,oc);
//            }
//
//        } catch (Exception ex){
//            ex.printStackTrace();
//        }
//        return ocList;
//    }



    public void dailyRun() throws Exception
    {
        LocalDate today = LocalDate.now();
        int lastDate =  db.getLastDateEod("RELIANCE");
        int todayInt = DateUtil.intDate(today);
        int daysToFetch = todayInt - lastDate;
        System.out.println("DailyRun: Last Days: " + lastDate);
        for(int i=0; i< daysToFetch; i++)
        {
            LocalDate prevDay  = today.minusDays(i);

            if ( prevDay.getDayOfWeek().getValue() > 5 ) continue;

            downloadEqBhav(prevDay);
            downloadIndexFile(prevDay);
            processEqFile(prevDay);
            processEqVol(prevDay);
            processIndexFile(prevDay);
        }

        //Now download Option Chains
        downloadOptionChain();
        printDates();
    }

    public TreeMap<Double, ZdOptionChain> downloadOptionChain(String underline)
    {
        return downloadOptionChain(underline,"-");
    }
    public TreeMap<Double, ZdOptionChain> downloadOptionChain(String underline, String expMonth)
    {
        if(expMonth == null || expMonth.length()==0) expMonth="-";
        String url = "http://nseindia.com/live_market/dynaContent/live_watch/option_chain/optionKeys.jsp?symbol=" + URLEncoder.encode(underline) + "&instrument=-&date="+expMonth;
        System.out.println(url);

        LocalDate asOfDate = LocalDate.now();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept" , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.addHeader("Accept-Encoding" , "gzip, deflate");
        get.addHeader("Accept-Language" , "en-US,en;q=0.5");
        get.addHeader("Connection" , "keep-alive");
        get.addHeader("Host" , "nseindia.com");
        get.addHeader("DNT" , "1");
        get.addHeader("User-Agent" , "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0");
        TreeMap<Double, ZdOptionChain> ocList = new TreeMap<>();
        try {

            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Connection", "keep-alive")
                        .header("Host", "nseindia.com")
                        .header("DNT", "1")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0")
                        .get();

            Element hdoctable =  doc.select("#wrapper_btm").get(0);
            Elements tables = hdoctable.select("table");
            System.out.println(tables.size());
            double underlinePrice=0.0;
            LocalDateTime dt = LocalDateTime.now();
            for(Element table: tables)
            {
                int rowCount = table.select("tr").size();
                if(rowCount > 1) continue;
                String currPriceString = table.select("b").text();
                String[] parts1 = currPriceString.split(" ");
                underlinePrice = Double.valueOf(parts1[1]);
                String currTimeString = table.select("span").get(1).text();
                currTimeString  = currTimeString.replaceAll("As on ", "");
                currTimeString  = currTimeString.replaceAll("IST","").trim();
                DateTimeFormatter fff = DateTimeFormatter.ofPattern("MMM dd, yyyy kk:mm:ss");
                dt = LocalDateTime.parse(currTimeString,fff);
                //As on Feb 05, 2016 15:30:45 IST
                System.out.println(table.select("b").text());
                System.out.println(currTimeString.replaceAll("As on ",""));
                break;
            }
            System.out.println("  +++++ underlinePrice ++++ " + underlinePrice);
            asOfDate = dt.toLocalDate();
            Element ocForm =  doc.select("#ocForm").get(0);
            Element dateSelect = ocForm.select("#date option[selected]").get(0);
            LocalDate expiryDate = LocalDate.parse(dateSelect.text(), ddMMMyyyy);

            Element octable = doc.select("#octable").get(0);
            Elements  trs = octable.select("tr");
            for(int r=2;r < trs.size();r++){
                Element tr = trs.get(r);
                Elements tds = tr.select("td");
                if( tds.size() < 8 ) continue;
                long oiCall =  tds.get(1).text().equals("-")? 0 : Long.valueOf(tds.get(1).text().replaceAll(",",""));
                long oiPut = tds.get(21).text().equals("-")? 0 : Long.valueOf(tds.get(21).text().replaceAll(",", ""));
                if( oiCall == 0 && oiPut == 0 ) continue;
                long oicCall = tds.get(2).text().equals("-")? 0 : Long.valueOf(tds.get(2).text().replaceAll(",", ""));
                long vCall = tds.get(3).text().equals("-")? 0 : Long.valueOf(tds.get(3).text().replaceAll(",", ""));
                double ivCall = tds.get(4).text().equals("-")? 0.0 : Double.valueOf(tds.get(4).text().replaceAll(",", ""));
                double ltpCall = tds.get(5).text().equals("-")? 0.0 : Double.valueOf(tds.get(5).text().replaceAll(",", ""));
                double changeCall = tds.get(6).text().equals("-")? 0.0 : Double.valueOf(tds.get(6).text().replaceAll(",", ""));
                int bidQtyCall =  tds.get(7).text().equals("-")? 0 : Integer.valueOf(tds.get(7).text().replaceAll(",", ""));
                double bidPriceCall = tds.get(8).text().equals("-")? 0.0 : Double.valueOf(tds.get(8).text().replaceAll(",", ""));
                double askPriceCall = tds.get(9).text().equals("-")? 0.0 : Double.valueOf(tds.get(9).text().replaceAll(",", ""));
                int askQtyCall = tds.get(10).text().equals("-")? 0 : Integer.valueOf(tds.get(10).text().replaceAll(",", ""));
                Double strike = tds.get(11).text().equals("-")? 0.0 : Double.valueOf(tds.get(11).text().replaceAll(",", ""));
                int bidQtyPut = tds.get(12).text().equals("-")? 0 : Integer.valueOf(tds.get(12).text().replaceAll(",", ""));
                double bidPricePut = tds.get(13).text().equals("-")? 0.0 : Double.valueOf(tds.get(13).text().replaceAll(",", ""));
                double askPricePut = tds.get(14).text().equals("-")? 0.0 : Double.valueOf(tds.get(14).text().replaceAll(",", ""));
                int askQtyPut = tds.get(15).text().equals("-")? 0 : Integer.valueOf(tds.get(15).text().replaceAll(",", ""));
                double changePut = tds.get(16).text().equals("-")? 0.0 : Double.valueOf(tds.get(16).text().replaceAll(",", ""));
                double ltpPut = tds.get(17).text().equals("-")? 0.0 : Double.valueOf(tds.get(17).text().replaceAll(",", ""));
                double ivPut = tds.get(18).text().equals("-")? 0.0 : Double.valueOf(tds.get(18).text().replaceAll(",", ""));
                long vPut = tds.get(19).text().equals("-")? 0 : Long.valueOf(tds.get(19).text().replaceAll(",", ""));
                long oicPut = tds.get(20).text().equals("-")? 0 : Long.valueOf(tds.get(20).text().replaceAll(",", ""));

                if( ltpCall == 0 && ltpPut == 0 ) continue;
                if( ltpCall < 1 || ltpPut < 1 ) continue;


                String oSymbol = underline + yyMMM.format(expiryDate).toUpperCase() + strikePriceFormatter.format(strike);
                Option call = new Option(oSymbol+"CE",underline, underlinePrice, "CE",expiryDate,strike, ltpCall, changeCall, askPriceCall,askQtyCall,bidPriceCall,bidQtyCall, ivCall, oiCall, oicCall,vCall);
                Option put = new Option(oSymbol+"PE",underline, underlinePrice, "PE",expiryDate,strike,ltpPut, changePut, askPricePut,askQtyPut ,bidPricePut,bidQtyPut, ivPut, oiPut, oicPut,vPut);
                ZdOptionChain oc = new ZdOptionChain(underline, underlinePrice, asOfDate, expiryDate, strike,call,put,0.0735);
                oc.greeks(0.0735, asOfDate);
                ocList.put(strike, oc);
            }
//            CloseableHttpResponse response = httpclient.execute(get);
//            String result = EntityUtils.toString(response.getEntity());
//            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ocList;
    }

    public List<LocalDate> downloadDates()
    {
        String underline="NIFTY";
        String url = "http://nseindia.com/live_market/dynaContent/live_watch/option_chain/optionKeys.jsp?symbol=" + URLEncoder.encode(underline) + "&instrument=-&date=-";
        System.out.println(url);

        List<LocalDate> dates = new ArrayList<>();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept" , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.addHeader("Accept-Encoding" , "gzip, deflate");
        get.addHeader("Accept-Language" , "en-US,en;q=0.5");
        get.addHeader("Connection" , "keep-alive");
        get.addHeader("Host" , "nseindia.com");
        get.addHeader("DNT" , "1");
        get.addHeader("User-Agent" , "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0");
        HashMap<Double, ZdOptionChain> ocList = new HashMap<>();
        try {

            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .header("Host", "nseindia.com")
                    .header("DNT", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0")
                    .get();

            Element ocForm =  doc.select("#ocForm").get(0);
            Elements dateSelect = ocForm.select("#date option");
            boolean firstVal=true;
            for(Element elem: dateSelect) {
                if(firstVal) {
                    firstVal = false;
                    continue;
                }
                LocalDate expiryDate = LocalDate.parse(elem.text(), ddMMMyyyy);
                System.out.println(expiryDate);
                dates.add(expiryDate);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dates;
    }
    public void calculateMargin() {
        String url = "https://zerodha.com/margin-calculator/SPAN/";
        System.out.println(url);
        try {
                HttpClientBuilder b = HttpClientBuilder.create();

                // setup a Trust Strategy that allows all certificates.
                //
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();
                b.setSslcontext( sslContext);

                // don't check Hostnames, either.
                //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
                HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

                // here's the special part:
                //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
                //      -- and create a Registry, to register it.
                //
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

                // now, we create connection-manager using our Registry.
                //      -- allows multi-threaded use
                PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
                b.setConnectionManager( connMgr);

                // finally, build the HttpClient;
                //      -- done!
                HttpClient client = b.build();

            ObjectNode form = AppConfig.mapper.createObjectNode();
            form.put("action","calculate");
            form.put("exchange[]","NFO");
            form.put("product[]","OPT");
            form.put("scrip[]","AJANTPHARM16FEB");
            form.put("option_type[]","PE");
            form.put("strike_price[]",1240);
            form.put("qty[]",400);
            form.put("trade[]","sell");


//            action:calculate
//            exchange[]:NFO
//            product[]:OPT
//            scrip[]:AJANTPHARM16FEB
//            option_type[]:PE
//            strike_price[]:1240
//            qty[]:400
//            trade[]:sell
//            exchange[]:NFO
//            product[]:OPT
//            scrip[]:AJANTPHARM16FEB
//            option_type[]:PE
//            strike_price[]:1300
//            qty[]:400
//            trade[]:sell

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("action","calculate"));
            urlParameters.add(new BasicNameValuePair("exchange[]","NFO"));
            urlParameters.add(new BasicNameValuePair("product[]","OPT"));
            urlParameters.add(new BasicNameValuePair("scrip[]","AJANTPHARM16FEB"));
            urlParameters.add(new BasicNameValuePair("option_type[]","PE"));
            urlParameters.add(new BasicNameValuePair("strike_price[]","1240"));
            urlParameters.add(new BasicNameValuePair("qty[]","400"));
            urlParameters.add(new BasicNameValuePair("trade[]","sell"));
            urlParameters.add(new BasicNameValuePair("exchange[]","NFO"));
            urlParameters.add(new BasicNameValuePair("product[]","OPT"));
            urlParameters.add(new BasicNameValuePair("scrip[]","AJANTPHARM16FEB"));
            urlParameters.add(new BasicNameValuePair("option_type[]","PE"));
            urlParameters.add(new BasicNameValuePair("strike_price[]","1300"));
            urlParameters.add(new BasicNameValuePair("qty[]","400"));
            urlParameters.add(new BasicNameValuePair("trade[]","sell"));

            HttpPost p = new HttpPost(url);
            p.setEntity(new UrlEncodedFormEntity(urlParameters));

            p.addHeader("X-Requested-With","XMLHttpRequest");
            p.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
            p.addHeader("Accept-Encoding","gzip, deflate");
            p.addHeader("Accept-Language","en-US, en; q=0.5");
            p.addHeader("Cache-Control","no-cache");
            p.addHeader("Connection","Keep-Alive");
            p.addHeader("Referer","https://zerodha.com/margin-calculator/SPAN/");
            p.addHeader("Host","zerodha.com");
            p.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");

            //Content-Length: 157
            //Content-Type: ; charset=UTF-8
           //Cookie: PHPSESSID=47snf10eoo6idoisjsaeb2qi67; __utmc=134287610; __utma=134287610.79898486.1455725926.1455775819.1455781993.4; __utmz=134287610.1455775819.3.3.utmcsr=bing|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); _ga=GA1.2.79898486.1455725926; __utmb=134287610.2.10.1455781993; __utmt=1


            HttpResponse r = client.execute(p);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                //Parse our JSON response
//                JSONParser j = new JSONParser();
//                JSONObject o = (JSONObject)j.parse(line);
//                Map response = (Map)o.get("response");

                System.out.println(line);
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public void printDates(){

        MongoCollection col = db.database.getCollection("DailyBar");
        Document d = new Document();
        Document s = new Document("date", -1);
        d.put("symbol", "RELIANCE");
        MongoCursor<Document> cursor = col.find(d).sort(s).limit(10).iterator();

        try {
            while(cursor.hasNext()) {
                Document row = cursor.next();
                int oDate = row.getInteger("date");
                LocalDate d1 = DateUtil.toDate(oDate);
                System.out.println("Daily: " + d1.toString() + "  " + d1.getDayOfWeek().name());
            }
        } finally {
            cursor.close();
        }

        col = db.database.getCollection("Volatility");
        d = new Document();
        s = new Document("date", -1);
        d.put("symbol", "RELIANCE");
        cursor = col.find(d).sort(s).limit(10).iterator();

        try {
            while(cursor.hasNext()) {
                Document row = cursor.next();
                int oDate = row.getInteger("date");
                LocalDate d1 = DateUtil.toDate(oDate);
                System.out.println("Volatility: " + d1.toString() + "  " + d1.getDayOfWeek().name());
            }
        } finally {
            cursor.close();
        }

        col = db.database.getCollection("OptionChain");
        d = new Document();
        s = new Document("on_date", -1);
        d.put("symbol", "RELIANCE");
        cursor = col.find(d).sort(s).limit(10).iterator();

        try {
            while(cursor.hasNext()) {
                Document row = cursor.next();
                int oDate = row.getInteger("on_date");
                LocalDate d1 = DateUtil.toDate(oDate);
                System.out.println("OptionChain: " + d1.toString() + "  " + d1.getDayOfWeek().name());
            }
        } finally {
            cursor.close();
        }
    }

    public static double getNumValue(Cell cell)
    {
        if( cell.getCellType() == Cell.CELL_TYPE_STRING )
        {
            return Double.valueOf(cell.getStringCellValue());
        }
        return cell.getNumericCellValue();
    }

    public void getUrl(String url, Consumer<String> lineProcessor) throws Exception
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept" , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.addHeader("Accept-Encoding" , "gzip, deflate");
        get.addHeader("Accept-Language" , "en-US,en;q=0.5");
        get.addHeader("Connection" , "keep-alive");
        get.addHeader("Host" , "nseindia.com");
        get.addHeader("DNT" , "1");
        get.addHeader("User-Agent" , "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0");
        CloseableHttpResponse response = httpclient.execute(get);
        // Get the response
        BufferedReader rd = new BufferedReader
                (new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            lineProcessor.accept(line);
        }
        rd.close();
        httpclient.close();
    }

    public void getEod1(String symbol)
    {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate;
        while (startDate.isAfter(LocalDate.of(2007, 1, 1))) {
            startDate = startDate.minusDays(50);
            getEod1(symbol, startDate, endDate);
            endDate = startDate;
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getEod1()
    {
        List<ZdSymbol> symbols = AppConfig.db.getSymbols();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(15);
        for(ZdSymbol symbol:symbols) {
            //System.out.println(symbol.symbol);
            if (!symbol.type.equals("EQ")) continue;
            getEod1(symbol.symbol,startDate,endDate);
        }
    }

    public void getEod1Full()
    {
        List<ZdSymbol> symbols = AppConfig.db.getSymbols();

        for(ZdSymbol symbol:symbols) {
            //System.out.println(symbol.symbol);
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate;
            if (!symbol.type.equals("EQ")) continue;
            while (startDate.isAfter(LocalDate.of(2007, 1, 1))) {
                startDate = startDate.minusDays(50);
                getEod1(symbol.symbol, startDate, endDate);
                endDate = startDate;
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getEod1(String symbol, LocalDate startDate, LocalDate endDate)
    {
        try {
            System.out.println("Processing " + symbol + " " + startDate.toString() + "  ->  " + endDate.toString());
            String url = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=" + URLEncoder.encode(symbol, "UTF-8") + "&segmentLink=3&symbolCount=2&series=EQ&dateRange=+&fromDate=" + startDate.format(NseLoader.dd_mm_yyyy) + "&toDate=" + endDate.format(NseLoader.dd_mm_yyyy) + "&dataType=PRICEVOLUMEDELIVERABLE";
                    //https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=RELIANCE&segmentLink=3&symbolCount=2&series=ALL&dateRange=+&fromDate=01-03-2016&toDate=23-03-2016&dataType=PRICEVOLUMEDELIVERABLE
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .header("Host", "nseindia.com")
                    .header("DNT", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0")
                    .get();
            Elements trs = doc.select("tr");
            for(Element tr: trs)
            {
                try {
                    Elements tds = tr.select("td");
                    if( tds.size() < 8)
                    {
                        //System.out.println(" Symbol: " + symbol);
                        //System.out.println(tr.text());
                        continue;
                    }
                    String symboi = tds.get(0).text();
                    String series   = tds.get(1).text();
                    LocalDate date   = LocalDate.parse(tds.get(2).text(), NseLoader.dd_mmm_yyyy);
                    double prev  = Double.valueOf(tds.get(3).text().replaceAll(",",""));
                    double open   = Double.valueOf(tds.get(4).text().replaceAll(",", ""));
                    double high   = Double.valueOf(tds.get(5).text().replaceAll(",", ""));
                    double low   = Double.valueOf(tds.get(6).text().replaceAll(",", ""));
                    double close   = Double.valueOf(tds.get(8).text().replaceAll(",", ""));
                    double last = close;
                    if( ! tds.get(7).text().equals("-") ) last   = Double.valueOf(tds.get(7).text().replaceAll(",", ""));
                    double vwap   = Double.valueOf(tds.get(9).text().replaceAll(",", ""));
                    long tradedqty   = Long.valueOf(tds.get(10).text().replaceAll(",", ""));
                    double turnover   = Double.valueOf(tds.get(11).text().replaceAll(",", ""));
                    long trades   = 0L;
                    long delqty   = 0L;
                    if( ! tds.get(13).text().equals("-") ) delqty = Long.valueOf(tds.get(13).text().replaceAll(",", ""));
                    double del_pct   = 100.00;
                    if( ! tds.get(14).text().equals("-") ) del_pct = Double.valueOf(tds.get(14).text().replaceAll(",", ""));
                    AppConfig.db.saveEod1(symbol,series,date,prev,open,high,low,last,close,vwap,tradedqty,turnover,trades,delqty,del_pct);
                } catch (MongoWriteException ex)
                {

                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public String getSplit(String symbol, LocalDate startDate)
    {
        try {
            //System.out.println("Processing " + symbol + " " + startDate.toString() + "  ->  " + startDate.toString());
            String url = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=" + URLEncoder.encode(symbol, "UTF-8") + "&segmentLink=3&symbolCount=2&series=EQ&dateRange=+&fromDate=" + startDate.format(NseLoader.dd_mm_yyyy) + "&toDate=" + startDate.format(NseLoader.dd_mm_yyyy) + "&dataType=PRICEVOLUMEDELIVERABLE";
            //https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=RELIANCE&segmentLink=3&symbolCount=2&series=ALL&dateRange=+&fromDate=01-03-2016&toDate=23-03-2016&dataType=PRICEVOLUMEDELIVERABLE
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,appliffcation/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .header("Host", "nseindia.com")
                    .header("DNT", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0")
                    .get();
            Elements trs = doc.select("tr");
            for(Element tr: trs)
            {
                Elements tds = tr.select("td");
                if( tds.size() < 6 ) continue;
                String title   = tds.get(2).attr("title");
                //System.out.println("Title: " + title);
                if( title != null) return title;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    public void processFiiFile(LocalDate date){
        try {
            String dateString = date.format(dd_mmm_yyyy);
            String excelFileName = "fii_stats_" + dateString.substring(0,3)+ dateString.substring(3,4).toUpperCase() + dateString.substring(4,6) + dateString.substring(6) +".xls";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            String url = "https://www1.nseindia.com/content/fo/"+excelFileName;
            HttpGet get = new HttpGet(url);
            get.addHeader("Accept" , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            get.addHeader("Accept-Encoding" , "gzip, deflate");
            get.addHeader("Accept-Language" , "en-US,en;q=0.5");
            get.addHeader("Connection" , "keep-alive");
            get.addHeader("Host" , "nseindia.com");
            get.addHeader("DNT" , "1");
            get.addHeader("User-Agent" , "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0");
            CloseableHttpResponse response = httpclient.execute(get);
            InputStream is = response.getEntity().getContent();
            String filePath = "d:\\savedata\\"+excelFileName;
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            int inByte;
            while((inByte = is.read()) != -1)
                fos.write(inByte);
            is.close();
            fos.close();
            response.close();

            System.out.println("Processing file " + filePath);
            FileInputStream inputStream = new FileInputStream(new File(filePath));
            Workbook workbook = new HSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);

            int startRow=0;
            int startCell=0;
            outerloop:
            for( int i=0; i< 10; i++)
            {
                startRow = i;
                Row row = firstSheet.getRow(i);
                for(int c=0;c<5;c++) {
                    startCell=c;
                    Cell cell = row.getCell(c);
                    if( cell.getCellType() == Cell.CELL_TYPE_STRING )
                    {
                        String value = cell.getStringCellValue();
                        if( value.startsWith("INDEX")) break outerloop;
                    }
                }
            }
            for( int i=0; i< 4; i++)
            {
                Row row = firstSheet.getRow(startRow + i);
                String type = row.getCell(startCell).getStringCellValue();
                double buyContracts = getNumValue(row.getCell(startCell+1));
                double buyAmount = getNumValue(row.getCell(startCell+2));
                double sellContracts = getNumValue(row.getCell(startCell+3));
                double sellAmount = getNumValue(row.getCell(startCell+4));
                double oiContracts = getNumValue(row.getCell(startCell+5));
                double oiAmount = getNumValue(row.getCell(startCell+6));

                AppConfig.db.saveFiiData(date,type,buyContracts,buyAmount,sellContracts,sellAmount,
                        oiContracts,oiAmount);
            }

            inputStream.close();
            new File(filePath).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){

        try {
            NseLoader loader = new NseLoader();
            //AppConfig.db.deleteEod("ABIRLANUVO");
            //loader.getEod1("ABIRLANUVO");
            //AppConfig.db.updateSplit("APOLLOHOSP",LocalDate.of(2010,9,2),2);

            List<ZdSplit> list = AppConfig.db.findSplits();
            for(ZdSplit split: list)
            {
                System.out.println(split.symbol + " " + split.date.toString() + " " + split.ratio);
                String isSplit = loader.getSplit(split.symbol,split.date);
                if( isSplit != null && isSplit.contains("BONUS 1:1"))
                {
                    System.out.println(split.date.toString());
                    System.out.println(isSplit);
                }
                //break;
            }
                    //loader.getEod1("HAVELLS");
            //AppConfig.db.fix();
//            loader.dailyRun();
            //AppConfig.db.fixMissing();
            //AppConfig.db.findSplits();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
