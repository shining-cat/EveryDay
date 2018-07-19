package fr.shining_cat.everyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import fr.shining_cat.everyday.dialogs.DialogFragmentInfos;
import fr.shining_cat.everyday.utils.TimeOperations;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final int ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION = 513;

    //TODO :  à l'air ok mais faire des tests lifecycle dans l'ensemble de l'appli.
    //TODO : animer les transitions entre pages/fragments?
    //TODO : haptic feedback!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setStartSessionBtnText();
        Button startSessionBtn = findViewById(R.id.start_session_btn);
        startSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartTimedSessionBtnClicked();
            }
        });
        Button viewStatsBtn = findViewById(R.id.view_stats_btn);
        Button startGuidedSessionBtn = findViewById(R.id.start_guided_session_btn);
        startGuidedSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartGuidedSessionClicked();
            }
        });
        viewStatsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewStatsBtnClicked();
            }
        });
    }

////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_infos) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragmentInfos dialogFragmentInfos = DialogFragmentInfos.newInstance();
            dialogFragmentInfos.show(fm, DialogFragmentInfos.DIALOG_FRAGMENT_INFOS_TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

////////////////////////////////////////
//customizing buttons' text
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setStartSessionBtnText();
    }

    private void setStartSessionBtnText() {
        String durationPreferenceKey = getString(R.string.pref_duration_key);
        long defaultDuration = Long.parseLong(getString(R.string.default_duration));
        long durationValue = PreferenceManager.getDefaultSharedPreferences(this).getLong(durationPreferenceKey, defaultDuration);
        //String startSessionBtnTxt = String.format(getString(R.string.start_session_btn_txt), duration);
        String durationString = getString(R.string.launch_session_for) + " \n";
        durationString += TimeOperations.convertMillisecondsToHoursAndMinutesString(
                durationValue,
                getString(R.string.generic_string_HOURS),
                getString(R.string.generic_string_MINUTES),
                true
        );
        //
        String infiniteSessionPreferenceKey = getString(R.string.pref_switch_infinite_session_key);
        boolean infiniteSession = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(infiniteSessionPreferenceKey, false);
        if(infiniteSession){
            durationString += "\n" + getString(R.string.start_session_btn_or_more_txt);
        }
        //
        Button startSessionBtn = findViewById(R.id.start_session_btn);
        startSessionBtn.setText(durationString);
    }

    private void setViewStatsBtnText(){

    }

    private void setStartGuidedSessionBtnText(){

    }
////////////////////////////////////////
//there are 3 buttons in main : open stats, launch guided session, and launch timed session
    private void onViewStatsBtnClicked() {
        Intent myIntent = new Intent(this, VizActivity.class);
        startActivity(myIntent);
    }

    private void onStartTimedSessionBtnClicked() {
        Intent myIntent = new Intent(this, SessionActivity.class);
        startActivity(myIntent);
    }

    private void onStartGuidedSessionClicked(){
        Log.d(TAG, "onStartGuidedSessionClicked");
        Intent chooseMp3Intent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseMp3Intent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseMp3Intent.setType("audio/*");
        this.startActivityForResult(chooseMp3Intent, ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION);
    }

////////////////////////////////////////
//INCOMING INTENTS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_CHOSING_MP3_FILE_FOR_GUIDED_SESSION:
                    //intent sent by ImportSessionsPreference => file has been chosen, parse and import with SessionsImportCSVParsingAsync
                    //WARNING : data coming back from Intent.ACTION_GET_CONTENT is not necessarily a File (but it can be which could lead to not detecting error at first) but has to be handled as a content Uri
                    Uri audioContentUri = data.getData();
                    Log.d(TAG, "onActivityResult::audioContentUri = " + audioContentUri);
                    Intent myIntent = new Intent(this, SessionActivity.class);
                    myIntent.putExtra(SessionActivity.SOUND_FOR_GUIDED_SESSION_URI_INTENT_KEY, audioContentUri);
                    startActivity(myIntent);
                    break;
            }
        }
    }

}
