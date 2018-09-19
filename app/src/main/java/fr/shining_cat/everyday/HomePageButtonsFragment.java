package fr.shining_cat.everyday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import fr.shining_cat.everyday.data.EveryDayRewardsDataRepository;
import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.utils.TimeOperations;
import fr.shining_cat.everyday.utils.UiUtils;
import fr.shining_cat.everyday.utils.WakelockController;

public class HomePageButtonsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String HOMEPAGEBUTTONSFRAGMENT_TAG = "HomePageButtonsFragment-tag";


    private HomePageButtonsFragmentListener mListener;


    private Context mContext;
    private View mRootView;


////////////////////////////////////////
//Main homepage Fragment
    public HomePageButtonsFragment() {
        // Required empty public constructor
    }

    public static HomePageButtonsFragment newInstance() {
        return new HomePageButtonsFragment();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        mContext = context;
        if (context instanceof HomePageButtonsFragmentListener) {
            mListener = (HomePageButtonsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement HomePageButtonsFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_homepage_buttons, container, false);
        //

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(this);
        //
        setStartSessionBtnText();
        Button startTimedSessionBtn = mRootView.findViewById(R.id.start_session_btn);
        startTimedSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null) {
                    mListener.startTimedSession();
                }else{
                    Log.e(TAG, "startTimedSessionBtn.onClick::no listener!!");
                }
            }
        });
        //
        Button viewStatsBtn = mRootView.findViewById(R.id.view_stats_btn);
        viewStatsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null) {
                    mListener.navigateToStats();
                }else{
                    Log.e(TAG, "viewStatsBtn.onClick::no listener!!");
                }
            }
        });
        //
        Button startGuidedSessionBtn = mRootView.findViewById(R.id.start_guided_session_btn);
        startGuidedSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null) {
                    mListener.startAudioSession();
                }else{
                    Log.e(TAG, "startGuidedSessionBtn.onClick::no listener!!");
                }
            }
        });
        //
        Button viewRewardsBtn = mRootView.findViewById(R.id.view_rewards_btn);
        viewRewardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null) {
                    mListener.navigateToRewards();
                }else{
                    Log.e(TAG, "viewRewardsBtn.onClick::no listener!!");
                }
            }
        });

        //TODO: temp for testing rewards attribution
        Button level1 = mRootView.findViewById(R.id.testlevel1);
        level1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAttributeRewards(1);
            }
        });
        Button level2 = mRootView.findViewById(R.id.testlevel2);
        level2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAttributeRewards(2);
            }
        });
        Button level3 = mRootView.findViewById(R.id.testlevel3);
        level3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAttributeRewards(3);
            }
        });
        Button level4 = mRootView.findViewById(R.id.testlevel4);
        level4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAttributeRewards(4);
            }
        });
        Button level5 = mRootView.findViewById(R.id.testlevel5);
        level5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testAttributeRewards(5);
            }
        });
        Button resetRewards = mRootView.findViewById(R.id.test_clear_rewards);
        resetRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testResetRewards();
            }
        });
        Button escapeRewards = mRootView.findViewById(R.id.test_escape_rewards);
        escapeRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numberOfDaysWithoutSession = (int) (Math.random() * 30);
                int numberOfDaysAlreadyPunished = Math.max(numberOfDaysWithoutSession - (int) (Math.random() * 30), 0);
                if(mListener!=null) {
                    mListener.testEscapeRewards(numberOfDaysWithoutSession, numberOfDaysAlreadyPunished);
                }else{
                    Log.e(TAG, "viewRewardsBtn.onClick::no listener!!");
                }
            }
        });

        //TODO:temp for testing rewards attribution

        return mRootView;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }



    //TODO: temp for testing rewards attribution

    private void testResetRewards(){
        Log.d(TAG, "testResetRewards");
        if(mListener!=null) {
            mListener.testResetRewards();
        }else{
            Log.e(TAG, "viewRewardsBtn.onClick::no listener!!");
        }
    }
    private void testAttributeRewards(int level){
        int fakeCurrentStreak = (int) (Math.random()*35);
        int[] rewardChances = Critter.REWARD_CHANCE_LEVEL_1;
        if(fakeCurrentStreak > Critter.REWARD_STREAK_LEVEL_2) rewardChances = Critter.REWARD_CHANCE_LEVEL_2;
        if(fakeCurrentStreak > Critter.REWARD_STREAK_LEVEL_3) rewardChances = Critter.REWARD_CHANCE_LEVEL_3;
        if(fakeCurrentStreak > Critter.REWARD_STREAK_LEVEL_4) rewardChances = Critter.REWARD_CHANCE_LEVEL_4;
        if(fakeCurrentStreak > Critter.REWARD_STREAK_LEVEL_5) rewardChances = Critter.REWARD_CHANCE_LEVEL_5;
        Log.d(TAG, "showRewardObtainedScreen:: level = " + level);
        if(mListener!=null) {
            mListener.testAttributeRewards(level, rewardChances);
        }else{
            Log.e(TAG, "viewRewardsBtn.onClick::no listener!!");
        }
    }
    //TODO: temp for testing rewards attribution

////////////////////////////////////////
//customizing buttons' text

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setStartSessionBtnText();
    }

    private void setStartSessionBtnText() {
        String durationPreferenceKey = getString(R.string.pref_duration_key);
        long defaultDuration = Long.parseLong(getString(R.string.default_duration));
        long durationValue = PreferenceManager.getDefaultSharedPreferences(mContext).getLong(durationPreferenceKey, defaultDuration);
        //String startSessionBtnTxt = String.format(getString(R.string.start_session_btn_txt), duration);
        String durationString = getString(R.string.launch_session_for) + " \n";
        durationString += TimeOperations.convertMillisecondsToHoursAndMinutesString(
                durationValue,
                getString(R.string.generic_string_HOURS),
                getString(R.string.generic_string_MINUTES),
                true
        );
        //
        String infiniteSessionPreferenceKey = getString(R.string.pref_switch_infinite_session_key);
        boolean infiniteSession = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(infiniteSessionPreferenceKey, false);
        if(infiniteSession){
            durationString += "\n" + getString(R.string.start_session_btn_or_more_txt);
        }
        //
        Button startSessionBtn = mRootView.findViewById(R.id.start_session_btn);
        startSessionBtn.setText(durationString);
    }

////////////////////////////////////////
//Listener interface
    public interface HomePageButtonsFragmentListener {
        void navigateToStats();
        void navigateToRewards();
        void startAudioSession();
        void startTimedSession();
        void testEscapeRewards(int numberOfDaysWithoutSession, int numberOfDaysAlreadyPunished);
        void testAttributeRewards(int level, int[] rewardsChances);
        void testResetRewards();
    }

}
