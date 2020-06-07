package com.zaf.triviapp.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.ui.MainActivity;

import java.util.Arrays;
import java.util.List;

public class LoginAuth extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 101;
    List<AuthUI.IdpConfig> providers;
    private MainActivity mainActivity;
    private AppDatabase mDb;
    private DatabaseReference mFirebaseDatabaseUsers;
    private DatabaseReference mFirebaseDatabaseScores;
    private FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_auth);

        mDb = AppDatabase.getInstance(getApplicationContext());
        mainActivity = new MainActivity();

        // Init providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        showSignInOptions();
    }

    private void showSignInOptions(){
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.triviapp_icon)
                .setTheme(R.style.AppTheme)
                .build(), MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                importUserDetailsToDb(user);
                firebaseInstance(user);
                Intent intent = new Intent(LoginAuth.this, MainActivity.class);
                intent.putExtra("frgToLoad", "profileFragment");
                startActivity(intent);
            }
            finish();
        }
    }

    private void importUserDetailsToDb(final FirebaseUser user){
        final UserDetails userDetails = new UserDetails(user.getUid(), user.getDisplayName(), user.getEmail(), 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDb.taskDao().insertLoggedUser(userDetails);

            }
        }).start();
        DynamicToast.make(getApplicationContext(),
                "Welcome " + user.getDisplayName(),
                getResources().getColor(R.color.colorAccentBlue),
                getResources().getColor(R.color.textWhite))
                .show();
        DynamicToast.make(getApplicationContext(),
                "Welcome " + user.getDisplayName(),
                getResources().getDrawable(R.drawable.ic_sentiment_welcome_smile_black_24dp),
                getResources().getColor(R.color.colorAccentBlue),
                getResources().getColor(R.color.textWhite))
                .show();
    }

    private void firebaseInstance(FirebaseUser user){
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabaseUsers = mFirebaseInstance.getReference("DataUsers");
        mFirebaseDatabaseScores = mFirebaseInstance.getReference("DataScores");

        addUser(user.getUid(), user.getDisplayName(), user.getEmail());
    }

    private void addUser(String uid, String username, String email){
        UserDetails userDetail = new UserDetails(uid, username, email, 0);
        mFirebaseDatabaseUsers.child("UserDetails").child(uid).setValue(userDetail);

        readScores(uid);
    }

    private void readScores(final String uid){
        mFirebaseDatabaseScores.child("ScoresByUser").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot category: dataSnapshot.getChildren()){
                    final Scores score = new Scores(uid, category.getKey(), Integer.parseInt(category.child("Score").getValue().toString()));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mDb.taskDao().insertScore(score);

                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DynamicToast.make(getApplicationContext(), "" + databaseError.getMessage(), getResources()
                        .getColor(R.color.colorAccentRed), getResources()
                        .getColor(R.color.textWhite))
                        .show();
            }
        });
    }
}
