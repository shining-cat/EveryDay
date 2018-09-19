package fr.shining_cat.everyday;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.shining_cat.everyday.data.Reward;
import fr.shining_cat.everyday.utils.CardAdapter;
import fr.shining_cat.everyday.utils.MiscUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class RewardDetailsCardFragment extends Fragment {

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    public static final String REWARD_DETAILS_FRAGMENT_TAG = "reward_details_Fragment-tag";

    private static final int FLASH_SCREEN_DURATION  = 100;


    private RewardDetailsCardFragmentListener mListener;
    private View mRootView;
    private Reward mReward;
    private CardView mCardView;
    private View mFlash;
    private ImageView mShareBtn;
    private View mShareBtnBckgnd;
    private ImageView mEditBtn;
    private View mEditBtnBckgnd;


////////////////////////////////////////
// this fragment is used by the VizSessionDetailsCardFragmentPagerAdapter. It shows all the details of one session
    public RewardDetailsCardFragment() {
        // Required empty public constructor
    }

    public static RewardDetailsCardFragment newInstance() {
        return new RewardDetailsCardFragment();
    }

////////////////////////////////////////
//plugging interface listener, here parent activity

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RewardDetailsCardFragmentListener) {
            mListener = (RewardDetailsCardFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RewardDetailsCardFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


////////////////////////////////////////
//getting the session data
    public void setContent(Reward reward) {
        mReward = reward;
    }

////////////////////////////////////////
//setting the view containing the data
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_reward_details, container, false);
        mFlash = mRootView.findViewById(R.id.reward_card_flash);
        mEditBtn = mRootView.findViewById(R.id.reward_card_edit_btn);
        mEditBtn.setOnClickListener(onEditClickListener);
        mEditBtnBckgnd = mRootView.findViewById(R.id.reward_card_edit_btn_background);
        mShareBtn = mRootView.findViewById(R.id.reward_card_share_btn);
        mShareBtn.setOnClickListener(onShareClickListener);
        mShareBtnBckgnd = mRootView.findViewById(R.id.reward_card_share_btn_background);
        //
        ImageView flowerPart = mRootView.findViewById(R.id.reward_details_flower_holder);
        ImageView legsColorPart = mRootView.findViewById(R.id.reward_details_legs_color_holder);
        ImageView legsPart = mRootView.findViewById(R.id.reward_details_legs_holder);
        //body does not change, we only need the color part for customization
        ImageView bodyColorPart = mRootView.findViewById(R.id.reward_details_body_color_holder);
        ImageView armsColorPart = mRootView.findViewById(R.id.reward_details_arms_color_holder);
        ImageView armsPart = mRootView.findViewById(R.id.reward_details_arms_holder);
        ImageView mouthPart = mRootView.findViewById(R.id.reward_details_mouth_holder);
        ImageView eyesPart = mRootView.findViewById(R.id.reward_details_eyes_holder);
        ImageView hornsPart = mRootView.findViewById(R.id.reward_details_horns_holder);
        //
        TextView rewardName = mRootView.findViewById(R.id.reward_details_name);
        TextView rewardLevel = mRootView.findViewById(R.id.reward_details_level);
        TextView rewardLastDate = mRootView.findViewById(R.id.reward_details_date);
        //
        View lostRewardVeil = mRootView.findViewById(R.id.reward_details_lost_veil);
        //
        if(mReward!=null){
            flowerPart.setImageResource(Critter.getFlowerDrawableResource(mReward.getRewardCode()));
            legsColorPart.setImageResource(Critter.getLegsColorDrawableResource(mReward.getRewardCode()));
            legsPart.setImageResource(Critter.getLegsDrawableResource(mReward.getRewardCode()));
            armsColorPart.setImageResource(Critter.getArmsColorDrawableResource(mReward.getRewardCode()));
            armsPart.setImageResource(Critter.getArmsDrawableResource(mReward.getRewardCode()));
            mouthPart.setImageResource(Critter.getMouthDrawableResource(mReward.getRewardCode()));
            eyesPart.setImageResource(Critter.getEyesDrawableResource(mReward.getRewardCode()));
            hornsPart.setImageResource(Critter.getHornsDrawableResource(mReward.getRewardCode()));
            if(!mReward.getRewardLegsColor().isEmpty()) {
                int color = Color.parseColor(mReward.getRewardLegsColor());
                legsColorPart.setColorFilter(color);
            }
            if(!mReward.getRewardBodyColor().isEmpty()) {
                int color = Color.parseColor(mReward.getRewardBodyColor());
                bodyColorPart.setColorFilter(color);
            }
            if(!mReward.getRewardArmsColor().isEmpty()) {
                int color = Color.parseColor(mReward.getRewardArmsColor());
                armsColorPart.setColorFilter(color);
            }
            //
            rewardLevel.setText(String.format(getString(R.string.reward_level), String.valueOf(mReward.getRewardLevel())));
            //
            if(mReward.getRewardName().isEmpty()){
                rewardName.setText(""); //let user decide for an empty name insteadf of reward code... user can get the code again when editting name, code will be proposed if name is empty
            }else{
                rewardName.setText(mReward.getRewardName());
            }
            DateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            long lastDate;
            if(mReward.getEscapedOrNot() == Reward.STATUS_NOT_ESCAPED){
                lastDate = mReward.getAcquisitionDate();
                rewardLastDate.setText(String.format(getString(R.string.reward_obtained_last_date), sdfDate.format(lastDate)));
                lostRewardVeil.setVisibility(GONE);
                //hide buttons user can not edit or share lost rewards
                mShareBtn.setVisibility(VISIBLE);
                mShareBtnBckgnd.setVisibility(VISIBLE);
                mEditBtn.setVisibility(VISIBLE);
                mEditBtnBckgnd.setVisibility(VISIBLE);
            }else{
                lastDate = mReward.getEscapingDate();
                rewardLastDate.setText(String.format(getString(R.string.reward_lost_last_date), sdfDate.format(lastDate)));
                lostRewardVeil.setVisibility(VISIBLE);
                //hide buttons user can not edit or share lost rewards
                mShareBtn.setVisibility(GONE);
                mShareBtnBckgnd.setVisibility(GONE);
                mEditBtn.setVisibility(GONE);
                mEditBtnBckgnd.setVisibility(GONE);
            }
        }else{
            Log.e(TAG, "onCreateView::data not available!");
            return null;
        }
        //
        mCardView = mRootView.findViewById(R.id.reward_details_cardview);
        mCardView.setMaxCardElevation(mCardView.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);
        //
        return mRootView;
    }

    public CardView getCardView() {
        return mCardView;
    }


////////////////////////////////////////
//Buttons click listeners
    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onRewardDetailsCardFragmentEditRewardDetails(mReward);
            }else{
                Log.e(TAG, "onEditClickListener::onClick::no listener to request");
            }

        }
    };
    private View.OnClickListener onShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            prepareSaveAndShareRewardCard();
        }
    };

////////////////////////////////////////
//SAVE AND SHARE CARD
    private void prepareSaveAndShareRewardCard(){
        CountDownTimer flashCountDown = new CountDownTimer(FLASH_SCREEN_DURATION, 100) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                saveAndShareRewardCard();
            }
        }.start();
        //show "flash"
        mFlash.setVisibility(VISIBLE);
    }

    private void saveAndShareRewardCard(){
        //hide "flash"
        mFlash.setVisibility(GONE);
        //hide buttons temporarily so they are not in screenshot
        mShareBtn.setVisibility(GONE);
        mShareBtnBckgnd.setVisibility(GONE);
        mEditBtn.setVisibility(GONE);
        mEditBtnBckgnd.setVisibility(GONE);
        //
        Bitmap cardBitmap = MiscUtils.getBitmapFromView(mCardView);
        //show buttons again
        mShareBtn.setVisibility(VISIBLE);
        mShareBtnBckgnd.setVisibility(VISIBLE);
        mEditBtn.setVisibility(VISIBLE);
        mEditBtnBckgnd.setVisibility(VISIBLE);
        //
        if(cardBitmap != null) {
            if(mListener != null) {
                mListener.onRewardDetailsCardFragmentExportRewardDetailCardAsBitmapToFileAndShare(cardBitmap);
            }else{
                Log.e(TAG, "saveAndShareRewardCard::no listener to request");
            }
        }else{
            Log.e(TAG, "saveAndShareRewardCard::BITMAP IS NULL!!");
        }
    }
////////////////////////////////////////
//INTERFACE
    public interface RewardDetailsCardFragmentListener {
        void onRewardDetailsCardFragmentExportRewardDetailCardAsBitmapToFileAndShare(Bitmap chartBitmap);
        void onRewardDetailsCardFragmentEditRewardDetails(Reward reward);
    }
}
