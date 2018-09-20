package fr.shining_cat.everyday.data;

import android.os.AsyncTask;
import android.util.Log;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class SessionsImportCSVParsingAsync extends AsyncTask <InputStreamReader, Integer, List<SessionRecord>>{

    public static final String ERROR_IOE_PARSING_FILE = "error_ioe_parsing_file";
    public static final String ERROR_DATE_PARSING = "error_date_parsing";
    public static final String ERROR_NUMBER_PARSING = "error_number_parsing";
    public static final String ERROR_UNKNOWN = "error_unwknown_parsing";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private SessionsImportCSVParsingAsyncListener mListener; // will be SettingsActivity
    private String mErrorCodeWhileParsingCsvFile;


////////////////////////////////////////
//AsyncTask actually handling the job of parsing the imported csv file. Gets an InputStreamReader from SettingsActivity with the csv content
//DOES NOT ACTUALLY HANDLE THE IMPORT IN DB => returns a List<SessionRecord> (if no error)
//with an interface to dispatch callbacks
//parsing is done with com.opencsv
//First line of the csv file is ignored (for headers)
//quote character is "
//separation character is ;
//structure and format of the csv file are specified in ImportSessionsPreference dialog message. see : R.string.pref_import_all_sessions_dialog_message
//NOT SET value for mood is 0
    public SessionsImportCSVParsingAsync(SessionsImportCSVParsingAsyncListener listener){
        if (!(listener instanceof SessionsImportCSVParsingAsyncListener)) {
            throw new RuntimeException(listener.toString()+ " must implement SessionsImportCSVParsingAsyncListener");
        }
        mListener = listener;
        mErrorCodeWhileParsingCsvFile = ERROR_UNKNOWN;
    }

    @Override
    protected void onPreExecute() {
        mListener.onImportSessionsParsingProgressStarted();
    }

    @Override
    protected List<SessionRecord> doInBackground(InputStreamReader... csvToImportInputStreamReader) {
        CSVParser csvParser = new CSVParserBuilder()
                .withIgnoreLeadingWhiteSpace(true)
                .withQuoteChar('"')
                .withSeparator(';')
                .build();
        CSVReader reader = new CSVReaderBuilder(csvToImportInputStreamReader[0])
                .withCSVParser(csvParser)
                .withSkipLines(1)
                .build();
        try {
            List<String[]> records = reader.readAll();
            Iterator<String[]> iterator = records.iterator();
            List<SessionRecord> sessionRecordsToImportList = new ArrayList<>();
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            int recordsToParseNumber = records.size();
            int parsedRecords = 0;
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                //WARNING : for simplicity and clarity (for the user), the order of the columns in the CSV is NOT the SessionRecord one's
                try {
                    long startTimeOfRecord = (sdf.parse(record[0])).getTime();
                    long endTimeOfRecord  = (sdf.parse(record[1])).getTime();
                    // in case duration field is empty, set duration to difference between start and end time
                    long sessionRealDuration = record[2].isEmpty()? endTimeOfRecord - startTimeOfRecord : Integer.parseInt(record[2])*60000; //user is required to give value in minutes (same as export)
                    String notes = record[3];
                    //0 is for "NOT SET", user could have nothing in field, or the csv file could have been generated by our app, which inserts the string 3NOT SET" for unset fields
                    int startBodyValue      = (record[4].isEmpty() || record[4].equals("NOT SET"))? 0 : Integer.parseInt(record[4]);
                    int startThoughtsValue  = (record[5].isEmpty() || record[5].equals("NOT SET"))? 0 : Integer.parseInt(record[5]);
                    int startFeelingsValue  = (record[6].isEmpty() || record[6].equals("NOT SET"))? 0 : Integer.parseInt(record[6]);
                    int startGlobalValue    = (record[7].isEmpty() || record[7].equals("NOT SET"))? 0 : Integer.parseInt(record[7]);
                    //
                    int endBodyValue        = (record[8].isEmpty() || record[8].equals("NOT SET"))? 0 : Integer.parseInt(record[8]);
                    int endThoughtsValue    = (record[9].isEmpty() || record[9].equals("NOT SET"))? 0 : Integer.parseInt(record[9]);
                    int endFeelingsValue    = (record[10].isEmpty() || record[10].equals("NOT SET"))? 0 : Integer.parseInt(record[10]);
                    int endGlobalValue      = (record[11].isEmpty() || record[11].equals("NOT SET"))? 0 : Integer.parseInt(record[11]);
                    //
                    int pausesCount = record[12].isEmpty()? 0 : Integer.parseInt(record[12]);
                    //
                    int realDurationVsPlanned = 0; //default case is "EQUAL"
                    if(record[13].equals("LESS")){
                        realDurationVsPlanned = -1;
                    }else if(record[13].equals("MORE")){
                        realDurationVsPlanned = +1;
                    }
                    //
                    String guideMp3 = record[14].isEmpty()? "" : record[14];
                    //
                    SessionRecord sessionRecordToAdd = new SessionRecord(startTimeOfRecord,
                                                                        startBodyValue,
                                                                        startThoughtsValue,
                                                                        startFeelingsValue,
                                                                        startGlobalValue,
                                                                        //
                                                                        endTimeOfRecord,
                                                                        endBodyValue,
                                                                        endThoughtsValue,
                                                                        endFeelingsValue,
                                                                        endGlobalValue,
                                                                        //
                                                                        notes,
                                                                        sessionRealDuration,
                                                                        pausesCount,
                                                                        realDurationVsPlanned,
                                                                        guideMp3);
                    sessionRecordsToImportList.add(sessionRecordToAdd);
                    parsedRecords +=1;
                    publishProgress(parsedRecords, recordsToParseNumber);
                } catch (ParseException pe) {
                    //error while parsing one of the records : report error and abort import
                    Log.e(TAG, "ParseException: " + pe.toString());
                    pe.printStackTrace();
                    mErrorCodeWhileParsingCsvFile = ERROR_DATE_PARSING;
                    return null;
                }catch (NumberFormatException nfe){
                    //error while parsing one of the records : report error and abort import
                    Log.e(TAG, "ParseException: " + nfe.toString());
                    nfe.printStackTrace();
                    mErrorCodeWhileParsingCsvFile = ERROR_NUMBER_PARSING;
                    return null;
                }
            }
            return sessionRecordsToImportList;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            e.printStackTrace();
            mErrorCodeWhileParsingCsvFile = ERROR_IOE_PARSING_FILE;
            return null;
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onImportSessionsParsingProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(List<SessionRecord> sessionRecordsToImportList) {
        if (sessionRecordsToImportList != null) {
            mListener.onImportSessionsParsingComplete(sessionRecordsToImportList);
        }else{ //there was an error parsing the csv
            mListener.onImportSessionsParsingError(mErrorCodeWhileParsingCsvFile);
        }
    }

////////////////////////////////////////
//Listener interface
    public interface SessionsImportCSVParsingAsyncListener {
        void onImportSessionsParsingProgressStarted();
        void onImportSessionsParsingProgressUpdate(int exported, int total);
        void onImportSessionsParsingError(String errorCode);
        void onImportSessionsParsingComplete(List<SessionRecord> sessionRecordsToImportList);
    }
}