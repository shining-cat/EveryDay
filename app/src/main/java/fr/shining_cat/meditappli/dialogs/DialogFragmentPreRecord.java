package fr.shining_cat.meditappli.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

public class DialogFragmentPreRecord extends DialogFragment {

    public static final String DIALOG_FRAGMENT_PRE_RECORD_MANUAL_ENTRY_TAG = "dialog_fragment_pre_record_manual_entry-tag";
    public static final String DIALOG_FRAGMENT_PRE_RECORD_NORMAL_TAG = "dialog_fragment_pre_record_normal-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private DialogFragmentPreRecordListener mListener;
    private MoodRecorderViewGroup mMoodRecorder;
    private boolean mManualEntry;
    private long mTimestampOfRecordinMillis;
    private TextView mManualDateEditTxt;
    private TextView mManualTimeEditTxt;
    private MoodRecord mPresetStartMood;

////////////////////////////////////////
//This DialogFragment handles the interface to record the user's state at the beginning of a session
// Used for normal and manual entry, and editting for session beginning user's state
    public DialogFragmentPreRecord(){
        // Empty constructor is required for DialogFragment
    }

////////////////////////////////////////
//if manualEntry, DialogFragmentPreRecord will show additional fields for the user to input infos that are normally automatically created, like date and time
    public static DialogFragmentPreRecord newInstance(boolean manualEntry){
        Log.d("DialogFragmentPreRecord", "newInstance");
        DialogFragmentPreRecord frag = new DialogFragmentPreRecord();
        Bundle args = new Bundle();
        args.putBoolean("manualEntry", manualEntry);
        frag.setArguments(args);
        return frag;
    }

////////////////////////////////////////
//trasnmitting existing datas for an edit
    public void presetContent(MoodRecord startMood) {
        Log.d(TAG, "presetContent");
        mPresetStartMood = startMood;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog");
        View dialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_pre_record, null);
        //
        mMoodRecorder = dialogBody.findViewById(R.id.pre_record_mood_recorder);
        TextView introTxtView = dialogBody.findViewById(R.id.pre_record_intro_txt);
        TextView manualDateFieldLabel = dialogBody.findViewById(R.id.pre_record_manual_date_field_label);
        mManualDateEditTxt = dialogBody.findViewById(R.id.pre_record_manual_date_value_field);
        TextView manualTimeFieldLabel = dialogBody.findViewById(R.id.pre_record_manual_time_field_label);
        mManualTimeEditTxt = dialogBody.findViewById(R.id.pre_record_manual_time_value_field);
        //default timeStamp is now
        mTimestampOfRecordinMillis = System.currentTimeMillis();
        //
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        mManualEntry = getArguments().getBoolean("manualEntry");
        if(mManualEntry){ //manual input
            builder.setTitle(getString(R.string.pre_record_dialog_manual_entry_title));
            introTxtView.setText(R.string.pre_record_intro_manual_entry_text);
            //
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat sdfTime = new SimpleDateFormat("HH:mm");
            Calendar timeOfRecordCal = Calendar.getInstance();
            if(mPresetStartMood!=null){ //Editting existing record
                timeOfRecordCal.setTimeInMillis(mPresetStartMood.getTimeOfRecord());
                mTimestampOfRecordinMillis = mPresetStartMood.getTimeOfRecord();
                int seekbarOffset = Integer.parseInt(getActivity().getString(R.string.mood_recorder_seekbars_start_value));
                mMoodRecorder.setBodyValue(mPresetStartMood.getBodyValue()/2 + seekbarOffset);
                mMoodRecorder.setThoughtsValue(mPresetStartMood.getThoughtsValue()/2 + seekbarOffset);
                mMoodRecorder.setFeelingsValue(mPresetStartMood.getFeelingsValue()/2 + seekbarOffset);
                mMoodRecorder.setGlobalValue(mPresetStartMood.getGlobalValue()/2 + seekbarOffset);
                Log.d(TAG, "onCreateDialog::Editting existing entry:: timeOfRecordCal = " + timeOfRecordCal.toString());
                Log.d(TAG, "onCreateDialog::Editting existing entry:: mPresetStartMood.getBodyValue() = " + mPresetStartMood.getBodyValue());
            }else {//creating new record manually
                timeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis);
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
        } else{//normal record creation
            builder.setTitle(getActivity().getString(R.string.pre_record_dialog_default_title));
            introTxtView.setText(R.string.pre_record_intro_default_text);
            manualDateFieldLabel.setVisibility(View.GONE);
            mManualDateEditTxt.setVisibility(View.GONE);
            manualTimeFieldLabel.setVisibility(View.GONE);
            mManualTimeEditTxt.setVisibility(View.GONE);
        }
        builder.setView(dialogBody);
        builder.setPositiveButton(getString(R.string.generic_string_OK),null);//null, because we want to override default behavior to control dismissal on positive click
        builder.setNegativeButton(getString(R.string.generic_string_CANCEL),onNegativeClickListener);
        return builder.create();
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
        if (context instanceof DialogFragmentPreRecordListener) {
            mListener = (DialogFragmentPreRecordListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DialogFragmentPreRecordListener");
        }
    }

////////////////////////////////////////
//DialogFragment UI handling : onPositive, onNegative and onCancel
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            //update timestamp for non manual entry in case the dialog has been kept open a long time:
            if(!mManualEntry){
                mTimestampOfRecordinMillis = System.currentTimeMillis();
            }
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Log.d(TAG, "mTimestampOfRecordinMillis = " + mTimestampOfRecordinMillis + " / formatted = " + sdfDate.format(mTimestampOfRecordinMillis));
            int seekbarOffset = Integer.parseInt(getActivity().getString(R.string.mood_recorder_seekbars_start_value));
            MoodRecord mood = new MoodRecord(
                    mTimestampOfRecordinMillis,
                    2*(mMoodRecorder.getBodyValue() - seekbarOffset),
                    2*(mMoodRecorder.getThoughtsValue() - seekbarOffset),
                    2*(mMoodRecorder.getFeelingsValue() - seekbarOffset),
                    2*(mMoodRecorder.getGlobalValue() - seekbarOffset));
            mListener.onValidateDialogFragmentPreRecord(mood);
            dismiss();
        }
    };

    private DialogInterface.OnClickListener onNegativeClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            Log.d(TAG, "onNegativeClickListener");
            //dismiss();
            onCancel(dialog);
        }
    };

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        mListener.onCancelDialogFragmentPreRecord();
        super.onCancel(dialog);
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
                //
                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat tdf = new SimpleDateFormat("HH:mm");
                Log.d(TAG, sdf.format(timeOfRecordCal.getTime()));
                //
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), dateSetListener,
                        timeOfRecordCal.get(Calendar.YEAR),
                        timeOfRecordCal.get(Calendar.MONTH),
                        timeOfRecordCal.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
            return true;
        }
    };

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat tdf = new SimpleDateFormat("HH:mm");
            //
            Calendar startTimeOfRecordCal = Calendar.getInstance();
            startTimeOfRecordCal.setTimeInMillis(mTimestampOfRecordinMillis); //to keep set time, and only change date
            startTimeOfRecordCal.set(year, month, dayOfMonth);
            //
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(System.currentTimeMillis());
            //get only date part of now and start time for comparisons :
            try {
                Date nowDatePart = sdf.parse(sdf.format(nowCal.getTime()));
                Date datePartOfStartRecord = sdf.parse(sdf.format(startTimeOfRecordCal.getTime()));
                //BUG in lollipop where setMaxDate does not prevent selecting out-of-range date => we have to re-check the range here
                if (datePartOfStartRecord.after(nowDatePart)) {
                    //forbidden
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.generic_string_ERROR));
                    builder.setMessage(getString(R.string.error_message_invalid_date_alert));
                    builder.setNegativeButton(getString(R.string.generic_string_OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                } else if (datePartOfStartRecord.equals(nowDatePart)) {
                    if(startTimeOfRecordCal.getTimeInMillis() > nowCal.getTimeInMillis()) { // if start is after now => set time field to now
                        mManualTimeEditTxt.setText(tdf.format(nowCal.getTime()));
                        startTimeOfRecordCal.setTimeInMillis(nowCal.getTimeInMillis());
                        mTimestampOfRecordinMillis = nowCal.getTimeInMillis();
                    }else{ // this case is OK
                        mManualDateEditTxt.setText(sdf.format(startTimeOfRecordCal.getTime()));
                        mTimestampOfRecordinMillis = startTimeOfRecordCal.getTimeInMillis();
                    }
                } else {
                    mManualDateEditTxt.setText(sdf.format(startTimeOfRecordCal.getTime()));
                    mTimestampOfRecordinMillis = startTimeOfRecordCal.getTimeInMillis();
                }
            } catch (ParseException e) {
                Log.e(TAG, "onDateSet::ERROR while parsing date object :: e = " + e.toString());
                e.printStackTrace();
            }
        }
    };

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
            if(timeOfRecordCal.getTimeInMillis() > System.currentTimeMillis()){
                //forbidden
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.generic_string_ERROR));
                builder.setMessage(getString(R.string.error_message_invalid_time_alert));
                builder.setNegativeButton(getString(R.string.generic_string_OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builder.show();
            }else{
                DateFormat tdf = new SimpleDateFormat("HH:mm");
                mManualTimeEditTxt.setText(tdf.format(timeOfRecordCal.getTime()));
                mTimestampOfRecordinMillis = timeOfRecordCal.getTimeInMillis();
            }
        }
    };

////////////////////////////////////////
//Listener interface
    public interface DialogFragmentPreRecordListener {
        void onCancelDialogFragmentPreRecord();
        void onValidateDialogFragmentPreRecord(MoodRecord moodRecord);
    }
}
