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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zaf.triviapp.R;
import com.zaf.triviapp.models.Category;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class CategoryDetailsActivity extends AppCompatActivity {

    public static final String SELECTED_CATEGORY = "selected_category";
    Toolbar toolbar;
    TextView categoryName;
    ImageView selectedCategoryImage;
    PieChart mChart;
    LinearLayout play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_details);

        final Category selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);

        toolbarOptions();
        spinnersOptions();
        chartOptions();
        backgroundPictureOptions(selectedCategory);

        categoryName = findViewById(R.id.selected_category_name);
        categoryName.setText(selectedCategory.getName());

        play = findViewById(R.id.play_button);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryDetailsActivity.this, GameplayActivity.class);
                intent.putExtra(SELECTED_CATEGORY, selectedCategory);

                startActivity(intent);
            }
        });

    }

    private void backgroundPictureOptions(Category selectedCategory) {
        selectedCategoryImage = findViewById(R.id.category_details_image);
        selectedCategoryImage.setImageResource(getResources().getIdentifier("t"+selectedCategory.getId(), "drawable", getPackageName()));
    }

    private void chartOptions() {
        mChart = findViewById(R.id.piechart);
        mChart.setNoDataText(getResources().getString(R.string.no_chart));
        Paint paint =  mChart.getPaint(Chart.PAINT_INFO);

        paint.setColor(getResources().getColor(R.color.colorAccentRed));
        mChart.invalidate();
    }

    private void spinnersOptions() {
        NiceSpinner difficulty = findViewById(R.id.nice_spinner_difficulty);
        difficulty.attachDataSource(new LinkedList<>(Arrays.asList("Easy", "Medium", "Hard")));

        NiceSpinner type = findViewById(R.id.nice_spinner_type);
        type.attachDataSource(new LinkedList<>(Arrays.asList("Any Type", "Multiple Choice", "True/False")));
    }


    private void toolbarOptions() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.category_details_menu_items);

        ImageView back = findViewById(R.id.back_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.categories_menu_scores)
                    Toast.makeText(CategoryDetailsActivity.this, "Scores", Toast.LENGTH_SHORT).show();
                else if(menuItem.getItemId()== R.id.categories_menu_settings)
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
