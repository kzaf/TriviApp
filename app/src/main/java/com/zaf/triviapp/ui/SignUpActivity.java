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

public class SignUpActivity extends AppCompatActivity {

    private EditText userName;
    private EditText email;
    private EditText password;
    private EditText repeatPassword;
    private TextView labelTv;
    private Button signUpButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        labelTv = findViewById(R.id.signup_label);
        signUpButton = findViewById(R.id.signup_button);
        userName = findViewById(R.id.signup_username);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        repeatPassword = findViewById(R.id.signup_repeat_password);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpCLick();
            }
        });

        labelTv.setText(Html.fromHtml("<font color=#0031AA>Trivi</font><font color=#AD0000>App</font>"));
    }

    private void signUpCLick(){

        if(!userName.getText().toString().equals("") || !email.getText().toString().equals("") || !password.getText().toString().equals("")){
            if(password.getText().toString().equals(repeatPassword.getText().toString())){
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    DynamicToast.makeSuccess(SignUpActivity.this, "Registration successful!").show();
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    intent.putExtra("LoggedUser", user);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    DynamicToast.makeError(SignUpActivity.this, "Authentication failed.").show();
                                }
                            }
                        });
            }
            else{
                DynamicToast.makeWarning(SignUpActivity.this, "Passwords does not match!").show();
            }
        }
        else{
            DynamicToast.makeWarning(SignUpActivity.this, "Please fill in all of the fields!").show();
        }


    }

}
