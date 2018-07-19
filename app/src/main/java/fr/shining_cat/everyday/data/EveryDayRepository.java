package fr.shining_cat.everyday.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;


////////////////////////////////////////
//Repository for all datas : sessions and animals
//with an interface to dispatch callbacks
public class EveryDayRepository {

    private SessionRecordDAO mSessionRecordDAO;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeAsc;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeDesc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationAsc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationDesc;
    private LiveData<List<SessionRecord>> mSessionsWithMp3;
    private LiveData<List<SessionRecord>> mSessionsWithoutMp3;
    private LiveData<List<SessionRecord>> mSessionsSearch;

    EveryDayRepository(Application application) {
        EveryDayRoomDatabase db = EveryDayRoomDatabase.getDatabase(application);
        mSessionRecordDAO = db.sessionRecordDao();
    }

////////////////////////////////////////
//SESSIONS
////////////////////////////////////////

////////////////////////////////////////
//GET ALL LIVE
    LiveData<List<SessionRecord>> getAllSessionsRecordsStartTimeAsc() {
        if(mAllSessionsStartTimeAsc == null){
            mAllSessionsStartTimeAsc = mSessionRecordDAO.getAllSessionsStartTimeAsc();
        }
        return mAllSessionsStartTimeAsc;
    }
    LiveData<List<SessionRecord>> getAllSessionsRecordsStartTimeDesc() {
        if(mAllSessionsStartTimeDesc == null){
            mAllSessionsStartTimeDesc = mSessionRecordDAO.getAllSessionsStartTimeDesc();
        }
        return mAllSessionsStartTimeDesc;
    }
    LiveData<List<SessionRecord>> getAllSessionsRecordsDurationAsc() {
        if(mAllSessionsDurationAsc == null){
            mAllSessionsDurationAsc = mSessionRecordDAO.getAllSessionsDurationAsc();
        }
        return mAllSessionsDurationAsc;
    }
    LiveData<List<SessionRecord>> getAllSessionsRecordsDurationDesc() {
        if(mAllSessionsDurationDesc == null){
            mAllSessionsDurationDesc = mSessionRecordDAO.getAllSessionsDurationDesc();
        }
        return mAllSessionsDurationDesc;
    }
////////////////////////////////////////
//GET ONLY WITH / WITHOUT MP3
    LiveData<List<SessionRecord>> getAllSessionsRecordsWithMp3() {
        if(mSessionsWithMp3 == null){
            mSessionsWithMp3 = mSessionRecordDAO.getAllSessionsWithMp3();
        }
        return mSessionsWithMp3;
    }
    LiveData<List<SessionRecord>> getAllSessionsRecordsWithoutMp3() {
        if(mSessionsWithoutMp3 == null){
            mSessionsWithoutMp3 = mSessionRecordDAO.getAllSessionsWithoutMp3();
        }
        return mSessionsWithoutMp3;
    }

////////////////////////////////////////
    //SEARCH REQUEST
    LiveData<List<SessionRecord>> getSessionsRecordsSearch(String searchRequest) {
        return mSessionRecordDAO.getSessionsSearch(searchRequest);
    }
////////////////////////////////////////
//GET ALL NOT LIVE
    public void getAllSessionsRecordsNotObservable(EveryDayRepoListener listener) {
        if (!(listener instanceof EveryDayRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new getAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class getAllSessionsAsyncTask extends AsyncTask<Void, Void, List<SessionRecord>> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        getAllSessionsAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected List<SessionRecord> doInBackground(final Void... params) {
            return mAsyncTaskDao.getAllSessionsNotLiveStartTimeAsc();
        }
        @Override
        protected void onPostExecute(List<SessionRecord> result) {
            //super.onPostExecute(result);
            mListener.onGetAllSessionsNotLiveComplete(result);
        }
    }

////////////////////////////////////////
//INSERT ONE
    public void insertSessionRecord (SessionRecord sessionRecord, EveryDayRepoListener listener){
        if (!(listener instanceof EveryDayRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new insertSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class insertSessionAsyncTask extends AsyncTask<SessionRecord, Void, Long> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        insertSessionAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener){
        mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Long doInBackground(final SessionRecord... params) {
            return mAsyncTaskDao.insert(params[0]);
        }
        @Override
        protected void onPostExecute(Long result) {
            //super.onPostExecute(result);
            mListener.onInsertOneSessionRecordComplete(result);
        }
    }

////////////////////////////////////////
//INSERT MULTIPLE
    public void insertMultipleSessionRecords (SessionRecord[] sessionRecords, EveryDayRepoListener listener){
        if (!(listener instanceof EveryDayRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new insertMultipleSessionsAsyncTask(mSessionRecordDAO, listener).execute(sessionRecords);
    }

    private static class insertMultipleSessionsAsyncTask extends AsyncTask<SessionRecord, Integer, Long[]> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        insertMultipleSessionsAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Long[] doInBackground(final SessionRecord... params) {
            return mAsyncTaskDao.insertMultiple(params);
        }

        @Override
        protected void onPostExecute(Long[] result) {
            //super.onPostExecute(result);
            mListener.onInsertMultipleSessionsRecordsComplete(result);
        }
    }

////////////////////////////////////////
//UPDATE ONE
    public void updateSessionRecord (SessionRecord sessionRecord, EveryDayRepoListener listener){
        if (!(listener instanceof EveryDayRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new updateSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class updateSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        updateSessionAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final SessionRecord... params) {
            return mAsyncTaskDao.updateSessionRecord(params[0]);
        }
        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.onUpdateOneSessionRecordComplete(result);
        }
    }

////////////////////////////////////////
//DELETE ONE
    public void deleteSessionRecord(SessionRecord sessionRecord, EveryDayRepoListener listener){
        if (!(listener instanceof EveryDayRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new deleteSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class deleteSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        deleteSessionAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final SessionRecord... params) {
            return mAsyncTaskDao.deleteSessionRecord(params[0]);
        }
        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.ondeleteOneSessionRecordComplete(result);
        }
    }

////////////////////////////////////////
//DELETE ALL
    public void deleteAllSessionsRecords(EveryDayRepoListener listener){
        if (!(listener instanceof EveryDayRepoListener)) {
           throw new RuntimeException(listener.toString()+ " must implement EveryDayRepoListener");
        }
        new deleteAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class deleteAllSessionsAsyncTask extends AsyncTask<Void, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDayRepoListener mListener;

        deleteAllSessionsAsyncTask(SessionRecordDAO dao, EveryDayRepoListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Integer doInBackground(final Void... params) {
            return mAsyncTaskDao.deleteAllSessions();
        }

        @Override
        protected void onPostExecute(Integer result) {
            //super.onPostExecute(result);
            mListener.ondeleteAllSessionsRecordsComplete(result);
        }
    }

////////////////////////////////////////
//ANIMALS
////////////////////////////////////////

////////////////////////////////////////
//Listener interface
    public interface EveryDayRepoListener {
        void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions);
        void ondeleteOneSessionRecordComplete(int result);
        void ondeleteAllSessionsRecordsComplete(int result);
        void onInsertOneSessionRecordComplete(long result);
        void onInsertMultipleSessionsRecordsComplete(Long[] result);
        void onUpdateOneSessionRecordComplete(int result);
    }


}
