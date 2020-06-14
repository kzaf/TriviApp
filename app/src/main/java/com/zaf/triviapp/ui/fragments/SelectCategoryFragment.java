package com.zaf.triviapp.ui.fragments;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.zaf.triviapp.R;
import com.zaf.triviapp.adapters.CategoriesAdapter;
import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;
import com.zaf.triviapp.ui.MainActivity;
import com.zaf.triviapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectCategoryFragment extends Fragment
        implements CategoriesAdapter.CategoriesAdapterListItemClickListener {

    private static final String SELECTED_CATEGORY = "selected_category";
    private static final String CATEGORIES_LIST = "categories_list";
    private static final String CATEGORIES_LAYOUT_MANAGER = "categories_layout_manager";
    public static final String HAS_INTERNET = "has_internet";
    private boolean hasInternet;
    private ProgressDialog progressDialog;
    private ArrayList<Category> categoriesList;
    private MainActivity mainActivity;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.categories_recycler_view) RecyclerView categoriesRecyclerView;
    @BindView(R.id.select_category_label) TextView selectCategoryLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_select_category, container, false);
        ButterKnife.bind(this, view);

        mainActivity = ((MainActivity)getActivity());

        if(savedInstanceState != null){
            this.hasInternet = savedInstanceState.getBoolean(HAS_INTERNET);
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
            Utils utils = new Utils(getActivity());
            this.hasInternet = utils.hasActiveInternetConnection();

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchCategories();
                    initUi();
                }
            });

            fetchCategories();
        }
        initUi();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(CATEGORIES_LIST, categoriesList);
        outState.putParcelable(CATEGORIES_LAYOUT_MANAGER, categoriesRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putBoolean(HAS_INTERNET, this.hasInternet);
        super.onSaveInstanceState(outState);
    }

    private void generateCategoriesList(List<Category> categoriesList) {
        CategoriesAdapter adapter = new CategoriesAdapter(this, categoriesList);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoriesRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        categoriesRecyclerView.scheduleLayoutAnimation();
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(mainActivity.getResources().getString(R.string.loading_categories));
        progressDialog.show();
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    private void initUi(){
        mainActivity.setBackButtonVisibility(false);
        mainActivity.toolbarOptions(this);
        if(mainActivity.getSharedPref().loadNightModeState()) {
            if (hasInternet) {
                selectCategoryLabel.setText(Html.fromHtml(mainActivity.getResources().getString(R.string.select_category_label_dark)));
            } else {
                selectCategoryLabel.setText(Html.fromHtml(mainActivity.getResources().getString(R.string.no_internet_label_dark)));
            }
        } else {
            if (hasInternet) {
                selectCategoryLabel.setText(Html.fromHtml(mainActivity.getResources().getString(R.string.select_category_label)));
            } else {
                selectCategoryLabel.setText(Html.fromHtml(mainActivity.getResources().getString(R.string.no_internet_label)));
            }
        }
    }

    private void fetchCategories() {
        initializeDialog();

        if (!hasInternet){
            DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.select_category_no_internet_label), mainActivity.getResources()
                    .getColor(R.color.colorAccentRed), mainActivity.getResources()
                    .getColor(R.color.textWhite))
                    .show();
            progressDialog.dismiss();
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }else{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get instance of Retrofit
            Call<CategoriesList> call = service.getAllCategories(); // Get all categories request

            call.enqueue(new Callback<CategoriesList>() {
                @Override
                public void onResponse(Call<CategoriesList> call, Response<CategoriesList> response) {
                    progressDialog.dismiss();
                    mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
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
                    mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    @Override
    public void onListItemClick(int item) {

        CategoryDetailsFragment categoryDetailsFragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SELECTED_CATEGORY, categoriesList.get(item));
        categoryDetailsFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, categoryDetailsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

}