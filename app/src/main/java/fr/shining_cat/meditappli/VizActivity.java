package fr.shining_cat.meditappli;

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
import java.util.List;

import fr.shining_cat.meditappli.data.MeditAppliRepository;
import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;
import fr.shining_cat.meditappli.data.SessionsListAdapter;
import fr.shining_cat.meditappli.utils.TimeOperations;

////////////////////////////////////////
//This activity holds the STATS fragments : ViewPagerSessionsDetailsFragment, ViewSessionsListFragment, ViewSessionsCalendarFragment, ViewStatsFragment
public class VizActivity extends AppCompatActivity
                        implements  PreRecordFragment.FragmentPreRecordListener,
                                    PostRecordFragment.FragmentPostRecordListener,
                                    SessionsListAdapter.SessionsListAdapterListener,
                                    MeditAppliRepository.MeditAppliRepoListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private final String SCREEN_VIZ_PRE_RECORD_VIEW = "pre record fragment screen";
    private final String SCREEN_VIZ_POST_RECORD_VIEW = "post record fragment screen";
    private final String SCREEN_VIZ_LIST_VIEW = "list view viz screen";
    private final String SCREEN_VIZ_SESSION_DETAILS_VIEW = "session details view viz screen";
    private final String SCREEN_VIZ_CALENDAR_VIEW = "calendar view viz screen";
    private final String SCREEN_VIZ_STATS_VIEW = "stats view viz screen";

    private final String EDITTING_OR_DELETING_EXISTING_SESSION = "editting or deleting an existing session";
    private final String ADDING_NEW_SESSION = "currently creating a new session";



    private String mCurrentScreen;
    private String mPreviousScreen;
    private MoodRecord mStartMood;
    private MoodRecord mEndMood;
    private String mEditOrNewSessionRecording;
    private SessionRecord mCurrentEdittedSessionRecord;
    private int mCurrentlyEdittedSessionIndexInAdapter;
    private ViewPagerSessionsDetailsFragment mViewPagerSessionsDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viz);
        mEditOrNewSessionRecording = "";
        //TODO : once every screen is functional, choose which will be the default landing one... calendar, stats, or list?
        showSessionsListView();
    }

////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_viz, menu);
        MenuItem addSessionButton = menu.findItem(R.id.action_add);
        MenuItem viewCalendarButton = menu.findItem(R.id.action_view_calendar);
        MenuItem viewListButton = menu.findItem(R.id.action_view_list);
        MenuItem viewStatsButton = menu.findItem(R.id.action_view_stats);
        MenuItem editSessionButton = menu.findItem(R.id.action_edit);
        switch (mCurrentScreen){
            case SCREEN_VIZ_PRE_RECORD_VIEW :
            case SCREEN_VIZ_POST_RECORD_VIEW :
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
                break;
            case SCREEN_VIZ_CALENDAR_VIEW :
                addSessionButton.setVisible(true);
                viewListButton.setVisible(true);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(true);
                editSessionButton.setVisible(false);
                break;
            case SCREEN_VIZ_LIST_VIEW :
                addSessionButton.setVisible(true);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(true);
                viewStatsButton.setVisible(true);
                editSessionButton.setVisible(false);
                break;
            case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                addSessionButton.setVisible(false);
                viewListButton.setVisible(false);
                viewCalendarButton.setVisible(false);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(true);
                break;
            case SCREEN_VIZ_STATS_VIEW :
                addSessionButton.setVisible(false);
                viewListButton.setVisible(true);
                viewCalendarButton.setVisible(true);
                viewStatsButton.setVisible(false);
                editSessionButton.setVisible(false);
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
                showStats();
                return true;
            case R.id.action_edit:
                //this button is only visible in SCREEN_VIZ_SESSION_DETAILS_VIEW
                //get current shown session object in ViewPagerSessionsDetailsFragment
                if(mViewPagerSessionsDetailsFragment!=null) {
                    mCurrentlyEdittedSessionIndexInAdapter = mViewPagerSessionsDetailsFragment.getCurrentPosition();
                    SessionRecord currentSession = mViewPagerSessionsDetailsFragment.getSessionRecordAtPosition(mCurrentlyEdittedSessionIndexInAdapter);
                    showEditOrDeleteSessionDialog(currentSession);
                }else{
                    Log.e(TAG, "onOptionsItemSelected::action_edit = mViewPagerSessionsDetailsFragment is NULL!");
                }
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
                        showSessionDetailsView(mCurrentlyEdittedSessionIndexInAdapter);
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
            case SCREEN_VIZ_CALENDAR_VIEW :
            case SCREEN_VIZ_LIST_VIEW :
            default:
                return false;
        }
    }

////////////////////////////////////////
//DIFFERENT SCREENS (fragments : ViewSessionsListFragment, ViewSessionsCalendarFragment, ViewStatsFragment, ViewPagerSessionsDetailsFragment)
    private void showSessionsListView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ViewSessionsListFragment viewSessionsListFragment = ViewSessionsListFragment.newInstance();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, viewSessionsListFragment, ViewSessionsListFragment.VIEW_SESSION_LIST_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_LIST_VIEW;
        invalidateOptionsMenu();
    }
    //sessionsListAdapter callbacks for list items interactions
    @Override
    public void onClickOnSession(int clickedSessionPositionInSessionsListAdapter) {
        showSessionDetailsView(clickedSessionPositionInSessionsListAdapter);
    }
    @Override
    public void onLongClickOnSession(int clickedSessionPositionInAdapter, SessionRecord clickedSession) {
        mCurrentlyEdittedSessionIndexInAdapter = clickedSessionPositionInAdapter;
        showEditOrDeleteSessionDialog(clickedSession);
    }
    //
    private void showSessionsCalendarView(){
        mCurrentScreen = SCREEN_VIZ_CALENDAR_VIEW;
        invalidateOptionsMenu();
        Toast.makeText(this, "La vue CALENDRIER n'est pas encore opérationnelle", Toast.LENGTH_LONG).show();
        //TODO: fragment for a calendar type view of sessions : ViewSessionsCalendarFragment
    }
    //
    private void showStats(){
        mCurrentScreen = SCREEN_VIZ_STATS_VIEW;
        invalidateOptionsMenu();
        Toast.makeText(this, "La vue STATISTIQUES n'est pas encore opérationnelle", Toast.LENGTH_LONG).show();
        //TODO: stats fragment : ViewStatsFragment
    }
    //
    private void showSessionDetailsView(int clickedSessionPositionInSessionsListAdapter){
        mPreviousScreen = mCurrentScreen;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mViewPagerSessionsDetailsFragment != null) {
            mViewPagerSessionsDetailsFragment = null;
        }
        mViewPagerSessionsDetailsFragment = ViewPagerSessionsDetailsFragment.newInstance();
        mViewPagerSessionsDetailsFragment.setStartingSessionDetails(clickedSessionPositionInSessionsListAdapter);
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, mViewPagerSessionsDetailsFragment, ViewPagerSessionsDetailsFragment.VIEW_PAGER_SESSION_DETAILS_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_SESSION_DETAILS_VIEW;
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
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat tdf = new SimpleDateFormat("HH:mm");
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
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat tdf = new SimpleDateFormat("HH:mm");
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
        } // else : manual new session creation, no preset content
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viz_activity_fragments_holder, preRecordFragment, PreRecordFragment.FRAGMENT_PRE_RECORD_MANUAL_ENTRY_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_VIZ_PRE_RECORD_VIEW;
        invalidateOptionsMenu();
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
    public void onCancelFragmentPostRecord(Boolean isManualEntry) {
        if(isManualEntry) {
            //go back to prerecord fragment
            showFragmentPreRecord(mStartMood); // we have stored what user eventually entered in prerecord fragment which can be different from mCurrentEdittedSessionRecord.getStartMood()
        }else{
            Log.e(TAG, "onCancelFragmentPostRecord::isManualEntry should always be true in this context");
        }
    }

    @Override
    public void onValidateFragmentPostRecord(MoodRecord moodRecord) {
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
            case SCREEN_VIZ_LIST_VIEW :
                showSessionsListView();
            case SCREEN_VIZ_SESSION_DETAILS_VIEW :
                showSessionDetailsView(mCurrentlyEdittedSessionIndexInAdapter);
        }
    }

////////////////////////////////////////
//MeditAppliRepository callbacks
    @Override
    public void onUpdateOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            mViewPagerSessionsDetailsFragment.setStartingSessionDetails(mCurrentlyEdittedSessionIndexInAdapter);
        }
        Toast.makeText(this, R.string.update_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void ondeleteOneSessionRecordComplete(int result) {
        if(mCurrentScreen.equals(SCREEN_VIZ_SESSION_DETAILS_VIEW)) {
            // go back to first entry? maybe exit to list/calendar view?
            mViewPagerSessionsDetailsFragment.setStartingSessionDetails(0);
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
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {}

}
