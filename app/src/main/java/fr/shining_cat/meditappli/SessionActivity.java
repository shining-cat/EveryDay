package fr.shining_cat.meditappli;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import fr.shining_cat.meditappli.data.MeditAppliRepository;
import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;
import fr.shining_cat.meditappli.dialogs.DialogFragmentPostRecord;
import fr.shining_cat.meditappli.dialogs.DialogFragmentPreRecord;


public class SessionActivity extends AppCompatActivity
                            implements  DialogFragmentPreRecord.DialogFragmentPreRecordListener,
                                        SessionInProgressFragment.SessionInProgressFragmentListener,
                                        DialogFragmentPostRecord.DialogFragmentPostRecordListener,
                                        MeditAppliRepository.MeditAppliRepoListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY = "SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY";

    private final static String HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY = "store if app has gone off screen or not";

    private View mControlsView;
    private View mContentView;
    private MoodRecord mStartMood;
    private MoodRecord mEndMood;
    long mDuration;
    int mPausesCount;
    int mRealVsPlannedDuration;
    String mGuideMp3;
    boolean mHasGoneOffscreen;

////////////////////////////////////////
//This activity is responsible for running a session (DialogFragmentPreRecord, then SessionInProgressFragment, then DialogFragmentPostRecord) with possibility to pause the session
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        mHasGoneOffscreen = false;
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.activity_session);
        //
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.session_activity_fragments_holder);
        //
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefActiveStatsCollect = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
         //normal start
        Log.d(TAG, "onCreate::normal start");
        if (prefActiveStatsCollect) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragmentPreRecord dialogFragmentPreRecord = DialogFragmentPreRecord.newInstance(false);
            dialogFragmentPreRecord.show(fm, DialogFragmentPreRecord.DIALOG_FRAGMENT_PRE_RECORD_NORMAL_TAG);
        } else {
            startSession();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        mHasGoneOffscreen = false;
        boolean comingFromSessionInProgressNotificationSessionIsRunning = intent.getBooleanExtra(SessionInProgressFragment.NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS, false);
        boolean comingFromSessionInProgressNotificationSessionHasEnded = intent.getBooleanExtra(SessionInProgressFragment.NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED, false);
        if(comingFromSessionInProgressNotificationSessionIsRunning) { //activity started by user's click on "session running" notification : do not start over!
            Log.d(TAG, "onNewIntent session is running");
            //RESUME SESSION (running or on pause), not start again
        }else if(comingFromSessionInProgressNotificationSessionHasEnded){//activity started by user's click on "session has ended" notification : do not start over!
            Log.d(TAG, "onNewIntent session has ended");
            showDialogFragmentPostRecord();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mHasGoneOffscreen = true;
        outState.putBoolean(HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY, mHasGoneOffscreen);
    }


////////////////////////////////////////
//changing on back navigation to pause session if running
   @Override
    public void onBackPressed() {
        Log.d(TAG, "onOverrideBackNav");
        FragmentManager fragmentManager = getSupportFragmentManager();
        SessionInProgressFragment currentSessionInProgress = (SessionInProgressFragment) fragmentManager.findFragmentByTag(SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
        if(currentSessionInProgress!=null && currentSessionInProgress.isVisible()) { //session running will override onBack behaviour to pause session rather than going back
            if(currentSessionInProgress.isNormalBackNavAllowed()) {
                super.onBackPressed();
            }else{
                currentSessionInProgress.onOverrideBackNav();
            }
        }else {
            super.onBackPressed();
        }
    }
////////////////////////////////////////
//PreRecord Dialog fragment callbacks
    @Override
    public void onCancelDialogFragmentPreRecord() {
        Log.d(TAG, "onCancelDialogFragmentPreRecord");
        finish();
    }
    @Override
    public void onValidateDialogFragmentPreRecord(MoodRecord mood) {
        //Log.d(TAG, mood.toString());
        mStartMood = mood;
        startSession();
    }
    private void startSession() {
        Log.d(TAG, "startSession");
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
        fragmentTransaction.add(R.id.session_activity_fragments_holder, sessionInProgressFragment, SessionInProgressFragment.SESSIONINPROGRESSFRAGMENT_TAG);
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
        Log.d(TAG, "onFinishSessionInProgressFragment");
        mDuration = duration;
        mPausesCount = pausesCount;
        mRealVsPlannedDuration = realVsPlannedDuration;
        mGuideMp3 = guideMp3;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefActiveStatsCollect = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
        //
        if(!mHasGoneOffscreen) {
            if (prefActiveStatsCollect) {
                showDialogFragmentPostRecord();
            } else {
                finish();
            }
        }else{
            //activity is on screen, normal behaviour will be displaying the post-record dialog (if prefs allow it)
        }
    }

////////////////////////////////////////
//POST RECORD
    private void showDialogFragmentPostRecord(){
        Log.d(TAG, "showDialogFragmentPostRecord");
        FragmentManager fm = getSupportFragmentManager();
        DialogFragmentPostRecord dialogFragmentPostRecord = DialogFragmentPostRecord.newInstance(false, mDuration, mPausesCount, mRealVsPlannedDuration, mGuideMp3, mStartMood.getTimeOfRecord());
        dialogFragmentPostRecord.show(fm, DialogFragmentPostRecord.DIALOG_FRAGMENT_POST_RECORD_NORMAL_TAG);
    }
////////////////////////////////////////
//PostRecord Dialog fragment callbacks
   @Override
    public void onValidateDialogFragmentPostRecord(MoodRecord mood) {
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
    public void onCancelDialogFragmentPostRecord() {
        finish();
    }

    @Override
    public void restartDialogFragmentPostRecord() {
        showDialogFragmentPostRecord();
    }

    @Override
    public void goBackToDialogFragmentPreRecord() {
        //won't happen here (only called for manual entry)
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

////////////////////////////////////////
//AUTO-GENERATED CODE FOR A FULL SCREEN ACTIVITY AUTO-HIDING STATUS AND ACTION BARS (nothing custom here)
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
   private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
