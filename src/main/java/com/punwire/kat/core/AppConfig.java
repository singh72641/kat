package com.punwire.kat.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.data.MongoDb;
import com.punwire.kat.data.NseLoader;
import com.punwire.kat.zerodha.ZdOptionStore;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Kanwal on 16/01/16.
 */
public class AppConfig {

    public static final long APPVERSION = 100L;
    public static ObjectMapper mapper = new ObjectMapper();
    public static MongoDb db = new MongoDb();
    public static NseLoader loader = new NseLoader();
    public static ZdOptionStore store = new ZdOptionStore();

    public static ObjectNode objectNode()
    {
        return mapper.createObjectNode();
    }

    public static ArrayNode arrayNode()
    {
        return mapper.createArrayNode();
    }
}
