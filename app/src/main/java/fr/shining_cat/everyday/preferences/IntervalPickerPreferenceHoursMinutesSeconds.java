package fr.shining_cat.everyday.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import java.util.concurrent.TimeUnit;

import fr.shining_cat.everyday.R;

public class IntervalPickerPreferenceHoursMinutesSeconds extends DialogPreference {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private long mCurrentLength;
    private NumberPicker mHoursSelector;
    private NumberPicker mMinutesSelector;
    private NumberPicker mSecondsSelector;
    private long mDefaultLength;

////////////////////////////////////////
//Preference Dialog that will store an interval length selected with hours and minutes and seconds as milliseconds
//Time spin picker in the form of HH:MM:SS
// allows a length of 00:00:00

    public IntervalPickerPreferenceHoursMinutesSeconds(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_interval_picker);
        setPositiveButtonText(R.string.generic_string_VALIDATE);
        setNegativeButtonText(R.string.generic_string_CANCEL);
        mDefaultLength = Long.parseLong(context.getString(R.string.default_intermediate_interval_length));
        mCurrentLength = this.getPersistedLong(mDefaultLength);
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
        mHoursSelector = view.findViewById(R.id.interval_hours_picker);
        mHoursSelector.setMaxValue(23);
        mHoursSelector.setMinValue(0);
        mHoursSelector.setFormatter(twoDigitsFormatter);
        mHoursSelector.setWrapSelectorWheel(false);
        //
        mMinutesSelector = view.findViewById(R.id.interval_minutes_picker);
        mMinutesSelector.setMaxValue(59);
        mMinutesSelector.setMinValue(0);
        mMinutesSelector.setFormatter(twoDigitsFormatter);
        mMinutesSelector.setWrapSelectorWheel(false);
        //
        mSecondsSelector = view.findViewById(R.id.interval_seconds_picker);
        mSecondsSelector.setMaxValue(59);
        mSecondsSelector.setMinValue(0);
        mSecondsSelector.setFormatter(twoDigitsFormatter);
        mSecondsSelector.setWrapSelectorWheel(false);
        //
        int fullHours   = (int) TimeUnit.MILLISECONDS.toHours(mCurrentLength);
        int fullMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(mCurrentLength) - TimeUnit.HOURS.toMinutes(fullHours));
        int fullSeconds = (int) (TimeUnit.MILLISECONDS.toSeconds(mCurrentLength) - (TimeUnit.HOURS.toSeconds(fullHours) + TimeUnit.MINUTES.toSeconds(fullMinutes)));
        //
        mHoursSelector.setValue(fullHours);
        mMinutesSelector.setValue(fullMinutes);
        mSecondsSelector.setValue(fullSeconds);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            int hours = mHoursSelector.getValue();
            int minutes = mMinutesSelector.getValue();
            int seconds = mSecondsSelector.getValue();
            long newLengthinMs = (((hours*60)+minutes)*60 +seconds)*1000;
            Log.d(TAG, "onDialogClosed::newLengthinMs = " + newLengthinMs);
            mCurrentLength = newLengthinMs;
            persistLong(newLengthinMs);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentLength = this.getPersistedLong(mDefaultLength);
        } else {
            // Set default state from the XML attribute
            mCurrentLength = (Long) defaultValue;
            persistLong(mCurrentLength);
        }
        Log.d(TAG, "onSetInitialValue::mCurrentLength = " + mCurrentLength);
    }

}
