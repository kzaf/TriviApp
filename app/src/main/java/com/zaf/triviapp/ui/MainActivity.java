package com.zaf.triviapp.ui;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    @BindView(R.id.back_button_main) ImageView backButton;
    @BindView(R.id.logo_image) ImageView logoImage;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        toolbarOptions();
        initFragment();
    }

    private void toolbarOptions() {

        logoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousFragment();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousFragment();
            }
        });

        if(sharedPref.loadNightModeState()) {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label_dark)));
        } else {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
        }
    }

    private void previousFragment(){

    }

    private void initFragment() {
        SelectCategoryFragment selectCategoryFragment = new SelectCategoryFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, selectCategoryFragment, "selectCategoryFragment")
                .commit();
    }

}
