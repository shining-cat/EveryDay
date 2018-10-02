package fr.shining_cat.everyday;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseThemedActivity extends AppCompatActivity {

    private String mCurrentTheme;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String chosenStylePrefKey = getString(R.string.pref_app_visual_theme_key);
        mCurrentTheme = mSharedPrefs.getString(chosenStylePrefKey, getString(R.string.default_theme_value));
        int chosenStyleResourceId = getResources().getIdentifier(mCurrentTheme, "style", getPackageName());
        setTheme(chosenStyleResourceId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String chosenStylePrefKey = getString(R.string.pref_app_visual_theme_key);
        String newTheme= mSharedPrefs.getString(chosenStylePrefKey, getString(R.string.default_theme_value));
        if(!newTheme.equals(mCurrentTheme)){
            recreate();
        }
    }
}
