package fr.shining_cat.everyday;

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
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fr.shining_cat.everyday.widgets.MoodRecorderViewGroup;


////////////////////////////////////////
//This Fragment handles the interface to record the user's state at the beginning of a session
// Used for normal and manual entry, and editting for session beginning user's state

public class PreRecordFragment extends Fragment {

    public static final String FRAGMENT_PRE_RECORD_MANUAL_ENTRY_TAG = "fragment_pre_record_manual_entry-tag";
    public static final String FRAGMENT_PRE_RECORD_NORMAL_TAG = "fragment_pre_record_normal-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final String ARG_MANUAL_ENTRY = "manual_entry_boolean_argument";

    private FragmentPreRecordListener mListener;
    private MoodRecorderViewGroup mMoodRecorder;
    private boolean mManualEntry;
    private long mTimestampOfRecordinMillis;
    private TextView mManualDateEditTxt;
    private TextView mManualTimeEditTxt;
    private MoodRecord mPresetStartMood;

    public PreRecordFragment() {
        // Required empty public constructor
    }

////////////////////////////////////////
//if manualEntry, DialogFragmentPreRecord will show additional fields for the user to input infos that are normally automatically created, like date and time
    public static PreRecordFragment newInstance(boolean manualEntry) {
        Log.d("FragmentPreRecord", "newInstance");
        PreRecordFragment fragment = new PreRecordFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_MANUAL_ENTRY, manualEntry);
        fragment.setArguments(args);
        return fragment;
    }

////////////////////////////////////////
//trasnmitting existing datas for an edit
    public void presetContent(MoodRecord startMood) {
        Log.d(TAG, "presetContent");
        mPresetStartMood = startMood;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mManualEntry = getArguments().getBoolean(ARG_MANUAL_ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment =  inflater.inflate(R.layout.fragment_pre_record, container, false);
        Log.d(TAG, "onCreateView");
        //
        TextView preRecordTitle = fragment.findViewById(R.id.pre_record_title_txtvw);
        mMoodRecorder = fragment.findViewById(R.id.pre_record_mood_recorder);
        TextView introTxtView = fragment.findViewById(R.id.pre_record_intro_txt);
        TextView manualDateFieldLabel = fragment.findViewById(R.id.pre_record_manual_date_field_label);
        mManualDateEditTxt = fragment.findViewById(R.id.pre_record_manual_date_value_field);
        TextView manualTimeFieldLabel = fragment.findViewById(R.id.pre_record_manual_time_field_label);
        mManualTimeEditTxt = fragment.findViewById(R.id.pre_record_manual_time_value_field);
        //default timeStamp is now
        mTimestampOfRecordinMillis = System.currentTimeMillis();
        //
        if(mManualEntry){ //manual input
            preRecordTitle.setText(getString(R.string.pre_record_dialog_manual_entry_title));
            introTxtView.setText(R.string.pre_record_intro_manual_entry_text);
            //
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            DateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar timeOfRecordCal = Calendar.getInstance();
            if(mPresetStartMood!=null){ //Editting existing record
                timeOfRecordCal.setTimeInMillis(mPresetStartMood.getTimeOfRecord());
                mTimestampOfRecordinMillis = mPresetStartMood.getTimeOfRecord();
                mMoodRecorder.setBodyValue(mPresetStartMood.getBodyValue());
                mMoodRecorder.setThoughtsValue(mPresetStartMood.getThoughtsValue());
                mMoodRecorder.setFeelingsValue(mPresetStartMood.getFeelingsValue());
                mMoodRecorder.setGlobalValue(mPresetStartMood.getGlobalValue());
                Log.d(TAG, "onCreateView::Editting existing entry:: timeOfRecordCal = " + timeOfRecordCal.toString());
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
            preRecordTitle.setText(getString(R.string.pre_record_dialog_default_title));
            introTxtView.setText(R.string.pre_record_intro_default_text);
            manualDateFieldLabel.setVisibility(View.GONE);
            mManualDateEditTxt.setVisibility(View.GONE);
            manualTimeFieldLabel.setVisibility(View.GONE);
            mManualTimeEditTxt.setVisibility(View.GONE);
        }
        Button negativeButton = fragment.findViewById(R.id.pre_record_cancel_btn);
        negativeButton.setOnClickListener(onNegativeClickListener);
        Button positiveButton = fragment.findViewById(R.id.pre_record_ok_btn);
        positiveButton.setOnClickListener(onPositiveClickListener);
        return fragment;
    }

////////////////////////////////////////
// UI handling : onPositive, and onCancel
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            //update timestamp for non manual entry in case the dialog has been kept open a long time:
            if(!mManualEntry){
                mTimestampOfRecordinMillis = System.currentTimeMillis();
            }
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Log.d(TAG, "mTimestampOfRecordinMillis = " + mTimestampOfRecordinMillis + " / formatted = " + sdfDate.format(mTimestampOfRecordinMillis));
            MoodRecord mood = new MoodRecord(
                    mTimestampOfRecordinMillis,
                    mMoodRecorder.getBodyValue(),
                    mMoodRecorder.getThoughtsValue(),
                    mMoodRecorder.getFeelingsValue(),
                    mMoodRecorder.getGlobalValue());
            mListener.onValidateFragmentPreRecord(mood);
        }
    };
    private View.OnClickListener onNegativeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onNegativeClickListener");
            mListener.onCancelFragmentPreRecord();
        }


    };

////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity -normal input- or VizActivity -manual entry and edit)

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentPreRecordListener) {
            mListener = (FragmentPreRecordListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentPreRecordListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
                DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                mManualTimeEditTxt.setText(tdf.format(timeOfRecordCal.getTime()));
                mTimestampOfRecordinMillis = timeOfRecordCal.getTimeInMillis();
            }
        }
    };

////////////////////////////////////////
//Listener interface
    public interface FragmentPreRecordListener {
        void onCancelFragmentPreRecord();
        void onValidateFragmentPreRecord(MoodRecord moodRecord);
    }

}
