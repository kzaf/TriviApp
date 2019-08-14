package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
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

    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String CATEGORIES_LIST = "categories_list";
    public static final String CATEGORIES_LAYOUT_MANAGER = "categories_layout_manager";
    ProgressDialog progressDialog;
    ArrayList<Category> categoriesList;
    SharedPref sharedPref;
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(CATEGORIES_LIST, categoriesList);
        outState.putParcelable(CATEGORIES_LAYOUT_MANAGER, categoriesRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                categoriesRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(CATEGORIES_LAYOUT_MANAGER));
            }
        }, 300);
        categoriesList = savedInstanceState.getParcelableArrayList(CATEGORIES_LIST);
        generateCategoriesList(categoriesList);
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
        selectCategoryLabel.setText(Html.fromHtml(getResources().getString(R.string.select_category_label)));
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_profile) startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                else if(menuItem.getItemId()== R.id.categories_menu_settings) startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                else if(menuItem.getItemId()== R.id.categories_menu_refresh) fetchCategories();
                else{
                    if (FirebaseAuth.getInstance().getCurrentUser() == null){
                        Intent intent = new Intent(SelectCategoryActivity.this, LoginAuth.class);
                        startActivity(intent);
                    }else{
                        AuthUI.getInstance()
                                .signOut(SelectCategoryActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //showSignInOptions();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SelectCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                return false;
            }
        });
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(SelectCategoryActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_categories));
        progressDialog.show();
    }

    private void fetchCategories() {
        initializeDialog();

        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get instance of Retrofit
        Call<CategoriesList> call = service.getAllCategories(); // Get all categories request

        call.enqueue(new Callback<CategoriesList>() {
            @Override
            public void onResponse(Call<CategoriesList> call, Response<CategoriesList> response) {
                progressDialog.dismiss();
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
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void generateCategoriesList(List<Category> categoriesList) {
        CategoriesAdapter adapter = new CategoriesAdapter(this, categoriesList);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(SelectCategoryActivity.this));
        categoriesRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        categoriesRecyclerView.scheduleLayoutAnimation();
    }
}