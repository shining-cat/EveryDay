package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.R;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.utils.TimeOperations;

public class ChartDisplay extends ConstraintLayout{


    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String DISPLAY_ROUNDING_INT             = "display clicked value as an integer";
    public static final String DISPLAY_ROUNDING_FLOAT           = "display clicked value as a float with two decimal points";
    public static final String DISPLAY_ROUNDING_FOMATTED_TIME   = "display clicked value as a formatted duration";
    //
    private static final float MULTI_BAR_CHART_GROUPSPACE   = 0.1f;
    private static final float MULTI_BAR_CHART_BARSPACE     = 0f;
    //next is to center the bars lots on value instead of having them left-align on value, so double bar chart will be more coherent with single-bar charts
    private static final float MULTI_BAR_CHART_OFFSET       = 0.5f;
    //
    private static final float SINGLE_BAR_CHART_BARWIDTH    = 0.9f; // will let 0.1 space between bars because they are positioned at every hour (so with total of 1.0 for each bar)

    private static final int FLASH_SCREEN_DURATION  = 100;

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
    private ImageView mShareBtn;
    private View mShareBtnBckgnd;
    private View mFlash;
    private TextView mDisplayValueTxtVw;
    private TextView mNoDataToShowTxtVw;
    private boolean mShowHelpBtn;
    private String mHelpMessage;

    public ChartDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHasDatasBeenSet = false;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mInflater != null) {
            View root = mInflater.inflate(R.layout.widget_chart_display, this, true);
            mChartView = root.findViewById(R.id.chart_display_columnChartView);
            mFlash = root.findViewById(R.id.chart_display_flash);
            mDisplayValueTxtVw = root.findViewById(R.id.chart_display_value_txtvw);
            mNoDataToShowTxtVw = root.findViewById(R.id.hide_me_no_data_txtvw);
            mHelpBtn = root.findViewById(R.id.chart_help_btn);
            mHelpBtnBckgnd = root.findViewById(R.id.chart_help_btn_background);
            mShareBtn = root.findViewById(R.id.chart_share_btn);
            mShareBtnBckgnd = root.findViewById(R.id.chart_share_btn_background);
        }else{
            Log.e(TAG, "ChartDisplay::mInflater == NULL!!");
        }

    }
    public void setListener(OnChartDisplayListener listener) {
        mListener = listener;
    }

    public void hideMeIHaveNoDataToShow(String noDataToShowMessage){
        mNoDataToShowTxtVw.setText(noDataToShowMessage);
        mNoDataToShowTxtVw.setVisibility(VISIBLE);
        mChartView.setVisibility(GONE);
        mDisplayValueTxtVw.setVisibility(GONE);
        mHelpBtn.setVisibility(GONE);
        mHelpBtnBckgnd.setVisibility(GONE);

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
        mNoDataToShowTxtVw.setVisibility(GONE);
        mChartView.setVisibility(VISIBLE);
        mHelpBtn.setVisibility(VISIBLE);
        mHelpBtnBckgnd.setVisibility(VISIBLE);
        //
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
        mShareBtn.setVisibility(VISIBLE);
        mShareBtnBckgnd.setVisibility(VISIBLE);
        mShareBtn.setOnClickListener(onShareBtnClicked);
        mBarDataSets = new ArrayList<>();
        int valuesListIndex = 0;
        for(List<Float> yValuesList : yAxisValuesLists){
            List<BarEntry> entries =  new ArrayList<>();
            for(int xAxisIndex = 0; xAxisIndex < xAxisValues.size(); xAxisIndex ++){
                float xValue = (float) xAxisValues.get(xAxisIndex);
                float yValue = (float) yValuesList.get(xAxisIndex);
                entries.add(new BarEntry(xValue, yValue));
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
        //mChartView.setOnChartGestureListener(onChartGestureListener);
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
        mChartView.setBackgroundColor(mContext.getResources().getColor(R.color.grey_n1));
        //
        mChartView.getLegend().setWordWrapEnabled(true);
        //
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryUltraLight, typedValue, true);
        mChartView.setBackgroundColor(typedValue.data);
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        mChartView.getLegend().setTextColor(typedValue.data);//we don't use this description field
        //TODO or not TODO: trouver comment obtenir un retour ligne pour chaque item de la légende plutôt qu'un simple wordwrap... pas possible avec les méthodes natives de la librairie
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
            mListener.onChartDisplayHelpButtonPressed(this, mHelpMessage);
        }else{
            Log.e(TAG, "dispatchOnHelpRequest::no listener to request");
        }
    }

////////////////////////////////////////
//Help button
    private OnClickListener onShareBtnClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            prepareSaveAndShareChart();
        }
    };

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
            mListener.onChartDisplaySelectedValue(this, entryIndex);
        }else{
            Log.e(TAG, "dispatchOnValueSelectedRequest::no listener to request");
        }
    }
    private void dispatchOnValueUnselectedRequest(){
        if(mListener != null) {
            mListener.onChartDisplayUnselectedValue(this);
        }else{
            Log.e(TAG, "dispatchOnValueUnselectedRequest::no listener to request!");
        }
    }

/*removed for share button so it is more like the rewards card, and more explicit
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
    };*/

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
//SAVE AND SHARE CHART
    private void prepareSaveAndShareChart(){
        CountDownTimer flashCountDown = new CountDownTimer(FLASH_SCREEN_DURATION, 100) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                saveAndShareChart();
            }
        }.start();
        //show "flash"
        mFlash.setVisibility(VISIBLE);
    }

    private void saveAndShareChart(){
        //hide "flash"
        mFlash.setVisibility(GONE);
        //hide helpBtn temporarily so it is not in screenshot
        mHelpBtn.setVisibility(GONE);
        mHelpBtnBckgnd.setVisibility(GONE);
        mShareBtn.setVisibility(GONE);
        mShareBtnBckgnd.setVisibility(GONE);
        //set Chart background color to white for a better view
        mChartView.setBackgroundColor(Color.WHITE);
        //
        Bitmap chartBitmap = MiscUtils.getBitmapFromView(this);
        //set Chart background color to normal
        mChartView.setBackgroundColor(mContext.getResources().getColor(R.color.grey_n1));
        //show helpBtn again if necessary
        if(mShowHelpBtn){
            mHelpBtn.setVisibility(VISIBLE);
            mHelpBtnBckgnd.setVisibility(VISIBLE);
        }
        mShareBtn.setVisibility(VISIBLE);
        mShareBtnBckgnd.setVisibility(VISIBLE);
        if(chartBitmap != null) {
            mListener.exportChartDisplayAsBitmapToFileAndShare(chartBitmap);
        }else{
            Log.e(TAG, "saveAndShareChart::BITMAP IS NULL!!");
        }
    }

////////////////////////////////////////
//INTERFACE
    public interface OnChartDisplayListener {
        void onChartDisplaySelectedValue(ChartDisplay chartDisplay, int valueIndex);
        void onChartDisplayUnselectedValue(ChartDisplay chartDisplay);
        void exportChartDisplayAsBitmapToFileAndShare(Bitmap chartBitmap);
        void onChartDisplayHelpButtonPressed(ChartDisplay chartDisplay, String helpMessage);
    }

}
