package com.zaf.triviapp.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.AppExecutors;
import com.zaf.triviapp.R;
import com.zaf.triviapp.adapters.CategoriesProfileAdapter;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.UserDetails;
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
    @BindView(R.id.profile_username_tv) TextView userName;
    @BindView(R.id.profile_email_tv) TextView userEmail;
    @BindView(R.id.login_user) TextView loginUser;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.profile_recycler_view) RecyclerView profileRecyclerView;

    private SharedPref sharedPref;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        mDb = AppDatabase.getInstance(getApplicationContext());

        toolbarOptions();
        chartOptions();
        setupUi(mDb.taskDao().loadUserDetails());
    }

    private void setupUi(final LiveData<UserDetails> loggedUser){
        loggedUser.observe(this, new Observer<UserDetails>() {
            @Override
            public void onChanged(@Nullable UserDetails userDetails) {
                if(userDetails == null){
                    userName.setText("Login to continue...");
                    userEmail.setText("");

                    loginUser.setText("Login");
                    loginUser.setBackgroundResource(R.drawable.custom_border_blue);

                    loginUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ProfileActivity.this, LoginAuth.class);
                            startActivity(intent);
                        }
                    });
                }else{
                    userName.setText(userDetails.getUserName());
                    userEmail.setText(userDetails.getUserEmail());

                    loginUser.setText("Logout");
                    loginUser.setBackgroundResource(R.drawable.custom_border_red);

                    loginUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialogLogout();
                        }
                    });
                }
            }
        });
    }

    private void alertDialogLogout(){
        new FancyGifDialog.Builder(this)
                .setTitle("Are you sure you want to logout?")
                .setNegativeBtnText("Nop! Keep me signed!")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("Yes I am sure!")
                .setNegativeBtnBackground("#FFA9A7A8")
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
                            }
                        });
                        DynamicToast.make(getApplicationContext(), "See you again!", getResources()
                                .getColor(R.color.colorAccentBlue), getResources()
                                .getColor(R.color.textWhite))
                                .show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(getApplicationContext(), "Cool!", getResources()
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
//        CategoriesProfileAdapter adapter = new CategoriesProfileAdapter(this, categoriesList);
//        profileRecyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
//        profileRecyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        profileRecyclerView.scheduleLayoutAnimation();
    }
}