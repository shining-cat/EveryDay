package fr.shining_cat.meditappli.data;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.List;

import fr.shining_cat.meditappli.SessionDetailsFragment;

public class SessionsDetailsFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private List<SessionRecord> mSessions; // Cached copy of sessions

////////////////////////////////////////
//FragmentStatePagerAdapter used by ViewPagerSessionsDetailsFragment on the ViewPager to display details info for one session in a ViewPagerSessionsDetailsFragment
//does not go far from usage described in documentation except hacking getItemPosition to allow current fragment update on notifyDataSetChanged
    public SessionsDetailsFragmentStatePagerAdapter(FragmentManager fm){
        super(fm);
    }

    public void setSessions(List<SessionRecord> sessions){
        Log.d(TAG, "setSessions");
        mSessions = sessions;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if(mSessions !=null){
            SessionRecord currentSession = mSessions.get(position);
            SessionDetailsFragment sessionDetailsFragment = SessionDetailsFragment.newInstance();
            sessionDetailsFragment.setContent(currentSession);
            return sessionDetailsFragment;
        }else{
            //data not yet ready
            Log.d(TAG, "getItem::data not available!");
            return null;
        }
    }

    public SessionRecord getSessionRecordAtPosition(int position){
        return mSessions.get(position);
    }

    @Override
    public int getCount() {
        if(mSessions != null){
            return mSessions.size();
        }else {
            return 0;
        }
    }

    //Hack disabling the FragmentStatePagerAdapter cache mechanism to allow current fragment update on notifyDataSetChanged
    //see : https://stackoverflow.com/questions/30080045/fragmentpageradapter-notifydatasetchanged-not-working
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
