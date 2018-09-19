package fr.shining_cat.everyday.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.R;

public class SessionsExporterAsync  extends AsyncTask <List, Integer, String>{

    public static final String ERROR_CREATING_EXPORT_FILE = "error_code_Creating_export_file";
    public static final String ERROR_WRITING_EXPORT_FILE = "error_code_Writing_export_file";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private SessionsExporterAsync.SessionsExporterAsyncListener mListener;
    private File mExportCsvFolder;
    private String mExportCsvFileName;

////////////////////////////////////////
//AsyncTask actually handling the job of exporting the database datas to a csv file
//with an interface to dispatch callbacks
//returns String with error_code or file canonical path is success
    public SessionsExporterAsync(String csvFileName, File exportCsvFolderName, SessionsExporterAsync.SessionsExporterAsyncListener listener){
        if (!(listener instanceof SessionsExporterAsync.SessionsExporterAsyncListener)) {
            throw new RuntimeException(listener.toString()+ " must implement BitmapToFileExporterAsyncListener");
        }
        mListener = listener;
        mExportCsvFolder = exportCsvFolderName;
        mExportCsvFileName = csvFileName;
    }

    @Override
    protected void onPreExecute() {
        mListener.onExportSessionsProgressStarted();
    }


    @Override
    protected String doInBackground(List... sessionRecordsList) {
        //create export csv file
        File exportCsvFile = new File(mExportCsvFolder, mExportCsvFileName);
        try {
            exportCsvFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Export File not created");
            e.printStackTrace();
            return ERROR_CREATING_EXPORT_FILE;
        }
        //write export
        try {
            FileWriter csvFileWriter = new FileWriter(exportCsvFile);
            CSVWriter openCsvFileWriter = new CSVWriter(csvFileWriter, ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            //check that every object in given List is actually a SessionRecord, put it in a new not generic list
            List<SessionRecord> sessionsRecords = new ArrayList<SessionRecord>();
            for(Object entry : sessionRecordsList[0]){
                if(entry instanceof SessionRecord){
                    sessionsRecords.add((SessionRecord) entry);
                }
            }
            if(sessionRecordsList[0].size() != sessionsRecords.size()){
                Log.e(TAG, "doInBackground::casting given sessions to SessionRecord lost items!");
            }
            int totalSessionsNumber = sessionsRecords.size();
            int records = 0;
            //write headers
            openCsvFileWriter.writeNext(SessionRecord.getSessionRecordHeaders());
            //loop on sessions records and write each line in the csv
            for (SessionRecord sessionRecord:sessionsRecords) {
                openCsvFileWriter.writeNext(sessionRecord.getSessionRecordArray());
                records += 1;
                publishProgress(records, totalSessionsNumber);
            }
            //
            openCsvFileWriter.close();
            //success : return export csv file name and path
            return exportCsvFile.getCanonicalPath();
        }catch (IOException e) {
            Log.e(TAG, "Could not write to Export File");
            e.printStackTrace();
            return ERROR_WRITING_EXPORT_FILE;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onExportSessionsProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(String result) {
        mListener.onExportSessionsComplete(result);
    }



////////////////////////////////////////
//Listener interface
    public interface SessionsExporterAsyncListener {
        void onExportSessionsProgressStarted();
        void onExportSessionsProgressUpdate(int exported, int total);
        void onExportSessionsComplete(String result);
    }
}
