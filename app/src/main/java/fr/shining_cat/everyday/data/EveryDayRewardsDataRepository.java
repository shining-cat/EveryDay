package fr.shining_cat.everyday.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;


////////////////////////////////////////
//Repository for Reward datas
//with an interface to dispatch callbacks
public class EveryDayRewardsDataRepository {

    private RewardDAO mRewardDAO;
    private LiveData<List<Reward>> mAllRewardsActiveAcquisitionDateAsc;
    private LiveData<List<Reward>> mAllRewardsActiveAcquisitionDateDesc;
    private LiveData<List<Reward>> mAllRewardsActiveLevelAsc;
    private LiveData<List<Reward>> mAllRewardsActiveLevelDesc;
    private LiveData<List<Reward>> mAllRewardsNotEscapedAcquisitionDateAsc;
    private LiveData<List<Reward>> mAllRewardsEscapedAcquisitionDateDesc;

    EveryDayRewardsDataRepository(Application application) {
        EveryDayRoomDatabase db = EveryDayRoomDatabase.getDatabase(application);
        mRewardDAO = db.rewardDao();
    }


////////////////////////////////////////
//GET ALL LIVE
    LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateAsc() {
        if(mAllRewardsActiveAcquisitionDateAsc == null){
            mAllRewardsActiveAcquisitionDateAsc = mRewardDAO.getAllRewardsActiveAcquisitionDateAsc();
        }
        return mAllRewardsActiveAcquisitionDateAsc;
    }
    LiveData<List<Reward>> getAllRewardsActiveAcquisitionDateDesc() {
        if(mAllRewardsActiveAcquisitionDateDesc == null){
            mAllRewardsActiveAcquisitionDateDesc = mRewardDAO.getAllRewardsActiveAcquisitionDateDesc();
        }
        return mAllRewardsActiveAcquisitionDateDesc;
    }
    LiveData<List<Reward>> getAllRewardsActiveLevelAsc() {
        if(mAllRewardsActiveLevelAsc == null){
            mAllRewardsActiveLevelAsc = mRewardDAO.getAllRewardsActiveLevelAsc();
        }
        return mAllRewardsActiveLevelAsc;
    }
    LiveData<List<Reward>> getAllRewardsActiveLevelDesc() {
        if(mAllRewardsActiveLevelDesc == null){
            mAllRewardsActiveLevelDesc = mRewardDAO.getAllRewardsActiveLevelDesc();
        }
        return mAllRewardsActiveLevelDesc;
    }

////////////////////////////////////////
//GET ONLY LOST / NOT LOST rewards
    LiveData<List<Reward>> getAllRewardsNotEscapedAcquisitionDateDesc() {
        if(mAllRewardsNotEscapedAcquisitionDateAsc == null){
            mAllRewardsNotEscapedAcquisitionDateAsc = mRewardDAO.getAllRewardsNotEscapedAcquisitionDatDesc();
        }
        return mAllRewardsNotEscapedAcquisitionDateAsc;
    }
    LiveData<List<Reward>> getAllRewardsEscapedAcquisitionDateDesc() {
        if(mAllRewardsEscapedAcquisitionDateDesc == null){
            mAllRewardsEscapedAcquisitionDateDesc = mRewardDAO.getAllRewardsEscapedAcquisitionDateDesc();
        }
        return mAllRewardsEscapedAcquisitionDateDesc;
    }

////////////////////////////////////////
//GET ONLY NON ACTIVE REWARDS FOR A SPECIFIC LEVEL (new reward attribution)
    LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActive(int level) {
        //no caching here
        return mRewardDAO.getAllRewardsOfSPecificLevelNotActive(level);
    }

////////////////////////////////////////
//GET ONLY NON ACTIVE or ESCAPED REWARDS FOR A SPECIFIC LEVEL (new reward attribution including re-attribution of escaped ones)
    LiveData<List<Reward>> getAllRewardsOfSPecificLevelNotActiveOrEscaped(int level) {
        //no caching here
        return mRewardDAO.getAllRewardsOfSPecificLevelNotActiveOrEscaped(level);
    }

////////////////////////////////////////
//GET number of rows in rewards table (this is used to determine if possible rewards have been generated already or not)
    void getNumberOfRows(EveryDayRewardsRepoListener listener) {
        //no caching here
        new GetNumberOfRowsAsyncTask(mRewardDAO, listener).execute();
    }

    private static class GetNumberOfRowsAsyncTask extends AsyncTask<Void, Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        GetNumberOfRowsAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(Void... params) {
            return mAsyncTaskDao.getNumberOfRows();
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onGetNumberOfRowsComplete(result);
        }
    }

////////////////////////////////////////
//GET number of possible rewards for a specific level
    void getNumberOfPossibleRewardsForLevel(int level, EveryDayRewardsRepoListener listener) {
        //no caching here
        new GetNumberOfPossibleRewardsForLevelAsyncTask(mRewardDAO, listener, level).execute(level);
    }

    private static class GetNumberOfPossibleRewardsForLevelAsyncTask extends AsyncTask<Integer , Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;
        private int mLevelQueried;

        GetNumberOfPossibleRewardsForLevelAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener, int level){
            mAsyncTaskDao = dao;
            mListener = listener;
            mLevelQueried = level;
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            return mAsyncTaskDao.getNumberOfPossibleRewardsForLevel(params[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onGetNumberOfPossibleRewardsForLevelComplete(mLevelQueried, result);
        }
    }

////////////////////////////////////////
//GET number of obtained rewards for a specific level
    void getNumberOfActiveNotEscapedRewardsForLevel(int level, EveryDayRewardsRepoListener listener) {
        //no caching here
        new GetNumberOfActiveRewardsForLevelAsyncTask(mRewardDAO, listener, level).execute(level);
    }

    private static class GetNumberOfActiveRewardsForLevelAsyncTask extends AsyncTask<Integer , Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;
        private int mLevelQueried;

        GetNumberOfActiveRewardsForLevelAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener, int level){
            mAsyncTaskDao = dao;
            mListener = listener;
            mLevelQueried = level;
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            return mAsyncTaskDao.getNumberOfActiveNotEscapedRewardsForLevel(params[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onGetNumberOfActiveRewardsForLevelComplete(mLevelQueried, result);
        }
    }

////////////////////////////////////////
//GET number of escaped rewards for a specific level
    void getNumberOfEscapedRewardsForLevel(int level, EveryDayRewardsRepoListener listener) {
        //no caching here
        new GetNumberOfEscapedRewardsForLevelAsyncTask(mRewardDAO, listener, level).execute(level);
    }

    private static class GetNumberOfEscapedRewardsForLevelAsyncTask extends AsyncTask<Integer , Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;
        private int mLevelQueried;

        GetNumberOfEscapedRewardsForLevelAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener, int level){
            mAsyncTaskDao = dao;
            mListener = listener;
            mLevelQueried = level;
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            return mAsyncTaskDao.getNumberOfEscapedRewardsForLevel(params[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete(mLevelQueried, result);
        }
    }


////////////////////////////////////////
//INSERT ONE
    void insertReward (Reward reward, EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new InsertRewardAsyncTask(mRewardDAO, listener).execute(reward);
    }

    private static class InsertRewardAsyncTask extends AsyncTask<Reward, Void, Long> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        InsertRewardAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Long doInBackground(final Reward... params) {
            return mAsyncTaskDao.insert(params[0]);
        }
        @Override
        protected void onPostExecute(Long result) {
            //super.onPostExecute(result);
            mListener.onInsertOneRewardComplete(result);
        }
    }

////////////////////////////////////////
//INSERT MULTIPLE
    void insertMultipleRewards (Reward[] rewards, EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new InsertMultipleRewardAsyncTask(mRewardDAO, listener).execute(rewards);
    }

    private static class InsertMultipleRewardAsyncTask extends AsyncTask<Reward, Integer, Long[]> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        InsertMultipleRewardAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Long[] doInBackground(final Reward... params) {
            return mAsyncTaskDao.insertMultiple(params);
        }

        @Override
        protected void onPostExecute(Long[] result) {
            //super.onPostExecute(result);
            mListener.onInsertMultipleRewardsComplete(result);
        }
    }

////////////////////////////////////////
//UPDATE ONE
    void updateReward (Reward reward, EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new UpdateRewardAsyncTask(mRewardDAO, listener).execute(reward);
    }

    private static class UpdateRewardAsyncTask extends AsyncTask<Reward, Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        UpdateRewardAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final Reward... params) {
            return mAsyncTaskDao.updateReward(params[0]);
        }
        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onUpdateOneRewardComplete(result);
        }
    }

////////////////////////////////////////
//UPDATE MULTIPLE
    void updateMultipleReward (Reward[] rewards, EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new UpdateMultipleRewardAsyncTask(mRewardDAO, listener).execute(rewards);
    }

    private static class UpdateMultipleRewardAsyncTask extends AsyncTask<Reward, Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        UpdateMultipleRewardAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final Reward... params) {
            return mAsyncTaskDao.updateMultipleReward(params);
        }
        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onUpdateMultipleRewardComplete(result);
        }
    }

////////////////////////////////////////
//DELETE ONE
    void deleteReward(Reward reward, EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new DeleteRewardAsyncTask(mRewardDAO, listener).execute(reward);
    }

    private static class DeleteRewardAsyncTask extends AsyncTask<Reward, Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        DeleteRewardAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final Reward... params) {
            return mAsyncTaskDao.deleteReward(params[0]);
        }
        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.ondeleteOneRewardComplete(result);
        }
    }

////////////////////////////////////////
//DELETE ALL
    void deleteAllRewards(EveryDayRewardsRepoListener listener){
        if (!(listener instanceof EveryDayRewardsRepoListener)) {
           throw new RuntimeException(listener.toString()+ " must implement EveryDayRewardsRepoListener");
        }
        new DeleteAllRewardsAsyncTask(mRewardDAO, listener).execute();
    }

    private static class DeleteAllRewardsAsyncTask extends AsyncTask<Void, Void, Integer> {
        private RewardDAO mAsyncTaskDao;
        private EveryDayRewardsRepoListener mListener;

        DeleteAllRewardsAsyncTask(RewardDAO dao, EveryDayRewardsRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final Void... params) {
            return mAsyncTaskDao.deleteAllRewards();
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.ondeleteAllRewardComplete(result);
        }
    }

////////////////////////////////////////
//Listener interface
    public interface EveryDayRewardsRepoListener {
        void onGetNumberOfRowsComplete(Integer result);
        void onGetNumberOfPossibleRewardsForLevelComplete(int levelQueried, Integer result);
        void onGetNumberOfActiveRewardsForLevelComplete(int levelQueried, Integer result);
        void onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete(int levelQueried, Integer result);
        void onInsertOneRewardComplete(long result);
        void onInsertMultipleRewardsComplete(Long[] result);
        void onUpdateOneRewardComplete(int result);
        void onUpdateMultipleRewardComplete(int result);
        //we will probably never delete any reward
        void ondeleteOneRewardComplete(int result);
        void ondeleteAllRewardComplete(int result);
    }


}
