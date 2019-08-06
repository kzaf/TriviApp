package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
import com.zaf.triviapp.adapters.CategoriesAdapter;
import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectCategoryActivity extends AppCompatActivity implements CategoriesAdapter.CategoriesAdapterListItemClickListener{

    public static final String SELECTED_CATEGORY = "selected_category";
    Toolbar toolbar;
    ProgressDialog progressDialog;
    RecyclerView categoriesRecyclerView;
    ArrayList<Category> categoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        toolbarOptions();

        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);

        initializeDialog();
        fetchCategories();
    }

    private void toolbarOptions() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.select_category_menu_items);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        TextView selectCategoryLabel= findViewById(R.id.select_category_label);
        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
        selectCategoryLabel.setText(Html.fromHtml(getResources().getString(R.string.select_category_label)));

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_scores)
                    Toast.makeText(SelectCategoryActivity.this, "Scores", Toast.LENGTH_SHORT).show();
                else if(menuItem.getItemId()== R.id.categories_menu_settings)
                    Toast.makeText(SelectCategoryActivity.this, "Categories", Toast.LENGTH_SHORT).show();
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

        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get instance of Retrofit
        Call<CategoriesList> call = service.getAllCategories(); // Get all categories request

        call.enqueue(new Callback<CategoriesList>() {
            @Override
            public void onResponse(Call<CategoriesList> call, Response<CategoriesList> response) {
                progressDialog.dismiss();
                generateCategoriesList(response.body().getCategory());
                if (response.body() != null) {
                    categoriesList = (ArrayList<Category>) response.body().getCategory();
                }
            }

            @Override
            public void onFailure(Call<CategoriesList> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void generateCategoriesList(List<Category> categoriesList) {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(SelectCategoryActivity.this));
        categoriesRecyclerView.setAdapter(new CategoriesAdapter(this, categoriesList));
    }

    @Override
    public void onListItemClick(int item) {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        intent.putExtra(SELECTED_CATEGORY, categoriesList.get(item));

        startActivity(intent);
    }
}