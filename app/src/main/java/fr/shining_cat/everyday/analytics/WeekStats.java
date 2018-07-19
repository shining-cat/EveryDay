package fr.shining_cat.everyday.analytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;

public abstract class WeekStats {

////////////////////////////////////////
//returns a list of 7 sessionRecords lists, sessions are sorted into the list corresponding to their respective DAY_OF_WEEK
    public static List<List<SessionRecord>> arrangeSessionsByDayOfWeek(List<SessionRecord> sessions){
        List<List<SessionRecord>> arrangedSessionsList= new ArrayList<>();
        //generating the 7 days empty lists to hold the different sessions
        for(int i=0; i<7; i++){
            ArrayList<SessionRecord> oneDaySessionsList= new ArrayList<SessionRecord>();
            arrangedSessionsList.add(oneDaySessionsList);
        }
        //sorting all sessions on DAY_OF_WEEK and assigning to corresponding list
        for(SessionRecord session : sessions){
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(session.getStartTimeOfRecord());
            int sessionDay = offsetForMondayAsFirstDayOfWeek(sessionCal.get(Calendar.DAY_OF_WEEK));
            arrangedSessionsList.get(sessionDay).add(session);
        }
        return arrangedSessionsList;
    }

    //offsetting day of week index to re-arrange days order : starting on monday (index 0) ending on sunday (index 6)
    private static int offsetForMondayAsFirstDayOfWeek(int initialDayIndex){
        int newIndex;
        if(initialDayIndex == 1) {
            newIndex = 6;
        }else{
            newIndex = initialDayIndex - 2;
        }
        return newIndex;
    }

}
