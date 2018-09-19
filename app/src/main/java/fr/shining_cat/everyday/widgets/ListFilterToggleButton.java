package fr.shining_cat.everyday.widgets;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.shining_cat.everyday.R;


public class ListFilterToggleButton extends ConstraintLayout {

    public static final String ARROW_MODE = "toggle in ARROW mode";
    public static final String CHECK_MODE = "toggle in CHECK mode";
    public static final String OPTION_NONE = "toggle set to NO OPTION";
    //OPTION A is "UP" or "YES"
    public static final String OPTION_A = "toggle set to option A";
    public static final String OPTION_UP = "toggle set to option UP";
    public static final String OPTION_YES = "toggle set to option YES";
    //OPTION B is "DOWN" or "NO"
    public static final String OPTION_B = "toggle set to option B";
    public static final String OPTION_DOWN = "toggle set to option DOWN";
    public static final String OPTION_NO = "toggle set to option NO";


    private static final float ACTIVE_STATUS_ALPHA      = 1f;
    private static final float INACTIVE_STATUS_ALPHA    = .3f;

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private TextView  mLabel;
    private ImageView mArrowOptionA;
    private ImageView mArrowOptionB;
    private ImageView mCheckOptionA;
    private ImageView mCheckOptionB;

    private String mLabelString;
    private String mMode;
    private String mActiveOption;

    public ListFilterToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInactive();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mInflater != null) {
            View root = mInflater.inflate(R.layout.sessions_list_filter_toggle_btn, this, true);
            mLabel = root.findViewById(R.id.sessions_list_filter_toggle_label);
            updateLabel();
            mArrowOptionA = root.findViewById(R.id.sessions_list_filter_toggle_option_A_arrow);
            mArrowOptionB = root.findViewById(R.id.sessions_list_filter_toggle_option_B_arrow);
            mCheckOptionA = root.findViewById(R.id.sessions_list_filter_toggle_option_A_check);
            mCheckOptionB = root.findViewById(R.id.sessions_list_filter_toggle_option_B_check);
            updateMode();
        }else{
            Log.e(TAG, "SessionsListFilterToggleButton::mInflater == NULL!!");
        }
    }

    public void setLabel(String label){
        mLabelString = label;
        updateLabel();
    }

    private void updateLabel(){
        if(mLabel != null) mLabel.setText(mLabelString);
    }

    public void setModeCheckOrArrow(String mode){
        mMode = mode;
        updateMode();
        //Log.d(TAG, "setModeCheckOrArrow :: mode = " + mode);
    }

    private void updateMode(){
        //Log.d(TAG, "updateMode");
        if(mMode == null) mMode = ARROW_MODE;
        switch(mMode){
            case CHECK_MODE:
                if(mArrowOptionA != null) mArrowOptionA.setVisibility(GONE);
                if(mArrowOptionB != null) mArrowOptionB.setVisibility(GONE);
                if(mCheckOptionA != null) mCheckOptionA.setVisibility(VISIBLE);
                if(mCheckOptionB != null) mCheckOptionB.setVisibility(VISIBLE);
                break;
            case ARROW_MODE:
            default:
                if(mCheckOptionA != null) mCheckOptionA.setVisibility(GONE);
                if(mCheckOptionB != null) mCheckOptionB.setVisibility(GONE);
                if(mArrowOptionA != null) mArrowOptionA.setVisibility(VISIBLE);
                if(mArrowOptionB != null) mArrowOptionB.setVisibility(VISIBLE);
        }
    }

    public void setActive(String option){
        //Log.d(TAG, "setActive::option = " + option);
        if(mLabel != null) mLabel.setAlpha(ACTIVE_STATUS_ALPHA);
        switch(option){
            case OPTION_B:
            case OPTION_DOWN:
            case OPTION_NO:
                if(mArrowOptionA != null) mArrowOptionA.setAlpha(INACTIVE_STATUS_ALPHA);
                if(mCheckOptionA != null) mCheckOptionA.setAlpha(INACTIVE_STATUS_ALPHA);
                if(mArrowOptionB != null) mArrowOptionB.setAlpha(ACTIVE_STATUS_ALPHA);
                if(mCheckOptionB != null) mCheckOptionB.setAlpha(ACTIVE_STATUS_ALPHA);
                //
                mActiveOption = OPTION_B;
                break;
            case OPTION_A:
            case OPTION_UP:
            case OPTION_YES:
            default:
                if(mArrowOptionA != null) mArrowOptionA.setAlpha(ACTIVE_STATUS_ALPHA);
                if(mCheckOptionA != null) mCheckOptionA.setAlpha(ACTIVE_STATUS_ALPHA);
                if(mArrowOptionB != null) mArrowOptionB.setAlpha(INACTIVE_STATUS_ALPHA);
                if(mCheckOptionB != null) mCheckOptionB.setAlpha(INACTIVE_STATUS_ALPHA);
                //
                mActiveOption = OPTION_A;
                break;
        }
    }

    public void setInactive(){
        if(mLabel != null) mLabel.setAlpha(INACTIVE_STATUS_ALPHA);
        if(mArrowOptionA != null) mArrowOptionA.setAlpha(INACTIVE_STATUS_ALPHA);
        if(mCheckOptionA != null) mCheckOptionA.setAlpha(INACTIVE_STATUS_ALPHA);
        if(mArrowOptionB != null) mArrowOptionB.setAlpha(INACTIVE_STATUS_ALPHA);
        if(mCheckOptionB != null) mCheckOptionB.setAlpha(INACTIVE_STATUS_ALPHA);
        //
        mActiveOption = OPTION_NONE;
    }

    public String toggle(){
        switch(mActiveOption){
            case OPTION_B:
                setActive(OPTION_A);
                break;
            case OPTION_A:
                setActive(OPTION_B);
                break;
            case OPTION_NONE:
            default:
                //do nothing, toggle is inactive
                break;
        }
        switch(mMode) {
            case CHECK_MODE:
                return (mActiveOption.equals(OPTION_A)) ? OPTION_YES : OPTION_NO;
            case ARROW_MODE:
                return (mActiveOption.equals(OPTION_A)) ? OPTION_UP : OPTION_DOWN;
            default:
                return mActiveOption;
        }
    }
    public boolean isActive(){
        return !(mActiveOption.equals(OPTION_NONE));
    }
}
