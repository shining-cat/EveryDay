package fr.shining_cat.everyday.widgets;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.R;
import fr.shining_cat.everyday.utils.TimeOperations;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ChartDisplay extends ConstraintLayout {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String DISPLAY_ROUNDING_INT = "display clicked value as an integer";
    public static final String DISPLAY_ROUNDING_FLOAT = "display clicked value as a float with two decimal points";
    public static final String DISPLAY_ROUNDING_FOMATTED_TIME = "display clicked value as a formatted duration";


    //
    private static final float MULTI_BAR_CHART_GROUPSPACE = 0.1f;
    private static final float MULTI_BAR_CHART_BARSPACE = 0f;
    //next is to center the bars lots on value instead of having them left-align on value, so double bar chart will be more coherent with single-bar charts
    private static final float MULTI_BAR_CHART_OFFSET = 0.5f;
    //
    private static final float SINGLE_BAR_CHART_BARWIDTH = 0.9f; // will let 0.1 space between bars because they are positioned at every hour (so with total of 1.0 for each bar)

    private static final int FLASH_SCREEN_DURATION = 100;

    private static final String EXPORT_BASE_NAME    = "EveryDay-chart.jpg";
    private static final String EXPORT_FOLDER       = "EveryDay-Charts";
    private static final String EXPORT_DESCRIPTION  = "Export chart from EveryDay app";
    private static final String EXPORT_MIMETYPE     = "image/jpeg";
    private static final Bitmap.CompressFormat EXPORT_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int EXPORT_QUALITY = 50;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 789;


    private Context mContext;
    private OnChartDisplayListener mListener;

    private ArrayList<IBarDataSet> mBarDataSets;
    private String mValueDisplayBaseString;
    private String mRoundYvalues;
    private boolean mShowPlusOnPositive;
    private boolean mHasDatasBeenSet;

    protected BarChart mChartView;
    private ImageView mHelpBtn;
    private View mHelpBtnBckgnd;
    private View mFlash;
    private TextView mDisplayValueTxtVw;
    private boolean mShowHelpBtn;
    private String mHelpMessage;

    public ChartDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHasDatasBeenSet = false;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mInflater != null) {
            View root = mInflater.inflate(R.layout.chart_display, this, true);
            mChartView = root.findViewById(R.id.chart_display_columnChartView);
            mFlash = root.findViewById(R.id.chart_display_flash);
            mDisplayValueTxtVw = root.findViewById(R.id.chart_display_value_txtvw);
            mHelpBtn = root.findViewById(R.id.chart_help_btn);
            mHelpBtnBckgnd = root.findViewById(R.id.chart_help_btn_background);
        }else{
            Log.e(TAG, "ChartDisplay::mInflater == NULL!!");
        }

    }
    public void setListener(OnChartDisplayListener listener) {
        mListener = listener;
    }

    public void setChartData(ArrayList<Integer> xAxisValues,
                             ArrayList<String> legendsList,
                             ArrayList<Integer> colorsList,
                             ArrayList<List<Float>> yAxisValuesLists,
                             String valueDisplayBaseString,
                             String roundYvalues,
                             boolean showPlusOnPositive,
                             boolean displayValuesAsIntOnChart,
                             boolean showHelpBtn,
                             String helpMessage){
        //Log.d(TAG, "setChartData");
        mValueDisplayBaseString = valueDisplayBaseString;
        mRoundYvalues = roundYvalues;
        mShowPlusOnPositive = showPlusOnPositive;
        mShowHelpBtn = showHelpBtn;
        if(mShowHelpBtn){
            mHelpBtn.setVisibility(VISIBLE);
            mHelpBtnBckgnd.setVisibility(VISIBLE);
            mHelpBtn.setOnClickListener(onHelpBtnClicked);
        }
        mHelpMessage = helpMessage;
        mBarDataSets = new ArrayList<>();
        int valuesListIndex = 0;
        for(List<Float> yValuesList : yAxisValuesLists){
            List<BarEntry> entries =  new ArrayList<>();
            for(int xAxisIndex = 0; xAxisIndex < xAxisValues.size(); xAxisIndex ++){
                //Log.d(TAG, "setChartData::xAxisIndex = " + xAxisIndex + " / xAxisValues.get(xAxisIndex) = " + xAxisValues.get(xAxisIndex) + " / yValuesList.get(xAxisIndex) = " + yValuesList.get(xAxisIndex));
                entries.add(new BarEntry((float) xAxisValues.get(xAxisIndex), yValuesList.get(xAxisIndex)));
            }
            BarDataSet barDataSet = new BarDataSet(entries, legendsList.get(valuesListIndex));
            barDataSet.setDrawValues(false);
            barDataSet.setColor(colorsList.get(valuesListIndex));
            mBarDataSets.add(barDataSet);
            valuesListIndex ++;
        }
        BarData data = new BarData(mBarDataSets);
        data.setBarWidth(SINGLE_BAR_CHART_BARWIDTH / mBarDataSets.size());
        if(displayValuesAsIntOnChart){
            data.setValueFormatter(new IntValueFormatter());
        }
        mChartView.setData(data);
        if(mBarDataSets.size() > 1){
            mChartView.groupBars(-MULTI_BAR_CHART_OFFSET, MULTI_BAR_CHART_GROUPSPACE, MULTI_BAR_CHART_BARSPACE);
        }
        mChartView.setOnChartValueSelectedListener(onChartValueSelectedListener);
        mChartView.setOnChartGestureListener(onChartGestureListener);
        initChartOptions();
        mChartView.invalidate();
        mHasDatasBeenSet = true;
    }

////////////////////////////////////////
//helper method to init BarChart visual options
    protected void initChartOptions() {
        mChartView.setFitBars(false); // if set to true , space added on right and left opf chart will be = to half of barwidth which will be differetn between single, double and quad barcharts... We really want the different charts to be aligned so hoping that they will always fit in the set widht
        mChartView.setDoubleTapToZoomEnabled(false);
        mChartView.getDescription().setEnabled(false);//we don't use this description field
        //mChartView.getAxisLeft().setDrawGridLines(false);//remove horizontal background grid // not needed if bith vertical axises are disabled
        mChartView.getAxisLeft().setDrawZeroLine(true);//draw bottom line on y=zero // not effective if both vertical axises are disabled
        //mChartView.getAxisLeft().setAxisMinimum(-1f);// removed because since size of a unit is dependant on graph auto-scaling, it results in variable bottom margin, which looks weird
        //mChartView.getAxisLeft().setEnabled(false);//no left axis
        mChartView.getAxisRight().setEnabled(false);//no right axis
        mChartView.getXAxis().setEnabled(false);//no x axis
        mChartView.setDrawBorders(true);//border around graph
        //
        mChartView.getLegend().setWordWrapEnabled(true);
        //TODO or not TODO: trouver comment obtenir un retour ligne pour chaque item de la légende plutôt qu'un simple wordwrap
    }

////////////////////////////////////////
//Formatter to have int instead of floats displayed in graphs
    class IntValueFormatter implements IValueFormatter {
        public IntValueFormatter() {}

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return "" + ((int) value);
        }
    }

////////////////////////////////////////
//Help button
    private OnClickListener onHelpBtnClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            dispatchOnHelpRequest();
        }
    };
    private void dispatchOnHelpRequest(){
        if(mListener != null){
            mListener.onHelpButtonPressed(this, mHelpMessage);
        }
    }

////////////////////////////////////////
//INTERACTIONS with graph

    private OnChartValueSelectedListener onChartValueSelectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            //Log.d(TAG, "onChartValueSelectedListener::onValueSelected");
            int entryIndex = -1;
            for(IBarDataSet barDataSet : mBarDataSets){
                entryIndex = barDataSet.getEntryIndex((BarEntry) e);
                if(entryIndex != -1) break;
            }
            dispatchOnValueSelectedRequest(entryIndex);

        }

        @Override
        public void onNothingSelected() {
            //Log.d(TAG, "onNothingSelected");
            dispatchOnValueUnselectedRequest();
        }
    };

    private void dispatchOnValueSelectedRequest(int entryIndex){
        if(mListener != null) {
            mListener.onSelectedValue(this, entryIndex);
        }else{
            Log.e(TAG, "dispatchOnValueSelectedRequest::no listener to request");
        }
    }
    private void dispatchOnValueUnselectedRequest(){
        if(mListener != null) {
            mListener.onUnselectedValue(this);
        }else{
            Log.e(TAG, "dispatchOnValueUnselectedRequest::no listener to request!");
        }
    }

    private OnChartGestureListener onChartGestureListener = new OnChartGestureListener() {
        @Override
        public void onChartLongPressed(MotionEvent me) {
            prepareSaveAndShareChart();//only way to get a long press interaction while keeping normal graph interactions is to implement it on the graph itself, even if it's the only interaction we need there
        }
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
        @Override
        public void onChartDoubleTapped(MotionEvent me) {}
        @Override
        public void onChartSingleTapped(MotionEvent me) {}
        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {}
    };

////////////////////////////////////////
//Displaying selected value

    private void showChartTextView(String message) {
        mDisplayValueTxtVw.setText(message);
        mDisplayValueTxtVw.setVisibility(View.VISIBLE);
    }

    public void hideDataNoneSelected() {
        //Log.d(TAG, "hideDataNoneSelected");
        if(mHasDatasBeenSet) {
            //TODO or not TODO : BUG in library here : programatically killing highlights does not reset the bar state to unselected, so next clic on same bar will trigger onNothingSelected instead of onValueSelectd
            mChartView.highlightValues(null);
            mDisplayValueTxtVw.setText("");
            mDisplayValueTxtVw.setVisibility(View.INVISIBLE);
        }else{
            Log.d(TAG, "hideDataNoneSelected:: NO DAT SET FOR THIS CHART!");
            //this may be voluntarily done
        }
    }

    public void displayDataForSelectedIndex(int indexInDataSet) {
        //Log.d(TAG, "displayDataForSelected::indexInDataSet = " + indexInDataSet);
        if(mHasDatasBeenSet) {
            ArrayList<String> displayStringElements = new ArrayList<>();
            Highlight[] highlights = new Highlight[mBarDataSets.size()];
            float xPosToDisplay = 0;
            for (int index = 0; index < mBarDataSets.size(); index++) {
                BarEntry entry = mBarDataSets.get(index).getEntryForIndex(indexInDataSet);
                Float yValue = entry.getY();
                Float xPos = entry.getX();
                if (index == 0)
                    xPosToDisplay = xPos; //only store once for the construction of the display string later
                //
                Highlight highlight = new Highlight(xPos, Float.NaN, index); // prepare highlight object
                highlights[index] = highlight;
                //
                String yValueString = "";
                if (yValue >= 0 && mShowPlusOnPositive) {
                    yValueString += "+";
                }
                switch (mRoundYvalues) {
                    case DISPLAY_ROUNDING_INT:
                        yValueString += String.valueOf(Math.round(yValue));
                        break;
                    case DISPLAY_ROUNDING_FOMATTED_TIME:
                        long durationInMs = (long) (yValue * 60000); //re-convert to ms
                        String formattedDuration = TimeOperations.convertMillisecondsToHoursMinutesAndSecondsString(
                                durationInMs,
                                mContext.getString(R.string.generic_string_SHORT_HOURS),
                                mContext.getString(R.string.generic_string_SHORT_MINUTES),
                                mContext.getString(R.string.generic_string_SHORT_SECONDS),
                                false);
                        if (formattedDuration == TimeOperations.TIME_IS_ZERO) {
                            formattedDuration = mContext.getString(R.string.zero);
                        }
                        yValueString += formattedDuration;
                        break;
                    case DISPLAY_ROUNDING_FLOAT:
                    default:
                        yValueString += String.valueOf((float) Math.round(yValue * 100) / 100);
                }
                displayStringElements.add(yValueString);
            }
            //Log.d(TAG, "displayDataForSelected::highlights size = " + highlights.length);
            mChartView.highlightValues(highlights);
            //
            showChartTextView(buildDisplayValueString(mValueDisplayBaseString, displayStringElements, xPosToDisplay));
        }else{
            Log.d(TAG, "hideDataNoneSelected:: NO DAT SET FOR THIS CHART!");
            //this may be voluntarily done
        }
    }

    protected String buildDisplayValueString(String baseString, ArrayList<String> elements, float xPosition){
        Log.e(TAG, "displayDataForSelectedIndex::SHOULD BE OVERRIDDEN!!");
        return "";
    }


////////////////////////////////////////
// EXPORTING a chart to BITMAP on EXTERNAL STORAGE

    private Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private boolean weHavePermissionToWriteOnExternalStorage(){
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private boolean weHavePermissionToReadExternalStorage(){
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // process is interrumpted, user has granted authorization he will have to re-click on chart for an export
                } else {
                    //nothing to do here, we do not store that user has denied authorisation, so he will be asked again if he tries to export a chart again, rather than counting on him to go in the device's settings to understand why the functionality is disabled
                }
                return;
            }
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }else{
            Log.e(TAG, "isExternalStorageWritable:: NO !!");
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }else{
            Log.e(TAG, "isExternalStorageReadable:: NO !!");
        }
        return false;
    }

    private File getPublicAlbumStorageDir(String albumName) {
        if(!weHavePermissionToWriteOnExternalStorage()){ // in API > 24 we need to ask for permission at runtime even when they're added in the manifest!
            Log.e(TAG, "getPublicAlbumStorageDir::no permission to external storage!!");
            if (Build.VERSION.SDK_INT >= 24) {
                if(mListener != null) {
                    mListener.onRequestPermissionApi24(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }else{
                    Log.e(TAG, "saveToGallery::no listener to request permission!!");
                }
            }
            return null; // process is interrumpted, we wait for user to grant or deny authorization, then ask for screenshot again by re-clicking the graph
        }
        // Get the directory for the user's public pictures directory.
        File picturesFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picturesFolderPath.mkdir();
        if(picturesFolderPath.exists()){
            File mySubFolder = new File(picturesFolderPath, albumName);
            mySubFolder.mkdir();
            if(mySubFolder.exists()) {
                return mySubFolder;
            }else{
                Log.e(TAG, "getPublicAlbumStorageDir::my SUBFOLDER DOES NOT EXIST!");
                return null;
            }
        }else{
            Log.e(TAG, "getPublicAlbumStorageDir::NO PICTURES FOLDER!");
            return null;
        }
    }

    private Uri saveToGallery(Bitmap bitmap, String fileName, String subFolderName, String fileDescription, Bitmap.CompressFormat format, int quality) {
        // restrain quality
        if (quality < 0 || quality > 100) quality = 50;
        if(!isExternalStorageWritable() || !isExternalStorageReadable()){
            Log.e(TAG, "saveToGallery:: EXTERNAL STORAGE NOT AVAILABLE");
            return null;
        }
        File folder = getPublicAlbumStorageDir(subFolderName);
        if(folder == null){
            Log.e(TAG, "saveToGallery::could get public album storage dir and create subfolder!");
            return null;
        }
        File file = new File(folder, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "saveToGallery::could not create new file!");
            e.printStackTrace();
            return null;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(EXPORT_FORMAT, quality, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "saveToGallery::could not write bitmap: " + e);
            e.printStackTrace();
            return null;
        }

        ContentValues values = new ContentValues(8);
        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, EXPORT_MIMETYPE);
        values.put(MediaStore.Images.Media.DESCRIPTION, fileDescription);
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, file.getPath());
        values.put(MediaStore.Images.Media.SIZE, file.length());

        return getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void prepareSaveAndShareChart(){
        CountDownTimer flashCountDown = new CountDownTimer(FLASH_SCREEN_DURATION, 100) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                saveAndShareChart();
            }
        }.start();
        //Log.d(TAG, "prepareSaveAndShareChart::show flash");
        mFlash.setVisibility(VISIBLE);
    }

    private void saveAndShareChart(){
        //Log.d(TAG, "saveAndShareChart::hide flash");
        mFlash.setVisibility(GONE);
        //hide temporarily helpBtn
        mHelpBtn.setVisibility(GONE);
        mHelpBtnBckgnd.setVisibility(GONE);
        //
        Bitmap chartBitmap = getBitmapFromView(this);
        //show helpBtn again if necessary
        if(mShowHelpBtn){
            mHelpBtn.setVisibility(VISIBLE);
            mHelpBtnBckgnd.setVisibility(VISIBLE);
        }
        if(chartBitmap != null) {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
            String exportFileName = sdf.format(System.currentTimeMillis()) + "_" + EXPORT_BASE_NAME;
            Uri savedChartBitmapUri = saveToGallery(chartBitmap, exportFileName, EXPORT_FOLDER, EXPORT_DESCRIPTION, EXPORT_FORMAT, EXPORT_QUALITY);
            //Log.d(TAG, "saveAndShareChart::savedChartBitmapUri = " + savedChartBitmapUri);
            if(savedChartBitmapUri != null){
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, savedChartBitmapUri);
                if(mListener != null) {
                    mListener.onShareChartBitmap(this, Intent.createChooser(shareIntent, "Share image using"));
                }else{
                    Log.e(TAG, "saveAndShareChart::NO LISTENER TO CREATE share chooser!!");
                }
            }else{
                Log.e(TAG, "saveAndShareChart::BITMAP URI is NULL!!");
            }
        }else{
            Log.e(TAG, "saveAndShareChart::BITMAP IS NULL!!");
        }
    }

////////////////////////////////////////
//INTERFACE
    public interface OnChartDisplayListener {
        void onSelectedValue(ChartDisplay chartDisplay, int valueIndex);
        void onUnselectedValue(ChartDisplay chartDisplay);
        void onRequestPermissionApi24(String[] whichPermission, int permissionRequestCode); // call : ActivityCompat.requestPermissions(getActivity(), whichPermission, permissionRequestCode) in Activity listener
        void onShareChartBitmap(ChartDisplay chartDisplay, Intent shareChooserIntent); // call startActivity(shareChooserIntent) in Activity listener
        void onHelpButtonPressed(ChartDisplay chartDisplay, String helpMessage);
    }

}
