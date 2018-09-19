package fr.shining_cat.everyday.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import fr.shining_cat.everyday.R;

public class DialogFragmentManualDurationEntry extends DialogFragment {

    public static final String DIALOG_FRAGMENT_MANUAL_DURATION_ENTRY_TAG = "dialog_fragment_manual_duration_entry-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private NumberPicker mHoursSelector;
    private NumberPicker mMinutesSelector;
    private NumberPicker mSecondsSelector;

    private DialogFragmentManualDurationEntryListener mListener;

////////////////////////////////////////
//this DialogFragment just shows some explication text and the app's infos
    public DialogFragmentManualDurationEntry(){
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogFragmentManualDurationEntry newInstance(){
        return new DialogFragmentManualDurationEntry();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DialogFragmentManualDurationEntryListener) {
            mListener = (DialogFragmentManualDurationEntryListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement DialogFragmentManualDurationEntryListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        NumberPicker.Formatter twoDigitsFormatter = new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        };
        //
        View dialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_manual_audio_duration_entry, null);
        //
        mHoursSelector = dialogBody.findViewById(R.id.duration_manual_picker_hours_picker);
        mHoursSelector.setMaxValue(23);
        mHoursSelector.setMinValue(0);
        mHoursSelector.setFormatter(twoDigitsFormatter);
        mHoursSelector.setWrapSelectorWheel(false);
        //
        mMinutesSelector = dialogBody.findViewById(R.id.duration_manual_picker_minutes_picker);
        mMinutesSelector.setMaxValue(59);
        mMinutesSelector.setMinValue(0);
        mMinutesSelector.setFormatter(twoDigitsFormatter);
        mMinutesSelector.setWrapSelectorWheel(false);
        //
        mSecondsSelector = dialogBody.findViewById(R.id.duration_manual_picker_seconds_picker);
        mSecondsSelector.setMaxValue(59);
        mSecondsSelector.setMinValue(0);
        mSecondsSelector.setFormatter(twoDigitsFormatter);
        mSecondsSelector.setWrapSelectorWheel(false);
        //
        //
        mHoursSelector.setValue(0);
        mMinutesSelector.setValue(0);
        mSecondsSelector.setValue(0);
        //
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setView(dialogBody);
        builder.setTitle(getString(R.string.title_dialog_audio_duration_manual_entry));
        builder.setPositiveButton(getString(R.string.generic_string_VALIDATE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirm();
            }
        });
        builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel(dialog);
            }
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        if(mListener!=null){
            mListener.onManualAudioDurationEntryCancel();
        }else{
            Log.e(TAG, "onCancel::no listener!!");
        }
        super.onCancel(dialog);
    }

    private void onConfirm(){
        int hours = mHoursSelector.getValue();
        int minutes = mMinutesSelector.getValue();
        int seconds = mSecondsSelector.getValue();
        long lengthinMs = (((hours*60)+minutes)*60 +seconds)*1000;
        Log.d(TAG, "onConfirm::lengthinMs = " + lengthinMs);
        if(mListener!=null){
            mListener.onManualAudioDurationEntryValidate(lengthinMs);
        }else{
            Log.e(TAG, "onConfirm::no listener!!");
        }
    }

////////////////////////////////////////
//Listener interface
    public interface DialogFragmentManualDurationEntryListener {
        void onManualAudioDurationEntryValidate(long manualAudioDuration);
        void onManualAudioDurationEntryCancel();
    }

}
