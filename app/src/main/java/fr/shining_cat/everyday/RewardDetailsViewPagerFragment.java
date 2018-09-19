package fr.shining_cat.everyday;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.utils.ShadowTransformer;

public class RewardDetailsViewPagerFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_PAGER_REWARD_DETAILS_FRAGMENT_TAG = "view_pager_reward_details_Fragment-tag";

    private View mRootView;
    private ViewPager mRewardsDetailsViewPager;
    private RewardDetailsCardFragmentPagerAdapter mRewardDetailsCardFragmentStatePagerAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;
    private Reward mRewardToOpenDetailsFragment;
    private RewardViewModel mRewardViewModel;


////////////////////////////////////////
//This is the fragment used to hold a viewPager (with VizSessionDetailsCardFragmentPagerAdapter as FragmentStatePagerAdapter) presenting the sessions details (VizSessionDetailsCardFragment)
    public RewardDetailsViewPagerFragment() {
        // Required empty public constructor
    }

    public static RewardDetailsViewPagerFragment newInstance() {
        return new RewardDetailsViewPagerFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_viewpager_rewards_details, container, false);
        //
        mRewardsDetailsViewPager = mRootView.findViewById(R.id.rewards_details_viewpager);
        mRewardDetailsCardFragmentStatePagerAdapter = new RewardDetailsCardFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        mRewardsDetailsViewPager.setAdapter(mRewardDetailsCardFragmentStatePagerAdapter);
        //
        mFragmentCardShadowTransformer = new ShadowTransformer(mRewardsDetailsViewPager, mRewardDetailsCardFragmentStatePagerAdapter);

        mRewardsDetailsViewPager.setPageTransformer(false, mFragmentCardShadowTransformer);
        mRewardsDetailsViewPager.setOffscreenPageLimit(3);
        //
        mRewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        mRewardViewModel.getAllRewardsActiveAcquisitionDateDesc().observe(this, new Observer<List<Reward>>() {
            @Override
            public void onChanged(@Nullable List<Reward> rewards) {
                // Update the cached copy of the rewards in the adapter.
                mRewardDetailsCardFragmentStatePagerAdapter.setRewards(rewards);
                mFragmentCardShadowTransformer.enableScaling(true);
                //set starting position if known
                if(mRewardToOpenDetailsFragment != null){
                    int rewardPositionInAdapter = mRewardDetailsCardFragmentStatePagerAdapter.getPositionOfSpecificReward(mRewardToOpenDetailsFragment);
                    mRewardsDetailsViewPager.setCurrentItem(rewardPositionInAdapter);
                }else{
                    mRewardsDetailsViewPager.setCurrentItem(0);
                }
            }
        });
        return mRootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRewardsDetailsViewPager.clearOnPageChangeListeners();
    }

    public int getCurrentPosition(){
        return mRewardsDetailsViewPager.getCurrentItem();
    }


////////////////////////////////////////
//get current object Reward
    public Reward getCurrentReward(){
        return ((RewardDetailsCardFragmentPagerAdapter) mRewardsDetailsViewPager.getAdapter()).getRewardAtPosition(mRewardsDetailsViewPager.getCurrentItem());
    }

////////////////////////////////////////
//get specific object Reward
    public Reward getRewardAtPosition(int position){
        return ((RewardDetailsCardFragmentPagerAdapter) mRewardsDetailsViewPager.getAdapter()).getRewardAtPosition(position);
    }

////////////////////////////////////////
//transmit desired starting position
    public void setStartingRewardDetailsWithReward(Reward startingReward) {
        mRewardToOpenDetailsFragment = startingReward;
    }
}
