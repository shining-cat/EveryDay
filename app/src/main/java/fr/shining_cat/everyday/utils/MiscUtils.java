package fr.shining_cat.everyday.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MiscUtils {

    public static double roundTwoDecimals(double d){
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public static ArrayList<Integer> getEmptyList(int size){
        ArrayList<Integer> emptyList = new ArrayList<>();
        for(int i = 0; i < size; i ++){
            emptyList.add(i);
        }
        return emptyList;
    }
}
