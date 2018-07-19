package fr.shining_cat.everyday.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import fr.shining_cat.everyday.R;

public class DialogFragmentInfos extends DialogFragment {

    public static final String DIALOG_FRAGMENT_INFOS_TAG = "dialog_fragment_infos-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

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
        View dialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_infos, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setView(dialogBody);
        builder.setNeutralButton(getString(R.string.generic_string_CLOSE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel(dialog);
            }
        });//null, because we want to override default behavior to control dismissal on positive click
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        super.onCancel(dialog);
    }
}
