package fr.shining_cat.meditappli.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import fr.shining_cat.meditappli.MoodRecord;
import fr.shining_cat.meditappli.MoodRecorderViewGroup;
import fr.shining_cat.meditappli.R;

public class DialogFragmentPostRecord extends DialogFragment {

    public static final String DIALOG_FRAGMENT_POST_RECORD_MANUAL_ENTRY_TAG = "dialog_fragment_post_record_manual_entry-tag";
    public static final String DIALOG_FRAGMENT_POST_RECORD_NORMAL_TAG = "dialog_fragment_post_record_normal-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private DialogFragmentPostRecordListener mListener;
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

////////////////////////////////////////
//This DialogFragment handles the interface to record the user's state at the end of a session
// Used for normal and manual entry, and editting for session beginning user's state
    public DialogFragmentPostRecord(){
        // Empty constructor is required for DialogFragment
    }

////////////////////////////////////////
//transmit values to be inserted in MoodRecord object describing the user's state at the end of a session

    public static DialogFragmentPostRecord newInstance(boolean manualEntry, long duration, int pausesCount, int realDurationVsPlanned, String guideMp3, long timeOfStart){
        DialogFragmentPostRecord frag = new DialogFragmentPostRecord();
        Bundle args = new Bundle();
        args.putBoolean("manualEntry", manualEntry);
        args.putLong("duration", duration);
        args.putInt("pausesCount", pausesCount);
        args.putInt("realDurationVsPlanned", realDurationVsPlanned);
        args.putLong("timeOfStart", timeOfStart);
        args.putString("mp3guide", guideMp3);
        frag.setArguments(args);
        return frag;
    }

////////////////////////////////////////
//trasnmitting existing datas for an edit
    public void presetContent(MoodRecord endMood) {
        mPresetEndMood = endMood;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_post_record, null);
        //
        mMoodRecorder = dialogBody.findViewById(R.id.post_record_mood_recorder);
        mNotesEditTxt = dialogBody.findViewById(R.id.post_record_notes_editTxt);
        TextView introTxtView = dialogBody.findViewById(R.id.post_record_intro_txt);
        TextView manualDateFieldLabel = dialogBody.findViewById(R.id.post_record_manual_date_field_label);
        mManualDateEditTxt = dialogBody.findViewById(R.id.post_record_manual_date_value_field);
        TextView manualTimeFieldLabel = dialogBody.findViewById(R.id.post_record_manual_time_field_label);
        mManualTimeEditTxt = dialogBody.findViewById(R.id.post_record_manual_time_value_field);
        TextView guideMp3Label = dialogBody.findViewById(R.id.post_record_guide_mp3_label);
        mGuideMp3EditTxt =  dialogBody.findViewById(R.id.post_record_guide_mp3_txtvw);
        //
        mTimeOfSessionStart = getArguments().getLong("timeOfStart");
        //default timeStamp is now
        mTimestampOfRecordinMillis = System.currentTimeMillis();
        //
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        mManualEntry = getArguments().getBoolean("manualEntry");
        if(mManualEntry) {
            builder.setTitle(getString(R.string.post_record_dialog_manual_entry_title));
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
                int seekbarOffset = Integer.parseInt(getActivity().getString(R.string.mood_recorder_seekbars_start_value));
                mMoodRecorder.setBodyValue(mPresetEndMood.getBodyValue()/2 + seekbarOffset);
                mMoodRecorder.setThoughtsValue(mPresetEndMood.getThoughtsValue()/2 + seekbarOffset);
                mMoodRecorder.setFeelingsValue(mPresetEndMood.getFeelingsValue()/2 + seekbarOffset);
                mMoodRecorder.setGlobalValue(mPresetEndMood.getGlobalValue()/2 + seekbarOffset);
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
            builder.setTitle(getActivity().getString(R.string.post_record_dialog_default_title));
            introTxtView.setText(R.string.post_record_intro_default_text);
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
        builder.setView(dialogBody);
        builder.setNegativeButton(getString(R.string.generic_string_CANCEL),onNegativeClickListener);
        builder.setPositiveButton(getString(R.string.generic_string_OK),null);//null, because we want to override default behavior to control dismissal on positive click
        return builder.create();
    }

////////////////////////////////////////
//modify inputmode because soft keyboard comes over dialog fields
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //resize the dialogfragment to activate the scroll of content when softkeyboard opens for the textfield
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

////////////////////////////////////////
//piggyback onStart to implement custom behavior on positive button (with controlled dismissal)
    @Override
    public void onStart(){
        Log.d(TAG, "onStart");
        super.onStart();
        final AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog!=null){
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(onPositiveClickListener);
        }
    }

////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity -normal input- or VizActivity -manual entry and edit)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DialogFragmentPostRecordListener) {
            mListener = (DialogFragmentPostRecordListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement DialogFragmentPostRecordListener");
        }
    }

////////////////////////////////////////
//DialogFragment UI handling : onPositive, onNegative and onCancel
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            //no need here to update timestamp for non manual entry because we'd rather store the time at which the dialog opened (= end of session)
            int seekbarOffset = Integer.parseInt(getActivity().getString(R.string.mood_recorder_seekbars_start_value));
            MoodRecord mood = new MoodRecord(
                    mTimestampOfRecordinMillis,
                    2*(mMoodRecorder.getBodyValue() - seekbarOffset),
                    2*(mMoodRecorder.getThoughtsValue() - seekbarOffset),
                    2*(mMoodRecorder.getFeelingsValue() - seekbarOffset),
                    2*(mMoodRecorder.getGlobalValue() - seekbarOffset));
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
            mListener.onValidateDialogFragmentPostRecord(mood);
            dismiss();
        }
    };

    private DialogInterface.OnClickListener onNegativeClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface fragmentPostRecordDialog, int whichButton) {
            Log.d(TAG, "onNegativeClickListener::onClick");
            properPostRecordExit();
        }
    };
    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        properPostRecordExit();
    }
    private void properPostRecordExit(){
        if(mManualEntry){
            //go back to prerecordfragment
            mListener.goBackToDialogFragmentPreRecord();
        }else {
            //warn user that session will not be recorded
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.generic_string_WARNING));
            builder.setMessage(getString(R.string.post_record_cancel_warning_message));
            builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //go back to postrecordfragment
                    mListener.restartDialogFragmentPostRecord();
                }
            });
            builder.setPositiveButton(getString(R.string.post_record_cancel_ok_do_not_record), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //cancel session record
                    mListener.onCancelDialogFragmentPostRecord();
                    dismiss();
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
    public interface DialogFragmentPostRecordListener {
        void onValidateDialogFragmentPostRecord(MoodRecord moodRecord);
        void onCancelDialogFragmentPostRecord();
        void restartDialogFragmentPostRecord();
        void goBackToDialogFragmentPreRecord();
    }
}
