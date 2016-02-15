package com.punwire.kat.zerodha;

import com.punwire.kat.data.MongoDb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kanwal on 02/02/16.
 */
public class ZerodhaAuto {

    static WebDriver driver;
    static Wait<WebDriver> wait;
    static String downloadPath = "d:\\savedata\\";
    static MongoDb db = new MongoDb();
    static DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    static DateTimeFormatter df1 = DateTimeFormatter.ofPattern("ddMMMyyyy");
    static DateTimeFormatter df2 = DateTimeFormatter.ofPattern("ddMMyyyy");
    static HashMap<String, WebElement> navElements = new HashMap<>();
    public List<ZdHolding> zHoldings = new ArrayList<>();
    public List<ZdHolding> zPositions = new ArrayList<>();
    public void startBrowser()
    {
        FirefoxProfile profile = FirefoxDriverProfile();
        driver = new FirefoxDriver(profile);
        wait = new WebDriverWait(driver, 30);
    }

    public static FirefoxProfile FirefoxDriverProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.dir", "d:\\savedata");
        profile.setPreference("browser.helperApps.neverAsk.openFile",
                "text/csv,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/zip,text/csv,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
        profile.setPreference("browser.download.manager.focusWhenStarting", false);
        profile.setPreference("browser.download.manager.useWindow", false);
        profile.setPreference("browser.download.manager.showAlertOnComplete", false);
        profile.setPreference("browser.download.manager.closeWhenDone", false);
        return profile;
    }

    public void getHoldings(){
        zHoldings = new ArrayList<>();

        WebElement e = navElements.get("Holdings");
        e.click();

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                System.out.println("Searching ...");
                return webDriver.findElement(By.id("holdings-table")) != null;
            }
        });

        WebElement holdingTable =  driver.findElement(By.id("holdings-table"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        List<WebElement> trs = holdingTable.findElements(By.tagName("tr"));
        for(int r=1;r < trs.size();r++){
            WebElement tr = trs.get(r);
            List<WebElement> tds = tr.findElements(By.cssSelector("span.ng-binding"));
            if( tds.size() < 2 ) continue;
            String symbol = tds.get(0).getAttribute("innerHTML");
            String t1Qty = tds.get(1).getAttribute("innerHTML");
            String qty = tds.get(2).getAttribute("innerHTML");
            String price = tds.get(3).getAttribute("innerHTML");
            String cPrice = tds.get(4).getAttribute("innerHTML");
            String cPnL = "0.00";
            if ( tds.size() > 5 ) cPnL = tds.get(5).getAttribute("innerHTML");

            ZdHolding zh = new ZdHolding(symbol,"EQ",(Integer.valueOf(t1Qty) + Integer.valueOf(qty) ),Double.valueOf(price), Double.valueOf(cPrice), Double.valueOf(cPnL));
            zHoldings.add(zh);
        }
    }

    public void save(){
        db.clearHolding();
        db.saveHoldings(zHoldings);
        db.saveHoldings(zPositions);
    }
    public void refresh() {
        getPositions();
        getHoldings();
    }

    public void getNavs(){
        WebElement nav = driver.findElement(By.id("primary-nav"));
        List<WebElement> navs = nav.findElements(By.className("ng-scope"));

        for(WebElement n: navs){
            String navName = n.getAttribute("innerHTML");
            navElements.put(navName,n);
            System.out.println(navName);
        }
    }

    public  void getPositions(){
        zPositions = new ArrayList<>();
        WebElement p = navElements.get("Positions");
        p.click();

        wait.until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver webDriver) {
                System.out.println("Searching ...");
                return webDriver.findElement(By.id("positions-net-table")) != null;
            }
        });

        WebElement posTable =  driver.findElement(By.id("positions-net-table"));

        List<WebElement> trs = posTable.findElements(By.tagName("tr"));
        for(int r=1;r < trs.size();r++){
            WebElement tr = trs.get(r);
            List<WebElement> tds = tr.findElements(By.cssSelector("span.ng-binding"));
            if( tds.size() < 2 ) continue;
            WebElement ee =  tds.get(1);
            String s = ee.getText();
            String symbol = ee.getAttribute("innerHTML");
            String[] parts = s.split(" ");
            if( parts.length < 2 || (! parts[1].contains("NFO")) ) continue;
            String qty = tds.get(3).getAttribute("innerHTML");
            String price = tds.get(4).getAttribute("innerHTML");
            String cPrice = tds.get(5).getAttribute("innerHTML");
            String cPnL = "0.00";
            if ( tds.size() > 6 ) cPnL = tds.get(6).getAttribute("innerHTML");

            ZdHolding zh = new ZdHolding(parts[0].trim(), ZdOptionMapper.getUnderline(parts[0]), parts[1].trim(),Integer.valueOf(qty),Double.valueOf(price), Double.valueOf(cPrice), Double.valueOf(cPnL));
            zPositions.add(zh);
        }
    }
    public void start() {
        try {
            startBrowser();
            driver.get("https://kite.zerodha.com");
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            getNavs();
            refresh();
            save();



//            new Select(driver.findElement(By.id("h_filetype"))).selectByVisibleText(option);
//            WebElement dateElement = driver.findElement(By.id("date"));
//            dateElement.clear();
//            dateElement.sendKeys(dateString);
//            dateElement.sendKeys(Keys.ENTER);
//            dateElement.sendKeys(Keys.TAB);
//            dateElement.sendKeys(Keys.ENTER);
//            WebElement getButton = driver.findElement(By.className("getdata-button"));
//            getButton.click();
//            do {
//                Thread.sleep(1000);
//            } while (!f.exists());

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){
        ZerodhaAuto za = new ZerodhaAuto();
        za.start();
    }

}
