package fr.shining_cat.everyday;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import fr.shining_cat.everyday.data.EveryDayRepository;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.data.SessionsExporterAsync;
import fr.shining_cat.everyday.data.SessionsImportCSVParsingAsync;
import fr.shining_cat.everyday.preferences.ImportSessionsPreference;


public class SettingsActivity extends AppCompatActivity
            implements EveryDayRepository.EveryDayRepoListener,
                        SessionsExporterAsync.SessionsExporterAsyncListener,
                        SessionsImportCSVParsingAsync.SessionsImportCSVParsingAsyncListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

////////////////////////////////////////
//INCOMING INTENTS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                //ImportSessionsPreference sent intent.ACTION_GET_CONTENT for choosing csv file to import
                case ImportSessionsPreference.ACTIVITY_CHOSING_CSV_FILE_FOR_IMPORT:
                    //intent sent by ImportSessionsPreference => file has been chosen, parse and import with SessionsImportCSVParsingAsync
                    //WARNING : data coming back from Intent.ACTION_GET_CONTENT is not necessarily a File (but it can be which could lead to not detecting error at first) but has to be handled as a content Uri
                    Uri csvContentUri = data.getData();
                    Log.d(TAG, "onActivityResult::csvContentUri = " + csvContentUri);
                    prepareAndLaunchImportSessionsFromCsvFile(csvContentUri);
                    //job progress callbacks : onImportSessionsParsingProgressStarted, onImportSessionsParsingProgressUpdate, onImportSessionsParsingError, onImportSessionsParsingComplete
                    break;
            }
        }
    }

////////////////////////////////////////
//EveryDayRepository callbacks
    @Override
    public void onGetAllSessionsNotLiveComplete(List<SessionRecord> allSessions) {
        //The only case when we want to get a non-observable object containing all the data is for creating the export csv file
        // we don't want the source to eventually change while writing the file
        //=> launch the export and display a confirmation dialog when done
        if(allSessions.size()==0){
            AlertDialog.Builder builder =  new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.generic_string_ERROR));
            builder.setMessage(getString(R.string.pref_export_no_sessions_found_error_message));
            builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
            });
            builder.show();
        }else{
            new SessionsExporterAsync(this, this).execute(allSessions);
        }
    }

    @Override
    public void ondeleteOneSessionRecordComplete(int result) {
        Toast.makeText(this, R.string.delete_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void ondeleteAllSessionsRecordsComplete(int result) {
        Log.d(TAG, "onDialogClosed:: DELETING ALL DATAS !! - TASK COMPLETED : result = " + result);
        Toast.makeText(this, R.string.delete_all_sessions_task_completed_message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInsertOneSessionRecordComplete(long result) {
        Toast.makeText(this, R.string.insert_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInsertMultipleSessionsRecordsComplete(Long[] result) {
        if(mProgressDialog!=null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        Log.d(TAG, "onInsertMultipleSessionsRecordsComplete:: result = " + result.length);
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(getString(R.string.import_sessions_from_csv_task_completed_title));
        adBuilder.setPositiveButton(R.string.generic_string_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adBuilder.setMessage(String.format(getString(R.string.import_sessions_from_csv_task_completed_message), result.length));
        adBuilder.show();
    }

    @Override
    public void onUpdateOneSessionRecordComplete(int result) {
        Toast.makeText(this, R.string.update_one_session_task_completed_message, Toast.LENGTH_LONG).show();
    }

////////////////////////////////////////
//SessionsExporterAsync callbacks
    @Override
    public void onExportSessionsProgressStarted() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.export_sessions_to_csv_task_progressdialog_title));
        mProgressDialog.show();
        Log.d(TAG, "onExportSessionsProgressStarted:: mProgressDialog = " + mProgressDialog + " / mProgressDialog showing : " + mProgressDialog.isShowing());
    }

    @Override
    public void onExportSessionsProgressUpdate(int exported, int total) {
        //if process is fast enough (as it should), progress dialog will not be visible to user...
        mProgressDialog.setMax(total);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(exported);
        Log.d(TAG, "onExportSessionsProgressUpdate:: mProgressDialog = " + mProgressDialog + " / mProgressDialog showing : " + mProgressDialog.isShowing());
    }

    @Override
    public void onExportSessionsComplete(String result) {
        if(mProgressDialog!=null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(getString(R.string.generic_string_ERROR));
        adBuilder.setPositiveButton(R.string.generic_string_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        switch(result){
            case SessionsExporterAsync.ERROR_EXTERNAL_STORAGE_NOT_ACCESSIBLE:
                adBuilder.setMessage(getString(R.string.error_message_External_Storage_Not_Accessible));
                break;
            case SessionsExporterAsync.ERROR_ACCESSING_CREATING_EXPORT_FOLDER:
                adBuilder.setMessage(getString(R.string.error_message_Accessing_Creating_Export_Folder));
                break;
            case SessionsExporterAsync.ERROR_CREATING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Creating_Export_file));
                break;
            case SessionsExporterAsync.ERROR_WRITING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Writing_export_file));
                break;
            default:
                adBuilder.setTitle(getString(R.string.export_sessions_to_csv_task_completed_title));
                adBuilder.setMessage(String.format(getString(R.string.export_sessions_to_csv_task_completed_message), result));
        }
        adBuilder.show();
    }

////////////////////////////////////////
//got content Uri for csv file to import as multiple sessions, prepare parsing, then create and execute SessionsImportCSVParsingAsync:
    private void prepareAndLaunchImportSessionsFromCsvFile(Uri csvContentUri){
        boolean thereWasAnError = false;
        if(csvContentUri!=null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(csvContentUri);
                if(inputStream!=null) {
                    InputStreamReader csvToImportStreamReader = new InputStreamReader(new BufferedInputStream(inputStream));
                    new SessionsImportCSVParsingAsync(this).execute(csvToImportStreamReader);
                }else{
                    thereWasAnError = true;
                    Log.e(TAG, "prepareAndLaunchImportSessionsFromCsvFile::inputStream == null!!");
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "prepareAndLaunchImportSessionsFromCsvFile::could not create inputstream! -  " + e);
                thereWasAnError = true;
                e.printStackTrace();
            }
        }else{
            thereWasAnError = true;
            Log.e(TAG, "prepareAndLaunchImportSessionsFromCsvFile::csvContentUri == null!!");
        }
        if(thereWasAnError){
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
            adBuilder.setTitle(getString(R.string.generic_string_ERROR));
            adBuilder.setPositiveButton(R.string.generic_string_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adBuilder.setMessage(getString(R.string.error_message_cant_read_import_file));
            adBuilder.show();
        }
    }

////////////////////////////////////////
//SessionsImportCSVParsingAsync callbacks
    @Override
    public void onImportSessionsParsingProgressStarted() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.parse_sessions_from_csv_task_progressdialog_title));
        mProgressDialog.show();
        Log.d(TAG, "onImportSessionsProgressStarted:: mProgressDialog = " + mProgressDialog + " / mProgressDialog showing : " + mProgressDialog.isShowing());
    }

    @Override
    public void onImportSessionsParsingProgressUpdate(int parsed, int total) {
        //if process is fast enough (as it should), progress dialog will not be visible to user...
        mProgressDialog.setMax(total);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(parsed);
    }

    @Override
    public void onImportSessionsParsingError(String errorCode) {
        Log.e(TAG, "onImportSessionsParsingError:: errorCode = " + errorCode );
        if(mProgressDialog!=null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        adBuilder.setTitle(getString(R.string.generic_string_ERROR));
        adBuilder.setPositiveButton(R.string.generic_string_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        switch(errorCode){
            case SessionsImportCSVParsingAsync.ERROR_DATE_PARSING:
                adBuilder.setMessage(getString(R.string.error_message_date_parsing));
                break;
            case SessionsImportCSVParsingAsync.ERROR_IOE_PARSING_FILE:
                adBuilder.setMessage(getString(R.string.error_message_ioe_parsing_file));
                break;
            default:
                adBuilder.setMessage(getString(R.string.error_message_unknown));
        }
        adBuilder.show();
    }

    @Override
    public void onImportSessionsParsingComplete(List<SessionRecord> sessionRecordsToImportList) {
        Log.d(TAG, "onImportSessionsParsingComplete");
        if(mProgressDialog!=null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        //csv file has been parsed now insert sessionRecordsToImportList to DB
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.insertMultiple(sessionRecordsToImportList, this);
        //
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.import_sessions_from_csv_task_progressdialog_title));
        mProgressDialog.setIndeterminate(true);//didn't find any simple way to get progress info from DAO multiple insert
        mProgressDialog.show();
    }

}