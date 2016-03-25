package com.punwire.kat.zerodha;

import com.punwire.kat.core.AppConfig;
import com.punwire.kat.data.NseLoader;

/**
 * Created by Kanwal on 02/02/16.
 */
public class ZdOptionMapper {

    public static String getUnderline(String name)
    {

        return beforeNum(name);
    }

    public static String getExpMonth(String name)
    {
        String sym = getUnderline(name);
        String eMonth = name.substring(sym.length(), sym.length() + 5 );
        return eMonth;
    }

    public static String getExpDate(String name)
    {
        String sym = getUnderline(name);
        String eMonth = name.substring(sym.length(), sym.length() + 5 );
        return AppConfig.db.getExpDates(eMonth).format(NseLoader.ddMMMyyyy).toUpperCase();
    }

    public static String getOptType(String name)
    {
        return name.endsWith("CE")?"CE":"PE";

    }

    public static String getStrike(String name)
    {
        String val = name.substring(0,name.length()-2);
        return onlyLastNum(val);

    }

    public static String beforeNum(String val)
    {
        for(int c=0;c<val.length();c++){
            if((val.charAt(c) >= '0' && val.charAt(c) <= '9')){
                return val.substring(0,c);
            }
        }
        return val;
    }

    public static String onlyLastNum(String val)
    {
        for(int c=(val.length()-1);c>=0;c--){
            if( !(val.charAt(c) >= '0' && val.charAt(c) <= '9') ){
                return val.substring(c+1);
            }
        }
        return val;
    }
}
