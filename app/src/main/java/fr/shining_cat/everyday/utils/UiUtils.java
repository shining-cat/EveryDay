package fr.shining_cat.everyday.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import fr.shining_cat.everyday.SessionInProgressFragment;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class UiUtils {


////////////////////////////////////////
//static method to hide soft keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View focusedView = activity.getCurrentFocus();
            if(focusedView!=null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

////////////////////////////////////////
//static method to remove sticky notification
    public static  void removeStickyNotification(Context context, int notificationId){
        //remove sticky notification set up by SessionInProgressFragment
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.cancel(notificationId);
        } else {
            Log.e("UiUtils", "removeStickyNotification::notifyManager is null!!");
        }
    }

}
