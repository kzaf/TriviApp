package com.zaf.triviapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.Category;

import java.util.ArrayList;
import java.util.List;

import lib.kingja.switchbutton.SwitchMultiButton;

public class CategoryDetailsActivity extends AppCompatActivity {

    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String DIFFICULTY = "difficulty";
    public static final String TYPE = "type";
    SwipeRefreshLayout mSwipeRefreshLayout;
    String difficulty = "Any Difficulty", type = "Any Type";
    Toolbar toolbar;
    TextView categoryName, toolbarTitle;
    ImageView selectedCategoryImage, back;
    PieChart mChart;
    LinearLayout play;
    NDialog nDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        final Category selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);

        categoryName = findViewById(R.id.selected_category_name);
        mChart = findViewById(R.id.piechart);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        play = findViewById(R.id.play_button);
        back = findViewById(R.id.back_button);

        toolbarOptions();
        chartOptions();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            selectedCategoryImage = findViewById(R.id.category_details_image);
            backgroundPictureOptions(selectedCategory);
        }

        categoryName.setText(selectedCategory.getName());

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet(selectedCategory);
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_details);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chartOptions();
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
        selectedCategoryImage.setImageResource(getResources().getIdentifier("t"+selectedCategory.getId(), "drawable", getPackageName()));
    }

    private void chartOptions() {
//        TODO: Fix refresh
//        if (mSwipeRefreshLayout.isRefreshing()) {
//            mSwipeRefreshLayout.setRefreshing(false);
//        }

        PieChart mChart = findViewById(R.id.piechart);

        mChart.setNoDataText(getResources().getString(R.string.no_chart));
        Paint paint =  mChart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getResources().getColor(R.color.colorAccentRed));

        List<PieEntry> pieChartEntries = new ArrayList<>();
        pieChartEntries.add(new PieEntry(24.0f, "Correct"));
        pieChartEntries.add(new PieEntry(30.8f, "Wrong"));

        PieDataSet dataset = new PieDataSet(pieChartEntries, "");
        dataset.setColors(getResources().getColor(R.color.colorAccentBlue), getResources().getColor(R.color.colorAccentRed));
        dataset.setSliceSpace(0);

        Description description = new Description();
        description.setText("This is Pie Chart");

        mChart.setDescription(description);

        mChart.setDrawHoleEnabled(false);
        mChart.setUsePercentValues(true);

        PieData data = new PieData(dataset);
        data.setValueFormatter(new PercentFormatter());

        mChart.setData(data);

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
                if(menuItem.getItemId()==R.id.category_details_profile)
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                else if(menuItem.getItemId()== R.id.category_details_settings)
                    Toast.makeText(CategoryDetailsActivity.this, "Categories", Toast.LENGTH_SHORT).show();
                else{
                    if (FirebaseAuth.getInstance().getCurrentUser() == null){
                        Intent intent = new Intent(CategoryDetailsActivity.this, LoginAuth.class);
                        startActivity(intent);
                    }else{
                        AuthUI.getInstance()
                                .signOut(CategoryDetailsActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //showSignInOptions();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CategoryDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                return false;
            }
        });
    }
}
