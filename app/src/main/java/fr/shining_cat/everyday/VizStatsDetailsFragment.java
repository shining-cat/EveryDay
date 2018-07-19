package fr.shining_cat.everyday;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.widgets.ChartDisplay;


public class VizStatsDetailsFragment extends Fragment
                                implements ChartDisplay.OnChartDisplayListener{


    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    protected List<SessionRecord> mAllSessions;
    protected List<List<SessionRecord>> mAllSessionsArranged;

    protected ChartDisplay mSessionsDurationChartViewHolder;
    protected ChartDisplay mSessionsNumberChartViewHolder;
    protected ChartDisplay mPausesCountChartViewHolder;
    protected ChartDisplay mPausesPerSessionChartViewHolder;
    protected ChartDisplay mStartingStoppingStreakSessionsCountChartViewHolder;
    protected ChartDisplay mUserStateAtSessionStartChartViewHolder;
    protected ChartDisplay mUserStateAtSessionEndChartViewHolder;
    protected ChartDisplay mUserStateDiffChartViewHolder;

    protected int mOldActiveValueIndex;

    public VizStatsDetailsFragment() {
        // Required empty public constructor
    }


    public static VizStatsDetailsFragment newInstance() {
        VizStatsDetailsFragment fragment = new VizStatsDetailsFragment();
        return fragment;
    }

    public void setAllSessionsList(List<SessionRecord> allSessions) {
        mAllSessions = allSessions;
        //Log.d(TAG, "setAllSessionsList:: total number of sessions = " + mAllSessions.size());
        mAllSessionsArranged = arrangeSessionsOnDesiredFrame();
        if(mSessionsDurationChartViewHolder != null){
            setSessionsDurationChart();
        }
        if(mSessionsNumberChartViewHolder != null){
            setSessionsNumberChart();
        }
        if(mPausesCountChartViewHolder != null){
            setPausesCountChart();
        }
        if(mPausesPerSessionChartViewHolder != null){
            setPausesPerSessionChart();
        }
        if(mStartingStoppingStreakSessionsCountChartViewHolder != null){
            setStreaksStartStopSessionChart();
        }
        if(mUserStateAtSessionStartChartViewHolder != null){
            setUserStateAtSessionStartChart();
        }
        if(mUserStateAtSessionEndChartViewHolder != null){
            setUserStateAtSessionEndChart();
        }
        if(mUserStateDiffChartViewHolder != null){
            setUserStateDiffChart();
        }
    }

    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        Log.e(TAG, "arrangeSessionsOnDesiredFrame::SHOULD BE OVERRIDDEN!!");
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView::SHOULD BE OVERRIDDEN!!");
        return null;
    }

    protected void setChartDisplayComponents(View rootView){
        //SESSIONS DURATION
        mSessionsDurationChartViewHolder = rootView.findViewById(R.id.sessions_duration_columnChartView_holder);
        //SESSIONS NUMBER
        mSessionsNumberChartViewHolder = rootView.findViewById(R.id.sessions_number_columnChartView_holder);
        //PAUSES NUMBER
        mPausesCountChartViewHolder = rootView.findViewById(R.id.pauses_number_columnChartView_holder);
        //PAUSES PER SESSION
        mPausesPerSessionChartViewHolder = rootView.findViewById(R.id.pauses_per_session_columnChartView_holder);
        //STARTING AND STOPPING STREAK SESSIONS COUNT
        mStartingStoppingStreakSessionsCountChartViewHolder = rootView.findViewById(R.id.start_stop_streak_sessions_count_columnChartView_holder);
        //USER STATE ON SESSION START
        mUserStateAtSessionStartChartViewHolder = rootView.findViewById(R.id.user_state_at_session_start_columnChartView_holder);
        //USER STATE ON SESSION END
        mUserStateAtSessionEndChartViewHolder = rootView.findViewById(R.id.user_state_at_session_end_columnChartView_holder);
        //USER STATE DIFF
        mUserStateDiffChartViewHolder = rootView.findViewById(R.id.user_state_diff_columnChartView_holder);
    }

    protected void setCharts(){
        if (mAllSessionsArranged != null) {
            setSessionsDurationChart();
            setSessionsNumberChart();
            setPausesCountChart();
            setPausesPerSessionChart();
            setStreaksStartStopSessionChart();
            setUserStateAtSessionStartChart();
            setUserStateAtSessionEndChart();
            setUserStateDiffChart();
        }
    }
////////////////////////////////////////
//ChartDisplay callbacks
    @Override
    public void onSelectedValue(ChartDisplay chartDisplay, int valueIndex) {
        synchronizeIndicationsDisplay(valueIndex);
    }

    @Override
    public void onUnselectedValue(ChartDisplay chartDisplay) {
        synchronizeUnlightening();
    }

    @Override
    public void onRequestPermissionApi24(String[] whichPermission, int permissionRequestCode) {
        ActivityCompat.requestPermissions(getActivity(), whichPermission, permissionRequestCode);
    }

    @Override
    public void onShareChartBitmap(ChartDisplay chartDisplay, Intent shareChooserIntent) {
        startActivity(shareChooserIntent);
    }

    @Override
    public void onHelpButtonPressed(ChartDisplay chartDisplay, String helpMessage) {
        Log.d(TAG, "onHelpButtonPressed::chartDisplay = " + chartDisplay);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.chart_help_dialog_title_description));
        builder.setMessage(helpMessage);
        builder.setCancelable(true);
        builder.setNeutralButton(getString(R.string.generic_string_CLOSE), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

////////////////////////////////////////
//synchronizing highlight
    private void synchronizeIndicationsDisplay(int indexInDataSet) {
        //Log.d(TAG, "synchronizeIndicationsDisplay:: indexInDataSet = " + indexInDataSet + " / mOldActiveValueIndex = " + mOldActiveValueIndex);
        if(indexInDataSet == mOldActiveValueIndex){
            synchronizeUnlightening();
        }else {
            mOldActiveValueIndex = indexInDataSet;
            //every chart here is built on the same 24-hours array so we can target entries by their respective dataset index
            mSessionsDurationChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mSessionsNumberChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mPausesCountChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mPausesPerSessionChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mStartingStoppingStreakSessionsCountChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateAtSessionStartChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateAtSessionEndChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateDiffChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
        }
    }


    private void synchronizeUnlightening() {
        Log.d(TAG, "synchronizeUnlightening");
        mOldActiveValueIndex = -1;
        //
        mSessionsDurationChartViewHolder.hideDataNoneSelected();
        mSessionsNumberChartViewHolder.hideDataNoneSelected();
        mPausesCountChartViewHolder.hideDataNoneSelected();
        mPausesPerSessionChartViewHolder.hideDataNoneSelected();
        mStartingStoppingStreakSessionsCountChartViewHolder.hideDataNoneSelected();
        mUserStateAtSessionStartChartViewHolder.hideDataNoneSelected();
        mUserStateAtSessionEndChartViewHolder.hideDataNoneSelected();
        mUserStateDiffChartViewHolder.hideDataNoneSelected();
    }

////////////////////////////////////////
//sessions duration graph
        protected void setSessionsDurationChart() {
            Log.e(TAG, "setSessionsDurationChart::SHOULD BE OVERRIDDEN!!");
        }


////////////////////////////////////////
//sessions number graph
    protected void setSessionsNumberChart() {
            Log.e(TAG, "setSessionsNumberChart::SHOULD BE OVERRIDDEN!!");
        }

////////////////////////////////////////
//Pauses count graph
    protected void setPausesCountChart() {
            Log.e(TAG, "setPausesCountChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//Pauses per session graph
    protected void setPausesPerSessionChart() {
        Log.e(TAG, "setPausesPerSessionChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    protected void setStreaksStartStopSessionChart() {
        Log.e(TAG, "setStreaksStartStopSessionChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session start graph
    protected void setUserStateAtSessionStartChart() {
        Log.e(TAG, "setUserStateAtSessionStartChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session end graph
    protected void setUserStateAtSessionEndChart() {
        Log.e(TAG, "setUserStateAtSessionEndChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session end graph
    protected void setUserStateDiffChart() {
        Log.e(TAG, "setUserStateDiffChart::SHOULD BE OVERRIDDEN!!");
    }

}