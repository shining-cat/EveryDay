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

public class RewardsEscapedFragment extends Fragment {

    public static final String FRAGMENT_REWARD_ESCAPED_TAG = "fragment_reward_escaped-tag";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final double NUMBER_OF_DAYS_TO_NUMBER_OF_REWARDS_CONVERT_FACTOR = 1.5;

    private static final String ARG_NUMBER_OF_DAYS                  = "number_of_days_int_argument";
    private static final String ARG_NUMBER_OF_DAYS_ALREADY_PUNISHED = "number_of_days_already_punished_int_argument";


    private View mRootView;
    private FragmentRewardsEscapedListener mListener;
    private RecyclerView mRewardsListRecyclerView;
    private RewardsEscapedListAdapter mRewardsListAdapter;
    private int mNumberOfDaysWithoutSession;
    private int mNumberOfDaysAlreadyPunished;
    private List<Reward> mRewardsLost;


    public RewardsEscapedFragment() {
        // Required empty public constructor
    }

    public static RewardsEscapedFragment newInstance(int numberOfDaysWithoutSession, int numberOfDaysAlreadyPunished) {
        Log.d("RewardsObtainedFragment", "newInstance");
        RewardsEscapedFragment fragment = new RewardsEscapedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NUMBER_OF_DAYS, numberOfDaysWithoutSession);
        args.putInt(ARG_NUMBER_OF_DAYS_ALREADY_PUNISHED, numberOfDaysAlreadyPunished);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNumberOfDaysWithoutSession = getArguments().getInt(ARG_NUMBER_OF_DAYS);
            mNumberOfDaysAlreadyPunished = getArguments().getInt(ARG_NUMBER_OF_DAYS_ALREADY_PUNISHED);
        }else{
            Log.e(TAG, "onCreate::did not get infos to escape rewards!!");
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
        mRewardsListAdapter = new RewardsEscapedListAdapter(getActivity());
        mRewardsListRecyclerView.setAdapter(mRewardsListAdapter);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRewardsListRecyclerView.setHasFixedSize(true);
        mRewardsListRecyclerView.setLayoutManager(recyclerLayoutManager);
        //
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.getAllRewardsNotEscapedAcquisitionDateDesc().observe(this, new Observer<List<Reward>>() {
            @Override
            public void onChanged(@Nullable final List<Reward> rewards) {
                escapeRewards(rewards);
            }
        });
        //
        Button positiveButton = mRootView.findViewById(R.id.reward_obtained_or_lost_ok_btn);
        positiveButton.setOnClickListener(onPositiveClickListener);
        return mRootView;
    }

    private void escapeRewards(List<Reward> rewards){
        mRewardsLost = new ArrayList<>();
        //convert number of days without session to number of rewards escaping, but remove those that would have already escaped on a previous computation
        int numberOfEscapedRewards = (int) (Math.pow(mNumberOfDaysWithoutSession, NUMBER_OF_DAYS_TO_NUMBER_OF_REWARDS_CONVERT_FACTOR) - Math.pow(mNumberOfDaysAlreadyPunished, NUMBER_OF_DAYS_TO_NUMBER_OF_REWARDS_CONVERT_FACTOR));
        Log.d(TAG, "escapeRewards::mNumberOfDaysWithoutSession = " + mNumberOfDaysWithoutSession + " / numberOfEscapedRewards = " + numberOfEscapedRewards);
        //pick rewards
        for(int i = 0; i < numberOfEscapedRewards; i++) {
            if(rewards.size() > 0){
                int randomIndex = (int) (Math.random()*(rewards.size()));
                mRewardsLost.add(rewards.get(randomIndex));
                rewards.remove(randomIndex);
            }else{
                Log.d(TAG, "escapeRewards:: all rewards have escaped!!");
                break;
            }
        }
        //change escaped reward status
        for(Reward rewardObtained : mRewardsLost){
            rewardObtained.setRewardEscapedOrNot(Reward.STATUS_ESCAPED);
            long now = Calendar.getInstance().getTimeInMillis();
            rewardObtained.setRewardEscapingDate(now);
        }
        //
        showEmptyListMessageOrNot(mRewardsLost.size() == 0);
        showLoadingRewardsMessage(false);
        TextView title = mRootView.findViewById(R.id.reward_obtained_or_lost_title_txtvw);
        title.setText(String.format(getString(R.string.rewards_lost_title), mNumberOfDaysWithoutSession, mRewardsLost.size()));
        title.setVisibility(View.VISIBLE);
        mRewardsListAdapter.setRewards(mRewardsLost);
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
// UI handling : onPositive, and onCancel
    private View.OnClickListener onPositiveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onPositiveClickListener");
            mListener.onValidateFragmentRewardsEscaped(mRewardsLost);
        }
    };

////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity )
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentRewardsEscapedListener) {
            mListener = (FragmentRewardsEscapedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentRewardsEscapedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

////////////////////////////////////////
//Listener interface
    public interface FragmentRewardsEscapedListener {
        void onValidateFragmentRewardsEscaped(List<Reward> rewards);
    }

}
