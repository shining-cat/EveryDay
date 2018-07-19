package fr.shining_cat.everyday;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.analytics.Mp3Stats;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.widgets.ChartDisplay;
import fr.shining_cat.everyday.widgets.Mp3FilesChartDisplay;


public class VizStatsMp3Fragment extends VizStatsDetailsFragment{

    public static final String VIEW_STATS_MP3_FRAGMENT_TAG = "view_stats_mp3_Fragment-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();
    
    private String mNoMp3Label;
    private List<String> mMp3Filenames;

    public VizStatsMp3Fragment() {
        // Required empty public constructor
    }

    public static VizStatsMp3Fragment newInstance() {
        VizStatsMp3Fragment fragment = new VizStatsMp3Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viz_stats_mp3, container, false);
        super.setChartDisplayComponents(rootView);
        super.setCharts();
        return rootView;
    }

    public void setNoMp3LabelString(String noMp3Label){
        mNoMp3Label = noMp3Label;
    }

    @Override
    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        mMp3Filenames = GeneralStats.getListOfMp3FileNames(mAllSessions, mNoMp3Label);
        if(mMp3Filenames != null) {
            return Mp3Stats.arrangeSessionsByMp3FileName(mAllSessions, mMp3Filenames);
        }else{
            Log.d(TAG, "arrangeSessionsOnDesiredFrame::COULD NOT CREATE ARRANGED LIST OF SESSIONS");
            return null;
        }
    }

////////////////////////////////////////
//sessions duration graph
    @Override
    protected void setSessionsDurationChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_duration_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> sessionsDurations = new ArrayList<>();
        for (List<SessionRecord> sessionsForMp3Filename : mAllSessionsArranged) {
            float averageDuration = (float) GeneralStats.getAverageDuration(sessionsForMp3Filename);
            //for graph lisibility, we set values to minutes, will be formatted differently again for clicked value display
            averageDuration = averageDuration / 60000f;
            sessionsDurations.add(averageDuration);
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(sessionsDurations);
        //
        ((Mp3FilesChartDisplay) mSessionsDurationChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mSessionsDurationChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_duration_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FOMATTED_TIME,
                false,
                true,
                true,
                getString(R.string.mp3_stats_duration_help_message));
        mSessionsDurationChartViewHolder.setListener(this);
    }


////////////////////////////////////////
//sessions number graph
    @Override
    protected void setSessionsNumberChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_sessions_number_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfSessionsPerMp3Filename = new ArrayList<>();
        for (List<SessionRecord> sessionsForMp3Filename : mAllSessionsArranged) {
            numberOfSessionsPerMp3Filename.add((float) sessionsForMp3Filename.size());
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsPerMp3Filename);
        //
        ((Mp3FilesChartDisplay) mSessionsNumberChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mSessionsNumberChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_sessions_number_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_sessions_number_help_message));
        mSessionsNumberChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses count graph
    @Override
    protected void setPausesCountChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_pauses_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfPausesPerMp3Filename = new ArrayList<>();
        for (List<SessionRecord> sessionsForMp3Filename : mAllSessionsArranged) {
            numberOfPausesPerMp3Filename.add((float) GeneralStats.getTotalNumberOfPauses(sessionsForMp3Filename));
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfPausesPerMp3Filename);
        //
        ((Mp3FilesChartDisplay) mPausesCountChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mPausesCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_pauses_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_pauses_count_help_message));
        mPausesCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses per session graph
    @Override
    protected void setPausesPerSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_pauses_per_session_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> averageNumberOfPausesBySessionPerMp3Filename = new ArrayList<>();
        for (List<SessionRecord> sessionsForMp3Filename : mAllSessionsArranged) {
            averageNumberOfPausesBySessionPerMp3Filename.add(GeneralStats.getAverageNumberOfPausesBySession(sessionsForMp3Filename));
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(averageNumberOfPausesBySessionPerMp3Filename);
        //
        ((Mp3FilesChartDisplay) mPausesPerSessionChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mPausesPerSessionChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_pauses_per_session_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_pauses_per_session_help_message));
        mPausesPerSessionChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    @Override
    protected void setStreaksStartStopSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_starting_streak_sessions_count_legend));
        legends.add(getString(R.string.mp3_stats_stopping_streak_sessions_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        //Here we can not work with mAllSessionsArranged, we have to FIRST filter starting and stopping streak sessions, THEN arrange those by hour of day, otherwise the filter method will compare only sessions in the same time slice, but two sessions can be consecutive (next day) and NOT in the same time slice...
        List<Float> numberOfSessionsStartingAStreakPerMp3Filename = new ArrayList<>();
        List<SessionRecord> onlyStartingStreakSessions = GeneralStats.filterGetOnlyStartingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStartingStreakSessionsByMp3Filename = Mp3Stats.arrangeSessionsByMp3FileName(onlyStartingStreakSessions, mMp3Filenames);
        for (List<SessionRecord> startingStreakSessionsForMp3Filename : onlyStartingStreakSessionsByMp3Filename) {
            numberOfSessionsStartingAStreakPerMp3Filename.add((float) startingStreakSessionsForMp3Filename.size());
        }
        //
        List<Float> numberOfSessionsStoppingAStreakPerMp3Filename = new ArrayList<>();
        List<SessionRecord> onlyStoppingStreakSessions = GeneralStats.filterGetOnlyStoppingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStoppingStreakSessionsByMp3Filename =  Mp3Stats.arrangeSessionsByMp3FileName(onlyStoppingStreakSessions, mMp3Filenames);
        for (List<SessionRecord> stoppingStreakSessionsForMp3Filename : onlyStoppingStreakSessionsByMp3Filename) {
            numberOfSessionsStoppingAStreakPerMp3Filename.add((float) stoppingStreakSessionsForMp3Filename.size());
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsStartingAStreakPerMp3Filename);
        yValuesLists.add(numberOfSessionsStoppingAStreakPerMp3Filename);
        //
        ((Mp3FilesChartDisplay) mStartingStoppingStreakSessionsCountChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mStartingStoppingStreakSessionsCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_starting_stopping_streak_sessions_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_starting_stopping_streak_sessions_count_help_message));
        mStartingStoppingStreakSessionsCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session start graph
    @Override
    protected  void setUserStateAtSessionStartChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_body_at_session_start_legend));
        legends.add(getString(R.string.mp3_stats_thoughts_at_session_start_legend));
        legends.add(getString(R.string.mp3_stats_feelings_at_session_start_legend));
        legends.add(getString(R.string.mp3_stats_global_at_session_start_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        //
        List<Float> bodyValues = new ArrayList<>();
        List<Float> thoughtsValues = new ArrayList<>();
        List<Float> feelingsValues = new ArrayList<>();
        List<Float> globalValues = new ArrayList<>();
        for(List<SessionRecord> sessionsWithOneMp3Filename : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageStartBodyValue(sessionsWithOneMp3Filename));
            thoughtsValues.add(GeneralStats.getAverageStartThoughtsValue(sessionsWithOneMp3Filename));
            feelingsValues.add(GeneralStats.getAverageStartFeelingsValue(sessionsWithOneMp3Filename));
            globalValues.add(GeneralStats.getAverageStartGlobalValue(sessionsWithOneMp3Filename));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        ((Mp3FilesChartDisplay) mUserStateAtSessionStartChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mUserStateAtSessionStartChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_user_state_on_session_start_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_user_state_on_session_start_help_message));
        mUserStateAtSessionStartChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateAtSessionEndChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_body_at_session_end_legend));
        legends.add(getString(R.string.mp3_stats_thoughts_at_session_end_legend));
        legends.add(getString(R.string.mp3_stats_feelings_at_session_end_legend));
        legends.add(getString(R.string.mp3_stats_global_at_session_end_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        //
        List<Float> bodyValues = new ArrayList<>();
        List<Float> thoughtsValues = new ArrayList<>();
        List<Float> feelingsValues = new ArrayList<>();
        List<Float> globalValues = new ArrayList<>();
        for(List<SessionRecord> sessionsWithOneMp3Filename : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageEndBodyValue(sessionsWithOneMp3Filename));
            thoughtsValues.add(GeneralStats.getAverageEndThoughtsValue(sessionsWithOneMp3Filename));
            feelingsValues.add(GeneralStats.getAverageEndFeelingsValue(sessionsWithOneMp3Filename));
            globalValues.add(GeneralStats.getAverageEndGlobalValue(sessionsWithOneMp3Filename));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        ((Mp3FilesChartDisplay) mUserStateAtSessionEndChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mUserStateAtSessionEndChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_user_state_on_session_end_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.mp3_stats_user_state_on_session_end_help_message));
        mUserStateAtSessionEndChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateDiffChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.mp3_stats_body_diff_legend));
        legends.add(getString(R.string.mp3_stats_thoughts_diff_legend));
        legends.add(getString(R.string.mp3_stats_feelings_diff_legend));
        legends.add(getString(R.string.mp3_stats_global_diff_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        //
        List<Float> bodyValues = new ArrayList<>();
        List<Float> thoughtsValues = new ArrayList<>();
        List<Float> feelingsValues = new ArrayList<>();
        List<Float> globalValues = new ArrayList<>();
        for(List<SessionRecord> sessionsWithOneMp3Filename : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageDiffBodyValue(sessionsWithOneMp3Filename));
            thoughtsValues.add(GeneralStats.getAverageDiffThoughtsValue(sessionsWithOneMp3Filename));
            feelingsValues.add(GeneralStats.getAverageDiffFeelingsValue(sessionsWithOneMp3Filename));
            globalValues.add(GeneralStats.getAverageDiffGlobalValue(sessionsWithOneMp3Filename));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        ((Mp3FilesChartDisplay) mUserStateDiffChartViewHolder).setMp3FileNamesLabel(mMp3Filenames);
        mUserStateDiffChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mMp3Filenames.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.mp3_stats_user_state_diff_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                true,
                true,
                true,
                getString(R.string.mp3_stats_user_state_diff_help_message));
        mUserStateDiffChartViewHolder.setListener(this);
    }

}