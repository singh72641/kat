package com.punwire.kat.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.punwire.kat.data.MongoDb;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Kanwal on 16/01/16.
 */
public class AppConfig {

    public static final long APPVERSION = 100L;
    public static ObjectMapper mapper = new ObjectMapper();
    public static MongoDb db = new MongoDb();


}
