package fr.shining_cat.everyday.analytics;

import java.util.ArrayList;
import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;

public abstract class DurationStats {

    //we will be arranging sessions on 5mn-duration-wide slices
    public static final int DURATION_STAT_SLICE_LENGTH = 300000;


////////////////////////////////////////
//returns a list of sessionRecords lists of 5mn-duration chunks, sessions are sorted into the list corresponding to their respective duration
//forcedLongestDuration must be given in case sessions does not contain the same number of sessions as the other graphs represented
// => otherwise we could end with arrangedSessionsList of a different size than the others because the longest session may not have the same length
    public static List<List<SessionRecord>> arrangeSessionsByDuration(List<SessionRecord> sessions, Long forcedLongestDuration){
        List<List<SessionRecord>> arrangedSessionsList= new ArrayList<>();
        long longestDuration;
        if(forcedLongestDuration == null) {
            longestDuration = GeneralStats.getLongestSession(sessions);
        }else{
            longestDuration = forcedLongestDuration;
        }
        int numberOfSlices =((int) longestDuration / DURATION_STAT_SLICE_LENGTH) + 1;
        //generating the empty lists to hold the different sessions
        for(int i = 0; i < numberOfSlices; i ++){
            ArrayList<SessionRecord> oneSliceSessionsList= new ArrayList<SessionRecord>();
            arrangedSessionsList.add(oneSliceSessionsList);
        }
        //sorting all sessions on their duration and assigning to corresponding list
        for(SessionRecord session : sessions){
            int whichSliceForSession = (int) session.getSessionRealDuration() / DURATION_STAT_SLICE_LENGTH;
            arrangedSessionsList.get(whichSliceForSession).add(session);
        }
        return arrangedSessionsList;
    }

}
