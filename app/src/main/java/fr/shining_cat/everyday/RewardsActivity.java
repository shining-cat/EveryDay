package fr.shining_cat.everyday;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.shining_cat.everyday.data.EveryDayRewardsDataRepository;
import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.data.RewardViewModel;
import fr.shining_cat.everyday.dialogs.DialogFragmentEditReward;
import fr.shining_cat.everyday.utils.BitmapToFileExporterAsync;
import fr.shining_cat.everyday.utils.MiscUtils;

public class RewardsActivity    extends BaseThemedActivity
                                implements  RewardsCollectionListAdapter.RewardsListAdapterListener,
                                            RewardDetailsCardFragment.RewardDetailsCardFragmentListener,
                                            MiscUtils.OnMiscUtilsListener,
                                            BitmapToFileExporterAsync.BitmapToFileExporterAsyncListener,
                                            DialogFragmentEditReward.DialogFragmentEditRewardListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 789;
    private static final int EXPORT_REWARD_CARD_PICTURE_QUALITY         = 90;

    private final String SCREEN_REWARDS_LIST_VIEW = "rewards list view fragment screen";
    private final String SCREEN_REWARD_DETAILS_VIEW = "rewards details view fragment screen";

    private String mCurrentScreen;
    private RewardDetailsViewPagerFragment mRewardDetailsViewPagerFragment;
    private Reward mEdittingReward;
    private Bitmap mRewardCardBitmapToSaveAndShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        showRewardsListView();
    }
////////////////////////////////////////
//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_rewards, menu);
        setTitle(getString(R.string.rewards));
        switch (mCurrentScreen){
            case SCREEN_REWARDS_LIST_VIEW :

                break;
            case SCREEN_REWARD_DETAILS_VIEW :

                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home://overriding up button behaviour to set different destination wether we're in top-level fragment or in low-level
                if(overrideNavigateBackAndUp()){
                    return true;
                }else{
                    return super.onOptionsItemSelected(item);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

////////////////////////////////////////
//overriding BACK and UP navigation to manage inner fragments and update options menu items
    @Override
    public void onBackPressed() {
        if(!overrideNavigateBackAndUp()){
            super.onBackPressed();
        }
    }

    private boolean overrideNavigateBackAndUp(){
        invalidateOptionsMenu();
        switch (mCurrentScreen){
            case SCREEN_REWARD_DETAILS_VIEW :
                showRewardsListView();
                return true;
            case SCREEN_REWARDS_LIST_VIEW:
            default:
                return false;
        }
    }

////////////////////////////////////////
//DIFFERENT SCREENS (fragments : RewardsListFragment, RewardDetailsViewPagerFragment)
    private void showRewardsListView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RewardsListFragment rewardsListFragment = RewardsListFragment.newInstance();
        fragmentTransaction.replace(R.id.rewards_activity_fragments_holder, rewardsListFragment, RewardsListFragment.VIEW_REWARDS_LIST_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_REWARDS_LIST_VIEW;
        invalidateOptionsMenu();
    }
    //
    private void showRewardDetailsView(Reward clickedReward){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mRewardDetailsViewPagerFragment != null) {
            mRewardDetailsViewPagerFragment = null;
        }
        mRewardDetailsViewPagerFragment = RewardDetailsViewPagerFragment.newInstance();
        mRewardDetailsViewPagerFragment.setStartingRewardDetailsWithReward(clickedReward);
        fragmentTransaction.replace(R.id.rewards_activity_fragments_holder, mRewardDetailsViewPagerFragment, RewardDetailsViewPagerFragment.VIEW_PAGER_REWARD_DETAILS_FRAGMENT_TAG);
        fragmentTransaction.commit();
        mCurrentScreen = SCREEN_REWARD_DETAILS_VIEW;
        invalidateOptionsMenu();
    }

////////////////////////////////////////
//RewardsCollectionListAdapter callbacks for list items interactions
    @Override
    public void onRewardsListAdapterClickOnReward(Reward clickedReward) {
        showRewardDetailsView(clickedReward);
    }

    @Override
    public void onRewardsListAdapterLongClickOnReward(Reward clickedReward) {
        editRewardDetails(clickedReward);
    }

////////////////////////////////////////
//RewardDetailsCardFragment callbacks
    @Override
    public void onRewardDetailsCardFragmentExportRewardDetailCardAsBitmapToFileAndShare(Bitmap rewardCardBitmap) {
        if(rewardCardBitmap != null) {
            mRewardCardBitmapToSaveAndShare = rewardCardBitmap;
            //check external storage permissions before attempting to write
            MiscUtils.checkExternalAuthorizationAndAskIfNeeded(this, this);
        }else{
            Log.e(TAG, "exportChartDisplayAsBitmapToFileAndShare::NO BITMAP TO SAVE!!");
        }
    }

    @Override
    public void onRewardDetailsCardFragmentEditRewardDetails(Reward reward) {
        editRewardDetails(reward);
    }

////////////////////////////////////////
//Edit Reward Details

    private void editRewardDetails(Reward reward){
        mEdittingReward = reward;
        String rewardName = reward.getRewardName();
        if(rewardName.isEmpty()){
            rewardName = reward.getRewardCode(); //if user has deleted reward name, reward code will be proposed by default (but empty name can be chosen and used)
        }
        int rewardLegsColor = Color.parseColor(reward.getRewardLegsColor());
        int rewardBodyColor = Color.parseColor(reward.getRewardBodyColor());
        int rewardArmsColor = Color.parseColor(reward.getRewardArmsColor());
        FragmentManager fm = getSupportFragmentManager();
        DialogFragmentEditReward dialogFragmentEditReward = DialogFragmentEditReward.newInstance(rewardName, rewardLegsColor, rewardBodyColor, rewardArmsColor);
        dialogFragmentEditReward.show(fm, DialogFragmentEditReward.DIALOG_FRAGMENT_EDIT_REWARD_TAG);
    }

////////////////////////////////////////
//DialogFragmentEditReward callbacks

    @Override
    public void onEditRewardValidate(String name, int colorLegs, int colorBody, int colorArms) {
        mEdittingReward.setRewardName(name);
        mEdittingReward.setRewardLegsColor(colorLegs);
        mEdittingReward.setRewardBodyColor(colorBody);
        mEdittingReward.setRewardArmsColor(colorArms);
        RewardViewModel rewardViewModel = ViewModelProviders.of(this).get(RewardViewModel.class);
        rewardViewModel.update(mEdittingReward, everyDayRewardsRepoListener);
    }

    @Override
    public void onEditRewardCancel() {
        Toast.makeText(this, getString(R.string.edit_reward_cancel_message), Toast.LENGTH_SHORT).show();
    }

    private void notifyRewardEditComplete(){
        Toast.makeText(this, getString(R.string.edit_reward_confirm_message), Toast.LENGTH_SHORT).show();
    }

    EveryDayRewardsDataRepository.EveryDayRewardsRepoListener everyDayRewardsRepoListener = new EveryDayRewardsDataRepository.EveryDayRewardsRepoListener(){
        @Override
        public void onUpdateOneRewardComplete(int result) {
            mEdittingReward = null;
            notifyRewardEditComplete();
        }

        @Override
        public void onUpdateMultipleRewardComplete(int result) {}
        @Override
        public void onGetNumberOfRowsComplete(Integer result) {}
        @Override
        public void onGetNumberOfPossibleRewardsForLevelComplete(int levelQueried, Integer result) {}
        @Override
        public void onGetNumberOfActiveRewardsForLevelComplete(int levelQueried, Integer result) {}
        @Override
        public void onGetNumberOfEscapedRewardsForLevelAsyncTaskComplete(int levelQueried, Integer result) {}
        @Override
        public void onInsertOneRewardComplete(long result) {}
        @Override
        public void onInsertMultipleRewardsComplete(Long[] result) {}
        @Override
        public void ondeleteOneRewardComplete(int result) {}
        @Override
        public void ondeleteAllRewardComplete(int result) {}
    };

////////////////////////////////////////
//MiscUtils callbacks
    @Override
    public void onRequestPermissionApi24(String[] whichPermission) {
        ActivityCompat.requestPermissions(this, whichPermission, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
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
        String rewardCardExportedFileName = String.format(getString(R.string.export_picture_file_base_name), nowString);
        new BitmapToFileExporterAsync(rewardCardExportedFileName, subFolderInPublicPicturesStorageDir, EXPORT_REWARD_CARD_PICTURE_QUALITY, this).execute(mRewardCardBitmapToSaveAndShare);

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
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
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

}