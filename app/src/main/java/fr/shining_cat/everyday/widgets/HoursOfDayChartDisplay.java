package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;

import fr.shining_cat.everyday.R;

public class HoursOfDayChartDisplay extends ChartDisplay {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private Context mContext;

    public HoursOfDayChartDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected String buildDisplayValueString(String baseString, ArrayList<String> elements, float xPosition) {
        String displayString = "";
        String[] displayStringElements = new String[elements.size() + 2];
        for(int i = 0; i < elements.size(); i ++){
            displayStringElements[i] = elements.get(i);
        }
        displayStringElements[elements.size()] = formatHourForDisplayOfMidnight(xPosition, true);
        displayStringElements[elements.size() + 1] = formatHourForDisplayOfMidnight(xPosition, false);
        displayString = String.format(baseString, (Object[]) displayStringElements);
        //Log.d(TAG, "buildDisplayValueString::displayString = " + displayString);
        return displayString;
    }

////////////////////////////////////////
//helper method to format as string starting (isStartHour = true) or ending hour (isStartHour = false) (=replace by "midnight" string when correct)
    private String formatHourForDisplayOfMidnight(float xPos, boolean isStartHour) {
        xPos = Math.round(xPos);//need to round because multi-bars graphs will send offsetted x positions (by half of bar width)
        if (isStartHour) {
            String startHour = String.valueOf((int) xPos) + mContext.getString(R.string.generic_string_SHORT_HOURS);
            if (xPos == 0) {
                startHour = mContext.getString(R.string.midnight);
            }
            return startHour;
        } else {
            String endHour = String.valueOf((int) xPos + 1) + mContext.getString(R.string.generic_string_SHORT_HOURS);
            if (xPos == 23) {
                endHour = mContext.getString(R.string.midnight);
            }
            return endHour;
        }

    }

}
