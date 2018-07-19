package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;

import fr.shining_cat.everyday.R;

public class DaysOfWeekChartDisplay extends ChartDisplay {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private Context mContext;

    public DaysOfWeekChartDisplay(Context context, @Nullable AttributeSet attrs) {
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
        displayStringElements[elements.size()] = formatDayForDisplay(xPosition);
        displayString = String.format(baseString, (Object[]) displayStringElements);
        //Log.d(TAG, "buildDisplayValueString::displayString = " + displayString);
        return displayString;
    }

////////////////////////////////////////
//helper method to format as string the day of week. in Weekstats we have set the days order to be starting on monday (index 0) and ending on sunday (index 6)
    private String formatDayForDisplay(float xPos) {
        int dayIndex = Math.round(xPos);//need to round because multi-bars graphs will send offsetted x positions (by half of bar width)
        String[] weekdays = mContext.getResources().getStringArray(R.array.weekdays_array);
        return weekdays[dayIndex];
    }

}
