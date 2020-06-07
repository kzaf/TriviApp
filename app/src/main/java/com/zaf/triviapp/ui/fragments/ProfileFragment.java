package com.zaf.triviapp.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.adapters.CategoriesProfileAdapter;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.TaskDao;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.threads.AppExecutors;
import com.zaf.triviapp.ui.MainActivity;
import com.zaf.triviapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends Fragment
        implements CategoriesProfileAdapter.CategoriesProfileAdapterListItemClickListener {

    public static final String SCORES_LIST = "scores_list";
    public static final String SCORES_LAYOUT_MANAGER = "scores_layout_manager";
    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String DATA_SCORES = "DataScores";
    public static final String TOTAL_SCORE = "total score";
    public static final String HAS_INTERNET = "has_internet";
    private boolean hasInternet;
    private ProgressDialog progressDialog;
    private ArrayList<Scores> scoresList;
    private AppDatabase mDb;
    private TaskDao taskDao;
    private MainActivity mainActivity;

    @BindView(R.id.profile_username_tv) TextView userName;
    @BindView(R.id.profile_email_tv) TextView userEmail;
    @BindView(R.id.login_user) TextView loginUser;
    @BindView(R.id.profile_percent) TextView profilePercent;
    @BindView(R.id.profile_success) TextView profileSuccess;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.profile_recycler_view) RecyclerView profileRecyclerView;
    @BindView(R.id.swipe_refresh_layout_profile) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.piechart_sum) PieChart mChart;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile, container, false);
        ButterKnife.bind(this, view);

        mainActivity = ((MainActivity)getActivity());
        mDb = AppDatabase.getInstance(mainActivity);
        taskDao = mDb.taskDao();

        if(savedInstanceState != null){
            // The RecyclerView keeps going back to initial state because the data in Adapter still being populated when we call the onRestoreInstanceState
            // It's a hack to delay the onRestoreInstanceState
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    profileRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(SCORES_LAYOUT_MANAGER));
                }
            }, 300);
            scoresList = savedInstanceState.getParcelableArrayList(SCORES_LIST);
            generateProfileCategoriesList(scoresList);
            this.hasInternet = savedInstanceState.getBoolean(HAS_INTERNET);

        }else{
            Utils utils = new Utils(getActivity());
            this.hasInternet = utils.hasActiveInternetConnection();

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    setupUi(taskDao);
                }
            });

        }
        setupUi(taskDao);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SCORES_LIST, scoresList);
        outState.putParcelable(SCORES_LAYOUT_MANAGER, profileRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putBoolean(HAS_INTERNET, this.hasInternet);
        super.onSaveInstanceState(outState);
    }

    private void setupUi(final TaskDao taskDao){
        initializeDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final UserDetails userDetails = taskDao.loadUserDetails();
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(userDetails == null) {
                            userNotLoggedPopulateUi();
                        } else {
                            userLoggedPopulateUi(userDetails);
                        }
                    }
                });
            }
        }).start();
    }

    private void readScores(final String uid){
        FirebaseDatabase.getInstance().getReference(DATA_SCORES).child("ScoresByUser").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot category: dataSnapshot.getChildren()){
                    final Scores score = new Scores(uid, category.getKey(), Integer.parseInt(category.child("Score").getValue().toString()));
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.taskDao().insertScore(score);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DynamicToast.make(mainActivity, "" + databaseError.getMessage(), getResources()
                        .getColor(R.color.colorAccentRed), getResources()
                        .getColor(R.color.textWhite))
                        .show();
            }
        });
    }

// TODO: Widget

//    private void sendScoresToWidget(List<Scores> scoresList) {
//        Intent intent = new Intent(this, AppWidgetProvider.class);
//        intent.putParcelableArrayListExtra("WidgetUpdatedScore", (ArrayList<? extends Parcelable>) scoresList);
//        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
//        sendBroadcast(intent);
//    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setMessage(getResources().getString(R.string.loading_profile));
        progressDialog.show();
    }

    private void userLoggedPopulateUi(UserDetails userDetails) {
        if(hasInternet){
            readScores(userDetails.getUserId());
        }

        userName.setText(userDetails.getUserName());
        userEmail.setText(userDetails.getUserEmail());

        loginUser.setText(getResources().getString(R.string.profile_logout_button));
        loginUser.setBackgroundResource(R.drawable.custom_border_red);

        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogLogout();
            }
        });
        taskDao.loadAllCategoriesScore().observe(this, new Observer<Scores[]>() {
            @Override
            public void onChanged(@Nullable Scores[] scores) {
                scoresList = new ArrayList<>(Arrays.asList(scores));
                chartOptions(true, setupTotalScore());
                generateProfileCategoriesList(scoresList);
            }
        });
    }

    private void userNotLoggedPopulateUi() {
        userName.setText(getResources().getString(R.string.profile_activity_not_logged_label));
        userEmail.setText("");

        loginUser.setText(getResources().getString(R.string.profile_activity_login_label));
        loginUser.setBackgroundResource(R.drawable.custom_border_blue);

        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.finish();
                Intent intent = new Intent(mainActivity, LoginAuth.class);
                startActivity(intent);
            }
        });
        chartOptions(false, 0);
    }

    private float setupTotalScore(){
        if (scoresList == null) {
            return 0;
        }else{
            float totalScores = 0;
            float sum = 1;
            for (int i=0; i <scoresList.size(); i++){
                totalScores =  totalScores + scoresList.get(i).getCategoryScore();
                sum = i + 1;
            }
            return totalScores/sum;
        }
    }

    private void alertDialogLogout(){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(getString(R.string.profile_activity_dialog_logout_title))
                .setNegativeBtnText(getResources().getString(R.string.profile_activity_dialog_logout_negatibe_btn_text))
                .setPositiveBtnBackground(getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(getResources().getString(R.string.profile_activity_dialog_logout_positive_btn_text))
                .setNegativeBtnBackground(getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
                .setGifResource(R.drawable.cancel)
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mDb.taskDao().deleteUser();
                                mDb.taskDao().resetScore();
                                FirebaseAuth.getInstance().signOut();

                                FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, new SelectCategoryFragment());
                                fragmentTransaction.commit();

                            }
                        }).start();
                        DynamicToast.make(mainActivity, getResources().getString(R.string.gameplay_error_dialog_toast_positive), getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity, getResources().getString(R.string.gameplay_error_dialog_toast_negative), getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

    private void chartOptions(boolean isUserLogged, float scores) {
        if (!isUserLogged){
            Paint paint =  mChart.getPaint(Chart.PAINT_INFO);
            paint.setColor(getResources().getColor(R.color.colorAccentRed));
            profilePercent.setText("");
            profileSuccess.setText("");
            mChart.setNoDataText(getResources().getString(R.string.no_chart));
        }else{
            profilePercent.setText(String.format("%s%%", String.format("%.2f", scores * 10)));
            profileSuccess.setText(TOTAL_SCORE);

            List<PieEntry> pieChartEntries = new ArrayList<>();
            pieChartEntries.add(new PieEntry(scores * 10, getResources().getString(R.string.category_details_activity_pie_entry_success)));
            pieChartEntries.add(new PieEntry((10 - scores) * 10, getResources().getString(R.string.category_details_activity_pie_entry_failure)));

            PieDataSet dataset = new PieDataSet(pieChartEntries, "");
            if(mainActivity.getSharedPref().loadNightModeState()) {
                dataset.setColors(getResources().getColor(R.color.colorAccentBlueDark), getResources().getColor(R.color.colorAccentRedDark));
            } else {
                dataset.setColors(getResources().getColor(R.color.colorAccentBlue), getResources().getColor(R.color.colorAccentRed));
            }            dataset.setSliceSpace(0);
            dataset.setValueTextSize(20);
            dataset.setValueTextColor(android.R.color.white);

            PieData data = new PieData(dataset);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(20);

            mChart.setDrawHoleEnabled(false);
            mChart.setDrawSliceText(false);
            mChart.getDescription().setEnabled(false);
            mChart.getLegend().setEnabled(false);

            mChart.setData(data);
            mChart.invalidate();
        }
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void generateProfileCategoriesList(List<Scores> scoresList) {
        CategoriesProfileAdapter adapter = new CategoriesProfileAdapter(this, scoresList);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        profileRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        profileRecyclerView.scheduleLayoutAnimation();

//        sendScoresToWidget(scoresList);
    }

    @Override
    public void onListItemClick(int item) {

        CategoryDetailsFragment categoryDetailsFragment = new CategoryDetailsFragment();
        Bundle bundle = new Bundle();
        String categoryName = scoresList.get(item).getCategoryName();
        bundle.putParcelable(SELECTED_CATEGORY, new Category(categoryName, 0));
        categoryDetailsFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, categoryDetailsFragment);
        fragmentTransaction.commit();

    }
}
