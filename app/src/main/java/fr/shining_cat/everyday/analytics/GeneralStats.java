package fr.shining_cat.everyday.analytics;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.TimeOperations;

public abstract class GeneralStats {

    private final static String TAG = "LOGGING::GeneralStats";
////////////////////////////////////////
//Sessions longest, total and average DURATION operations
    public static long getLongestSession(List<SessionRecord> sessions){
        long longestDuration = 0;
        if(sessions.size() != 0) {
            for (SessionRecord session : sessions) {
                //Log.d(TAG, "getLongestSession::session.getSessionRealDuration() = " + session.getSessionRealDuration()/60000 + " VS : longestDuration = " + longestDuration/60000);
                if (session.getSessionRealDuration() > longestDuration) {
                    //Log.d(TAG, "getLongestSession:: current is longer");
                    longestDuration = session.getSessionRealDuration();
                }
            }
        }
        return longestDuration;
    }

    public static long getTotalLength(List<SessionRecord> sessions){
        long totalLength = 0;
        if(sessions.size() != 0) {
            for (SessionRecord session : sessions) {
                totalLength += session.getSessionRealDuration();
                //Log.d(TAG, "getTotalLength::session.getSessionRealDuration() = " + session.getSessionRealDuration()/60000 + " / totalLength = " + totalLength/60000);
            }
        }
        return totalLength;
    }

    //returns long and not float, because duration is in millisecond... don't really care about fractions of milliseconds ;p
    public static long getAverageDuration(List<SessionRecord> sessions){
        long averageDuration = 0;
        if(sessions.size() != 0) {
            averageDuration = getTotalLength(sessions) / sessions.size();
            //Log.d(TAG, "getAverageDuration::sessions.size() = " + sessions.size() + " / average = " + averageDuration/60000);
        }
        return averageDuration;
    }

////////////////////////////////////////
//Pauses (sessions interruptions)
    public static int getTotalNumberOfPauses(List<SessionRecord> sessions){
        int totalPauses = 0;
        for(SessionRecord session : sessions){
            totalPauses += session.getPausesCount();
            //Log.d(TAG, "getTotalNumberOfPauses::session.getPausesCount() = " + session.getPausesCount() + " / totalPauses = " + totalPauses);
        }
        return totalPauses;
    }

    public static float getAverageNumberOfPausesBySession(List<SessionRecord> sessions){
        float averageNumberOfPausesBySession = 0;
        if(sessions.size() != 0) {
            averageNumberOfPausesBySession = ((float) getTotalNumberOfPauses(sessions) / (float) sessions.size());
            //Log.d(TAG, "getAverageNumberOfPausesBySession::sessions.size() = " + sessions.size() + " / averageNumberOfPausesBySession = " + averageNumberOfPausesBySession);
        }
        return averageNumberOfPausesBySession;
    }
////////////////////////////////////////
//longest, current, number of start and stop STREAKS operations
    public static int getLongestStreak(List<SessionRecord> sessions){
        //
        DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        //
        int currentStreak = 0;
        int longestStreak = 0;
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(0);
        for(SessionRecord session : sessions){
            long currentSessionTimeStamp = session.getStartTimeOfRecord();
            Calendar currentSessionCal = Calendar.getInstance();
            currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
            //Log.d(TAG, "getLongestStreak::previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
            //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
            if(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)){
                //do nothing, streak has not been broken but has neither been augmented
                //Log.d(TAG, "getLongestStreak:: SAME DAY streak not broken but not augmented");
            }else {
                //add 1 day to previous session
                previousSessionCal.add(Calendar.DATE, 1);
                //Log.d(TAG, "getLongestStreak:: ADDED 1 DAY to previous => previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
                //compare
                if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)) {
                    //Log.d(TAG, "getLongestStreak:: previous + 1 day is SAME DAY as current => STREAK IS AUGMENTED by 1");
                    currentStreak += 1;
                    //Log.d(TAG, "getLongestStreak:: currentStreak = " + currentStreak);
                    if (currentStreak > longestStreak) {
                        //Log.d(TAG, "getLongestStreak:: currentStreak > longestStreak so longest streak is augmented by 1");
                        longestStreak += 1;
                        //Log.d(TAG, "getLongestStreak:: longestStreak = " + longestStreak);
                    }
                } else {
                    //Log.d(TAG, "getLongestStreak::no streak");
                    currentStreak = 0;
                }
            }
            previousSessionCal = currentSessionCal;
        }
        return longestStreak;
    }

    public static int getCurrentStreak(List<SessionRecord> sessions){
        int currentStreak = 0;
        //duplicate given list to prevent modifying it if caller still needs it
        List<SessionRecord> reversedSessionsList = new ArrayList<>(sessions);
        //reverse sessions list to only examine running streak from last item
        Collections.reverse(reversedSessionsList);
        //
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(System.currentTimeMillis());
        boolean firstPass = true;
        for(SessionRecord session : reversedSessionsList) {
            long currentSessionTimeStamp = session.getStartTimeOfRecord();
            Calendar currentSessionCal = Calendar.getInstance();
            currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
            //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
            if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)) {
                if(firstPass){
                    //if last session was on same day as TODAY, we want to count it as the first day of the streak
                    firstPass = false;
                    currentStreak += 1;
                }else {
                    //session has been done on same day as previous one examined : do nothing, streak has not been broken but has neither been augmented
                }
            } else {
                //remove 1 day to previous session
                previousSessionCal.add(Calendar.DATE, -1);
                //compare
                if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)) {
                    currentStreak += 1;
                }else{
                    return currentStreak;
                }
            }
        }
        return currentStreak;
    }
    //count the number of sessions that started a streak, not intended for the whole sessions list but for a list of session already sorted (hour of day, day of week, or day of month)
    public static int getStartedStreaksNumber(List<SessionRecord> sessions){
        int startedStreaks = 0;
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(0);
        for(SessionRecord session : sessions){
            long currentSessionTimeStamp = session.getStartTimeOfRecord();
            Calendar currentSessionCal = Calendar.getInstance();
            currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
            //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
            if(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)){
                //do nothing, streak has not been broken but has neither been augmented
            }else {
                //add 1 day to previous session
                previousSessionCal.add(Calendar.DATE, 1);
                //compare, if current examined session is NOT on the following day, then it's a new streak start, otherwise it's just the same streak continuing
                if (!(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal))) {
                    startedStreaks += 1;
                }
            }
            previousSessionCal = currentSessionCal;
        }
        return startedStreaks;
    }

    public static int getStoppedStreaksNumber(List<SessionRecord> sessions){
        int stoppedStreaks = 0;
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(0);
        boolean firstTest = true;
        for(SessionRecord session : sessions){
            long currentSessionTimeStamp = session.getStartTimeOfRecord();
            Calendar currentSessionCal = Calendar.getInstance();
            currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
            //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
            if(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)){
                //do nothing, streak has not been broken but has neither been augmented
            }else {
                //add 1 day to previous session
                previousSessionCal.add(Calendar.DATE, 1);
                //compare, if current examined session is NOT on the following day, then running streak was broken, otherwise it's just the same streak continuing
                if (!(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal))) {
                    if(firstTest){
                        firstTest = false;
                    }else {
                        stoppedStreaks += 1;
                    }
                }
            }
            previousSessionCal = currentSessionCal;
        }
        //check last session against NOW to check if last session was last of a running streak or if it is still running
        long nowTimeStamp = System.currentTimeMillis();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(nowTimeStamp);
        //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
        if(TimeOperations.setTimePartOfDateToMidnight(previousSessionCal) == TimeOperations.setTimePartOfDateToMidnight(nowCal)){
            //do nothing, streak has not been broken but has neither been augmented
        }else {
            //add 1 day to last session
            previousSessionCal.add(Calendar.DATE, 1);
            //compare, if current examined session is NOT on the following day, then running streak was broken, otherwise it's just the same streak continuing
            if (!(TimeOperations.setTimePartOfDateToMidnight(previousSessionCal) == TimeOperations.setTimePartOfDateToMidnight(nowCal))) {
                stoppedStreaks += 1;
            }
        }
        //
        return stoppedStreaks;
    }

    public static List<SessionRecord> filterGetOnlyStartingStreakSessions(List<SessionRecord> sessions){
        //
        DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        //
        ArrayList<SessionRecord> onlyStartingStreakSessions = new ArrayList<>();
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(0);
        if(sessions.size() != 0) {
            for (SessionRecord currentSession : sessions) {
                long currentSessionTimeStamp = currentSession.getStartTimeOfRecord();
                Calendar currentSessionCal = Calendar.getInstance();
                currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
                //Log.d(TAG, "filterGetOnlyStartingStreakSessions::previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
                //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
                if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)) {
                    //do nothing, streak has not been broken but has neither been augmented
                    //Log.d(TAG, "filterGetOnlyStartingStreakSessions:: SAME DAY streak not broken but not augmented");
                } else {
                    //add 1 day to previous currentSession
                    previousSessionCal.add(Calendar.DATE, 1);
                    //Log.d(TAG, "filterGetOnlyStartingStreakSessions:: ADDED 1 DAY to previous => previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
                    //compare, if current examined currentSession is NOT on the following day, then it's a new streak start, otherwise it's just the same streak continuing
                    if (!(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal))) {
                        //Log.d(TAG, "filterGetOnlyStartingStreakSessions:: previous streak was broken => current session is starting a new streak");
                        onlyStartingStreakSessions.add(currentSession);
                    }else{
                        //Log.d(TAG, "filterGetOnlyStartingStreakSessions:: previous + 1 day is SAME DAY as current => streak continues => current session is not starting a new streak");
                    }
                }
                previousSessionCal = currentSessionCal;
            }
        }
        return onlyStartingStreakSessions;
    }

    public static List<SessionRecord> filterGetOnlyStoppingStreakSessions(List<SessionRecord> sessions){
        //
        DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        //
        ArrayList<SessionRecord> onlyStoppingStreakSessions = new ArrayList<>();
        Calendar previousSessionCal = Calendar.getInstance();
        previousSessionCal.setTimeInMillis(0);
        SessionRecord previousSession = null;
        if(sessions.size() != 0) {
            for (SessionRecord currentSession : sessions) {
                long currentSessionTimeStamp = currentSession.getStartTimeOfRecord();
                Calendar currentSessionCal = Calendar.getInstance();
                currentSessionCal.setTimeInMillis(currentSessionTimeStamp);
                //Log.d(TAG, "filterGetOnlyStoppingStreakSessions::previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
                //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
                if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal)) {
                    //do nothing, streak has not been broken but has neither been augmented
                    //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: SAME DAY streak not broken but not augmented");
                } else {
                    //add 1 day to previous currentSession
                    previousSessionCal.add(Calendar.DATE, 1);
                    //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: ADDED 1 DAY to previous => previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / currentSessionCal = " + sdfDate.format(currentSessionCal.getTime()));
                    //compare, if current examined currentSession is NOT on the following day, then running streak was broken, otherwise it's just the same streak continuing
                    if (!(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(currentSessionCal))) {
                        //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: previous streak was broken => previous session is a broking streak session");
                        if(previousSession != null) {
                            onlyStoppingStreakSessions.add(previousSession);
                        }else{
                            //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: previous session is null");
                        }
                    }else{
                        //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: previous + 1 day is SAME DAY as current => streak continues => previous session did not break a streak");
                    }
                }
                previousSession = currentSession;
                previousSessionCal = currentSessionCal;
            }
            //check last session against NOW to check if last session was last of a running streak or if it is still running
            long nowTimeStamp = System.currentTimeMillis();
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(nowTimeStamp);
            //Log.d(TAG, "filterGetOnlyStoppingStreakSessions::checking last session of list against NOW");
            //compare previous and current to avoid counting two sessions in the same day as +1 day in the streak
            if (TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(nowCal)) {
                //do nothing, last session was today, so streak can not have been broken yet
                //Log.d(TAG, "filterGetOnlyStoppingStreakSessions::SAME DAY streak not broken but not augmented");
            } else {
                //add 1 day to last session
                previousSessionCal.add(Calendar.DATE, 1);
                //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: ADDED 1 DAY to previous => previousSessionCal = " + sdfDate.format(previousSessionCal.getTime()) + " / nowCal = " + sdfDate.format(nowCal.getTime()));
                //compare, if current examined session is NOT on the following day, then running streak was broken, otherwise it's just the same streak continuing
                if (!(TimeOperations.getTimestampSameDateAtMidnight(previousSessionCal) == TimeOperations.getTimestampSameDateAtMidnight(nowCal))) {
                    //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: previous streak was broken => previous session is a broking streak session");
                    //previousSession is never null here, no need to check
                    onlyStoppingStreakSessions.add(previousSession);
                }else{
                    //Log.d(TAG, "filterGetOnlyStoppingStreakSessions:: previous + 1 day is SAME DAY as today => streak continues => previous session did not break a streak");
                }
            }
        }
        return onlyStoppingStreakSessions;
    }

////////////////////////////////////////
//Sessions BODY values operations

    //returns null if no session has any record for start body value
    public static Float getAverageStartBodyValue(List<SessionRecord> sessions){
        Float averageStartBodyValue = null;
        if(sessions.size() != 0) {
            long totalStartBodyValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getStartBodyValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalStartBodyValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageStartBodyValue = ((float) totalStartBodyValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageStartBodyValue;
    }

    //returns null if no session has any record for end body value
    public static Float getAverageEndBodyValue(List<SessionRecord> sessions){
        Float averageEndBodyValue = null;
        if(sessions.size() != 0) {
            long totalEndBodyValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getEndBodyValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalEndBodyValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageEndBodyValue = ((float) totalEndBodyValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageEndBodyValue;
    }

    //returns 0f if no session has any record for start or end body value =>  diff is assumed to be 0 if calculating it is not possible
    public static Float getAverageDiffBodyValue(List<SessionRecord> sessions){
        Float averageDiffBodyValue = 0f;
        Float averageEndBodyValue = getAverageEndBodyValue(sessions);
        Float averageStartBodyValue = getAverageStartBodyValue(sessions);
        //if one of the values can not be calculated because no session has any record to do so, then the diff has no meaning
        if(averageStartBodyValue != null && averageEndBodyValue != null){
            averageDiffBodyValue = averageEndBodyValue - averageStartBodyValue;
        }
        return averageDiffBodyValue;
    }
////////////////////////////////////////
//Sessions THOUGHTS values operations

    //returns null if no session has any record for start thoughts value
    public static Float getAverageStartThoughtsValue(List<SessionRecord> sessions){
        Float averageStartThoughtsValue = null;
        if(sessions.size() != 0) {
            long totalStartThoughtsValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getStartThoughtsValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalStartThoughtsValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageStartThoughtsValue = ((float) totalStartThoughtsValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageStartThoughtsValue;
    }

    //returns null if no session has any record for end thoughts value
    public static Float getAverageEndThoughtsValue(List<SessionRecord> sessions){
        Float averageEndThoughtsValue = null;
        if(sessions.size() != 0) {
            long totalEndThoughtsValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getEndThoughtsValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalEndThoughtsValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageEndThoughtsValue = ((float) totalEndThoughtsValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageEndThoughtsValue;
    }

    //returns 0f if no session has any record for start or end thoughts value =>  diff is assumed to be 0 if calculating it is not possible
    public static Float getAverageDiffThoughtsValue(List<SessionRecord> sessions){
        Float averageDiffThoughtsValue = 0f;
        Float averageEndThoughtsValue = getAverageEndThoughtsValue(sessions);
        Float averageStartThoughtsValue = getAverageStartThoughtsValue(sessions);
        //if one of the values can not be calculated because no session has any record to do so, then the diff has no meaning
        if(averageStartThoughtsValue != null && averageEndThoughtsValue != null){
            averageDiffThoughtsValue = averageEndThoughtsValue - averageStartThoughtsValue;
        }
        return averageDiffThoughtsValue;
    }
////////////////////////////////////////
//Sessions FEELINGS values operations

    //returns null if no session has any record for start feelings value
    public static Float getAverageStartFeelingsValue(List<SessionRecord> sessions){
        Float averageStartFeelingsValue = null;
        if(sessions.size() != 0) {
            long totalStartFeelingsValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getStartFeelingsValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalStartFeelingsValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageStartFeelingsValue = ((float) totalStartFeelingsValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageStartFeelingsValue;
    }

    //returns null if no session has any record for end feelings value
    public static Float getAverageEndFeelingsValue(List<SessionRecord> sessions){
        Float averageEndFeelingsValue = null;
        if(sessions.size() != 0) {
            long totalEndFeelingsValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getEndFeelingsValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalEndFeelingsValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageEndFeelingsValue = ((float) totalEndFeelingsValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageEndFeelingsValue;
    }

    //returns 0f if no session has any record for start or end feelings value =>  diff is assumed to be 0 if calculating it is not possible
    public static Float getAverageDiffFeelingsValue(List<SessionRecord> sessions){
        Float averageDiffFeelingsValue = 0f;
        Float averageEndFeelingsValue = getAverageEndFeelingsValue(sessions);
        Float averageStartFeelingsValue = getAverageStartFeelingsValue(sessions);
        //if one of the values can not be calculated because no session has any record to do so, then the diff has no meaning
        if(averageStartFeelingsValue != null && averageEndFeelingsValue != null){
            averageDiffFeelingsValue = averageEndFeelingsValue - averageStartFeelingsValue;
        }
        return averageDiffFeelingsValue;
    }

////////////////////////////////////////
//Sessions GLOBAL values operations

    //returns null if no session has any record for start global value
    public static Float getAverageStartGlobalValue(List<SessionRecord> sessions){
        Float averageStartGlobalValue = null;
        if(sessions.size() != 0) {
            long totalStartGlobalValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getStartGlobalValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if (value != 0) {
                    totalStartGlobalValue += value;
                    numberOfRecordsWithValueSet++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageStartGlobalValue = ((float) totalStartGlobalValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageStartGlobalValue;
    }

    //returns null if no session has any record for end global value
    public static Float getAverageEndGlobalValue(List<SessionRecord> sessions){
        Float averageEndGlobalValue = null;
        if(sessions.size() != 0) {
            long totalEndGlobalValue = 0;
            int numberOfRecordsWithValueSet = 0;
            for (SessionRecord session : sessions) {
                int value = session.getEndGlobalValue();
                //only take into account values when set (actual value range is from 1 -WORST- to 5 -BEST- and 0 is for NOT SET)
                if(value != 0) {
                    totalEndGlobalValue += value;
                    numberOfRecordsWithValueSet ++;
                }
            }
            if(numberOfRecordsWithValueSet != 0) {
                averageEndGlobalValue = ((float) totalEndGlobalValue / (float) numberOfRecordsWithValueSet);
            }
        }
        return averageEndGlobalValue;
    }

    //returns 0f if no session has any record for start or end global value => diff is assumed to be 0 if calculating it is not possible
    public static Float getAverageDiffGlobalValue(List<SessionRecord> sessions){
        Float averageDiffGlobalValue = 0f;
        Float averageEndGlobalValue = getAverageEndGlobalValue(sessions);
        Float averageStartGlobalValue = getAverageStartGlobalValue(sessions);
        //if one of the values can not be calculated because no session has any record to do so, then the diff has no meaning
        if(averageStartGlobalValue != null && averageEndGlobalValue != null){
            averageDiffGlobalValue = averageEndGlobalValue - averageStartGlobalValue;
        }
        return averageDiffGlobalValue;
    }
////////////////////////////////////////
//Sessions MP3

    public static boolean areThereSessionsWithMp3(List<SessionRecord> sessions){
        if(sessions.size() != 0) {
            for (SessionRecord session : sessions) {
                if(!session.getGuideMp3().isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean areThereSessionsWithNoMp3(List<SessionRecord> sessions){
        if(sessions.size() != 0) {
            for (SessionRecord session : sessions) {
                if(session.getGuideMp3().isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getListOfMp3FileNames(List<SessionRecord> sessions, String noMp3Label) {
        if (sessions.size() != 0) {
            List<String> mp3Filenames = new ArrayList<>();
            //get all mp3 files' name
            for (SessionRecord session : sessions) {
                if(!session.getGuideMp3().isEmpty()){
                    String mp3name = session.getGuideMp3();
                    if(!mp3Filenames.contains(mp3name)){
                        mp3Filenames.add(mp3name);
                    }
                }
            }
            //alphabetically sort the mp3 filenames list
            Collections.sort(mp3Filenames, String.CASE_INSENSITIVE_ORDER);
            //prepend the "no mp3" label at the start of the list if necessary
            //index 0 is for sessions with NO MP3 - see MP3Stats.arrangeSessionsByDayOfMonth(List<SessionRecord> sessions, List<String> mp3Filenames)
            if(areThereSessionsWithNoMp3(sessions)){
                mp3Filenames.add(0, noMp3Label);
            }
            return mp3Filenames;
        }else{
            return null;
        }
    }

}
