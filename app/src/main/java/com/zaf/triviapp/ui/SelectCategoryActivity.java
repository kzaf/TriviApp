package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.adapters.CategoriesAdapter;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectCategoryActivity extends AppCompatActivity
        implements CategoriesAdapter.CategoriesAdapterListItemClickListener {

    private static final String SELECTED_CATEGORY = "selected_category";
    private static final String CATEGORIES_LIST = "categories_list";
    private static final String CATEGORIES_LAYOUT_MANAGER = "categories_layout_manager";
    public static final String WIFI = "WIFI";
    public static final String MOBILE = "MOBILE";
    private ProgressDialog progressDialog;
    private ArrayList<Category> categoriesList;
    private SharedPref sharedPref;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.categories_recycler_view) RecyclerView categoriesRecyclerView;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.select_category_label) TextView selectCategoryLabel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        ButterKnife.bind(this);

        toolbarOptions();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchCategories();
            }
        });

        if(savedInstanceState != null){
            // The RecyclerView keeps going back to initial state because the data in Adapter still being populated when we call the onRestoreInstanceState
            // It's a hack to delay the onRestoreInstanceState
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    categoriesRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(CATEGORIES_LAYOUT_MANAGER));
                }
            }, 300);
            categoriesList = savedInstanceState.getParcelableArrayList(CATEGORIES_LIST);
            generateCategoriesList(categoriesList);
        }else{
            fetchCategories();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CATEGORIES_LIST, categoriesList);
        outState.putParcelable(CATEGORIES_LAYOUT_MANAGER, categoriesRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onListItemClick(int item) {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        intent.putExtra(SELECTED_CATEGORY, categoriesList.get(item));

        startActivity(intent);
    }

    private void toolbarOptions() {
        toolbar.inflateMenu(R.menu.select_category_menu_items);
        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
        if (haveNetworkConnection()) selectCategoryLabel.setText(Html.fromHtml(getResources().getString(R.string.select_category_label)));
        else selectCategoryLabel.setText(Html.fromHtml(getResources().getString(R.string.no_internet_label)));
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_profile) startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                else if(menuItem.getItemId()== R.id.categories_menu_settings) startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                else fetchCategories();
                return false;
            }
        });
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(SelectCategoryActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_categories));
        progressDialog.show();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    private void fetchCategories() {
        initializeDialog();

        if (!haveNetworkConnection()){
            DynamicToast.make(getApplicationContext(), getResources().getString(R.string.select_category_no_internet_label), getResources()
                    .getColor(R.color.colorAccentRed), getResources()
                    .getColor(R.color.textWhite))
                    .show();
            progressDialog.dismiss();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }else{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get instance of Retrofit
            Call<CategoriesList> call = service.getAllCategories(); // Get all categories request

            call.enqueue(new Callback<CategoriesList>() {
                @Override
                public void onResponse(Call<CategoriesList> call, Response<CategoriesList> response) {
                    progressDialog.dismiss();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    generateCategoriesList(response.body().getCategory());
                    if (response.body() != null) {
                        categoriesList = (ArrayList<Category>) response.body().getCategory();
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
                @Override
                public void onFailure(Call<CategoriesList> call, Throwable t) {
                    progressDialog.dismiss();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(WIFI))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase(MOBILE))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void generateCategoriesList(List<Category> categoriesList) {
        CategoriesAdapter adapter = new CategoriesAdapter(this, categoriesList);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(SelectCategoryActivity.this));
        categoriesRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        categoriesRecyclerView.scheduleLayoutAnimation();
    }
}