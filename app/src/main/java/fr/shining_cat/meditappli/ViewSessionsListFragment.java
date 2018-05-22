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

import java.util.List;

import fr.shining_cat.meditappli.data.SessionRecord;
import fr.shining_cat.meditappli.data.SessionRecordViewModel;
import fr.shining_cat.meditappli.data.SessionsListAdapter;

public class ViewSessionsListFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_SESSION_LIST_FRAGMENT_TAG = "view_session_list_Fragment-tag";

    private View mRootView;

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
        RecyclerView sessionsListRecyclerView = mRootView.findViewById(R.id.sessions_list_recyclerview);
        final SessionsListAdapter sessionsListAdapter = new SessionsListAdapter(getActivity(), (SessionsListAdapter.SessionsListAdapterListener) getActivity());
        sessionsListRecyclerView.setAdapter(sessionsListAdapter);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        sessionsListRecyclerView.setHasFixedSize(true);
        sessionsListRecyclerView.setLayoutManager(recyclerLayoutManager);
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getAllSessionsRecords().observe(this, new Observer<List<SessionRecord>>() {
            @Override
            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                // Update the cached copy of the words in the adapter.
                sessionsListAdapter.setSessions(sessionsRecords);
                showLoadingSessionsMessage(false);
                if(sessionsRecords!=null) {
                    showEmptyListMessageOrNot(sessionsRecords.size() < 1);
                }else{
                    showEmptyListMessageOrNot(true);
                }
            }
        });
        return mRootView;
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
