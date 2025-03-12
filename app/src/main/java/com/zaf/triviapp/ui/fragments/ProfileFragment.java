package com.zaf.triviapp.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.Chart;
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
import com.zaf.triviapp.databinding.ActivityProfileBinding;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.ui.MainActivity;
import com.zaf.triviapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment implements CategoriesProfileAdapter.CategoriesProfileAdapterListItemClickListener {

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
    private RecyclerView recyclerView;
    private ActivityProfileBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        binding = ActivityProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mainActivity = ((MainActivity)getActivity());
        mDb = AppDatabase.getInstance(mainActivity);
        taskDao = mDb.taskDao();

        if(savedInstanceState != null){
            // The RecyclerView keeps going back to initial state because the data in Adapter still being populated when we call the onRestoreInstanceState
            // It's a hack to delay the onRestoreInstanceState
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
//                    binding.profileRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(SCORES_LAYOUT_MANAGER));
                    recyclerView = (RecyclerView) binding.profileRecyclerView;

                    LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom);
                    recyclerView.setLayoutAnimation(controller);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                    recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.scheduleLayoutAnimation();
                }
            }, 300);
            scoresList = savedInstanceState.getParcelableArrayList(SCORES_LIST);
            generateProfileCategoriesList(scoresList);
            this.hasInternet = savedInstanceState.getBoolean(HAS_INTERNET);

        }else{
            Utils utils = new Utils(getActivity());
            this.hasInternet = utils.hasActiveInternetConnection();

            binding.swipeRefreshLayoutProfile.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
//        outState.putParcelable(SCORES_LAYOUT_MANAGER, binding.profileRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putBoolean(HAS_INTERNET, this.hasInternet);
        super.onSaveInstanceState(outState);
    }

    private void setupUi(final TaskDao taskDao){
        mainActivity.setBackButtonVisibility(true);
        mainActivity.toolbarOptions(this);
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mDb.taskDao().insertScore(score);

                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DynamicToast.make(mainActivity, "" + databaseError.getMessage(), mainActivity.getResources()
                        .getColor(R.color.colorAccentRed), mainActivity.getResources()
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
        progressDialog.setMessage(mainActivity.getResources().getString(R.string.loading_profile));
        progressDialog.show();
    }

    private void userLoggedPopulateUi(UserDetails userDetails) {
        if(hasInternet){
            readScores(userDetails.getUserId());
        }

        binding.profileUsernameTv.setText(userDetails.getUserName());
        binding.profileEmailTv.setText(userDetails.getUserEmail());

        binding.loginUser.setText(mainActivity.getResources().getString(R.string.profile_logout_button));
        binding.loginUser.setBackgroundResource(R.drawable.custom_border_red);

        binding.loginUser.setOnClickListener(new View.OnClickListener() {
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
        binding.profileUsernameTv.setText(mainActivity.getResources().getString(R.string.profile_activity_not_logged_label));
        binding.profileEmailTv.setText("");

        binding.loginUser.setText(mainActivity.getResources().getString(R.string.profile_activity_login_label));
        binding.loginUser.setBackgroundResource(R.drawable.custom_border_blue);

        binding.loginUser.setOnClickListener(new View.OnClickListener() {
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
                .setNegativeBtnText(mainActivity.getResources().getString(R.string.profile_activity_dialog_logout_negatibe_btn_text))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.profile_activity_dialog_logout_positive_btn_text))
                .setNegativeBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
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
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();

                            }
                        }).start();
                        DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.gameplay_error_dialog_toast_positive), mainActivity.getResources()
                                .getColor(R.color.colorAccentBlue), mainActivity.getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.gameplay_error_dialog_toast_negative), mainActivity.getResources()
                                .getColor(R.color.colorAccentBlue), mainActivity.getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

    private void chartOptions(boolean isUserLogged, float scores) {
        if (!isUserLogged){
            Paint paint =  binding.piechartSum.getPaint(Chart.PAINT_INFO);
            paint.setColor(mainActivity.getResources().getColor(R.color.colorAccentRed));
            binding.profilePercent.setText("");
            binding.profileSuccess.setText("");
            binding.piechartSum.setNoDataText(mainActivity.getResources().getString(R.string.no_chart));
        }else{
            binding.profilePercent.setText(String.format("%s%%", String.format("%.2f", scores * 10)));
            binding.profileSuccess.setText(TOTAL_SCORE);

            List<PieEntry> pieChartEntries = new ArrayList<>();
            pieChartEntries.add(new PieEntry(scores * 10, mainActivity.getResources().getString(R.string.category_details_activity_pie_entry_success)));
            pieChartEntries.add(new PieEntry((10 - scores) * 10, mainActivity.getResources().getString(R.string.category_details_activity_pie_entry_failure)));

            PieDataSet dataset = new PieDataSet(pieChartEntries, "");
            if(mainActivity.getSharedPref().loadNightModeState()) {
                dataset.setColors(mainActivity.getResources().getColor(R.color.colorAccentBlueDark), mainActivity.getResources().getColor(R.color.colorAccentRedDark));
            } else {
                dataset.setColors(mainActivity.getResources().getColor(R.color.colorAccentBlue), mainActivity.getResources().getColor(R.color.colorAccentRed));
            }            dataset.setSliceSpace(0);
            dataset.setValueTextSize(20);
            dataset.setValueTextColor(android.R.color.white);

            PieData data = new PieData(dataset);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(20);

            binding.piechartSum.setDrawHoleEnabled(false);
            binding.piechartSum.setDrawSliceText(false);
            binding.piechartSum.getDescription().setEnabled(false);
            binding.piechartSum.getLegend().setEnabled(false);

            binding.piechartSum.setData(data);
            binding.piechartSum.invalidate();
        }
        if (binding.swipeRefreshLayoutProfile.isRefreshing()) {
            binding.swipeRefreshLayoutProfile.setRefreshing(false);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void generateProfileCategoriesList(List<Scores> scoresList) {
//        CategoriesProfileAdapter adapter = new CategoriesProfileAdapter(this, scoresList);
//        binding.profileRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
//        binding.profileRecyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        binding.profileRecyclerView.scheduleLayoutAnimation();

        // TODO
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
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }
}
