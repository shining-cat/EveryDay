package fr.shining_cat.meditappli.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


////////////////////////////////////////
//Application RoomDatabase
//todo : add bestioles entity to create table
@Database(entities = {SessionRecord.class}, version = 1)
public abstract class MeditAppliRoomDatabase extends RoomDatabase {

    public abstract SessionRecordDAO sessionRecordDao();

    //singleton to prevent having multiple instances of the database opened at the same time :
    private static MeditAppliRoomDatabase INSTANCE;
    public static MeditAppliRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeditAppliRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeditAppliRoomDatabase.class, "meditappli_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }



}
