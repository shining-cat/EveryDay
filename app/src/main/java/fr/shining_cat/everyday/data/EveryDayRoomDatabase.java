package fr.shining_cat.everyday.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


////////////////////////////////////////
//Application RoomDatabase

@Database(entities = {SessionRecord.class, Reward.class}, version = 1)
public abstract class EveryDayRoomDatabase extends RoomDatabase {

    public abstract SessionRecordDAO sessionRecordDao();
    public abstract RewardDAO rewardDao();

    //singleton to prevent having multiple instances of the database opened at the same time :
    private static EveryDayRoomDatabase INSTANCE;
    public static EveryDayRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EveryDayRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EveryDayRoomDatabase.class, "everyday_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }



}
