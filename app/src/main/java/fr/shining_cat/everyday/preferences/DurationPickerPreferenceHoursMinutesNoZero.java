package fr.shining_cat.everyday.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import java.util.concurrent.TimeUnit;

import fr.shining_cat.everyday.R;

public class DurationPickerPreferenceHoursMinutesNoZero extends DialogPreference {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private long mCurrentDuration;
    private NumberPicker mHoursSelector;
    private NumberPicker mMinutesSelector;
    private long mDefaultDuration;

////////////////////////////////////////
//Preference Dialog that will store a Duration selected with hours and minutes as milliseconds
//Time spin picker in the form of HH:MM
// does not allow a duration of 00:00
    public DurationPickerPreferenceHoursMinutesNoZero(Context context, AttributeSet attrs) {

        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_duration_picker);
        setPositiveButtonText(R.string.generic_string_VALIDATE);
        setNegativeButtonText(R.string.generic_string_CANCEL);
        mDefaultDuration = Long.parseLong(context.getString(R.string.default_duration));
        mCurrentDuration = this.getPersistedLong(mDefaultDuration);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        NumberPicker.Formatter twoDigitsFormatter = new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        };
        mHoursSelector = view.findViewById(R.id.hours_picker);
        mHoursSelector.setMaxValue(23);
        mHoursSelector.setMinValue(00);
        mHoursSelector.setFormatter(twoDigitsFormatter);
        mHoursSelector.setWrapSelectorWheel(false);
        mHoursSelector.setOnValueChangedListener(onHourValueChangedListener);
        //
        mMinutesSelector = view.findViewById(R.id.minutes_picker);
        mMinutesSelector.setMaxValue(59);
        mMinutesSelector.setWrapSelectorWheel(false);
        mMinutesSelector.setFormatter(twoDigitsFormatter);
        //
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(mCurrentDuration);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(mCurrentDuration) - TimeUnit.HOURS.toMinutes(fullHours));
        if(fullHours==0){
            mMinutesSelector.setMinValue(01);
            mMinutesSelector.setWrapSelectorWheel(false);
        } else{
            mMinutesSelector.setMinValue(00);
            mMinutesSelector.setWrapSelectorWheel(false);
        }
        mHoursSelector.setValue(fullHours);
        mMinutesSelector.setValue(fullMinutes);
    }
    NumberPicker.OnValueChangeListener onHourValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            if(mHoursSelector.getValue()==0){
                mMinutesSelector.setMinValue(01);
            } else{
                mMinutesSelector.setMinValue(00);
            }
        }
    };
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            mHoursSelector.setOnValueChangedListener(null);
            int hours = mHoursSelector.getValue();
            int minutes = mMinutesSelector.getValue();
            long newDurationinMs = ((hours*60)+minutes)*60000;
            Log.d(TAG, "onDialogClosed::newDurationinMs = " + newDurationinMs);
            mCurrentDuration = newDurationinMs;
            persistLong(newDurationinMs);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentDuration = this.getPersistedLong(mDefaultDuration);
        } else {
            // Set default state from the XML attribute
            mCurrentDuration = (Long) defaultValue;
            persistLong(mCurrentDuration);
        }
        Log.d(TAG, "onSetInitialValue::mCurrentDuration = " + mCurrentDuration);
    }

}
