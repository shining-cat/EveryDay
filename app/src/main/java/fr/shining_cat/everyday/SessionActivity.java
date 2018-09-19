package fr.shining_cat.everyday;

import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import fr.shining_cat.everyday.analytics.GeneralStats;
import fr.shining_cat.everyday.data.EveryDayRewardsDataRepository;
import fr.shining_cat.everyday.data.EveryDaySessionsDataRepository;
import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.dialogs.DialogFragmentManualDurationEntry;
import fr.shining_cat.everyday.utils.UiUtils;

public class SessionActivity extends AppCompatActivity
                            implements  PreRecordFragment.FragmentPreRecordListener,
                                        SessionInProgressFragment.SessionInProgressFragmentListener,
                                        PostRecordFragment.PostRecordFragmentListener,
                                        EveryDaySessionsDataRepository.EveryDaySessionsRepoListener,
                                        DialogFragmentManualDurationEntry.DialogFragmentManualDurationEntryListener,
                                        RewardsObtainedFragment.FragmentRewardObtainedListener,
                                        EveryDayRewardsDataRepository.EveryDayRewardsRepoListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY = "SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY";

    private final static String HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY = "store if app has gone off screen or not";

    private MoodRecord mStartMood;
    private MoodRecord mEndMood;
    private long mDuration;
    private int mPausesCount;
    private int mRealVsPlannedDuration;
    private String mGuideMp3;
    private boolean mHasGoneOffscreen;
    private boolean mSessionHasEnded;
    private boolean mSessionPostRecordingHasStarted;
    private int mRewardLevel;
    private int[] mRewardChances;



////////////////////////////////////////
//This activity is responsible for running a session (DialogFragmentPreRecord, then SessionInProgressFragment, then DialogFragmentPostRecord) with possibility to pause the session
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mHasGoneOffscreen = false;
        //
        setContentView(R.layout.activity_session);
        //
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefActiveStatsCollect = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
        if (prefActiveStatsCollect) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            PreRecordFragment preRecordFragment = PreRecordFragment.newInstance(false);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.session_activity_fragments_holder, preRecordFragment, PreRecordFragment.FRAGMENT_PRE_RECORD_NORMAL_TAG);
            fragmentTransaction.commit();
        } else {
            startSession();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mHasGoneOffscreen = false;
        Log.d(TAG, "onNewIntent:mHasGoneOffscreen = " + mHasGoneOffscreen);
        boolean comingFromSessionInProgressNotificationSessionIsRunning = intent.getBooleanExtra(SessionInProgressFragment.NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS, false);
        boolean comingFromSessionInProgressNotificationSessionHasEnded = intent.getBooleanExtra(SessionInProgressFragment.NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED, false);
        if(comingFromSessionInProgressNotificationSessionIsRunning) { //activity started by user's click on "session running" notification : do not start over!
            Log.d(TAG, "onNewIntent session is running");
            //RESUME SESSION (running or on pause), not start again
            FragmentManager fragmentManager = getSupportFragmentManager();
            SessionInProgressFragment currentSessionInProgress = (SessionInProgressFragment) fragmentManager.findFragmentByTag(SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
            if(currentSessionInProgress!=null && currentSessionInProgress.isVisible()) {
                currentSessionInProgress.comingBackToSession();
            }
            }else if(comingFromSessionInProgressNotificationSessionHasEnded){//activity started by user's click on "session has ended" notification : do not start over!
                Log.d(TAG, "onNewIntent session has ended");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean prefActiveStatsCollect = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
                boolean prefActiveRewardsCollect = prefs.getBoolean(getString(R.string.pref_rewards_collector_active_key), Boolean.valueOf(getString(R.string.default_rewards_collector_active)));
                if(prefActiveStatsCollect){
                    showFragmentPostRecord();
                }else if(prefActiveRewardsCollect){
                    prepareRewards(mDuration);
                }else {
                    quitSessionActivity();
                }
                showFragmentPostRecord();
            }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        mHasGoneOffscreen = true;
        outState.putBoolean(HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY, mHasGoneOffscreen);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHasGoneOffscreen = false;
        Log.d(TAG, "onResume:mHasGoneOffscreen = " + mHasGoneOffscreen + " / mSessionHasEnded = " + mSessionHasEnded);
        if(mSessionHasEnded && !mSessionPostRecordingHasStarted){
            showFragmentPostRecord();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        UiUtils.removeStickyNotification(this, SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
    }

////////////////////////////////////////
//changing on back navigation to pause session if running
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        FragmentManager fragmentManager = getSupportFragmentManager();
        SessionInProgressFragment currentSessionInProgress = (SessionInProgressFragment) fragmentManager.findFragmentByTag(SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
        PostRecordFragment postRecordFragment = (PostRecordFragment) fragmentManager.findFragmentByTag(PostRecordFragment.FRAGMENT_POST_RECORD_NORMAL_TAG);
        RewardsObtainedFragment rewardsObtainedFragment =  (RewardsObtainedFragment) fragmentManager.findFragmentByTag(RewardsObtainedFragment.FRAGMENT_REWARD_OBTAINED_TAG);
        if(currentSessionInProgress!=null && currentSessionInProgress.isVisible()) { //session running will override onBack behaviour to pause session rather than going back
            if(currentSessionInProgress.isNormalBackNavAllowed()) {
                super.onBackPressed();
            }else{
                currentSessionInProgress.onOverrideBackNav();
            }
        }else if(postRecordFragment != null && postRecordFragment.isVisible()){
            postRecordFragment.properCancelling();
        }else if(rewardsObtainedFragment != null && rewardsObtainedFragment.isVisible()){
            rewardsObtainedFragment.onBackNavClicked();
        }else {
            super.onBackPressed();
        }
    }
////////////////////////////////////////
//PreRecord fragment callbacks
    @Override
    public void onCancelFragmentPreRecord() {
        Log.d(TAG, "onCancelFragmentPreRecord");
        quitSessionActivity();
    }

    @Override
    public void onValidateFragmentPreRecord(MoodRecord moodRecord) {
        mStartMood = moodRecord;
        startSession();
    }
    private void startSession() {
        Log.d(TAG, "startSession");
        mSessionHasEnded = false;
        mSessionPostRecordingHasStarted = false;
        //
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SessionInProgressFragment sessionInProgressFragment = SessionInProgressFragment.newInstance();
        //check if we were given an audio file URI to be used as session support
        if(getIntent() != null){
            Uri audioContentUri = getIntent().getParcelableExtra(SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY);
            if(audioContentUri!=null) {
                //give the audio uri to sessionInProgressFragment
                sessionInProgressFragment.setSessionAudioSupport(audioContentUri);
            }
        }
        //
        fragmentTransaction.replace(R.id.session_activity_fragments_holder, sessionInProgressFragment, SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
        fragmentTransaction.commit();
    }

////////////////////////////////////////
//quit SessionActivity (this)
    private void quitSessionActivity(){
        finish();
    }
////////////////////////////////////////
//SessionInProgress fragment callbacks

    @Override
    public void askForManualAudioDurationEntry() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragmentManualDurationEntry dialogFragmentManualDurationEntry = DialogFragmentManualDurationEntry.newInstance();
        dialogFragmentManualDurationEntry.show(fm, DialogFragmentManualDurationEntry.DIALOG_FRAGMENT_MANUAL_DURATION_ENTRY_TAG);
    }

    @Override
    public void onFinishSessionInProgressFragment(long duration, int pausesCount, int realVsPlannedDuration, String guideMp3) {
        Log.d(TAG, "onFinishSessionInProgressFragment:mHasGoneOffscreen = " + mHasGoneOffscreen);
        mSessionHasEnded = true;
        mDuration = duration;
        mPausesCount = pausesCount;
        mRealVsPlannedDuration = realVsPlannedDuration;
        mGuideMp3 = guideMp3;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefActiveStatsCollect = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
        boolean prefActiveRewardsCollect = prefs.getBoolean(getString(R.string.pref_rewards_collector_active_key), Boolean.valueOf(getString(R.string.default_rewards_collector_active)));
        //
        if(!mHasGoneOffscreen) {
            if(prefActiveStatsCollect){
                showFragmentPostRecord();
            }else if(prefActiveRewardsCollect){
                prepareRewards(mDuration);
            }else {
                quitSessionActivity();
            }
        }else{
            //activity is on screen, normal behaviour will be displaying the post-record dialog (if prefs allow it)
        }
    }

////////////////////////////////////////
//DialogFragmentManualDurationEntry fragment callbacks

    @Override
    public void onManualAudioDurationEntryValidate(long manualAudioDuration) {
        //check that the onscreen fragment is actually SessionInProgressFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        SessionInProgressFragment currentSessionInProgress = (SessionInProgressFragment) fragmentManager.findFragmentByTag(SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
        if(currentSessionInProgress!=null && currentSessionInProgress.isVisible()) {
            currentSessionInProgress.onManualAudioDurationEntryCallback(manualAudioDuration);
        }else{
            Log.e(TAG, "onManualAudioDurationEntryValidate::got manual duration value but SessionInProgressFragment is not there!!");
        }
    }

    @Override
    public void onManualAudioDurationEntryCancel() {
        Log.d(TAG, "onManualAudioDurationEntryCancel");
        quitSessionActivity();
    }

////////////////////////////////////////
//PRE RECORD
    private void showFragmentPreRecord(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        PreRecordFragment preRecordFragment = PreRecordFragment.newInstance(false);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.session_activity_fragments_holder, preRecordFragment, PreRecordFragment.FRAGMENT_PRE_RECORD_NORMAL_TAG);
        fragmentTransaction.commit();
    }


////////////////////////////////////////
//POST RECORD
    private void showFragmentPostRecord(){
        Log.d(TAG, "showFragmentPostRecord");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PostRecordFragment postRecordFragment = PostRecordFragment.newInstance(false, mDuration, mPausesCount, mRealVsPlannedDuration, mGuideMp3, mStartMood.getTimeOfRecord());
        fragmentTransaction.replace(R.id.session_activity_fragments_holder, postRecordFragment, PostRecordFragment.FRAGMENT_POST_RECORD_NORMAL_TAG);
        fragmentTransaction.commit();
        mSessionPostRecordingHasStarted = true;
    }
////////////////////////////////////////
//PostRecord fragment callbacks
   @Override
    public void onValidatePostRecordFragment(MoodRecord mood) {
        Log.d(TAG, mood.toString());
        UiUtils.hideSoftKeyboard(this);
        mEndMood = mood;
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.insertWithMoods(mStartMood, mEndMood, this);
        UiUtils.removeStickyNotification(this, SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefActiveRewardsCollect = prefs.getBoolean(getString(R.string.pref_rewards_collector_active_key), Boolean.valueOf(getString(R.string.default_rewards_collector_active)));
        if(prefActiveRewardsCollect){
            prepareRewards(mEndMood.getSessionRealDuration());
        }else {
            quitSessionActivity();
        }
    }

    @Override
    public void onCancelPostRecordFragment(Boolean isManualEntry) {//here there is no going back : if the user confirms the cancelling of postrecord fragment, the session is discarded
        UiUtils.hideSoftKeyboard(this);
        if(!isManualEntry) {
            UiUtils.removeStickyNotification(this, SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
            quitSessionActivity();
        }else{
            Log.e(TAG, "onCancelPostRecordFragment::isManualEntry should always be false in this context");
        }
    }

////////////////////////////////////////
//Rewards acquisition
    private void prepareRewards(long sessionDuration){
        mRewardLevel = Critter.REWARD_LEVEL_1;
        if(sessionDuration > Critter.REWARD_DURATION_LEVEL_2){
            mRewardLevel = Critter.REWARD_LEVEL_2;
            if(sessionDuration > Critter.REWARD_DURATION_LEVEL_3){
                mRewardLevel = Critter.REWARD_LEVEL_3;
                if(sessionDuration > Critter.REWARD_DURATION_LEVEL_4){
                    mRewardLevel = Critter.REWARD_LEVEL_4;
                    if(sessionDuration > Critter.REWARD_DURATION_LEVEL_5){
                        mRewardLevel = Critter.REWARD_LEVEL_5;
                    }
                }
            }
        }
        Log.d(TAG, "prepareRewards::sessionDuration = " + sessionDuration + " / mRewardLevel = " + mRewardLevel);
        //
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getAllSessionsRecordsInBunch(this);
    }

    private void showFragmentRewardObtained(){
        Log.d(TAG, "showFragmentRewardObtained");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RewardsObtainedFragment rewardsObtainedFragment = RewardsObtainedFragment.newInstance(mRewardLevel, mRewardChances);
        fragmentTransaction.replace(R.id.session_activity_fragments_holder, rewardsObtainedFragment, RewardsObtainedFragment.FRAGMENT_REWARD_OBTAINED_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onValidateFragmentRewardObtained(List<Reward> rewards) {
        Log.d(TAG, "onValidateFragmentRewardObtained:: rewards size = " + rewards.size());
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.updateMultipleReward(rewards, this);
    }

////////////////////////////////////////
//EveryDaySessionsDataRepository callbacks
    @Override
    public void onInsertOneSessionRecordComplete(long result) {
        Toast.makeText(this, R.string.insert_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {
        int currentStreak = GeneralStats.getCurrentStreak(allSessions);
        mRewardChances = Critter.REWARD_CHANCE_LEVEL_1;
        if(currentStreak > Critter.REWARD_STREAK_LEVEL_2) mRewardChances = Critter.REWARD_CHANCE_LEVEL_2;
        if(currentStreak > Critter.REWARD_STREAK_LEVEL_3) mRewardChances = Critter.REWARD_CHANCE_LEVEL_3;
        if(currentStreak > Critter.REWARD_STREAK_LEVEL_4) mRewardChances = Critter.REWARD_CHANCE_LEVEL_4;
        if(currentStreak > Critter.REWARD_STREAK_LEVEL_5) mRewardChances = Critter.REWARD_CHANCE_LEVEL_5;
        Log.d(TAG, "onGetAllSessionsNotLiveComplete::currentStreak = " + currentStreak + " / mRewardChances = " + mRewardChances);
        showFragmentRewardObtained();
    }
    @Override
    public void onGetLatestRecordedSessionDateComplete(long latestSessionRecordedDate) {

    }
    //not used here
    @Override
    public void ondeleteOneSessionRecordComplete(int result) {}
    @Override
    public void ondeleteAllSessionsRecordsComplete(int result) {}
    @Override
    public void onInsertMultipleSessionsRecordsComplete(Long[] result) {}
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {}


////////////////////////////////////////
//EveryDayRewardsDataRepository callbacks
    @Override
    public void onUpdateMultipleRewardComplete(int result) {
        Toast.makeText(this, R.string.rewards_obtained_recorded_message, Toast.LENGTH_SHORT).show();
        quitSessionActivity();
    }
    @Override
    public void onInsertMultipleRewardsComplete(Long[] result) {}
    @Override
    public void onGetNumberOfRowsComplete(Integer result) {}
    @Override
    public void onGetNumberOfPossibleRewardsForLevelComplete(int levelQueried, Integer result) {}
    @Override
    public void onGetNumberOfActiveRewardsForLevelComplete(int levelQueried, Integer result) {}
    @Override
    public void onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete(int levelQueried, Integer result) {}
    @Override
    public void onInsertOneRewardComplete(long result) {}
    @Override
    public void onUpdateOneRewardComplete(int result) {}
    @Override
    public void ondeleteOneRewardComplete(int result) {}
    @Override
    public void ondeleteAllRewardComplete(int result) {}
}
