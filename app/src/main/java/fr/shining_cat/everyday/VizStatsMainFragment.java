package fr.shining_cat.everyday;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.TimeOperations;


public class VizStatsMainFragment   extends Fragment{

    public static final String VIEW_STATS_MAIN_FRAGMENT_TAG = "view_stats_main_Fragment-tag";


    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private final static String VIEW_STATS_DAY = "view stats for day";
    private final static String VIEW_STATS_WEEK = "view stats for week";
    private final static String VIEW_STATS_MONTH = "view stats for month";
    private final static String VIEW_STATS_DURATION = "view stats for duration";
    private final static String VIEW_STATS_MP3 = "view stats for mp3";


    private VizStatsMainFragmentListener mListener;

    private List<SessionRecord> mAllSessions;

    private ProgressBar mLoadingIndicator;
    private TextView mTotalSessionsValueTxtVw;
    private TextView mLongestSessionValueTxtVw;
    private TextView mLongestStreakValueTxtVw;
    private TextView mTotalLengthValueTxtVw;
    private TextView mAverageSessionLengthValueTxtVw;
    private TextView mCurrentStreakValueTxtVw;
    private Button mViewStatsDayBtn;
    private Button mViewStatsWeekBtn;
    private Button mViewStatsMonthBtn;
    private Button mViewStatsDurationBtn;
    private Button mViewStatsMp3Btn;

    public VizStatsMainFragment() {}



    public static VizStatsMainFragment newInstance() {
        VizStatsMainFragment fragment = new VizStatsMainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_viz_stats_main, container, false);
        //
        mLoadingIndicator = rootView.findViewById(R.id.loading_indicator);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        //General statistics
        mTotalSessionsValueTxtVw = rootView.findViewById(R.id.total_sessions_value);
        mLongestSessionValueTxtVw = rootView.findViewById(R.id.longest_session_value);
        mLongestStreakValueTxtVw = rootView.findViewById(R.id.longest_streak_value);
        mTotalLengthValueTxtVw = rootView.findViewById(R.id.total_length_value);
        mAverageSessionLengthValueTxtVw = rootView.findViewById(R.id.average_session_length_value);
        mCurrentStreakValueTxtVw = rootView.findViewById(R.id.current_streak_value);
        //
        mViewStatsDayBtn = rootView.findViewById(R.id.view_stats_day_btn);
        mViewStatsDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewStatsFor(VIEW_STATS_DAY);
            }
        });
        mViewStatsDayBtn.setVisibility(View.INVISIBLE);
        //
        mViewStatsWeekBtn = rootView.findViewById(R.id.view_stats_week_btn);
        mViewStatsWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewStatsFor(VIEW_STATS_WEEK);
            }
        });
        mViewStatsWeekBtn.setVisibility(View.INVISIBLE);
        //
        mViewStatsMonthBtn = rootView.findViewById(R.id.view_stats_month_btn);
        mViewStatsMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewStatsFor(VIEW_STATS_MONTH);
            }
        });
        mViewStatsMonthBtn.setVisibility(View.INVISIBLE);
        //
        mViewStatsDurationBtn = rootView.findViewById(R.id.view_stats_duration_btn);
        mViewStatsDurationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewStatsFor(VIEW_STATS_DURATION);
            }
        });
        mViewStatsDurationBtn.setVisibility(View.INVISIBLE);
        //
        mViewStatsMp3Btn = rootView.findViewById(R.id.view_stats_mp3_btn);
        mViewStatsMp3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewStatsFor(VIEW_STATS_MP3);
            }
        });
        mViewStatsMp3Btn.setVisibility(View.GONE);
        return rootView;
    }

    public void setAllSessionsList(List<SessionRecord> allSessions) {
        mLoadingIndicator.setVisibility(View.GONE);
        mAllSessions = allSessions;
        updateGeneralStatistics();
        //only show buttons when data is available
        mViewStatsDayBtn.setVisibility(View.VISIBLE);
        mViewStatsWeekBtn.setVisibility(View.VISIBLE);
        mViewStatsMonthBtn.setVisibility(View.VISIBLE);
        mViewStatsDurationBtn.setVisibility(View.VISIBLE);
        updateMp3StatsBtnVisibility();
    }

    private void updateGeneralStatistics(){
        mTotalSessionsValueTxtVw.setText(String.format(getString(R.string.total_number_of_sessions_value), mAllSessions.size()));
        //
        long longestSessionDuration = GeneralStats.getLongestSession(mAllSessions);
        mLongestSessionValueTxtVw.setText(TimeOperations.convertMillisecondsToHoursAndMinutesString(
                                                                            longestSessionDuration,
                                                                            getString(R.string.generic_string_SHORT_HOURS),
                                                                            getString(R.string.generic_string_SHORT_MINUTES),
                                                                            false));
        //
        mLongestStreakValueTxtVw.setText(String.format(getString(R.string.streak_value), GeneralStats.getLongestStreak(mAllSessions)));
        //
        long totalLength = GeneralStats.getTotalLength(mAllSessions);
        mTotalLengthValueTxtVw.setText(TimeOperations.convertMillisecondsToHoursAndMinutesString(
                                                                            totalLength,
                                                                            getString(R.string.generic_string_SHORT_HOURS),
                                                                            getString(R.string.generic_string_SHORT_MINUTES),
                                                                            false));
        //
        long averageDuration = GeneralStats.getAverageDuration(mAllSessions);
        mAverageSessionLengthValueTxtVw.setText(TimeOperations.convertMillisecondsToHoursMinutesAndSecondsString(
                                                                            averageDuration,
                                                                            getString(R.string.generic_string_SHORT_HOURS),
                                                                            getString(R.string.generic_string_SHORT_MINUTES),
                                                                            getString(R.string.generic_string_SHORT_SECONDS),
                                                                            false));
        //
        mCurrentStreakValueTxtVw.setText(String.format(getString(R.string.streak_value), GeneralStats.getCurrentStreak(mAllSessions)));
    }

    private void updateMp3StatsBtnVisibility(){
        //only show stats for mp3 button if there are sessions with mp3 file recorded
        if(GeneralStats.areThereSessionsWithMp3(mAllSessions)){
            mViewStatsMp3Btn.setVisibility(View.VISIBLE);
        }else{
            mViewStatsMp3Btn.setVisibility(View.GONE);
        }
    }

    private void openViewStatsFor(String whichStats){
        switch (whichStats){
            case VIEW_STATS_DAY:
                mListener.onOpenViewStatsDay();
                break;
            case VIEW_STATS_WEEK:
                mListener.onOpenViewStatsWeek();
                break;
            case VIEW_STATS_MONTH:
                mListener.onOpenViewStatsMonth();
                break;
            case VIEW_STATS_DURATION:
                mListener.onOpenViewStatsDuration();
                break;
            case VIEW_STATS_MP3:
                mListener.onOpenViewStatsMp3();
                break;
        }
    }
////////////////////////////////////////
//plugging interface listener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VizStatsMainFragmentListener) {
            mListener = (VizStatsMainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement VizStatsMainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

////////////////////////////////////////
//Listener interface
    public interface VizStatsMainFragmentListener {
        void onOpenViewStatsDay();
        void onOpenViewStatsWeek();
        void onOpenViewStatsMonth();
        void onOpenViewStatsDuration();
        void onOpenViewStatsMp3();
    }
}
