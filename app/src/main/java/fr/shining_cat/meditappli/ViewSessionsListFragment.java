package fr.shining_cat.meditappli;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;
import fr.shining_cat.meditappli.data.SessionsListAdapter;
import fr.shining_cat.meditappli.widgets.SearchBarWidget;
import fr.shining_cat.meditappli.widgets.SessionsListFilterToggleButton;

public class ViewSessionsListFragment extends Fragment implements SearchBarWidget.SearchWidgetListener {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_SESSION_LIST_FRAGMENT_TAG = "view_session_list_Fragment-tag";

    private final static String SORT_FIELD_DATE = "sorting sessions list on DATE field";
    private final static String SORT_FIELD_DURATION = "sorting sessions list on DURATION field";
    private final static String SORT_FIELD_MP3 = "sorting sessions list on MP3 field";
    private final static String SORT_DIRECTION_ASC = "sorting sessions list ASCENDANT";
    private final static String SORT_DIRECTION_DESC = "sorting sessions list DESCENDANT";
    private final static String SORT_WITH = "sorting sessions list WITH";
    private final static String SORT_WITHOUT = "sorting sessions list WITHOUT";
    private final static String SEARCH = "searching sessions for something";


    private View mRootView;
    private RecyclerView mSessionsListRecyclerView;
    private SessionsListAdapter mSessionsListAdapter;
    private SessionsListFilterToggleButton mDateFilterToggleBtn;
    private SessionsListFilterToggleButton mDurationFilterToggleBtn;
    private SessionsListFilterToggleButton mMp3FilterToggleBtn;
    private SearchBarWidget mSearchBarWidget;

////////////////////////////////////////
//This is the fragment used to hold a RecyclerView (with SessionsListAdapter as adapter) presenting each session summary as a scrollable vertical list
    public ViewSessionsListFragment() {
        // Required empty public constructor
    }

    public static ViewSessionsListFragment newInstance() {
        return new ViewSessionsListFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_view_sessions_list, container, false);
        showLoadingSessionsMessage(true);
        showEmptyListMessageOrNot(false);
        //
        mSessionsListRecyclerView = mRootView.findViewById(R.id.sessions_list_recyclerview);
        mSessionsListAdapter = new SessionsListAdapter(getActivity(), (SessionsListAdapter.SessionsListAdapterListener) getActivity());
        mSessionsListRecyclerView.setAdapter(mSessionsListAdapter);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        mSessionsListRecyclerView.setHasFixedSize(true);
        mSessionsListRecyclerView.setLayoutManager(recyclerLayoutManager);
        //
        mDateFilterToggleBtn = mRootView.findViewById(R.id.sessions_list_date_toggle_filter);
        mDateFilterToggleBtn.setLabel(getString(R.string.date));
        mDateFilterToggleBtn.setModeCheckOrArrow(SessionsListFilterToggleButton.ARROW_MODE);
        mDateFilterToggleBtn.setOnClickListener(mOnDateFilterToggleClickListener);
        //
        mDurationFilterToggleBtn = mRootView.findViewById(R.id.sessions_list_duration_toggle_filter);
        mDurationFilterToggleBtn.setLabel(getString(R.string.duration));
        mDurationFilterToggleBtn.setOnClickListener(mOnDurationFilterToggleClickListener);
        mDurationFilterToggleBtn.setModeCheckOrArrow(SessionsListFilterToggleButton.ARROW_MODE);
        //
        mMp3FilterToggleBtn = mRootView.findViewById(R.id.sessions_list_mp3_toggle_filter);
        mMp3FilterToggleBtn.setLabel(getString(R.string.mp3));
        mMp3FilterToggleBtn.setOnClickListener(mOnMp3FilterToggleClickListener);
        mMp3FilterToggleBtn.setModeCheckOrArrow(SessionsListFilterToggleButton.CHECK_MODE);
        //
        mSearchBarWidget = mRootView.findViewById(R.id.searchBarWidget);
        mSearchBarWidget.setListener(this);
        //
        //default sessions list filter and sorting is on date descendant
        mDateFilterToggleBtn.setActive(SessionsListFilterToggleButton.OPTION_DOWN);
        filterAndSortSessionsList(SORT_FIELD_DATE, SORT_DIRECTION_DESC);
        setOtherFiltersInactive(mDateFilterToggleBtn);
        setSearchBarInactive();
        //
        return mRootView;
    }

////////////////////////////////////////
//FILTERING
    private View.OnClickListener mOnDateFilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mDateFilterToggleBtn.isActive()){
                String dateFilterDirection = (mDateFilterToggleBtn.toggle().equals(SessionsListFilterToggleButton.OPTION_UP)) ? SORT_DIRECTION_ASC : SORT_DIRECTION_DESC;
                filterAndSortSessionsList(SORT_FIELD_DATE, dateFilterDirection);
            }else{
                mDateFilterToggleBtn.setActive(SessionsListFilterToggleButton.OPTION_UP);
                filterAndSortSessionsList(SORT_FIELD_DATE, SORT_DIRECTION_ASC);
                setOtherFiltersInactive(mDateFilterToggleBtn);
                setSearchBarInactive();
            }
        }
    };
    private View.OnClickListener mOnDurationFilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mDurationFilterToggleBtn.isActive()){
                String durationFilterDirection = (mDurationFilterToggleBtn.toggle().equals(SessionsListFilterToggleButton.OPTION_UP)) ? SORT_DIRECTION_ASC : SORT_DIRECTION_DESC;
                filterAndSortSessionsList(SORT_FIELD_DURATION, durationFilterDirection);
            }else{
                mDurationFilterToggleBtn.setActive(SessionsListFilterToggleButton.OPTION_UP);
                filterAndSortSessionsList(SORT_FIELD_DURATION, SORT_DIRECTION_ASC);
                setOtherFiltersInactive(mDurationFilterToggleBtn);
                setSearchBarInactive();
            }
        }
    };
    private View.OnClickListener mOnMp3FilterToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mMp3FilterToggleBtn.isActive()){
                String mp3FilterParam = (mMp3FilterToggleBtn.toggle().equals(SessionsListFilterToggleButton.OPTION_YES)) ? SORT_WITH : SORT_WITHOUT;
                filterAndSortSessionsList(SORT_FIELD_MP3, mp3FilterParam);
            }else{
                mMp3FilterToggleBtn.setActive(SessionsListFilterToggleButton.OPTION_YES);
                filterAndSortSessionsList(SORT_FIELD_MP3, SORT_WITH);
                setOtherFiltersInactive(mMp3FilterToggleBtn);
                setSearchBarInactive();
            }
        }
    };
    @Override
    public void onSearchWidgetActivation() {
        setOtherFiltersInactive(null);
    }

    @Override
    public void onSearchWidgetLaunchSearch(String searchRequest) {
        filterAndSortSessionsList(SEARCH, searchRequest);
    }

    @Override
    public void onSearchWidgetResetSearch() {
        filterAndSortSessionsList(SEARCH, null);
    }

    private void setOtherFiltersInactive(SessionsListFilterToggleButton onlyActiveToggle){
        if(onlyActiveToggle != mDateFilterToggleBtn) mDateFilterToggleBtn.setInactive();
        if(onlyActiveToggle != mDurationFilterToggleBtn) mDurationFilterToggleBtn.setInactive();
        if(onlyActiveToggle != mMp3FilterToggleBtn) mMp3FilterToggleBtn.setInactive();
    }

    private void setSearchBarInactive(){
        mSearchBarWidget.setInactive();
    }

    private void filterAndSortSessionsList(String filterAndSortField, String filterAndSortParam){
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        //
        switch (filterAndSortField){
            case SEARCH:
                if(filterAndSortParam != null && !filterAndSortParam.isEmpty()){
                    Log.d(TAG, "filterAndSortSessionsList::SEARCH::filterAndSortParam = " + filterAndSortParam);
                    sessionRecordViewModel.getSessionsRecordsSearch("%" + filterAndSortParam + "%").observe(this, new Observer<List<SessionRecord>>() {
                        @Override
                        public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                            // Update the cached copy of the sessions in the adapter.
                            mSessionsListAdapter.setSessions(sessionsRecords);
                            mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                            showLoadingSessionsMessage(false);
                            if(sessionsRecords!=null) {
                                showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                            }else{
                                showEmptyListMessageOrNot(true);
                            }
                        }
                    });
                }else{ // resetting search request : show all, default to sort on date and desc
                    sessionRecordViewModel.getAllSessionsRecordsStartTimeDesc().observe(this, new Observer<List<SessionRecord>>() {
                        @Override
                        public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                            // Update the cached copy of the sessions in the adapter.
                            mSessionsListAdapter.setSessions(sessionsRecords);
                            mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                            showLoadingSessionsMessage(false);
                            if(sessionsRecords!=null) {
                                showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                            }else{
                                showEmptyListMessageOrNot(true);
                            }
                        }
                    });
                }
                break;
            case SORT_FIELD_DURATION:
                switch (filterAndSortParam) {
                    case SORT_DIRECTION_DESC:
                        sessionRecordViewModel.getAllSessionsRecordsDurationDesc().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                    case SORT_DIRECTION_ASC:
                    default:
                        sessionRecordViewModel.getAllSessionsRecordsDurationAsc().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                }
                break;
            case SORT_FIELD_MP3:
                switch (filterAndSortParam) {
                    case SORT_WITHOUT:
                        sessionRecordViewModel.getAllSessionsRecordsWithoutMp3().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                    case SORT_WITH:
                    default:
                        sessionRecordViewModel.getAllSessionsRecordsWithMp3().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                }
                break;
            case SORT_FIELD_DATE:
            default:
                switch (filterAndSortParam) {
                    case SORT_DIRECTION_ASC:
                        sessionRecordViewModel.getAllSessionsRecordsStartTimeAsc().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                    case SORT_DIRECTION_DESC:
                    default:
                        sessionRecordViewModel.getAllSessionsRecordsStartTimeDesc().observe(this, new Observer<List<SessionRecord>>() {
                            @Override
                            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                                // Update the cached copy of the sessions in the adapter.
                                mSessionsListAdapter.setSessions(sessionsRecords);
                                mSessionsListRecyclerView.getLayoutManager().scrollToPosition(0);
                                showLoadingSessionsMessage(false);
                                if(sessionsRecords!=null) {
                                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                                }else{
                                    showEmptyListMessageOrNot(true);
                                }
                            }
                        });
                        break;
                }
                break;
        }
    }












////////////////////////////////////////
//hiding or showing "loading sessions message"
    private void showLoadingSessionsMessage(boolean showIt){
        TextView loadingSessions = mRootView.findViewById(R.id.loading_sessions_list_message);
        if(showIt){
            loadingSessions.setVisibility(View.VISIBLE);
        }else{
            loadingSessions.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////
//hiding or showing "no sessions message"
    private void showEmptyListMessageOrNot(boolean showIt){
        TextView emptyListMessage = mRootView.findViewById(R.id.empty_sessions_list_message);
        if(showIt){
            emptyListMessage.setVisibility(View.VISIBLE);
        }else{
            emptyListMessage.setVisibility(View.GONE);
        }
    }


}
