package fr.shining_cat.meditappli;

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

import fr.shining_cat.meditappli.data.MeditAppliRepository;
import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;


public class SessionActivity extends AppCompatActivity
                            implements  PreRecordFragment.FragmentPreRecordListener,
                                        SessionInProgressFragment.SessionInProgressFragmentListener,
                                        PostRecordFragment.FragmentPostRecordListener,
                                        MeditAppliRepository.MeditAppliRepoListener{

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
            showFragmentPostRecord();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mHasGoneOffscreen = true;
        Log.d(TAG, "onSaveInstanceState:mHasGoneOffscreen = " + mHasGoneOffscreen);
        outState.putBoolean(HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY, mHasGoneOffscreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHasGoneOffscreen = false;
        Log.d(TAG, "onResume:mHasGoneOffscreen = " + mHasGoneOffscreen + " / mSessionHasEnded = " + mSessionHasEnded);
        if(mSessionHasEnded){
            showFragmentPostRecord();
        }
    }

////////////////////////////////////////
//changing on back navigation to pause session if running
   @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        FragmentManager fragmentManager = getSupportFragmentManager();
        SessionInProgressFragment currentSessionInProgress = (SessionInProgressFragment) fragmentManager.findFragmentByTag(SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
        PostRecordFragment postRecordFragment = (PostRecordFragment) fragmentManager.findFragmentByTag(PostRecordFragment.FRAGMENT_POST_RECORD_NORMAL_TAG);
        if(currentSessionInProgress!=null && currentSessionInProgress.isVisible()) { //session running will override onBack behaviour to pause session rather than going back
            if(currentSessionInProgress.isNormalBackNavAllowed()) {
                super.onBackPressed();
            }else{
                currentSessionInProgress.onOverrideBackNav();
            }
        }else if(postRecordFragment != null && postRecordFragment.isVisible()){
            postRecordFragment.properCancelling();
        }else {
            super.onBackPressed();
        }
    }
////////////////////////////////////////
//PreRecord fragment callbacks
    @Override
    public void onCancelFragmentPreRecord() {
        Log.d(TAG, "onCancelFragmentPreRecord");
        finish();
    }

    @Override
    public void onValidateFragmentPreRecord(MoodRecord moodRecord) {
        mStartMood = moodRecord;
        startSession();
    }
    private void startSession() {
        Log.d(TAG, "startSession");
        mSessionHasEnded = false;
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
//SessionInProgress fragment callbacks
    @Override
    public void onAbortSessionInProgressFragment() {
        finish();
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
        //
        if(!mHasGoneOffscreen) {
            if (prefActiveStatsCollect) {
                showFragmentPostRecord();
            } else {
                finish();
            }
        }else{
            //activity is on screen, normal behaviour will be displaying the post-record dialog (if prefs allow it)
        }
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
    }
////////////////////////////////////////
//PostRecord fragment callbacks
   @Override
    public void onValidateFragmentPostRecord(MoodRecord mood) {
        Log.d(TAG, mood.toString());
        mEndMood = mood;
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.insertWithMoods(mStartMood, mEndMood, this);
        //remove sticky notification set up by SessionInProgressFragment
        NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
           notifyManager.cancel(SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
        }else{
           Log.e(TAG, "removeStickyNotification::notifyManager is null!!");
        }
        //TODO : this is where we will plug gamification
        finish();
    }

    @Override
    public void onCancelFragmentPostRecord(Boolean isManualEntry) {//here there is not going back : if the user confirms the cancelling of postrecord fragment, the session is discarded
        if(!isManualEntry) {
            //remove sticky notification set up by SessionInProgressFragment
            NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notifyManager != null) {
                notifyManager.cancel(SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
            } else {
                Log.e(TAG, "removeStickyNotification::notifyManager is null!!");
            }
            finish();
        }else{
            Log.e(TAG, "onCancelFragmentPostRecord::isManualEntry should always be false in this context");
        }
    }



////////////////////////////////////////
//MeditAppliRepository callbacks
    @Override
    public void onInsertOneSessionRecordComplete(long result) {
        Toast.makeText(this, R.string.insert_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    //not used here
    @Override
    public void ondeleteOneSessionRecordComplete(int result) {}
    @Override
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {}
    @Override
    public void ondeleteAllSessionsRecordsComplete(int result) {}
    @Override
    public void onInsertMultipleSessionsRecordsComplete(Long[] result) {}
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {}


}
