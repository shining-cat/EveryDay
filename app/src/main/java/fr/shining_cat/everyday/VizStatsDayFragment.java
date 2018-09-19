package fr.shining_cat.everyday;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.analytics.DayStats;
import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.widgets.ChartDisplay;


public class VizStatsDayFragment extends VizStatsDetailsFragment{

    public static final String VIEW_STATS_DAY_FRAGMENT_TAG = "view_stats_day_Fragment-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public VizStatsDayFragment() {
        // Required empty public constructor
    }

    public static VizStatsDayFragment newInstance() {
        VizStatsDayFragment fragment = new VizStatsDayFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viz_stats_day, container, false);
        super.setChartDisplayComponents(rootView);
        super.setCharts();
        return rootView;
    }

    @Override
    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        return DayStats.arrangeSessionsByHourOfDay(mAllSessions);
    }

////////////////////////////////////////
//sessions duration graph
    @Override
    protected void setSessionsDurationChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_duration_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> sessionsDurations = new ArrayList<>();
        for (List<SessionRecord> sessionsForHour : mAllSessionsArranged) {
            //Log.d(TAG, "setSessionsDurationChart::average session duration for one hour");
            float averageDuration = (float) GeneralStats.getAverageDuration(sessionsForHour);
            //for graph lisibility, we set values to minutes, will be formatted differently again for clicked value display
            averageDuration = averageDuration / 60000f;
            sessionsDurations.add(averageDuration);
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(sessionsDurations);
        //
        mSessionsDurationChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_duration_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FOMATTED_TIME,
                false,
                true,
                true,
                getString(R.string.day_stats_duration_help_message));
        mSessionsDurationChartViewHolder.setListener(this);
    }


////////////////////////////////////////
//sessions number graph
    @Override
    protected void setSessionsNumberChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_sessions_number_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfSessionsPerHour = new ArrayList<>();
        for (List<SessionRecord> sessionsForHour : mAllSessionsArranged) {
            //Log.d(TAG, "setSessionsNumberChart::counting sessions for one hour : " + numberOfSessionsForAnHour);
            numberOfSessionsPerHour.add((float) sessionsForHour.size());
        }
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsPerHour);
        //
        mSessionsNumberChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_sessions_number_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.day_stats_sessions_number_help_message));
        mSessionsNumberChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//Pauses count graph
    @Override
    protected void setPausesCountChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_pauses_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> numberOfPausesPerHour = new ArrayList<>();
        int totalPauses = 0;
        for (List<SessionRecord> sessionsForHour : mAllSessionsArranged) {
            //Log.d(TAG, "setPausesCountChart::counting for one hour");
            int numberOfPausesForAnHour = GeneralStats.getTotalNumberOfPauses(sessionsForHour);
            totalPauses += numberOfPausesForAnHour;
            numberOfPausesPerHour.add((float) numberOfPausesForAnHour);
        }
        //do not display chart if there is no pauses at all
        if(totalPauses == 0) {
            mPausesCountChartViewHolder.hideMeIHaveNoDataToShow(getString(R.string.day_stats_pauses_count_legend) + ":\n" + getString(R.string.stats_pauses_count_nothing_to_display));
        } else {
            ArrayList<List<Float>> yValuesLists = new ArrayList<>();
            yValuesLists.add(numberOfPausesPerHour);
            //
            mPausesCountChartViewHolder.setChartData(
                    MiscUtils.getEmptyList(24),
                    legends,
                    colors,
                    yValuesLists,
                    getString(R.string.day_stats_pauses_count_clicked_value),
                    ChartDisplay.DISPLAY_ROUNDING_INT,
                    false,
                    true,
                    true,
                    getString(R.string.day_stats_pauses_count_help_message));
            mPausesCountChartViewHolder.setListener(this);
        }
    }

////////////////////////////////////////
//Pauses per session graph
    @Override
    protected void setPausesPerSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_pauses_per_session_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        //
        List<Float> averageNumberOfPausesBySessionPerHour = new ArrayList<>();
        float totalPauses = 0;
        for (List<SessionRecord> sessionsForHour : mAllSessionsArranged) {
            //Log.d(TAG, "setPausesPerSessionChart::counting and average for one hour");
            float numberOfPausesForASession = GeneralStats.getAverageNumberOfPausesBySession(sessionsForHour);
            totalPauses += numberOfPausesForASession;
            averageNumberOfPausesBySessionPerHour.add(numberOfPausesForASession);
        }
        //do not display chart if there is no pauses at all
        if(totalPauses == 0) {
            mPausesPerSessionChartViewHolder.hideMeIHaveNoDataToShow(getString(R.string.day_stats_pauses_per_session_legend) + ":\n" + getString(R.string.stats_pauses_count_nothing_to_display));
        } else {
            ArrayList<List<Float>> yValuesLists = new ArrayList<>();
            yValuesLists.add(averageNumberOfPausesBySessionPerHour);
            //
            mPausesPerSessionChartViewHolder.setChartData(
                    MiscUtils.getEmptyList(24),
                    legends,
                    colors,
                    yValuesLists,
                    getString(R.string.day_stats_pauses_per_session_clicked_value),
                    ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                    false,
                    true,
                    true,
                    getString(R.string.day_stats_pauses_per_session_help_message));
            mPausesPerSessionChartViewHolder.setListener(this);
        }
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    @Override
    protected void setStreaksStartStopSessionChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_starting_streak_sessions_count_legend));
        legends.add(getString(R.string.day_stats_stopping_streak_sessions_count_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        //Here we can not work with mAllSessionsArranged, we have to FIRST filter starting and stopping streak sessions, THEN arrange those by hour of day, otherwise the filter method will compare only sessions in the same time slice, but two sessions can be consecutive (next day) and NOT in the same time slice...
        List<Float> numberOfSessionsStartingAStreakPerHour = new ArrayList<>();
        //Log.d(TAG, "setStreaksStartStopSessionChart:: number of starting streaks = " + GeneralStats.getStartedStreaksNumber(mAllSessions));
        //Log.d(TAG, "setStreaksStartStopSessionChart:: number of stopping streaks = " + GeneralStats.getStoppedStreaksNumber(mAllSessions));
        List<SessionRecord> onlyStartingStreakSessions = GeneralStats.filterGetOnlyStartingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStartingStreakSessionsByHourOfDay = DayStats.arrangeSessionsByHourOfDay(onlyStartingStreakSessions);
        for (List<SessionRecord> startingStreakSessionsForHour : onlyStartingStreakSessionsByHourOfDay) {
            //Log.d(TAG, "setStreaksStartStopSessionChart::counting starting streak sessions for one hour : " + numberOfSessionsStartingAStreakForAnHour) ;
            numberOfSessionsStartingAStreakPerHour.add((float) startingStreakSessionsForHour.size());
            //Log.d(TAG, "setStreaksStartStopSessionChart:: numberOfSessionsStartingAStreakForAnHour = " + numberOfSessionsStartingAStreakForAnHour);
        }
        //
        List<Float> numberOfSessionsStoppingAStreakPerHour = new ArrayList<>();
        List<SessionRecord> onlyStoppingStreakSessions = GeneralStats.filterGetOnlyStoppingStreakSessions(mAllSessions);
        List<List<SessionRecord>> onlyStoppingStreakSessionsByHourOfDay = DayStats.arrangeSessionsByHourOfDay(onlyStoppingStreakSessions);
        for (List<SessionRecord> stoppingStreakSessionsForHour : onlyStoppingStreakSessionsByHourOfDay) {
            //Log.d(TAG, "setStreaksStartStopSessionChart::counting stopping streak sessions for one hour : " + numberOfSessionsStoppingAStreakForAnHour) ;
            numberOfSessionsStoppingAStreakPerHour.add((float) stoppingStreakSessionsForHour.size());
            //Log.d(TAG, "setStreaksStartStopSessionChart:: numberOfSessionsStoppingAStreakForAnHour = " + numberOfSessionsStoppingAStreakForAnHour);
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(numberOfSessionsStartingAStreakPerHour);
        yValuesLists.add(numberOfSessionsStoppingAStreakPerHour);
        //
        mStartingStoppingStreakSessionsCountChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_starting_stopping_streak_sessions_count_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_INT,
                false,
                true,
                true,
                getString(R.string.day_stats_starting_stopping_streak_sessions_count_help_message));
        mStartingStoppingStreakSessionsCountChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session start graph
    @Override
    protected  void setUserStateAtSessionStartChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_body_at_session_start_legend));
        legends.add(getString(R.string.day_stats_thoughts_at_session_start_legend));
        legends.add(getString(R.string.day_stats_feelings_at_session_start_legend));
        legends.add(getString(R.string.day_stats_global_at_session_start_legend));
        //
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        //
        List<Float> bodyValues      = new ArrayList<>();
        List<Float> thoughtsValues  = new ArrayList<>();
        List<Float> feelingsValues  = new ArrayList<>();
        List<Float> globalValues    = new ArrayList<>();
        for(List<SessionRecord> sessionsInOneHour : mAllSessionsArranged) {
            //here we will insert 0 when there is no value to calculate an average in the hour examined since 0 is a value that could only be obtained if every value is unset => same case
            bodyValues.add((GeneralStats.getAverageStartBodyValue(sessionsInOneHour) != null) ? GeneralStats.getAverageStartBodyValue(sessionsInOneHour) : 0);
            thoughtsValues.add((GeneralStats.getAverageStartThoughtsValue(sessionsInOneHour) != null) ? GeneralStats.getAverageStartThoughtsValue(sessionsInOneHour) : 0);
            feelingsValues.add((GeneralStats.getAverageStartFeelingsValue(sessionsInOneHour) != null) ? GeneralStats.getAverageStartFeelingsValue(sessionsInOneHour) : 0);
            globalValues.add((GeneralStats.getAverageStartGlobalValue(sessionsInOneHour) != null) ? GeneralStats.getAverageStartGlobalValue(sessionsInOneHour) : 0);
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionStartChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_user_state_on_session_start_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.day_stats_user_state_on_session_start_help_message));
        mUserStateAtSessionStartChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateAtSessionEndChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_body_at_session_end_legend));
        legends.add(getString(R.string.day_stats_thoughts_at_session_end_legend));
        legends.add(getString(R.string.day_stats_feelings_at_session_end_legend));
        legends.add(getString(R.string.day_stats_global_at_session_end_legend));
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
        for(List<SessionRecord> sessionsInOneHour : mAllSessionsArranged) {
            //here we will insert 0 when there is no value to calculate an average in the hour examined since 0 is a value that could only be obtained if every value is unset => same case
            bodyValues.add((GeneralStats.getAverageEndBodyValue(sessionsInOneHour) != null) ? GeneralStats.getAverageEndBodyValue(sessionsInOneHour) : 0);
            thoughtsValues.add((GeneralStats.getAverageEndThoughtsValue(sessionsInOneHour) != null) ? GeneralStats.getAverageEndThoughtsValue(sessionsInOneHour) : 0);
            feelingsValues.add((GeneralStats.getAverageEndFeelingsValue(sessionsInOneHour) != null) ? GeneralStats.getAverageEndFeelingsValue(sessionsInOneHour) : 0);
            globalValues.add((GeneralStats.getAverageEndGlobalValue(sessionsInOneHour) != null) ? GeneralStats.getAverageEndGlobalValue(sessionsInOneHour) : 0);
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateAtSessionEndChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_user_state_on_session_end_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                false,
                true,
                true,
                getString(R.string.day_stats_user_state_on_session_end_help_message));
        mUserStateAtSessionEndChartViewHolder.setListener(this);
    }

////////////////////////////////////////
//User state at session end graph
    @Override
    protected  void setUserStateDiffChart() {
        ArrayList<String> legends = new ArrayList<>();
        legends.add(getString(R.string.day_stats_body_diff_legend));
        legends.add(getString(R.string.day_stats_thoughts_diff_legend));
        legends.add(getString(R.string.day_stats_feelings_diff_legend));
        legends.add(getString(R.string.day_stats_global_diff_legend));
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
        for(List<SessionRecord> sessionsInOneHour : mAllSessionsArranged) {
            //here we will insert value as is, since it will be 0f if calculation is not possible (no start or no end value)
            // => it will appear on the chart as "no variation between start and end of session", just as it would if there were actually no variation...
            bodyValues.add(GeneralStats.getAverageDiffBodyValue(sessionsInOneHour));
            thoughtsValues.add(GeneralStats.getAverageDiffThoughtsValue(sessionsInOneHour));
            feelingsValues.add(GeneralStats.getAverageDiffFeelingsValue(sessionsInOneHour));
            globalValues.add(GeneralStats.getAverageDiffGlobalValue(sessionsInOneHour));
        }
        //
        ArrayList<List<Float>> yValuesLists =  new ArrayList<>();
        yValuesLists.add(bodyValues);
        yValuesLists.add(thoughtsValues);
        yValuesLists.add(feelingsValues);
        yValuesLists.add(globalValues);
        //
        mUserStateDiffChartViewHolder.setChartData(
                MiscUtils.getEmptyList(24),
                legends,
                colors,
                yValuesLists,
                getString(R.string.day_stats_user_state_diff_clicked_value),
                ChartDisplay.DISPLAY_ROUNDING_FLOAT,
                true,
                true,
                true,
                getString(R.string.day_stats_user_state_diff_help_message));
        mUserStateDiffChartViewHolder.setListener(this);
    }

}