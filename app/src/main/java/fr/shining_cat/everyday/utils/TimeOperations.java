package fr.shining_cat.everyday.utils;

import android.util.Log;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import fr.shining_cat.everyday.R;

public abstract class TimeOperations {

    private static final String TAG = "LOGGING::TimeOperations";

    public static final String TIME_IS_ZERO = "given duration is zero!";

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
        }
        //if(milliseconds >= 60*1000) { // time is > 1mn, display minutes value
            if(milliseconds >= 60*60*1000) { //Only format minutes on two digits when total > 1h (there's something on the left of the minutes value)
                if(fullMinutes == 0) {//do not display minutes value if it's 0 and hours value is not 0
                    return durationString;
                } else{
                    durationString += " ";
                    durationString += String.format("%02d", fullMinutes);
                }
            }else {
                durationString += " ";
                durationString += String.valueOf(fullMinutes);
            }
            if(spaced) durationString += " ";
            durationString += minutesSymbol;
        //}
        return durationString;
    }

////////////////////////////////////////
//static helper method to convert a long milliseconds value to a formatted string, passing the desired symbols for hours, minutes and seconds, and if one wants a space between value and symbol
    public static String convertMillisecondsToHoursMinutesAndSecondsString(long totalMilliseconds,
                                                                           String hoursSymbol,
                                                                           String minutesSymbol,
                                                                           String secondsSymbol,
                                                                           boolean spaceBetweenValueAndUnit){
        String durationString = "";
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(totalMilliseconds);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(totalMilliseconds) - TimeUnit.HOURS.toMinutes(fullHours));
        int fullSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(totalMilliseconds) - (TimeUnit.HOURS.toSeconds(fullHours) + TimeUnit.MINUTES.toSeconds(fullMinutes)));
        if(totalMilliseconds >= 60*60*1000) { // time is > 1h, display hours value
            durationString = String.valueOf(fullHours);//we never format hours on two digits
            if (spaceBetweenValueAndUnit) durationString += " ";
            durationString += hoursSymbol;
        }
        if(totalMilliseconds >= 60*1000) { // time is > 1mn, display minutes value
            durationString += " ";
            if(totalMilliseconds >= 60*60*1000) { //only format minutes on two digits when total > 1h (=there's something on the left of the minutes value)
                durationString += String.format("%02d", fullMinutes);
            }else {
                durationString += String.valueOf(fullMinutes);
            }
            if(spaceBetweenValueAndUnit) durationString += " ";
            durationString += minutesSymbol;
        }
        if(totalMilliseconds >= 1000 && fullSeconds != 0) { // time is > 1s, display seconds value
            durationString += " ";
            if(totalMilliseconds >= 60*1000) { //only format seconds on two digits when total > 1mn (=there's something on the left of the seconds value)
                durationString += String.format("%02d", fullSeconds);
            }else {
                durationString += String.valueOf(fullSeconds);
            }
            if(spaceBetweenValueAndUnit) durationString += " ";
            durationString += secondsSymbol;
        }
        if(totalMilliseconds == 0){
            durationString = TIME_IS_ZERO;
        }
        return durationString;
    }

////////////////////////////////////////
//static helper method returns a Date object set to TODAY at midnight
    public static Date giveTodayMidnightDate(){
        // today
        Calendar today = Calendar.getInstance();
        // reset hour, minutes, seconds and millis
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

////////////////////////////////////////
//static helper method returns a timestamp in Millis set to SAME DAY at midnight MODIFYING GIVEN CALENDAR
    public static long setTimePartOfDateToMidnight(Calendar calendar){
        // reset hour, minutes, seconds and millis
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

////////////////////////////////////////
//static helper method returns a timestamp in Millis set to SAME DAY at midnight WITHOUT MODIFYING GIVEN CALENDAR
    public static long getTimestampSameDateAtMidnight(Calendar calendar){
        //duplicating given calendar
        Calendar modifyingCalendar = Calendar.getInstance();
        modifyingCalendar.setTimeInMillis(calendar.getTimeInMillis());
        // reset hour, minutes, seconds and millis
        modifyingCalendar.set(Calendar.HOUR_OF_DAY, 0);
        modifyingCalendar.set(Calendar.MINUTE, 0);
        modifyingCalendar.set(Calendar.SECOND, 0);
        modifyingCalendar.set(Calendar.MILLISECOND, 0);
        return modifyingCalendar.getTimeInMillis();
    }

}
