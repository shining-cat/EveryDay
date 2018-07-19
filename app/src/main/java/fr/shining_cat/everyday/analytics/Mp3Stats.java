package fr.shining_cat.everyday.analytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;

public abstract class Mp3Stats {


////////////////////////////////////////
//returns a list of sessionRecords lists as long as the mp3filenames list (which may include the "nomp3 label"), sessions are sorted into the list corresponding to their respective getGuideMp3() field
    public static List<List<SessionRecord>> arrangeSessionsByMp3FileName(List<SessionRecord> sessions, List<String> mp3Filenames){
        List<List<SessionRecord>> arrangedSessionsList= new ArrayList<>();
        //generating the empty lists to hold the different sessions
        for(int i=0; i<mp3Filenames.size(); i++){
            ArrayList<SessionRecord> oneFileSessionsList= new ArrayList<SessionRecord>();
            arrangedSessionsList.add(oneFileSessionsList);
        }
        //sorting all sessions on getGuideMp3() and assigning to corresponding list
        for(SessionRecord session : sessions){
            if(session.getGuideMp3().isEmpty()){
                //index 0 is for sessions with NO MP3 - see GeneralStats.getListOfMp3FileNames(List<SessionRecord> sessions, String noMp3Label)
                arrangedSessionsList.get(0).add(session);
            }else {
                //add session to list at same index as its mp3 filename is in mp3Filenames
                String sessionMp3FileName = session.getGuideMp3();
                arrangedSessionsList.get(mp3Filenames.indexOf(sessionMp3FileName)).add(session);
            }
        }
        return arrangedSessionsList;
    }


}
