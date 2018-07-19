package fr.shining_cat.everyday;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import fr.shining_cat.everyday.data.EveryDayRepository;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.utils.TimeOperations;
import fr.shining_cat.everyday.utils.UiUtils;

////////////////////////////////////////
//This activity holds the STATS fragments : VizViewPagerSessionsDetailsFragment, VizSessionsListFragment, VizSessionsCalendarFragment, ViewStatsFragment
public class VizActivity extends AppCompatActivity
                        implements  PreRecordFragment.FragmentPreRecordListener,
                                    PostRecordFragment.PostRecordFragmentListener,
                                    VizSessionsListAdapter.SessionsListAdapterListener,
        EveryDayRepository.EveryDayRepoListener,
                                    VizSessionsCalendarFragment.SessionsCalendarListener,
                                    VizStatsMainFragment.VizStatsMainFragmentListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

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
    private MoodRecord mStartMood;
    private MoodRecord mEndMood;
    private String mEditOrNewSessionRecording;
    private SessionRecord mCurrentEdittedSessionRecord;
    private VizViewPagerSessionsDetailsFragment mVizViewPagerSessionsDetailsFragment;
    private Long mCurrentMonthShownInCalendarScreen;

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
        //showSessionsCalendarView();
        //showSessionsListView();
        showSessionStatsMainView();
    }

////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d(TAG, "onCreateOptionsMenu");
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
        setTitle(getString(R.string.app_name));
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
                //get current shown session object in VizViewPagerSessionsDetailsFragment
                if(mVizViewPagerSessionsDetailsFragment !=null) {
                    SessionRecord currentSession = mVizViewPagerSessionsDetailsFragment.getCurrentSessionRecord();
                    showEditOrDeleteSessionDialog(currentSession);
                }else{
                    Log.e(TAG, "onOptionsItemSelected::action_edit = mVizViewPagerSessionsDetailsFragment is NULL!");
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
//DIFFERENT SCREENS (fragments : VizSessionsListFragment, VizSessionsCalendarFragment, ViewStatsFragment, VizViewPagerSessionsDetailsFragment)
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
    private void showSessionDetailsView(SessionRecord clickedSession){
        mPreviousScreen = mCurrentScreen;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mVizViewPagerSessionsDetailsFragment != null) {
            mVizViewPagerSessionsDetailsFragment = null;
        }
        mVizViewPagerSessionsDetailsFragment = VizViewPagerSessionsDetailsFragment.newInstance();
        mVizViewPagerSessionsDetailsFragment.setStartingSessionDetailsWithSessionRecord(clickedSession);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, mVizViewPagerSessionsDetailsFragment, VizViewPagerSessionsDetailsFragment.VIEW_PAGER_SESSION_DETAILS_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_SESSION_DETAILS_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionStatsMainView(){
        //query data not live : (no caching here, could be done by checking if mAllSessionsNotLive is null or not)
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getAllSessionsRecordsInBunch(this);
        //meanwhile we prepare and show ragment (with no data, and a loding animation)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMainFragment vizStatsMainFragment = VizStatsMainFragment.newInstance();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMainFragment, VizStatsMainFragment.VIEW_STATS_MAIN_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MAIN_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionsStatsDayView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsDayFragment vizStatsDayFragment = VizStatsDayFragment.newInstance();
        vizStatsDayFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsDayFragment, VizStatsDayFragment.VIEW_STATS_DAY_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_DAY_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionsStatsWeekView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsWeekFragment vizStatsWeekFragment = VizStatsWeekFragment.newInstance();
        vizStatsWeekFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsWeekFragment, VizStatsWeekFragment.VIEW_STATS_WEEK_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_WEEK_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionsStatsMonthView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMonthFragment vizStatsMonthFragment = VizStatsMonthFragment.newInstance();
        vizStatsMonthFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMonthFragment, VizStatsMonthFragment.VIEW_STATS_MONTH_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MONTH_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showSessionsStatsDurationView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsDurationFragment vizStatsDurationFragment = VizStatsDurationFragment.newInstance();
        vizStatsDurationFragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsDurationFragment, VizStatsDurationFragment.VIEW_STATS_DURATION_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_DURATION_VIEW;
        invalidateOptionsMenu();
    }
    private void showSessionsStatsMp3View(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VizStatsMp3Fragment vizStatsMp3Fragment = VizStatsMp3Fragment.newInstance();
        vizStatsMp3Fragment.setNoMp3LabelString(getString(R.string.mp3_stats_no_mp3_file_label));
        vizStatsMp3Fragment.setAllSessionsList(mAllSessionsNotLive);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, vizStatsMp3Fragment, VizStatsMp3Fragment.VIEW_STATS_MP3_FRAGMENT_TAG);
        fragmentTransaction.commit();
        //
        mCurrentScreen = SCREEN_VIZ_STATS_MP3_VIEW;
        invalidateOptionsMenu();
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
//EveryDayRepository callbacks
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            mVizViewPagerSessionsDetailsFragment.setStartingSessionDetailsWithSessionRecord(mCurrentEdittedSessionRecord);
        }
        Toast.makeText(this, R.string.update_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void ondeleteOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            // go back to first entry? maybe exit to list/calendar view?
            //mVizViewPagerSessionsDetailsFragment.setStartingSessionDetailsWithIndex(0);
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
        VizStatsMainFragment vizStatsMainFragment = (VizStatsMainFragment) fragmentManager.findFragmentByTag(VizStatsMainFragment.VIEW_STATS_MAIN_FRAGMENT_TAG);
        if(mCurrentScreen == SCREEN_VIZ_STATS_MAIN_VIEW && vizStatsMainFragment!= null && vizStatsMainFragment.isVisible()){
            vizStatsMainFragment.setAllSessionsList(mAllSessionsNotLive);
        }else{
            Log.e(TAG, "onGetAllSessionsNotLiveComplete::DATA IS READY BUT MAIN STATS FRAGMENT NOT FOUND!!");
        }
    }



}
