package com.zaf.triviapp.ui.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Trace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.gohn.nativedialog.ButtonClickListener;
import com.gohn.nativedialog.ButtonType;
import com.gohn.nativedialog.NDialog;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.threads.AppExecutors;
import com.zaf.triviapp.ui.CategoryDetailsActivity;
import com.zaf.triviapp.ui.GameplayActivity;
import com.zaf.triviapp.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lib.kingja.switchbutton.SwitchMultiButton;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class CategoryDetailsFragment extends Fragment {

    private static final String SELECTED_CATEGORY = "selected_category";
    private static final String DIFFICULTY = "difficulty";
    private static final String TYPE = "type";
    private MainActivity mainActivity;
    private AppDatabase mDb;
    private int scorePercentage = 0;
    public static final String[] difficultyOptions = {"Any Difficulty", "Easy", "Medium", "Hard"};
    public static final String[] typeOptions = {"Any Type", "Multiple Choice", "True/False"};
    private String difficulty = "Any Difficulty", type = "Any Type";
    @BindView(R.id.text_success) TextView textSuccess;
    @BindView(R.id.category_details_image) @Nullable ImageView selectedCategoryImage;
    @BindView(R.id.selected_category_name) TextView categoryName;
    @BindView(R.id.play_button) LinearLayout play;
    @BindView(R.id.swipe_refresh_layout_details) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.piechart) PieChart mChart;
    @BindView(R.id.text_percent) TextView textPercent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_category_details, container, false);
        ButterKnife.bind(this, view);

        mainActivity = ((MainActivity)getActivity());
        mDb = AppDatabase.getInstance(mainActivity);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Category selectedCategory = bundle.getParcelable(SELECTED_CATEGORY);
            populateUi(selectedCategory);
        }

        return view;
    }

    private void populateUi(final Category selectedCategory) {
        mainActivity.setBackButtonVisibility(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            backgroundPictureOptions(selectedCategory);
        }

        categoryName.setText(selectedCategory.getName());

        checkIfUserIsLogged();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOptionsDialog(selectedCategory);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkIfUserIsLogged();
            }
        });
    }

    private void backgroundPictureOptions(Category selectedCategory) {
        int imageId = getResources().getIdentifier("t"+selectedCategory.getId(), "drawable", mainActivity.getPackageName());
        if (imageId != 0) {
            if (selectedCategoryImage != null) {
                selectedCategoryImage.setImageResource(imageId);
            }
        } else {
            if (selectedCategoryImage != null) {
                selectedCategoryImage.setImageResource(getResources().getIdentifier("t9", "drawable", mainActivity.getPackageName()));
            }
        }
    }

    private void checkIfUserIsLogged() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mDb.taskDao().checkIfUsersTableIsEmpty().size() == 0){
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    mChart.setNoDataText(getResources().getString(R.string.no_chart));
                    mChart.getPaint(Chart.PAINT_INFO).setColor(getResources().getColor(R.color.colorAccentRed));

                    textPercent.setText(getResources().getString(R.string.category_details_login_text));

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            textPercent.setBackground(getResources().getDrawable(R.drawable.custom_border_blue));
                            textPercent.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mainActivity.finish();
                                    startActivity(new Intent(mainActivity, LoginAuth.class));
                                }
                            });
                        }
                    });

                }else {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            loadSuccessPercentage();
                        }
                    });
                }
            }
        }).start();

    }

    private void gameOptionsDialog(final Category selectedCategory){
        View dialogLayout = View.inflate(mainActivity, R.layout.gameplay_options_dialog, null);

        NDialog nDialog = new NDialog(mainActivity, ButtonType.TWO_BUTTON);
        nDialog.setIcon(R.drawable.triviapp_icon);
        nDialog.setTitle(getResources().getString(R.string.category_details_activity_game_options_dialog_title));
        nDialog.setCustomView(dialogLayout);

        SwitchMultiButton mSwitchMultiButtonDifficulty = dialogLayout.findViewById(R.id.switch_difficulty);
        mSwitchMultiButtonDifficulty.setText(difficultyOptions).setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                difficulty = tabText;
            }
        });

        SwitchMultiButton mSwitchMultiButtonType = dialogLayout.findViewById(R.id.switch_type);
        mSwitchMultiButtonType.setText(typeOptions).setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                type = tabText;
            }
        });

        ButtonClickListener buttonClickListener = new ButtonClickListener() {
            @Override
            public void onClick(int button) {
                switch (button) {
                    case NDialog.BUTTON_POSITIVE:
                        GameplayFragment gameplayFragment = new GameplayFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(SELECTED_CATEGORY, selectedCategory);
                        bundle.putString(DIFFICULTY, difficulty);
                        bundle.putString(TYPE, type);
                        gameplayFragment.setArguments(bundle);

                        FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, gameplayFragment);
                        fragmentTransaction.commit();

                        break;
                    case NDialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        nDialog.setPositiveButtonText(getResources().getString(R.string.category_details_activity_game_options_dialog_positive_button));
        nDialog.setPositiveButtonTextColor(Color.BLUE);
        nDialog.setPositiveButtonClickListener(buttonClickListener);

        nDialog.setNegativeButtonText(getResources().getString(R.string.category_details_activity_game_options_dialog_negative_button));
        nDialog.setNegativeButtonTextColor(Color.RED);
        nDialog.setNegativeButtonClickListener(buttonClickListener);

        nDialog.show();
    }

    private void loadSuccessPercentage(){
        mDb.taskDao().loadSelectedCategoryScore(categoryName.getText().toString()).observe(mainActivity, new Observer<Scores>() {
            @Override
            public void onChanged(@Nullable Scores scores) {
                final List<PieEntry> pieChartEntries = new ArrayList<>();

                if (scores != null){
                    scorePercentage = scores.getCategoryScore() * 10;
                    float successScore = (float) scorePercentage;
                    float failScore = (float) 100 - scorePercentage;
                    pieChartEntries.add(new PieEntry(successScore, getResources().getString(R.string.category_details_activity_pie_entry_correct)));
                    pieChartEntries.add(new PieEntry(failScore, getResources().getString(R.string.category_details_activity_pie_entry_wrong)));
                    textPercent.setText(scorePercentage + " %");
                    textSuccess.setText(getResources().getString(R.string.category_details_activity_pie_entry_success));
                }else{
                    pieChartEntries.add(new PieEntry(0, getResources().getString(R.string.category_details_activity_pie_entry_correct)));
                    pieChartEntries.add(new PieEntry(0, getResources().getString(R.string.category_details_activity_pie_entry_wrong)));
                    textPercent.setText(getResources().getString(R.string.category_details_activity_pie_entry_havent_played_yet_text) + categoryName.getText().toString());
                    textSuccess.setText("");
                }

                PieDataSet dataset = new PieDataSet(pieChartEntries, "");
                if(mainActivity.getSharedPref().loadNightModeState()) {
                    dataset.setColors(getResources().getColor(R.color.colorAccentBlueDark), getResources().getColor(R.color.colorAccentRedDark));
                } else {
                    dataset.setColors(getResources().getColor(R.color.colorAccentBlue), getResources().getColor(R.color.colorAccentRed));
                }
                dataset.setSliceSpace(0);
                dataset.setValueTextSize(20);
                dataset.setValueTextColor(android.R.color.white);

                PieData data = new PieData(dataset);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(20);

                mChart.setDrawHoleEnabled(false);
                mChart.setData(data);
                mChart.setDrawSliceText(false);
                mChart.getDescription().setEnabled(false);
                mChart.getLegend().setEnabled(false);

                mChart.invalidate();

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

}
