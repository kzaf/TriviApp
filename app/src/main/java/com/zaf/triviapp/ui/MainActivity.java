package com.zaf.triviapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.ui.fragments.SelectCategoryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    @BindView(R.id.back_button_main) ImageView backButton;
    @BindView(R.id.logo_image) ImageView logoImage;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.toolbar) Toolbar toolbar;


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

    @SuppressLint("RestrictedApi")
    private void toolbarOptions() {
        toolbar.inflateMenu(R.menu.select_category_menu_items);
        if(toolbar.getMenu() instanceof MenuBuilder){
            ((MenuBuilder) toolbar.getMenu()).setOptionalIconsVisible(true);
        }
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_profile) {
                    // TODO
                    //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if(menuItem.getItemId()== R.id.categories_menu_settings) {
                    // TODO
                    //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                } else {
                    // TODO
                    //fetchCategories();
                }
                return false;
            }
        });

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

    public SharedPref getSharedPref() {
        return sharedPref;
    }

    public void setBackButtonVisibility(boolean toBeVisible){
        if (toBeVisible){
            backButton.setVisibility(View.VISIBLE);
        }{
            backButton.setVisibility(View.INVISIBLE);
        }
    }
}
