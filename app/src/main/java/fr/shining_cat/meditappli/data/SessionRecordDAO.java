package fr.shining_cat.meditappli.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

////////////////////////////////////////
//DAO interface for SessionRecords entities data storage operations
@Dao
public interface SessionRecordDAO {

    @Insert
    long insert(SessionRecord sessionRecord);

    @Insert
    Long[] insertMultiple(SessionRecord... sessionRecords);

    @Update
    int updateSessionRecord(SessionRecord sessionRecord);

    @Delete
    int deleteSessionRecord(SessionRecord sessionRecord);

    @Query("DELETE FROM sessions_table")
    int deleteAllSessions();

    //ROOM does not allow parameters for the ORDER BY clause to prevent injection
    @Query("SELECT * from sessions_table ORDER BY startTimeOfRecord ASC")
    LiveData<List<SessionRecord>> getAllSessionsStartTimeAsc();

    @Query("SELECT * from sessions_table ORDER BY startTimeOfRecord DESC")
    LiveData<List<SessionRecord>> getAllSessionsStartTimeDesc();

    @Query("SELECT * from sessions_table ORDER BY sessionRealDuration ASC")
    LiveData<List<SessionRecord>> getAllSessionsDurationAsc();

    @Query("SELECT * from sessions_table ORDER BY sessionRealDuration DESC")
    LiveData<List<SessionRecord>> getAllSessionsDurationDesc();

    //
    @Query("SELECT * from sessions_table ORDER BY startTimeOfRecord ASC")
    List<SessionRecord> getAllSessionsNotLive();

    //
    @Query("SELECT * from sessions_table WHERE guidemp3 != '' ORDER BY startTimeOfRecord DESC")
    LiveData<List<SessionRecord>> getAllSessionsWithMp3();

    @Query("SELECT * from sessions_table WHERE guidemp3 = '' ORDER BY startTimeOfRecord DESC")
    LiveData<List<SessionRecord>> getAllSessionsWithoutMp3();

    //
    @Query("SELECT * from sessions_table WHERE guidemp3 LIKE :searchRequest OR notes LIKE :searchRequest ORDER BY startTimeOfRecord DESC")
    LiveData<List<SessionRecord>> getSessionsSearch(String searchRequest);
}
