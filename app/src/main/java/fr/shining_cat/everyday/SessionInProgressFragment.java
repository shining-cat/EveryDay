package fr.shining_cat.everyday;

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
import android.widget.TextView;

import java.io.IOException;

import fr.shining_cat.everyday.utils.TimeOperations;
import fr.shining_cat.everyday.utils.UiUtils;
import fr.shining_cat.everyday.utils.WakelockController;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static fr.shining_cat.everyday.broadcastreceivers.AlarmSetterBroadcastReceiver.MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY;

//TODO ? BUG: sur le samsung de pa, plusieurs click normaux declenchent le long click o_O

public class SessionInProgressFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SESSIONINPROGRESSFRAGMENT_TAG = "SessionInProgressFragment-tag";

    private static final int STICKY_SESSION_RUNNING_NOTIFICATION_INTENT_ID = 713;
    public static final int STICKY_SESSION_RUNNING_NOTIFICATION_ID = 715;
    public static final String NOTIFICATION_INTENT_COMING_FROM_SESSION_IN_PROGRESS = "This intent was launched by the notification set by SessionInProgress, session is running";
    public static final String NOTIFICATION_INTENT_COMING_FROM_SESSION_FINISHED = "This intent was launched by the notification set by SessionInProgress, session has ended";

    private SessionInProgressFragmentListener mListener;

    private final String PLAY_START_SESSION_SOUND = "play start session sound if one is set to";
    private final String PLAY_INTERMEDIATE_INTERVAL_SESSION_SOUND = "play intermediate interval sound if one is set to";
    private final String PLAY_END_SESSION_SOUND = "play end session sound if one is set to";

    private final String SESSION_STATE_NORMAL_RUNNING = "session is running up to planned duration";
    private final String SESSION_STATE_OVERSTAY_RUNNING = "session is running over planned duration";
    private final String SESSION_STATE_PAUSED = "session is paused";
    private final String SESSION_STATE_ENDED = "session has reached planned duration";

    private Context mContext;
    private View mRootView;
    private TextView mPauseInstructionTxtView;
    private TextView mStartingCountdownTxtView;
    private TextView mRemainingDurationTxtView;
    private View mBubbleCounterIntPart;
    private View mBubbleCounterExtPart;
    private Animation mAnimPulseExpand;
    private Animation mAnimPulseRetract;
    private Animation mAnimPulseFadeOut;
    private Animation mAnimPulseOverstayIn;
    private Animation mAnimPulseOverstayOut;
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
    private long mIntermediateIntervalLength;
    private long mTempElapsedTimeWhileOverstaying;
    private int mPausesCount;
    private String mGuideMp3;
    private int mPreviousRingerMode;
    private boolean mRingerModeIsOverridden;
    private boolean mIsNormalBackNavAllowed;
    private AlertDialog mPauseAlertDialog;
    private boolean sessionIsFinished;
    private Uri mAudioContentUri;
    private int mAudioFileDuration;
    private MediaPlayer mMediaPlayer;
    private Ringtone mRingtone;
    private Uri mLastPlayedRingtoneUri;


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
        mRootView.setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
        //
        mPauseInstructionTxtView = mRootView.findViewById(R.id.session_in_progress_pause_instruction_txtview);
        mStartingCountdownTxtView = mRootView.findViewById(R.id.session_in_progress_countdown_txtview);
        mBubbleCounterExtPart = mRootView.findViewById(R.id.bubble_counter_ext);
        mBubbleCounterIntPart = mRootView.findViewById(R.id.bubble_counter_int);
        mRemainingDurationTxtView = mRootView.findViewById(R.id.session_running_countdown_txtview);
        mRemainingDurationTxtView.setVisibility(View.INVISIBLE);
        // countdowntimer dispatches ontick on start (before the first update cycle has even begun), so we have to offset the start value of mElapsedTime
        mElapsedTime = - Long.parseLong(getString(R.string.run_session_countdown_update_interval));
        mPausesCount = 0;
        mStartSessionCountDownIsRunning = false;
        mRunSessionCountDownIsRunning = false;
        mRunSessionCountUpIsRunning = false;
        mRingerModeIsOverridden = false;
        mPreviousRingerMode = -1;
        mIsNormalBackNavAllowed = true;
        mGuideMp3 = "";
        //if we were passed an audio file URI for a guided session, retrieve name, artist, and duration
        if(mAudioContentUri != null){
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this.getActivity(), mAudioContentUri);
            //get audio file display name :
            String fileNameStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if(fileNameStr == null){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_TITLE :: fileNameStr is NULL!!");
                mGuideMp3 = getActivity().getString(R.string.guided_session_filename_unknown);
            }else if(fileNameStr.isEmpty()){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_TITLE :: fileNameStr is EMPTY!!");
                mGuideMp3 = getActivity().getString(R.string.guided_session_filename_unknown);
            }else {
                mGuideMp3 = fileNameStr;
            }
            //get audio file artist name
            String artistNameStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if(artistNameStr == null){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_ARTIST :: artistNameStr is NULL!!");
                mGuideMp3 += " - " + getActivity().getString(R.string.guided_session_artist_unknown);

            }else if(artistNameStr.isEmpty()){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_ARTIST :: artistNameStr is EMPTY!!");
                mGuideMp3 += " - " + getActivity().getString(R.string.guided_session_artist_unknown);
            }else {
                mGuideMp3 += " - " + artistNameStr;
            }
            //get audio file album name
            String albumNameStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if(albumNameStr == null){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_ALBUM :: albumNameStr is NULL!!");
                mGuideMp3 += " - " + getActivity().getString(R.string.guided_session_album_unknown);

            }else if(albumNameStr.isEmpty()){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_ALBUM :: artistNameStr is EMPTY!!");
                mGuideMp3 += " - " + getActivity().getString(R.string.guided_session_album_unknown);
            }else {
                mGuideMp3 += " - " + albumNameStr;
            }
            Log.d(TAG, "onAttach::mGuideMp3 = " + mGuideMp3 + " / mAudioFileDuration in minutes = " + mAudioFileDuration/60000);
            //get audio file duration :
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(durationStr == null){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_DURATION :: durationStr is NULL!!");
                if(mListener!=null) {
                    mListener.askForManualAudioDurationEntry();
                }else{
                    Log.e(TAG, "onAttach::no listener!!");
                }
            }else if(durationStr.isEmpty()){
                Log.e(TAG, "onAttach::MediaMetadataRetriever could not retrieve METADATA_KEY_DURATION :: durationStr is EMPTY!!");
                if(mListener!=null) {
                    mListener.askForManualAudioDurationEntry();
                }else{
                    Log.e(TAG, "onAttach::no listener!!");
                }
            }else {
                mAudioFileDuration = Integer.parseInt(durationStr);
                Log.d(TAG, "onAttach::mGuideMp3 = " + mGuideMp3 + " /  mAudioFileDuration = " + mAudioFileDuration / 60000);
                prepareStart();
            }
        }else{
            prepareStart();
        }
        return mRootView;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
        destroyBubbleTimerAnimations();
        destroyAllCountDownTimers();
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if(mHidePauseInstructionHandler!=null){
            mHidePauseInstructionHandler.removeCallbacksAndMessages(null);
        }
    }

    public void comingBackToSession(){
        delayedHideSystemUI(1000);
    }

    private void prepareStart(){
        showBrieflyHowToPauseInstruction();
        startCountdownBeforeBeginningSession();
        handleKeepScreenOn(true);
        handleAlarmPostponing();

    }


////////////////////////////////////////
//manual duration entry (mAudioFileDuration) called if audio file duration could not be obtained from MediaMetadataRetriever

    public void onManualAudioDurationEntryCallback(long manualAudioDuration){
        mAudioFileDuration = (int) manualAudioDuration; //safe cast because duration can not exceed 24hrs as Ms
        prepareStart();
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
String mCountdownTxt;
    private void startCountdownBeforeBeginningSession() {
        Log.d(TAG, "startCountdownBeforeBeginningSession::mElapsedTime = " + mElapsedTime);
        //
        delayedHideSystemUI(1000);
        //
        mRemainingDurationTxtView.setVisibility(View.INVISIBLE);
        int startSessionCountdownLength = Integer.parseInt(getString(R.string.start_session_countdown_delay));
        if (mElapsedTime <= 0) {
            mCountdownTxt = getString(R.string.start_countdown_beginning_session);
        }else{
            mCountdownTxt = getString(R.string.start_countdown_resuming_session);
        }
        mStartingCountdownTxtView.setText(String.format(mCountdownTxt, startSessionCountdownLength));
        Log.d(TAG, "startCountdownBeforeBeginningSession::mStartingCountdownTxtView = " + mStartingCountdownTxtView);
        mStartingCountdownTxtView.setVisibility(View.VISIBLE);
        mStartSessionCountDown = new CountDownTimer(startSessionCountdownLength, 500) {
            public void onTick(long millisUntilFinished) {
                mStartingCountdownTxtView.setText(String.format(mCountdownTxt,(1 + millisUntilFinished / 1000)));
            }
            public void onFinish() {
                mStartingCountdownTxtView.setText(String.format(mCountdownTxt,0));
                mStartingCountdownTxtView.setVisibility(View.INVISIBLE);
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
        playRingtoneSound(PLAY_START_SESSION_SOUND);
        handleVibrateDevice();
        launchBubbleTimer();
        sessionIsFinished = false;
        mIsNormalBackNavAllowed = false;
    }

////////////////////////////////////////
//Session RUNS
    private void launchBubbleTimer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //
        int defaultDuration = Integer.parseInt(getString(R.string.default_duration));
        if(mAudioContentUri != null){  // this is an audio guided session
            mPlannedDuration = mAudioFileDuration;
        }else { //this is a pre-set duration session
            mPlannedDuration = prefs.getLong(getString(R.string.pref_duration_key), defaultDuration);
        }
        mSessionRemainingDuration = mPlannedDuration;
        //
        startAnimPulseRetract();
        //
        mRemainingDurationTxtView.setVisibility(View.VISIBLE);
        String displayRemainingTimeOnLaunch = durationFormattedComplete(mSessionRemainingDuration);
        Log.d(TAG, "launchBubbleTimer::displayRemainingTimeOnLaunch = " + displayRemainingTimeOnLaunch);
        mRemainingDurationTxtView.setText(displayRemainingTimeOnLaunch);
        //
        boolean defaultInfiniteSession = Boolean.parseBoolean(getString(R.string.default_infinite_session));
        final boolean infiniteSession = prefs.getBoolean(getString(R.string.pref_switch_infinite_session_key), defaultInfiniteSession);
        //
        long defaultIntermediateIntervalLength = Long.parseLong(getString(R.string.default_intermediate_interval_length));
        mIntermediateIntervalLength = prefs.getLong(getString(R.string.pref_intermediate_intervals_key), defaultIntermediateIntervalLength);
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
            //TODO? handle mediaplayer errors and display alertdialogs accordingly
            mRunSessionCountDownIsRunning = true;
            if(mAudioContentUri != null){ // launch audio playback
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(getActivity().getApplicationContext(), mAudioContentUri);
                    mMediaPlayer.prepare();
                    if (mElapsedTime > 0){
                        mMediaPlayer.seekTo((int) mElapsedTime); // elapsedTime is updated only on completed onTick => audio guide will restart a bit sooner than it was paused, but ticker is set on 1s, so...
                    }
                    mMediaPlayer.start();
                } catch (IOException e) {
                    Log.e(TAG, "launchBubbleTimer::could not play selected sound file:: e = " + e);
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "launchBubbleTimer::mRingerModeIsOverridden = " + mRingerModeIsOverridden);
            mRunSessionCountDown = new CountDownTimer(mSessionRemainingDuration, runSessionCountDownUpdateInterval) {
                public void onTick(long millisUntilFinished) {
                    //regularly check if our ringtone is done playing to activate ringer_mode silent
                    if(mRingtone!=null){
                        if(!mRingtone.isPlaying() && !mRingerModeIsOverridden){
                            handleDoNotDisturbMode(true);
                        }
                    }
                    mElapsedTime += runSessionCountDownUpdateInterval; //update elapsed time so that if the user pauses the session, we will have an up-to-date elapsed time
                    //Log.d(TAG, "onTick::mElapsedTime in seconds = " + (mElapsedTime/1000));
                    if(mIntermediateIntervalLength!=0){
                        //Log.d(TAG, "onTick::mElapsedTime % intermediateIntervalLength = " + (mElapsedTime % intermediateIntervalLength));
                        if(mElapsedTime > 0 && mElapsedTime % mIntermediateIntervalLength < 50){ //in case onTick is not precise to the millisecond
                            playRingtoneSound(PLAY_INTERMEDIATE_INTERVAL_SESSION_SOUND);
                        }
                    }
                    String differenceBtwnPlannedAndElapsedTime = durationFormattedComplete(Math.min(mPlannedDuration, Math.abs(mPlannedDuration - mElapsedTime)));
                    mRemainingDurationTxtView.setText(differenceBtwnPlannedAndElapsedTime);
                    updateStickyNotification(SESSION_STATE_NORMAL_RUNNING);
                }

                public void onFinish() {
                    handleDoNotDisturbMode(false); //allow end of planned session sound to play
                    playRingtoneSound(PLAY_END_SESSION_SOUND);
                    handleVibrateDevice();
                    //mElapsedTime += runSessionCountDownUpdateInterval; //can't rely on this because countdowntimer may skip last onTick if remaining time is < to 1 interval (even for 1 millisecond) => as a result, we neraly always miss 1 interval from total duration
                    //mElapsedTime = mSessionRemainingDuration; //wrong too because then if a pause has occurred, then only the duration remaining at resume time will be acounted for...
                    mElapsedTime = mPlannedDuration;  // if we have reached onFinish here, means we have done the planned duration, we overwrite previous sub-counting in mElapsedTime with the real value (otherwise it would have missed one onTick at least)
                    Log.d(TAG, "onFinish::mElapsedTime = " + mElapsedTime);
                    mRunSessionCountDownIsRunning = false;
                    destroyBubbleTimerAnimations();
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
            WakelockController.acquire(getActivity(), mSessionRemainingDuration);
        } else{ // means we are back from a pause AND we have exceeded the planned duration
            Log.d(TAG, "launchBubbleTimer::back from pause, planned duration exceeded");
            launchSessionOverStay();
        }
    }

////////////////////////////////////////
//Session can run longer than preset time if user has set it to in savedSharedPrefs : we run a countdowntimer for 1mn and relaunch it until user stops the session
    private void launchSessionOverStay(){
        mRunSessionCountUpIsRunning = true;
        mTempElapsedTimeWhileOverstaying = mElapsedTime;
        final int oneMnInMs = 60000;
        final long runSessionOverstayUpdateInterval = Long.parseLong(getString(R.string.run_session_countdown_update_interval));
        //
        startAnimPulseOverstayOut();
        mBubbleCounterExtPart.setAlpha(1f);
        //
        mRunSessionCountUp = new CountDownTimer(oneMnInMs, runSessionOverstayUpdateInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                //regularly check if our ringtone is done playing to activate ringer_mode silent
                if(mRingtone!=null){
                    if(!mRingtone.isPlaying() && !mRingerModeIsOverridden){
                        handleDoNotDisturbMode(true);
                    }
                }
                mElapsedTime += runSessionOverstayUpdateInterval; //update elapsed time so that if the user pauses the session, we will have an up-to-date elapsed time
                updateStickyNotification(SESSION_STATE_OVERSTAY_RUNNING);
                String plannedDuration = durationFormattedShort(mPlannedDuration);
                String bonusDuration = durationFormattedComplete((mElapsedTime - mPlannedDuration));
                Log.d(TAG, "launchSessionOverStay::onTick::plannedDuration = " + mPlannedDuration/1000 + " / mElapsedTime = " + mElapsedTime/1000);
                if(mIntermediateIntervalLength!=0){
                    if(mElapsedTime % mIntermediateIntervalLength < 50){ //in case onTick is not precise to the millisecond
                        playRingtoneSound(PLAY_INTERMEDIATE_INTERVAL_SESSION_SOUND);
                    }
                }
                if(bonusDuration.isEmpty()) {
                    mRemainingDurationTxtView.setText(plannedDuration);
                }else{
                    mRemainingDurationTxtView.setText(plannedDuration + " + " + bonusDuration);
                }

            }
            @Override
            public void onFinish() {
                mRunSessionCountUpIsRunning = false;
                mElapsedTime = mTempElapsedTimeWhileOverstaying + oneMnInMs;
                updateStickyNotification(SESSION_STATE_OVERSTAY_RUNNING);
                launchSessionOverStay();
            }
        }.start();
        WakelockController.acquire(getActivity(), oneMnInMs);
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
        */
        super.onPause();
    }

    private void pauseSession() {
        Log.d(TAG, "pauseSession");
        destroyAllCountDownTimers();
        stopBubbleTimerAnimations();
        showPauseDialog();
        handleKeepScreenOn(false);
        handleDoNotDisturbMode(false);
        updateStickyNotification(SESSION_STATE_PAUSED);
        WakelockController.release();
        if(mMediaPlayer!=null){
            mMediaPlayer.pause();
        }
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
            updateStickyNotification(SESSION_STATE_ENDED);
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
        stopBubbleTimerAnimations();
        handleKeepScreenOn(false);
        handleDoNotDisturbMode(false);
        Log.d(TAG, "endSession::mElapsedTime = " + mElapsedTime);
        int realDurationVsPlanned = Long.compare(mElapsedTime, mPlannedDuration);
        WakelockController.release();
        //do not remove the sticky notification yet, keep it active until mood has been recorded. => removing will be handled then by SessionActivity
        mListener.onFinishSessionInProgressFragment(mElapsedTime, mPausesCount, realDurationVsPlanned, mGuideMp3);
    }

////////////////////////////////////////
//BUBBLE TIMER ANIMATION CONTROL
//pulse will be constituted of two animations : expand, then retract. Start and end values for animated properties will be function of mElapsedTime, so changing at every cycle

    //TODO eventually : BUG on configuration change : if device rotates during animation, pivot point is wrong until next cycle... did not find any way to handle this during running animation yet

    private Animation.AnimationListener animPulseExpandListener = new Animation.AnimationListener(){
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            startAnimPulseRetract();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
    private Animation.AnimationListener animPulseRetractListener = new Animation.AnimationListener(){
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            startAnimPulseExpand();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };

    private void startAnimPulseExpand(){
        int animPulseExpandDuration = Integer.parseInt(getString(R.string.anim_pulse_expand_duration));
        //I chose to set this proportional to the square root of the elapsed time because the mind will rather interpret the area than the widht or height as the important data
        float startScale = Math.max(0f, (float) Math.sqrt((double) mElapsedTime / (double) mPlannedDuration));
        mAnimPulseExpand = new ScaleAnimation(startScale, 1f, startScale, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimPulseExpand.setDuration(animPulseExpandDuration);
        mAnimPulseExpand.setFillAfter(true);
        mAnimPulseExpand.setAnimationListener(animPulseExpandListener);
        mBubbleCounterIntPart.startAnimation(mAnimPulseExpand);
    }
    private void startAnimPulseRetract(){
        int animPulseRetractDuration = Integer.parseInt(getString(R.string.anim_pulse_retract_duration));
        //I chose to set this proportional to the square root of the elapsed time because the mind will rather interpret the area than the widht or height as the important data
        //Adding the duration of the anim here to be sure to aim at the right value for the end of the anim, and avoid a jump between the value reached and the nex start of mAnimPulseExpand
        float endScale = Math.max(0f, (float) Math.sqrt((float) (mElapsedTime + animPulseRetractDuration) / (float) mPlannedDuration));
        mAnimPulseRetract = new ScaleAnimation(1f, endScale, 1f, endScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimPulseRetract.setFillAfter(true);
        mAnimPulseRetract.setDuration(animPulseRetractDuration);
        mAnimPulseRetract.setAnimationListener(animPulseRetractListener);
        mBubbleCounterIntPart.startAnimation(mAnimPulseRetract);
        //
        mBubbleCounterExtPart.setAlpha(1f);
        mAnimPulseFadeOut = new AlphaAnimation(1f, 0f);
        mAnimPulseFadeOut.setDuration(2*animPulseRetractDuration);
        mAnimPulseFadeOut.setFillAfter(true);
        mBubbleCounterExtPart.startAnimation(mAnimPulseFadeOut);

    }
    //animations for overstay have no link to elapsed time
    private void startAnimPulseOverstayIn(){
        int animPulseOverstayInDuration = Integer.parseInt(getString(R.string.anim_pulse_expand_duration));
        mAnimPulseOverstayIn = new AlphaAnimation(0.1f, 1f);
        mAnimPulseOverstayIn.setDuration(animPulseOverstayInDuration);
        mAnimPulseOverstayIn.setFillAfter(true);
        mAnimPulseOverstayIn.setAnimationListener(animPulseOverstayInListener);
        mBubbleCounterIntPart.startAnimation(mAnimPulseOverstayIn);
    }
    private void startAnimPulseOverstayOut(){
        int animPulseOverstayOutDuration = Integer.parseInt(getString(R.string.anim_pulse_retract_duration));
        mAnimPulseOverstayOut = new AlphaAnimation(1f, 0.1f);
        mAnimPulseOverstayOut.setDuration(animPulseOverstayOutDuration);
        mAnimPulseOverstayOut.setFillAfter(true);
        mAnimPulseOverstayOut.setAnimationListener(animPulseOverstayOutListener);
        mBubbleCounterIntPart.startAnimation(mAnimPulseOverstayOut);
    }

    private Animation.AnimationListener animPulseOverstayInListener = new Animation.AnimationListener(){
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            startAnimPulseOverstayOut();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
    private Animation.AnimationListener animPulseOverstayOutListener = new Animation.AnimationListener(){
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            startAnimPulseOverstayIn();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
    private void stopBubbleTimerAnimations(){//will not really stop them but rather prevent the cycle to start again
        if(mAnimPulseExpand !=null) {
            mAnimPulseExpand.setAnimationListener(null);
        }
        if(mAnimPulseRetract !=null) {
            mAnimPulseRetract.setAnimationListener(null);
        }
        if(mAnimPulseOverstayIn !=null) {
            mAnimPulseOverstayIn.setAnimationListener(null);
        }
        if(mAnimPulseOverstayOut !=null) {
            mAnimPulseOverstayOut.setAnimationListener(null);
        }
    }

    private void destroyBubbleTimerAnimations(){
        stopBubbleTimerAnimations();
        if(mAnimPulseExpand != null && mAnimPulseExpand.hasStarted()){
            mAnimPulseExpand.cancel();
            mAnimPulseExpand =null;
        }
        if(mAnimPulseRetract != null && mAnimPulseRetract.hasStarted()){
            mAnimPulseRetract.cancel();
            mAnimPulseRetract =null;
        }
        if(mAnimPulseOverstayIn != null && mAnimPulseOverstayIn.hasStarted()){
            mAnimPulseOverstayIn.cancel();
            mAnimPulseOverstayIn =null;
        }
        if(mAnimPulseOverstayOut != null && mAnimPulseOverstayOut.hasStarted()){
            mAnimPulseOverstayOut.cancel();
            mAnimPulseOverstayOut =null;
        }
    }

////////////////////////////////////////
//display / update / remove sticky notification while session runs/is paused / ends
    //TODO : when app is killed by user by swiping it from recent apps, we receive no callback => find a way to remove sticky notif in this case, because it stays there!
    //see : https://stackoverflow.com/questions/40169163/java-android-close-application-notifications-after-swiping-in-recents
    //see : https://stackoverflow.com/questions/19568315/how-to-handle-code-when-app-is-killed-by-swiping-in-android
    //see : https://stackoverflow.com/questions/48525884/how-to-detect-app-is-removed-from-recents-overview-screen-on-android-oreo
    private void displayStickyNotification(){
       Log.d(TAG, "displayStickyNotification");
        String totalPlannedTime = durationFormattedShort(mPlannedDuration);
        String remainingTime = durationFormattedComplete(mSessionRemainingDuration);
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
        String totalPlannedTime = durationFormattedShort(mPlannedDuration);
        String elapsedTimeComplete = durationFormattedComplete(mElapsedTime);
        String elapsedTimeShort = durationFormattedShort(mElapsedTime);
        String differenceBtwnPlannedAndElapsedTime = durationFormattedComplete(Math.min(mPlannedDuration, Math.abs(mPlannedDuration - mElapsedTime)));
        String notifText;
        Notification.Builder notifyBuilder;
        switch(sessionState){
            case SESSION_STATE_OVERSTAY_RUNNING:
                notifText = String.format(getString(R.string.session_overstay_notification_text), totalPlannedTime, differenceBtwnPlannedAndElapsedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
                break;
            case SESSION_STATE_PAUSED:
                notifText = String.format(getString(R.string.session_paused_notification_text), elapsedTimeComplete, totalPlannedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
                break;
            case SESSION_STATE_ENDED:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean recordStatsPref = prefs.getBoolean(getString(R.string.pref_switch_collect_stats_key), Boolean.valueOf(getString(R.string.default_collect_stats)));
                if(!recordStatsPref){ // user is not recording any stats : no need to show the end session notification
                    UiUtils.removeStickyNotification(mContext, STICKY_SESSION_RUNNING_NOTIFICATION_ID);
                    return;
                }else{
                    notifText = String.format(getString(R.string.session_ended_notification_text), elapsedTimeShort);
                }
                notifyBuilder = buildRunningSessionNotification(notifText, true);
                break;
            default: // SESSION_STATE_NORMAL_RUNNING
                notifText = String.format(getString(R.string.session_running_notification_text), differenceBtwnPlannedAndElapsedTime, totalPlannedTime);
                notifyBuilder = buildRunningSessionNotification(notifText, false);
        }
        NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            //Log.d(TAG, "updateStickyNotification::notifText = " + notifText);
            notifyManager.notify(STICKY_SESSION_RUNNING_NOTIFICATION_ID, notifyBuilder.build());
        }else{
            Log.e(TAG, "updateStickyNotification::notifyManager is null!!");
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
    private String durationFormattedComplete(long duration){
        return TimeOperations.convertMillisecondsToHoursMinutesAndSecondsString(
                duration,
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                getString(R.string.generic_string_SHORT_SECONDS),
                false);
    }

    private String durationFormattedShort(long duration){
        return TimeOperations.convertMillisecondsToHoursAndMinutesString(
                duration,
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                false);
    }
////////////////////////////////////////
//Helper method to set the device in Ringer mode silent or back to normal (or silent if SDK < Marshmallow) according to saved SharedPref
    private void handleDoNotDisturbMode(boolean activateDND) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean prefActiveDND = prefs.getBoolean(getString(R.string.pref_switch_do_not_disturb_key), Boolean.valueOf(getString(R.string.default_do_not_disturb)));
        if(prefActiveDND){
            //REMOVED usage of Do not disturb mode on versions >= marshmallow because we just want the ringtones shut, and DND also shuts mediaplayer off
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (activateDND) {
                    mPreviousRingerMode = audioManager.getRingerMode();
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    mRingerModeIsOverridden = true;
                    Log.d(TAG, "handleDoNotDisturbMode::DO activate DND::mPreviousRingerMode = " + mPreviousRingerMode);
                }else{
                    if(mPreviousRingerMode == -1){ // first passage
                        mPreviousRingerMode = AudioManager.RINGER_MODE_NORMAL;
                        //Warning on android 5.0 : setting to RINGER_MODE_NORMAL not always working is a reported "won't fix"  bug
                        // https://issuetracker.google.com/issues/37008264
                    }
                    audioManager.setRingerMode(mPreviousRingerMode);
                    mRingerModeIsOverridden = false;
                    Log.d(TAG, "handleDoNotDisturbMode::deactivate DND -> go back to previous mode : mPreviousRingerMode = " + mPreviousRingerMode);
                }
            } else {
                Log.e(TAG, "handleDoNotDisturbMode::audioManager == NULL!!");
            }
        }else{ //user has not activated do not disturb during sessions, but we just set the flag to true to prevent useless following passage here
            if (activateDND) {
                //do nothing but set flag to true
                mRingerModeIsOverridden = true;
            }else{
                //do nothing but set flag to false
                mRingerModeIsOverridden = false;
            }
        }
    }

////////////////////////////////////////
//Helper method to play sound set in savedSharedPref

// reported bug when playing ringtone: MediaPlayer: error (1, -19) ... could not trace precisely error cause
// consequence of bug: all sounds on device are broken until killing everyday app.
// seems the mediaplayer was not released.
// => added mMediaPlayer.setOnCompletionListener to release it when playing guiding mp3
// BUT notifs are played through Ringtone which has no release method...
// => changed code to recycle the existing Ringtone object if it would play the same sound instead of recreating a new instance each time...
// bug not reported again then

    //TODO: BUG 2x session sur support audio a crashé: une fois avant la fin du support, une fois au milieu de la sonnerie de fin... voir si pb avec pauses... fragment affichÃ© ensuite = mood start recorder pour session sans support audio

    private void playRingtoneSound(String playRingtoneSound) {
        //if(mAudioContentUri==null) { //override user's prefs if session is audio guided => no start or end sound
        // => removed because sometimes the audio support has no noticeable end, so we let the user decide and set the notification parameter to off if she does not want it
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String ringtoneString = "";
            Log.d(TAG, "playRingtoneSound " + playRingtoneSound);
            switch (playRingtoneSound) {
                case PLAY_START_SESSION_SOUND:
                    String defaultStartSoundUriString = uriFromRaw(getString(R.string.default_session_start_sound)).toString();
                    ringtoneString = prefs.getString(getString(R.string.pref_ringtone_start_key), defaultStartSoundUriString);
                    break;
                case PLAY_END_SESSION_SOUND:
                    String defaultEndSoundUriString = uriFromRaw(getString(R.string.default_session_end_sound)).toString();
                    ringtoneString = prefs.getString(getString(R.string.pref_ringtone_end_key), defaultEndSoundUriString);
                    break;
                case PLAY_INTERMEDIATE_INTERVAL_SESSION_SOUND:
                    if(mAudioContentUri==null) { //only play intermediate interval sounds when there is no audio support already playing, it is too disturbing
                        String defaultIntermediateIntervalSoundUriString = uriFromRaw(getString(R.string.default_intermediate_interval_sound)).toString();
                        ringtoneString = prefs.getString(getString(R.string.pref_ringtone_intermediate_intervals_key), defaultIntermediateIntervalSoundUriString);
                    }
                    break;
                default:
                    Log.e(TAG, "playRingtoneSound:: Wrong instruction to play session sound");
                    return;
            }
            Uri ringtoneUri = Uri.parse(ringtoneString);
            if (ringtoneUri != null) {
                if (ringtoneUri.toString().length() > 0) {
                    //suspend DND so user can hear ringtone (especially intermediate intervals)
                    handleDoNotDisturbMode(false);
                    if(mRingtone!=null){
                        Log.d(TAG, "playRingtoneSound::mRingtone is not null!");
                        if(mRingtone.isPlaying()){ // in case previous ringtone is still playing (long session start sound + short intermediate intervals for example)
                            Log.d(TAG, "playRingtoneSound::mRingtone.isPlaying() ... stopping...");
                            mRingtone.stop();
                        }
                    }
                    if(mRingtone != null && mLastPlayedRingtoneUri != null && mLastPlayedRingtoneUri.equals(ringtoneUri)){
                        //last time mRingtone played it was the same sound uri, recycle it
                        Log.d(TAG, "playRingtoneSound:: recycling mRingtone to play same sound again");
                    }else{
                        Log.d(TAG, "playRingtoneSound:: getting new Ringtone to play different sound");
                        mRingtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                    }
                    mLastPlayedRingtoneUri = ringtoneUri;
                    Log.d(TAG, "playRingtoneSound::mRingtone = " + mRingtone);
                    mRingtone.play();
                } else {
                    Log.d(TAG, "playRingtoneSound::session sound set to silence");
                }
            } else {
                Log.e(TAG, "playRingtoneSound::URI for session sound was null!");
            }
        //}
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
        void askForManualAudioDurationEntry();
        void onFinishSessionInProgressFragment(long duration, int pausesCount, int realVsPlannedDuration, String guideMp3);
    }

////////////////////////////////////////
//FULLSCREEN : SYSTEM UI HANDLING

    public void delayedHideSystemUI(int delayMillis) {
        Log.d(TAG, "delayedHideSystemUI");
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemUI();
        }
    };

    private void hideSystemUI() {
        Log.d(TAG, "hideSystemUI");
        mRootView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            Log.d(TAG, "onSystemUiVisibilityChange::visibility = " + visibility);
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) { //The system bars are visible
                Log.d(TAG, "onSystemUiVisibilityChange::The system bars are visible");
                delayedHideSystemUI(3000);
            } else {//The system bars are NOT visible
                Log.d(TAG, "onSystemUiVisibilityChange::The system bars are NOT visible");

            }
        }
    };
}
