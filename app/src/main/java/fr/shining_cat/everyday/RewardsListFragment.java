package fr.shining_cat.everyday;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fr.shining_cat.everyday.data.EveryDayRewardsDataRepository;
import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.widgets.ListFilterToggleButton;

public class RewardsListFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_REWARDS_LIST_FRAGMENT_TAG = "view_rewards_list_Fragment-tag";

    private final static String SORT_FIELD_DATE = "sorting rewards list on DATE field";
    private final static String SORT_FIELD_LEVEL = "sorting rewards list on LEVEL field";
    private final static String SORT_FIELD_DETAINED = "sorting rewards list on ACTIVE status";
    private final static String SORT_DIRECTION_ASC = "sorting rewards list ASCENDANT";
    private final static String SORT_DIRECTION_DESC = "sorting rewards list DESCENDANT";
    private final static String SORT_OWNED = "sorting rewards list ACTIVE";
    private final static String SORT_LOST = "sorting rewards list INACTIVE";


    private View mRootView;
    private RecyclerView mRewardsListRecyclerView;
    private RewardsCollectionListAdapter mRewardsCollectionListAdapter;
    private ListFilterToggleButton mDateFilterToggleBtn;
    private ListFilterToggleButton mLevelFilterToggleBtn;
    private ListFilterToggleButton mActiveStatusFilterToggleBtn;

    private String mCurrentFilterAndSortField;
    private String mCurrentFilterAndSortParam;



////////////////////////////////////////
//This is the fragment used to hold a RecyclerView (with RewardsCollectionListAdapter as adapter) presenting each reward card in a scrollable vertical grid
    public RewardsListFragment() {
        // Required empty public constructor
    }

    public static RewardsListFragment newInstance() {
        return new RewardsListFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_rewards_list, container, false);
        showLoadingRewardsMessage(true);
        showEmptyListMessageOrNot(false);

        mRewardsListRecyclerView = mRootView.findViewById(R.id.rewards_list_recyclerview);
        mRewardsCollectionListAdapter = new RewardsCollectionListAdapter(getActivity(), (RewardsCollectionListAdapter.RewardsListAdapterListener) getActivity());
        mRewardsListRecyclerView.setAdapter(mRewardsCollectionListAdapter);
        GridLayoutManager recyclerLayoutManager = new GridLayoutManager(getActivity(), Integer.parseInt(getString(R.string.default_rewards_grid_span)));
        mRewardsListRecyclerView.setHasFixedSize(true);
        mRewardsListRecyclerView.setLayoutManager(recyclerLayoutManager);
        //
        mDateFilterToggleBtn = mRootView.findViewById(R.id.rewards_list_date_toggle_filter);
        mDateFilterToggleBtn.setLabel(getString(R.string.date));
        mDateFilterToggleBtn.setModeCheckOrArrow(ListFilterToggleButton.ARROW_MODE);
        mDateFilterToggleBtn.setOnClickListener(mOnDateFilterToggleClickListener);
        //
        mLevelFilterToggleBtn = mRootView.findViewById(R.id.rewards_list_level_toggle_filter);
        mLevelFilterToggleBtn.setLabel(getString(R.string.level));
        mLevelFilterToggleBtn.setOnClickListener(mOnLevelFilterToggleClickListener);
        mLevelFilterToggleBtn.setModeCheckOrArrow(ListFilterToggleButton.ARROW_MODE);
        //
        mActiveStatusFilterToggleBtn = mRootView.findViewById(R.id.rewards_list_active_status_toggle_filter);
        mActiveStatusFilterToggleBtn.setLabel(getString(R.string.active_status_filter_label));
        mActiveStatusFilterToggleBtn.setOnClickListener(mOnActiveStatusFilterToggleClickListener);
        mActiveStatusFilterToggleBtn.setModeCheckOrArrow(ListFilterToggleButton.CHECK_MODE);
        //
        //default rewards list filter and sorting is on date descendant
        mDateFilterToggleBtn.setActive(ListFilterToggleButton.OPTION_DOWN);
        filterAndSortRewardsList(SORT_FIELD_DATE, SORT_DIRECTION_DESC);
        setOtherFiltersInactive(mDateFilterToggleBtn);
        //get rewards general stats
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        for(int i = 1; i <= Critter.NUMBER_OF_REWARDS_LEVELS; i++){
            rewardViewModel.getNumberOfPossibleRewardsForLevel(i, everyDayRewardsRepoListener);
            rewardViewModel.getNumberOfActiveNotEscapedRewardsForLevel(i, everyDayRewardsRepoListener);
            rewardViewModel.getNumberOfEscapedRewardsForLevel(i, everyDayRewardsRepoListener);
        }
        //
        return mRootView;
    }

////////////////////////////////////////
//Display count
    EveryDayRewardsDataRepository.EveryDayRewardsRepoListener everyDayRewardsRepoListener = new EveryDayRewardsDataRepository.EveryDayRewardsRepoListener() {
        @Override
        public void onGetNumberOfPossibleRewardsForLevelComplete(int levelQueried, Integer result) {
            TextView valuePossibleRewardsLevel1 = mRootView.findViewById(R.id.level1_possible_value);
            TextView valuePossibleRewardsLevel2 = mRootView.findViewById(R.id.level2_possible_value);
            TextView valuePossibleRewardsLevel3 = mRootView.findViewById(R.id.level3_possible_value);
            TextView valuePossibleRewardsLevel4 = mRootView.findViewById(R.id.level4_possible_value);
            TextView valuePossibleRewardsLevel5 = mRootView.findViewById(R.id.level5_possible_value);
            switch (levelQueried){
                case Critter.REWARD_LEVEL_1:
                    valuePossibleRewardsLevel1.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_2:
                    valuePossibleRewardsLevel2.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_3:
                    valuePossibleRewardsLevel3.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_4:
                    valuePossibleRewardsLevel4.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_5:
                    valuePossibleRewardsLevel5.setText(String.valueOf(result));
                    break;
                default:
                    Log.e(TAG, "onGetNumberOfPossibleRewardsForLevelComplete::unhandled level value : " + levelQueried + " !!");
            }
        }

        @Override
        public void onGetNumberOfActiveRewardsForLevelComplete(int levelQueried, Integer result) {
            TextView valueObtainedRewardsLevel1 = mRootView.findViewById(R.id.level1_obtained_value);
            TextView valueObtainedRewardsLevel2 = mRootView.findViewById(R.id.level2_obtained_value);
            TextView valueObtainedRewardsLevel3 = mRootView.findViewById(R.id.level3_obtained_value);
            TextView valueObtainedRewardsLevel4 = mRootView.findViewById(R.id.level4_obtained_value);
            TextView valueObtainedRewardsLevel5 = mRootView.findViewById(R.id.level5_obtained_value);
            switch (levelQueried){
                case Critter.REWARD_LEVEL_1:
                    valueObtainedRewardsLevel1.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_2:
                    valueObtainedRewardsLevel2.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_3:
                    valueObtainedRewardsLevel3.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_4:
                    valueObtainedRewardsLevel4.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_5:
                    valueObtainedRewardsLevel5.setText(String.valueOf(result));
                    break;
                default:
                    Log.e(TAG, "onGetNumberOfActiveRewardsForLevelComplete::unhandled level value : " + levelQueried + " !!");
            }
        }

        @Override
        public void onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete(int levelQueried, Integer result) {
            TextView valueLostRewardsLevel1 = mRootView.findViewById(R.id.level1_lost_value);
            TextView valueLostRewardsLevel2 = mRootView.findViewById(R.id.level2_lost_value);
            TextView valueLostRewardsLevel3 = mRootView.findViewById(R.id.level3_lost_value);
            TextView valueLostRewardsLevel4 = mRootView.findViewById(R.id.level4_lost_value);
            TextView valueLostRewardsLevel5 = mRootView.findViewById(R.id.level5_lost_value);
            switch (levelQueried){
                case Critter.REWARD_LEVEL_1:
                    valueLostRewardsLevel1.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_2:
                    valueLostRewardsLevel2.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_3:
                    valueLostRewardsLevel3.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_4:
                    valueLostRewardsLevel4.setText(String.valueOf(result));
                    break;
                case Critter.REWARD_LEVEL_5:
                    valueLostRewardsLevel5.setText(String.valueOf(result));
                    break;
                default:
                    Log.e(TAG, "onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete::unhandled level value : " + levelQueried + " !!");
            }
        }
        @Override
        public void onGetNumberOfRowsComplete(Integer result) {}
        @Override
        public void onInsertOneRewardComplete(long result) {}
        @Override
        public void onInsertMultipleRewardsComplete(Long[] result) {}
        @Override
        public void onUpdateOneRewardComplete(int result) {}
        @Override
        public void onUpdateMultipleRewardComplete(int result) {}
        @Override
        public void ondeleteOneRewardComplete(int result) {}
        @Override
        public void ondeleteAllRewardComplete(int result) {}
    };
////////////////////////////////////////
//FILTERING
    private View.OnClickListener mOnDateFilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mDateFilterToggleBtn.isActive()){
                String dateFilterDirection = (mDateFilterToggleBtn.toggle().equals(ListFilterToggleButton.OPTION_UP)) ? SORT_DIRECTION_ASC : SORT_DIRECTION_DESC;
                filterAndSortRewardsList(SORT_FIELD_DATE, dateFilterDirection);
            }else{
                mDateFilterToggleBtn.setActive(ListFilterToggleButton.OPTION_UP);
                filterAndSortRewardsList(SORT_FIELD_DATE, SORT_DIRECTION_ASC);
                setOtherFiltersInactive(mDateFilterToggleBtn);
            }
        }
    };
    private View.OnClickListener mOnLevelFilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mLevelFilterToggleBtn.isActive()){
                String levelFilterDirection = (mLevelFilterToggleBtn.toggle().equals(ListFilterToggleButton.OPTION_UP)) ? SORT_DIRECTION_ASC : SORT_DIRECTION_DESC;
                filterAndSortRewardsList(SORT_FIELD_LEVEL, levelFilterDirection);
            }else{
                mLevelFilterToggleBtn.setActive(ListFilterToggleButton.OPTION_UP);
                filterAndSortRewardsList(SORT_FIELD_LEVEL, SORT_DIRECTION_ASC);
                setOtherFiltersInactive(mLevelFilterToggleBtn);
            }
        }
    };
    private View.OnClickListener mOnActiveStatusFilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mActiveStatusFilterToggleBtn.isActive()){
                String activeStatusFilterParam = (mActiveStatusFilterToggleBtn.toggle().equals(ListFilterToggleButton.OPTION_YES)) ? SORT_OWNED : SORT_LOST;
                filterAndSortRewardsList(SORT_FIELD_DETAINED, activeStatusFilterParam);
            }else{
                mActiveStatusFilterToggleBtn.setActive(ListFilterToggleButton.OPTION_YES);
                filterAndSortRewardsList(SORT_FIELD_DETAINED, SORT_OWNED);
                setOtherFiltersInactive(mActiveStatusFilterToggleBtn);
            }
        }
    };

    private void setOtherFiltersInactive(ListFilterToggleButton onlyActiveToggle){
        if(onlyActiveToggle != mDateFilterToggleBtn) mDateFilterToggleBtn.setInactive();
        if(onlyActiveToggle != mLevelFilterToggleBtn) mLevelFilterToggleBtn.setInactive();
        if(onlyActiveToggle != mActiveStatusFilterToggleBtn) mActiveStatusFilterToggleBtn.setInactive();
    }


    private void filterAndSortRewardsList(String filterAndSortField, String filterAndSortParam){
        Log.d(TAG, "filterAndSortRewardsList");
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        mCurrentFilterAndSortField = filterAndSortField;
        mCurrentFilterAndSortParam = filterAndSortParam;
        //
        switch (filterAndSortField){
            case SORT_FIELD_LEVEL:
                switch (filterAndSortParam) {
                    case SORT_DIRECTION_DESC:
                        rewardViewModel.getAllRewardsActiveLevelDesc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_LEVEL) && mCurrentFilterAndSortParam.equals(SORT_DIRECTION_DESC)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                    case SORT_DIRECTION_ASC:
                    default:
                        rewardViewModel.getAllRewardsActiveLevelAsc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_LEVEL) && mCurrentFilterAndSortParam.equals(SORT_DIRECTION_ASC)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                }
                break;
            case SORT_FIELD_DETAINED:
                switch (filterAndSortParam) {
                    case SORT_LOST:
                        rewardViewModel.getAllRewardsEscapedAcquisitionDateDesc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_DETAINED) && mCurrentFilterAndSortParam.equals(SORT_LOST)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                    case SORT_OWNED:
                    default:
                        rewardViewModel.getAllRewardsNotEscapedAcquisitionDateDesc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_DETAINED) && mCurrentFilterAndSortParam.equals(SORT_OWNED)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                }
                break;
            case SORT_FIELD_DATE:
            default:
                switch (filterAndSortParam) {
                    case SORT_DIRECTION_ASC:
                        rewardViewModel.getAllRewardsActiveAcquisitionDateAsc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_DATE) && mCurrentFilterAndSortParam.equals(SORT_DIRECTION_ASC)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                    case SORT_DIRECTION_DESC:
                    default:
                        rewardViewModel.getAllRewardsActiveAcquisitionDateDesc().observe(this, new Observer<List<Reward>>() {
                            @Override
                            public void onChanged(@Nullable final List<Reward> rewards) {
                                if (mCurrentFilterAndSortField.equals(SORT_FIELD_DATE) && mCurrentFilterAndSortParam.equals(SORT_DIRECTION_DESC)){
                                    updateDisplayedDatas(rewards);
                                }
                            }
                        });
                        break;
                }
                break;
        }

    }


    private void updateDisplayedDatas(List<Reward> rewards){
        // Update the cached copy of the rewards in the adapter.
        Log.d(TAG, "updateDisplayedDatas");
        mRewardsCollectionListAdapter.setRewards(rewards);
        mRewardsListRecyclerView.getLayoutManager().scrollToPosition(0);
        showLoadingRewardsMessage(false);
        if(rewards!=null) {
            showEmptyListMessageOrNot(rewards.size() < 1);
        }else{
            showEmptyListMessageOrNot(true);
        }
    }

////////////////////////////////////////
//hiding or showing "loading rewards message"
    private void showLoadingRewardsMessage(boolean showIt){
        TextView loadingRewards = mRootView.findViewById(R.id.loading_rewards_list_message);
        if(showIt){
            loadingRewards.setVisibility(View.VISIBLE);
        }else{
            loadingRewards.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////
//hiding or showing "no rewards message"
    private void showEmptyListMessageOrNot(boolean showIt){
        TextView emptyListMessage = mRootView.findViewById(R.id.empty_rewards_list_message);
        if(showIt){
            emptyListMessage.setVisibility(View.VISIBLE);
        }else{
            emptyListMessage.setVisibility(View.GONE);
        }
    }


}
