package fr.shining_cat.everyday;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.data.EveryDaySessionsDataRepository;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.utils.TimeOperations;
import fr.shining_cat.everyday.utils.UiUtils;

////////////////////////////////////////
//This activity holds the STATS fragments : VizSessionDetailsViewPagerFragment, VizSessionsListFragment, VizSessionsCalendarFragment, ViewStatsFragment
public class VizActivity extends BaseThemedActivity
                        implements  PreRecordFragment.FragmentPreRecordListener,
                                    PostRecordFragment.PostRecordFragmentListener,
                                    VizSessionsListAdapter.SessionsListAdapterListener,
                                    EveryDaySessionsDataRepository.EveryDaySessionsRepoListener,
                                    VizSessionsCalendarFragment.SessionsCalendarListener,
                                    VizStatsMainFragment.VizStatsMainFragmentListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();



    private final String CURRENT_SCREEN_KEY = "current screen key";
    private final String PREVIOUS_SCREEN_KEY = "previous screen key";
    private final String ANTE_PREVIOUS_SCREEN_KEY = "ante-previous screen key";
    private final String CURRENT_SESSION_KEY = "current session key";
    private final String CURRENT_EDIT_OR_NEW_SESSION_RECORDING_KEY = "current or new session recording key";
    private final String CURRENT_EDITTED_SESSION_KEY = "currently editted session key";
    private final String START_MOOD_KEY = "start mood key";
    private final String END_MOOD_KEY = "end mood key";
    private final String CURRENT_MONTH_SHOWN_IN_CALENDAR_KEY = "current month shown in calendar key";

    private final String SCREEN_VIZ_PRE_RECORD_VIEW = "pre record fragment screen";
    private final String SCREEN_VIZ_POST_RECORD_VIEW = "post record fragment screen";
    private final String SCREEN_VIZ_LIST_VIEW = "list view viz screen";
    private final String SCREEN_VIZ_SESSION_DETAILS_VIEW = "session details view viz screen";
    private final String SCREEN_VIZ_CALENDAR_VIEW = "calendar view viz screen";
    private final String SCREEN_VIZ_STATS_MAIN_VIEW = "stats view viz main screen";
    private final String SCREEN_VIZ_STATS_DAY_VIEW = "stats view viz day screen";
    private final String SCREEN_VIZ_STATS_WEEK_VIEW = "stats view viz week screen";
    private final String SCREEN_VIZ_STATS_MONTH_VIEW = "stats view viz month screen";
    private final String SCREEN_VIZ_STATS_DURATION_VIEW = "stats view viz duration screen";
    private final String SCREEN_VIZ_STATS_MP3_VIEW = "stats view viz mp3 screen";

    private final String EDITTING_OR_DELETING_EXISTING_SESSION = "editting or deleting an existing session";
    private final String ADDING_NEW_SESSION = "currently creating a new session";

    private List<SessionRecord> mAllSessionsNotLive;

    private String mCurrentScreen;
    private String mPreviousScreen;
    private String mAntePreviousScreen;
    private MoodRecord mStartMood;
    private MoodRecord mEndMood;
    private String mEditOrNewSessionRecording;
    private SessionRecord mCurrentEdittedSessionRecord;
    private Long mCurrentMonthShownInCalendarScreen;
    private VizSessionDetailsViewPagerFragment mVizSessionDetailsViewPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viz);
        mEditOrNewSessionRecording = "";
        // today
        Calendar today = Calendar.getInstance();
        // reset hour, minutes, seconds and millis
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        mCurrentMonthShownInCalendarScreen = today.getTime().getTime();
        if(savedInstanceState==null){
            Log.d(TAG, "onCreate::savedInstanceState is null");
            showSessionsCalendarView();
        }else{
            Log.d(TAG, "onCreate::getting saved state");
            mCurrentScreen = savedInstanceState.getString(CURRENT_SCREEN_KEY);
            mPreviousScreen= savedInstanceState.getString(PREVIOUS_SCREEN_KEY);
            mAntePreviousScreen= savedInstanceState.getString(ANTE_PREVIOUS_SCREEN_KEY);
            mEditOrNewSessionRecording = savedInstanceState.getString(CURRENT_EDIT_OR_NEW_SESSION_RECORDING_KEY);
            mCurrentEdittedSessionRecord = (SessionRecord) savedInstanceState.getSerializable(CURRENT_EDITTED_SESSION_KEY);
            mStartMood = (MoodRecord) savedInstanceState.getSerializable(START_MOOD_KEY);
            mEndMood = (MoodRecord) savedInstanceState.getSerializable(END_MOOD_KEY);
            mCurrentMonthShownInCalendarScreen = savedInstanceState.getLong(CURRENT_MONTH_SHOWN_IN_CALENDAR_KEY);
            if(mCurrentScreen!=null) {
                switch (mCurrentScreen) {
                    case SCREEN_VIZ_PRE_RECORD_VIEW:
                        //handled inside prerecordfragment, nothing to do here
                        break;
                    case SCREEN_VIZ_POST_RECORD_VIEW:
                        //handled inside prerecordfragment, nothing to do here
                        break;
                    case SCREEN_VIZ_SESSION_DETAILS_VIEW:
                        SessionRecord session = (SessionRecord) savedInstanceState.getSerializable(CURRENT_SESSION_KEY);
                        if(session!=null){
                            showSessionDetailsView(session);
                        }else{
                            Log.e(TAG, "onCreate::COULD NOT GET SESSION OBJECT " );
                        }
                        break;
                    case SCREEN_VIZ_LIST_VIEW:
                        showSessionsListView();
                        break;
                    case SCREEN_VIZ_CALENDAR_VIEW:
                        showSessionsCalendarView();
                        break;
                    case SCREEN_VIZ_STATS_MAIN_VIEW:
                        showSessionStatsMainView();
                        break;
                    case SCREEN_VIZ_STATS_DAY_VIEW:
                        showSessionsStatsDayView();
                        break;
                    case SCREEN_VIZ_STATS_WEEK_VIEW:
                        showSessionsStatsWeekView();
                        break;
                    case SCREEN_VIZ_STATS_MONTH_VIEW:
                        showSessionsStatsMonthView();
                        break;
                    case SCREEN_VIZ_STATS_DURATION_VIEW:
                        showSessionsStatsDurationView();
                        break;
                    case SCREEN_VIZ_STATS_MP3_VIEW:
                        showSessionsStatsMp3View();
                        break;
                }
            }else{
                showSessionsCalendarView();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_SCREEN_KEY, mCurrentScreen);
        outState.putString(CURRENT_EDIT_OR_NEW_SESSION_RECORDING_KEY, mEditOrNewSessionRecording);
        if(mPreviousScreen != null) {
            outState.putString(PREVIOUS_SCREEN_KEY, mPreviousScreen);
        }
        if(mAntePreviousScreen != null) {
            outState.putString(ANTE_PREVIOUS_SCREEN_KEY, mAntePreviousScreen);
        }
        outState.putLong(CURRENT_MONTH_SHOWN_IN_CALENDAR_KEY, mCurrentMonthShownInCalendarScreen);
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW) && mVizSessionDetailsViewPagerFragment !=null) {
            SessionRecord currentSession = mVizSessionDetailsViewPagerFragment.getCurrentSessionRecord();
            outState.putSerializable(CURRENT_SESSION_KEY, currentSession);
        }
        outState.putSerializable(CURRENT_EDITTED_SESSION_KEY, mCurrentEdittedSessionRecord);
        outState.putSerializable(START_MOOD_KEY, mStartMood);
        outState.putSerializable(END_MOOD_KEY, mEndMood);
        super.onSaveInstanceState(outState);
    }

////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viz, menu);
        MenuItem addSessionButton = menu.findItem(R.id.action_add);
        MenuItem viewCalendarButton = menu.findItem(R.id.action_view_calendar);
        MenuItem viewListButton = menu.findItem(R.id.action_view_list);
        MenuItem viewStatsButton = menu.findItem(R.id.action_view_stats);
        MenuItem editSessionButton = menu.findItem(R.id.action_edit);
        MenuItem viewStatsDayButton = menu.findItem(R.id.action_view_stats_day);
        MenuItem viewStatsWeekButton = menu.findItem(R.id.action_view_stats_week);
        MenuItem viewStatsMonthButton = menu.findItem(R.id.action_view_stats_month);
        MenuItem viewStatsDurationButton = menu.findItem(R.id.action_view_stats_duration);
        MenuItem viewStatsMp3Button = menu.findItem(R.id.action_view_stats_mp3);
        setTitle(getString(R.string.statistics));
        switch (mCurrentScreen){
            case SCREEN_VIZ_PRE_RECORD_VIEW :
            case SCREEN_VIZ_POST_RECORD_VIEW :
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(false);
                break;
            case SCREEN_VIZ_CALENDAR_VIEW :
                addSessionButton.setVisible(true);
                viewListButton.setVisible(true);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(true);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(false);
                break;
            case SCREEN_VIZ_LIST_VIEW :
                addSessionButton.setVisible(true);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(true);
                viewStatsButton.setVisible(true);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(false);
                break;
            case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(true);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(false);
                break;
            case SCREEN_VIZ_STATS_MAIN_VIEW:
                addSessionButton.setVisible(false);
                viewListButton.setVisible(true);
                viewCalendarButton.setVisible(true);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(false);
                break;
            case SCREEN_VIZ_STATS_DAY_VIEW:
                setTitle(getString(R.string.generic_string_BACK));
                //
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(false);
                viewStatsWeekButton.setVisible(true);
                viewStatsMonthButton.setVisible(true);
                viewStatsDurationButton.setVisible(true);
                viewStatsMp3Button.setVisible(true);
                break;
            case SCREEN_VIZ_STATS_WEEK_VIEW:
                setTitle(getString(R.string.generic_string_BACK));
                //
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(true);
                viewStatsWeekButton.setVisible(false);
                viewStatsMonthButton.setVisible(true);
                viewStatsDurationButton.setVisible(true);
                viewStatsMp3Button.setVisible(true);
                break;
            case SCREEN_VIZ_STATS_MONTH_VIEW:
                setTitle(getString(R.string.generic_string_BACK));
                //
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(true);
                viewStatsWeekButton.setVisible(true);
                viewStatsMonthButton.setVisible(false);
                viewStatsDurationButton.setVisible(true);
                viewStatsMp3Button.setVisible(true);
                break;
            case SCREEN_VIZ_STATS_DURATION_VIEW:
                setTitle(getString(R.string.generic_string_BACK));
                //
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(true);
                viewStatsWeekButton.setVisible(true);
                viewStatsMonthButton.setVisible(true);
                viewStatsDurationButton.setVisible(false);
                viewStatsMp3Button.setVisible(true);
                break;
            case SCREEN_VIZ_STATS_MP3_VIEW:
                setTitle(getString(R.string.generic_string_BACK));
                //
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                //
                viewStatsDayButton.setVisible(true);
                viewStatsWeekButton.setVisible(true);
                viewStatsMonthButton.setVisible(true);
                viewStatsDurationButton.setVisible(true);
                viewStatsMp3Button.setVisible(false);
                break;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_add:
                mEditOrNewSessionRecording = ADDING_NEW_SESSION;
                //showDialogFragmentPreRecord();
                mPreviousScreen = mCurrentScreen;
                showFragmentPreRecord(null);
                return true;
            case R.id.action_view_list:
                showSessionsListView();
                return true;
            case R.id.action_view_calendar:
                showSessionsCalendarView();
                return true;
            case R.id.action_view_stats:
                showSessionStatsMainView();
                return true;
            case R.id.action_edit:
                //this button is only visible in SCREEN_VIZ_SESSION_DETAILS_VIEW
                //get current shown session object in VizSessionDetailsViewPagerFragment
                if(mVizSessionDetailsViewPagerFragment !=null) {
                    SessionRecord currentSession = mVizSessionDetailsViewPagerFragment.getCurrentSessionRecord();
                    showEditOrDeleteSessionDialog(currentSession);
                }else{
                    Log.e(TAG, "onOptionsItemSelected::action_edit = mVizSessionDetailsViewPagerFragment is NULL!");
                }
                return true;
            case R.id.action_view_stats_day:
                showSessionsStatsDayView();
                return true;
            case R.id.action_view_stats_week:
                showSessionsStatsWeekView();
                return true;
            case R.id.action_view_stats_month:
                showSessionsStatsMonthView();
                return true;
            case R.id.action_view_stats_duration:
                showSessionsStatsDurationView();
                return true;
            case R.id.action_view_stats_mp3:
                showSessionsStatsMp3View();
                return true;
            case android.R.id.home://overriding up button behaviour to set different destination wether we're in top-level fragment or in low-level
                if(overrideNavigateBackAndUp()){
                    return true;
                }else{
                    return super.onOptionsItemSelected(item);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

////////////////////////////////////////
//overriding BACK and UP navigation to manage inner fragments and update options menu items
    @Override
    public void onBackPressed() {
        if(!overrideNavigateBackAndUp()){
            super.onBackPressed();
        }
    }

    private boolean overrideNavigateBackAndUp(){
        Log.d(TAG, "overrideNavigateBackAndUp::mCurrentScreen = " + mCurrentScreen + " / mPreviousScreen = " + mPreviousScreen + " / mAntePreviousScreen = " + mAntePreviousScreen);
        invalidateOptionsMenu();
        switch (mCurrentScreen){
            case SCREEN_VIZ_PRE_RECORD_VIEW : // go back to viz screen previously shown
                switch (mPreviousScreen){
                    case SCREEN_VIZ_CALENDAR_VIEW :
                        showSessionsCalendarView();
                        return true;
                    case SCREEN_VIZ_LIST_VIEW :
                        showSessionsListView();
                        return true;
                    case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                        showSessionDetailsView(mCurrentEdittedSessionRecord);
                        return true;
                    default:
                        return false;
                }
            case SCREEN_VIZ_POST_RECORD_VIEW : //go back to pre-record fragment
                showFragmentPreRecord(mStartMood); // we have stored what user eventually entered in prerecord fragment which can be different from mCurrentEdittedSessionRecord.getStartMood()
                return true;
            case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                switch (mPreviousScreen){
                    case SCREEN_VIZ_CALENDAR_VIEW :
                        showSessionsCalendarView();
                        return true;
                    case SCREEN_VIZ_LIST_VIEW :
                        showSessionsListView();
                        return true;
                    case SCREEN_VIZ_PRE_RECORD_VIEW : //user has come down the path of list or calendar, then details, then edit, we need to be able to get back the same path
                        switch (mAntePreviousScreen) {
                            case SCREEN_VIZ_CALENDAR_VIEW:
                                showSessionsCalendarView();
                                return true;
                            case SCREEN_VIZ_LIST_VIEW:
                                showSessionsListView();
                                return true;
                            default:
                                return false;
                        }
                    default:
                        return false;
                }
            case SCREEN_VIZ_STATS_DAY_VIEW:
            case SCREEN_VIZ_STATS_WEEK_VIEW:
            case SCREEN_VIZ_STATS_MONTH_VIEW:
            case SCREEN_VIZ_STATS_DURATION_VIEW:
            case SCREEN_VIZ_STATS_MP3_VIEW: //back to main stats page
                showSessionStatsMainView();
                return true;
            case SCREEN_VIZ_CALENDAR_VIEW :
            case SCREEN_VIZ_LIST_VIEW :
            case SCREEN_VIZ_STATS_MAIN_VIEW :
            default:
                return false;
        }
    }

////////////////////////////////////////
//DIFFERENT SCREENS (fragments : VizSessionsListFragment, VizSessionsCalendarFragment, ViewStatsFragment, VizSessionDetailsViewPagerFragment)
    private void showSessionsListView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizSessionsListFragment vizSessionsListFragment = VizSessionsListFragment.newInstance();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizSessionsListFragment, VizSessionsListFragment.VIEW_SESSION_LIST_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_LIST_VIEW;
        invalidateOptionsMenu();
    }
    //sessionsListAdapter callbacks for list items interactions
    @Override
    public void onClickOnSession(SessionRecord clickedSession) {
        showSessionDetailsView(clickedSession);
    }
    @Override
    public void onLongClickOnSession(SessionRecord clickedSession) {
        showEditOrDeleteSessionDialog(clickedSession);
    }
    //
    private void showSessionsCalendarView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizSessionsCalendarFragment vizSessionsCalendarFragment = VizSessionsCalendarFragment.newInstance(mCurrentMonthShownInCalendarScreen);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizSessionsCalendarFragment, VizSessionsCalendarFragment.VIEW_SESSION_CALANEDAR_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_CALENDAR_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionDetailsView(SessionRecord sessionToShow){
        Log.d(TAG, "showSessionDetailsView");
        mPreviousScreen = mCurrentScreen;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mVizSessionDetailsViewPagerFragment != null) {
            mVizSessionDetailsViewPagerFragment = null;
        }
        mVizSessionDetailsViewPagerFragment = VizSessionDetailsViewPagerFragment.newInstance();
        mVizSessionDetailsViewPagerFragment.setStartingSessionDetailsWithSessionRecord(sessionToShow);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, mVizSessionDetailsViewPagerFragment, VizSessionDetailsViewPagerFragment.VIEW_PAGER_SESSION_DETAILS_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_SESSION_DETAILS_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void getAllSessionsRecordsInBunch(){
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getAllSessionsRecordsInBunch(this);
    }
    //
    private void showSessionStatsMainView(){
        //we prepare and show fragment (with no data, and a loading animation)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMainFragment vizStatsMainFragment = VizStatsMainFragment.newInstance();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMainFragment, VizStatsMainFragment.VIEW_STATS_MAIN_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MAIN_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsMainFragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }

    //
    private void showSessionsStatsDayView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsDayFragment vizStatsDayFragment = VizStatsDayFragment.newInstance();
        //vizStatsDayFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsDayFragment, VizStatsDayFragment.VIEW_STATS_DAY_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_DAY_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsDayFragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }
    //
    private void showSessionsStatsWeekView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsWeekFragment vizStatsWeekFragment = VizStatsWeekFragment.newInstance();
        //vizStatsWeekFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsWeekFragment, VizStatsWeekFragment.VIEW_STATS_WEEK_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_WEEK_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsWeekFragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }
    //
    private void showSessionsStatsMonthView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMonthFragment vizStatsMonthFragment = VizStatsMonthFragment.newInstance();
        //vizStatsMonthFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMonthFragment, VizStatsMonthFragment.VIEW_STATS_MONTH_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MONTH_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsMonthFragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }
    //
    private void showSessionsStatsDurationView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsDurationFragment vizStatsDurationFragment = VizStatsDurationFragment.newInstance();
        //vizStatsDurationFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsDurationFragment, VizStatsDurationFragment.VIEW_STATS_DURATION_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_DURATION_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsDurationFragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }
    private void showSessionsStatsMp3View(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMp3Fragment vizStatsMp3Fragment = VizStatsMp3Fragment.newInstance();
        vizStatsMp3Fragment.setNoMp3LabelString(getString(R.string.mp3_stats_no_mp3_file_label));
        //vizStatsMp3Fragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMp3Fragment, VizStatsMp3Fragment.VIEW_STATS_MP3_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MP3_VIEW;
        invalidateOptionsMenu();
        //query data not live : or using cached data
        if(mAllSessionsNotLive == null) {
            getAllSessionsRecordsInBunch();
        }else{
            vizStatsMp3Fragment.setAllSessionsList(mAllSessionsNotLive);
        }
    }


////////////////////////////////////////
//edit or delete a session
    private void showEditOrDeleteSessionDialog(SessionRecord sessionRecord){
        mEditOrNewSessionRecording = EDITTING_OR_DELETING_EXISTING_SESSION;
        mCurrentEdittedSessionRecord = sessionRecord;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_session_dialog_title));
        //
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        //
        String message = sdf.format(mCurrentEdittedSessionRecord.getStartTimeOfRecord());
        message += " - " + tdf.format(mCurrentEdittedSessionRecord.getStartTimeOfRecord());
        message += " - " + TimeOperations.convertMillisecondsToHoursAndMinutesString(
                mCurrentEdittedSessionRecord.getSessionRealDuration(),
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                false);
        builder.setMessage(message);
        builder.setNegativeButton(getString(R.string.generic_string_DELETE), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showConfirmDeleteSessionDialog();
            }
        });
        builder.setNeutralButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mEditOrNewSessionRecording = "";
            }
        });
        builder.setPositiveButton(getString(R.string.generic_string_EDIT), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                editSessionStartDialog();
            }
        });
        builder.show();
    }
////////////////////////////////////////
//DELETE a session
    private void showConfirmDeleteSessionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_session_dialog_title));
        //
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        //
        String message = sdf.format(mCurrentEdittedSessionRecord.getStartTimeOfRecord());
        message += " - " + tdf.format(mCurrentEdittedSessionRecord.getStartTimeOfRecord());
        message += " - " + TimeOperations.convertMillisecondsToHoursAndMinutesString(
                mCurrentEdittedSessionRecord.getSessionRealDuration(),
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                false);
        builder.setMessage(message);
        builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mEditOrNewSessionRecording = "";
            }
        });
        builder.setPositiveButton(getString(R.string.generic_string_DELETE), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteSessionRecord();
            }
        });
        builder.show();
    }

    private void deleteSessionRecord(){
        //delete selected session
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        Log.d(TAG, "deleteSessionRecord::id = " + mCurrentEdittedSessionRecord.getId());
        sessionRecordViewModel.deleteOneSession(mCurrentEdittedSessionRecord, this);
        mEditOrNewSessionRecording = "";
    }

////////////////////////////////////////
//Manually create or edit a session

    private void editSessionStartDialog(){
        if(mEditOrNewSessionRecording.equals(EDITTING_OR_DELETING_EXISTING_SESSION) && mCurrentEdittedSessionRecord != null){
            MoodRecord startMood = mCurrentEdittedSessionRecord.getStartMood();
            if(mPreviousScreen != null){
                mAntePreviousScreen = mPreviousScreen; //this is saved in case user has come down the path of list or calendar, then details, then edit, we need to be able to get back the same path
            }
            mPreviousScreen = mCurrentScreen;
            showFragmentPreRecord(startMood);
        }else{
            Log.e(TAG, "editSessionStartDialog:: No session found to edit!");
        }
    }

    private void showFragmentPreRecord(MoodRecord startMood) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PreRecordFragment preRecordFragment = PreRecordFragment.newInstance(true);
        if(startMood != null){
            preRecordFragment.presetContent(startMood);
        } // else : manual new session creation, no content preset
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, preRecordFragment, PreRecordFragment.FRAGMENT_PRE_RECORD_MANUAL_ENTRY_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_PRE_RECORD_VIEW;
        invalidateOptionsMenu();
    }

////////////////////////////////////////
//VizStatsMainFragment fragment callbacks

    @Override
    public void onOpenViewStatsDay() {showSessionsStatsDayView();}

    @Override
    public void onOpenViewStatsWeek() {showSessionsStatsWeekView();}

    @Override
    public void onOpenViewStatsMonth() {showSessionsStatsMonthView();}

    @Override
    public void onOpenViewStatsDuration() {showSessionsStatsDurationView();}

    @Override
    public void onOpenViewStatsMp3() {showSessionsStatsMp3View();}

////////////////////////////////////////
//VizSessionsCalendarFragment fragment callbacks
    @Override
    public void onSaveCurrentDate(Long currentMonthDateAsLong) {
        mCurrentMonthShownInCalendarScreen = currentMonthDateAsLong;
    }
////////////////////////////////////////
//PreRecord fragment callbacks
    @Override
    public void onCancelFragmentPreRecord() {
        mEditOrNewSessionRecording = "";
        overrideNavigateBackAndUp();
    }

    @Override
    public void onValidateFragmentPreRecord(MoodRecord moodRecord) {
        mStartMood = moodRecord;
        FragmentManager fragmentManager = getSupportFragmentManager();
        PostRecordFragment postRecordFragment = PostRecordFragment.newInstance(true, 0, 0, 0, "", mStartMood.getTimeOfRecord());
        if(mEditOrNewSessionRecording.equals(EDITTING_OR_DELETING_EXISTING_SESSION) && mCurrentEdittedSessionRecord != null){
            postRecordFragment.presetContent(mCurrentEdittedSessionRecord.getEndMood());
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, postRecordFragment, PostRecordFragment.FRAGMENT_POST_RECORD_MANUAL_ENTRY_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_POST_RECORD_VIEW;
        invalidateOptionsMenu();
    }
////////////////////////////////////////
//PostRecord Dialog fragment callbacks
    @Override
    public void onCancelPostRecordFragment(Boolean isManualEntry) {
        UiUtils.hideSoftKeyboard(this);
        if(isManualEntry) {
            //go back to prerecord fragment
            showFragmentPreRecord(mStartMood); // we have stored what user eventually entered in prerecord fragment which can be different from mCurrentEdittedSessionRecord.getStartMood()
        }else{
            Log.e(TAG, "onCancelPostRecordFragment::isManualEntry should always be true in this context");
        }
    }

    @Override
    public void onValidatePostRecordFragment(MoodRecord moodRecord) {
        UiUtils.hideSoftKeyboard(this);
        mEndMood = moodRecord;
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        if(mEditOrNewSessionRecording.equals(EDITTING_OR_DELETING_EXISTING_SESSION) && mCurrentEdittedSessionRecord != null){
            //update existing record
            Log.d(TAG, "onValidateDialogFragmentPostRecord::calling update id = " + mCurrentEdittedSessionRecord.getId());
            sessionRecordViewModel.updateWithMoods(mCurrentEdittedSessionRecord.getId(), mStartMood, mEndMood, this);
        } else {
            // insert new record
            sessionRecordViewModel.insertWithMoods(mStartMood, mEndMood, this);
        }
        mEditOrNewSessionRecording = "";
        switch (mPreviousScreen){
            case SCREEN_VIZ_CALENDAR_VIEW :
                showSessionsCalendarView();
                break;
            case SCREEN_VIZ_LIST_VIEW :
                showSessionsListView();
                break;
            case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                showSessionDetailsView(mCurrentEdittedSessionRecord);
                break;
        }
    }

////////////////////////////////////////
//EveryDaySessionsDataRepository callbacks
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            mVizSessionDetailsViewPagerFragment.setStartingSessionDetailsWithSessionRecord(mCurrentEdittedSessionRecord);
        }
        Toast.makeText(this, R.string.update_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetLatestRecordedSessionDateComplete(long latestSessionRecordedDate) {}

    @Override
    public void ondeleteOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            // go back to first entry? maybe exit to list/calendar view?
            //mVizSessionDetailsViewPagerFragment.setStartingSessionDetailsWithIndex(0);
        }
        Toast.makeText(this, R.string.delete_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onInsertOneSessionRecordComplete(long result) {
        Toast.makeText(this, R.string.insert_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    //not used here
    @Override
    public void ondeleteAllSessionsRecordsComplete(int result) {}
    @Override
    public void onInsertMultipleSessionsRecordsComplete(Long[] result) {}
    @Override
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {
        mAllSessionsNotLive = allSessions;
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch(mCurrentScreen){
            case SCREEN_VIZ_STATS_MAIN_VIEW:
                VizStatsMainFragment vizStatsMainFragment = (VizStatsMainFragment) fragmentManager.findFragmentByTag(VizStatsMainFragment.VIEW_STATS_MAIN_FRAGMENT_TAG);
                if(vizStatsMainFragment!= null && vizStatsMainFragment.isVisible()){
                    vizStatsMainFragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT MAIN STATS FRAGMENT NOT FOUND!!");
                }
                break;
            case SCREEN_VIZ_STATS_DAY_VIEW:
                VizStatsDayFragment vizStatsDayFragment = (VizStatsDayFragment) fragmentManager.findFragmentByTag(VizStatsDayFragment.VIEW_STATS_DAY_FRAGMENT_TAG);
                if(vizStatsDayFragment!= null && vizStatsDayFragment.isVisible()){
                    vizStatsDayFragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT DAY STATS FRAGMENT NOT FOUND!!");
                }
                break;
            case SCREEN_VIZ_STATS_WEEK_VIEW:
                VizStatsWeekFragment vizStatsWeekFragment = (VizStatsWeekFragment) fragmentManager.findFragmentByTag(VizStatsWeekFragment.VIEW_STATS_WEEK_FRAGMENT_TAG);
                if(vizStatsWeekFragment!= null && vizStatsWeekFragment.isVisible()){
                    vizStatsWeekFragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT WEEK STATS FRAGMENT NOT FOUND!!");
                }
                break;
            case SCREEN_VIZ_STATS_MONTH_VIEW:
                VizStatsMonthFragment vizStatsMonthFragment = (VizStatsMonthFragment) fragmentManager.findFragmentByTag(VizStatsMonthFragment.VIEW_STATS_MONTH_FRAGMENT_TAG);
                if(vizStatsMonthFragment!= null && vizStatsMonthFragment.isVisible()){
                    vizStatsMonthFragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT MONTH STATS FRAGMENT NOT FOUND!!");
                }
                break;
            case SCREEN_VIZ_STATS_DURATION_VIEW:
                VizStatsDurationFragment vizStatsDurationFragment = (VizStatsDurationFragment) fragmentManager.findFragmentByTag(VizStatsDurationFragment.VIEW_STATS_DURATION_FRAGMENT_TAG);
                if(vizStatsDurationFragment!= null && vizStatsDurationFragment.isVisible()){
                    vizStatsDurationFragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT DURATION STATS FRAGMENT NOT FOUND!!");
                }
                break;
            case SCREEN_VIZ_STATS_MP3_VIEW:
                VizStatsMp3Fragment vizStatsMp3Fragment = (VizStatsMp3Fragment) fragmentManager.findFragmentByTag(VizStatsMp3Fragment.VIEW_STATS_MP3_FRAGMENT_TAG);
                if(vizStatsMp3Fragment!= null && vizStatsMp3Fragment.isVisible()){
                    vizStatsMp3Fragment.setAllSessionsList(mAllSessionsNotLive);
                }else{
                    Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT MP3 STATS FRAGMENT NOT FOUND!!");
                }
                break;
        }
    }



}
