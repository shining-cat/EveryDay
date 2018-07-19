package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.R;

public class Mp3FilesChartDisplay extends ChartDisplay {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private Context mContext;

    private List<String> mMp3Filenames;

    public Mp3FilesChartDisplay(Context context, @Nullable AttributeSet attrs) {
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
        displayStringElements[elements.size()] = convertXposToMp3FileNameForDisplay(xPosition);
        displayString = String.format(baseString, (Object[]) displayStringElements);
        //Log.d(TAG, "buildDisplayValueString::displayString = " + displayString);
        return displayString;
    }

////////////////////////////////////////
//helper method to get the mp3 filename from the xPos, if not possible, returns the xpos as string
    private String convertXposToMp3FileNameForDisplay(float xPos) {
        int mp3FileNameIndex = Math.round(xPos);//need to round because multi-bars graphs will send offsetted x positions (by half of bar width)
        if(mMp3Filenames != null) {
            try {
                return mMp3Filenames.get(mp3FileNameIndex);
            }catch (IndexOutOfBoundsException iobe){
                Log.e("Mp3FilesChartDisplay", "convertXposToMp3FileNameForDisplay:: INDEX OUT OF BOUND, could not get mp3 file name from xPos = " + xPos);
                iobe.printStackTrace();
                return String.valueOf(xPos);
            }
        }
        return String.valueOf(xPos);
    }

    public void setMp3FileNamesLabel(List<String> mp3Filenames){
        mMp3Filenames = mp3Filenames;
    }

}
