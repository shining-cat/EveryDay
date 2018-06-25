package fr.shining_cat.meditappli.widgets;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import fr.shining_cat.meditappli.R;


public class SearchBarWidget extends ConstraintLayout {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();


    private EditText mSearchEditText;
    private ImageView mClearBtn;
    private ImageView mSearchBtn;
    private Context mContext;
    private boolean mIsActive;
    private SearchWidgetListener mListener;


    public SearchBarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setInactive();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mInflater != null) {
            View root = mInflater.inflate(R.layout.search_bar_widget, this, true);
            mSearchEditText = root.findViewById(R.id.searchbar_widget_edittext);
            mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        launchSearch();
                        handled = true;
                    }
                    return handled;
                }
            });
            mClearBtn = root.findViewById(R.id.searchbar_widget_clear_btn);
            mClearBtn.setOnClickListener(mOnClearClickListener);
            mSearchBtn = root.findViewById(R.id.searchbar_widget_search_btn);
            mSearchBtn.setOnClickListener(mOnSearchClickListener);
        }else{
            Log.e(TAG, "SessionsListFilterToggleButton::mInflater == NULL!!");
        }
    }


    public void setListener(SearchWidgetListener listener) {
        mListener = listener;
    }

    public void setActive(){
        if(mListener!=null){
            mListener.onSearchWidgetActivation();
        }else{
            Log.e(TAG, "launchSearch:: NOBODY LISTENS TO ME!!");
        }
        mIsActive = true;
        if(mSearchEditText != null){
            mSearchEditText.setAlpha(1f);
            mSearchEditText.setEnabled(true);
            mSearchEditText.setVisibility(VISIBLE);
            mSearchEditText.requestFocus();
        }
        if(mClearBtn != null) mClearBtn.setAlpha(1f);
        if(mSearchBtn != null){
            mSearchBtn.setAlpha(1f);
        }
        showKeyboard();
    }

    public void setInactive(){
        mIsActive = false;
        if(mSearchEditText != null){
            mSearchEditText.setAlpha(.5f);
            mSearchEditText.setText(null);
            mSearchEditText.clearFocus();
            mSearchEditText.setVisibility(GONE);
            hideKeyboard();
        }
        if(mClearBtn != null){
            mClearBtn.setAlpha(.5f);
            mClearBtn.setVisibility(GONE);
        }
        if(mSearchBtn != null){
            mSearchBtn.setAlpha(.5f);
            mSearchBtn.setVisibility(VISIBLE);
        }

    }

    private final OnClickListener mOnClearClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mIsActive){
                resetSearch();
            }
        }
    };
    private final OnClickListener mOnSearchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mIsActive){
                launchSearch();
            }else{
                setActive();
            }
        }
    };

    private void launchSearch(){
        String searchRequest = mSearchEditText.getText().toString();
        if(!searchRequest.isEmpty()) {
            mClearBtn.setVisibility(VISIBLE);
            mSearchBtn.setVisibility(GONE);
            mSearchEditText.clearFocus();
            //mSearchEditText.setEnabled(false);
            hideKeyboard();
            if (mListener != null) {
                mListener.onSearchWidgetLaunchSearch(searchRequest);
            } else {
                Log.e(TAG, "launchSearch:: NOBODY LISTENS TO ME!!");
            }
        }
    }

    private void resetSearch(){
        mClearBtn.setVisibility(GONE);
        mSearchBtn.setVisibility(VISIBLE);
        mSearchEditText.setEnabled(true);
        mSearchEditText.setText(null);
        mSearchEditText.requestFocus();
        showKeyboard();
        if(mListener != null){
            mListener.onSearchWidgetResetSearch();
        }else{
            Log.e(TAG, "launchSearch:: NOBODY LISTENS TO ME!!");
        }
    }

////////////////////////////////////////
//show / hide soft keyboard

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
////////////////////////////////////////
//INTERFACE
    public interface SearchWidgetListener{
        void onSearchWidgetActivation();
        void onSearchWidgetLaunchSearch(String searchRequest);
        void onSearchWidgetResetSearch();
    }
}
