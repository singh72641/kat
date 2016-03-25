package com.punwire.kat.zerodha;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punwire.kat.core.AppConfig;

/**
 * Created by Kanwal on 13/02/16.
 */
public class ZdSymbol {
    public String name;
    public String symbol;
    public int lotSize;
    public String type;

    public ZdSymbol(String name, String symbol, int lotSize, String type)
    {
        this.name = name;
        this.lotSize = lotSize;
        this.symbol = symbol;
        this.type = type;
    }

    public ObjectNode toJson(){
        ObjectNode result = AppConfig.objectNode();
        result.put("symbol", symbol);
        result.put("name", name);
        result.put("lot", lotSize);
        result.put("type", type);
        return result;
    }

}
