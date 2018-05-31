package fr.shining_cat.meditappli;

import android.annotation.SuppressLint;
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

//TODO: peut-etre remplacer les dialogFragments par des fragments pour optimiser... skipped frames à chaque ouverture...
public class SessionActivity extends AppCompatActivity
                            implements  DialogFragmentPreRecord.DialogFragmentPreRecordListener,
                                        SessionInProgressFragment.SessionInProgressFragmentListener,
                                        DialogFragmentPostRecord.DialogFragmentPostRecordListener,
                                        MeditAppliRepository.MeditAppliRepoListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY = "SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY";

    private final static String HAS_GONE_OFF_SCREEN_SAVED_INSTANCE_STATE_KEY = "store if app has gone off screen or not";

    private View mDecorView;
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
            FragmentManager fm = getSupportFragmentManager();
            DialogFragmentPreRecord dialogFragmentPreRecord = DialogFragmentPreRecord.newInstance(false);
            dialogFragmentPreRecord.show(fm, DialogFragmentPreRecord.DIALOG_FRAGMENT_PRE_RECORD_NORMAL_TAG);
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
            showDialogFragmentPostRecord();
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
            showDialogFragmentPostRecord();
        }
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
        //remove sticky notification set up by SessionInProgressFragment
        NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.cancel(SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
        }else{
            Log.e(TAG, "removeStickyNotification::notifyManager is null!!");
        }
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

}
