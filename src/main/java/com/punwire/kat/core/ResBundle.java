package com.punwire.kat.core;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by Kanwal on 16/01/16.
 */
public class ResBundle {

    public static ResourceBundle bundle = ResourceBundle.getBundle("Bundle", Locale.getDefault());

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getMessage(String key, Object... params  ) {
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
