package fr.shining_cat.meditappli.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import fr.shining_cat.meditappli.MoodRecord;

public class SessionRecordViewModel extends AndroidViewModel {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private MeditAppliRepository mMeditApplyRepository;

    private LiveData<List<SessionRecord>> mAllSessionsRecords;

////////////////////////////////////////
//AndroidViewModel for SessionRecords entities data storage operations with some commodity methods
    public SessionRecordViewModel(Application application){
        super(application);
        mMeditApplyRepository = new MeditAppliRepository(application);
        mAllSessionsRecords = mMeditApplyRepository.getAllSessionsRecords();
    }

////////////////////////////////////////
//GET ALL LIVE
    public LiveData<List<SessionRecord>> getAllSessionsRecords(){return mAllSessionsRecords;}

////////////////////////////////////////
//GET ALL NOT LIVE
    public void getAllSessionsRecordsInBunch(MeditAppliRepository.MeditAppliRepoListener listener){
        mMeditApplyRepository.getAllSessionsRecordsNotObservable(listener);
    }

////////////////////////////////////////
//INSERT ONE providing MoodRecord objects (start and end)
    public void insertWithMoods (MoodRecord startMood, MoodRecord endMood, MeditAppliRepository.MeditAppliRepoListener listener){
        SessionRecord sessionRecord = new SessionRecord(
                startMood.getTimeOfRecord(),
                startMood.getBodyValue(),
                startMood.getThoughtsValue(),
                startMood.getFeelingsValue(),
                startMood.getGlobalValue(),
                //
                endMood.getTimeOfRecord(),
                endMood.getBodyValue(),
                endMood.getThoughtsValue(),
                endMood.getFeelingsValue(),
                endMood.getGlobalValue(),
                //
                endMood.getNotes(),
                endMood.getSessionRealDuration(),
                endMood.getPausesCount(),
                endMood.getRealDurationVsPlanned(),
                endMood.getGuideMp3());
        this.insert(sessionRecord, listener);
    }

////////////////////////////////////////
//INSERT ONE
    public void insert (SessionRecord sessionRecord, MeditAppliRepository.MeditAppliRepoListener listener){
        mMeditApplyRepository.insertSessionRecord(sessionRecord, listener);
    }

////////////////////////////////////////
//INSERT MULTIPLE
    public void insertMultiple (List<SessionRecord> sessionRecordsList, MeditAppliRepository.MeditAppliRepoListener listener){
        SessionRecord[] sessionRecordsArray = sessionRecordsList.toArray(new SessionRecord[sessionRecordsList.size()]);
        Log.d(TAG, "insertMultiple::sessionRecordsList size = " + sessionRecordsList.size());
        Log.d(TAG, "insertMultiple::sessionRecordsArray length = " + sessionRecordsArray.length);
        mMeditApplyRepository.insertMultipleSessionRecords(sessionRecordsArray, listener);
    }

////////////////////////////////////////
//UPDATE ONE providing SessionRecord entity's ID and MoodRecord objects (start and end)
    public void updateWithMoods (long sessionToUpdateId, MoodRecord startMood, MoodRecord endMood, MeditAppliRepository.MeditAppliRepoListener listener){
        SessionRecord sessionRecord = new SessionRecord(
                startMood.getTimeOfRecord(),
                startMood.getBodyValue(),
                startMood.getThoughtsValue(),
                startMood.getFeelingsValue(),
                startMood.getGlobalValue(),
                //
                endMood.getTimeOfRecord(),
                endMood.getBodyValue(),
                endMood.getThoughtsValue(),
                endMood.getFeelingsValue(),
                endMood.getGlobalValue(),
                //
                endMood.getNotes(),
                endMood.getSessionRealDuration(),
                endMood.getPausesCount(),
                endMood.getRealDurationVsPlanned(),
                endMood.getGuideMp3());
        sessionRecord.setId(sessionToUpdateId);
        this.update(sessionRecord, listener);
    }

////////////////////////////////////////
//UPDATE ONE
    public void update (SessionRecord sessionRecord, MeditAppliRepository.MeditAppliRepoListener listener){
        mMeditApplyRepository.updateSessionRecord(sessionRecord, listener);
    }

////////////////////////////////////////
//DELETE ONE
    public void deleteOneSession (SessionRecord sessionRecord, MeditAppliRepository.MeditAppliRepoListener listener){
        mMeditApplyRepository.deleteSessionRecord(sessionRecord, listener);
    }

////////////////////////////////////////
//DELETE ALL
    public void deleteAllSessions (MeditAppliRepository.MeditAppliRepoListener listener){
        mMeditApplyRepository.deleteAllSessionsRecords(listener);
    }

}
