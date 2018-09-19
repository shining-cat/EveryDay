package fr.shining_cat.everyday.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import fr.shining_cat.everyday.utils.MiscUtils;

////////////////////////////////////////
//ROOM entity for rewards storage
//with some convenience getters : get individual body-parts codes

//reward_code is a string built this way :
//      [flower-part-code]_[legs-part-code]-[arms-part-code]_[mouth-part-code]_[eyes-part-code]_[horns-part-code]
//      each code is a 1-digit number from 0 to 6 (for now) linked to the index of the corresponding png resource
//      example : 1_3_5_0_2_2

//upon creation, acquisition_date is set to 0, escape_date is set to 0, active_or_not is set to 0, and escaped_or_not is set to 0;
//On the first time the reward is obtained, active_or_not is set to 1, and never re-set to 0 again.
//when a reward is obtained, then acquisition_date is set to the moment it happens; it will be modified only if reward is re-obtained,
//if a reward escapes, then escaped_or_not is set to 1, and escape_date is set to the moment it happens, if it is obtained again, then escaped_or_not is re-set to 0

@Entity(tableName = "rewards_table")
public class Reward {

    public static final String CRITTER_CODE_SEPARATOR  = "_";
    public static final int FLOWERS_CODE_INDEX_IN_CRITTER_CODE  = 0;
    public static final int LEGS_CODE_INDEX_IN_CRITTER_CODE     = 1;
    public static final int ARMS_CODE_INDEX_IN_CRITTER_CODE     = 2;
    public static final int MOUTH_CODE_INDEX_IN_CRITTER_CODE    = 3;
    public static final int EYES_CODE_INDEX_IN_CRITTER_CODE     = 4;
    public static final int HORNS_CODE_INDEX_IN_CRITTER_CODE    = 5;
    public static final long NO_ACQUISITION_DATE = 0;
    public static final long NO_ESCAPING_DATE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ESCAPED = 1;
    public static final int STATUS_NOT_ESCAPED = 0;
    public static final String NO_NAME = "";
    public static final String DEFAULT_COLOR_IS_WHITE = "#00000000";


    private final static String REWARD_CODE_COLUMN_NAME              = "rewardCode";
    private final static String REWARD_LEVEL_COLUMN_NAME             = "rewardLevel";
    private final static String DATE_ACQUISITION_COLUMN_NAME         = "acquisitionDate";
    private final static String DATE_ESCAPING_COLUMN_NAME            = "escapingDate";
    private final static String ACTIVE_STATE_COLUMN_NAME             = "activeOrNot";
    private final static String ESCAPED_STATE_COLUMN_NAME            = "escapedOrNot";
    private final static String REWARD_CUSTOM_NAME_COLUMN_NAME       = "rewardName";
    private final static String REWARD_CUSTOM_LEGS_COLOR_COLUMN_NAME = "rewardLegsColor";
    private final static String REWARD_CUSTOM_BODY_COLOR_COLUMN_NAME = "rewardBodyColor";
    private final static String REWARD_CUSTOM_ARMS_COLOR_COLUMN_NAME = "rewardArmsColor";

    @PrimaryKey(autoGenerate = true)
    private long id;
    //
    @ColumnInfo(name = REWARD_CODE_COLUMN_NAME)
    private String mRewardCode;
    @ColumnInfo(name = REWARD_LEVEL_COLUMN_NAME)
    private int mRewardLevel;
    @ColumnInfo(name = DATE_ACQUISITION_COLUMN_NAME)
    private long mAcquisitionDate;
    @ColumnInfo(name = DATE_ESCAPING_COLUMN_NAME)
    private long mEscapingDate;
    @ColumnInfo(name = ACTIVE_STATE_COLUMN_NAME)
    private int mActiveOrNot;
    @ColumnInfo(name = ESCAPED_STATE_COLUMN_NAME)
    private int mEscapedOrNot;
    @ColumnInfo(name = REWARD_CUSTOM_NAME_COLUMN_NAME)
    private String mRewardName;
    @ColumnInfo(name = REWARD_CUSTOM_LEGS_COLOR_COLUMN_NAME)
    private String mRewardLegsColor;
    @ColumnInfo(name = REWARD_CUSTOM_BODY_COLOR_COLUMN_NAME)
    private String mRewardBodyColor;
    @ColumnInfo(name = REWARD_CUSTOM_ARMS_COLOR_COLUMN_NAME)
    private String mRewardArmsColor;

    public Reward(String rewardCode, int rewardLevel, long acquisitionDate, long escapingDate, int activeOrNot, int escapedOrNot,
                  String rewardName, String rewardLegsColor, String rewardBodyColor, String rewardArmsColor){
        mRewardCode = rewardCode;
        mRewardLevel = rewardLevel;
        mAcquisitionDate = acquisitionDate;
        mEscapingDate = escapingDate;
        mActiveOrNot = activeOrNot;
        mEscapedOrNot = escapedOrNot;
        mRewardName = rewardName;
        mRewardLegsColor = rewardLegsColor;
        mRewardBodyColor = rewardBodyColor;
        mRewardArmsColor = rewardArmsColor;
    }

    public void setId(long setId){id = setId;}
    public long getId(){return id;}
    //
    public String getRewardCode() {return mRewardCode;}
    public int getRewardLevel() {return mRewardLevel;}
    public long getAcquisitionDate() {return mAcquisitionDate;}
    public long getEscapingDate() {return mEscapingDate;}
    public int getActiveOrNot() {return mActiveOrNot;}
    public int getEscapedOrNot() {return mEscapedOrNot;}
    public String getRewardName() {return mRewardName;}
    public String getRewardLegsColor() {return mRewardLegsColor;}
    public String getRewardBodyColor() {return mRewardBodyColor;}
    public String getRewardArmsColor() {return mRewardArmsColor;}

////////////////////////////////////////
//convenience getters
    public int getRewardFlowerCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[FLOWERS_CODE_INDEX_IN_CRITTER_CODE]);
    }
    public int getRewardLegsCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[LEGS_CODE_INDEX_IN_CRITTER_CODE]);
    }
    public int getRewardArmsCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[ARMS_CODE_INDEX_IN_CRITTER_CODE]);
    }
    public int getRewardMouthCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[MOUTH_CODE_INDEX_IN_CRITTER_CODE]);
    }
    public int getRewardEyesCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[EYES_CODE_INDEX_IN_CRITTER_CODE]);
    }
    public int getRewardHornsCode(){
        String[] rewardCodeSplit = mRewardCode.split(CRITTER_CODE_SEPARATOR);
        return Integer.valueOf(rewardCodeSplit[HORNS_CODE_INDEX_IN_CRITTER_CODE]);
    }
////////////////////////////////////////
//setters to facilitate update
    public void setRewardName(String name){
        mRewardName = name;
    }

    public void setRewardLegsColor(int color) {
        mRewardLegsColor = MiscUtils.convertColorIntToString(color);
    }
    public void setRewardBodyColor(int color) {
        mRewardBodyColor = MiscUtils.convertColorIntToString(color);
    }
    public void setRewardArmsColor(int color) {
        mRewardArmsColor = MiscUtils.convertColorIntToString(color);
    }
    public void setRewardActiveOrNot(int activeOrNot) {
        mActiveOrNot = activeOrNot;
    }
    public void setRewardEscapedOrNot(int escapedOrNot) {
        mEscapedOrNot = escapedOrNot;
    }
    public void setRewardAcquisitionDate(long acquisitionDate) {
        mAcquisitionDate = acquisitionDate;
    }
    public void setRewardEscapingDate(long escapingDate) {
        mEscapingDate = escapingDate;
    }
}
