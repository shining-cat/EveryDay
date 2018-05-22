package fr.shining_cat.meditappli.preferences;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import fr.shining_cat.meditappli.R;

public class ImportSessionsPreference extends DialogPreference {

    public static final int ACTIVITY_CHOSING_CSV_FILE_FOR_IMPORT = 321;

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private AppCompatActivity mContext;

////////////////////////////////////////
//Preference Dialog that will launch an intent.ACTION_GET_CONTENT (caught by parent SettingsActivity), prompting the user to choose a csv file for import
    public ImportSessionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (AppCompatActivity) context;
        //we just need a regular alertdialog here, no need for a custom layout
        setDialogTitle(R.string.pref_import_sessions_dialog_title);
        setDialogMessage(R.string.pref_import_all_sessions_dialog_message);
        setPositiveButtonText(R.string.pref_import_all_sessions_choose_file_button);

        setNegativeButtonText(R.string.generic_string_CANCEL);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            Intent openCsvIntent = new Intent(Intent.ACTION_GET_CONTENT);
            openCsvIntent.addCategory(Intent.CATEGORY_OPENABLE);
            //does not allow to choose a .csv file on my Archos API26 :
            //openCsvIntent.setType("text/csv");
            //set a larger filter
            openCsvIntent.setType("text/comma-separated-values");
            mContext.startActivityForResult(openCsvIntent, ACTIVITY_CHOSING_CSV_FILE_FOR_IMPORT);
        }

    }


}
