package fr.shining_cat.everyday.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import fr.shining_cat.everyday.R;

public class TimePickerPreference extends DialogPreference {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private TimePicker mTimePicker;
    private String mCurrentTime;
    private String mDefaultTimePickerValue;

////////////////////////////////////////
//Preference Dialog that will store a Time of the day selected with 2 android TimePickers
    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_time_picker);
        setPositiveButtonText(R.string.generic_string_VALIDATE);
        setNegativeButtonText(R.string.generic_string_CANCEL);
        mDefaultTimePickerValue = context.getString(R.string.default_time_picker);
        mCurrentTime = this.getPersistedString(mDefaultTimePickerValue);
    }

    //override because somehow the dimming effect is lost when pref is disabled by linked parent if textColorPrimary and textColorSecondary are defined in applied theme
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = view.findViewById(android.R.id.title);
        TextView summary = view.findViewById(android.R.id.summary);
        if (title.isEnabled()) {
            title.setAlpha(1f);
        } else {
            title.setAlpha(0.3f);
        }
        if (summary.isEnabled()) {
            summary.setAlpha(1f);
        } else {
            summary.setAlpha(0.3f);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mTimePicker = view.findViewById(R.id.timePicker);

        mTimePicker.setIs24HourView(true);
        int hour = Integer.parseInt(mCurrentTime.substring(0, 2));
        int minutes = Integer.parseInt(mCurrentTime.substring(3));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minutes);
        }else{
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minutes);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        String hours = String.format("%02d", mTimePicker.getCurrentHour());
        String minutes = String.format("%02d", mTimePicker.getCurrentMinute());
        mCurrentTime = hours + ":" + minutes;
        persistString(mCurrentTime);
        Log.d(TAG, "onDialogClosed::mCurrentTime = " + mCurrentTime);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentTime = this.getPersistedString(mDefaultTimePickerValue);
        } else {
            // Set default state from the XML attribute
            mCurrentTime = (String) defaultValue;
            persistString(mCurrentTime);
        }
        Log.d(TAG, "onSetInitialValue::mCurrentTime = " + mCurrentTime);
    }
}
