package fr.shining_cat.everyday.analytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;

public abstract class DayStats {

////////////////////////////////////////
//returns a list of 24 sessionRecords lists, sessions are sorted into the list corresponding to their respective HOUR_OF_DAY
    public static List<List<SessionRecord>> arrangeSessionsByHourOfDay(List<SessionRecord> sessions){
        List<List<SessionRecord>> arrangedSessionsList= new ArrayList<>();
        //generating the 24 hours empty lists to hold the different sessions
        for(int i=0; i<24; i++){
            ArrayList<SessionRecord> oneHourSessionsList= new ArrayList<SessionRecord>();
            arrangedSessionsList.add(oneHourSessionsList);
        }
        //sorting all sessions on HOUR_OF_DAY and assigning to corresponding list
        for(SessionRecord session : sessions){
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(session.getStartTimeOfRecord());
            int sessionHour = sessionCal.get(Calendar.HOUR_OF_DAY);
            arrangedSessionsList.get(sessionHour).add(session);
        }
        return arrangedSessionsList;
    }

}
