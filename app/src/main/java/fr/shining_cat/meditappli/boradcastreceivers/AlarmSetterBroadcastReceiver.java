package fr.shining_cat.meditappli;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import fr.shining_cat.meditappli.preferences.TimePickerPreference;

import static fr.shining_cat.meditappli.AlarmRingerBroadcastReceiver.MEDITATION_REMINDER_DISPLAY_NOTIFICATION;

public class AlarmSetterBroadcastReceiver extends BroadcastReceiver {
    /*
    set (or cancel) the alarm according to preferences when user sets one AND on device reboot (all alarms are reset on device reboot)
     */
    public final static String MEDITATION_REMINDER_SET_ALARM = "fr.shining_cat.meditappli.SET_ALARM";
    public final static String MEDITATION_REMINDER_CANCEL_ALARM = "fr.shining_cat.meditappli.CANCEL_ALARM";
    public final static String MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY = "fr.shining_cat.meditappli.CANCEL_ALARM_FOR_TODAY";
    public final static int ALARM_NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 0;

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //program the alarm here
        String action = intent.getAction();
        Log.d(TAG, "onReceive::action = " + action);
        Toast.makeText(context, "INTENT received! : "+ action, Toast.LENGTH_LONG).show();
        if (    action.equals(MEDITATION_REMINDER_SET_ALARM) ||
                action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY)||
                action.equals(MEDITATION_REMINDER_CANCEL_ALARM)) { //otherwise we are not concerned
            AlarmManager alarmMngr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmMngr == null) {
                Log.e(TAG, "setAlarm::AlarmManager not provided :: ABORTED ALARM SETTING");
            } else {
                //prepare alarmIntent and pendingAlarmIntent
                Intent alarmIntent = new Intent(context, AlarmRingerBroadcastReceiver.class);
                alarmIntent.setAction(MEDITATION_REMINDER_DISPLAY_NOTIFICATION);
                PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(context, ALARM_NOTIFICATION_PENDING_INTENT_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmMngr.cancel(pendingAlarmIntent);
                //check if alarm is activated in preference  => to know if we have to reset it on device boot
                String isAlarmActivePreferenceKey = context.getString(R.string.pref_active_notification_key);
                boolean isAlarmActive = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(isAlarmActivePreferenceKey, false);
                //get time preference value
                String timeOfDayNotificationPreferenceKey = context.getString(R.string.pref_notification_time_key);
                String defaultTimePickerValue = context.getString(R.string.default_time_picker);
                String timeOfDayNotificationPrefValueAsString = PreferenceManager.getDefaultSharedPreferences(context).getString(timeOfDayNotificationPreferenceKey, defaultTimePickerValue);
                int hours = Integer.parseInt(timeOfDayNotificationPrefValueAsString.substring(0, 2));
                int minutes = Integer.parseInt(timeOfDayNotificationPrefValueAsString.substring(3));
                //build the corresponding calendar object
                Calendar alarmAbsTimeCalendar = Calendar.getInstance();
                //set calendar object to NOW
                alarmAbsTimeCalendar.setTimeInMillis(System.currentTimeMillis());
                //change calendar object TIME to preference's value (DATE is TODAY)
                alarmAbsTimeCalendar.set(Calendar.HOUR_OF_DAY, hours);
                alarmAbsTimeCalendar.set(Calendar.MINUTE, minutes);
                alarmAbsTimeCalendar.set(Calendar.SECOND, 0);
                alarmAbsTimeCalendar.set(Calendar.MILLISECOND, 0);
                //set alarm if user just set it in preferences, or if device just booted and alarm should be active
                if (action.equals(MEDITATION_REMINDER_SET_ALARM) || (action.equals(Intent.ACTION_BOOT_COMPLETED) && isAlarmActive)) {
                    /*//for tests :
                        Toast.makeText(context, "SETTING ALARM", Toast.LENGTH_LONG).show();
                        alarmMngr.setRepeating(AlarmManager.RTC_WAKEUP, alarmAbsTimeCalendar.getTimeInMillis(), 1 * 60 * 1000, pendingAlarmIntent);
                    */
                    alarmMngr.setRepeating(AlarmManager.RTC_WAKEUP, alarmAbsTimeCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingAlarmIntent);
                    Log.d(TAG, "setAlarm::alarm set to  : " + timeOfDayNotificationPrefValueAsString);
                }
                //a session has started : cancel the alarm for the current day and set it again starting on the next day
                else if (action.equals(MEDITATION_REMINDER_CANCEL_ALARM_FOR_TODAY)) {
                    //change calendar object DATE to TODAY + 1
                    alarmAbsTimeCalendar.add(Calendar.DATE, 1);
                    alarmMngr.setRepeating(AlarmManager.RTC_WAKEUP, alarmAbsTimeCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingAlarmIntent);
                    Log.d(TAG, "setAlarm::alarm cancelled for today, ALARM IS SET FOR TOMORROW :: alarm set to  : " + timeOfDayNotificationPrefValueAsString);
                }
                //completely cancel the alarm
                else if (action.equals(MEDITATION_REMINDER_CANCEL_ALARM)) {
                    Log.d(TAG, "onReceive::alarm canceled : NO MORE ALARM!");
                }
            }
        }
    }
}
