package fr.shining_cat.everyday.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;


////////////////////////////////////////
//Repository for sessions datas
//with an interface to dispatch callbacks
public class EveryDaySessionsDataRepository {

    private SessionRecordDAO mSessionRecordDAO;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeAsc;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeDesc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationAsc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationDesc;
    private LiveData<List<SessionRecord>> mSessionsWithMp3;
    private LiveData<List<SessionRecord>> mSessionsWithoutMp3;

    EveryDaySessionsDataRepository(Application application) {
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
//GET LATEST RECORDED SESSION DATE
    public void getLatestRecordedSessionDate(EveryDaySessionsRepoListener listener) {
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new GetLatestRecordedSessionDateAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class GetLatestRecordedSessionDateAsyncTask extends AsyncTask<Void, Void, Long> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        GetLatestRecordedSessionDateAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected Long doInBackground(final Void... params) {
            return mAsyncTaskDao.getLatestRecordedSessionDate();
        }
        @Override
        protected void onPostExecute(Long latestSessionRecordedDate) {
            //super.onPostExecute(result);
            mListener.onGetLatestRecordedSessionDateComplete(latestSessionRecordedDate);
        }
    }

////////////////////////////////////////
//SEARCH REQUEST
    LiveData<List<SessionRecord>> getSessionsRecordsSearch(String searchRequest) {
        return mSessionRecordDAO.getSessionsSearch(searchRequest);
    }

////////////////////////////////////////
//GET ALL NOT LIVE
    public void getAllSessionsRecordsNotObservable(EveryDaySessionsRepoListener listener) {
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new GetAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class GetAllSessionsAsyncTask extends AsyncTask<Void, Void, List<SessionRecord>> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        GetAllSessionsAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener){
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
    public void insertSessionRecord (SessionRecord sessionRecord, EveryDaySessionsRepoListener listener){
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new InsertSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class InsertSessionAsyncTask extends AsyncTask<SessionRecord, Void, Long> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        InsertSessionAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener){
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
    public void insertMultipleSessionRecords (SessionRecord[] sessionRecords, EveryDaySessionsRepoListener listener){
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new InsertMultipleSessionsAsyncTask(mSessionRecordDAO, listener).execute(sessionRecords);
    }

    private static class InsertMultipleSessionsAsyncTask extends AsyncTask<SessionRecord, Integer, Long[]> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        InsertMultipleSessionsAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener){
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
    public void updateSessionRecord (SessionRecord sessionRecord, EveryDaySessionsRepoListener listener){
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new UpdateSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class UpdateSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        UpdateSessionAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener) {
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
    public void deleteSessionRecord(SessionRecord sessionRecord, EveryDaySessionsRepoListener listener){
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new DeleteSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class DeleteSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        DeleteSessionAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener) {
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
    public void deleteAllSessionsRecords(EveryDaySessionsRepoListener listener){
        if (!(listener instanceof EveryDaySessionsRepoListener)) {
           throw new RuntimeException(listener.toString()+ " must implement EveryDaySessionsRepoListener");
        }
        new DeleteAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class DeleteAllSessionsAsyncTask extends AsyncTask<Void, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private EveryDaySessionsRepoListener mListener;

        DeleteAllSessionsAsyncTask(SessionRecordDAO dao, EveryDaySessionsRepoListener listener) {
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
//Listener interface
    public interface EveryDaySessionsRepoListener {
        void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions);
        void ondeleteOneSessionRecordComplete(int result);
        void ondeleteAllSessionsRecordsComplete(int result);
        void onInsertOneSessionRecordComplete(long result);
        void onInsertMultipleSessionsRecordsComplete(Long[] result);
        void onUpdateOneSessionRecordComplete(int result);
        void onGetLatestRecordedSessionDateComplete(long latestSessionRecordedDate);
    }


}
