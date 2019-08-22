package com.zaf.triviapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.threads.AppExecutors;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.preferences.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import lib.kingja.switchbutton.SwitchMultiButton;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.about_tv) TextView aboutButton;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.theme_switch) Switch themeSwitch;
    @BindView(R.id.vibrate_switch) Switch vibrateSwitch;
    @BindView(R.id.button_delete_account)LinearLayout deleteAccount;
    @BindView(R.id.button_reset_score)LinearLayout resetScore;
    private String emailReauth, passReauth;
    private NDialog nDialog;
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
//                deleteUserFromFirebase();
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
                startActivity(new Intent(SettingsActivity.this, AboutPageActivity.class));
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
                        deleteUserFromFirebase();

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

    private void deleteUserFromFirebase(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            DynamicToast.make(getApplicationContext(), "You have to be logged", getResources()
                    .getColor(R.color.colorAccentRed), getResources()
                    .getColor(R.color.textWhite))
                    .show();
        }else{
            reauthenticateDialog(user);

        }
    }

    private void reauthenticateDialog(final FirebaseUser user){
        View dialogLayout = View.inflate(this, R.layout.reauth_dialog_layout, null);

        nDialog = new NDialog(SettingsActivity.this, ButtonType.TWO_BUTTON);
        nDialog.setIcon(R.drawable.triviapp_icon);
        nDialog.setTitle("Please enter you credentials");
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
                        reauthAndDelete();
                    case NDialog.BUTTON_NEGATIVE:
                        break;
                }
            }
            private void reauthAndDelete() {
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
                                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mDb.taskDao().deleteUser();
                                                    mDb.taskDao().resetScore();
                                                }
                                            });
                                            deleteFirebaseRecords();
                                            DynamicToast.make(getApplicationContext(), "Bye bye..!", getResources()
                                                    .getColor(R.color.colorAccentBlue), getResources()
                                                    .getColor(R.color.textWhite))
                                                    .show();
                                            finish();
                                            startActivity(new Intent(SettingsActivity.this, SelectCategoryActivity.class));
                                        }
                                    }
                                });
                        }
                    });
            }
            private void deleteFirebaseRecords(){
                Query scoresQuery = FirebaseDatabase.getInstance().getReference().child("DataScores").child("ScoresByUser").child(user.getUid());
                Query usersQuery = FirebaseDatabase.getInstance().getReference().child("DataUsers").child("UserDetails").child(user.getUid());

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
        };
        nDialog.setPositiveButtonText("Delete!");
        nDialog.setPositiveButtonTextColor(Color.RED);
        nDialog.setPositiveButtonClickListener(buttonClickListener);

        nDialog.setNegativeButtonText("Back");
        nDialog.setNegativeButtonTextColor(Color.BLUE);
        nDialog.setNegativeButtonClickListener(buttonClickListener);

        nDialog.show();
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
