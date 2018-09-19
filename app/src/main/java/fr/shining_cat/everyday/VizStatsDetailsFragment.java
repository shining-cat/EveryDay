package fr.shining_cat.everyday;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.BitmapToFileExporterAsync;
import fr.shining_cat.everyday.utils.MiscUtils;
import fr.shining_cat.everyday.widgets.ChartDisplay;


public class VizStatsDetailsFragment    extends Fragment
                                        implements  ChartDisplay.OnChartDisplayListener,
                                                    MiscUtils.OnMiscUtilsListener,
                                                    BitmapToFileExporterAsync.BitmapToFileExporterAsyncListener {


    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 789;

    private static final int EXPORT_CHART_PICTURE_QUALITY         = 50;


    protected List<SessionRecord> mAllSessions;
    protected List<List<SessionRecord>> mAllSessionsArranged;

    protected ChartDisplay mSessionsDurationChartViewHolder;
    protected ChartDisplay mSessionsNumberChartViewHolder;
    protected ChartDisplay mPausesCountChartViewHolder;
    protected ChartDisplay mPausesPerSessionChartViewHolder;
    protected ChartDisplay mStartingStoppingStreakSessionsCountChartViewHolder;
    protected ChartDisplay mUserStateAtSessionStartChartViewHolder;
    protected ChartDisplay mUserStateAtSessionEndChartViewHolder;
    protected ChartDisplay mUserStateDiffChartViewHolder;

    private Bitmap mChartBitmapToSaveAndShare;

    protected int mOldActiveValueIndex;

    public VizStatsDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mChartBitmapToSaveAndShare != null){
            mChartBitmapToSaveAndShare = null;
        }
    }

    public static VizStatsDetailsFragment newInstance() {
        VizStatsDetailsFragment fragment = new VizStatsDetailsFragment();
        return fragment;
    }

    public void setAllSessionsList(List<SessionRecord> allSessions) {
        mAllSessions = allSessions;
        //Log.d(TAG, "setAllSessionsList:: total number of sessions = " + mAllSessions.size());
        mAllSessionsArranged = arrangeSessionsOnDesiredFrame();
        if(mSessionsDurationChartViewHolder != null){
            setSessionsDurationChart();
        }
        if(mSessionsNumberChartViewHolder != null){
            setSessionsNumberChart();
        }
        if(mPausesCountChartViewHolder != null){
            setPausesCountChart();
        }
        if(mPausesPerSessionChartViewHolder != null){
            setPausesPerSessionChart();
        }
        if(mStartingStoppingStreakSessionsCountChartViewHolder != null){
            setStreaksStartStopSessionChart();
        }
        if(mUserStateAtSessionStartChartViewHolder != null){
            setUserStateAtSessionStartChart();
        }
        if(mUserStateAtSessionEndChartViewHolder != null){
            setUserStateAtSessionEndChart();
        }
        if(mUserStateDiffChartViewHolder != null){
            setUserStateDiffChart();
        }
    }

    protected List<List<SessionRecord>> arrangeSessionsOnDesiredFrame(){
        Log.e(TAG, "arrangeSessionsOnDesiredFrame::SHOULD BE OVERRIDDEN!!");
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView::SHOULD BE OVERRIDDEN!!");
        return null;
    }

    protected void setChartDisplayComponents(View rootView){
        //SESSIONS DURATION
        mSessionsDurationChartViewHolder = rootView.findViewById(R.id.sessions_duration_columnChartView_holder);
        //SESSIONS NUMBER
        mSessionsNumberChartViewHolder = rootView.findViewById(R.id.sessions_number_columnChartView_holder);
        //PAUSES NUMBER
        mPausesCountChartViewHolder = rootView.findViewById(R.id.pauses_number_columnChartView_holder);
        //PAUSES PER SESSION
        mPausesPerSessionChartViewHolder = rootView.findViewById(R.id.pauses_per_session_columnChartView_holder);
        //STARTING AND STOPPING STREAK SESSIONS COUNT
        mStartingStoppingStreakSessionsCountChartViewHolder = rootView.findViewById(R.id.start_stop_streak_sessions_count_columnChartView_holder);
        //USER STATE ON SESSION START
        mUserStateAtSessionStartChartViewHolder = rootView.findViewById(R.id.user_state_at_session_start_columnChartView_holder);
        //USER STATE ON SESSION END
        mUserStateAtSessionEndChartViewHolder = rootView.findViewById(R.id.user_state_at_session_end_columnChartView_holder);
        //USER STATE DIFF
        mUserStateDiffChartViewHolder = rootView.findViewById(R.id.user_state_diff_columnChartView_holder);
    }

    protected void setCharts(){
        if (mAllSessionsArranged != null) {
            setSessionsDurationChart();
            setSessionsNumberChart();
            setPausesCountChart();
            setPausesPerSessionChart();
            setStreaksStartStopSessionChart();
            setUserStateAtSessionStartChart();
            setUserStateAtSessionEndChart();
            setUserStateDiffChart();
        }
    }
////////////////////////////////////////
//ChartDisplay callbacks
    @Override
    public void onChartDisplaySelectedValue(ChartDisplay chartDisplay, int valueIndex) {
        synchronizeIndicationsDisplay(valueIndex);
    }

    @Override
    public void onChartDisplayUnselectedValue(ChartDisplay chartDisplay) {
        synchronizeUnlightening();
    }

    @Override
    public void onChartDisplayHelpButtonPressed(ChartDisplay chartDisplay, String helpMessage) {
        Log.d(TAG, "onChartDisplayHelpButtonPressed::chartDisplay = " + chartDisplay);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.chart_help_dialog_title_description));
        builder.setMessage(helpMessage);
        builder.setCancelable(true);
        builder.setNeutralButton(getString(R.string.generic_string_CLOSE), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void exportChartDisplayAsBitmapToFileAndShare(Bitmap chartBitmap) {
        if(chartBitmap != null) {
            mChartBitmapToSaveAndShare = chartBitmap;
            //check external storage permissions before attempting to write
            MiscUtils.checkExternalAuthorizationAndAskIfNeeded(getActivity(), this);
        }else{
            Log.e(TAG, "exportChartDisplayAsBitmapToFileAndShare::NO BITMAP TO SAVE!!");
        }
    }

////////////////////////////////////////
//MiscUtils callbacks
    @Override
    public void onRequestPermissionApi24(String[] whichPermission) {
        //ActivityCompat.requestPermissions(getActivity(), whichPermission, permissionRequestCode);
        requestPermissions(whichPermission, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onPermissionToWriteOnExternalStorageOk() {
        //authorization granted, ask again for subfolder in PICTURE FOLDER
        String exportPictureFolderName = getString(R.string.export_picture_folder_name);
        MiscUtils.getSubFolderInPublicAlbumStorageDir(exportPictureFolderName, this);
    }

    //Not actually a MiscUtils callbacks but this callback will be triggered as an answer after call to requestPermissions above
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //authorization granted, ask again for subfolder in PICTURE FOLDER
                    String exportPictureFolderName = getString(R.string.export_picture_folder_name);
                    MiscUtils.getSubFolderInPublicAlbumStorageDir(exportPictureFolderName, this);
                } else {
                    //nothing to do here, we do not store that user has denied authorisation, so he will be asked again if he tries to export a chart again, rather than counting on him to go in the device's settings to understand why the functionality is disabled
                }
                return;
            }
        }
    }

    @Override
    public void onSubFolderInPublicPicturesStorageDirObtained(File subFolderInPublicPicturesStorageDir) {
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HH-mm-ss", Locale.getDefault());
        String nowString =sdf.format(System.currentTimeMillis());
        String chartExportedFileName = String.format(getString(R.string.export_picture_file_base_name), nowString);
        new BitmapToFileExporterAsync(chartExportedFileName, subFolderInPublicPicturesStorageDir, EXPORT_CHART_PICTURE_QUALITY, this).execute(mChartBitmapToSaveAndShare);

    }

    @Override
    public void onSubFolderInPublicDocumentStorageDirObtained(File subFolderInPublicDocumentStorageDir) {}

////////////////////////////////////////
//BitmapToFileExporterAsync callbacks
    @Override
    public void onExportBitmapStarted() {
        Log.d(TAG, "onExportBitmapStarted");
    }

    @Override
    public void onExportBitmapComplete(String result) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
        adBuilder.setTitle(getString(R.string.generic_string_ERROR));
        adBuilder.setPositiveButton(R.string.generic_string_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        switch(result){
            case BitmapToFileExporterAsync.ERROR_CREATING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Creating_Export_file));
                adBuilder.show();
                break;
            case BitmapToFileExporterAsync.ERROR_WRITING_EXPORT_FILE:
                adBuilder.setMessage(getString(R.string.error_message_Writing_export_picture_file));
                adBuilder.show();
                break;
            default:
                Uri savedChartBitmapUri = Uri.fromFile(new File(result));
                //
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpg");
                shareIntent.putExtra(Intent.EXTRA_STREAM, savedChartBitmapUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image_intent_chooser_title)));
        }
    }

////////////////////////////////////////
//synchronizing highlight
    private void synchronizeIndicationsDisplay(int indexInDataSet) {
        //Log.d(TAG, "synchronizeIndicationsDisplay:: indexInDataSet = " + indexInDataSet + " / mOldActiveValueIndex = " + mOldActiveValueIndex);
        if(indexInDataSet == mOldActiveValueIndex){
            synchronizeUnlightening();
        }else {
            mOldActiveValueIndex = indexInDataSet;
            //every chart here is built on the same 24-hours array so we can target entries by their respective dataset index
            mSessionsDurationChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mSessionsNumberChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mPausesCountChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mPausesPerSessionChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mStartingStoppingStreakSessionsCountChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateAtSessionStartChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateAtSessionEndChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
            mUserStateDiffChartViewHolder.displayDataForSelectedIndex(indexInDataSet);
        }
    }

    private void synchronizeUnlightening() {
        Log.d(TAG, "synchronizeUnlightening");
        mOldActiveValueIndex = -1;
        //
        mSessionsDurationChartViewHolder.hideDataNoneSelected();
        mSessionsNumberChartViewHolder.hideDataNoneSelected();
        mPausesCountChartViewHolder.hideDataNoneSelected();
        mPausesPerSessionChartViewHolder.hideDataNoneSelected();
        mStartingStoppingStreakSessionsCountChartViewHolder.hideDataNoneSelected();
        mUserStateAtSessionStartChartViewHolder.hideDataNoneSelected();
        mUserStateAtSessionEndChartViewHolder.hideDataNoneSelected();
        mUserStateDiffChartViewHolder.hideDataNoneSelected();
    }

////////////////////////////////////////
//sessions duration graph
    protected void setSessionsDurationChart() {
            Log.e(TAG, "setSessionsDurationChart::SHOULD BE OVERRIDDEN!!");
        }


////////////////////////////////////////
//sessions number graph
    protected void setSessionsNumberChart() {
            Log.e(TAG, "setSessionsNumberChart::SHOULD BE OVERRIDDEN!!");
        }

////////////////////////////////////////
//Pauses count graph
    protected void setPausesCountChart() {
            Log.e(TAG, "setPausesCountChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//Pauses per session graph
    protected void setPausesPerSessionChart() {
        Log.e(TAG, "setPausesPerSessionChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//Starting and stopping streaks graph
    protected void setStreaksStartStopSessionChart() {
        Log.e(TAG, "setStreaksStartStopSessionChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session start graph
    protected void setUserStateAtSessionStartChart() {
        Log.e(TAG, "setUserStateAtSessionStartChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session end graph
    protected void setUserStateAtSessionEndChart() {
        Log.e(TAG, "setUserStateAtSessionEndChart::SHOULD BE OVERRIDDEN!!");
    }

////////////////////////////////////////
//User state at session end graph
    protected void setUserStateDiffChart() {
        Log.e(TAG, "setUserStateDiffChart::SHOULD BE OVERRIDDEN!!");
    }


}