package fr.shining_cat.everyday;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static fr.shining_cat.everyday.data.Reward.CRITTER_CODE_SEPARATOR;
import static fr.shining_cat.everyday.data.Reward.ARMS_CODE_INDEX_IN_CRITTER_CODE;
import static fr.shining_cat.everyday.data.Reward.EYES_CODE_INDEX_IN_CRITTER_CODE;
import static fr.shining_cat.everyday.data.Reward.FLOWERS_CODE_INDEX_IN_CRITTER_CODE;
import static fr.shining_cat.everyday.data.Reward.HORNS_CODE_INDEX_IN_CRITTER_CODE;
import static fr.shining_cat.everyday.data.Reward.LEGS_CODE_INDEX_IN_CRITTER_CODE;
import static fr.shining_cat.everyday.data.Reward.MOUTH_CODE_INDEX_IN_CRITTER_CODE;

public abstract class Critter {

    // TODO : we need 6 versions of FLOWER and MOUTH + 7 versions of LEGS, ARMS, and EYES (6 normal and 1 "off" version : legs or arms folded, eyes closed), the "off" version of HORNS is simply empty
    //always "active" parts : flower, mouth
    public static final int NUMBER_OF_REWARDS_LEVELS = 5;
    public static final int REWARD_LEVEL_1 = 1; // 0 to 5mn    : flower = X | legs = 0 | arms = 0 | mouth = X | eyes = 0 | horns = 0  => 36 combinations
    public static final int REWARD_LEVEL_2 = 2; // 5 to 15mn   : flower = X | legs = X | arms = 0 | mouth = X | eyes = 0 | horns = 0  => 216 combinations
    public static final int REWARD_LEVEL_3 = 3; // 15 to 30mn  : flower = X | legs = X | arms = X | mouth = X | eyes = 0 | horns = 0  => 1296 combinations
    public static final int REWARD_LEVEL_4 = 4; // 30 to 60mn  : flower = X | legs = X | arms = X | mouth = X | eyes = X | horns = 0  => 7776 combinations
    public static final int REWARD_LEVEL_5 = 5; // 60mn and +  : flower = X | legs = X | arms = X | mouth = X | eyes = X | horns = X  => 46656 combinations
    //total combinations => 55980

    public static final long REWARD_DURATION_LEVEL_1 = 0;
    public static final long REWARD_DURATION_LEVEL_2 = 300000; //5mn
    public static final long REWARD_DURATION_LEVEL_3 = 900000; //15mn
    public static final long REWARD_DURATION_LEVEL_4 = 1800000; //30mn
    public static final long REWARD_DURATION_LEVEL_5 = 3600000; //60mn

    public static final int REWARD_STREAK_LEVEL_1 = 1;
    public static final int REWARD_STREAK_LEVEL_2 = 7;
    public static final int REWARD_STREAK_LEVEL_3 = 14;
    public static final int REWARD_STREAK_LEVEL_4 = 21;
    public static final int REWARD_STREAK_LEVEL_5 = 28;

    public static final int[] REWARD_CHANCE_LEVEL_1 = {100,  0,  0,  0};
    public static final int[] REWARD_CHANCE_LEVEL_2 = {100, 20,  0,  0};
    public static final int[] REWARD_CHANCE_LEVEL_3 = {100, 30, 10,  0};
    public static final int[] REWARD_CHANCE_LEVEL_4 = {100, 40, 15,  5};
    public static final int[] REWARD_CHANCE_LEVEL_5 = {100, 50, 20, 10};

    private static final int NUMBER_OF_PARTS = 6;

    //TODO: vectorize body parts in two files each : one with only black lines and shadows, the other full white for colorization
    //TODO: the lines one will be put on top of the other, which will be altered via setColorFilter according to user set color
    //TODO: beware when importing future body parts as vectors, seems that from API 24 and above, too long paths will break everything, while the vector will work on lower APIs because then it uses the support library...
    private static final int LEGS_PART_OFF  =  R.drawable.legs_0;
    private static final int ARMS_PART_OFF  =  R.drawable.arms_0;
    private static final int EYES_PART_OFF  =  R.drawable.eyes_0;
    private static final int HORNS_PART_OFF =  R.drawable.horns_0; // = empty picture

    private static final int[] FLOWER_PARTS       = {R.drawable.flower_1, R.drawable.flower_2, R.drawable.flower_3, R.drawable.flower_4, R.drawable.flower_5, R.drawable.flower_6};
    private static final int[] LEGS_COLOR_PARTS   = {LEGS_PART_OFF, R.drawable.legs_1, R.drawable.legs_2, R.drawable.legs_3, R.drawable.legs_4, R.drawable.legs_5, R.drawable.legs_6};
    private static final int[] LEGS_PARTS         = {LEGS_PART_OFF, R.drawable.legs_1, R.drawable.legs_2, R.drawable.legs_3, R.drawable.legs_4, R.drawable.legs_5, R.drawable.legs_6};
    private static final int[] ARMS_COLOR_PARTS   = {ARMS_PART_OFF, R.drawable.arms_1, R.drawable.arms_2, R.drawable.arms_3, R.drawable.arms_4, R.drawable.arms_5, R.drawable.arms_6};
    private static final int[] ARMS_PARTS         = {ARMS_PART_OFF, R.drawable.arms_1, R.drawable.arms_2, R.drawable.arms_3, R.drawable.arms_4, R.drawable.arms_5, R.drawable.arms_6};
    private static final int[] MOUTH_PARTS        = {R.drawable.mouth_1, R.drawable.mouth_2, R.drawable.mouth_3, R.drawable.mouth_4, R.drawable.mouth_5, R.drawable.mouth_6};
    private static final int[] EYES_PARTS         = {EYES_PART_OFF, R.drawable.eyes_1, R.drawable.eyes_2, R.drawable.eyes_3, R.drawable.eyes_4, R.drawable.eyes_5, R.drawable.eyes_6};
    private static final int[] HORNS_PARTS        = {HORNS_PART_OFF, R.drawable.horns_1, R.drawable.horns_2, R.drawable.horns_3, R.drawable.horns_4, R.drawable.horns_5, R.drawable.horns_6};
    
    public static String getRandomCritterCode(int critterLevel){
        String[] randomParts = new String[NUMBER_OF_PARTS];
        //init array for lowest level
        randomParts[FLOWERS_CODE_INDEX_IN_CRITTER_CODE] = String.valueOf(getRandomIndexFromIntArray(FLOWER_PARTS));
        randomParts[LEGS_CODE_INDEX_IN_CRITTER_CODE]    = "0";
        randomParts[ARMS_CODE_INDEX_IN_CRITTER_CODE]    = "0";
        randomParts[MOUTH_CODE_INDEX_IN_CRITTER_CODE]   = String.valueOf(getRandomIndexFromIntArray(MOUTH_PARTS));
        randomParts[EYES_CODE_INDEX_IN_CRITTER_CODE]    = "0";
        randomParts[HORNS_CODE_INDEX_IN_CRITTER_CODE]   = "0";
        //switch with no "break" => selective cumulative effect
        switch(critterLevel){
            case REWARD_LEVEL_5:
                randomParts[HORNS_CODE_INDEX_IN_CRITTER_CODE] = String.valueOf(getRandomIndexFromIntArray(HORNS_PARTS));
            case REWARD_LEVEL_4:
                randomParts[EYES_CODE_INDEX_IN_CRITTER_CODE]  = String.valueOf(getRandomIndexFromIntArray(EYES_PARTS));
            case REWARD_LEVEL_3:
                randomParts[ARMS_CODE_INDEX_IN_CRITTER_CODE]  = String.valueOf(getRandomIndexFromIntArray(ARMS_PARTS));
            case REWARD_LEVEL_2:
                randomParts[LEGS_CODE_INDEX_IN_CRITTER_CODE]  = String.valueOf(getRandomIndexFromIntArray(LEGS_PARTS));
            case REWARD_LEVEL_1:
            default:
                //array is already initialized at this level
        }
        String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, randomParts);
        Log.d("LOGGING::CRITTER", "getRandomCritterCode:: code = " + critterCode);
        return critterCode;
    }

    private static int getRandomIndexFromIntArray(int[] whichArray){
        return (int) (Math.random()*whichArray.length);
    }

    //generate all possible codes, taking into account the level arrangement : if horns are present, then it's a level 5, all other parts have to be > 0
    public static List<String> getAllPossibleCrittersCode(){
        List<String> allPossibleCritters = new ArrayList<>();
        /*int crittersLevel1Count = 0;
        int crittersLevel2Count = 0;
        int crittersLevel3Count = 0;
        int crittersLevel4Count = 0;
        int crittersLevel5Count = 0;*/
        for(int i = 0; i < HORNS_PARTS.length; i++){
            if(i != 0){ //REWARD_LEVEL_5
                for(int j = 1; j < EYES_PARTS.length; j++){ // do not include eye = 0
                    for(int k = 0; k < MOUTH_PARTS.length; k++){ // mouth has no "off" level
                        for(int l = 1; l < ARMS_PARTS.length; l++) { // do not include arms = 0
                            for(int m = 1; m < LEGS_PARTS.length; m++){ // do not include legs = 0
                                for(int n = 0; n < FLOWER_PARTS.length; n++){ // flower has no "off" level
                                    String [] parts = {String.valueOf(n), String.valueOf(m), String.valueOf(l), String.valueOf(k), String.valueOf(j), String.valueOf(i)};
                                    String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, parts);
                                    allPossibleCritters.add(critterCode);
                                    //crittersLevel5Count += 1;
                                }
                            }
                        }
                    }
                }
            }else{
                for(int j = 0; j < EYES_PARTS.length; j++){
                    if(j != 0){ //REWARD_LEVEL_4
                        for(int k = 0; k < MOUTH_PARTS.length; k++){ // mouth has no "off" level
                            for(int l = 1; l < ARMS_PARTS.length; l++) { // do not include arms = 0
                                for(int m = 1; m < LEGS_PARTS.length; m++){ // do not include legs = 0
                                    for(int n = 0; n < FLOWER_PARTS.length; n++){ // flower has no "off" level
                                        String [] parts = {String.valueOf(n), String.valueOf(m), String.valueOf(l), String.valueOf(k), String.valueOf(j), String.valueOf(i)};
                                        String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, parts);
                                        allPossibleCritters.add(critterCode);
                                        //crittersLevel4Count += 1;
                                    }
                                }
                            }
                        }
                    }else{
                        for(int k = 0; k < MOUTH_PARTS.length; k++){ // mouth has no "off" level
                            for(int l = 0; l < ARMS_PARTS.length; l++) {
                                if(l != 0) { //REWARD_LEVEL_3
                                    for(int m = 1; m < LEGS_PARTS.length; m++){ // do not include legs = 0
                                        for(int n = 0; n < FLOWER_PARTS.length; n++){ // flower has no "off" level
                                            String [] parts = {String.valueOf(n), String.valueOf(m), String.valueOf(l), String.valueOf(k), String.valueOf(j), String.valueOf(i)};
                                            String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, parts);
                                            allPossibleCritters.add(critterCode);
                                            //crittersLevel3Count += 1;
                                        }
                                    }
                                }else{
                                    for(int m = 0; m < LEGS_PARTS.length; m++){ //no need to check here because we have no other parts with "off" status below
                                        if(m != 0) {//REWARD_LEVEL_2
                                            for (int n = 0; n < FLOWER_PARTS.length; n++) { // flower has no "off" level
                                                String[] parts = {String.valueOf(n), String.valueOf(m), String.valueOf(l), String.valueOf(k), String.valueOf(j), String.valueOf(i)};
                                                String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, parts);
                                                allPossibleCritters.add(critterCode);
                                                //crittersLevel2Count += 1;
                                            }
                                        }else{//REWARD_LEVEL_1
                                            for (int n = 0; n < FLOWER_PARTS.length; n++) { // flower has no "off" level
                                                String[] parts = {String.valueOf(n), String.valueOf(m), String.valueOf(l), String.valueOf(k), String.valueOf(j), String.valueOf(i)};
                                                String critterCode = TextUtils.join(CRITTER_CODE_SEPARATOR, parts);
                                                allPossibleCritters.add(critterCode);
                                                //crittersLevel1Count += 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*Log.d("LOGGING::CRITTER", "LEVEL 1: " + crittersLevel1Count + " critters");
        Log.d("LOGGING::CRITTER", "LEVEL 2: " + crittersLevel2Count + " critters");
        Log.d("LOGGING::CRITTER", "LEVEL 3: " + crittersLevel3Count + " critters");
        Log.d("LOGGING::CRITTER", "LEVEL 4: " + crittersLevel4Count + " critters");
        Log.d("LOGGING::CRITTER", "LEVEL 5: " + crittersLevel5Count + " critters");*/
        Log.d("LOGGING::CRITTER", "getAllPossibleCrittersCode:: number of critters = " + allPossibleCritters.size());
        return allPossibleCritters;
    }

////////////////////////////////////////
//GET CRITTER LEVEL FROM CRITTER CODE
    public static int getCritterLevel(String critterCode){
        //Log.d("getCritterLevel", "critterCode = " + critterCode);
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        if(Integer.valueOf(critterCodeParts[HORNS_CODE_INDEX_IN_CRITTER_CODE]) != 0){
            return REWARD_LEVEL_5;
        }else if(Integer.valueOf(critterCodeParts[EYES_CODE_INDEX_IN_CRITTER_CODE]) != 0){
            return REWARD_LEVEL_4;
        }else if(Integer.valueOf(critterCodeParts[ARMS_CODE_INDEX_IN_CRITTER_CODE]) != 0){
            return REWARD_LEVEL_3;
        }else if(Integer.valueOf(critterCodeParts[LEGS_CODE_INDEX_IN_CRITTER_CODE]) != 0){
            return REWARD_LEVEL_2;
        }else{
            return REWARD_LEVEL_1;
        }
    }
    
////////////////////////////////////////
//GET DIFFERENT PARTS DRAWABLE RESOURCE CODE
    public static int getFlowerDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int flowerResourceIndexWanted = Integer.valueOf(critterCodeParts[FLOWERS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(flowerResourceIndexWanted > FLOWER_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "getFlowerDrawableResource::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return FLOWER_PARTS[0];
        }else {
            return FLOWER_PARTS[flowerResourceIndexWanted];
        }
    }
    public static int getLegsColorDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int legsResourceIndexWanted = Integer.valueOf(critterCodeParts[LEGS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(legsResourceIndexWanted > LEGS_COLOR_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "legsResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return LEGS_COLOR_PARTS[0];
        }else {
            return LEGS_COLOR_PARTS[legsResourceIndexWanted];
        }
    }
    public static int getLegsDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int legsResourceIndexWanted = Integer.valueOf(critterCodeParts[LEGS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(legsResourceIndexWanted > LEGS_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "legsResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return LEGS_PARTS[0];
        }else {
            return LEGS_PARTS[legsResourceIndexWanted];
        }
    }
    public static int getArmsColorDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int armsResourceIndexWanted = Integer.valueOf(critterCodeParts[ARMS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(armsResourceIndexWanted > ARMS_COLOR_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "armsResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return ARMS_COLOR_PARTS[0];
        }else {
            return ARMS_COLOR_PARTS[armsResourceIndexWanted];
        }
    }
    public static int getArmsDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int armsResourceIndexWanted = Integer.valueOf(critterCodeParts[ARMS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(armsResourceIndexWanted > ARMS_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "armsResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return ARMS_PARTS[0];
        }else {
            return ARMS_PARTS[armsResourceIndexWanted];
        }
    }
    public static int getMouthDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int mouthResourceIndexWanted = Integer.valueOf(critterCodeParts[MOUTH_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(mouthResourceIndexWanted > MOUTH_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "mouthResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return MOUTH_PARTS[0];
        }else {
            return MOUTH_PARTS[mouthResourceIndexWanted];
        }
    }
    public static int getEyesDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int eyesResourceIndexWanted = Integer.valueOf(critterCodeParts[EYES_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(eyesResourceIndexWanted > EYES_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "eyesResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return EYES_PARTS[0];
        }else {
            return EYES_PARTS[eyesResourceIndexWanted];
        }
    }
    public static int getHornsDrawableResource(String critterCode){
        String[] critterCodeParts = critterCode.split(CRITTER_CODE_SEPARATOR);
        int hornsResourceIndexWanted = Integer.valueOf(critterCodeParts[HORNS_CODE_INDEX_IN_CRITTER_CODE]);
        //safety check in case code stored in DB is not correlated with available drawable resources anymore :
        if(hornsResourceIndexWanted > HORNS_PARTS.length - 1){
            Log.e("LOGGING::CRITTER", "hornsResourceIndexWanted::WANTED INDEX IS NOT AVAILABLE !! switching to first one");
            return HORNS_PARTS[0];
        }else {
            return HORNS_PARTS[hornsResourceIndexWanted];
        }
    }
}
