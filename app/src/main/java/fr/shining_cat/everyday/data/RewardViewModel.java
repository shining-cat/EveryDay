package fr.shining_cat.everyday.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

public class RewardViewModel extends AndroidViewModel {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private EveryDayRewardsDataRepository mEveryDayRewardsDataRepository;

    private LiveData<List<Reward>> mAllRewardsActiveAcquisitionDateAsc;
    private LiveData<List<Reward>> mAllRewardsActiveAcquisitionDateDesc;
    private LiveData<List<Reward>> mAllRewardsActiveLevelAsc;
    private LiveData<List<Reward>> mAllRewardsActiveLevelDesc;
    private LiveData<List<Reward>> mAllRewardsNotEscapedAcquisitionDateAsc;
    private LiveData<List<Reward>> mAllRewardsEscapedAcquisitionDateDesc;

////////////////////////////////////////
//AndroidViewModel for SessionRecords entities data storage operations with some commodity methods
    public RewardViewModel(Application application){
        super(application);
        mEveryDayRewardsDataRepository = new EveryDayRewardsDataRepository(application);
    }

////////////////////////////////////////
//GET ALL ACTIVE LIVE
    public LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateAsc() {
        if(mAllRewardsActiveAcquisitionDateAsc == null){
            mAllRewardsActiveAcquisitionDateAsc = mEveryDayRewardsDataRepository.getAllRewardsActiveAcquisitionDateAsc();
        }
        return mAllRewardsActiveAcquisitionDateAsc;
    }
    public LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateDesc() {
        if(mAllRewardsActiveAcquisitionDateDesc == null){
            mAllRewardsActiveAcquisitionDateDesc = mEveryDayRewardsDataRepository.getAllRewardsActiveAcquisitionDateDesc();
        }
        return mAllRewardsActiveAcquisitionDateDesc;
    }
    public LiveData<List<Reward>> getAllRewardsActiveLevelAsc() {
        if(mAllRewardsActiveLevelAsc == null){
            mAllRewardsActiveLevelAsc = mEveryDayRewardsDataRepository.getAllRewardsActiveLevelAsc();
        }
        return mAllRewardsActiveLevelAsc;
    }
    public LiveData<List<Reward>> getAllRewardsActiveLevelDesc() {
        if(mAllRewardsActiveLevelDesc == null){
            mAllRewardsActiveLevelDesc = mEveryDayRewardsDataRepository.getAllRewardsActiveLevelDesc();
        }
        return mAllRewardsActiveLevelDesc;
    }

////////////////////////////////////////
//GET ONLY LOST / NOT LOST rewards
    public LiveData<List<Reward>> getAllRewardsNotEscapedAcquisitionDateDesc() {
        if(mAllRewardsNotEscapedAcquisitionDateAsc == null){
            mAllRewardsNotEscapedAcquisitionDateAsc = mEveryDayRewardsDataRepository.getAllRewardsNotEscapedAcquisitionDateDesc();
        }
        return mAllRewardsNotEscapedAcquisitionDateAsc;
    }
    public LiveData<List<Reward>> getAllRewardsEscapedAcquisitionDateDesc() {
        if(mAllRewardsEscapedAcquisitionDateDesc == null){
            mAllRewardsEscapedAcquisitionDateDesc = mEveryDayRewardsDataRepository.getAllRewardsEscapedAcquisitionDateDesc();
        }
        return mAllRewardsEscapedAcquisitionDateDesc;
    }

////////////////////////////////////////
//GET ONLY NON ACTIVE REWARDS FOR A SPECIFIC LEVEL (new reward attribution)
    public LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActive(int level) {
        //no caching here
        return mEveryDayRewardsDataRepository.getAllRewardsOfSPecificLevelNotActive(level);
    }

////////////////////////////////////////
//GET ONLY NON ACTIVE or ESCAPED REWARDS FOR A SPECIFIC LEVEL (new reward attribution including re-attribution of escaped ones)
    public LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActiveOrEscaped(int level) {
        //no caching here
        return mEveryDayRewardsDataRepository.getAllRewardsOfSPecificLevelNotActiveOrEscaped(level);
    }

////////////////////////////////////////
//GET number of rows in rewards table (this is used to determine if possible rewards have been generated already or not)
   public void getNumberOfRows(EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener) {
        //no caching here
        mEveryDayRewardsDataRepository.getNumberOfRows(listener);
   }

////////////////////////////////////////
//GET number of possible rewards for a specific level
   public void getNumberOfPossibleRewardsForLevel(int level, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener) {
        //no caching here
        mEveryDayRewardsDataRepository.getNumberOfPossibleRewardsForLevel(level, listener);
   }

////////////////////////////////////////
//GET number of obtained rewards for a specific level
    public void getNumberOfActiveNotEscapedRewardsForLevel(int level, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener) {
        //no caching here
        mEveryDayRewardsDataRepository.getNumberOfActiveNotEscapedRewardsForLevel(level, listener);
    }

////////////////////////////////////////
//GET number of escaped rewards for a specific level
    public void getNumberOfEscapedRewardsForLevel(int level, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener) {
        //no caching here
        mEveryDayRewardsDataRepository.getNumberOfEscapedRewardsForLevel(level, listener);
    }

////////////////////////////////////////
//INSERT ONE
    public void insert (Reward reward, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        mEveryDayRewardsDataRepository.insertReward(reward, listener);
    }

////////////////////////////////////////
//INSERT MULTIPLE
    public void insertMultiple (List<Reward> rewardsList, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        Reward[] rewardsArray = rewardsList.toArray(new Reward[rewardsList.size()]);
        mEveryDayRewardsDataRepository.insertMultipleRewards(rewardsArray, listener);
    }

////////////////////////////////////////
//UPDATE ONE
    public void update (Reward reward, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        mEveryDayRewardsDataRepository.updateReward(reward, listener);
    }

////////////////////////////////////////
//UPDATE ONE
    public void updateMultipleReward (List<Reward> rewardsList, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        Reward[] rewardsArray = rewardsList.toArray(new Reward[rewardsList.size()]);
        mEveryDayRewardsDataRepository.updateMultipleReward(rewardsArray, listener);
    }

////////////////////////////////////////
//DELETE ONE
    public void deleteOneReward (Reward reward, EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        mEveryDayRewardsDataRepository.deleteReward(reward, listener);
    }

////////////////////////////////////////
//DELETE ALL
    public void deleteAllRewards (EveryDayRewardsDataRepository.EveryDayRewardsRepoListener listener){
        mEveryDayRewardsDataRepository.deleteAllRewards(listener);
    }

}
