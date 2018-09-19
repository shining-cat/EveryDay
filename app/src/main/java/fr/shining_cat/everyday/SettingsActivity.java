package fr.shining_cat.everyday;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.data.EveryDaySessionsDataRepository;
import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.data.SessionsExporterAsync;
import fr.shining_cat.everyday.data.SessionsImportCSVParsingAsync;
import fr.shining_cat.everyday.preferences.ImportSessionsPreference;
import fr.shining_cat.everyday.utils.MiscUtils;


public class SettingsActivity extends AppCompatActivity
            implements EveryDaySessionsDataRepository.EveryDaySessionsRepoListener,
                        SessionsExporterAsync.SessionsExporterAsyncListener,
                        SessionsImportCSVParsingAsync.SessionsImportCSVParsingAsyncListener,
                        MiscUtils.OnMiscUtilsListener{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 789;

    private ProgressDialog mProgressDialog;
    private List<SessionRecord> mAllSessions;


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
//MiscUtils callbacks

    @Override
    public void onRequestPermissionApi24(String[] whichPermission) {
        ActivityCompat.requestPermissions(this, whichPermission, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    //Not actually a MiscUtils callbacks but this callback will be triggered as an answer after call to requestPermissions above
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //authorization granted, ask again for subfolder in DOCUMENTS
                    String exportCsvFolderName = getString(R.string.export_sessions_csv_folder_name);
                    MiscUtils.getSubFolderInPublicDocumentStorageDir(exportCsvFolderName, this);
                } else {
                    //nothing to do here, we do not store that user has denied authorisation, so he will be asked again if he tries to export the DB again, rather than counting on him to go in the device's settings to understand why the functionality is disabled
                }
                return;
            }
        }
    }

    @Override
    public void onPermissionToWriteOnExternalStorageOk(){
        //authorization granted, ask again for subfolder in DOCUMENTS
        String exportCsvFolderName = getString(R.string.export_sessions_csv_folder_name);
        MiscUtils.getSubFolderInPublicDocumentStorageDir(exportCsvFolderName, this);
    }

    @Override
    public void onSubFolderInPublicDocumentStorageDirObtained(File subFolderInPublicDocumentStorageDir) {
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.getDefault());
        String nowString =sdf.format(System.currentTimeMillis());
        String csvFileName = String.format(getString(R.string.export_sessions_csv_file_base_name), nowString);
        new SessionsExporterAsync(csvFileName, subFolderInPublicDocumentStorageDir, this).execute(mAllSessions);
    }
    @Override
    public void onSubFolderInPublicPicturesStorageDirObtained(File subFolderInPublicPicturesStorageDir) {}

////////////////////////////////////////
//EveryDaySessionsDataRepository callbacks
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
            mAllSessions = allSessions;
            //check external storage permissions before attempting to write
            MiscUtils.checkExternalAuthorizationAndAskIfNeeded(this, this);
        }
    }

    @Override
    public void onGetLatestRecordedSessionDateComplete(long latestSessionRecordedDate) {}

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
        Log.d(TAG, "onExportBitmapStarted:: mProgressDialog = " + mProgressDialog + " / mProgressDialog showing : " + mProgressDialog.isShowing());
    }

    @Override
    public void onExportSessionsProgressUpdate(int exported, int total) {
        //if process is fast enough (as it should), progress dialog will not be visible to user...
        mProgressDialog.setMax(total);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(exported);
        Log.d(TAG, "onExportBitmapProgressUpdate:: mProgressDialog = " + mProgressDialog + " / mProgressDialog showing : " + mProgressDialog.isShowing());
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
            case SessionsExporterAsync.ERROR_CREATING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Creating_Export_file));
                break;
            case SessionsExporterAsync.ERROR_WRITING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Writing_export_csv_file));
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
            case SessionsImportCSVParsingAsync.ERROR_NUMBER_PARSING:
                adBuilder.setMessage(getString(R.string.error_message_import_file_format_problem));
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