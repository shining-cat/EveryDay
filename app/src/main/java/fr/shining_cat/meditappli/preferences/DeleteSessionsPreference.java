package fr.shining_cat.meditappli.preferences;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;

import fr.shining_cat.meditappli.R;
import fr.shining_cat.meditappli.data.MeditAppliRepository;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;

public class DeleteSessionsPreference extends DialogPreference {


    private AppCompatActivity mContext;

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

////////////////////////////////////////
//Preference Dialog that will ask for confirmation before deleting all entries from sessions table
    public DeleteSessionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (AppCompatActivity) context;
        //we just need a regular alertdialog here, no need for a custom layout
        setDialogTitle(R.string.pref_delete_all_sessions_title);
        setDialogMessage(R.string.pref_delete_all_sessions_message);
        setPositiveButtonText(R.string.generic_string_DELETE);
        setNegativeButtonText(R.string.generic_string_CANCEL);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            Log.d(TAG, "onDialogClosed:: DELETING ALL DATAS !!");
            SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(mContext).get(SessionRecordViewModel.class);
            sessionRecordViewModel.deleteAllSessions((MeditAppliRepository.MeditAppliRepoListener)mContext);
        }
    }
}
