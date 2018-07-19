package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;

import fr.shining_cat.everyday.R;
import fr.shining_cat.everyday.analytics.DurationStats;
import fr.shining_cat.everyday.utils.TimeOperations;

public class DurationChartDisplay extends ChartDisplay {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private Context mContext;

    public DurationChartDisplay(Context context, @Nullable AttributeSet attrs) {
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
        displayStringElements[elements.size()] = formatXposAsTopOrBottomOfDurationSlice(xPosition, true);
        displayStringElements[elements.size() + 1] = formatXposAsTopOrBottomOfDurationSlice(xPosition, false);
        displayString = String.format(baseString, (Object[]) displayStringElements);
        //Log.d(TAG, "buildDisplayValueString::displayString = " + displayString);
        return displayString;
    }

////////////////////////////////////////
//helper method to format as string top (isBottomOfDurationSlice = true) or bottom (isBottomOfDurationSlice = false) of the "duration-slice" (=replace 0 by "zéro" string when correct)
    private String formatXposAsTopOrBottomOfDurationSlice(float xPos, boolean isBottomOfDurationSlice) {
        xPos = Math.round(xPos);//need to round because multi-bars graphs will send offsetted x positions (by half of bar width)
        if (isBottomOfDurationSlice) {
            long bottomOfDurationSlice = (int) xPos * DurationStats.DURATION_STAT_SLICE_LENGTH;
            String bottomOfDurationSliceString = TimeOperations.convertMillisecondsToHoursAndMinutesString(
                                                        bottomOfDurationSlice,
                                                        mContext.getString(R.string.generic_string_HOURS),
                                                        mContext.getString(R.string.generic_string_MINUTES),
                                                        true
                                                );
            if (xPos == 0) {
                bottomOfDurationSliceString = mContext.getString(R.string.zero);
            }
            return bottomOfDurationSliceString;
        } else {
            long topOfDurationSlice = (int) (xPos + 1) * DurationStats.DURATION_STAT_SLICE_LENGTH;
            String topOfDurationSliceString = TimeOperations.convertMillisecondsToHoursAndMinutesString(
                                                        topOfDurationSlice,
                                                        mContext.getString(R.string.generic_string_HOURS),
                                                        mContext.getString(R.string.generic_string_MINUTES),
                                                        true
                                                );
            return topOfDurationSliceString;
        }

    }
}
