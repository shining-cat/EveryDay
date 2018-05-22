package fr.shining_cat.meditappli;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

public class MoodRecorderViewGroup extends ConstraintLayout {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    //initializing values out of normal range to be able to detect if one has been preset or not
    private int mBodyValue = -1000;
    private int mThoughtsValue = -1000;
    private int mFeelingsValue = -1000;
    private int mGlobalValue = -1000;

    private SeekBar mSeekBarBody;
    private SeekBar mSeekBarThoughts;
    private SeekBar mSeekBarFeelings;
    private SeekBar mSeekBarGlobal;

//WARNING : values are adapted for symmetry around the MIDDLE of the seekbar : seekbar goes from 0 to 10 and default to 5
// but when recorded we do (value-5)*2

////////////////////////////////////////
//View extension containing and managing value selectors (currently as Seekbars)
//for MoodRecord  bodyValue, thoughtsValue, feelingsValue, and globalValue values

    public MoodRecorderViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = null;
        if (mInflater != null) {
            root = mInflater.inflate(R.layout.mood_recorder, this, true);
        }else{
            Log.e(TAG, "MoodRecorder::mInflater == NULL!!");
        }
        Log.d(TAG, "MoodRecorder");
        //
        mSeekBarBody = root.findViewById(R.id.seekBarBody);
        if(mBodyValue==-1000){
            mBodyValue = mSeekBarBody.getProgress();
        }else{
            mSeekBarBody.setProgress(mBodyValue);
        }
        mSeekBarBody.setOnSeekBarChangeListener(onSeekBarBodyChangeListener);
        //
        mSeekBarThoughts = root.findViewById(R.id.seekBarThoughts);
        if(mThoughtsValue==-1000){
            mThoughtsValue = mSeekBarThoughts.getProgress();
        }else{
            mSeekBarThoughts.setProgress(mThoughtsValue);
        }
        mSeekBarThoughts.setOnSeekBarChangeListener(onSeekBarThoughtsChangeListener);
        //
        mSeekBarFeelings = root.findViewById(R.id.seekBarFeelings);
        if(mFeelingsValue==-1000){
            mFeelingsValue = mSeekBarFeelings.getProgress();
        }else{
            mSeekBarFeelings.setProgress(mFeelingsValue);
        }
        mSeekBarFeelings.setOnSeekBarChangeListener(onSeekBarFeelingsChangeListener);
        //
        mSeekBarGlobal = root.findViewById(R.id.seekBarGlobal);
        if(mGlobalValue==-1000){
            mGlobalValue = mSeekBarGlobal.getProgress();
        }else{
            mSeekBarGlobal.setProgress(mGlobalValue);
        }
        mSeekBarGlobal.setOnSeekBarChangeListener(onSeekBarGlobalChangeListener);
    }

    public int getBodyValue() {return mBodyValue;}
    public int getThoughtsValue() {return mThoughtsValue;}
    public int getFeelingsValue() {return mFeelingsValue;}
    public int getGlobalValue() {return mGlobalValue;}

    public void setBodyValue(int mBodyValue) {
        this.mBodyValue = mBodyValue;
        if(mSeekBarBody!=null){
            mSeekBarBody.setProgress(mBodyValue);
        }else{
            Log.d(TAG, "setBodyValue seekbar null");
        }
    }

    public void setThoughtsValue(int mThoughtsValue) {
        this.mThoughtsValue = mThoughtsValue;
        if(mSeekBarThoughts!=null){
            mSeekBarThoughts.setProgress(mThoughtsValue);
        }else{
            Log.d(TAG, "setThoughtsValue seekbar null");
        }
    }

    public void setFeelingsValue(int mFeelingsValue) {
        this.mFeelingsValue = mFeelingsValue;
        if(mSeekBarFeelings!=null){
            mSeekBarFeelings.setProgress(mFeelingsValue);
        }else{
            Log.d(TAG, "setFeelingsValue seekbar null");
        }
    }

    public void setGlobalValue(int mGlobalValue) {
        this.mGlobalValue = mGlobalValue;
        if(mSeekBarGlobal!=null){
            mSeekBarGlobal.setProgress(mGlobalValue);
        }else{
            Log.d(TAG, "setGlobalValue seekbar null");
        }
    }

    SeekBar.OnSeekBarChangeListener onSeekBarBodyChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mBodyValue = seekBar.getProgress();
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarThoughtsChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mThoughtsValue = seekBar.getProgress();
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarFeelingsChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mFeelingsValue = seekBar.getProgress();
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarGlobalChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mGlobalValue = seekBar.getProgress();
        }
    };


}
