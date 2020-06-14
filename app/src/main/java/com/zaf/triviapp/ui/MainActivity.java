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
import androidx.fragment.app.Fragment;

import com.zaf.triviapp.R;
import com.zaf.triviapp.dialogs.Dialogs;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.ui.fragments.CategoryDetailsFragment;
import com.zaf.triviapp.ui.fragments.GameplayFragment;
import com.zaf.triviapp.ui.fragments.ProfileFragment;
import com.zaf.triviapp.ui.fragments.SelectCategoryFragment;
import com.zaf.triviapp.ui.fragments.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    private String intentFragment = "";
    private Dialogs dialogs;
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
        dialogs = new Dialogs(MainActivity.this);

        toolbarOptions(new SelectCategoryFragment());
        initFragment();
    }

    @SuppressLint("RestrictedApi")
        public void toolbarOptions(final Fragment currentFragment) {
        toolbar.getMenu().clear();
        if(toolbar.getMenu() instanceof MenuBuilder){
            ((MenuBuilder) toolbar.getMenu()).setOptionalIconsVisible(true);
        }
        if (currentFragment instanceof ProfileFragment){
            toolbar.inflateMenu(R.menu.profile_menu_items);
        }else if (currentFragment instanceof GameplayFragment) {
            toolbar.inflateMenu(R.menu.gameplay_menu_item);
        }else if (currentFragment instanceof SettingsFragment){
            toolbar.getMenu().clear();
        }else {
            toolbar.inflateMenu(R.menu.select_category_menu_items);
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.categories_menu_profile) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment(), "profileFragment")
                            .addToBackStack(null)
                            .commit();
                } else if(menuItem.getItemId() == R.id.categories_menu_settings || menuItem.getItemId() == R.id.profile_settings) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SettingsFragment(), "settingsFragment")
                            .addToBackStack(null)
                            .commit();
                } else if(menuItem.getItemId() == R.id.categories_menu_refresh){
                    if (currentFragment instanceof CategoryDetailsFragment){
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new CategoryDetailsFragment(), "categoryDetailsFragment")
                                .addToBackStack(null)
                                .commit();
                    }else {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SelectCategoryFragment(), "selectCategoryFragment")
                                .addToBackStack(null)
                                .commit();
                    }
                }else if(menuItem.getItemId() == R.id.gameplay_exit){
                    dialogs.alertDialogExit();
                }
                return false;
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
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof GameplayFragment) {
            return;
        }
        if(getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else if(getFragmentManager().getBackStackEntryCount() == 1) {
            moveTaskToBack(false);
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    public void initFragment() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            intentFragment = bundle.getString("frgToLoad");
        }
        if (intentFragment == null || !intentFragment.equals("profileFragment")){
            SelectCategoryFragment selectCategoryFragment = new SelectCategoryFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, selectCategoryFragment, "selectCategoryFragment")
                    .commit();
        }else{
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, profileFragment, "profileFragment")
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        previousFragment();
    }

    public SharedPref getSharedPref() {
        return sharedPref;
    }

    public Dialogs getDialogs() {
        return dialogs;
    }

    public void setBackButtonVisibility(boolean toBeVisible){
        if (toBeVisible){
            backButton.setVisibility(View.VISIBLE);
        }else {
            backButton.setVisibility(View.INVISIBLE);
        }
    }
}
