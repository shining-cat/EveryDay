package fr.shining_cat.everyday;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.analytics.DurationStats;
import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.analytics.MonthStats;
import fr.shining_cat.everyday.analytics.Mp3Stats;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.widgets.ChartDisplay;


public class VizStatsDurationFragment extends VizStatsDetailsFragment{

    public static final String VIEW_STATS_DURATION_FRAGMENT_TAG = "view_stats_duration_Fragment-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public VizStatsDurationFragment() {
        // Required empty public constructor
    }

    public static VizStatsDurationFragment newInstance() {
        VizStatsDurationFragment fragment = new VizStatsDurationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viz_stats_duration, container, false);
        super.setChartDisplayComponents(rootView);
        super.setCharts();
        return rootView;
    }



    @Override
    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        return DurationStats.arrangeSessionsByDuration(mAllSessions, null);
    }

////////////////////////////////////////
//sessions duration graph
    @Override
    protected void setSessionsDurationChart() {
        //This chart is not present here (duration/duration would make no sense)
        mSessionsDurationChartViewHolder.setVisibility(View.GONE);
        //the ChartDisplay object can handle not having any data set, we just need to hide it from there, but removing it completely breaks the composition used here, so we let it be in the fragment layout but hide it
    }


////////////////////////////////////////
//sessions number graph
    @Override
    protected void setSessionsNumberChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_sessions_number_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfSessionsPerDurationSlice = new ArrayList<>();
        for (List<SessionRecord> sessionsForDurationSlice : mAllSessionsArranged) {
            numberOfSessionsPerDurationSlice.add((float) sessionsForDurationSlice.size());
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsPerDurationSlice);
        //
        mSessionsNumberChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.duration_stats_sessions_number_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.duration_stats_sessions_number_help_message));
        mSessionsNumberChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses count graph
    @Override
    protected void setPausesCountChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_pauses_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfPausesPerDurationSlice = new ArrayList<>();
        int totalPauses = 0;
        for (List<SessionRecord> sessionsForDurationSlice : mAllSessionsArranged) {
            int numberOfPausesForADurationSlice = GeneralStats.getTotalNumberOfPauses(sessionsForDurationSlice);
            totalPauses += numberOfPausesForADurationSlice;
            numberOfPausesPerDurationSlice.add((float) numberOfPausesForADurationSlice);
        }
        //do not display chart if there is no pauses at all
        if(totalPauses == 0) {
            mPausesCountChartViewHolder.hideMeIHaveNoDataToShow(getString(R.string.duration_stats_pauses_count_legend) + ":\n" + getString(R.string.stats_pauses_count_nothing_to_display));
        } else {
            ArrayList<List<Float>> yValuesLists = new ArrayList<>();
            yValuesLists.add(numberOfPausesPerDurationSlice);
            //
            mPausesCountChartViewHolder.setChartData(
                    MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                    legends,
                    colors,
                    yValuesLists,
                    getString(R.string.duration_stats_pauses_count_clicked_value),
                    ChartDisplay.DISPLAY_ROUNDING_INT,
                    false,
                    true,
                    true,
                    getString(R.string.duration_stats_pauses_count_help_message));
            mPausesCountChartViewHolder.setListener(this);
        }
    }

////////////////////////////////////////
//Pauses per session graph
    @Override
    protected void setPausesPerSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_pauses_per_session_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> averageNumberOfPausesBySessionPerDurationSlice = new ArrayList<>();
        float totalPauses = 0;
        for (List<SessionRecord> sessionsForDurationSlice : mAllSessionsArranged) {
            float numberOfPausesForASession = GeneralStats.getAverageNumberOfPausesBySession(sessionsForDurationSlice);
            totalPauses += numberOfPausesForASession;
            averageNumberOfPausesBySessionPerDurationSlice.add(numberOfPausesForASession);
        }
        //do not display chart if there is no pauses at all
        if(totalPauses == 0) {
            mPausesPerSessionChartViewHolder.hideMeIHaveNoDataToShow(getString(R.string.duration_stats_pauses_per_session_legend) + ":\n" + getString(R.string.stats_pauses_count_nothing_to_display));
        } else {
            ArrayList<List<Float>> yValuesLists = new ArrayList<>();
            yValuesLists.add(averageNumberOfPausesBySessionPerDurationSlice);
            //
            mPausesPerSessionChartViewHolder.setChartData(
                    MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                    legends,
                    colors,
                    yValuesLists,
                    getString(R.string.duration_stats_pauses_per_session_clicked_value),
                    ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                    false,
                    true,
                    true,
                    getString(R.string.duration_stats_pauses_per_session_help_message));
            mPausesPerSessionChartViewHolder.setListener(this);
        }
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    @Override
    protected void setStreaksStartStopSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_starting_streak_sessions_count_legend));
        legends.add(getString(R.string.duration_stats_stopping_streak_sessions_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        //Here we can not work with mAllSessionsArranged, we have to FIRST filter starting and stopping streak sessions, THEN arrange those by hour of day, otherwise the filter method will compare only sessions in the same time slice, but two sessions can be consecutive (next day) and NOT in the same time slice...
        //BUT the numberOfSessionsStartingAStreakPerDurationSlice and numberOfSessionsStoppingAStreakPerDurationSlice must be the same size as mAllSessions to keep the graph consistent with the others
        // (there could be different numbers of items in onlyStartingStreakSessionsByDurationSlice and onlyStoppingStreakSessionsByDurationSlice..)
        List<Float> numberOfSessionsStartingAStreakPerDurationSlice = new ArrayList<>();
        List<SessionRecord> onlyStartingStreakSessions = GeneralStats.filterGetOnlyStartingStreakSessions(mAllSessions);
        long longestDurationOfAllSessions = GeneralStats.getLongestSession(mAllSessions);
        //we need to give the longest of all sessions duration here because once filtered, the longest resulting may not have the same length and we would end up with different sizes of Lists, breaking the ChartDisplay and the consistence of the whole page
        List<List<SessionRecord>> onlyStartingStreakSessionsByDurationSlice = DurationStats.arrangeSessionsByDuration(onlyStartingStreakSessions, longestDurationOfAllSessions);
        for (List<SessionRecord> startingStreakSessionsForDurationSlice : onlyStartingStreakSessionsByDurationSlice) {
            numberOfSessionsStartingAStreakPerDurationSlice.add((float) startingStreakSessionsForDurationSlice.size());
        }
        //
        List<Float> numberOfSessionsStoppingAStreakPerDurationSlice = new ArrayList<>();
        List<SessionRecord> onlyStoppingStreakSessions = GeneralStats.filterGetOnlyStoppingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStoppingStreakSessionsByDurationSlice = DurationStats.arrangeSessionsByDuration(onlyStoppingStreakSessions, longestDurationOfAllSessions);
        for (List<SessionRecord> stoppingStreakSessionsForDurationSlice : onlyStoppingStreakSessionsByDurationSlice) {
            numberOfSessionsStoppingAStreakPerDurationSlice.add((float) stoppingStreakSessionsForDurationSlice.size());
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsStartingAStreakPerDurationSlice);
        yValuesLists.add(numberOfSessionsStoppingAStreakPerDurationSlice);

        //
        mStartingStoppingStreakSessionsCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.duration_stats_starting_stopping_streak_sessions_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.duration_stats_starting_stopping_streak_sessions_count_help_message));
        mStartingStoppingStreakSessionsCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session start graph
    @Override
    protected  void setUserStateAtSessionStartChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_body_at_session_start_legend));
        legends.add(getString(R.string.duration_stats_thoughts_at_session_start_legend));
        legends.add(getString(R.string.duration_stats_feelings_at_session_start_legend));
        legends.add(getString(R.string.duration_stats_global_at_session_start_legend));
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
        for(List<SessionRecord> sessionsInOneDurationSlice : mAllSessionsArranged) {
            //here we will insert 0 when there is no value to calculate an average in the hour examined since 0 is a value that could only be obtained if every value is unset => same case
            bodyValues.add((GeneralStats.getAverageStartBodyValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageStartBodyValue(sessionsInOneDurationSlice) : 0);
            thoughtsValues.add((GeneralStats.getAverageStartThoughtsValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageStartThoughtsValue(sessionsInOneDurationSlice) : 0);
            feelingsValues.add((GeneralStats.getAverageStartFeelingsValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageStartFeelingsValue(sessionsInOneDurationSlice) : 0);
            globalValues.add((GeneralStats.getAverageStartGlobalValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageStartGlobalValue(sessionsInOneDurationSlice) : 0);
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionStartChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.duration_stats_user_state_on_session_start_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.duration_stats_user_state_on_session_start_help_message));
        mUserStateAtSessionStartChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateAtSessionEndChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_body_at_session_end_legend));
        legends.add(getString(R.string.duration_stats_thoughts_at_session_end_legend));
        legends.add(getString(R.string.duration_stats_feelings_at_session_end_legend));
        legends.add(getString(R.string.duration_stats_global_at_session_end_legend));
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
        for(List<SessionRecord> sessionsInOneDurationSlice : mAllSessionsArranged) {
            //here we will insert 0 when there is no value to calculate an average in the hour examined since 0 is a value that could only be obtained if every value is unset => same case
            bodyValues.add((GeneralStats.getAverageEndBodyValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageEndBodyValue(sessionsInOneDurationSlice) : 0);
            thoughtsValues.add((GeneralStats.getAverageEndThoughtsValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageEndThoughtsValue(sessionsInOneDurationSlice) : 0);
            feelingsValues.add((GeneralStats.getAverageEndFeelingsValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageEndFeelingsValue(sessionsInOneDurationSlice) : 0);
            globalValues.add((GeneralStats.getAverageEndGlobalValue(sessionsInOneDurationSlice) != null) ? GeneralStats.getAverageEndGlobalValue(sessionsInOneDurationSlice) : 0);
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionEndChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.duration_stats_user_state_on_session_end_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.duration_stats_user_state_on_session_end_help_message));
        mUserStateAtSessionEndChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateDiffChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.duration_stats_body_diff_legend));
        legends.add(getString(R.string.duration_stats_thoughts_diff_legend));
        legends.add(getString(R.string.duration_stats_feelings_diff_legend));
        legends.add(getString(R.string.duration_stats_global_diff_legend));
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
        for(List<SessionRecord> sessionsInOneDurationSlice : mAllSessionsArranged) {
            //here we will insert value as is, since it will be 0f if calculation is not possible (no start or no end value)
            // => it will appear on the chart as "no variation between start and end of session", just as it would if there were actually no variation...
            bodyValues.add(GeneralStats.getAverageDiffBodyValue(sessionsInOneDurationSlice));
            thoughtsValues.add(GeneralStats.getAverageDiffThoughtsValue(sessionsInOneDurationSlice));
            feelingsValues.add(GeneralStats.getAverageDiffFeelingsValue(sessionsInOneDurationSlice));
            globalValues.add(GeneralStats.getAverageDiffGlobalValue(sessionsInOneDurationSlice));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateDiffChartViewHolder.setChartData(
                MiscUtils.getEmptyList(mAllSessionsArranged.size()),
                legends,
                colors,
                yValuesLists,
                getString(R.string.duration_stats_user_state_diff_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                true,
                true,
                true,
                getString(R.string.duration_stats_user_state_diff_help_message));
        mUserStateDiffChartViewHolder.setListener(this);
    }

}