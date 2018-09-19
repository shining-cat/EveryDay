package fr.shining_cat.everyday;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.utils.CardAdapter;

public class RewardDetailsCardFragmentPagerAdapter  extends FragmentStatePagerAdapter
                                                    implements CardAdapter {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();


    private List<Reward> mRewards; // Cached copy of rewards
    private float mBaseElevation;

    public RewardDetailsCardFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setRewards(List<Reward> rewards){
        Log.d(TAG, "setSessions");
        mRewards = rewards;
        notifyDataSetChanged();
    }

    public void setBaseElevation(float baseElevation){
        mBaseElevation = baseElevation;
    }

    @Override
    public Fragment getItem(int position) {
        if(mRewards !=null){
            Reward currentReward = mRewards.get(position);
            RewardDetailsCardFragment rewardDetailsCardFragment = RewardDetailsCardFragment.newInstance();
            rewardDetailsCardFragment.setContent(currentReward);
            return rewardDetailsCardFragment;
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
        return ((RewardDetailsCardFragment) getItem(position)).getCardView();
    }

    public Reward getRewardAtPosition(int position){
        return mRewards.get(position);
    }

    public int getPositionOfSpecificReward(Reward wantedReward){
        long idReward = wantedReward.getId();
        for(Reward reward : mRewards){
            if(reward.getId() == idReward) return mRewards.indexOf(reward);
        }
        return -1;
    }

    @Override
    public int getCount() {
        if(mRewards != null){
            return mRewards.size();
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
