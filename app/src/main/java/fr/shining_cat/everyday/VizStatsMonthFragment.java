package fr.shining_cat.everyday;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.analytics.MonthStats;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.widgets.ChartDisplay;


public class VizStatsMonthFragment extends VizStatsDetailsFragment{

    public static final String VIEW_STATS_MONTH_FRAGMENT_TAG = "view_stats_month_Fragment-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public VizStatsMonthFragment() {
        // Required empty public constructor
    }

    public static VizStatsMonthFragment newInstance() {
        VizStatsMonthFragment fragment = new VizStatsMonthFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viz_stats_month, container, false);
        super.setChartDisplayComponents(rootView);
        super.setCharts();
        return rootView;
    }

    @Override
    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        return MonthStats.arrangeSessionsByDayOfMonth(mAllSessions);
    }

////////////////////////////////////////
//sessions duration graph
    @Override
    protected void setSessionsDurationChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_duration_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> sessionsDurations = new ArrayList<>();
        for (List<SessionRecord> sessionsForDay : mAllSessionsArranged) {
            float averageDuration = (float) GeneralStats.getAverageDuration(sessionsForDay);
            //for graph lisibility, we set values to minutes, will be formatted differently again for clicked value display
            averageDuration = averageDuration / 60000f;
            sessionsDurations.add(averageDuration);
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(sessionsDurations);
        //
        mSessionsDurationChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_duration_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FOMATTED_TIME,
                false,
                true,
                true,
                getString(R.string.month_stats_duration_help_message));
        mSessionsDurationChartViewHolder.setListener(this);
    }


////////////////////////////////////////
//sessions number graph
    @Override
    protected void setSessionsNumberChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_sessions_number_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfSessionsPerDay = new ArrayList<>();
        for (List<SessionRecord> sessionsForDay : mAllSessionsArranged) {
            numberOfSessionsPerDay.add((float) sessionsForDay.size());
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsPerDay);
        //
        mSessionsNumberChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_sessions_number_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.month_stats_sessions_number_help_message));
        mSessionsNumberChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses count graph
    @Override
    protected void setPausesCountChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_pauses_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfPausesPerDay = new ArrayList<>();
        for (List<SessionRecord> sessionsForDay : mAllSessionsArranged) {
            //Log.d(TAG, "setPausesCountChart::counting for one hour");
            numberOfPausesPerDay.add((float) GeneralStats.getTotalNumberOfPauses(sessionsForDay));
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfPausesPerDay);
        //
        mPausesCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_pauses_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.month_stats_pauses_count_help_message));
        mPausesCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses per session graph
    @Override
    protected void setPausesPerSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_pauses_per_session_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> averageNumberOfPausesBySessionPerDay = new ArrayList<>();
        for (List<SessionRecord> sessionsForDay : mAllSessionsArranged) {
            averageNumberOfPausesBySessionPerDay.add(GeneralStats.getAverageNumberOfPausesBySession(sessionsForDay));
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(averageNumberOfPausesBySessionPerDay);
        //
        mPausesPerSessionChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_pauses_per_session_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.month_stats_pauses_per_session_help_message));
        mPausesPerSessionChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    @Override
    protected void setStreaksStartStopSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_starting_streak_sessions_count_legend));
        legends.add(getString(R.string.month_stats_stopping_streak_sessions_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        //Here we can not work with mAllSessionsArranged, we have to FIRST filter starting and stopping streak sessions, THEN arrange those by hour of day, otherwise the filter method will compare only sessions in the same time slice, but two sessions can be consecutive (next day) and NOT in the same time slice...
        List<Float> numberOfSessionsStartingAStreakPerDay = new ArrayList<>();
        List<SessionRecord> onlyStartingStreakSessions = GeneralStats.filterGetOnlyStartingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStartingStreakSessionsByDayOfMonth = MonthStats.arrangeSessionsByDayOfMonth(onlyStartingStreakSessions);
        for (List<SessionRecord> startingStreakSessionsForDay : onlyStartingStreakSessionsByDayOfMonth) {
            numberOfSessionsStartingAStreakPerDay.add((float) startingStreakSessionsForDay.size());
        }
        //
        List<Float> numberOfSessionsStoppingAStreakPerDay = new ArrayList<>();
        List<SessionRecord> onlyStoppingStreakSessions = GeneralStats.filterGetOnlyStoppingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStoppingStreakSessionsByDayOfMonth = MonthStats.arrangeSessionsByDayOfMonth(onlyStoppingStreakSessions);
        for (List<SessionRecord> stoppingStreakSessionsForDay : onlyStoppingStreakSessionsByDayOfMonth) {
            numberOfSessionsStoppingAStreakPerDay.add((float) stoppingStreakSessionsForDay.size());
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsStartingAStreakPerDay);
        yValuesLists.add(numberOfSessionsStoppingAStreakPerDay);
        //
        mStartingStoppingStreakSessionsCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_starting_stopping_streak_sessions_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.month_stats_starting_stopping_streak_sessions_count_help_message));
        mStartingStoppingStreakSessionsCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session start graph
    @Override
    protected  void setUserStateAtSessionStartChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_body_at_session_start_legend));
        legends.add(getString(R.string.month_stats_thoughts_at_session_start_legend));
        legends.add(getString(R.string.month_stats_feelings_at_session_start_legend));
        legends.add(getString(R.string.month_stats_global_at_session_start_legend));
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
        for(List<SessionRecord> sessionsInOneDay : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageStartBodyValue(sessionsInOneDay));
            thoughtsValues.add(GeneralStats.getAverageStartThoughtsValue(sessionsInOneDay));
            feelingsValues.add(GeneralStats.getAverageStartFeelingsValue(sessionsInOneDay));
            globalValues.add(GeneralStats.getAverageStartGlobalValue(sessionsInOneDay));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionStartChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_user_state_on_session_start_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.month_stats_user_state_on_session_start_help_message));
        mUserStateAtSessionStartChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateAtSessionEndChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_body_at_session_end_legend));
        legends.add(getString(R.string.month_stats_thoughts_at_session_end_legend));
        legends.add(getString(R.string.month_stats_feelings_at_session_end_legend));
        legends.add(getString(R.string.month_stats_global_at_session_end_legend));
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
        for(List<SessionRecord> sessionsInOneDay : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageEndBodyValue(sessionsInOneDay));
            thoughtsValues.add(GeneralStats.getAverageEndThoughtsValue(sessionsInOneDay));
            feelingsValues.add(GeneralStats.getAverageEndFeelingsValue(sessionsInOneDay));
            globalValues.add(GeneralStats.getAverageEndGlobalValue(sessionsInOneDay));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionEndChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_user_state_on_session_end_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.month_stats_user_state_on_session_end_help_message));
        mUserStateAtSessionEndChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateDiffChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.month_stats_body_diff_legend));
        legends.add(getString(R.string.month_stats_thoughts_diff_legend));
        legends.add(getString(R.string.month_stats_feelings_diff_legend));
        legends.add(getString(R.string.month_stats_global_diff_legend));
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
        for(List<SessionRecord> sessionsInOneDay : mAllSessionsArranged) {
            bodyValues.add(GeneralStats.getAverageDiffBodyValue(sessionsInOneDay));
            thoughtsValues.add(GeneralStats.getAverageDiffThoughtsValue(sessionsInOneDay));
            feelingsValues.add(GeneralStats.getAverageDiffFeelingsValue(sessionsInOneDay));
            globalValues.add(GeneralStats.getAverageDiffGlobalValue(sessionsInOneDay));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateDiffChartViewHolder.setChartData(
                MiscUtils.getEmptyList(31),
                legends,
                colors,
                yValuesLists,
                getString(R.string.month_stats_user_state_diff_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                true,
                true,
                true,
                getString(R.string.month_stats_user_state_diff_help_message));
        mUserStateDiffChartViewHolder.setListener(this);
    }

}