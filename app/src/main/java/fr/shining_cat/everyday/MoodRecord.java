package fr.shining_cat.everyday;

public class MoodRecord {

    //values range from 1 (WORST) to 5 (BEST), 0 is for NOT SET
    private int mBodyValue;
    private int mThoughtsValue;
    private int mFeelingsValue;
    private int mGlobalValue;
    //
    private long mTimeOfRecord;
    private String mNotes;
    private long mSessionRealDuration;
    private int mPausesCount;
    private int mRealDurationVsPlanned; //<0 if real < planned; =0 if real = planned; >0 if real > planned  (obtained via Long.compare(real, planned)
    private String mGuideMp3;

////////////////////////////////////////
// Helper object to store and pass around all values that will be necessary for characterizing a session (2 MoodRecord will make 1 SessionRecord)

    public MoodRecord(long timeOfRecord, int bodyValue, int thoughtsValue, int feelingsValue, int globalValue){
        mTimeOfRecord = timeOfRecord;
        mBodyValue = bodyValue;
        mThoughtsValue = thoughtsValue;
        mFeelingsValue = feelingsValue;
        mGlobalValue = globalValue;
        mNotes = "";
        mSessionRealDuration = 0;
        mPausesCount = 0;
    }

    public long getTimeOfRecord() {return mTimeOfRecord;}
    public int getBodyValue() {return mBodyValue;}
    public int getThoughtsValue() {return mThoughtsValue;}
    public int getFeelingsValue() {return mFeelingsValue;}
    public int getGlobalValue() {return mGlobalValue;}

    public String getNotes() {return mNotes;}
    public void setNotes(String notes) {this.mNotes = notes;}

    public long getSessionRealDuration() {return mSessionRealDuration;}
    public void setSessionRealDuration(long sessionRealDuration) {this.mSessionRealDuration = sessionRealDuration;}

    public int getPausesCount() {return mPausesCount;}
    public void setPausesCount(int pausesCount) {this.mPausesCount = pausesCount;}

    public int getRealDurationVsPlanned() {return mRealDurationVsPlanned;}
    public void setRealDurationVsPlanned(int realDurationVsPlanned) {this.mRealDurationVsPlanned = realDurationVsPlanned;}

    @Override
    public String toString() {
        return "MOOD : timeStamp = " + mTimeOfRecord +
                "\n\tBody = " + mBodyValue +
                "\n\tThoughts = " + mThoughtsValue +
                "\n\tFeelings = " + mFeelingsValue +
                "\n\tGlobal = " + mGlobalValue +
                "\n\tNotes : " + mNotes +
                "\n\tMP3 : " + mGuideMp3 +
                "\n\tReal duration = " + mSessionRealDuration +
                "\n\treal Vs planned = " + mRealDurationVsPlanned +
                "\n\tPauses : " + mPausesCount;
    }

    public String getGuideMp3(){return mGuideMp3;}
    public void setGuideMp3(String guideMp3) {this.mGuideMp3 = guideMp3;}
}
