package com.zaf.triviapp.ui;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.gohn.nativedialog.ButtonClickListener;
import com.gohn.nativedialog.ButtonType;
import com.gohn.nativedialog.NDialog;
import com.zaf.triviapp.AppExecutors;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.models.Category;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lib.kingja.switchbutton.SwitchMultiButton;

public class CategoryDetailsActivity extends AppCompatActivity {

    private static final String SELECTED_CATEGORY = "selected_category";
    private static final String DIFFICULTY = "difficulty";
    private static final String TYPE = "type";
    private String difficulty = "Any Difficulty", type = "Any Type";
    private NDialog nDialog;
    private SharedPref sharedPref;
    private AppDatabase mDb;
    private int scorePercentage = 0;
    @BindView(R.id.swipe_refresh_layout_details) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.selected_category_name) TextView categoryName;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.text_percent) TextView textPercent;
    @BindView(R.id.text_success) TextView textSuccess;
    @BindView(R.id.category_details_image) @Nullable ImageView selectedCategoryImage;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.piechart) PieChart mChart;
    @BindView(R.id.play_button) LinearLayout play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        ButterKnife.bind(this);

        final Category selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);

        mDb = AppDatabase.getInstance(getApplicationContext());

        toolbarOptions();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            backgroundPictureOptions(selectedCategory);
        }

        categoryName.setText(selectedCategory.getName());

        checkIfUserIsLogged();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet(selectedCategory);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkIfUserIsLogged();
            }
        });
    }

    private void loadSuccessPercentage(){
        mDb.taskDao().loadSelectedCategoryScore(categoryName.getText().toString()).observe(CategoryDetailsActivity.this, new Observer<Scores>() {
            @Override
            public void onChanged(@Nullable Scores scores) {
                final List<PieEntry> pieChartEntries = new ArrayList<>();

                if (scores != null){
                    scorePercentage = scores.getCategoryScore() * 10;
                    float successScore = (float) scorePercentage;
                    float failScore = (float) 100 - scorePercentage;
                    pieChartEntries.add(new PieEntry(successScore, "Correct"));
                    pieChartEntries.add(new PieEntry(failScore, "Wrong"));
                    textPercent.setText(scorePercentage + " %");
                }else{
                    pieChartEntries.add(new PieEntry(0, "Correct"));
                    pieChartEntries.add(new PieEntry(0, "Wrong"));
                    textPercent.setText("You haven't played yet in " + categoryName.getText().toString());
                    textSuccess.setText("");
                }

                PieDataSet dataset = new PieDataSet(pieChartEntries, "");
                dataset.setColors(getResources().getColor(R.color.colorAccentBlue), getResources().getColor(R.color.colorAccentRed));
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

                if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void bottomSheet(final Category selectedCategory){
        View dialogLayout = View.inflate(this, R.layout.gameplay_options_dialog, null);

        nDialog = new NDialog(CategoryDetailsActivity.this, ButtonType.TWO_BUTTON);
        nDialog.setIcon(R.drawable.triviapp_icon);
        nDialog.setTitle("Select gameplay options");
        nDialog.setCustomView(dialogLayout);

        SwitchMultiButton mSwitchMultiButtonDifficulty = dialogLayout.findViewById(R.id.switch_difficulty);
        mSwitchMultiButtonDifficulty.setText("Any Difficulty", "Easy", "Medium", "Hard").setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                difficulty = tabText;
            }
        });

        SwitchMultiButton mSwitchMultiButtonType = dialogLayout.findViewById(R.id.switch_type);
        mSwitchMultiButtonType.setText("Any Type", "Multiple Choice", "True/False").setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
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
                        Intent intent = new Intent(CategoryDetailsActivity.this, GameplayActivity.class);
                        intent.putExtra(SELECTED_CATEGORY, selectedCategory);
                        intent.putExtra(DIFFICULTY, difficulty);
                        intent.putExtra(TYPE, type);

                        startActivity(intent);
                        break;
                    case NDialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        nDialog.setPositiveButtonText("Play!");
        nDialog.setPositiveButtonTextColor(Color.BLUE);
        nDialog.setPositiveButtonClickListener(buttonClickListener);

        nDialog.setNegativeButtonText("Back");
        nDialog.setNegativeButtonTextColor(Color.RED);
        nDialog.setNegativeButtonClickListener(buttonClickListener);

        nDialog.show();
    }

    private void backgroundPictureOptions(Category selectedCategory) {
        int imageId = getResources().getIdentifier("t"+selectedCategory.getId(), "drawable", getPackageName());
        if (imageId != 0){
            selectedCategoryImage.setImageResource(imageId);
        }else{
            selectedCategoryImage.setImageResource(getResources().getIdentifier("t9", "drawable", getPackageName()));
        }
    }

    private void checkIfUserIsLogged() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDb.taskDao().checkIfUsersTableIsEmpty().size() == 0){
                    if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                    mChart.setNoDataText(getResources().getString(R.string.no_chart));
                    Paint paint =  mChart.getPaint(Chart.PAINT_INFO);
                    paint.setColor(getResources().getColor(R.color.colorAccentRed));
                }else {
                    loadSuccessPercentage();
                }
            }
        });
    }

    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));

        toolbar.inflateMenu(R.menu.category_details_menu_items);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.category_details_profile) startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                else if(menuItem.getItemId()== R.id.category_details_settings) startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                else checkIfUserIsLogged();
                return false;
            }
        });
    }
}
