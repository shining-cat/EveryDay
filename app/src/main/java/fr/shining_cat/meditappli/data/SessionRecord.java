package fr.shining_cat.meditappli.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.shining_cat.meditappli.MoodRecord;


////////////////////////////////////////
//ROOM entity for sessions storage
//with some convenience getters : getStartMood, getEndMood, getSessionRecordHeaders, getSessionRecordArray
@Entity (tableName = "sessions_table")
public class SessionRecord {

    private final static String START_TIME_OF_RECORD_COLUMN_NAME = "startTimeOfRecord";
    private final static String START_BODY_VALUE_COLUMN_NAME = "startBodyValue";
    private final static String START_THOUGHTS_VALUE_COLUMN_NAME = "startThoughtsValue";
    private final static String START_FEELINGS_VALUE_COLUMN_NAME = "startFeelingsValue";
    private final static String START_GLOBAL_VALUE_COLUMN_NAME = "startGlobalValue";

    private final static String END_TIME_OF_RECORD_COLUMN_NAME = "endTimeOfRecord";
    private final static String END_BODY_VALUE_COLUMN_NAME = "endBodyValue";
    private final static String END_THOUGHTS_VALUE_COLUMN_NAME = "endThoughtsValue";
    private final static String END_FEELINGS_VALUE_COLUMN_NAME = "endFeelingsValue";
    private final static String END_GLOBAL_VALUE_COLUMN_NAME = "endGlobalValue";

    private final static String NOTES_COLUMN_NAME = "notes";
    private final static String SESSION_REAL_DURATION_COLUMN_NAME = "sessionRealDuration";
    private final static String PAUSES_COUNT_COLUMN_NAME = "pausesCount";
    private final static String REAL_DURATION_VS_PLANNED_COLUMN_NAME = "realDurationVsPlanned";
    private final static String MP3_GUIDE_COLUMN_NAME = "guidemp3";

    @PrimaryKey(autoGenerate = true)
    private long id;
    //
    @ColumnInfo(name = START_TIME_OF_RECORD_COLUMN_NAME)
    private long mStartTimeOfRecord;
    @ColumnInfo(name = START_BODY_VALUE_COLUMN_NAME)
    private int mStartBodyValue;
    @ColumnInfo(name = START_THOUGHTS_VALUE_COLUMN_NAME)
    private int mStartThoughtsValue;
    @ColumnInfo(name = START_FEELINGS_VALUE_COLUMN_NAME)
    private int mStartFeelingsValue;
    @ColumnInfo(name = START_GLOBAL_VALUE_COLUMN_NAME)
    private int mStartGlobalValue;
    //
    @ColumnInfo(name = END_TIME_OF_RECORD_COLUMN_NAME)
    private long mEndTimeOfRecord;
    @ColumnInfo(name = END_BODY_VALUE_COLUMN_NAME)
    private int mEndBodyValue;
    @ColumnInfo(name = END_THOUGHTS_VALUE_COLUMN_NAME)
    private int mEndThoughtsValue;
    @ColumnInfo(name = END_FEELINGS_VALUE_COLUMN_NAME)
    private int mEndFeelingsValue;
    @ColumnInfo(name = END_GLOBAL_VALUE_COLUMN_NAME)
    private int mEndGlobalValue;
    //
    @ColumnInfo(name = NOTES_COLUMN_NAME)
    private String mNotes;
    @ColumnInfo(name = SESSION_REAL_DURATION_COLUMN_NAME)
    private long mSessionRealDuration;
    @ColumnInfo(name = PAUSES_COUNT_COLUMN_NAME)
    private int mPausesCount;
    @ColumnInfo(name = REAL_DURATION_VS_PLANNED_COLUMN_NAME)
    private int mRealDurationVsPlanned; //<0 if real < planned; =0 if real = planned; >0 if real > planned  (obtained via Long.compare(real, planned)
    @ColumnInfo(name = MP3_GUIDE_COLUMN_NAME)
    private String mGuideMp3;

    public SessionRecord(long startTimeOfRecord,
                         int startBodyValue,
                         int startThoughtsValue,
                         int startFeelingsValue,
                         int startGlobalValue,
                         //
                         long endTimeOfRecord,
                         int endBodyValue,
                         int endThoughtsValue,
                         int endFeelingsValue,
                         int endGlobalValue,
                         //
                         String notes,
                         long sessionRealDuration,
                         int pausesCount,
                         int realDurationVsPlanned,
                         String guideMp3){
        //
        mStartTimeOfRecord = startTimeOfRecord;
        mStartBodyValue = startBodyValue;
        mStartThoughtsValue = startThoughtsValue;
        mStartFeelingsValue = startFeelingsValue;
        mStartGlobalValue = startGlobalValue;
        //
        mEndTimeOfRecord = endTimeOfRecord;
        mEndBodyValue = endBodyValue;
        mEndThoughtsValue = endThoughtsValue;
        mEndFeelingsValue = endFeelingsValue;
        mEndGlobalValue = endGlobalValue;
        //
        mNotes = notes;
        mSessionRealDuration = sessionRealDuration;
        mPausesCount = pausesCount;
        mRealDurationVsPlanned = realDurationVsPlanned;
        mGuideMp3 = guideMp3;
    }

    public void setId(long setId){id = setId;}
    public long getId(){return id;}
    //
    public long getStartTimeOfRecord() {return mStartTimeOfRecord;}
    public int getStartBodyValue() {return mStartBodyValue;}
    public int getStartThoughtsValue() {return mStartThoughtsValue;}
    public int getStartFeelingsValue() {return mStartFeelingsValue;}
    public int getStartGlobalValue() {return mStartGlobalValue;}
    //
    public long getEndTimeOfRecord() {return mEndTimeOfRecord;}
    public int getEndBodyValue() {return mEndBodyValue;}
    public int getEndThoughtsValue() {return mEndThoughtsValue;}
    public int getEndFeelingsValue() {return mEndFeelingsValue;}
    public int getEndGlobalValue() {return mEndGlobalValue;}
    //
    public String getNotes() {return mNotes;}
    public long getSessionRealDuration() {return mSessionRealDuration;}
    public int getPausesCount() {return mPausesCount;}
    public int getRealDurationVsPlanned() {return mRealDurationVsPlanned;}
    public String getGuideMp3(){return mGuideMp3;}

////////////////////////////////////////
//convenience getters
    public MoodRecord getStartMood(){
        MoodRecord startMood = new MoodRecord(
                getStartTimeOfRecord(),
                getStartBodyValue(),
                getStartThoughtsValue(),
                getStartFeelingsValue(),
                getStartGlobalValue());
        return startMood;
    }
    //
    public MoodRecord getEndMood(){
        MoodRecord endMood = new MoodRecord(
                getEndTimeOfRecord(),
                getEndBodyValue(),
                getEndThoughtsValue(),
                getEndFeelingsValue(),
                getEndGlobalValue());
        endMood.setPausesCount(getPausesCount());
        endMood.setNotes(getNotes());
        endMood.setRealDurationVsPlanned(getRealDurationVsPlanned());
        endMood.setGuideMp3(getGuideMp3());
        return endMood;
    }
    //getSessionRecordHeaders, getSessionRecordArray
    public static String[] getSessionRecordHeaders(){
        String[] headers = {START_TIME_OF_RECORD_COLUMN_NAME,
                            END_TIME_OF_RECORD_COLUMN_NAME,
                            SESSION_REAL_DURATION_COLUMN_NAME,
                            NOTES_COLUMN_NAME,
                            START_BODY_VALUE_COLUMN_NAME,
                            START_THOUGHTS_VALUE_COLUMN_NAME,
                            START_FEELINGS_VALUE_COLUMN_NAME,
                            START_GLOBAL_VALUE_COLUMN_NAME,
                            END_BODY_VALUE_COLUMN_NAME,
                            END_THOUGHTS_VALUE_COLUMN_NAME,
                            END_FEELINGS_VALUE_COLUMN_NAME,
                            END_GLOBAL_VALUE_COLUMN_NAME,
                            PAUSES_COUNT_COLUMN_NAME,
                            REAL_DURATION_VS_PLANNED_COLUMN_NAME,
                            MP3_GUIDE_COLUMN_NAME};
        return headers;
    }
    //
    public String[] getSessionRecordArray(){
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String realDurationVsPlannedString = "EQUAL";
        if(getRealDurationVsPlanned() < 0){
            realDurationVsPlannedString = "LESS";
        }else if(getRealDurationVsPlanned() > 0){
            realDurationVsPlannedString = "MORE";
        }
        String[] values = { sdf.format(getStartTimeOfRecord()),
                            sdf.format(getEndTimeOfRecord()),
                            String.valueOf(getSessionRealDuration()/60000), //duration is converted to minutes for csv export (same as import)
                            getNotes(),
                            String.valueOf(getStartBodyValue()),
                            String.valueOf(getStartThoughtsValue()),
                            String.valueOf(getStartFeelingsValue()),
                            String.valueOf(getStartGlobalValue()),
                            String.valueOf(getEndBodyValue()),
                            String.valueOf(getEndThoughtsValue()),
                            String.valueOf(getEndFeelingsValue()),
                            String.valueOf(getEndGlobalValue()),
                            String.valueOf(getPausesCount()),
                            realDurationVsPlannedString,
                            getGuideMp3()
                        };
        return values;
    }
}
