package com.zaf.triviapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gohn.nativedialog.ButtonClickListener;
import com.gohn.nativedialog.ButtonType;
import com.gohn.nativedialog.NDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.ui.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    public static final String DATA_SCORES = "DataScores";
    public static final String DATA_USERS = "DataUsers";
    public static final String SCORES_BY_USER = "ScoresByUser";
    public static final String USER_DETAILS = "UserDetails";
    @BindView(R.id.theme_switch) Switch themeSwitch;
    @BindView(R.id.vibrate_switch) Switch vibrateSwitch;
    @BindView(R.id.button_delete_account) LinearLayout deleteAccount;
    @BindView(R.id.button_reset_score)LinearLayout resetScore;
    @BindView(R.id.about_tv) TextView aboutButton;
    private String emailReauth, passReauth;
    private Vibrator vibe;
    private NDialog nDialog;
    private MainActivity mainActivity;
    private AppDatabase mDb;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);
        ButterKnife.bind(this, view);

        mainActivity = ((MainActivity)getActivity());
        mDb = AppDatabase.getInstance(mainActivity);

        switchStateChange();
        buttonOptions();

        return view;
    }

    private void switchStateChange() {
        if(mainActivity.getSharedPref().loadNightModeState()){
            themeSwitch.setChecked(true);
        }
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mainActivity.getSharedPref().setNightModeEnabled(true);
                }else{
                    mainActivity.getSharedPref().setNightModeEnabled(false);
                }
                restartApp(mainActivity.getClass());
            }
        });

        if(mainActivity.getSharedPref().loadVibrateState()) {
            vibrateSwitch.setChecked(true);
        }else{
            vibrateSwitch.setChecked(false);
        }

        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mainActivity.getSharedPref().setVibrateEnabled(true);
                    vibe = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibe != null) {
                        vibe.vibrate(50);
                    }
                }else{
                    mainActivity.getSharedPref().setVibrateEnabled(false);
                }
            }
        });
    }

    private void buttonOptions(){
        mainActivity.setBackButtonVisibility(true);
        mainActivity.toolbarOptions(this);
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
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new AboutPageFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void alertResetScores(){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.settings_activity_alert_reset_score_title))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setNegativeBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.settings_activity_alert_reset_score_positive_button_text))
                .setNegativeBtnText(mainActivity.getResources().getString(R.string.settings_activity_alert_reset_score_negative_button_text))
                .setGifResource(R.drawable.reset)
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        resetScorePositiveAlertButtonClick();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.gameplay_error_dialog_toast_negative), mainActivity.getResources()
                                .getColor(R.color.colorAccentBlue), mainActivity.getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                }).build();
    }

    private void resetScorePositiveAlertButtonClick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final UserDetails userDetails = mDb.taskDao().loadUserDetails();
                if(userDetails != null){
                    mDb.taskDao().resetScore();
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Query scoresQuery = FirebaseDatabase.getInstance().getReference().child(DATA_SCORES).child(SCORES_BY_USER).child(userDetails.getUserId());
                            scoresQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().removeValue();
                                    }
                                    DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.settings_activity_reset_score_toast), getResources()
                                            .getColor(R.color.orange), mainActivity.getResources()
                                            .getColor(R.color.textBlack))
                                            .show();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                        }
                    });

                }else{
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.settings_activity_reset_score_fail_toast), getResources()
                                    .getColor(R.color.orange), mainActivity.getResources()
                                    .getColor(R.color.textBlack))
                                    .show();
                        }
                    });
                }
            }
        }).start();
    }


    private void restartApp(Class c){
        Intent intent = new Intent(mainActivity, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void alertDialogDeleteAccount(){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.settings_activity_delete_account_dialog_title))
                .setMessage(mainActivity.getResources().getString(R.string.settings_activity_delete_account_dialog_message))
                .setNegativeBtnText(mainActivity.getResources().getString(R.string.settings_activity_delete_account_dialog_negative_button_text))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.settings_activity_delete_account_dialog_positive_button_text))
                .setNegativeBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
                .setGifResource(R.drawable.cancel)
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        deleteUserFromFirebase();

                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.gameplay_error_dialog_toast_negative), mainActivity.getResources()
                                .getColor(R.color.colorAccentBlue), mainActivity.getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

    private void deleteUserFromFirebase(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.settings_activity_delete_from_firebase_toast), mainActivity.getResources()
                    .getColor(R.color.colorAccentRed), mainActivity.getResources()
                    .getColor(R.color.textWhite))
                    .show();
        }else{
            reauthenticateDialog(user);

        }
    }

    private void reauthenticateDialog(final FirebaseUser user){
        View dialogLayout = View.inflate(mainActivity, R.layout.reauth_dialog_layout, null);

        nDialog = new NDialog(mainActivity, ButtonType.TWO_BUTTON);
        nDialog.setIcon(R.drawable.triviapp_icon);
        nDialog.setTitle(mainActivity.getResources().getString(R.string.settings_activity_reauth_ndialog_title));
        nDialog.setCustomView(dialogLayout);

        final EditText email = dialogLayout.findViewById(R.id.email_et);
        final EditText pass = dialogLayout.findViewById(R.id.pass_et);

        ButtonClickListener buttonClickListener = new ButtonClickListener() {
            @Override
            public void onClick(int button) {
                switch (button) {
                    case NDialog.BUTTON_POSITIVE:
                        emailReauth = email.getText().toString();
                        passReauth = pass.getText().toString();
                        reauthAndDelete(user);
                    case NDialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        nDialog.setPositiveButtonText(mainActivity.getResources().getString(R.string.settings_activity_reauth_ndialog_positive_button_text));
        nDialog.setPositiveButtonTextColor(Color.RED);
        nDialog.setPositiveButtonClickListener(buttonClickListener);

        nDialog.setNegativeButtonText(mainActivity.getResources().getString(R.string.settings_activity_reauth_ndialog_negative_button_text));
        nDialog.setNegativeButtonTextColor(Color.BLUE);
        nDialog.setNegativeButtonClickListener(buttonClickListener);

        nDialog.show();
    }

    private void reauthAndDelete(final FirebaseUser user) {
        AuthCredential credential = EmailAuthProvider.getCredential(emailReauth, passReauth);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mDb.taskDao().deleteUser();
                                                    mDb.taskDao().resetScore();
                                                }
                                            }).start();
                                            deleteFirebaseRecords(user);
                                            DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.settings_activity_delete_user_toast), mainActivity.getResources()
                                                    .getColor(R.color.colorAccentBlue), mainActivity.getResources()
                                                    .getColor(R.color.textWhite))
                                                    .show();

                                            FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_container, new SelectCategoryFragment());
                                            fragmentTransaction.addToBackStack(null);
                                            fragmentTransaction.commit();
                                        }
                                    }
                                });
                    }
                });
    }

    private void deleteFirebaseRecords(FirebaseUser user){
        Query scoresQuery = FirebaseDatabase.getInstance().getReference().child(DATA_SCORES).child(SCORES_BY_USER).child(user.getUid());
        Query usersQuery = FirebaseDatabase.getInstance().getReference().child(DATA_USERS).child(USER_DETAILS).child(user.getUid());

        scoresQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
