package com.zaf.triviapp.ui;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.models.CategoriesList;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.models.Question;
import com.zaf.triviapp.models.QuestionList;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameplayActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String DIFFICULTY = "difficulty";
    public static final String TYPE = "type";
    private int questionIndex = 0;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    ImageView back, cancel;
    LinearLayout secondTwoButtons, button1, button2, button3, button4;
    Category selectedCategory;
    String difficulty, type;
    ArrayList<Question> questionList;
    TextView toolbarTitle, gameplayCategoryName, gameplayDifficultyLevel, gameplayStepNumber,
            question, answer1, answer2, answer3, answer4;

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
        gameplayStepNumber = findViewById(R.id.gameplay_step_number);
        secondTwoButtons = findViewById(R.id.second_two_buttons);
        question = findViewById(R.id.question_text);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);

        button1 = findViewById(R.id.play_button);
        button2 = findViewById(R.id.play_button2);
        button3 = findViewById(R.id.play_button3);
        button4 = findViewById(R.id.play_button4);

        selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);
        difficulty = getIntent().getStringExtra(DIFFICULTY);
        type = getIntent().getStringExtra(TYPE);

        gameplayCategoryName.setText(selectedCategory.getName());

        initializeDialog();
        toolbarOptions();
        fetchQuestions();
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

        if(type.equals("True/False")) type = "boolean";
        else if(type.equals("Multiple Choice")) type = "multiple";
        else type = null;

        if(difficulty.equals("Any Difficulty")) difficulty = null;
        else difficulty.toLowerCase();

        Call<QuestionList> call = service.getQuestions(selectedCategory.getId(), difficulty, type);// Get questions request

        call.enqueue(new Callback<QuestionList>() {
            @Override
            public void onResponse(Call<QuestionList> call, Response<QuestionList> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    questionList = (ArrayList<Question>) response.body().getTrivia_questions();
                    populateQuestions(questionList, questionIndex);
                }
            }
            @Override
            public void onFailure(Call<QuestionList> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void populateQuestions(ArrayList<Question> questionList, int i){

        if(i == 10){
            Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();
            return;
        }

        answer1.setTextColor(getResources().getColor(R.color.textBlack));
        answer2.setTextColor(getResources().getColor(R.color.textBlack));
        answer3.setTextColor(getResources().getColor(R.color.textBlack));
        answer4.setTextColor(getResources().getColor(R.color.textBlack));

        switch (questionList.get(i).getDifficulty()){
            case "medium":
                gameplayDifficultyLevel.setText("Medium");
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.orange));
                break;
            case "hard":
                gameplayDifficultyLevel.setText("Hard");
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.colorAccentRed));
                break;
            default:
                gameplayDifficultyLevel.setText("Easy");
                gameplayDifficultyLevel.setTextColor(getResources().getColor(R.color.green));
        }

        List<String> mixedQuestions = questionList.get(i).getIncorrect_answers();
        mixedQuestions.add(questionList.get(i).getCorrect_answer());
        Collections.shuffle(mixedQuestions);

        // Question
        question.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(questionList.get(i).getQuestion(), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));

        // Answers
        if(questionList.get(i).getType().equals("boolean")){
            secondTwoButtons.setVisibility(View.INVISIBLE);
            answer1.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(0), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer1.setOnClickListener(this);
            answer2.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(1), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer2.setOnClickListener(this);
        }else{
            secondTwoButtons.setVisibility(View.VISIBLE);
            answer1.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(0), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer1.setOnClickListener(this);
            answer2.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(1), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer2.setOnClickListener(this);
            answer3.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(2), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer3.setOnClickListener(this);
            answer4.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(3), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(i).getQuestion()));
            answer4.setOnClickListener(this);
        }

        // Step counter
        gameplayStepNumber.setText(++i + "/10");

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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.answer1:
                checkAnswerCorrection(answer1);
                break;

            case R.id.answer2:
                checkAnswerCorrection(answer2);
                break;

            case R.id.answer3:
                checkAnswerCorrection(answer3);
                break;

            case R.id.answer4:
                checkAnswerCorrection(answer4);
                break;

            default:
                break;
        }
    }

    private void checkAnswerCorrection(TextView answer) {
        if(answer.getText().toString().equals(questionList.get(questionIndex).getCorrect_answer())){
            answer1.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer2.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer3.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer4.setTextColor(getResources().getColor(R.color.colorAccentRed));

            answer.setTextColor(getResources().getColor(R.color.green));
        }else{
            answer1.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer2.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer3.setTextColor(getResources().getColor(R.color.colorAccentRed));
            answer4.setTextColor(getResources().getColor(R.color.colorAccentRed));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                populateQuestions(questionList, questionIndex++);
            }
        }, 2000);
    }
}