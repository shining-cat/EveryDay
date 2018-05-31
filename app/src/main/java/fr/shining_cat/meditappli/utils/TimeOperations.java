package fr.shining_cat.meditappli.utils;

import android.util.Log;


import java.util.concurrent.TimeUnit;

public class TimeOperations {

    private static final String TAG = "LOGGING::TimeOperations";

////////////////////////////////////////
//static helper method to convert a long milliseconds value to a formatted string, passing the desired symbols for hours and minutes, and if one wants a space between value and symbol
    public static String convertMillisecondsToHoursAndMinutesString(long milliseconds,
                                                                    String hoursSymbol,
                                                                    String minutesSymbol,
                                                                    boolean spaced){
        String durationString = "";
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(milliseconds);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(fullHours));
        if(milliseconds >= 60*60*1000) { // time is > 1h, display hours value
            durationString = String.valueOf(fullHours);//we never format hours on two digits
            if (spaced) durationString += " ";
            durationString += hoursSymbol;
            durationString += " ";
        }
        if(milliseconds >= 60*1000 && fullMinutes != 0) { // time is > 1mn and fullminutes is not 0, display minutes value
            if(milliseconds >= 60*60*1000) { //only format minutes on two digits when total > 1h (=there's something on the left of the minutes value)
                durationString += String.format("%02d", fullMinutes);
            }else {
                durationString += String.valueOf(fullMinutes);
            }
            if(spaced) durationString += " ";
            durationString += minutesSymbol;
            durationString += " ";
        }
        return durationString;
    }

////////////////////////////////////////
//static helper method to convert a long milliseconds value to a formatted string, passing the desired symbols for hours, minutes and seconds, and if one wants a space between value and symbol
    public static String convertMillisecondsToHoursMinutesAndSecondsString(long milliseconds,
                                                                           String hoursSymbol,
                                                                           String minutesSymbol,
                                                                           String secondsSymbol,
                                                                           boolean spaced){
        String durationString = "";
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(milliseconds);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(fullHours));
        int fullSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(milliseconds) - (TimeUnit.HOURS.toSeconds(fullHours) + TimeUnit.MINUTES.toSeconds(fullMinutes)));
        if(milliseconds >= 60*60*1000) { // time is > 1h, display hours value
            durationString = String.valueOf(fullHours);//we never format hours on two digits
            if (spaced) durationString += " ";
            durationString += hoursSymbol;
            durationString += " ";
        }
        if(milliseconds >= 60*1000) { // time is > 1mn, display minutes value
            if(milliseconds >= 60*60*1000) { //only format minutes on two digits when total > 1h (=there's something on the left of the minutes value)
                durationString += String.format("%02d", fullMinutes);
            }else {
                durationString += String.valueOf(fullMinutes);
            }
            if(spaced) durationString += " ";
            durationString += minutesSymbol;
            durationString += " ";
        }
        if(milliseconds >= 1000) { // time is > 1s, display seconds value
            if(milliseconds >= 60*1000) { //only format seconds on two digits when total > 1mn (=there's something on the left of the seconds value)
                durationString += String.format("%02d", fullSeconds);
            }else {
                durationString += String.valueOf(fullSeconds);
            }
            if(spaced) durationString += " ";
            durationString += secondsSymbol;
        }
        return durationString;
    }
}
