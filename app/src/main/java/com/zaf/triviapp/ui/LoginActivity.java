package com.zaf.triviapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.zaf.triviapp.MainActivity;
import com.zaf.triviapp.R;

public class LoginActivity extends AppCompatActivity {

//    private static final String TAG = LoginActivity.class.getName();
//    private static final String GOOGLE_ACCOUNT = "google_account";

    private EditText email;
    private EditText password;
//    private Button googleSignInButton;
    private Button signInButton;
//    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        email = findViewById(R.id.signin_email);
        password = findViewById(R.id.signin_password);
//        googleSignInButton = findViewById(R.id.login_with_google_button);
        signInButton = findViewById(R.id.login_button);
        TextView labelTv = findViewById(R.id.login_label);
        TextView signupTv = findViewById(R.id.signup_button);

        labelTv.setText(Html.fromHtml("<font color=#0031AA>Trivi</font><font color=#AD0000>App</font>"));
        signupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                LoginActivity.this.startActivity(mainIntent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        googleSignInClient = GoogleSignIn.getClient(this, gso);
//        googleSignInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent signInIntent = googleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, 101);
//            }
//        });
    }

    private void loginUser() {
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            DynamicToast.makeSuccess(LoginActivity.this, "Welcome!").show();
                            startApplication(user);
                        } else {
                            DynamicToast.makeError(LoginActivity.this, "Authentication failed.").show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        startApplication(currentUser);

//        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
//        if (alreadyloggedAccount != null) {
//            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
//            onLoggedIn(alreadyloggedAccount);
//        } else {
//            Log.d(TAG, "Not logged in");
//        }
    }

    private void startApplication(FirebaseUser currentUser) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("LoggedUser", user);
            startActivity(intent);
            finish();
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK)
//            switch (requestCode) {
//                case 101:
//                    try {
//                        // The Task returned from this call is always completed, no need to attach a listener.
//                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                        GoogleSignInAccount account = task.getResult(ApiException.class);
//                        onLoggedIn(account);
//                    } catch (ApiException e) {
//                        // The ApiException status code indicates the detailed failure reason.
//                        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//                    }
//                    break;
//            }
//    }
//
//    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(GOOGLE_ACCOUNT, googleSignInAccount);
//
//        startActivity(intent);
//        finish();
//    }
}
