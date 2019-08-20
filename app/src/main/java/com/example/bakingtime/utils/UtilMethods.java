package com.example.bakingtime.utils;

import java.text.DecimalFormat;

/**
 * Helper methods to clean up data for the UI
 */
public class UtilMethods {

    /**
     * Removes the tailing zeros from double
     * @param d double to be converted to string and cleaned
     * @return the cleaned up string
     */
    public static String removeZero(double d){
        DecimalFormat df = new DecimalFormat("###.#");
        return df.format(d);
    }

    /**
     * Will capitalize the first character of the string
     * @param s string to be converted
     * @return updated string
     */
    public static String capitalizeFirstLetter(String s){
        s = s.toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
