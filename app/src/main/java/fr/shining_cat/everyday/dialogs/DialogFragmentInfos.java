package fr.shining_cat.everyday.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.support.constraint.ConstraintLayout.LayoutParams;

import fr.shining_cat.everyday.R;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class DialogFragmentInfos extends DialogFragment {

    public static final String DIALOG_FRAGMENT_INFOS_TAG = "dialog_fragment_infos-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private View mDialogBody;

////////////////////////////////////////
//this DialogFragment just shows some explication text and the app's infos
    public DialogFragmentInfos(){
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogFragmentInfos newInstance(){
        return new DialogFragmentInfos();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_infos, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setView(mDialogBody);
        builder.setPositiveButton(getString(R.string.generic_string_CLOSE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel(dialog);
            }
        });
        AlertDialog dialog = builder.create();
        //dialog.getWindow().setLayout(600, 400);
        return dialog;
    }

    @Override
    public void onResume() {
        int screenLayout = getActivity().getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
        if(screenLayout != SCREENLAYOUT_SIZE_XLARGE) { //keep basic dialog size behaviour for xlarge screens
            // Store access variables for window and blank point
            Window window = getDialog().getWindow();
            Point size = new Point();
            // Store dimensions of the screen in `size`
            Display display;
            if (window != null) {
                display = window.getWindowManager().getDefaultDisplay();
                display.getSize(size);
                int orientation = display.getRotation();
                int dialogToScreenWidthRatio;
                if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
                    // LANDSCAPE mode
                    dialogToScreenWidthRatio = Integer.parseInt(getString(R.string.default_dialog_to_screen_width_ratio_landscape));
                    // Set the width of the dialog proportional to dialogToScreenWidthRatio% of the screen width
                    window.setLayout((size.x * dialogToScreenWidthRatio / 100), LayoutParams.WRAP_CONTENT);
                } else {
                    // PORTRAIT mode
                    dialogToScreenWidthRatio = Integer.parseInt(getString(R.string.default_dialog_to_screen_width_ratio_portrait));
                    window.setLayout((size.x * dialogToScreenWidthRatio / 100), LayoutParams.WRAP_CONTENT);
                }
                window.setGravity(Gravity.CENTER);
            } else {
                Log.e(TAG, "onResume::could not get screen size to set dialog width : window is NULL!");
            }
        }
        // Call super onResume after sizing
        super.onResume();
    }

    private static String getSizeName(Context context) {
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case 4: // Configuration.SCREENLAYOUT_SIZE_XLARGE is API >= 9
                return "xlarge";
            default:
                return "undefined";
        }
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        super.onCancel(dialog);
    }
}
