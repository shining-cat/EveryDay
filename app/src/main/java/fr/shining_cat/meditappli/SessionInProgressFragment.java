package fr.shining_cat.meditappli;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.IOException;

import fr.shining_cat.meditappli.utils.TimeOperations;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static fr.shining_cat.meditappli.broadcastreceivers.AlarmSetterBroadcastReceiver.MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY;


public class SessionInProgressFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SESSIONINPROGRESSFRAGMENT_TAG = "SessionInProgressFragment-tag";

    private static final int STICKY_SESSION_RUNNING_NOTIFICATION_INTENT_ID = 713;
    public static final int STICKY_SESSION_RUNNING_NOTIFICATION_ID = 715;
    public static final String NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS = "This intent was launched by the notification set by SessionInProgress, session is running";
    public static final String NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED = "This intent was launched by the notification set by SessionInProgress, session has ended";

    private SessionInProgressFragmentListener mListener;

    private final String PLAY_START_SESSION_SOUND = "play start session sound if one is set to";
    private final String PLAY_END_SESSION_SOUND = "play end session sound if one is set to";

    private final String SESSION_STATE_NORMAL_RUNNING = "session is running up to planned duration";
    private final String SESSION_STATE_OVERSTAY_RUNNING = "session is running over planned duration";
    private final String SESSION_STATE_PAUSED = "session is paused";
    private final String SESSION_STATE_ENDED = "session has reached planned duration";

    private Context mContext;
    private View mRootView;
    private TextView mPauseInstructionTxtView;
    private TextView mCountdownTxtView;
    private TextView mRemainingDurationTxtView;
    private View mBubbleCounter;
    private Animation mAnimPulse;
    private Handler mHidePauseInstructionHandler;
    private CountDownTimer mStartSessionCountDown;
    private boolean mStartSessionCountDownIsRunning;
    private CountDownTimer mRunSessionCountDown;
    private boolean mRunSessionCountDownIsRunning;
    private CountDownTimer mRunSessionCountUp;
    private boolean mRunSessionCountUpIsRunning;
    private long mSessionRemainingDuration;
    private long mPlannedDuration;
    private long mElapsedTime;
    private int mPausesCount;
    private String mGuideMp3;
    private int mPreviousInterruptionFilter;
    private boolean mDNDIsOverridden;
    private long mDelayBeforeDND;
    private boolean mIsNormalBackNavAllowed;
    private AlertDialog mPauseAlertDialog;
    private boolean sessionIsFinished;
    private Uri mAudioContentUri;
    int mAudioFileDuration;
    MediaPlayer mMediaPlayer;

    //TODO: center bubbleCounter : position is not stable on screen rotation, probably because of the status bar
////////////////////////////////////////
//Fragment shown while session is running (or paused)
    public SessionInProgressFragment() {
        // Required empty public constructor
    }

    public static SessionInProgressFragment newInstance() {
        return new SessionInProgressFragment();
    }

    public void setSessionAudioSupport(Uri audioContentUri){
        Log.d(TAG, "setSessionAudioSupport::audioContentUri = " + audioContentUri);
        if(audioContentUri != null) {
            mAudioContentUri = audioContentUri;
        }else{
            Log.e(TAG, "setSessionAudioSupport::audioContentUri IS NULL !!");
        }
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
        if (context instanceof SessionInProgressFragmentListener) {
            mListener = (SessionInProgressFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SessionInProgressFragmentListener");
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_session_in_progress, container, false);
        //
        mPauseInstructionTxtView = mRootView.findViewById(R.id.session_in_progress_pause_instruction_txtview);
        mCountdownTxtView = mRootView.findViewById(R.id.session_in_progress_countdown_txtview);
        mBubbleCounter = mRootView.findViewById(R.id.bubble_counter);
        mRemainingDurationTxtView = mRootView.findViewById(R.id.session_running_countdown_txtview);
        mRemainingDurationTxtView.setVisibility(View.INVISIBLE);
        // countdowntimer dispatches ontick on start (before the first update cycle has even begun), so we have to offset the start value of mElapsedTime
        mElapsedTime = - Long.parseLong(getString(R.string.run_session_countdown_update_interval));
        mPausesCount = 0;
        mStartSessionCountDownIsRunning = false;
        mRunSessionCountDownIsRunning = false;
        mRunSessionCountUpIsRunning = false;
        mDNDIsOverridden = false;
        mPreviousInterruptionFilter = -1;
        mIsNormalBackNavAllowed = true;
        mDelayBeforeDND = Long.parseLong(getString(R.string.delay_before_DND_to_let_ringtone_time_to_play));
        mGuideMp3 = getString(R.string.guided_session_default_filename);
        //if we were passed an audio file URI for a guided session, retrieve name and duration
        if(mAudioContentUri != null){
            //duration
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this.getActivity(), mAudioContentUri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            mAudioFileDuration = Integer.parseInt(durationStr);
            //file name
            Cursor returnCursor = this.getActivity().getContentResolver().query(mAudioContentUri, null, null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                mGuideMp3 = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
            Log.d(TAG, "onAttach::mGuideMp3 = " + mGuideMp3 + " / mAudioFileDuration = " + mAudioFileDuration);
        }
        //
        prepareBubbleTimerAnimation();
        prepareStart();
        return mRootView;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
        destroyBubbleTimerAnimation();
        destroyAllCountDownTimers();
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if(mHidePauseInstructionHandler!=null){
            mHidePauseInstructionHandler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void prepareStart(){
        showBrieflyHowToPauseInstruction();
        startCountdownBeforeBeginningSession();
        handleKeepScreenOn(true);
        handleAlarmPostponing();
    }
////////////////////////////////////////
//if user has set a reminder alarm in saved Sharedprefs, we cancel it for the current day when he starts a session
    private void handleAlarmPostponing() {
        Log.d(TAG, "handleAlarmPostponing");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean activeNotification = prefs.getBoolean(getString(R.string.pref_active_notification_key), Boolean.valueOf(getString(R.string.default_notification_active)));
        if(activeNotification) {
            Intent setAlarmIntent = new Intent();
            setAlarmIntent.setAction(MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY);
            getActivity().sendBroadcast(setAlarmIntent);
        }
    }

////////////////////////////////////////
//The session is paused only on long click to prevent accidental interruptions => on simple click we show briefly a reminder
    private void showBrieflyHowToPauseInstruction() {
        mPauseInstructionTxtView.setVisibility(View.VISIBLE);
        mHidePauseInstructionHandler = new Handler(Looper.getMainLooper());
        int pauseInstructionHideDelay = Integer.parseInt(getString(R.string.pause_instruction_hide_delay));
        mHidePauseInstructionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hidePauseInstruction();
            }
        }, pauseInstructionHideDelay);
    }
    private void hidePauseInstruction() {
        mPauseInstructionTxtView.setVisibility(View.INVISIBLE);
    }

////////////////////////////////////////
//There is a fixed duration countdown before a session really starts : R.string.start_session_countdown_delay
    private void startCountdownBeforeBeginningSession() {
        Log.d(TAG, "startCountdownBeforeBeginningSession");
        mRemainingDurationTxtView.setVisibility(View.INVISIBLE);
        int startSessionCountdown = Integer.parseInt(getString(R.string.start_session_countdown_delay));
        mCountdownTxtView.setText(String.format(getString(R.string.start_countdown), startSessionCountdown));
        mCountdownTxtView.setVisibility(View.VISIBLE);
        mStartSessionCountDown = new CountDownTimer(startSessionCountdown, 500) {
            public void onTick(long millisUntilFinished) {
                mCountdownTxtView.setText(String.format(getString(R.string.start_countdown),(1 + millisUntilFinished / 1000)));
            }
            public void onFinish() {
                mCountdownTxtView.setText(String.format(getString(R.string.start_countdown),0));
                mCountdownTxtView.setVisibility(View.INVISIBLE);
                startSession();
            }
        }.start();
        mStartSessionCountDownIsRunning = true;
    }

////////////////////////////////////////
//Session STARTS
    private void startSession() {
        Log.d(TAG, "startSession");
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBrieflyHowToPauseInstruction();
            }
        });
        mRootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pauseSession();
                return true;
            }
        });
        playSessionSound(PLAY_START_SESSION_SOUND);
        handleVibrateDevice();
        launchBubbleTimer();
        sessionIsFinished = false;
        mIsNormalBackNavAllowed = false;
    }

////////////////////////////////////////
//Session RUNS
    private void launchBubbleTimer() {
        startBubbleTimerAnimation();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //
        int defaultDuration = Integer.parseInt(getString(R.string.default_duration));
        if(mAudioContentUri != null){  // this is a audio guided session
            mPlannedDuration = mAudioFileDuration;
        }else { //this is a pre-set duration session
            mPlannedDuration = prefs.getLong(getString(R.string.pref_duration_key), defaultDuration);
        }
        mSessionRemainingDuration = mPlannedDuration;
        //
        mRemainingDurationTxtView.setVisibility(View.VISIBLE);
        mRemainingDurationTxtView.setText(durationFormatted(mSessionRemainingDuration));
        //
        boolean defaultInfiniteSession = Boolean.parseBoolean(getString(R.string.default_infinite_session));
        final boolean infiniteSession = prefs.getBoolean(getString(R.string.pref_switch_infinite_session_key), defaultInfiniteSession);
        //
        if(mElapsedTime < mPlannedDuration) {
            final long runSessionCountDownUpdateInterval = Long.parseLong(getString(R.string.run_session_countdown_update_interval));
            if (mElapsedTime > - runSessionCountDownUpdateInterval) { // means we are back from a pause, just calculate remaining duration
                Log.d(TAG, "launchBubbleTimer::back from pause, session not complete BUT already started");
                // countdowntimer dispatches ontick on start (before the first update cycle has even begun), so we have to offset the start value of mElapsedTime
                mElapsedTime -= runSessionCountDownUpdateInterval;
                mSessionRemainingDuration = mSessionRemainingDuration - mElapsedTime;
                updateStickyNotification(SESSION_STATE_NORMAL_RUNNING);
            } else{ //beginning session
                Log.d(TAG, "launchBubbleTimer::session STARTING");
                displayStickyNotification();
            }
/////////////////
//actual session start :
/////////////////
            mRunSessionCountDownIsRunning = true;
            if(mAudioContentUri != null){ // launch audio playback
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(getActivity().getApplicationContext(), mAudioContentUri);
                    mMediaPlayer.prepare();
                    if (mElapsedTime > 0){
                        mMediaPlayer.seekTo((int) mElapsedTime); // elapsedTime is updated only on completed onTick => audio guide will restart a bit sooner than it was paused.. this is good
                    }
                    mMediaPlayer.start();
                } catch (IOException e) {
                    Log.e(TAG, "launchBubbleTimer::could not play selected sound file:: e = " + e);
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "launchBubbleTimer::mDNDIsOverridden = " + mDNDIsOverridden);
            mRunSessionCountDown = new CountDownTimer(mSessionRemainingDuration, runSessionCountDownUpdateInterval) {
                public void onTick(long millisUntilFinished) {
                    //DND on activation will shut notifications, even our own, so we delay activation to be able to play our notif
                    if(!mDNDIsOverridden) {
                        if ((mSessionRemainingDuration - millisUntilFinished) >= mDelayBeforeDND) {
                            handleDoNotDisturbMode(true);
                        }
                    }
                    mElapsedTime += runSessionCountDownUpdateInterval; //update elapsed time so that if the user pauses the session, we will have an up-to-date elapsed time
                    Log.d(TAG, "onTick::mElapsedTime = " + mElapsedTime);
                    mRemainingDurationTxtView.setText(durationFormatted((60000 + millisUntilFinished)));
                    updateStickyNotification(SESSION_STATE_NORMAL_RUNNING);
                }

                public void onFinish() {
                    handleDoNotDisturbMode(false); //allow end of planned session sound to play
                    playSessionSound(PLAY_END_SESSION_SOUND);
                    handleVibrateDevice();
                    //mElapsedTime += runSessionCountDownUpdateInterval; //can't rely on this because countdowntimer may skip last onTick if remaining time is < to 1 interval (even for 1 millisecond) => as a result, we neraly always miss 1 interval from total duration
                    //mElapsedTime = mSessionRemainingDuration; //wrong too because then if a pause has occured, then only the duration remaining at resume time will be acounted for...
                    mElapsedTime = mPlannedDuration;  // if we have reached onFinish here, means we have done the planned duration, we overwrite previous sub-counting in mElapsedTime with the real value (otherwise it would have missed one onTick at least)
                    Log.d(TAG, "onFinish::mElapsedTime = " + mElapsedTime);
                    mRunSessionCountDownIsRunning = false;
                    if(mMediaPlayer!=null){
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    updateStickyNotification(SESSION_STATE_ENDED);
                    if (infiniteSession) {
                        launchSessionOverStay();
                    } else {
                        endSession();
                        mRemainingDurationTxtView.setVisibility(View.INVISIBLE);
                    }
                }

            }.start();
        } else{ // means we are back from a pause AND we have exceeded the planned duration
            Log.d(TAG, "launchBubbleTimer::back from pause, planned duration exceeded");
            launchSessionOverStay();
        }
    }

////////////////////////////////////////
//Session can runs longer than preset time if user has set it to in savedSharedPrefs : we run a countdowntimer for 1mn and relaunch it until user stops the session
    private void launchSessionOverStay(){
        String plannedDuration = durationFormatted(mPlannedDuration);
        String bonusDuration = durationFormatted((mElapsedTime - mPlannedDuration));
        Log.d(TAG, "launchSessionOverStay::plannedDuration = " + mPlannedDuration/1000 + " / mElapsedTime = " + mElapsedTime/1000);
        //TODO : grooming presentation
        mRemainingDurationTxtView.setText(plannedDuration + " + " + bonusDuration);
        mRunSessionCountUpIsRunning = true;
        final int oneMnInMs = 60000;
        long runSessionCountDownUpdateInterval = Long.parseLong(getString(R.string.run_session_countdown_update_interval));
        mRunSessionCountUp = new CountDownTimer(oneMnInMs, runSessionCountDownUpdateInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "launchSessionOverStay:: mDNDIsOverridden = " + mDNDIsOverridden + " / (oneMnInMs - millisUntilFinished) = " + (oneMnInMs - millisUntilFinished));
                if(!mDNDIsOverridden) {
                    if ((oneMnInMs - millisUntilFinished) >= mDelayBeforeDND) {
                        handleDoNotDisturbMode(true);
                    }
                }
            }
            @Override
            public void onFinish() {
                mRunSessionCountUpIsRunning = false;
                mElapsedTime += oneMnInMs;
                updateStickyNotification(SESSION_STATE_OVERSTAY_RUNNING);
                launchSessionOverStay();
            }
        }.start();
    }

////////////////////////////////////////
//Session PAUSE
    @Override
    public void onPause() {
        /*
        I wanted initially to put the session on pause any time the user did something else. But it has proved apparently impossible
        to discriminate with enough precision these cases from "legitimate" onpause calls, like the ones made by the OS when the screen
        goes off, or an external app (like a clock) takes precedence. I give up on this feature, letting to the user the responsibility
        of his engagement in the practice
                //here we detect app loss of focus. Since the sole purpose of the app is to help the user to progress along the mindfulness path, we do not allow the session to run in background
                //BUT if the setting to keep screen ON during session is set to OFF, then the screen switching off will also call onPause()
                //in THIS only case, we will allow the session to continue => this is why we test if the screen is on
                Log.d(TAG, "onPause::mRunSessionCountDownIsRunning = " + mRunSessionCountDownIsRunning + " / mRunSessionCountUpIsRunning = " + mRunSessionCountUpIsRunning + " / mStartSessionCountDownIsRunning = " + mStartSessionCountDownIsRunning);
                if(!sessionIsFinished) {//onpause will be called when SessionActivity removes this fragment, but then the session will be finished, so no need to pause it
                    PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                    boolean screenOn = false;
                    if (pm != null) {
                        screenOn = pm.isInteractive();
                        Log.d(TAG, "onPause::screenOn = " + screenOn);
                    } else {
                        Log.e(TAG, "onPause::PowerManager = null, could not determine if screen is ON");
                    }
                    if (screenOn) { //pb the screen may not be OFF (feature like clock can keep screen on and app is still in background -so paused)
                        pauseSession();
                    } else {
                        Log.d(TAG, "onPause:screen is off, do nothing : onPause was not caused by user action");
                    }
                }
        */
        super.onPause();
    }

    private void pauseSession() {
        Log.d(TAG, "pauseSession");
        //test : see if behaviour is more intuitive without this condition :
        //if (mElapsedTime > 0) {
            destroyAllCountDownTimers();
            stopBubbleTimerAnimation();
            showPauseDialog();
            handleKeepScreenOn(false);
            handleDoNotDisturbMode(false);
            updateStickyNotification(SESSION_STATE_PAUSED);
            if(mMediaPlayer!=null){
                mMediaPlayer.pause();
            }
        /*}else{//fragment onpause happened during countdown to start (or before first onTick) before beginning session => just abort session
            removeStickyNotification();
            mListener.onAbortSessionInProgressFragment();
        }*/
    }
    private void showPauseDialog() {
        Log.d(TAG, "showPauseDialog");
        if(mPauseAlertDialog == null) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
            adBuilder.setPositiveButton(getString(R.string.resume), onPauseDialogResumeBtnListener);
            adBuilder.setNegativeButton(getString(R.string.finish), onPauseDialogEndBtnListener);
            mPauseAlertDialog = adBuilder.create();
            mPauseAlertDialog.setCancelable(false);
            mPauseAlertDialog.setTitle(getString(R.string.pause));
        }
        if(!mPauseAlertDialog.isShowing()) {
            mPauseAlertDialog.show();
        }
    }
    private DialogInterface.OnClickListener onPauseDialogResumeBtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            resumeSession();
        }
    };
    private DialogInterface.OnClickListener onPauseDialogEndBtnListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            removeStickyNotification();
            endSession();
        }
    };
    private void resumeSession(){
        Log.d(TAG, "resumeSession");
        mPausesCount += 1;
        handleKeepScreenOn(true);
        startCountdownBeforeBeginningSession();
    }

////////////////////////////////////////
//Session ENDS
    private void endSession() {
        Log.d(TAG, "endSession");
        sessionIsFinished = true;
        destroyAllCountDownTimers();
        stopBubbleTimerAnimation();
        handleKeepScreenOn(false);
        handleDoNotDisturbMode(false);
        Log.d(TAG, "endSession::mElapsedTime = " + mElapsedTime);
        int realDurationVsPlanned = Long.compare(mElapsedTime, mPlannedDuration);
        //do not remove the sticky notification yet, keep it active until mood has been recorded. => removing will be handled then by SessionActivity
        //removeStickyNotification();
        mListener.onFinishSessionInProgressFragment(mElapsedTime, mPausesCount, realDurationVsPlanned, mGuideMp3);
    }

////////////////////////////////////////
//BUBBLE TIMER ANIMATION CONTROL

    private void prepareBubbleTimerAnimation(){
        //TODO : améliorer l'animation, bug, et affiner le résultat recherché
        mAnimPulse = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.pulse_animation);
        mAnimPulse.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                mBubbleCounter.startAnimation(animation);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void startBubbleTimerAnimation(){
        if(mAnimPulse!=null) {
            mBubbleCounter.startAnimation(mAnimPulse);
        }
    }
    private void stopBubbleTimerAnimation(){
        if(mAnimPulse!=null) {
            mAnimPulse.setAnimationListener(null);
        }
    }

    private void destroyBubbleTimerAnimation(){
        if(mAnimPulse!= null && mAnimPulse.hasStarted()){
            mAnimPulse.cancel();
            mAnimPulse=null;
        }
    }

////////////////////////////////////////
//display / update / remove sticky notification while session runs/is paused / ends
    private void displayStickyNotification(){
       Log.d(TAG, "displayStickyNotification");
        String totalPlannedTime = durationFormatted(mPlannedDuration);
        String remainingTime = durationFormatted(mSessionRemainingDuration);
        String notifText = String.format(getString(R.string.session_running_notification_text), remainingTime, totalPlannedTime);
        //Builds the notification with all of the parameters
        Notification.Builder notifyBuilder = buildRunningSessionNotification(notifText, false);
        NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.notify(STICKY_SESSION_RUNNING_NOTIFICATION_ID, notifyBuilder.build());
        }else{
            Log.e(TAG, "displayStickyNotification::notifyManager is null!!");
        }
    }
    private Notification.Builder buildRunningSessionNotification(String notifText, boolean sessionFinished){
        String notifTitle = getString(R.string.session_in_progress_notification_title);
        // prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(mContext, SessionActivity.class);
        if(sessionFinished){
            //Log.d(TAG, "buildRunningSessionNotification::putting extra NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED in notification intent");
            intent.putExtra(NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED, true);
        }else {
            //Log.d(TAG, "buildRunningSessionNotification::putting extra NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS in notification intent");
            intent.putExtra(NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS, true);
        }
        PendingIntent comeBackPendingIntent = PendingIntent.getActivity(mContext, STICKY_SESSION_RUNNING_NOTIFICATION_INTENT_ID, intent, FLAG_UPDATE_CURRENT );
        //
        Notification.Builder notifyBuilder = new Notification.Builder(mContext)
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                //TODO : create ICON for notifications
                .setSmallIcon(R.drawable.ic_info_white_24dp)
                .setContentIntent(comeBackPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false);
        return notifyBuilder;
    }

    private void updateStickyNotification(String sessionState){
        String totalPlannedTime = durationFormatted(mPlannedDuration);
        String elapsedTime = durationFormatted(mElapsedTime);
        String differenceBtwnPlannedAndElapsedTime = durationFormatted(Math.min(mPlannedDuration, Math.abs(60000 + mPlannedDuration - mElapsedTime))); //we don't want to display remaining ENTIRE minutes, but rather include the commenced minute. But we don't want the first onTick to display a value bigger than total duration
        String notifText;
        Notification.Builder notifyBuilder;
        switch(sessionState){
            case SESSION_STATE_OVERSTAY_RUNNING:
                notifText = String.format(getString(R.string.session_overstay_notification_text), totalPlannedTime, differenceBtwnPlannedAndElapsedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
                break;
            case SESSION_STATE_PAUSED:
                notifText = String.format(getString(R.string.session_paused_notification_text), elapsedTime, totalPlannedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
                break;
            case SESSION_STATE_ENDED:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean recordStatsPref = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
                if(!recordStatsPref){ // user is not recording any stats : no need to show the end session notification
                    removeStickyNotification();
                    return;
                }else{
                    notifText = String.format(getString(R.string.session_ended_notification_text), elapsedTime);
                }
                notifyBuilder = buildRunningSessionNotification(notifText, true);
                break;
            default: // SESSION_STATE_NORMAL_RUNNING
                notifText = String.format(getString(R.string.session_running_notification_text), differenceBtwnPlannedAndElapsedTime, totalPlannedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
        }
        NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            Log.d(TAG, "updateStickyNotification::notifText = " + notifText);
            notifyManager.notify(STICKY_SESSION_RUNNING_NOTIFICATION_ID, notifyBuilder.build());
        }else{
            Log.e(TAG, "updateStickyNotification::notifyManager is null!!");
        }
    }

    private void removeStickyNotification(){
        Log.d(TAG, "removeStickyNotification");
        NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.cancel(STICKY_SESSION_RUNNING_NOTIFICATION_ID);
        }else{
            Log.e(TAG, "removeStickyNotification::notifyManager is null!!");
        }
    }

////////////////////////////////////////
//IF a session is running, override backNav to pause the session, otherwise, let parent activity handle the back nav
    public boolean isNormalBackNavAllowed(){
        return mIsNormalBackNavAllowed;
    }
    public void onOverrideBackNav(){
        pauseSession();
    }


////////////////////////////////////////
//Helper method to change KEEP-screen-on device state (setting only applied while session running, need to remove it at the end)
    private void handleKeepScreenOn(boolean keepOn){
        Log.d(TAG, "handleKeepScreenOn");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean keepScreenOnPref = prefs.getBoolean(getString(R.string.pref_switch_keep_screen_on_key), Boolean.valueOf(getString(R.string.default_keep_screen_on)));
        if(keepScreenOnPref) { //user saved pref
            if (keepOn) {
                Log.d(TAG, "handleKeepScreenOn::keepOn : keeping SCREEN ON");
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                Log.d(TAG, "handleKeepScreenOn::keepOn : NOT keeping SCREEN on ANYMORE");
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

////////////////////////////////////////
//Helper method to format duration string
    private String durationFormatted(long duration){
        return TimeOperations.convertMillisecondsToHoursAndMinutesString(
                duration,
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                false);
    }

////////////////////////////////////////
//Helper method to set the device in DND or back to normal (or silent if SDK < Marshmallow) according to saved SharedPref
    private void handleDoNotDisturbMode(boolean activateDND) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean prefActiveDND = prefs.getBoolean(getString(R.string.pref_switch_do_not_disturb_key), Boolean.valueOf(getString(R.string.default_do_not_disturb)));
        if(prefActiveDND){
            //behaviour is different below or above Marshmallow (android6)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //we do not check for granted access here, since it has to have been done when the setting was first activated in SettingsActivity (since default value on install is OFF)
                NotificationManager notifManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                if(notifManager!=null){
                    if (activateDND) {
                        Log.d(TAG, "handleDoNotDisturbMode::DO activate DND");
                        mPreviousInterruptionFilter = notifManager.getCurrentInterruptionFilter();
                        notifManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                        mDNDIsOverridden = true;
                    } else {
                        Log.d(TAG, "handleDoNotDisturbMode::deactivate DND -> go back to previous mode");
                        if(mPreviousInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_UNKNOWN){
                            mPreviousInterruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL;
                        }
                        notifManager.setInterruptionFilter(mPreviousInterruptionFilter);
                        mDNDIsOverridden = false;
                    }
                }else{
                    Log.e(TAG, "handleDoNotDisturbMode::notifManager == NULL!!");
                }
            }else{
                AudioManager ringerMode = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (ringerMode != null) {
                    if (activateDND) {
                        mPreviousInterruptionFilter = ringerMode.getRingerMode();
                        ringerMode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        mDNDIsOverridden = true;
                        Log.d(TAG, "handleDoNotDisturbMode::DO activate DND::mPreviousInterruptionFilter = " + mPreviousInterruptionFilter);
                    }else{
                        if(mPreviousInterruptionFilter == -1){
                            mPreviousInterruptionFilter = AudioManager.RINGER_MODE_VIBRATE;
                            //mPreviousInterruptionFilter = AudioManager.RINGER_MODE_NORMAL;
                            //Warning : setting to RINGER_MODE_NORMAL not always working is a reported "won't fix" android 5.0 bug
                            // https://issuetracker.google.com/issues/37008264
                            // => setting to VIBRATE seems to be working but I only have a non-vibrating android5.0 device so I can't test for strange behaviours on vibrating android5 devices
                        }
                        ringerMode.setRingerMode(mPreviousInterruptionFilter);
                        mDNDIsOverridden = false;
                        Log.d(TAG, "handleDoNotDisturbMode::deactivate DND -> go back to previous mode : mPreviousInterruptionFilter = " + mPreviousInterruptionFilter);
                    }
                } else {
                    Log.e(TAG, "handleDoNotDisturbMode::ringerMode == NULL!!");
                }

            }
        }else{ //user has not activated do not disturb during sessions, but we just set the flag to true to prevent useless following passage here
            if (activateDND) {
                //do nothing but set flag to true
                mDNDIsOverridden = true;
            }else{
                //do nothing but set flag to false
                mDNDIsOverridden = false;
            }
        }
    }

////////////////////////////////////////
//Helper method to play sound set in savedSharedPref
    private void playSessionSound(String play_session_sound) {
        if(mAudioContentUri==null) { //override user's prefs if session is audio guided => no start or end sound
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String soundString;
            Log.d(TAG, "playSessionSound " + play_session_sound);
            switch (play_session_sound) {
                case PLAY_START_SESSION_SOUND:
                    String defaultStartSoundUriString = uriFromRaw(getString(R.string.default_session_start_sound)).toString();
                    soundString = prefs.getString(getString(R.string.pref_ringtone_start_key), defaultStartSoundUriString);
                    break;
                case PLAY_END_SESSION_SOUND:
                    String defaultEndSoundUriString = uriFromRaw(getString(R.string.default_session_end_sound)).toString();
                    soundString = prefs.getString(getString(R.string.pref_ringtone_start_key), defaultEndSoundUriString);
                    break;
                default:
                    Log.e(TAG, "Wrong instruction to play session sound");
                    return;
            }
            Uri soundUri = Uri.parse(soundString);
            if (soundUri != null) {
                if (soundUri.toString().length() > 0) {
                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), soundUri);
                    ringtone.play();
                } else {
                    Log.d(TAG, "session sound set to silence");
                }
            } else {
                Log.e(TAG, "URI for session sound was null!");
            }
        }
    }
    private Uri uriFromRaw(String name) {
        int resId = getActivity().getResources().getIdentifier(name, "raw", getActivity().getPackageName());
        return Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + resId);
    }

////////////////////////////////////////
//Helper method to vibrate device if possible
    private void handleVibrateDevice(){
        Log.d(TAG, "handleVibrateDevice");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean vibrateStartEndSession = prefs.getBoolean(getString(R.string.pref_switch_vibration_key), Boolean.valueOf(getString(R.string.default_vibration_active)));
        Vibrator vibrator = ((Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE));
        if(vibrator!=null) {
            if (vibrateStartEndSession && vibrator.hasVibrator()) {
                Log.d(TAG, "handleVibrateDevice::DO VIBRATE");
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(150);
                }
            }
        }
    }

////////////////////////////////////////
//Helper method to destroy all CountDownTimers that may be running
    private void destroyAllCountDownTimers(){
        Log.d(TAG, "destroyAllCountDownTimers");
        if(mStartSessionCountDown!=null){
            mStartSessionCountDown.cancel();
            mStartSessionCountDownIsRunning = false;
            mStartSessionCountDown = null;
        }
        if(mRunSessionCountDown!=null){
            mRunSessionCountDown.cancel();
            mRunSessionCountDownIsRunning = false;
            mRunSessionCountDown = null;
        }
        if(mRunSessionCountUp!=null){
            mRunSessionCountUp.cancel();
            mRunSessionCountUpIsRunning = false;
            mRunSessionCountUp = null;
        }
    }

////////////////////////////////////////
//Listener interface
    public interface SessionInProgressFragmentListener {
        void onAbortSessionInProgressFragment();
        void onFinishSessionInProgressFragment(long duration, int pausesCount, int realVsPlannedDuration, String guideMp3);
    }
}
