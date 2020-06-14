package com.zaf.triviapp.dialogs;

import android.app.Activity;
import android.content.Context;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;

public class Dialogs {
    public static void alertDialogExit(Context context){
        final Activity mainActivity = (Activity) context;
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_title))
                .setMessage(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_message))
                .setNegativeBtnText(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_negative_button))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_positive_button))
                .setNegativeBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
                .setGifResource(R.drawable.cancel)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        // TODO
                        //finish();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity,
                                mainActivity.getResources().getString(R.string.gameplay_exit_dialog_keep_going_toast),
                                mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_blue_24dp),
                                mainActivity.getResources().getColor(R.color.colorAccentBlue),
                                mainActivity.getResources().getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

}
