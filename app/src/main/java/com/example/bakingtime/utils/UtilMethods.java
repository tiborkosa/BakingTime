package com.example.bakingtime.utils;

import java.text.DecimalFormat;

public class UtilMethods {

    public static String removeZero(double d){
        DecimalFormat df = new DecimalFormat("###.#");
        return df.format(d);
    }

    public static String capitalizeFirstLetter(String s){
        s = s.toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
