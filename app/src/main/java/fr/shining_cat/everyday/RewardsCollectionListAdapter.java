package fr.shining_cat.everyday;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import fr.shining_cat.everyday.data.Reward;


////////////////////////////////////////
//RecyclerView.Adapter used by viewRewardsListFragment on the RecyclerView to display a vertical scrolling grid of Rewards
//Two listeners on the ViewHolder : onClick and onLongClick, dispatching callbacks to interface
public class RewardsCollectionListAdapter extends RecyclerView.Adapter<RewardsCollectionListAdapter.RewardViewHolder>{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();


    public class RewardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView mFlowerPart;
        private ImageView mLegsColorPart;
        private ImageView mLegsPart;
        private ImageView mBodyColorPart;
        private ImageView mArmsColorPart;
        private ImageView mArmsPart;
        private ImageView mMouthPart;
        private ImageView mEyesPart;
        private ImageView mHornsPart;
        private View mLostRewardVeil;

        RewardViewHolder(View itemView) {
            super(itemView);
            mFlowerPart = itemView.findViewById(R.id.reward_flower_holder);
            mLegsPart = itemView.findViewById(R.id.reward_legs_holder);
            mArmsPart = itemView.findViewById(R.id.reward_arms_holder);
            mMouthPart = itemView.findViewById(R.id.reward_mouth_holder);
            mEyesPart = itemView.findViewById(R.id.reward_eyes_holder);
            mHornsPart = itemView.findViewById(R.id.reward_horns_holder);
            mLostRewardVeil = itemView.findViewById(R.id.reward_lost_veil);
            mLegsColorPart = itemView.findViewById(R.id.reward_legs_color_holder);
            mBodyColorPart = itemView.findViewById(R.id.reward_body_color_holder);
            mArmsColorPart = itemView.findViewById(R.id.reward_arms_color_holder);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                //for now we will pass the position because that is the only interesting info, but I fear it might not be the most accurate way, since we will open a new adapter at the end of this chain...
                mListener.onRewardsListAdapterClickOnReward(mRewards.get(this.getAdapterPosition()));
            }else{
                Log.e(TAG, "onLongClick::no listener to request");
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mListener != null){
                mListener.onRewardsListAdapterLongClickOnReward(mRewards.get(this.getAdapterPosition()));
                return true;
            }else{
                Log.e(TAG, "onLongClick::no listener to request");
                return false;
            }
        }
    }

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<Reward> mRewards; // Cached copy of rewards
    private RewardsListAdapterListener mListener;

    public RewardsCollectionListAdapter(Context context, RewardsListAdapterListener rewardsListAdapterListener){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (rewardsListAdapterListener instanceof RewardsListAdapterListener) {
            mListener = rewardsListAdapterListener;
        } else {
            throw new RuntimeException(rewardsListAdapterListener.toString()+ " must implement RewardsListAdapterListener");
        }
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.rewards_list_recyclerview_item, parent, false);
        return new RewardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RewardViewHolder holder, int position) {
        if(mRewards !=null){
            Reward currentReward = mRewards.get(holder.getAdapterPosition());
            //
            //TODO: Check if binding views is still so slow when using definitive optimized resources
            holder.mFlowerPart.setImageResource(Critter.getFlowerDrawableResource(currentReward.getRewardCode()));
            holder.mLegsColorPart.setImageResource(Critter.getLegsColorDrawableResource(currentReward.getRewardCode()));
            holder.mLegsPart.setImageResource(Critter.getLegsDrawableResource(currentReward.getRewardCode()));
            holder.mArmsColorPart.setImageResource(Critter.getArmsColorDrawableResource(currentReward.getRewardCode()));
            holder.mArmsPart.setImageResource(Critter.getArmsDrawableResource(currentReward.getRewardCode()));
            holder.mMouthPart.setImageResource(Critter.getMouthDrawableResource(currentReward.getRewardCode()));
            holder.mEyesPart.setImageResource(Critter.getEyesDrawableResource(currentReward.getRewardCode()));
            holder.mHornsPart.setImageResource(Critter.getHornsDrawableResource(currentReward.getRewardCode()));
            if(!currentReward.getRewardLegsColor().isEmpty()) {
                int color = Color.parseColor(currentReward.getRewardLegsColor());
                holder.mLegsColorPart.setColorFilter(color);
            }
            if(!currentReward.getRewardBodyColor().isEmpty()) {
                int color = Color.parseColor(currentReward.getRewardBodyColor());
                holder.mBodyColorPart.setColorFilter(color);
            }
            if(!currentReward.getRewardArmsColor().isEmpty()) {
                int color = Color.parseColor(currentReward.getRewardArmsColor());
                holder.mArmsColorPart.setColorFilter(color);
            }
            if(currentReward.getEscapedOrNot() == Reward.STATUS_ESCAPED){//reward has been "lost"
                holder.mLostRewardVeil.setVisibility(View.VISIBLE);
            }else{
                holder.mLostRewardVeil.setVisibility(View.GONE);
            }
        }else{
            //data not ready yet
            Log.d(TAG, "onBindViewHolder::data not available!");
        }
    }

    @Override
    public int getItemCount() {
        if(mRewards != null){
            return mRewards.size();
        }else {
            return 0;
        }
    }

    public void setRewards(List<Reward> rewards){
        mRewards = rewards;
        Log.d(TAG, "setRewards::number of rewards = " + mRewards.size());
        notifyDataSetChanged();
    }


////////////////////////////////////////
//Listener interface
    public interface RewardsListAdapterListener {
        void onRewardsListAdapterClickOnReward(Reward clickedReward);
        void onRewardsListAdapterLongClickOnReward(Reward clickedReward);
    }
}
