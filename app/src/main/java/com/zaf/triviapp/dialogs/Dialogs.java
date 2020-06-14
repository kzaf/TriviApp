package com.zaf.triviapp.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.ui.MainActivity;
import com.zaf.triviapp.ui.fragments.CategoryDetailsFragment;

import static com.zaf.triviapp.ui.fragments.ProfileFragment.SELECTED_CATEGORY;

public class Dialogs {

    MainActivity mainActivity;
    Context context;

    public Dialogs(Context context) {
        this.context = context;
        this.mainActivity = (MainActivity) context;
    }

    public void alertDialogExit(){
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
                        mainActivity.initFragment();
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

    public void errorDialog(final Category selectedCategory){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.gameplay_error_dialog_title))
                .setMessage(mainActivity.getResources().getString(R.string.gameplay_error_dialog_message))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_text))
                .setGifResource(R.drawable.error)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        CategoryDetailsFragment categoryDetailsFragment = new CategoryDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(SELECTED_CATEGORY, selectedCategory);
                        categoryDetailsFragment.setArguments(bundle);

                        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, categoryDetailsFragment);
                        fragmentTransaction.commit();
                    }
                })
                .build();
    }

}
