package fr.shining_cat.everyday.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

////////////////////////////////////////
//DAO interface for Reward entities data storage operations
@Dao
public interface RewardDAO {
    @Insert
    long insert(Reward reward);

    @Insert
    Long[] insertMultiple(Reward... rewards);

    @Update
    int updateReward(Reward reward);

    @Update
    int updateMultipleReward(Reward... rewards);

    @Delete
    int deleteReward(Reward reward);

    @Query("DELETE FROM rewards_table")
    int deleteAllRewards();

    //ROOM does not allow parameters for the ORDER BY clause to prevent injection so we need a proxy for each WHERE clause used

    //here we query for all "active" rewards, ie all rewards that have at one point been obtained, regardless if they have been lost or not
    //sort on acquisitionDate ASC
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 ORDER BY acquisitionDate ASC")
    LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateAsc();
    //sort on acquisitionDate DESC
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 ORDER BY acquisitionDate DESC")
    LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateDesc();
    //sort on rewardLevel ASC
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 ORDER BY rewardLevel ASC")
    LiveData<List<Reward>> getAllRewardsActiveLevelAsc();
    //sort on rewardLevel DESC
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 ORDER BY rewardLevel DESC")
    LiveData<List<Reward>> getAllRewardsActiveLevelDesc();

    ///here we query for ACTIVE AND NOT LOST rewards :
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 AND escapedOrNot == 0 ORDER BY acquisitionDate DESC")
    LiveData<List<Reward>> getAllRewardsNotEscapedAcquisitionDatDesc();
    //here we query for ACTIVE AND LOST rewards :
    @Query("SELECT * from rewards_table WHERE activeOrNot == 1 AND escapedOrNot == 1 ORDER BY escapingDate DESC")
    LiveData<List<Reward>> getAllRewardsEscapedAcquisitionDateDesc();

    //here we query for NON ACTIVE rewards for specific LEVEL:
    @Query("SELECT * from rewards_table WHERE rewardLevel == :level AND activeOrNot == 0")
    LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActive(int level);

    //here we query for NON ACTIVE or ACTIVE AND ESCAPED rewards for specific LEVEL:
    @Query("SELECT * from rewards_table WHERE rewardLevel == :level AND (activeOrNot == 0 OR escapedOrNot == 1)")
    LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActiveOrEscaped(int level);

    //just count entries in table (this is used to determine if possible rewards have been generated already or not)
    @Query("SELECT COUNT(id) FROM rewards_table")
    int getNumberOfRows();

    //COUNTS :
    @Query("SELECT COUNT(id) FROM rewards_table WHERE rewardLevel == :level ")
    int getNumberOfPossibleRewardsForLevel(int level);
    @Query("SELECT COUNT(id) FROM rewards_table WHERE rewardLevel == :level AND activeOrNot == 1 AND escapedOrNot == 0")
    int getNumberOfActiveNotEscapedRewardsForLevel(int level);
    @Query("SELECT COUNT(id) FROM rewards_table WHERE rewardLevel == :level AND activeOrNot == 1 AND escapedOrNot == 1")
    int getNumberOfEscapedRewardsForLevel(int level);

    //We will probably never query all the rewards
    @Query("SELECT * from rewards_table ORDER BY acquisitionDate ASC")
    LiveData<List<Reward>> getAllRewardsAcquisitionDateAsc();
    //We will probably never query the inactive rewards (created but not used in app yet)
    @Query("SELECT * from rewards_table WHERE activeOrNot == 0")
    LiveData<List<Reward>> getAllRewardsInactive();

}
