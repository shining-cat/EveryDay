package fr.shining_cat.everyday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.CardAdapter;
import fr.shining_cat.everyday.utils.TimeOperations;


public class VizSessionDetailsCardFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SESSION_DETAILS_FRAGMENT_TAG = "session_details_Fragment-tag";

    private final int[] smileysResourceId = {R.drawable.smiley_not_set, R.drawable.smiley_angry, R.drawable.smiley_bad, R.drawable.smiley_ok, R.drawable.smiley_good, R.drawable.smiley_great};

    private View mRootView;
    private SessionRecord mSessionRecord;
    private CardView mCardView;

////////////////////////////////////////
// this fragment is used by the VizSessionDetailsCardFragmentPagerAdapter. It shows all the details of one session
    public VizSessionDetailsCardFragment() {
        // Required empty public constructor
    }

    public static VizSessionDetailsCardFragment newInstance() {
        return new VizSessionDetailsCardFragment();
    }

////////////////////////////////////////
//getting the session data
    public void setContent(SessionRecord currentSession) {
        mSessionRecord = currentSession;
    }

////////////////////////////////////////
//setting the view containing the data
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_viz_session_details, container, false);
        mCardView = mRootView.findViewById(R.id.viz_session_details_cardview);
        mCardView.setMaxCardElevation(mCardView.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);
        //
        if(mSessionRecord!=null){
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            //
            TextView startDateTxtvw = mRootView.findViewById(R.id.start_date_txtvw);
            //Log.d(TAG, "onCreateView::startDateTxtvw = " + startDateTxtvw);
            startDateTxtvw.setText(sdf.format(mSessionRecord.getStartTimeOfRecord()));
            Log.d(TAG, "onCreateView::StartTimeOfRecord = " + mSessionRecord.getStartTimeOfRecord());
            TextView startTimeTxtvw = mRootView.findViewById(R.id.start_time_txtvw);
            startTimeTxtvw.setText(tdf.format(mSessionRecord.getStartTimeOfRecord()));
            TextView endDateTxtvw = mRootView.findViewById(R.id.end_date_txtvw);
            endDateTxtvw.setText(sdf.format(mSessionRecord.getEndTimeOfRecord()));
            TextView endTimeTxtvw = mRootView.findViewById(R.id.end_time_txtvw);
            endTimeTxtvw.setText(tdf.format(mSessionRecord.getEndTimeOfRecord()));
            //
            TextView durationTxtvw = mRootView.findViewById(R.id.duration_txtvw);
            durationTxtvw.setText(TimeOperations.convertMillisecondsToHoursAndMinutesString(
                    mSessionRecord.getSessionRealDuration(),
                    getString(R.string.generic_string_SHORT_HOURS),
                    getString(R.string.generic_string_SHORT_MINUTES),
                    false));
            //
            TextView pauseCountLabel = mRootView.findViewById(R.id.pauses_count_label);
            TextView pausesCountTxtvw = mRootView.findViewById(R.id.pauses_count_txtvw);
            if(mSessionRecord.getPausesCount() == 0){
                pauseCountLabel.setVisibility(View.GONE);
                pausesCountTxtvw.setVisibility(View.GONE);
            }else {
                pauseCountLabel.setVisibility(View.VISIBLE);
                pausesCountTxtvw.setVisibility(View.VISIBLE);
                pausesCountTxtvw.setText(String.valueOf(mSessionRecord.getPausesCount()));
            }
            //
            TextView realDurationVsPlannedTxtvw = mRootView.findViewById(R.id.real_duration_vs_planned_duration_txtvw);
            realDurationVsPlannedTxtvw.setVisibility(View.VISIBLE);
            if(mSessionRecord.getRealDurationVsPlanned() < 0){
                realDurationVsPlannedTxtvw.setText(getString(R.string.real_duration_vs_planned_LESS));
            }else if(mSessionRecord.getRealDurationVsPlanned() > 0){
                realDurationVsPlannedTxtvw.setText(getString(R.string.real_duration_vs_planned_MORE));
            }else{
                realDurationVsPlannedTxtvw.setVisibility(View.GONE);
            }
            //
            TextView guideMp3Label = mRootView.findViewById(R.id.guide_mp3_label);
            TextView guideMp3Txtvw = mRootView.findViewById(R.id.guide_mp3_txtvw);
            if(mSessionRecord.getGuideMp3().isEmpty()){
                guideMp3Label.setVisibility(View.GONE);
                guideMp3Txtvw.setVisibility(View.GONE);
            }else{
                guideMp3Label.setVisibility(View.VISIBLE);
                guideMp3Txtvw.setVisibility(View.VISIBLE);
                guideMp3Txtvw.setText(mSessionRecord.getGuideMp3());
            }
            //
            String tempEnd;
            //
            ImageView bodyStartValueImgVw = mRootView.findViewById(R.id.body_array_start_value);
            int startBodyValue = mSessionRecord.getStartBodyValue();
            int startBodyValueResId = smileysResourceId[startBodyValue];
            bodyStartValueImgVw.setImageResource(startBodyValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String startBodyValueContDesc;
            if(startBodyValue == 0) {//means Value not set
                startBodyValueContDesc = String.format(getString(R.string.body_state_start_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(startBodyValue) + getString(R.string.state_set_content_description_complement);
                startBodyValueContDesc = String.format(getString(R.string.body_state_start_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::startBodyValue = " + startBodyValue + " / startBodyValueContDesc = " + startBodyValueContDesc);
            bodyStartValueImgVw.setContentDescription(startBodyValueContDesc);
            //
            ImageView bodyEndValueImgVw = mRootView.findViewById(R.id.body_array_end_value);
            int endBodyValue = mSessionRecord.getEndBodyValue();
            int endBodyValueResId = smileysResourceId[endBodyValue];
            bodyEndValueImgVw.setImageResource(endBodyValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String endBodyValueContDesc;
            if(endBodyValue == 0) {//means Value not set
                endBodyValueContDesc = String.format(getString(R.string.body_state_end_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(endBodyValue) + getString(R.string.state_set_content_description_complement);
                endBodyValueContDesc = String.format(getString(R.string.body_state_end_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::endBodyValue = " + endBodyValue + " / endBodyValueContDesc = " + endBodyValueContDesc);
            bodyEndValueImgVw.setContentDescription(endBodyValueContDesc);
            //
            ImageView thoughtsStartValueImgVw = mRootView.findViewById(R.id.thoughts_array_start_value);
            int startThoughtsValue = mSessionRecord.getStartThoughtsValue();
            int startThoughtsValueResId = smileysResourceId[startThoughtsValue];
            thoughtsStartValueImgVw.setImageResource(startThoughtsValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String startThoughtsValueContDesc;
            if(startThoughtsValue == 0) {//means Value not set
                startThoughtsValueContDesc = String.format(getString(R.string.thoughts_state_start_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(startThoughtsValue) + getString(R.string.state_set_content_description_complement);
                startThoughtsValueContDesc = String.format(getString(R.string.thoughts_state_start_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::startThoughtsValue = " + startThoughtsValue + " / startThoughtsValueContDesc = " + startThoughtsValueContDesc);
            thoughtsStartValueImgVw.setContentDescription(startThoughtsValueContDesc);
            //
            ImageView thoughtsEndValueImgVw = mRootView.findViewById(R.id.thoughts_array_end_value);
            int endThoughtsValue = mSessionRecord.getEndThoughtsValue();
            int endThoughtsValueResId = smileysResourceId[endThoughtsValue];
            thoughtsEndValueImgVw.setImageResource(endThoughtsValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String endThoughtsValueResIdContDesc;
            if(endThoughtsValue == 0) {//means Value not set
                endThoughtsValueResIdContDesc = String.format(getString(R.string.thoughts_state_end_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(endThoughtsValue) + getString(R.string.state_set_content_description_complement);
                endThoughtsValueResIdContDesc = String.format(getString(R.string.thoughts_state_end_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::endThoughtsValue = " + endThoughtsValue + " / endThoughtsValueResIdContDesc = " + endThoughtsValueResIdContDesc);
            thoughtsEndValueImgVw.setContentDescription(endThoughtsValueResIdContDesc);
            //
            ImageView feelingsStartValueImgVw = mRootView.findViewById(R.id.feelings_array_start_value);
            int startFeelingsValue = mSessionRecord.getStartFeelingsValue();
            int startFeelingsValueResId = smileysResourceId[startFeelingsValue];
            feelingsStartValueImgVw.setImageResource(startFeelingsValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String startFeelingsValueContDesc;
            if(startFeelingsValue == 0) {//means Value not set
                startFeelingsValueContDesc = String.format(getString(R.string.feelings_state_start_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(startFeelingsValue) + getString(R.string.state_set_content_description_complement);
                startFeelingsValueContDesc = String.format(getString(R.string.feelings_state_start_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::startFeelingsValue = " + startFeelingsValue + " / startFeelingsValueContDesc = " + startFeelingsValueContDesc);
            feelingsStartValueImgVw.setContentDescription(startFeelingsValueContDesc);
            //
            ImageView feelingsEndValueImgVw = mRootView.findViewById(R.id.feelings_array_end_value);
            int feelingsEndValue = mSessionRecord.getEndFeelingsValue();
            int feelingsEndValueResId = smileysResourceId[feelingsEndValue];
            feelingsEndValueImgVw.setImageResource(feelingsEndValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String feelingsEndValueContDesc;
            if(feelingsEndValue == 0) {//means Value not set
                feelingsEndValueContDesc = String.format(getString(R.string.feelings_state_end_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(feelingsEndValue) + getString(R.string.state_set_content_description_complement);
                feelingsEndValueContDesc = String.format(getString(R.string.feelings_state_end_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::feelingsEndValue = " + feelingsEndValue + " / feelingsEndValueContDesc = " + feelingsEndValueContDesc);
            feelingsEndValueImgVw.setContentDescription(feelingsEndValueContDesc);
            //
            ImageView globalStartValueImgVw = mRootView.findViewById(R.id.global_array_start_value);
            int globalStartValue = mSessionRecord.getStartGlobalValue();
            int globalStartValueResId = smileysResourceId[globalStartValue];
            globalStartValueImgVw.setImageResource(globalStartValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String globalStartValueContDesc;
            if(globalStartValue == 0) {//means Value not set
                globalStartValueContDesc = String.format(getString(R.string.global_state_start_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(globalStartValue) + getString(R.string.state_set_content_description_complement);
                globalStartValueContDesc = String.format(getString(R.string.global_state_start_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::globalStartValue = " + globalStartValue + " / globalStartValueContDesc = " + globalStartValueContDesc);
            globalStartValueImgVw.setContentDescription(globalStartValueContDesc);
            //
            ImageView globalEndValueImgVw = mRootView.findViewById(R.id.global_array_end_value);
            int globalEndValue = mSessionRecord.getEndGlobalValue();
            int globalEndValueResId = smileysResourceId[globalEndValue];
            globalEndValueImgVw.setImageResource(globalEndValueResId); //not same behaviour as SmileRating, because here we have a symbol at index 0 for "NOT SET"
            String globalEndValueContDesc;
            if(globalEndValue == 0) {//means Value not set
                globalEndValueContDesc = String.format(getString(R.string.global_state_end_default_content_description), getString(R.string.state_not_set_content_description_complement));
            }else{
                tempEnd = String.valueOf(globalEndValue) + getString(R.string.state_set_content_description_complement);
                globalEndValueContDesc = String.format(getString(R.string.global_state_end_default_content_description), tempEnd);
            }
            //Log.d(TAG, "onCreateView::globalEndValue = " + globalEndValue + " / globalEndValueContDesc = " + globalEndValueContDesc);
            globalEndValueImgVw.setContentDescription(globalEndValueContDesc);
            //
            TextView notesLabel = mRootView.findViewById(R.id.notes_label);
            TextView notesTxtvw = mRootView.findViewById(R.id.notes_txtvw);
            if(mSessionRecord.getNotes().isEmpty()){
                notesLabel.setVisibility(View.GONE);
                notesTxtvw.setVisibility(View.GONE);
            }else {
                notesLabel.setVisibility(View.VISIBLE);
                notesTxtvw.setVisibility(View.VISIBLE);
                notesTxtvw.setText(mSessionRecord.getNotes());
            }
        }else{
            Log.e(TAG, "onCreateView::data not available!");
            return null;
        }
        //
        return mRootView;
    }

    public CardView getCardView() {
        return mCardView;
    }

}
