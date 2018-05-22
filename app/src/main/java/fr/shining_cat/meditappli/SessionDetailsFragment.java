package fr.shining_cat.meditappli;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.utils.TimeOperations;

public class SessionDetailsFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String SESSION_DETAILS_FRAGMENT_TAG = "session_details_Fragment-tag";

    private View mRootView;

   private SessionRecord mSessionRecord;

////////////////////////////////////////
// this fragment is used by the SessionsDetailsFragmentStatePagerAdapter. It shows all the details of one session
    public SessionDetailsFragment() {
        // Required empty public constructor
    }

    public static SessionDetailsFragment newInstance() {
        return new SessionDetailsFragment();
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
        mRootView = inflater.inflate(R.layout.fragment_session_details, container, false);
        //
        if(mSessionRecord!=null){
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat tdf = new SimpleDateFormat("HH:mm");
            //
            TextView startDateTxtvw = mRootView.findViewById(R.id.start_date_txtvw);
            startDateTxtvw.setText(sdf.format(mSessionRecord.getStartTimeOfRecord()));
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
            TextView pausesCountTxtvw = mRootView.findViewById(R.id.pauses_count_txtvw);
            //
            pausesCountTxtvw.setText(String.valueOf(mSessionRecord.getPausesCount()));
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
            TextView bodyStartValueTxtvw = mRootView.findViewById(R.id.body_array_start_value);
            bodyStartValueTxtvw.setText(String.valueOf(mSessionRecord.getStartBodyValue()));
            TextView bodyEndValueTxtvw = mRootView.findViewById(R.id.body_array_end_value);
            bodyEndValueTxtvw.setText(String.valueOf(mSessionRecord.getEndBodyValue()));
            //
            TextView thoughtsStartValueTxtvw = mRootView.findViewById(R.id.thoughts_array_start_value);
            thoughtsStartValueTxtvw.setText(String.valueOf(mSessionRecord.getStartThoughtsValue()));
            TextView thoughtsEndValueTxtvw = mRootView.findViewById(R.id.thoughts_array_end_value);
            thoughtsEndValueTxtvw.setText(String.valueOf(mSessionRecord.getEndThoughtsValue()));
            //
            TextView feelingsStartValueTxtvw = mRootView.findViewById(R.id.feelings_array_start_value);
            feelingsStartValueTxtvw.setText(String.valueOf(mSessionRecord.getStartFeelingsValue()));
            TextView feelingsEndValueTxtvw = mRootView.findViewById(R.id.feelings_array_end_value);
            feelingsEndValueTxtvw.setText(String.valueOf(mSessionRecord.getEndFeelingsValue()));
            //
            TextView globalStartValueTxtvw = mRootView.findViewById(R.id.global_array_start_value);
            globalStartValueTxtvw.setText(String.valueOf(mSessionRecord.getStartGlobalValue()));
            TextView globalEndValueTxtvw = mRootView.findViewById(R.id.global_array_end_value);
            globalEndValueTxtvw.setText(String.valueOf(mSessionRecord.getEndGlobalValue()));
            //
            TextView notesTxtvw = mRootView.findViewById(R.id.notes_txtvw);
            notesTxtvw.setText(mSessionRecord.getNotes());
        }else{
            Log.d(TAG, "onCreateView::data not available!");
            return null;
        }
        //
        return mRootView;
    }

}
