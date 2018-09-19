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
import fr.shining_cat.everyday.utils.ShadowTransformer;

public class VizSessionDetailsViewPagerFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_PAGER_SESSION_DETAILS_FRAGMENT_TAG = "view_pager_session_details_Fragment-tag";

    private View mRootView;
    private ViewPager mSessionsDetailsViewPager;
    private VizSessionDetailsCardFragmentPagerAdapter mVizSessionDetailsCardFragmentStatePagerAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;
    private SessionRecord mSessionToOpenDetailsFragment;
    private SessionRecordViewModel mSessionRecordViewModel;


////////////////////////////////////////
//This is the fragment used to hold a viewPager (with VizSessionDetailsCardFragmentPagerAdapter as FragmentStatePagerAdapter) presenting the sessions details (VizSessionDetailsCardFragment)
    public VizSessionDetailsViewPagerFragment() {
        // Required empty public constructor
    }

    public static VizSessionDetailsViewPagerFragment newInstance() {
        return new VizSessionDetailsViewPagerFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_viz_viewpager_sessions_details, container, false);
        //
        mSessionsDetailsViewPager = mRootView.findViewById(R.id.sessions_details_viewpager);
        mVizSessionDetailsCardFragmentStatePagerAdapter = new VizSessionDetailsCardFragmentPagerAdapter(getActivity().getSupportFragmentManager());
        mSessionsDetailsViewPager.setAdapter(mVizSessionDetailsCardFragmentStatePagerAdapter);
        //
        mFragmentCardShadowTransformer = new ShadowTransformer(mSessionsDetailsViewPager, mVizSessionDetailsCardFragmentStatePagerAdapter);

        mSessionsDetailsViewPager.setPageTransformer(false, mFragmentCardShadowTransformer);
        mSessionsDetailsViewPager.setOffscreenPageLimit(3);
        //
        mSessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        mSessionRecordViewModel.getAllSessionsRecordsStartTimeAsc().observe(this, new Observer<List<SessionRecord>>() {
            @Override
            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                // Update the cached copy of the sessions in the adapter.
                mVizSessionDetailsCardFragmentStatePagerAdapter.setSessions(sessionsRecords);
                mFragmentCardShadowTransformer.enableScaling(true);
                //set starting position if known
                if(mSessionToOpenDetailsFragment != null){
                    int sessionPositionInAdapter = mVizSessionDetailsCardFragmentStatePagerAdapter.getPositionOfSpecificSessionRecord(mSessionToOpenDetailsFragment);
                    mSessionsDetailsViewPager.setCurrentItem(sessionPositionInAdapter);
                }else{
                    mSessionsDetailsViewPager.setCurrentItem(0);
                }
            }
        });

        return mRootView;
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mSessionsDetailsViewPager.clearOnPageChangeListeners();
    }




    public int getCurrentPosition(){
        return mSessionsDetailsViewPager.getCurrentItem();
    }

////////////////////////////////////////
//get current object SessionRecord
    public SessionRecord getCurrentSessionRecord(){
        return ((VizSessionDetailsCardFragmentPagerAdapter) mSessionsDetailsViewPager.getAdapter()).getSessionRecordAtPosition(mSessionsDetailsViewPager.getCurrentItem());
    }



////////////////////////////////////////
//get specific object SessionRecord
    public SessionRecord getSessionRecordAtPosition(int position){
        return ((VizSessionDetailsCardFragmentPagerAdapter) mSessionsDetailsViewPager.getAdapter()).getSessionRecordAtPosition(position);
    }

////////////////////////////////////////
//transmit desired starting position
    public void setStartingSessionDetailsWithSessionRecord(SessionRecord startingSessionRecord) {
        mSessionToOpenDetailsFragment = startingSessionRecord;
    }
}
