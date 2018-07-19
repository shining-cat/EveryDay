package fr.shining_cat.everyday.analytics;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;

public abstract class MonthStats {

////////////////////////////////////////
//returns a list of 31 sessionRecords lists, sessions are sorted into the list corresponding to their respective DAY_OF_MONTH
    public static List<List<SessionRecord>> arrangeSessionsByDayOfMonth(List<SessionRecord> sessions){
        List<List<SessionRecord>> arrangedSessionsList= new ArrayList<>();
        //generating the 31 days empty lists to hold the different sessions
        for(int i=0; i<31; i++){
            ArrayList<SessionRecord> oneDaySessionsList= new ArrayList<SessionRecord>();
            arrangedSessionsList.add(oneDaySessionsList);
        }
        //sorting all sessions on DAY_OF_MONTH and assigning to corresponding list
        for(SessionRecord session : sessions){
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(session.getStartTimeOfRecord());
            int sessionDay = (sessionCal.get(Calendar.DAY_OF_MONTH) - 1); // offsetting because first day of month for Calendar is 1 and not 0
            //Log.d("MonthStats", "::sessionDay = " + sessionDay);
            arrangedSessionsList.get(sessionDay).add(session);
        }
        return arrangedSessionsList;
    }

}
