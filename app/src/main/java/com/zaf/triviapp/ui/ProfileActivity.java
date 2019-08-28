package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.threads.AppExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity
        implements CategoriesProfileAdapter.CategoriesProfileAdapterListItemClickListener{

    public static final String SCORES_LIST = "scores_list";
    public static final String SCORES_LAYOUT_MANAGER = "scores_layout_manager";
    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String WIFI = "WIFI";
    public static final String MOBILE = "MOBILE";
    public static final String DATA_SCORES = "DataScores";
    public static final String TOTAL_SCORE = "total score";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.profile_username_tv) TextView userName;
    @BindView(R.id.profile_email_tv) TextView userEmail;
    @BindView(R.id.login_user) TextView loginUser;
    @BindView(R.id.profile_percent) TextView profilePercent;
    @BindView(R.id.profile_success) TextView profileSuccess;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.profile_recycler_view) RecyclerView profileRecyclerView;
    @BindView(R.id.swipe_refresh_layout_profile) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.piechart_sum) PieChart mChart;
    private ProgressDialog progressDialog;
    private ArrayList<Scores> scoresList;
    private SharedPref sharedPref;
    private AppDatabase mDb;
    private TaskDao taskDao;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        mDb = AppDatabase.getInstance(getApplicationContext());
        taskDao = mDb.taskDao();

        toolbarOptions();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupUi(taskDao);
            }
        });

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
            setupUi(taskDao);

        }else{
            setupUi(taskDao);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SCORES_LIST, scoresList);
        outState.putParcelable(SCORES_LAYOUT_MANAGER, profileRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    private void setupUi(final TaskDao taskDao){
        initializeDialog();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                UserDetails userDetails = taskDao.loadUserDetails();
                if(userDetails == null){
                    userNotLoggedPopulateUi();
                }else{
                    userLoggedPopulateUi(userDetails);
                }
            }
        });
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
                DynamicToast.make(getApplicationContext(), "" + databaseError.getMessage(), getResources()
                        .getColor(R.color.colorAccentRed), getResources()
                        .getColor(R.color.textWhite))
                        .show();
            }
        });
    }

    private void userLoggedPopulateUi(UserDetails userDetails) {
        if(haveNetworkConnection()){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) progressDialog.dismiss();
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_profile));
        progressDialog.show();
    }

    private void userNotLoggedPopulateUi() {
        userName.setText(getResources().getString(R.string.profile_activity_not_logged_label));
        userEmail.setText("");

        loginUser.setText(getResources().getString(R.string.profile_activity_login_label));
        loginUser.setBackgroundResource(R.drawable.custom_border_blue);

        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ProfileActivity.this, LoginAuth.class);
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
        new FancyGifDialog.Builder(this)
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
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.taskDao().deleteUser();
                                mDb.taskDao().resetScore();
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(ProfileActivity.this, SelectCategoryActivity.class));
                            }
                        });
                        DynamicToast.make(getApplicationContext(), getResources().getString(R.string.gameplay_error_dialog_toast_positive), getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(getApplicationContext(), getResources().getString(R.string.gameplay_error_dialog_toast_negative), getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelectCategoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));

        toolbar.inflateMenu(R.menu.profile_menu_items);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.profile_settings) startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return false;
            }
        });
    }

    private void chartOptions(boolean isUserLogged, float scores) {
        if (!isUserLogged){
            Paint paint =  mChart.getPaint(Chart.PAINT_INFO);
            paint.setColor(getResources().getColor(R.color.colorAccentRed));
            profilePercent.setText("");
            profileSuccess.setText("");
            mChart.setNoDataText(getResources().getString(R.string.no_chart));
        }else{
            profilePercent.setText(scores * 10 + "%");
            profileSuccess.setText(TOTAL_SCORE);

            List<PieEntry> pieChartEntries = new ArrayList<>();
            pieChartEntries.add(new PieEntry(scores * 10, getResources().getString(R.string.category_details_activity_pie_entry_success)));
            pieChartEntries.add(new PieEntry((10 - scores) * 10, getResources().getString(R.string.category_details_activity_pie_entry_failure)));

            PieDataSet dataset = new PieDataSet(pieChartEntries, "");
            dataset.setColors(getResources().getColor(R.color.colorAccentBlue), getResources().getColor(R.color.colorAccentRed));
            dataset.setSliceSpace(0);
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
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
        if (progressDialog != null) progressDialog.dismiss();
    }

    private void generateProfileCategoriesList(List<Scores> scoresList) {
        CategoriesProfileAdapter adapter = new CategoriesProfileAdapter(this, scoresList);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        profileRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        profileRecyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onListItemClick(int item) {
        Intent intent = new Intent(this, CategoryDetailsActivity.class);
        String categoryName = scoresList.get(item).getCategoryName();
        intent.putExtra(SELECTED_CATEGORY, new Category(categoryName, 0));

        startActivity(intent);
    }
}