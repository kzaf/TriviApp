package com.zaf.triviapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.AppExecutors;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.preferences.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.theme_switch) Switch themeSwitch;
    @BindView(R.id.vibrate_switch) Switch vibrateSwitch;
    @BindView(R.id.button_delete_account)LinearLayout deleteAccount;
    @BindView(R.id.button_reset_score)LinearLayout resetScore;
    private SharedPref sharedPref;
    private Vibrator vibe;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDb = AppDatabase.getInstance(getApplicationContext());

        ButterKnife.bind(this);

        switchStateChange();
        buttonOptions();

        toolbarOptions();
    }

    private void switchStateChange() {
        if(sharedPref.loadNightModeState()){
            themeSwitch.setChecked(true);
        }
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sharedPref.setNightModeEnabled(true);
                    restartApp(SettingsActivity.class);
                }else{
                    sharedPref.setNightModeEnabled(false);
                    restartApp(SettingsActivity.class);
                }
            }
        });

        if(sharedPref.loadVibrateState()) vibrateSwitch.setChecked(true);

        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sharedPref.setVibrateEnabled(true);
                    vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(50);
                }else{
                    sharedPref.setVibrateEnabled(false);
                }
            }
        });
    }

    private void buttonOptions(){
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDeleteAccount();
            }
        });

        resetScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertResetScores();
            }
        });
    }

    private void alertDialogDeleteAccount(){
        new FancyGifDialog.Builder(this)
                .setTitle("Are you sure you want to delete your account?")
                .setMessage("You will permanently lose your score if you delete, are you sure?")
                .setNegativeBtnText("Nop! Keep me signed!")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("Yes I am sure!")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.cancel)
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.taskDao().deleteUser();
                                mDb.taskDao().resetScore();
                                // TODO: Firebase
                            }
                        });
                        DynamicToast.make(getApplicationContext(), "Bye bye..!", getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(getApplicationContext(), "Cool!", getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }


    private void alertResetScores(){
        new FancyGifDialog.Builder(this)
            .setTitle("Are you sure you want to reset your score?")
            .setPositiveBtnBackground("#b80c00")
            .setNegativeBtnBackground("#FFA9A7A8")
            .setPositiveBtnText("Yes")
            .setNegativeBtnText("Nop!")
            .setGifResource(R.drawable.reset)
            .isCancellable(true)
            .OnPositiveClicked(new FancyGifDialogListener() {
                @Override
                public void OnClick() {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            final UserDetails userDetails = mDb.taskDao().loadUserDetails();
                            if(userDetails != null){
                                mDb.taskDao().resetScore();
                                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DynamicToast.make(getApplicationContext(), "Your score has been reset!", getResources()
                                                .getColor(R.color.orange), getResources()
                                                .getColor(R.color.textBlack))
                                                .show();
                                    }
                                });
                            }else{
                                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DynamicToast.make(getApplicationContext(), "You are not logged in!", getResources()
                                                .getColor(R.color.orange), getResources()
                                                .getColor(R.color.textBlack))
                                                .show();
                                    }
                                });
                            }
                        }
                    });
                }
            })
            .OnNegativeClicked(new FancyGifDialogListener() {
                @Override
                public void OnClick() {
                    DynamicToast.make(getApplicationContext(), "Cool!", getResources()
                            .getColor(R.color.colorAccentBlue), getResources()
                            .getColor(R.color.textWhite))
                            .show();
                    }
        }).build();
    }



    private void restartApp(Class c){
        Intent intent = new Intent(getApplicationContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartApp(SelectCategoryActivity.class);
                finish();
            }
        });
        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
    }

    @Override
    public void onBackPressed() {
        restartApp(SelectCategoryActivity.class);
    }
}
