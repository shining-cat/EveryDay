package fr.shining_cat.everyday;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.data.SessionRecordViewModel;
import fr.shining_cat.everyday.utils.TimeOperations;


/**
 * A simple {@link Fragment} subclass.
 */
public class VizSessionsCalendarFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String VIEW_SESSION_CALANEDAR_FRAGMENT_TAG = "view_session_calendar_Fragment-tag";

    private static final String ARG_STARTING_MONTH = "starting_month_date_argument";

    private View mRootView;
    private SessionsCalendarListener mListener;
    private TextView mCalendarTitle;
    private CompactCalendarView mCalendarView;
    private VizSessionsListAdapter mVizSessionsListAdapter;
    private Date mCurrentDate;


////////////////////////////////////////
//This is the fragment used to hold a Calendar and a linked RecyclerView (with SessionsDayListAdapter as adapter) presenting each session (for selected calendar day) summary as a scrollable vertical list
    public VizSessionsCalendarFragment() {
        // Required empty public constructor
    }

    public static VizSessionsCalendarFragment newInstance(Long startingDateAsTime) {
        VizSessionsCalendarFragment fragment = new VizSessionsCalendarFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_STARTING_MONTH, startingDateAsTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStoredCurrentDate(new Date(getArguments().getLong(ARG_STARTING_MONTH)));
    }
    ////////////////////////////////////////
//plugging interface listener, here parent activity (SessionActivity -normal input- or VizActivity -manual entry and edit)

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SessionsCalendarListener) {
            mListener = (SessionsCalendarListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SessionsCalendarListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

////////////////////////////////////////
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_viz_sessions_calendar, container, false);
        //
        showLoadingSessionsMessage(false);
        showEmptyListMessageOrNot(false);
        showSelectDayMessageOrNot(true);
        //
        mCalendarView = mRootView.findViewById(R.id.compactcalendar_view);
        mCalendarView.setListener(mCompactCalendarListener);
        mCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);
        //
        mCalendarTitle = mRootView.findViewById(R.id.calendar_title_txtvw);
        if(mCurrentDate == null){
            //no previously stored date to go back to, calendarView will default to NOW, just update currentDate as NOW too
            updateStoredCurrentDate(TimeOperations.giveTodayMidnightDate());
        } else{
            mCalendarView.setCurrentDate(mCurrentDate);
        }
        //
        ImageButton previousMonthBtn = mRootView.findViewById(R.id.previous_month_btn);
        previousMonthBtn.setOnClickListener(mOnPreviousMonthClickListener);
        ImageButton nextMonthBtn = mRootView.findViewById(R.id.next_month_btn);
        nextMonthBtn.setOnClickListener(mOnNextMonthClickListener);
        //
        Button jumpToToday = mRootView.findViewById(R.id.jump_to_today_btn);
        jumpToToday.setOnClickListener(mOnJumpToTodayClickListener);
        //
        SessionRecordViewModel sessionRecordViewModel = ViewModelProviders.of(this).get(SessionRecordViewModel.class);
        sessionRecordViewModel.getAllSessionsRecordsStartTimeAsc().observe(this, new Observer<List<SessionRecord>>() {
            @Override
            public void onChanged(@Nullable final List<SessionRecord> sessionsRecords) {
                mCalendarView.invalidate();
                updateCalendarViewContent(sessionsRecords);
            }
        });
        //
        RecyclerView sessionsOfDayRecyclerView = mRootView.findViewById(R.id.sessions_list_for_one_day_recyclerview);
        mVizSessionsListAdapter = new VizSessionsListAdapter(getActivity(), (VizSessionsListAdapter.SessionsListAdapterListener) getActivity());
        sessionsOfDayRecyclerView.setAdapter(mVizSessionsListAdapter);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        sessionsOfDayRecyclerView.setHasFixedSize(true);
        sessionsOfDayRecyclerView.setLayoutManager(recyclerLayoutManager);
        //
        updateCalendarTitle(mCurrentDate);
        //
        return mRootView;
    }

    private View.OnClickListener mOnPreviousMonthClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mCalendarView.showPreviousMonth();
        }
    };
    private View.OnClickListener mOnNextMonthClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mCalendarView.showNextMonth();
        }
    };
    private View.OnClickListener mOnJumpToTodayClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mCalendarView.setCurrentDate(new Date());
            updateCalendarTitle(new Date());
        }
    };
    private CompactCalendarView.CompactCalendarViewListener mCompactCalendarListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date dateClicked) {
            showLoadingSessionsMessage(true);
            Log.d(TAG, "Day was clicked: " + dateClicked);
            updateEventsListOfDay(dateClicked);
        }

        @Override
        public void onMonthScroll(Date firstDayOfNewMonth) {
            Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
            updateCalendarTitle(firstDayOfNewMonth);
        }
    };

    private void updateCalendarTitle(Date date){
        updateStoredCurrentDate(date);
        DateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String displayedMonth = sdf.format(date);
        mCalendarTitle.setText(displayedMonth);
        Log.d(TAG, "updateCalendarTitle mCurrentDate = " + mCurrentDate);
        updateEventsListOfDay(date);
    }

    private void updateCalendarViewContent(List<SessionRecord> sessionsRecords){
        mCalendarView.removeAllEvents();
        List<Event> eventsList = new ArrayList<>();
        for(SessionRecord sessionRecord : sessionsRecords){
            Event event = new Event(Color.GREEN, sessionRecord.getStartTimeOfRecord(), sessionRecord);
            eventsList.add(event);
        }
        mCalendarView.addEvents(eventsList);
        updateEventsListOfDay(mCurrentDate);
    }

    private void updateEventsListOfDay(Date date){
        updateStoredCurrentDate(date);
        List<Event> eventsListForClickedDay = mCalendarView.getEvents(date);
        List<SessionRecord> sessionsRecords  = new ArrayList<>();
        for(Event event : eventsListForClickedDay){
            sessionsRecords.add((SessionRecord) event.getData());
        }
        showLoadingSessionsMessage(false);
        Log.d(TAG, "updateEventsListOfDay sessionsRecords = " + sessionsRecords.size());
        if(sessionsRecords!=null) {
            // Update the cached copy of the sessions in the adapter.
            mVizSessionsListAdapter.setSessions(sessionsRecords);
            showSelectDayMessageOrNot(false);
            if(sessionsRecords.size()==0){
                showEmptyListMessageOrNot(true);
                //mVizSessionsListAdapter.notifyDataSetChanged();
            }else{
                showEmptyListMessageOrNot(false);
            }
        }else{
            Log.e(TAG, "updateEventsListOfDay sessionsRecords is NULL!");
        }
    }

    private void updateStoredCurrentDate(Date date){
        mCurrentDate = date;
        mListener.onSaveCurrentDate(mCurrentDate.getTime());
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

////////////////////////////////////////
//hiding or showing "no sessions message"
    private void showSelectDayMessageOrNot(boolean showIt){
        TextView selectDayMessage = mRootView.findViewById(R.id.select_day_sessions_list_message);
        if(showIt){
            selectDayMessage.setVisibility(View.VISIBLE);
        }else{
            selectDayMessage.setVisibility(View.GONE);
        }
    }

////////////////////////////////////////
//Listener interface
    public interface SessionsCalendarListener {
        void onSaveCurrentDate(Long currentMonthDateAsLong);
    }
}
