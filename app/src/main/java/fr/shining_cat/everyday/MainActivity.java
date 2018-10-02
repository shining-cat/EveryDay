package fr.shining_cat.everyday;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.EveryDayRewardsDataRepository;
import fr.shining_cat.everyday.data.EveryDaySessionsDataRepository;
import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.dialogs.DialogFragmentInfos;
import fr.shining_cat.everyday.utils.TimeOperations;
import fr.shining_cat.everyday.utils.UiUtils;

public class MainActivity   extends BaseThemedActivity
                            implements  RewardsEscapedFragment.FragmentRewardsEscapedListener,
                                        EveryDaySessionsDataRepository.EveryDaySessionsRepoListener,
                                        HomePageButtonsFragment.HomePageButtonsFragmentListener,
                                        RewardsObtainedFragment.FragmentRewardObtainedListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();


    private final String AUDIO_GUIDED_SESSION = "audio guided session type";
    private final String TIMED_SESSION        = "timed session type";

    public static final int ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION = 513;

    private ProgressDialog mProgressDialog;
    private String mSessionTypeSelected;
    private boolean mUserHasFollowedAirplaneModeReminder;
    private boolean mSessionWithAudioGuideIsWaitingToStart;
    private boolean mSessionHasBeenStarted;
    Uri mAudioContentUri;

    //TODO : animer les transitions entre pages/fragments?
    //TODO : sounds for some interactions? (screenshots for sharing...)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        mUserHasFollowedAirplaneModeReminder = false;
        mSessionWithAudioGuideIsWaitingToStart = false;
        mSessionHasBeenStarted = false;
        //
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //check if rewards have already been generated, do it otherwise
        boolean prefRewardsGenerated = prefs.getBoolean(getString(R.string.pref_rewards_already_generated_key), false);
        //check first in sharedprefs, faster
        if(!prefRewardsGenerated) {
            RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
            //then confirm by checking if there are rewards in the DB, if not, we will generate them, if yes, we update the pref value to true...
            notifyActionByToast(getString(R.string.checking_existing_rewards_message));
            rewardViewModel.getNumberOfRows(everyDayRewardsRepoListener);
        }
        //in case a sticky notif still hangs around...
        UiUtils.removeStickyNotification(this, SessionInProgressFragment.STICKY_SESSION_RUNNING_NOTIFICATION_ID);
        //check for last recorded session, then eventually for streak break
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.looking_for_last_recorded_session_progress_dialog_title));
        mProgressDialog.show();
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getLatestRecordedSessionDate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog!=null){
            if(mProgressDialog.isShowing()){
                mProgressDialog.cancel();
            }
            mProgressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //user can be coming back from airplane mode reminder dialog // or sessionActivity (after finish) // or from audio guide intent chosing
        Log.d(TAG, "onResume");
        if(mUserHasFollowedAirplaneModeReminder){//we are back from the airplane mode settings screen
            prepareLaunchSession();
        }else if(mSessionWithAudioGuideIsWaitingToStart){
            launchSession();
        }else if(mSessionHasBeenStarted){//we are back from SessionActivity's finish()
            mSessionHasBeenStarted = false;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean prefAirplaneMode = prefs.getBoolean(getString(R.string.pref_switch_airplane_mode_key), Boolean.valueOf(getString(R.string.default_airplane_mode)));
            if(prefAirplaneMode && isAirplaneModeOn(this)) {
                openAirplaneModeSettings(); //open settings, no need for a dialog here
            }
        }
    }

    private void showHomepageButtons(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HomePageButtonsFragment homePageButtonsFragment = HomePageButtonsFragment.newInstance();
        fragmentTransaction.replace(R.id.main_activity_fragments_holder, homePageButtonsFragment, HomePageButtonsFragment.HOMEPAGEBUTTONSFRAGMENT_TAG);
        fragmentTransaction.commit();
    }

////////////////////////////////////////
//GENERATING AND INSERTING ALL POSSIBLE REWARDS INTO TABLE, and CALLBACKS
    private void fillRewardsTableWithAllPossibleCodes(){
        notifyActionByToast(getString(R.string.generating_possible_rewards_message));
        Log.d(TAG, "fillRewardsTableWithAllPossibleCodes::GENERATING POSSIBLE REWARDS");
        List<String> allCritersCodes = Critter.getAllPossibleCrittersCode();
        List<Reward> rewardsList = new ArrayList<>();
        for(String critterCode : allCritersCodes){
            /*//for testing purpose, DB is generated with altered rewards status (active or not, acuqisition and escaping date)
            int activeOrNot  = (Math.random() > .01)? Reward.STATUS_INACTIVE : Reward.STATUS_ACTIVE;
            int escapedOrNot = (Math.random() > .3)? Reward.STATUS_NOT_ESCAPED : Reward.STATUS_ESCAPED;
            //random timestamp between 01/01/2015 00:00 and 09/08/2018 00:00
            long acquisDate = Math.round(Math.random()*113705400000L) + 1420070400000L;
            long escapeDate = (escapedOrNot == Reward.STATUS_ESCAPED)? (Math.round(Math.random()*113705400000L) + 1420070400000L) : Reward.NO_ESCAPING_DATE;*/
            Reward reward = new Reward( critterCode,
                                        Critter.getCritterLevel(critterCode),
                                        0L,
                                        0L,
                                        Reward.STATUS_INACTIVE,
                                        Reward.STATUS_NOT_ESCAPED,
                                        critterCode, //default name is reward code
                                        Reward.DEFAULT_COLOR_IS_WHITE,
                                        Reward.DEFAULT_COLOR_IS_WHITE,
                                        Reward.DEFAULT_COLOR_IS_WHITE);
            rewardsList.add(reward);
        }
        notifyActionByToast(getString(R.string.generating_possible_rewards_message) + " - " + getString(R.string.generic_string_OK));
        Log.d(TAG, "fillRewardsTableWithAllPossibleCodes::INSERTING POSSIBLE REWARDS");
        notifyActionByToast(getString(R.string.inserting_possible_rewards_message));
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.insertMultiple(rewardsList, everyDayRewardsRepoListener);
    }

    private void storeRewardsWereGeneratedInPrefs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.pref_rewards_already_generated_key), true);
        editor.commit();
    }

////////////////////////////////////////
//LOSING REWARDS FOR BREAKING STREAKS

    private void compareDaysSinceLastSesionOrLastEscaping(long latestSessionRecordedDate){
        if(latestSessionRecordedDate > 0) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long lastRewardsEscapingDate = prefs.getLong(getString(R.string.pref_rewards_escaped_date_key), 0L);
            int numberOfDaysAlreadyPunished = 0;
            if (lastRewardsEscapingDate > latestSessionRecordedDate) {
                //rewards have already been lost for the period between lastRecordedSession and lastRewardsEscapingDate
                numberOfDaysAlreadyPunished = Math.max((TimeOperations.getNumberOfFullDaysBetween2TimestampsInMillis(latestSessionRecordedDate, lastRewardsEscapingDate) - 1), 0);
            }
            long now = System.currentTimeMillis();
            int numberOfDaysWithoutSession = Math.max((TimeOperations.getNumberOfFullDaysBetween2TimestampsInMillis(latestSessionRecordedDate, now) - 1), 0);
            Log.d(TAG, "compareDaysSinceLastSesionOrLastEscaping:: numberOfDaysWithoutSession = " + numberOfDaysWithoutSession + " / numberOfDaysAlreadyPunished = " + numberOfDaysAlreadyPunished);
            if(numberOfDaysWithoutSession > numberOfDaysAlreadyPunished) {
                escapeRewards(numberOfDaysWithoutSession, numberOfDaysAlreadyPunished);
            }else{
                showHomepageButtons();
            }
        }else{
            showHomepageButtons();
        }
    }

    private void escapeRewards(int numberOfDaysWithoutSession, int numberOfDaysAlreadyPunished){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RewardsEscapedFragment rewardsObtainedFragment = RewardsEscapedFragment.newInstance(numberOfDaysWithoutSession, numberOfDaysAlreadyPunished);
        fragmentTransaction.replace(R.id.main_activity_fragments_holder, rewardsObtainedFragment, RewardsEscapedFragment.FRAGMENT_REWARD_ESCAPED_TAG);
        fragmentTransaction.commit();
    }
    private void storeRewardsEscapedInPrefs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        long now = Calendar.getInstance().getTimeInMillis();
        editor.putLong(getString(R.string.pref_rewards_escaped_date_key), now);
        editor.commit();
    }

////////////////////////////////////////
//RewardsEscapedFragment callbacks
    @Override
    public void onValidateFragmentRewardsEscaped(List<Reward> rewards) {
        Log.d(TAG, "onValidateFragmentRewardObtained:: rewards size = " + rewards.size());
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.updateMultipleReward(rewards, everyDayRewardsRepoListener);
    }

////////////////////////////////////////
//HomePageButtonsFragment callbacks

    @Override
    public void navigateToStats() {
        Intent myIntent = new Intent(this, VizActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void navigateToRewards() {
        Intent myIntent = new Intent(this, RewardsActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void startAudioSession() {
        mSessionTypeSelected = AUDIO_GUIDED_SESSION;
        checkAirplaneModeIfNecessary();
    }

    @Override
    public void startTimedSession() {
        mSessionTypeSelected = TIMED_SESSION;
        checkAirplaneModeIfNecessary();
    }

    @Override
    public void testEscapeRewards(int numberOfDaysWithoutSession, int numberOfDaysAlreadyPunished) {
        escapeRewards(numberOfDaysWithoutSession, numberOfDaysAlreadyPunished);
    }

    @Override
    public void testAttributeRewards(int level, int[] rewardsChances) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RewardsObtainedFragment rewardsObtainedFragment = RewardsObtainedFragment.newInstance(level, rewardsChances);
        fragmentTransaction.replace(R.id.main_activity_fragments_holder, rewardsObtainedFragment, RewardsObtainedFragment.FRAGMENT_REWARD_OBTAINED_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void testResetRewards() {
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.deleteAllRewards(everyDayRewardsRepoListener);
    }


    private void checkAirplaneModeIfNecessary(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefAirplaneMode = prefs.getBoolean(getString(R.string.pref_switch_airplane_mode_key), Boolean.valueOf(getString(R.string.default_airplane_mode)));
        if(prefAirplaneMode && !isAirplaneModeOn(this)) {
            remindUSerOfAirplaneModeSettings();
        }else {
            prepareLaunchSession();
        }
    }

    private void prepareLaunchSession(){
        mUserHasFollowedAirplaneModeReminder = false;
        switch (mSessionTypeSelected){
            case AUDIO_GUIDED_SESSION: //open file chooser
                Intent chooseMp3Intent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseMp3Intent.addCategory(Intent.CATEGORY_OPENABLE);
                chooseMp3Intent.setType("audio/*");
                this.startActivityForResult(chooseMp3Intent, ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION);
                break;
            case TIMED_SESSION: //start session immediately
            default:
                launchSession();
        }
    }

    private void launchSession(){
        mSessionHasBeenStarted = true;
        mSessionWithAudioGuideIsWaitingToStart = false;
        mUserHasFollowedAirplaneModeReminder = false;
        if(mAudioContentUri != null) { //must be AUDIO_GUIDED_SESSION
            Log.d(TAG, "onActivityResult::audioContentUri = " + mAudioContentUri);
            Intent myIntent = new Intent(this, SessionActivity.class);
            myIntent.putExtra(SessionActivity.SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY, mAudioContentUri);
            startActivity(myIntent);
        }else{
            mSessionHasBeenStarted = true;
            Intent myIntent = new Intent(this, SessionActivity.class);
            startActivity(myIntent);
        }
    }
////////////////////////////////////////
//open airplane mode section from device settings

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void remindUSerOfAirplaneModeSettings() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.airplane_mode_starting_session_reminder_dialog_title));
            builder.setMessage(getString(R.string.airplane_mode_starting_session_reminder_dialog_text));
            builder.setNegativeButton(getString(R.string.generic_string_PASS), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    prepareLaunchSession();
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.generic_string_OPEN_SETTING), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mUserHasFollowedAirplaneModeReminder = true;
                    openAirplaneModeSettings();
                }
            });
            builder.show();
    }

    private void openAirplaneModeSettings(){
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

////////////////////////////////////////
//EveryDaySessionsDataRepository callbacks
    @Override
    public void onGetLatestRecordedSessionDateComplete(long latestSessionRecordedDate) {
        Log.d(TAG, "onGetLatestRecordedSessionDateComplete:: latestSessionRecordedDate = " + latestSessionRecordedDate);
        if(mProgressDialog!=null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        compareDaysSinceLastSesionOrLastEscaping(latestSessionRecordedDate);
    }
    @Override
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {}
    @Override
    public void ondeleteOneSessionRecordComplete(int result) {}
    @Override
    public void ondeleteAllSessionsRecordsComplete(int result) {}
    @Override
    public void onInsertOneSessionRecordComplete(long result) {}
    @Override
    public void onInsertMultipleSessionsRecordsComplete(Long[] result) {}
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {}

////////////////////////////////////////
//EveryDayRewardsDataRepository callbacks
    EveryDayRewardsDataRepository.EveryDayRewardsRepoListener everyDayRewardsRepoListener = new EveryDayRewardsDataRepository.EveryDayRewardsRepoListener() {
        @Override
        public void onGetNumberOfRowsComplete(Integer result) {
            if(result == 0){
                Log.d(TAG, "onGetNumberOfRowsComplete:: table is EMPTY!");
                fillRewardsTableWithAllPossibleCodes();
            }else{
                notifyActionByToast(getString(R.string.checking_existing_rewards_message) + " - " + getString(R.string.generic_string_OK));
                storeRewardsWereGeneratedInPrefs();
                Log.d(TAG, "onGetNumberOfRowsComplete:: number of rewards in table = " + result);
            }
        }
        @Override
        public void onInsertMultipleRewardsComplete(Long[] result) {
            Log.d(TAG, "onInsertMultipleRewardsComplete:: number of rewards inserted in table = " + result.length);
            notifyActionByToast(getString(R.string.inserting_possible_rewards_message) + " - " + getString(R.string.generic_string_OK));
            storeRewardsWereGeneratedInPrefs();
        }
        @Override
        public void onUpdateMultipleRewardComplete(int result) {
            Log.d(TAG, "onUpdateMultipleRewardComplete:: number of rewards updated : " + result);
            notifyActionByToast(getString(R.string.rewards_escaped_recorded_message));
            showHomepageButtons();
            storeRewardsEscapedInPrefs(); //only record this here in case user kills the app or use back navigation to exit before the recording of escaped rewards
        }
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
        public void ondeleteAllRewardComplete(int result) {
            //TODO: temp for testing rewards attribution
            notifyActionByToast("Table des récompenses vidée");
            fillRewardsTableWithAllPossibleCodes();
            //TODO: temp for testing rewards attribution
        }
    };


    //TODO: temp for testing rewards attribution
    @Override
    public void onValidateFragmentRewardObtained(List<Reward> rewards) {
        Log.d(TAG, "onValidateFragmentRewardObtained:: rewards size = " + rewards.size());
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.updateMultipleReward(rewards, new EveryDayRewardsDataRepository.EveryDayRewardsRepoListener() {
            @Override
            public void onUpdateMultipleRewardComplete(int result) {
                Log.d(TAG, "onUpdateMultipleRewardComplete:: number of rewards updated : " + result);
                notifyActionByToast(getString(R.string.rewards_obtained_recorded_message));
                showHomepageButtons();
            }
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
            public void onInsertMultipleRewardsComplete(Long[] result) {}
            @Override
            public void onUpdateOneRewardComplete(int result) {}
            @Override
            public void ondeleteOneRewardComplete(int result) {}
            @Override
            public void ondeleteAllRewardComplete(int result) {}
        });
    }
//TODO: temp for testing rewards attribution

////////////////////////////////////////
//helper method to show a toast with custom message
    private void notifyActionByToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_infos) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragmentInfos dialogFragmentInfos = DialogFragmentInfos.newInstance();
            dialogFragmentInfos.show(fm, DialogFragmentInfos.DIALOG_FRAGMENT_INFOS_TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


////////////////////////////////////////
//INCOMING INTENTS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION:
                    //WARNING : data coming back from Intent.ACTION_GET_CONTENT is not necessarily a File (but it can be which could lead to not detecting error at first) but has to be handled as a content Uri
                    mAudioContentUri = data.getData();
                    mSessionWithAudioGuideIsWaitingToStart = true;
                    //wait for onresume (supposed to always come AFTER onactivityresult) to really launch session because there is the text to show the airplane mode reminder, which would fire now instead of at the end of session
                    break;
            }
        }
    }



}
