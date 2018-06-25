package fr.shining_cat.meditappli.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;


////////////////////////////////////////
//Repository for all datas : sessions and animals
//with an interface to dispatch callbacks
public class MeditAppliRepository {

    private SessionRecordDAO mSessionRecordDAO;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeAsc;
    private LiveData<List<SessionRecord>> mAllSessionsStartTimeDesc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationAsc;
    private LiveData<List<SessionRecord>> mAllSessionsDurationDesc;
    private LiveData<List<SessionRecord>> mSessionsWithMp3;
    private LiveData<List<SessionRecord>> mSessionsWithoutMp3;
    private LiveData<List<SessionRecord>> mSessionsSearch;

    MeditAppliRepository(Application application) {
        MeditAppliRoomDatabase db = MeditAppliRoomDatabase.getDatabase(application);
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
    public void getAllSessionsRecordsNotObservable(MeditAppliRepoListener listener) {
        if (!(listener instanceof MeditAppliRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new getAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class getAllSessionsAsyncTask extends AsyncTask<Void, Void, List<SessionRecord>> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        getAllSessionsAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener){
            mAsyncTaskDao = dao;
            mListener = listener;
        }
        @Override
        protected List<SessionRecord> doInBackground(final Void... params) {
            return mAsyncTaskDao.getAllSessionsNotLive();
        }
        @Override
        protected void onPostExecute(List<SessionRecord> result) {
            //super.onPostExecute(result);
            mListener.onGetAllSessionsNotLiveComplete(result);
        }
    }

////////////////////////////////////////
//INSERT ONE
    public void insertSessionRecord (SessionRecord sessionRecord, MeditAppliRepoListener listener){
        if (!(listener instanceof MeditAppliRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new insertSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class insertSessionAsyncTask extends AsyncTask<SessionRecord, Void, Long> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        insertSessionAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener){
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
    public void insertMultipleSessionRecords (SessionRecord[] sessionRecords, MeditAppliRepoListener listener){
        if (!(listener instanceof MeditAppliRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new insertMultipleSessionsAsyncTask(mSessionRecordDAO, listener).execute(sessionRecords);
    }

    private static class insertMultipleSessionsAsyncTask extends AsyncTask<SessionRecord, Integer, Long[]> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        insertMultipleSessionsAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener){
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
    public void updateSessionRecord (SessionRecord sessionRecord, MeditAppliRepoListener listener){
        if (!(listener instanceof MeditAppliRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new updateSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class updateSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        updateSessionAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener) {
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
    public void deleteSessionRecord(SessionRecord sessionRecord, MeditAppliRepoListener listener){
        if (!(listener instanceof MeditAppliRepoListener)) {
            throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new deleteSessionAsyncTask(mSessionRecordDAO, listener).execute(sessionRecord);
    }

    private static class deleteSessionAsyncTask extends AsyncTask<SessionRecord, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        deleteSessionAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener) {
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
    public void deleteAllSessionsRecords(MeditAppliRepoListener listener){
        if (!(listener instanceof MeditAppliRepoListener)) {
           throw new RuntimeException(listener.toString()+ " must implement MeditAppliRepoListener");
        }
        new deleteAllSessionsAsyncTask(mSessionRecordDAO, listener).execute();
    }

    private static class deleteAllSessionsAsyncTask extends AsyncTask<Void, Void, Integer> {
        private SessionRecordDAO mAsyncTaskDao;
        private MeditAppliRepoListener mListener;

        deleteAllSessionsAsyncTask(SessionRecordDAO dao, MeditAppliRepoListener listener) {
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
    public interface MeditAppliRepoListener {
        void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions);
        void ondeleteOneSessionRecordComplete(int result);
        void ondeleteAllSessionsRecordsComplete(int result);
        void onInsertOneSessionRecordComplete(long result);
        void onInsertMultipleSessionsRecordsComplete(Long[] result);
        void onUpdateOneSessionRecordComplete(int result);
    }


}
