package com.zaf.triviapp.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.login.LoginAuth;
import com.zaf.triviapp.models.Category;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.back_button) ImageView back;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        toolbarOptions();
        chartOptions();
    }

    private void toolbarOptions() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));

        toolbar.inflateMenu(R.menu.profile_menu_items);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.profile_settings)
                    Toast.makeText(ProfileActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                else{
                    if (FirebaseAuth.getInstance().getCurrentUser() == null){
                        Intent intent = new Intent(ProfileActivity.this, LoginAuth.class);
                        startActivity(intent);
                    }else{
                        AuthUI.getInstance()
                                .signOut(ProfileActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //showSignInOptions();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                return false;
            }
        });
    }

    private void chartOptions() {

        PieChart mChart = findViewById(R.id.piechart_sum);

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

    private void generateCategoriesList(List<Category> categoriesList) {
        //TODO: Fix Adapter
    }
}
