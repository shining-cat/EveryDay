package fr.shining_cat.everyday;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import fr.shining_cat.everyday.data.Reward;


////////////////////////////////////////
//RecyclerView.Adapter used by RewardsObtainedFragment on the RecyclerView to display a vertical scrolling list of Rewards
public class RewardsObtainedListAdapter extends RecyclerView.Adapter<RewardsObtainedListAdapter.RewardViewHolder>{

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public class RewardViewHolder extends RecyclerView.ViewHolder {

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
        }
    }

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<Reward> mRewards; // Cached copy of rewards

    public RewardsObtainedListAdapter(Context context){
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item_rewards_list, parent, false);
        return new RewardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RewardViewHolder holder, int position) {
        if(mRewards !=null){
            Reward currentReward = mRewards.get(holder.getAdapterPosition());
            //TODO: Check if binding views is still so slow when using definitive optimized resources
            holder.mFlowerPart.setImageResource(Critter.getFlowerDrawableResource(currentReward.getRewardCode()));
            holder.mLegsPart.setImageResource(Critter.getLegsDrawableResource(currentReward.getRewardCode()));
            holder.mArmsPart.setImageResource(Critter.getArmsDrawableResource(currentReward.getRewardCode()));
            holder.mMouthPart.setImageResource(Critter.getMouthDrawableResource(currentReward.getRewardCode()));
            holder.mEyesPart.setImageResource(Critter.getEyesDrawableResource(currentReward.getRewardCode()));
            holder.mHornsPart.setImageResource(Critter.getHornsDrawableResource(currentReward.getRewardCode()));
            //parts not used here
            holder.mLegsColorPart.setVisibility(View.GONE);
            holder.mBodyColorPart.setVisibility(View.GONE);
            holder.mArmsColorPart.setVisibility(View.GONE);
            holder.mLostRewardVeil.setVisibility(View.GONE);
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

}
