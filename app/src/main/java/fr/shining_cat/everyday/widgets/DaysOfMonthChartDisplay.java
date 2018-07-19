package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;

public class DaysOfMonthChartDisplay extends ChartDisplay {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private Context mContext;

    public DaysOfMonthChartDisplay(Context context, @Nullable AttributeSet attrs) {
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
        displayStringElements[elements.size()] = String.valueOf(Math.round(xPosition + 1)); //offsetting to display first day of month as 1 and not 0
        displayString = String.format(baseString, (Object[]) displayStringElements);
        //Log.d(TAG, "buildDisplayValueString::displayString = " + displayString);
        return displayString;
    }

}
