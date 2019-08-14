package com.zaf.triviapp.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.preferences.SharedPref;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.models.Question;
import com.zaf.triviapp.models.QuestionList;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameplayActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SELECTED_CATEGORY = "selected_category";
    public static final String DIFFICULTY = "difficulty";
    public static final String TYPE = "type";
    private int questionIndex = 0;
    private int scoreCorrectAnswers = 0;
    Category selectedCategory;
    String difficulty, type;
    ArrayList<Question> questionList;
    SharedPref sharedPref;
    @BindView(R.id.toolbar) Toolbar toolbar;
    ProgressDialog progressDialog;
    @BindView(R.id.back_button) ImageView back;
    @BindView(R.id.cancel_button) ImageView cancel;
    @BindView(R.id.second_two_buttons) LinearLayout secondTwoButtons;
    @BindView(R.id.play_button) LinearLayout button1;
    @BindView(R.id.play_button2) LinearLayout button2;
    @BindView(R.id.play_button3) LinearLayout button3;
    @BindView(R.id.play_button4) LinearLayout button4;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    @BindView(R.id.gameplay_selected_category_name) TextView gameplayCategoryName;
    @BindView(R.id.gameplay_difficulty_level) TextView gameplayDifficultyLevel;
    @BindView(R.id.gameplay_step_number) TextView gameplayStepNumber;
    @BindView(R.id.question_text) TextView question;
    @BindView(R.id.answer1) TextView answer1;
    @BindView(R.id.answer2) TextView answer2;
    @BindView(R.id.answer3) TextView answer3;
    @BindView(R.id.answer4) TextView answer4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) setTheme(R.style.AppThemeDark);
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        ButterKnife.bind(this);

        selectedCategory = getIntent().getParcelableExtra(SELECTED_CATEGORY);
        difficulty = getIntent().getStringExtra(DIFFICULTY);
        type = getIntent().getStringExtra(TYPE);

        gameplayCategoryName.setText(selectedCategory.getName());

        initializeDialog();
        toolbarOptions();
        fetchQuestions();
    }

    @Override
    public void onBackPressed() {
        alertDialogExit();
    }

    private void toolbarOptions() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogExit();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogExit();
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
        else difficulty = difficulty.toLowerCase();

        Call<QuestionList> call = service.getQuestions(selectedCategory.getId(), difficulty, type);// Get questions request

        call.enqueue(new Callback<QuestionList>() {
            @Override
            public void onResponse(Call<QuestionList> call, Response<QuestionList> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    questionList = (ArrayList<Question>) response.body().getTrivia_questions();
                    populateQuestions(questionList);
                }
            }
            @Override
            public void onFailure(Call<QuestionList> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void populateQuestions(ArrayList<Question> questionList){

        if (questionList.size() == 0){
            errorDialog();
            return;
        }

        button1.setBackground(getResources().getDrawable(R.drawable.custom_border));
        button2.setBackground(getResources().getDrawable(R.drawable.custom_border));
        button3.setBackground(getResources().getDrawable(R.drawable.custom_border));
        button4.setBackground(getResources().getDrawable(R.drawable.custom_border));

        switch (questionList.get(questionIndex).getDifficulty()){
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

        List<String> mixedQuestions = questionList.get(questionIndex).getIncorrect_answers();
        mixedQuestions.add(questionList.get(questionIndex).getCorrect_answer());
        Collections.shuffle(mixedQuestions);

        // Question
        question.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(questionList.get(questionIndex).getQuestion(), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(questionList.get(questionIndex).getQuestion()));

        // Answers
        if(questionList.get(questionIndex).getType().equals("boolean")){
            secondTwoButtons.setVisibility(View.INVISIBLE);
            answer1.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(0), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(0)));
            answer1.setOnClickListener(this);
            answer2.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(1), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(1)));
            answer2.setOnClickListener(this);
        }else{
            secondTwoButtons.setVisibility(View.VISIBLE);
            answer1.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(0), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(0)));
            answer1.setOnClickListener(this);
            answer2.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(1), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(1)));
            answer2.setOnClickListener(this);
            answer3.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(2), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(2)));
            answer3.setOnClickListener(this);
            answer4.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(mixedQuestions.get(3), Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(mixedQuestions.get(3)));
            answer4.setOnClickListener(this);
        }

        // Step counter
        int step = questionIndex + 1;
        gameplayStepNumber.setText(step + "/10");

        questionIndex++;
    }

    private void alertDialogEndGame(int score){
        String message;
        int gifImage;
        if(score==10){
            message = "Wow! You rock!";
            gifImage = R.drawable.wow;
        }
        else if(score<10 && score>=5){
            message = "Well done!";
            gifImage = R.drawable.welldone;
        }
        else if(score<5 && score>=2){
            message = "You can do better";
            gifImage = R.drawable.meh;
        }
        else{
            message = "Hmm.. Again?";
            gifImage = R.drawable.fail;
        }
        new FancyGifDialog.Builder(this)
                .setTitle(message)
                .setMessage("Your score is " + score + "/10")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("OK")
                .setGifResource(gifImage)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        finish();
                    }
                })
                .build();
    }

    private void errorDialog(){
        new FancyGifDialog.Builder(this)
                .setTitle("Ooops.. something went wrong!")
                .setMessage("Please refresh!")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("Back to Quiz!")
                .setGifResource(R.drawable.error)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        finish();
                    }
                })
                .build();
    }

    private void alertDialogExit(){
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
                checkAnswerCorrection(answer1, button1);
                break;

            case R.id.answer2:
                checkAnswerCorrection(answer2, button2);
                break;

            case R.id.answer3:
                checkAnswerCorrection(answer3, button3);
                break;

            case R.id.answer4:
                checkAnswerCorrection(answer4, button4);
                break;

            default:
                break;
        }
    }

    private void checkAnswerCorrection(TextView answer, LinearLayout button) {

        button1.setBackgroundColor(getResources().getColor(R.color.colorAccentRed));
        button2.setBackgroundColor(getResources().getColor(R.color.colorAccentRed));
        button3.setBackgroundColor(getResources().getColor(R.color.colorAccentRed));
        button4.setBackgroundColor(getResources().getColor(R.color.colorAccentRed));

        String answerText = answer.getText().toString();
        String answerCorrect = questionList.get(questionIndex-1).getCorrect_answer();

        if(answerText.equals(answerCorrect)){
            button.setBackgroundColor(getResources().getColor(R.color.green));
            particlesEffect(button);
            scoreCorrectAnswers++;
        }else{
            ArrayList<TextView> questions = new ArrayList<>();
            questions.add(answer1);
            questions.add(answer2);
            questions.add(answer3);
            questions.add(answer4);
            for (int j=0; j<questions.size(); j++){
                if(questions.get(j).getText().toString().equals(questionList.get(questionIndex-1).getCorrect_answer())){
                    manageBlinkEffect((LinearLayout) questions.get(j).getParent());
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(questionIndex == 10){
                    alertDialogEndGame(scoreCorrectAnswers);
                    return;
                }
                populateQuestions(questionList);
            }
        }, 2000);
    }

    private void particlesEffect(LinearLayout button){
        new ParticleSystem(this, 10, getResources().getDrawable(R.drawable.ic_star_yellow_24dp), 1000)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(button, 10);
    }

    private void manageBlinkEffect(LinearLayout parent) {
        ObjectAnimator anim = ObjectAnimator.ofInt(parent,
                "backgroundColor",
                getResources().getColor(R.color.green),
                Color.WHITE,
                getResources().getColor(R.color.green));
        anim.setDuration(500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(2);
        anim.start();
    }
}