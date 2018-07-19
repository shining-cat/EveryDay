package fr.shining_cat.everyday;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.List;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;

public class VizViewPagerSessionsDetailsFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_PAGER_SESSION_DETAILS_FRAGMENT_TAG = "view_pager_session_details_Fragment-tag";

    private View mRootView;
    private ViewPager mSessionsDetailsViewPager;
    private VizSessionsDetailsFragmentStatePagerAdapter mVizSessionsDetailsFragmentStatePagerAdapter;
    private SessionRecord mSessionToOpenDetailsFragment;
    private SessionRecordViewModel mSessionRecordViewModel;
    private ImageButton mNextSessionBtn;
    private ImageButton mPrevSessionBtn;



////////////////////////////////////////
//This is the fragment used to hold a viewPager (with VizSessionsDetailsFragmentStatePagerAdapter as FragmentStatePagerAdapter) presenting the sessions details (VizSessionDetailsFragment)
    public VizViewPagerSessionsDetailsFragment() {
        // Required empty public constructor
    }

    public static VizViewPagerSessionsDetailsFragment newInstance() {
        return new VizViewPagerSessionsDetailsFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_viz_viewpager_sessions_details, container, false);
        //
        mSessionsDetailsViewPager = mRootView.findViewById(R.id.sessions_details_viewpager);
        mVizSessionsDetailsFragmentStatePagerAdapter = new VizSessionsDetailsFragmentStatePagerAdapter(getActivity().getSupportFragmentManager());
        mSessionsDetailsViewPager.setAdapter(mVizSessionsDetailsFragmentStatePagerAdapter);
        mSessionsDetailsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                coordinationNavBtnsAndPostionInAdapter();
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mSessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        mSessionRecordViewModel.getAllSessionsRecordsStartTimeAsc().observe(this, new Observer<List<SessionRecord>>() {

            @Override
            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                // Update the cached copy of the words in the adapter.
                mVizSessionsDetailsFragmentStatePagerAdapter.setSessions(sessionsRecords);
                //set starting position if known
                if(mSessionToOpenDetailsFragment != null){
                    int sessionPositionInAdapter = mVizSessionsDetailsFragmentStatePagerAdapter.getPositionOfSpecificSessionRecord(mSessionToOpenDetailsFragment);
                    mSessionsDetailsViewPager.setCurrentItem(sessionPositionInAdapter);
                }else{
                    mSessionsDetailsViewPager.setCurrentItem(0);
                }
                coordinationNavBtnsAndPostionInAdapter();
            }
        });
        mNextSessionBtn = mRootView.findViewById(R.id.next_session_details_btn);
        mNextSessionBtn.setOnClickListener(onNextSessionBtnClickListener);
        mPrevSessionBtn = mRootView.findViewById(R.id.previous_session_details_btn);
        mPrevSessionBtn.setOnClickListener(onPrevSessionBtnClickListener);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPrevSessionBtn.setVisibility(View.GONE);
        mNextSessionBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSessionsDetailsViewPager.clearOnPageChangeListeners();
    }

    private View.OnClickListener onNextSessionBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSessionsDetailsViewPager.setCurrentItem(mSessionsDetailsViewPager.getCurrentItem() +1, true);
        }
    };

    private View.OnClickListener onPrevSessionBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSessionsDetailsViewPager.setCurrentItem(mSessionsDetailsViewPager.getCurrentItem() -1, true);
        }
    };

    private void coordinationNavBtnsAndPostionInAdapter(){
        if((mSessionsDetailsViewPager.getCurrentItem() - 1) < 0){
            mPrevSessionBtn.setVisibility(View.GONE);
        }else{
            mPrevSessionBtn.setVisibility(View.VISIBLE);
        }
        if((mSessionsDetailsViewPager.getCurrentItem() + 1) > (mSessionsDetailsViewPager.getAdapter().getCount() - 1)){
            mNextSessionBtn.setVisibility(View.GONE);
        }else{
            mNextSessionBtn.setVisibility(View.VISIBLE);
        }

    }

    public int getCurrentPosition(){
        return mSessionsDetailsViewPager.getCurrentItem();
    }

////////////////////////////////////////
//get specific object SessionRecord
    public SessionRecord getCurrentSessionRecord(){
        return ((VizSessionsDetailsFragmentStatePagerAdapter) mSessionsDetailsViewPager.getAdapter()).getSessionRecordAtPosition(mSessionsDetailsViewPager.getCurrentItem());
    }



////////////////////////////////////////
//get specific object SessionRecord
    public SessionRecord getSessionRecordAtPosition(int position){
        return ((VizSessionsDetailsFragmentStatePagerAdapter) mSessionsDetailsViewPager.getAdapter()).getSessionRecordAtPosition(position);
    }

////////////////////////////////////////
//transmit desired starting position
    public void setStartingSessionDetailsWithSessionRecord(SessionRecord startingSessionRecord) {
        mSessionToOpenDetailsFragment = startingSessionRecord;
    }
}
