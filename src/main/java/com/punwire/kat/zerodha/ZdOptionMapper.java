package com.punwire.kat.zerodha;

/**
 * Created by Kanwal on 02/02/16.
 */
public class ZdOptionMapper {

    public static String getUnderline(String name)
    {

        return beforeNum(name);
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
