package fr.shining_cat.meditappli;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


////////////////////////////////////////
//This Fragment handles the interface to record the user's state at the end of a session
// Used for normal and manual entry, and editting for session beginning user's state

public class PostRecordFragment extends Fragment {

    public static final String FRAGMENT_POST_RECORD_MANUAL_ENTRY_TAG = "fragment_post_record_manual_entry-tag";
    public static final String FRAGMENT_POST_RECORD_NORMAL_TAG = "fragment_post_record_normal-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final String ARG_MANUAL_ENTRY = "manual_entry_boolean_argument";
    private static final String ARG_DURATION = "session_duration_long_argument";
    private static final String ARG_PAUSES_COUNT = "pauses_count_int_argument";
    private static final String ARG_REAL_DURATION_VS_PLANNED_DURATION = "compare_real_vs_planned_duration_int_argument";
    private static final String ARG_TIME_OF_START = "time_of_session_start_long_argument";
    private static final String ARG_MP3_GUIDE = "mp3_support_file_name_string_argument";

    private FragmentPostRecordListener mListener;
    private MoodRecorderViewGroup mMoodRecorder;
    private EditText mNotesEditTxt;
    private boolean mManualEntry;
    private long mTimestampOfRecordinMillis;
    private TextView mManualDateEditTxt;
    private TextView mManualTimeEditTxt;
    private EditText mGuideMp3EditTxt;
    private long mDuration;
    private int mPausesCount;
    private long mTimeOfSessionStart;
    private int mRealDurationVsPlanned;
    private String mGuideMp3;
    private MoodRecord mPresetEndMood;

    public PostRecordFragment() {
        // Required empty public constructor
    }

////////////////////////////////////////
//transmit values to be inserted in MoodRecord object describing the user's state at the end of a session

    public static PostRecordFragment newInstance(boolean manualEntry, long duration, int pausesCount, int realDurationVsPlanned, String guideMp3, long timeOfStart){
        PostRecordFragment fragment = new PostRecordFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_MANUAL_ENTRY, manualEntry);
        args.putLong(ARG_DURATION, duration);
        args.putInt(ARG_PAUSES_COUNT, pausesCount);
        args.putInt(ARG_REAL_DURATION_VS_PLANNED_DURATION, realDurationVsPlanned);
        args.putLong(ARG_TIME_OF_START, timeOfStart);
        args.putString(ARG_MP3_GUIDE, guideMp3);
        fragment.setArguments(args);
        return fragment;
    }

////////////////////////////////////////
//trasnmitting existing datas for an edit
    public void presetContent(MoodRecord endMood) {
        mPresetEndMood = endMood;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mManualEntry = getArguments().getBoolean(ARG_MANUAL_ENTRY);
            mDuration = getArguments().getLong(ARG_DURATION);
            mPausesCount = getArguments().getInt(ARG_PAUSES_COUNT);
            mRealDurationVsPlanned = getArguments().getInt(ARG_REAL_DURATION_VS_PLANNED_DURATION);
            mTimeOfSessionStart = getArguments().getLong(ARG_TIME_OF_START);
            mGuideMp3 = getArguments().getString(ARG_MP3_GUIDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_post_record, container, false);

        //
        TextView postRecordTitle = fragment.findViewById(R.id.post_record_title_txtvw);
        mMoodRecorder = fragment.findViewById(R.id.post_record_mood_recorder);
        mNotesEditTxt = fragment.findViewById(R.id.post_record_notes_editTxt);
        TextView introTxtView = fragment.findViewById(R.id.post_record_intro_txt);
        TextView manualDateFieldLabel = fragment.findViewById(R.id.post_record_manual_date_field_label);
        mManualDateEditTxt = fragment.findViewById(R.id.post_record_manual_date_value_field);
        TextView manualTimeFieldLabel = fragment.findViewById(R.id.post_record_manual_time_field_label);
        mManualTimeEditTxt = fragment.findViewById(R.id.post_record_manual_time_value_field);
        TextView guideMp3Label = fragment.findViewById(R.id.post_record_guide_mp3_label);
        mGuideMp3EditTxt =  fragment.findViewById(R.id.post_record_guide_mp3_txtvw);
        Button negativeButton = fragment.findViewById(R.id.post_record_cancel_btn);
        negativeButton.setOnClickListener(onNegativeClickListener);
        Button positiveButton = fragment.findViewById(R.id.post_record_ok_btn);
        positiveButton.setOnClickListener(onPositiveClickListener);
        //default timeStamp is now
        mTimestampOfRecordinMillis = System.currentTimeMillis();
        if(mManualEntry) {
            postRecordTitle.setText(getString(R.string.post_record_dialog_manual_entry_title));
            negativeButton.setText(getString(R.string.generic_string_BACK));
            introTxtView.setText(R.string.post_record_intro_manual_entry_text);
            //
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat sdfTime = new SimpleDateFormat("HH:mm");
            Calendar timeOfRecordCal = Calendar.getInstance();
            //
            if(mPresetEndMood!=null){ //Editting existing record : get existing values
                if(mPresetEndMood.getTimeOfRecord() < mTimeOfSessionStart){//if recorded end time is before new start time, preset to new start time instead of recorded end time
                    timeOfRecordCal.setTimeInMillis(mTimeOfSessionStart);
                    mTimestampOfRecordinMillis = mTimeOfSessionStart;
                } else {
                    timeOfRecordCal.setTimeInMillis(mPresetEndMood.getTimeOfRecord());
                    mTimestampOfRecordinMillis = mPresetEndMood.getTimeOfRecord();
                }
                mMoodRecorder.setBodyValue(mPresetEndMood.getBodyValue());
                mMoodRecorder.setThoughtsValue(mPresetEndMood.getThoughtsValue());
                mMoodRecorder.setFeelingsValue(mPresetEndMood.getFeelingsValue());
                mMoodRecorder.setGlobalValue(mPresetEndMood.getGlobalValue());
                //
                mDuration = mPresetEndMood.getSessionRealDuration();
                mPausesCount = mPresetEndMood.getPausesCount();
                mRealDurationVsPlanned = mPresetEndMood.getRealDurationVsPlanned();
                mNotesEditTxt.setText(mPresetEndMood.getNotes());
                mGuideMp3EditTxt.setText(mPresetEndMood.getGuideMp3());
            }else {//creating new record manually
                timeOfRecordCal.setTimeInMillis(mTimeOfSessionStart); // set value to that of session start time, so it will be not far from what the user will probably want
                mTimestampOfRecordinMillis = mTimeOfSessionStart;
                //initial setting, we will calculate difference between start and end, not counting eventual pauses, for simplicity sake
                mDuration = -1;
                // not stored for manual entry
                mPausesCount = 0;
                // not stored for manual entry
                mRealDurationVsPlanned = 0;
            }
            //
            manualDateFieldLabel.setVisibility(View.VISIBLE);
            mManualDateEditTxt.setText(sdfDate.format(timeOfRecordCal.getTime()));
            mManualDateEditTxt.setOnTouchListener(onTouchDateManualField);
            mManualDateEditTxt.setVisibility(View.VISIBLE);
            //
            manualTimeFieldLabel.setVisibility(View.VISIBLE);
            mManualTimeEditTxt.setText(sdfTime.format(timeOfRecordCal.getTime()));
            mManualTimeEditTxt.setOnTouchListener(onTouchManualTimeField);
            mManualTimeEditTxt.setVisibility(View.VISIBLE);
            //
            guideMp3Label.setVisibility(View.VISIBLE);
            mGuideMp3EditTxt.setVisibility(View.VISIBLE);
        }else{ // we are showing the post record dialog at the end of a session :
            postRecordTitle.setText(getString(R.string.post_record_dialog_default_title));
            introTxtView.setText(R.string.post_record_intro_default_text);
            negativeButton.setText(getString(R.string.generic_string_CANCEL));
            manualDateFieldLabel.setVisibility(View.GONE);
            mManualDateEditTxt.setVisibility(View.GONE);
            manualTimeFieldLabel.setVisibility(View.GONE);
            mManualTimeEditTxt.setVisibility(View.GONE);
            guideMp3Label.setVisibility(View.GONE);
            mGuideMp3EditTxt.setVisibility(View.GONE);
            mDuration = getArguments().getLong("duration");
            mPausesCount = getArguments().getInt("pausesCount");
            mRealDurationVsPlanned = getArguments().getInt("realDurationVsPlanned");
            mGuideMp3 = getArguments().getString("mp3guide");
        }
        return fragment;
    }

    ////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity -normal input- or VizActivity -manual entry and edit)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentPostRecordListener) {
            mListener = (FragmentPostRecordListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement FragmentPostRecordListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

////////////////////////////////////////
//UI handling : onPositive, and onCancel
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            //no need here to update timestamp for non manual entry because we'd rather store the time at which the dialog opened (= end of session)
            MoodRecord mood = new MoodRecord(
                    mTimestampOfRecordinMillis,
                    mMoodRecorder.getBodyValue(),
                    mMoodRecorder.getThoughtsValue(),
                    mMoodRecorder.getFeelingsValue(),
                    mMoodRecorder.getGlobalValue());
            //for manual entry, calculate duration based on entered start and end time
            if(mManualEntry){
                mGuideMp3 = mGuideMp3EditTxt.getText().toString();
                if((mTimestampOfRecordinMillis - mTimeOfSessionStart)>0){
                    mDuration = mTimestampOfRecordinMillis - mTimeOfSessionStart;
                }else {//prevent incoherent start/end time storage
                    showInvalidTimeDialogError();
                    return;
                }
            }
            mood.setSessionRealDuration(mDuration);
            mood.setPausesCount(mPausesCount);
            mood.setRealDurationVsPlanned(mRealDurationVsPlanned);
            //here we allow empty values
            mood.setNotes(mNotesEditTxt.getText().toString());
            mood.setGuideMp3(mGuideMp3);
            mListener.onValidateFragmentPostRecord(mood);
        }
    };
    private View.OnClickListener onNegativeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onNegativeClickListener");
            properCancelling();
        }
    };

    public void properCancelling(){
        Log.d(TAG, "properCancelling::mManualEntry = " + mManualEntry);
        if(mManualEntry){
            //same action for manualEntry && edit and manualEntry && create new
            mListener.onCancelFragmentPostRecord(true);
        }else {
            //warn user that session will not be recorded
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.generic_string_WARNING));
            builder.setMessage(getString(R.string.post_record_cancel_warning_message));
            builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //go back to postrecordfragment
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.post_record_cancel_ok_do_not_record), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //cancel session record
                    mListener.onCancelFragmentPostRecord(false);
                }
            });
            builder.show();
        }
    }

////////////////////////////////////////
//DATE MANUAL PICKING
    View.OnTouchListener onTouchDateManualField = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Calendar timeOfRecordCal = Calendar.getInstance();
                //set calendar object to NOW or previously set value
                timeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis);
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), dateSetListener,
                        timeOfRecordCal.get(Calendar.YEAR),
                        timeOfRecordCal.get(Calendar.MONTH),
                        timeOfRecordCal.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.getDatePicker().setMinDate(mTimeOfSessionStart);
                dpd.show();
            }
            return true;
        }
    };
    //need second check because datepicker range does not prevent user to select apparently inactive dates on Lollipop
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat tdf = new SimpleDateFormat("HH:mm");
            //
            Calendar endTimeOfRecordCal = Calendar.getInstance();
            endTimeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis); //to keep set time, and only change date
            endTimeOfRecordCal.set(year, month, dayOfMonth);
            //
            Calendar startTimeOfRecordCal = Calendar.getInstance();
            startTimeOfRecordCal.setTimeInMillis(mTimeOfSessionStart);
            //
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(System.currentTimeMillis());
            //get only date part of now, start and finish time for comparisons :
            try {
                Date nowDatePart = sdf.parse(sdf.format(nowCal.getTime()));
                Date datePartOfStartRecord = sdf.parse(sdf.format(startTimeOfRecordCal.getTime()));
                Date datePartOfEndRecord = sdf.parse(sdf.format(endTimeOfRecordCal.getTime()));
                //date of end session must be >= start session date and <= today
                //only check on the date part, because time is set independently
                //if date end = date start => set time to same as start time (it cannot be anterior)
                //if date end = today AND current set time is > now => set time to now (it can not be posterior)
                if (datePartOfEndRecord.before(datePartOfStartRecord) || datePartOfEndRecord.after(nowDatePart)) {
                    //forbidden
                    showInvalidDateDialogError();
                } else if (datePartOfEndRecord.equals(datePartOfStartRecord)) { //start date is same as end day => we compare complete timestamp
                    if(endTimeOfRecordCal.getTimeInMillis() <= startTimeOfRecordCal.getTimeInMillis()) { // if end is before start => set time field to same as start
                        mManualTimeEditTxt.setText(tdf.format(startTimeOfRecordCal.getTime()));
                        endTimeOfRecordCal.setTimeInMillis(startTimeOfRecordCal.getTimeInMillis());
                        mTimestampOfRecordinMillis = endTimeOfRecordCal.getTimeInMillis();
                    }else{ // this case is OK
                        mManualDateEditTxt.setText(sdf.format(endTimeOfRecordCal.getTime()));
                        mTimestampOfRecordinMillis = endTimeOfRecordCal.getTimeInMillis();
                    }
                } else if (datePartOfEndRecord.equals(nowDatePart)) {
                    if(endTimeOfRecordCal.getTimeInMillis() > nowCal.getTimeInMillis()) { // if end is after now => set time field to now
                        mManualTimeEditTxt.setText(tdf.format(nowCal.getTime()));
                        endTimeOfRecordCal.setTimeInMillis(nowCal.getTimeInMillis());
                        mTimestampOfRecordinMillis = nowCal.getTimeInMillis();
                    }else{ // this case is OK
                        mManualDateEditTxt.setText(sdf.format(endTimeOfRecordCal.getTime()));
                        mTimestampOfRecordinMillis = endTimeOfRecordCal.getTimeInMillis();
                    }
                } else {
                    //there is enough difference between start and end and now dates, so we don't force change the time field
                    mManualDateEditTxt.setText(sdf.format(endTimeOfRecordCal.getTime()));
                    mTimestampOfRecordinMillis = endTimeOfRecordCal.getTimeInMillis();
                }

            } catch (ParseException e) {
                Log.e(TAG, "onDateSet::ERROR while parsing date object :: e = " + e.toString());
                e.printStackTrace();
            }
        }
    };

    private void showInvalidDateDialogError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.generic_string_ERROR));
        builder.setMessage(getString(R.string.error_message_invalid_date_alert));
        builder.setNegativeButton(getString(R.string.generic_string_OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {//default behaviour : dismiss
            }
        });
        builder.show();
    }
////////////////////////////////////////
//TIME MANUAL PICKING
    View.OnTouchListener onTouchManualTimeField = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Calendar timeOfRecordCal = Calendar.getInstance();
                //set calendar object to NOW or previously set value
                timeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis);
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), timeSetListener,
                        timeOfRecordCal.get(Calendar.HOUR_OF_DAY),
                        timeOfRecordCal.get(Calendar.MINUTE),
                        true
                );
                tpd.show();
            }
            return true;
        }
    };
    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar timeOfRecordCal = Calendar.getInstance();
            timeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis); //to keep set date, and only change time
            timeOfRecordCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            timeOfRecordCal.set(Calendar.MINUTE, minute);
            Log.d(TAG, "onTimeSet::timeOfRecordCal.getTimeInMillis()> System.currentTimeMillis() = " + String.valueOf(timeOfRecordCal.getTimeInMillis()> System.currentTimeMillis()));
            Log.d(TAG, "onTimeSet::timeOfRecordCal.getTimeInMillis() < mTimeOfSessionStart = " + String.valueOf(timeOfRecordCal.getTimeInMillis() < mTimeOfSessionStart));
            DateFormat logDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Log.d(TAG, "mTimestampOfRecordinMillis = " + timeOfRecordCal.getTimeInMillis() + " / formatted = " + logDate.format(timeOfRecordCal.getTimeInMillis()));
            Log.d(TAG, "mTimestampOfRecordinMillis = " + mTimeOfSessionStart + " / formatted = " + logDate.format(mTimeOfSessionStart));
            if(timeOfRecordCal.getTimeInMillis()> System.currentTimeMillis() || timeOfRecordCal.getTimeInMillis() <= mTimeOfSessionStart){
                //forbidden
                showInvalidTimeDialogError();
            } else {
                DateFormat tdf = new SimpleDateFormat("HH:mm");
                mManualTimeEditTxt.setText(tdf.format(timeOfRecordCal.getTime()));
                mTimestampOfRecordinMillis = timeOfRecordCal.getTimeInMillis();
            }
        }
    };

    private void showInvalidTimeDialogError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.generic_string_ERROR));
        builder.setMessage(getString(R.string.error_message_invalid_time_alert));
        builder.setNegativeButton(getString(R.string.generic_string_OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

////////////////////////////////////////
//Listener interface
    public interface FragmentPostRecordListener {
        void onValidateFragmentPostRecord(MoodRecord moodRecord);
        void onCancelFragmentPostRecord(Boolean isManualEntry);//boolean param is not really used in listeners yet, because SessionActivity only handles isManualEntry=false cases and VizActivity only handles isManualEntry=true cases. But in case it is needed later, I let it here
    }
}
