package fr.shining_cat.meditappli.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import fr.shining_cat.meditappli.R;
import fr.shining_cat.meditappli.utils.TimeOperations;


////////////////////////////////////////
//RecyclerView.Adapter used by viewSessionsListFragment on the RecyclerView to display a vertical scrolling list of Sessions summary
//Two listeners on the ViewHolder : onClick and onLongClick, dispatching callbacks to interface
public class SessionsListAdapter extends RecyclerView.Adapter<SessionsListAdapter.SessionViewHolder>{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView sessionItemDateTxtvw;
        private final TextView sessionItemTimeTxtvw;
        private final TextView sessionItemDurationTxtvw;

        SessionViewHolder(View itemView) {
            super(itemView);
            sessionItemDateTxtvw = itemView.findViewById(R.id.sessions_list_item_date_txtvw);
            sessionItemTimeTxtvw = itemView.findViewById(R.id.sessions_list_item_time_txtvw);
            sessionItemDurationTxtvw = itemView.findViewById(R.id.sessions_list_item_duration_txtvw);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //mListener.onClickOnSession(mSessions.get(this.getAdapterPosition()));
            //for now we will pass the position because that is the only interesting info, but I fear it might not be the most accurate way, since we will open a new adapter at the end of this chain...
            mListener.onClickOnSession(this.getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onLongClickOnSession(this.getAdapterPosition(), mSessions.get(this.getAdapterPosition()));
            return true;
        }
    }

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<SessionRecord> mSessions; // Cached copy of sessions
    private SessionsListAdapterListener mListener;

    public SessionsListAdapter(Context context, SessionsListAdapterListener sessionsListAdapterListener){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (sessionsListAdapterListener instanceof SessionsListAdapterListener) {
            mListener = sessionsListAdapterListener;
        } else {
            throw new RuntimeException(sessionsListAdapterListener.toString()+ " must implement SessionsListAdapterListener");
        }
    }

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.sessions_list_recyclerview_item, parent, false);
        return new SessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position) {
        if(mSessions !=null){
            SessionRecord currentSession = mSessions.get(holder.getAdapterPosition());
            //
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = sdf.format(currentSession.getStartTimeOfRecord());
            DateFormat tdf = new SimpleDateFormat("HH:mm");
            String formattedTime = tdf.format(currentSession.getStartTimeOfRecord());
            //
            holder.sessionItemDateTxtvw.setText(formattedDate);
            holder.sessionItemTimeTxtvw.setText(formattedTime);
            holder.sessionItemDurationTxtvw.setText(TimeOperations.convertMillisecondsToHoursAndMinutesString(
                    currentSession.getSessionRealDuration(),
                    mContext.getResources().getString(R.string.generic_string_SHORT_HOURS),
                    mContext.getResources().getString(R.string.generic_string_SHORT_MINUTES),
                    false));
        }else{
            //data not yet ready
            Log.d(TAG, "onBindViewHolder::data not available!");
        }
    }

    @Override
    public int getItemCount() {
        if(mSessions != null){
            return mSessions.size();
        }else {
            return 0;
        }
    }

    public void setSessions(List<SessionRecord> sessions){
        mSessions = sessions;
        Log.d(TAG, "setSessions::mSessions size = " + mSessions.size());
        notifyDataSetChanged();
    }

////////////////////////////////////////
//Listener interface
    public interface SessionsListAdapterListener {
        void onClickOnSession(int clickedSessionPosition);
        void onLongClickOnSession(int clickedSessionPositionInAdapter, SessionRecord clickedSession);
    }
}
