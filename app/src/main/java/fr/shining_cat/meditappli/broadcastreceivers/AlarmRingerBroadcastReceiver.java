package fr.shining_cat.meditappli.broadcastreceivers;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import fr.shining_cat.meditappli.MainActivity;
import fr.shining_cat.meditappli.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmRingerBroadcastReceiver extends BroadcastReceiver {

////////////////////////////////////////
//handles the broadcast from alarmManager and dispatch a notification


    public final static String MEDITATION_REMINDER_DISPLAY_NOTIFICATION = "fr.shining_cat.meditappli.REMINDER";
    private static final String ACTION_CANCEL_NOTIFICATION = "fr.shining_cat.meditappli.ACTION_CANCEL_NOTIFICATION";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final int REMINDER_NOTIFICATION_PENDING_INTENT_ID = 613;
    private static final int REMINDER_NOTIFICATION_CANCEL_PENDING_INTENT_ID = 615;
    private static final int SESSION_ALARM_REMINDER_NOTIFICATION_ID = 617;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(MEDITATION_REMINDER_DISPLAY_NOTIFICATION)) {
            //fire reminder notification
            Log.d(TAG, "onReceive ::  ALARM RECEIVED!!");
            //Sets up the pending intent that is delivered when the notification is clicked
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, REMINDER_NOTIFICATION_PENDING_INTENT_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Sets up the pending intent to cancel the notification,
            // delivered when the user dismisses the notification
            Intent cancelIntent = new Intent(ACTION_CANCEL_NOTIFICATION);
            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, REMINDER_NOTIFICATION_CANCEL_PENDING_INTENT_ID, cancelIntent, PendingIntent.FLAG_ONE_SHOT);
            //set notification text as settings stored it
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String notifText = preferences.getString(context.getString(R.string.pref_notification_text_key), context.getString(R.string.reminder_alarm_notification_default_text));
            //Builds the notification with all of the parameters
            Notification.Builder notifyBuilder = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.reminder_alarm_notification_title))
                    .setContentText(notifText)
                    //TODO : create ICON for notifications
                    .setSmallIcon(R.drawable.ic_info_white_24dp)
                    .setContentIntent(notificationPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDeleteIntent(cancelPendingIntent)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
            //set notification sound as settings stored it
            String notifSoundURIAsString = preferences.getString(context.getString(R.string.pref_notification_ringtone_key), null); // default = null. null is also the value stored if user chose SILENCE
            Log.d(TAG, "onReceive ::  sound to play : " + notifSoundURIAsString);
            if (notifSoundURIAsString != null) {
                Uri notifSoundURI = Uri.parse(notifSoundURIAsString);
                Log.d(TAG, "onReceive ::  notifSoundURI : " + notifSoundURI.toString());
                notifyBuilder.setSound(notifSoundURI);
            }
            //Delivers the notification
            NotificationManager notifyManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (notifyManager != null) {
                notifyManager.notify(SESSION_ALARM_REMINDER_NOTIFICATION_ID, notifyBuilder.build());
            } else {
                Log.e(TAG, "onReceive::notifyManager is null!!");
            }
        }else {
            Log.e(TAG, "onReceive::action is null!!");
        }

    }

}
