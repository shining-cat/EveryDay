package fr.shining_cat.meditappli;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

public class MoodRecorderViewGroup extends ConstraintLayout {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private SmileRating mSmileyRatingBody;
    private SmileRating mSmileyRatingThoughts;
    private SmileRating mSmileyRatingFeelings;
    private SmileRating mSmileyRatingGlobal;

    private int mBodyValue;
    private int mThoughtsValue;
    private int mFeelingsValue;
    private int mGlobalValue;

////////////////////////////////////////
//View extension containing and managing value selectors as smiley faces selectors
//for MoodRecord  bodyValue, thoughtsValue, feelingsValue, and globalValue values
//  VALUE 0 in DATABASE means NOT set, range of set values is from 1 to 5
//  BUT
//  VALUE -1 in SmileRating means NOT SET, and range of set values is from 0 to 4

    public MoodRecorderViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "MoodRecorder " +
                "\nBaseRating.NONE = " + BaseRating.NONE +
                "\nBaseRating.TERRIBLE = " + BaseRating.TERRIBLE +
                "\nBaseRating.BAD = " + BaseRating.BAD +
                "\nBaseRating.OK = " + BaseRating.OKAY +
                "\nBaseRating.GOOD = " + BaseRating.GOOD +
                "\nBaseRating.GREAT = " + BaseRating.GREAT);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mInflater != null) {
            View root = mInflater.inflate(R.layout.mood_recorder, this, true);
            mSmileyRatingBody = root.findViewById(R.id.smileyRatingBody);
            Log.d(TAG, "MoodRecorder mSmileyRatingBody uninitialized returns " + mSmileyRatingBody.getRating());
            mSmileyRatingBody.setNameForSmile(BaseRating.TERRIBLE, null);
            mSmileyRatingBody.setNameForSmile(BaseRating.BAD, null);
            mSmileyRatingBody.setNameForSmile(BaseRating.OKAY, null);
            mSmileyRatingBody.setNameForSmile(BaseRating.GOOD, null);
            mSmileyRatingBody.setNameForSmile(BaseRating.GREAT, null);
            if(mBodyValue == 0){ //TODO! VERIFIER les valeurs not set and co
                mBodyValue = mSmileyRatingBody.getRating(); //returns 0  if none is selected //TODO => useless if that is the case
            }else{
                mSmileyRatingBody.setSelectedSmile(mBodyValue -1);
            }
            mSmileyRatingBody.setOnRatingSelectedListener(onBodySmileyRatingSelectedListener);
            //
            mSmileyRatingThoughts = root.findViewById(R.id.smileyRatingThoughts);
            mSmileyRatingThoughts.setNameForSmile(BaseRating.TERRIBLE, null);
            mSmileyRatingThoughts.setNameForSmile(BaseRating.BAD, null);
            mSmileyRatingThoughts.setNameForSmile(BaseRating.OKAY, null);
            mSmileyRatingThoughts.setNameForSmile(BaseRating.GOOD, null);
            mSmileyRatingThoughts.setNameForSmile(BaseRating.GREAT, null);
            if(mThoughtsValue == 0){
                mThoughtsValue = mSmileyRatingThoughts.getRating(); //returns 0  if none is selected
            }else{
                mSmileyRatingThoughts.setSelectedSmile(mThoughtsValue -1);
            }
            mSmileyRatingThoughts.setOnRatingSelectedListener(onThoughtsSmileyRatingSelectedListener);
            //
            mSmileyRatingFeelings = root.findViewById(R.id.smileyRatingFeelings);
            mSmileyRatingFeelings.setNameForSmile(BaseRating.TERRIBLE, null);
            mSmileyRatingFeelings.setNameForSmile(BaseRating.BAD, null);
            mSmileyRatingFeelings.setNameForSmile(BaseRating.OKAY, null);
            mSmileyRatingFeelings.setNameForSmile(BaseRating.GOOD, null);
            mSmileyRatingFeelings.setNameForSmile(BaseRating.GREAT, null);
            if(mFeelingsValue == 0){
                mFeelingsValue = mSmileyRatingFeelings.getRating(); //returns 0  if none is selected
            }else{
                mSmileyRatingFeelings.setSelectedSmile(mFeelingsValue -1);
            }
            mSmileyRatingFeelings.setOnRatingSelectedListener(onFeelingsSmileyRatingSelectedListener);
            //
            mSmileyRatingGlobal = root.findViewById(R.id.smileyRatingGlobal);
            mSmileyRatingGlobal.setNameForSmile(BaseRating.TERRIBLE, null);
            mSmileyRatingGlobal.setNameForSmile(BaseRating.BAD, null);
            mSmileyRatingGlobal.setNameForSmile(BaseRating.OKAY, null);
            mSmileyRatingGlobal.setNameForSmile(BaseRating.GOOD, null);
            mSmileyRatingGlobal.setNameForSmile(BaseRating.GREAT, null);
            if(mGlobalValue == 0){
                mGlobalValue = mSmileyRatingGlobal.getRating(); //returns 0  if none is selected
            }else{
                mSmileyRatingGlobal.setSelectedSmile(mGlobalValue -1);
            }
            mSmileyRatingGlobal.setOnRatingSelectedListener(onGlobalSmileyRatingSelectedListener);
        }else{
            Log.e(TAG, "MoodRecorder::mInflater == NULL!!");
        }
    }

    public void setBodyValue(int mBodyValue) {
        Log.d(TAG, "setBodyValue mBodyValue = " + mBodyValue);
        this.mBodyValue = mBodyValue;
        if(mSmileyRatingBody!=null){
            mSmileyRatingBody.setSelectedSmile(mBodyValue -1);
        }else{
            Log.d(TAG, "setBodyValue SmileyRating null");
        }
    }

    public void setThoughtsValue(int mThoughtsValue) {
        Log.d(TAG, "setBodyValue mThoughtsValue = " + mThoughtsValue);
        this.mThoughtsValue = mThoughtsValue;
        if(mSmileyRatingThoughts!=null){
            mSmileyRatingThoughts.setSelectedSmile(mThoughtsValue -1);
        }else{
            Log.d(TAG, "setThoughtsValue SmileyRating null");
        }
    }

    public void setFeelingsValue(int mFeelingsValue) {
        Log.d(TAG, "setBodyValue mFeelingsValue = " + mFeelingsValue);
        this.mFeelingsValue = mFeelingsValue;
        if(mSmileyRatingFeelings!=null){
            mSmileyRatingFeelings.setSelectedSmile(mFeelingsValue -1);
        }else{
            Log.d(TAG, "setFeelingsValue SmileyRating null");
        }
    }

    public void setGlobalValue(int mGlobalValue) {
        Log.d(TAG, "setBodyValue mGlobalValue = " + mGlobalValue);
        this.mGlobalValue = mGlobalValue;
        if(mSmileyRatingGlobal!=null){
            mSmileyRatingGlobal.setSelectedSmile(mGlobalValue -1);
        }else{
            Log.d(TAG, "setGlobalValue SmileyRating null");
        }
    }

    private SmileRating.OnRatingSelectedListener onBodySmileyRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int level, boolean reselected) {
            Log.d(TAG, "onBodySmileyRatingSelectedListener level = " + level);
            mBodyValue = level;
        }
    };
    private SmileRating.OnRatingSelectedListener onThoughtsSmileyRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int level, boolean reselected) {
            mThoughtsValue = level;
        }
    };
    private SmileRating.OnRatingSelectedListener onFeelingsSmileyRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int level, boolean reselected) {
            mFeelingsValue = level;
        }
    };
    private SmileRating.OnRatingSelectedListener onGlobalSmileyRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int level, boolean reselected) {
            mGlobalValue = level;
        }
    };


    public int getBodyValue() {return mBodyValue;}
    public int getThoughtsValue() {return mThoughtsValue;}
    public int getFeelingsValue() {return mFeelingsValue;}
    public int getGlobalValue() {return mGlobalValue;}

}
