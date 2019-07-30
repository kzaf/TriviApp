package com.zaf.triviapp.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        TextView labelTv = findViewById(R.id.signup_label);
        labelTv.setText(Html.fromHtml("<font color=#0031AA>Trivi</font><font color=#AD0000>App</font>"));
    }
}
