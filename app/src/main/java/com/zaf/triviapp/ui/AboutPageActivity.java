package com.zaf.triviapp.ui;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutPageActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    @BindView(R.id.toolbar_title_about) TextView toolbarTitle;
    @BindView(R.id.mail_tv) TextView mailTextView;
    @BindView(R.id.about_text) TextView aboutTextView;
    @BindView(R.id.back_button_about) ImageView back;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        ButterKnife.bind(this);

        toolbarOptions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aboutTextView.setText(Html.fromHtml(getResources().getString(R.string.about_description),
                    Html.FROM_HTML_MODE_COMPACT));
        } else {
            aboutTextView.setText(Html.fromHtml(getResources().getString(R.string.about_description)));
        }

        initAdMob();
    }

    private void initAdMob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(sharedPref.loadNightModeState()) {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label_dark)));
        } else {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
        }
    }
}
