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

    @Query("SELECT * from sessions_table ORDER BY startTimeOfRecord DESC")
    LiveData<List<SessionRecord>> getAllSessions();

    @Query("SELECT * from sessions_table ORDER BY startTimeOfRecord DESC")
    List<SessionRecord> getAllSessionsNotLive();

}
