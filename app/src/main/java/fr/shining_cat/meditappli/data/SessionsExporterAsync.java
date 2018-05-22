package fr.shining_cat.meditappli.data;

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

import fr.shining_cat.meditappli.R;
import fr.shining_cat.meditappli.SessionActivity;

public class SessionsExporterAsync extends AsyncTask <List, Integer, String>{

    public static final String ERROR_EXTERNAL_STORAGE_NOT_ACCESSIBLE = "error_code_External_Storage_Not_Accessible";
    public static final String ERROR_ACCESSING_CREATING_EXPORT_FOLDER = "error_code_Accessing_Creating_Export_Folder";
    public static final String ERROR_CREATING_EXPORT_FILE = "error_code_Creating_export_file";
    public static final String ERROR_WRITING_EXPORT_FILE = "error_code_Writing_export_file";


    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private SessionsExporterAsync.SessionsExporterAsyncListener mListener;
    private String mExportCsvFileName;

////////////////////////////////////////
//AsyncTask actually handling the job of exporting the database datas to a csv file
//with an interface to dispatch callbacks
//returns String with error_code or file canonical path is success
    public SessionsExporterAsync(Context context, SessionsExporterAsync.SessionsExporterAsyncListener listener){
        if (!(listener instanceof SessionsExporterAsync.SessionsExporterAsyncListener)) {
            throw new RuntimeException(listener.toString()+ " must implement SessionsExporterAsyncListener");
        }
        mListener = listener;
        //accessing resources
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH-mm-ss");
        String nowString =sdf.format(System.currentTimeMillis());
        mExportCsvFileName = context.getResources().getString(R.string.export_sessions_csv_file_name) + "_" + nowString + ".csv";
    }

    @Override
    protected void onPreExecute() {
        String state = Environment.getExternalStorageState();
        if(!Environment.MEDIA_MOUNTED.equals(state)){
            Log.e(TAG, "::onPreExecute:: external storage not available");
            cancel(true);
        }else{
            mListener.onExportSessionsProgressStarted();
        }

    }

    @Override
    protected String doInBackground(List... sessionRecordsList) {
        if(isCancelled()){
            Log.d(TAG, "::doInBackground::isCancelled => abort");
            return ERROR_EXTERNAL_STORAGE_NOT_ACCESSIBLE;
        }
        // Get the directory for the user's public documents directory.
        File documentsFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if(!documentsFolderPath.exists()){
            if(!documentsFolderPath.mkdir()){
                Log.e(TAG, "::doInBackground:: could neither access nor create DOCUMENTS folder on external storage");
                return ERROR_ACCESSING_CREATING_EXPORT_FOLDER;
            }
        }
        //create export csv file
        File exportCsvFile = new File(documentsFolderPath, mExportCsvFileName);
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
