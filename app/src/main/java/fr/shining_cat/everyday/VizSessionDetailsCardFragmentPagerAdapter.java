package fr.shining_cat.everyday;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;

import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.CardAdapter;

public class VizSessionDetailsCardFragmentPagerAdapter  extends FragmentStatePagerAdapter
                                                        implements CardAdapter {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private List<SessionRecord> mSessions; // Cached copy of sessions
    private float mBaseElevation;

    public VizSessionDetailsCardFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setSessions(List<SessionRecord> sessions){
        Log.d(TAG, "setSessions");
        mSessions = sessions;
        notifyDataSetChanged();
    }

    public void setBaseElevation(float baseElevation){
        mBaseElevation = baseElevation;
    }

    @Override
    public Fragment getItem(int position) {
        if(mSessions !=null){
            SessionRecord currentSession = mSessions.get(position);
            VizSessionDetailsCardFragment vizSessionDetailsCardFragment = VizSessionDetailsCardFragment.newInstance();
            vizSessionDetailsCardFragment.setContent(currentSession);
            return vizSessionDetailsCardFragment;
        }else{
            //data not yet ready
            Log.d(TAG, "getItem::data not available!");
            return null;
        }
    }


    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return ((VizSessionDetailsCardFragment) getItem(position)).getCardView();
    }

    public SessionRecord getSessionRecordAtPosition(int position){
        return mSessions.get(position);
    }

    public int getPositionOfSpecificSessionRecord(SessionRecord sessionRecord){
        long idSession = sessionRecord.getId();
        for(SessionRecord session : mSessions){
            if(session.getId() == idSession) return mSessions.indexOf(session);
        }
        return -1;
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
