package fr.shining_cat.everyday;

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
import java.util.Locale;

import fr.shining_cat.everyday.data.SessionRecord;
import fr.shining_cat.everyday.utils.TimeOperations;


////////////////////////////////////////
//RecyclerView.Adapter used by viewSessionsListFragment on the RecyclerView to display a vertical scrolling list of Sessions summary
//Two listeners on the ViewHolder : onClick and onLongClick, dispatching callbacks to interface
public class VizSessionsListAdapter extends RecyclerView.Adapter<VizSessionsListAdapter.SessionViewHolder>{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView sessionItemDateTxtvw;
        private final TextView sessionItemTimeTxtvw;
        private final TextView sessionItemDurationTxtvw;
        private final TextView sessionItemMp3Txtvw;
        private final TextView sessionItemNotesTxtvw;

        SessionViewHolder(View itemView) {
            super(itemView);
            sessionItemDateTxtvw = itemView.findViewById(R.id.sessions_list_item_date_txtvw);
            sessionItemTimeTxtvw = itemView.findViewById(R.id.sessions_list_item_time_txtvw);
            sessionItemDurationTxtvw = itemView.findViewById(R.id.sessions_list_item_duration_txtvw);
            sessionItemMp3Txtvw = itemView.findViewById(R.id.sessions_list_item_mp3_txtvw);
            sessionItemNotesTxtvw = itemView.findViewById(R.id.sessions_list_item_notes_txtvw);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //for now we will pass the position because that is the only interesting info, but I fear it might not be the most accurate way, since we will open a new adapter at the end of this chain...
            mListener.onClickOnSession(mSessions.get(this.getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onLongClickOnSession(mSessions.get(this.getAdapterPosition()));
            return true;
        }
    }

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<SessionRecord> mSessions; // Cached copy of sessions
    private SessionsListAdapterListener mListener;

    public VizSessionsListAdapter(Context context, SessionsListAdapterListener sessionsListAdapterListener){
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
        View itemView = mInflater.inflate(R.layout.recyclerview_item_sessions_list, parent, false);
        return new SessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position) {
        if(mSessions !=null){
            SessionRecord currentSession = mSessions.get(holder.getAdapterPosition());
            //
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(currentSession.getStartTimeOfRecord());
            DateFormat tdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String formattedTime = tdf.format(currentSession.getStartTimeOfRecord());
            //
            holder.sessionItemDateTxtvw.setText(formattedDate);
            holder.sessionItemTimeTxtvw.setText(formattedTime);
            holder.sessionItemDurationTxtvw.setText(TimeOperations.convertMillisecondsToHoursAndMinutesString(
                    currentSession.getSessionRealDuration(),
                    mContext.getResources().getString(R.string.generic_string_SHORT_HOURS),
                    mContext.getResources().getString(R.string.generic_string_SHORT_MINUTES),
                    false));
            if(!currentSession.getGuideMp3().isEmpty()){
                holder.sessionItemMp3Txtvw.setVisibility(View.VISIBLE);
                holder.sessionItemMp3Txtvw.setText(currentSession.getGuideMp3());
            }else{
                holder.sessionItemMp3Txtvw.setVisibility(View.GONE);
                holder.sessionItemMp3Txtvw.setText("");
            }
            if(!currentSession.getNotes().isEmpty()){
                holder.sessionItemNotesTxtvw.setVisibility(View.VISIBLE);
                holder.sessionItemNotesTxtvw.setText(currentSession.getNotes());
            }else{
                holder.sessionItemNotesTxtvw.setVisibility(View.GONE);
                holder.sessionItemNotesTxtvw.setText("");
            }
        }else{
            //data not ready yet
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
        Log.d(TAG, "setSessions::number of sessions = " + mSessions.size());
        notifyDataSetChanged();
    }


////////////////////////////////////////
//Listener interface
    public interface SessionsListAdapterListener {
        void onClickOnSession(SessionRecord clickedSession);
        void onLongClickOnSession(SessionRecord clickedSession);
    }
}
