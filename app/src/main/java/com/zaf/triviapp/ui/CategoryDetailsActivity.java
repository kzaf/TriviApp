package com.zaf.triviapp.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.gohn.nativedialog.ButtonClickListener;
import com.gohn.nativedialog.ButtonType;
import com.gohn.nativedialog.NDialog;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.threads.AppExecutors;
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
    public static final String[] difficultyOptions = {"Any Difficulty", "Easy", "Medium", "Hard"};
    public static final String[] typeOptions = {"Any Type", "Multiple Choice", "True/False"};
    private String difficulty = "Any Difficulty", type = "Any Type";
    private AppDatabase mDb;
    private int scorePercentage = 0;
    private SharedPref sharedPref;
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
        if(sharedPref.loadNightModeState()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        ButterKnife.bind(this);

        final Category selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);

        mDb = AppDatabase.getInstance(getApplicationContext());

        toolbarOptions();

        populateUi(selectedCategory);
    }

    private void populateUi(final Category selectedCategory) {
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

    private void loadSuccessPercentage(){
        mDb.taskDao().loadSelectedCategoryScore(categoryName.getText().toString()).observe(CategoryDetailsActivity.this, new Observer<Scores>() {
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
                if(sharedPref.loadNightModeState()) {
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

    private void gameOptionsDialog(final Category selectedCategory){
        View dialogLayout = View.inflate(this, R.layout.gameplay_options_dialog, null);

        NDialog nDialog = new NDialog(CategoryDetailsActivity.this, ButtonType.TWO_BUTTON);
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
        nDialog.setPositiveButtonText(getResources().getString(R.string.category_details_activity_game_options_dialog_positive_button));
        nDialog.setPositiveButtonTextColor(Color.BLUE);
        nDialog.setPositiveButtonClickListener(buttonClickListener);

        nDialog.setNegativeButtonText(getResources().getString(R.string.category_details_activity_game_options_dialog_negative_button));
        nDialog.setNegativeButtonTextColor(Color.RED);
        nDialog.setNegativeButtonClickListener(buttonClickListener);

        nDialog.show();
    }

    private void backgroundPictureOptions(Category selectedCategory) {
        int imageId = getResources().getIdentifier("t"+selectedCategory.getId(), "drawable", getPackageName());
        if (imageId != 0) {
            selectedCategoryImage.setImageResource(imageId);
        } else {
            selectedCategoryImage.setImageResource(getResources().getIdentifier("t9", "drawable", getPackageName()));
        }
    }

    private void checkIfUserIsLogged() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mDb.taskDao().checkIfUsersTableIsEmpty().size() == 0){
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    mChart.setNoDataText(getResources().getString(R.string.no_chart));
                    Paint paint =  mChart.getPaint(Chart.PAINT_INFO);
                    paint.setColor(getResources().getColor(R.color.colorAccentRed));

                    textPercent.setText(getResources().getString(R.string.category_details_login_text));
                    textPercent.setBackground(getResources().getDrawable(R.drawable.custom_border_blue));
                    textPercent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                            Intent intent = new Intent(CategoryDetailsActivity.this, LoginAuth.class);
                            startActivity(intent);
                        }
                    });
                }else {
                    loadSuccessPercentage();
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(sharedPref.loadNightModeState()) {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label_dark)));
        } else {
            toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
        }

        toolbar.inflateMenu(R.menu.category_details_menu_items);
        if(toolbar.getMenu() instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) toolbar.getMenu();
            m.setOptionalIconsVisible(true);
        }
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.category_details_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else if(menuItem.getItemId()== R.id.category_details_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                } else {
                    checkIfUserIsLogged();
                }
                return false;
            }
        });
    }
}
