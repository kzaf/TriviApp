package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.models.Question;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameplayActivity extends AppCompatActivity {

    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String DIFFICULTY = "difficulty";
    public static final String TYPE = "type";
    Toolbar toolbar;
    ProgressDialog progressDialog;
    ImageView back, cancel;
    TextView toolbarTitle, gameplayCategoryName, gameplayDifficultyLevel;
    LinearLayout secondTwoButtons;
    Category selectedCategory;
    String difficulty, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        toolbar = findViewById(R.id.toolbar);
        back = findViewById(R.id.back_button);
        cancel = findViewById(R.id.cancel_button);
        toolbarTitle = findViewById(R.id.toolbar_title);
        gameplayCategoryName = findViewById(R.id.gameplay_selected_category_name);
        gameplayDifficultyLevel = findViewById(R.id.gameplay_difficulty_level);
        secondTwoButtons = findViewById(R.id.second_two_buttons);

        selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);
        difficulty = getIntent().getStringExtra(DIFFICULTY);
        type = getIntent().getStringExtra(TYPE);


        gameplayCategoryName.setText(selectedCategory.getName());
        gameplayDifficultyLevel.setText(difficulty);
        switch (difficulty){
            case "Medium":
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.orange));
                break;
            case "Hard":
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.colorAccentRed));
                break;
            default:
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.green));
        }

        initializeDialog();
        toolbarOptions();
        fetchQuestions();

        if(type.equals("True/False")){
            secondTwoButtons.setVisibility(View.INVISIBLE);
        }
    }

    private void toolbarOptions() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        toolbarTitle.setText(Html.fromHtml(getResources().getString(R.string.triviapp_label)));
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(GameplayActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading_categories));
        progressDialog.show();
    }

    private void fetchQuestions() {

        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get instance of Retrofit
        Call<List<Question>> call = service.getQuestions(selectedCategory.getId(), difficulty, type); // Get questions request

        call.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                progressDialog.dismiss();
//                generateCategoriesList(response.body().getCategory());
//                if (response.body() != null) {
//                    categoriesList = (ArrayList<Category>) response.body().getCategory();
//                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void alertDialog(){
        new FancyGifDialog.Builder(this)
                .setTitle("Are you sure you want to give up?")
                .setMessage("Score will not be saved if you leave the quiz!")
                .setNegativeBtnText("Back to Quiz")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("Leave!")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.cancel)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        finish();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(getApplicationContext(),
                                "Nice! Keep going!",
                                getResources().getDrawable(R.drawable.ic_thumb_up_blue_24dp),
                                getResources().getColor(R.color.colorAccentBlue),
                                getResources().getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }
}
