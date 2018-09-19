package fr.shining_cat.everyday.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

import fr.shining_cat.everyday.R;

public class DialogFragmentEditReward extends DialogFragment {

    public static final String DIALOG_FRAGMENT_EDIT_REWARD_TAG = "dialog_fragment_edit_reward-tag";

    private static final String ARG_REWARD_NAME       = "reward_name_string_argument";
    private static final String ARG_REWARD_LEGS_COLOR = "reward_legs_color_int_argument";
    private static final String ARG_REWARD_BODY_COLOR = "reward_body_color_int_argument";
    private static final String ARG_REWARD_ARMS_COLOR = "reward_arms_color_int_argument";

    private final String TAG = "LOGGING::" + this.getClass().getSimpleName();

    private DialogFragmentEditRewardListener mListener;
    private String mName;
    private int mColorLegs;
    private int mColorBody;
    private int mColorArms;
    private EditText mNameEditTxt;
    private ColorSeekBar mColorSeekBarLegs;
    private ColorSeekBar mColorSeekBarBody;
    private ColorSeekBar mColorSeekBarArms;

////////////////////////////////////////
//this DialogFragment just shows some explication text and the app's infos
    public DialogFragmentEditReward(){
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogFragmentEditReward newInstance(String name, int colorLegs, int colorBody, int colorArms){
        DialogFragmentEditReward dfer = new DialogFragmentEditReward();
        Bundle args = new Bundle();
        args.putString(ARG_REWARD_NAME, name);
        args.putInt(ARG_REWARD_LEGS_COLOR, colorLegs);
        args.putInt(ARG_REWARD_BODY_COLOR, colorBody);
        args.putInt(ARG_REWARD_ARMS_COLOR, colorArms);
        dfer.setArguments(args);
        return dfer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName =  getArguments().getString(ARG_REWARD_NAME);
            mColorLegs =  getArguments().getInt(ARG_REWARD_LEGS_COLOR);
            mColorBody =  getArguments().getInt(ARG_REWARD_BODY_COLOR);
            mColorArms =  getArguments().getInt(ARG_REWARD_ARMS_COLOR);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DialogFragmentEditRewardListener) {
            mListener = (DialogFragmentEditRewardListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement DialogFragmentEditRewardListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogBody =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_edit_reward, null);
        //
        mNameEditTxt = dialogBody.findViewById(R.id.edit_reward_name_editTxt);
        if(mName != null && !mName.isEmpty()){
            mNameEditTxt.setText(mName);
        }
        mColorSeekBarLegs = dialogBody.findViewById(R.id.edit_reward_colorSlider_legs);
        mColorSeekBarLegs.setOnColorChangeListener(onLegsColorChanged);
        mColorSeekBarLegs.setColor(mColorLegs);
        mColorSeekBarBody = dialogBody.findViewById(R.id.edit_reward_colorSlider_body);
        mColorSeekBarBody.setOnColorChangeListener(onBodyColorChanged);
        mColorSeekBarBody.setColor(mColorBody);
        mColorSeekBarArms = dialogBody.findViewById(R.id.edit_reward_colorSlider_arms);
        mColorSeekBarArms.setOnColorChangeListener(onArmsColorChanged);
        mColorSeekBarArms.setColor(mColorArms);
        //
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setView(dialogBody);
        builder.setTitle(getString(R.string.edit_reward_dialog_title));
        builder.setPositiveButton(getString(R.string.generic_string_VALIDATE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirm();
            }
        });
        builder.setNegativeButton(getString(R.string.generic_string_CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel(dialog);
            }
        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel");
        if(mListener!=null){
            mListener.onEditRewardCancel();
        }else{
            Log.e(TAG, "onCancel::no listener!!");
        }
        super.onCancel(dialog);
    }

    private void onConfirm(){
        String name = mNameEditTxt.getText().toString();
        if(mListener!=null){
            mListener.onEditRewardValidate(name, mColorLegs, mColorBody, mColorArms);
        }else{
            Log.e(TAG, "onConfirm::no listener!!");
        }
    }
////////////////////////////////////////
//ColorSeekBar listeners

    ColorSeekBar.OnColorChangeListener onLegsColorChanged = new ColorSeekBar.OnColorChangeListener() {
        @Override
        public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
            mColorLegs = color;
        }
    };

    ColorSeekBar.OnColorChangeListener onBodyColorChanged = new ColorSeekBar.OnColorChangeListener() {
        @Override
        public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
            mColorBody = color;
        }
    };
    ColorSeekBar.OnColorChangeListener onArmsColorChanged = new ColorSeekBar.OnColorChangeListener() {
        @Override
        public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
            mColorArms = color;
        }
    };

////////////////////////////////////////
//Listener interface
    public interface DialogFragmentEditRewardListener {
        void onEditRewardValidate(String name, int colorLegs, int colorBody, int colorArms);
        void onEditRewardCancel();
    }

}
