package fr.shining_cat.meditappli.utils;

import android.util.Log;


import java.util.concurrent.TimeUnit;

public class TimeOperations {

    private static final String TAG = "LOGGING::TimeOperations";

////////////////////////////////////////
//static helper method to convert a long milliseconds value to a formatted string, passing the desired symbols for hours and minutes, and if one wants a space between value and symbol
    public static String convertMillisecondsToHoursAndMinutesString(long milliseconds, String hoursSymbol, String minutesSymbol, boolean spaced){
        String durationString = "";
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(milliseconds);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(fullHours));
        if(fullHours!=0){
            durationString = String.valueOf(fullHours);
            if (spaced) durationString += " ";
            durationString += hoursSymbol;
            if(fullMinutes!=0) {
                durationString += " ";
                durationString += String.format("%02d", fullMinutes);
                if(spaced) durationString += " ";
                durationString += minutesSymbol;
            }
        } else{
            durationString += String.valueOf(fullMinutes);
            if(spaced) durationString += " ";
            durationString += minutesSymbol;
        }
        return durationString;
    }


}
