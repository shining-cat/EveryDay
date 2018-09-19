package fr.shining_cat.everyday;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;


////////////////////////////////////////
//This Fragment shows reward(s) obtained after a session

public class RewardsObtainedFragment extends Fragment {

    public static final String FRAGMENT_REWARD_OBTAINED_TAG = "fragment_reward_obtained_or_lost-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final String ARG_LEVEL         = "level_int_argument";
    private static final String ARG_REWARDS_CHANCES = "reward_count_int_argument";


    private View mRootView;
    private FragmentRewardObtainedListener mListener;
    private RecyclerView mRewardsListRecyclerView;
    private RewardsObtainedListAdapter mRewardsListAdapter;
    private int mLevel;
    private int[] mRewardsChances;
    private List<Reward> mPickedRewards;
    private int mRewardsGrantedCount;
    private int mRewardsPickedCount;


    public RewardsObtainedFragment() {
        // Required empty public constructor
    }

    public static RewardsObtainedFragment newInstance(int level, int[] rewardsChances) {
        Log.d("RewardsObtainedFragment", "newInstance");
        RewardsObtainedFragment fragment = new RewardsObtainedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LEVEL, level);
        args.putIntArray(ARG_REWARDS_CHANCES, rewardsChances);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLevel = getArguments().getInt(ARG_LEVEL);
            mRewardsChances = getArguments().getIntArray(ARG_REWARDS_CHANCES);
            if(mRewardsChances != null && mLevel != 0){
                for(int chance : mRewardsChances){
                    if(100 * Math.random() < chance) mRewardsGrantedCount += 1;
                }
            }else{
                Log.e(TAG, "onCreate::did not get infos to create rewards!!");
            }
        }else{
            Log.e(TAG, "onCreate::did not get infos to create rewards!!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_reward_obtained_or_lost, container, false);
        showLoadingRewardsMessage(true);
        showEmptyListMessageOrNot(false);
        //
        mRewardsListRecyclerView = mRootView.findViewById(R.id.rewards_obtained_or_lost_list_recyclerview);
        mRewardsListAdapter = new RewardsObtainedListAdapter(getActivity());
        mRewardsListRecyclerView.setAdapter(mRewardsListAdapter);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRewardsListRecyclerView.setHasFixedSize(true);
        mRewardsListRecyclerView.setLayoutManager(recyclerLayoutManager);
        //
        mPickedRewards = new ArrayList<>();
        getAllRewardsOfSPecificLevelNotActiveOrEscaped(mLevel);
        //
        Button positiveButton = mRootView.findViewById(R.id.reward_obtained_or_lost_ok_btn);
        positiveButton.setOnClickListener(onPositiveClickListener);
        return mRootView;
    }

    private void getAllRewardsOfSPecificLevelNotActiveOrEscaped(int levelDesired){
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.getAllRewardsOfSPecificLevelNotActiveOrEscaped(levelDesired).observe(this, new Observer<List<Reward>>() {
            @Override
            public void onChanged(@Nullable final List<Reward> rewards) {
                pickRewards(rewards);
            }
        });
    }

    private void pickRewards(List<Reward> availableRewards){
        //pick rewards
        boolean missingRewards = false;
        int startPosition = mRewardsPickedCount;
        for(int i = startPosition; i < mRewardsGrantedCount; i++) {
            if(availableRewards.size() > 0) {
                int randomIndex = (int) (Math.random() * (availableRewards.size()));
                mPickedRewards.add(availableRewards.get(randomIndex));
                availableRewards.remove(randomIndex);
                mRewardsPickedCount += 1;
            }else{
                missingRewards = true;
                Log.d(TAG, "pickRewards:: no more rewards of level " + mLevel + " available!!");
                break;
            }
        }
        if(missingRewards){
            if(mLevel < 5) {//we have no more rewards of required level available, so we start picking on the level above
                mLevel += 1;
                getAllRewardsOfSPecificLevelNotActiveOrEscaped(mLevel);
            }else{
                Log.d(TAG, "pickRewards:: all rewards have been collected!!");
            }
        }else {
            finalizeRewardsAttribution();
        }
    }

    private void finalizeRewardsAttribution(){
        //change obtained reward status
        for(Reward rewardObtained : mPickedRewards){
            rewardObtained.setRewardActiveOrNot(Reward.STATUS_ACTIVE);
            rewardObtained.setRewardEscapedOrNot(Reward.STATUS_NOT_ESCAPED);
            long now = Calendar.getInstance().getTimeInMillis();
            rewardObtained.setRewardAcquisitionDate(now);
        }
        //
        showEmptyListMessageOrNot(mPickedRewards.size() == 0);
        showLoadingRewardsMessage(false);
        TextView title = mRootView.findViewById(R.id.reward_obtained_or_lost_title_txtvw);
        title.setText(String.format(getString(R.string.rewards_obtained_title), mPickedRewards.size()));
        title.setVisibility(View.VISIBLE);
        mRewardsListAdapter.setRewards(mPickedRewards);
    }

////////////////////////////////////////
//hiding or showing "loading rewards message"
    private void showLoadingRewardsMessage(boolean showIt){
        TextView loadingRewards = mRootView.findViewById(R.id.loading_rewards_obtained_or_lost_list_message);
        if(showIt){
            loadingRewards.setVisibility(View.VISIBLE);
        }else{
            loadingRewards.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////
//hiding or showing "no rewards message"
    private void showEmptyListMessageOrNot(boolean showIt){
        TextView emptyListMessage = mRootView.findViewById(R.id.empty_rewards_obtained_or_lost_list_message);
        if(showIt){
            emptyListMessage.setVisibility(View.VISIBLE);
        }else{
            emptyListMessage.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////
// UI handling : onPositive, and back clicked
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            exitAndTransmitRewardsForRecording();
        }
    };

    public void onBackNavClicked(){
        exitAndTransmitRewardsForRecording();
    }

    private void exitAndTransmitRewardsForRecording(){
        mListener.onValidateFragmentRewardObtained(mPickedRewards);
    }
////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity )
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentRewardObtainedListener) {
            mListener = (FragmentRewardObtainedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentRewardObtainedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

////////////////////////////////////////
//Listener interface
    public interface FragmentRewardObtainedListener {
        void onValidateFragmentRewardObtained(List<Reward> rewards);
    }

}
