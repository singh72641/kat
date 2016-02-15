package com.punwire.kat.data;

import ch.qos.logback.core.util.FileUtil;
import com.punwire.kat.core.DateUtil;
import com.punwire.kat.core.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kanwal on 13/01/16.
 */
public class CsvLoader {
    public static String dataPath="d:\\savedata\\";
    MongoDb db = new MongoDb();

    public void load(boolean removeExisting) {
        File folder = new File(dataPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();
                String symbol = fileName.replace(".txt", "");
                System.out.println("File " + fileName.replace(".txt", ""));
                db.saveStock(new Stock(symbol));
                if( removeExisting ) db.removeSymbol(symbol);
                load(symbol);
            }
        }


    }
    public void load(String symbol){
        File f = new File(dataPath + symbol + ".txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            line = reader.readLine();
            List<Bar> bars = new ArrayList<>();
            while(line != null)
            {
                String[] parts = line.split(",");

                //print(parts.get(0))
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDateTime = LocalDateTime.parse(parts[1], format);
                long startTime = DateUtil.longDateTime(startDateTime);
                System.out.println(startTime);
                double open = Double.valueOf(parts[2]);
                double high = Double.valueOf(parts[3]);
                double low = Double.valueOf(parts[4]);
                double close = Double.valueOf(parts[5]);
                long volume = Long.valueOf(parts[6]);
                long oi = 0;
                if( parts.length > 7 ) oi = Long.valueOf(parts[7]);

                Bar b = new Bar(symbol, startTime, 1, open,high,low,close,volume,oi);
                bars.add(b);
                line = reader.readLine();
            }
            db.saveBars(symbol,bars);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
