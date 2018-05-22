package fr.shining_cat.meditappli;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import fr.shining_cat.meditappli.utils.TimeOperations;

import static fr.shining_cat.meditappli.broadcastreceivers.AlarmSetterBroadcastReceiver.MEDITATION_REMINDER_SET_ALARM;
import static fr.shining_cat.meditappli.broadcastreceivers.AlarmSetterBroadcastReceiver.MEDITATION_REMINDER_CANCEL_ALARM;

public class SettingsFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

////////////////////////////////////////
//This fragment handles the preferences objects behaviour that need special care
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setDurationPreferenceSummary();
        setNotificationActiveChildrenState();
        setNotificationTimePreferenceSummary();
        setNotificationTextSummary();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }
    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

////////////////////////////////////////
//Monitoring changes to SharedPrefs
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
       Log.d(TAG, "onSharedPreferenceChanged::key = " + key);
        if (key.equals(getString(R.string.pref_duration_key))) {
            setDurationPreferenceSummary();
        } else if(key.equals(getString(R.string.pref_active_notification_key))) {
            setNotificationActiveChildrenState();
        } else if(key.equals(getString(R.string.pref_notification_time_key))) {
            setNotificationTimePreferenceSummary();
            //pref_notification_time_key can only be changed if the alarm is set to ACTIVE so we set the alarm now :
            setAlarm(true);
        }else if(key.equals(getString(R.string.pref_notification_text_key))) {
            setNotificationTextSummary();
        }else if(key.equals(getString(R.string.pref_switch_do_not_disturb_key))){
            checkIfPermissionGrantedforDND();
        }
    }

////////////////////////////////////////
//User ticked the Do not disturb activation mode during sessions, but we need to first check that permission is granted to modify this setting
    private void checkIfPermissionGrantedforDND() {
        //behaviour is different below or above Marshmallow (android6)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notifManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            // Check if the notification policy access has been granted for the app.
            if(notifManager!=null) {
                if (!notifManager.isNotificationPolicyAccessGranted()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.pref_switch_do_not_disturb_author_dialog_title));
                    builder.setMessage(getString(R.string.pref_switch_do_not_disturb_author_dialog_text));
                    builder.setNegativeButton(getString(R.string.generic_string_OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
            }else{
                Log.e(TAG, "checkIfPermissionGrantedforDND::notifManager == NULL!!");
            }
        }else{
            //no need for such permission runtime asking for older devices - we use AudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

////////////////////////////////////////
//Updating the duration preference summary with user's set value
    private void setDurationPreferenceSummary(){
        Log.d(TAG, "setDurationPreferenceSummary");
        //get duration preference value and format for use
        String durationPreferenceKey = getString(R.string.pref_duration_key);
        String durationPrefSummary;
        int defaultDuration = Integer.parseInt(getString(R.string.default_duration));
        long durationPreferenceValue = getPreferenceManager().getSharedPreferences().getLong(durationPreferenceKey, defaultDuration);
        durationPrefSummary = TimeOperations.convertMillisecondsToHoursAndMinutesString(
                durationPreferenceValue,
                getString(R.string.generic_string_SHORT_HOURS),
                getString(R.string.generic_string_SHORT_MINUTES),
                false);
        // Set summary to be the user-description for the selected value
        Preference durationPref = findPreference(durationPreferenceKey);
        durationPref.setSummary(durationPrefSummary);
    }

////////////////////////////////////////
//Setting the alarm accordingly to user's choice, AND updating the en- or disabling of Notification activation pref's subordinates prefs (set time, set ringtone, set reminder text) according to Notification pref status
    private void setNotificationActiveChildrenState() {
        String notificationActivePreferenceKey = getString(R.string.pref_active_notification_key);
        boolean defaultNotificationActive = Boolean.parseBoolean(getString(R.string.default_notification_active));
        boolean isNotificationActive = getPreferenceManager().getSharedPreferences().getBoolean(notificationActivePreferenceKey, defaultNotificationActive);
        Log.d(TAG, "setNotificationActiveChildrenState:isNotificationActive : " + isNotificationActive);
        //activate or inactivate notification settings
        String notificationTimePreferenceKey = getString(R.string.pref_notification_time_key);
        Preference notificationTimePref = findPreference(notificationTimePreferenceKey);
        notificationTimePref.setEnabled(isNotificationActive);
        //
        String notificationRingtonePreferenceKey = getString(R.string.pref_notification_ringtone_key);
        Preference notificationRingtonePref = findPreference(notificationRingtonePreferenceKey);
        notificationRingtonePref.setEnabled(isNotificationActive);
        //
        String notificationTextPreferenceKey = getString(R.string.pref_notification_text_key);
        Preference notificationTextPref = findPreference(notificationTextPreferenceKey);
        notificationTextPref.setEnabled(isNotificationActive);
        //activate the alarm with current stored value
        setAlarm(isNotificationActive);
    }

////////////////////////////////////////
//Helper method to launch broadcast setting alarm or not (broadcast monitored by AlarmSetterBroadcastReceiver)
    private void setAlarm(boolean activateAlarm){ //can only be set if
        if (activateAlarm) {
            Log.d(TAG, "setAlarm :: call for SET ALARM!");
            Intent setAlarmIntent = new Intent();
            setAlarmIntent.setAction(MEDITATION_REMINDER_SET_ALARM);
            getActivity().sendBroadcast(setAlarmIntent);
        } else{
            Log.d(TAG, "setAlarm :: call for CANCEL ALARM!");
            Intent cancelAlarmIntent = new Intent();
            cancelAlarmIntent.setAction(MEDITATION_REMINDER_CANCEL_ALARM);
            getActivity().sendBroadcast(cancelAlarmIntent);
        }
    }

////////////////////////////////////////
//Updating the notification time preference summary with user's set value
    private void setNotificationTimePreferenceSummary(){
        Log.d(TAG, "setNotificationTimePreferenceSummary");
        //get time preference value
        String timeOfDayNotificationPreferenceKey = getString(R.string.pref_notification_time_key);
        String defaultTimePickerValue = getString(R.string.default_time_picker);
        String timeOfDayNotificationPrefValue = getPreferenceManager().getSharedPreferences().getString(timeOfDayNotificationPreferenceKey, defaultTimePickerValue);
        // Set summary to be the user-description for the selected value
        Preference durationPref = findPreference(timeOfDayNotificationPreferenceKey);
        durationPref.setSummary(timeOfDayNotificationPrefValue);
    }

////////////////////////////////////////
//Updating the notification time preference summary with user's set value
    private void setNotificationTextSummary(){
        Log.d(TAG, "setNotificationTextSummary");
        //get notification text preference value and format for use
        String notifTextPreferenceKey = getString(R.string.pref_notification_text_key);
        String notifTextPrefSummary = getPreferenceManager().getSharedPreferences().getString(notifTextPreferenceKey, getString(R.string.reminder_alarm_notification_default_text));
        // Set summary to be the user-description for the selected value
        Preference notifTextPref = findPreference(notifTextPreferenceKey);
        notifTextPref.setSummary(notifTextPrefSummary);
    }
}