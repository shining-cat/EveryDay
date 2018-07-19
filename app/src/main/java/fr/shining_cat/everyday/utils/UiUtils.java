package fr.shining_cat.everyday.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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
}
