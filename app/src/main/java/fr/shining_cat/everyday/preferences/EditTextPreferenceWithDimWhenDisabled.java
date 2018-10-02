package fr.shining_cat.everyday.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class EditTextPreferenceWithDimWhenDisabled extends EditTextPreference {
    public EditTextPreferenceWithDimWhenDisabled(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //override because somehow the dimming effect is lost when pref is disabled by linked parent if textColorPrimary and textColorSecondary are defined in applied theme
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = view.findViewById(android.R.id.title);
        TextView summary = view.findViewById(android.R.id.summary);
        if (title.isEnabled()) {
            title.setAlpha(1f);
        } else {
            title.setAlpha(0.3f);
        }
        if (summary.isEnabled()) {
            summary.setAlpha(1f);
        } else {
            summary.setAlpha(0.3f);
        }
    }
}
