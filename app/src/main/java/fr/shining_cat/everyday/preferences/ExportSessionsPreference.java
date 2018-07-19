package fr.shining_cat.everyday.preferences;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.preference.DialogPreference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;

import fr.shining_cat.everyday.R;
import fr.shining_cat.everyday.data.EveryDayRepository;
import fr.shining_cat.everyday.data.SessionRecordViewModel;

public class ExportSessionsPreference extends DialogPreference {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private AppCompatActivity mContext;

////////////////////////////////////////
//Preference Dialog that will ask for confirmation then get all sessions records. Since exporting is the only case where we will ask for a NOT LIVE List of this data,
// the EveryDayRepository callback onGetAllSessionsNotLiveComplete is handled by parent SettingsActivity, which will then actually proceed to export the data to a csv file
    public ExportSessionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (AppCompatActivity) context;
        //we just need a regular alertdialog here, no need for a custom layout
        setDialogTitle(R.string.pref_export_sessions_dialog_title);
        setDialogMessage(R.string.pref_export_all_sessions_dialog_message);
        setPositiveButtonText(R.string.pref_export_all_sessions_export_button);
        setNegativeButtonText(R.string.generic_string_CANCEL);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            Log.d(TAG, "onDialogClosed:: EXPORTING ALL DATAS !!");
            SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(mContext).get(SessionRecordViewModel.class);
            sessionRecordViewModel.getAllSessionsRecordsInBunch((EveryDayRepository.EveryDayRepoListener)mContext);
        }
    }
}
