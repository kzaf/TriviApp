package com.zaf.triviapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.theme_switch) Switch themeSwitch;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

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

        toolbarOptions();
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
}
