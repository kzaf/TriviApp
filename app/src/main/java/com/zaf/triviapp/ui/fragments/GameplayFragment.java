package com.zaf.triviapp.ui.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.plattysoft.leonids.ParticleSystem;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.zaf.triviapp.R;
import com.zaf.triviapp.database.AppDatabase;
import com.zaf.triviapp.database.tables.Scores;
import com.zaf.triviapp.database.tables.UserDetails;
import com.zaf.triviapp.models.Category;
import com.zaf.triviapp.models.Question;
import com.zaf.triviapp.models.QuestionList;
import com.zaf.triviapp.network.GetDataService;
import com.zaf.triviapp.network.RetrofitClientInstance;
import com.zaf.triviapp.ui.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameplayFragment extends Fragment implements View.OnClickListener{

    private static final String SELECTED_CATEGORY = "selected_category";
    private static final String DIFFICULTY = "difficulty";
    private static final String TYPE = "type";
    private static final String QUESTION_LIST = "question_list";
    private static final String QUESTION_INDEX = "question_index";
    private static final String QUESTION = "question";
    private static final String ANSWER_1 = "answer1";
    private static final String ANSWER_2 = "answer2";
    private static final String ANSWER_3 = "answer3";
    private static final String ANSWER_4 = "answer4";
    private static final String STEP = "step";
    private static final String IS_TRUE_FALSE = "is_true_false";
    private static final String LEVEL = "level";
    private static final String SCORE_CORRECT_ANSWERS = "socre_correct_answers";
    private static final String IS_DIALOG_OPEN = "is_dialog_open";
    private MainActivity mainActivity;
    private AppDatabase mDb;
    private Vibrator vibe;
    private static boolean isDialogOpen = false;
    private int questionIndex = 0;
    private int scoreCorrectAnswers = 0;
    private Category selectedCategory;
    private String difficulty, type;
    private ArrayList<Question> questionList;
    private ProgressDialog progressDialog;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    @BindView(R.id.second_two_buttons) LinearLayout secondTwoButtons;
    @BindView(R.id.gameplay_selected_category_name) TextView gameplayCategoryName;
    @BindView(R.id.gameplay_difficulty_level) TextView gameplayDifficultyLevel;
    @BindView(R.id.gameplay_step_number) TextView gameplayStepNumber;
    @BindView(R.id.question_text) TextView question;
    @BindView(R.id.answer1) TextView answer1;
    @BindView(R.id.answer2) TextView answer2;
    @BindView(R.id.answer3) TextView answer3;
    @BindView(R.id.answer4) TextView answer4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_gameplay, container, false);
        ButterKnife.bind(this, view);

        mainActivity = ((MainActivity)getActivity());
        mDb = AppDatabase.getInstance(mainActivity);

        populateUi(savedInstanceState);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!isDialogOpen){
            if (questionList == null)
                errorDialog();
            else{
                outState.putParcelableArrayList(QUESTION_LIST, questionList);
                outState.putInt(QUESTION_INDEX, questionIndex);
                outState.putString(LEVEL, questionList.get(questionIndex-1).getDifficulty());
                outState.putString(QUESTION, question.getText().toString());
                outState.putString(ANSWER_1, answer1.getText().toString());
                outState.putString(ANSWER_2, answer2.getText().toString());
                outState.putString(ANSWER_3, answer3.getText().toString());
                outState.putString(ANSWER_4, answer4.getText().toString());
                outState.putString(STEP, gameplayStepNumber.getText().toString());
                outState.putInt(SCORE_CORRECT_ANSWERS, scoreCorrectAnswers);

                if (secondTwoButtons.getVisibility() == View.INVISIBLE)
                    outState.putBoolean(IS_TRUE_FALSE, true);
                else
                    outState.putBoolean(IS_TRUE_FALSE, false);
            }
        }else{
            outState.putInt(SCORE_CORRECT_ANSWERS, scoreCorrectAnswers);
            outState.putBoolean(IS_DIALOG_OPEN, isDialogOpen);
        }
        super.onSaveInstanceState(outState);
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

    private void populateUi(Bundle savedInstanceState) {
        mainActivity.setBackButtonVisibility(false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            selectedCategory = bundle.getParcelable(SELECTED_CATEGORY);
            difficulty = bundle.getString(DIFFICULTY);
            type = bundle.getString(TYPE);
        }

        gameplayCategoryName.setText(selectedCategory.getName());
        vibe = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);

        if (savedInstanceState != null){
            if (isDialogOpen){
                isDialogOpen = savedInstanceState.getBoolean(IS_DIALOG_OPEN);
                scoreCorrectAnswers = savedInstanceState.getInt(SCORE_CORRECT_ANSWERS);
                alertDialogEndGame(scoreCorrectAnswers);
            }else{
                populateUiOnOrientationChange(savedInstanceState);
            }
        }else{
            initializeDialog();
            fetchQuestions();
        }
    }

    private void populateUiOnOrientationChange(Bundle savedInstanceState) {
        questionList = savedInstanceState.getParcelableArrayList(QUESTION_LIST);
        questionIndex = savedInstanceState.getInt(QUESTION_INDEX);
        scoreCorrectAnswers = savedInstanceState.getInt(SCORE_CORRECT_ANSWERS);

        setLevelLabelTextAndColor(savedInstanceState.getString(LEVEL));

        question.setText(Html.fromHtml(savedInstanceState.getString(QUESTION), Html.FROM_HTML_MODE_COMPACT));
        answer1.setText(Html.fromHtml(savedInstanceState.getString(ANSWER_1), Html.FROM_HTML_MODE_COMPACT));
        answer1.setOnClickListener(this);
        answer2.setText(Html.fromHtml(savedInstanceState.getString(ANSWER_2), Html.FROM_HTML_MODE_COMPACT));
        answer2.setOnClickListener(this);

        if(savedInstanceState.getBoolean(IS_TRUE_FALSE)) {
            secondTwoButtons.setVisibility(View.INVISIBLE);
        } else{
            secondTwoButtons.setVisibility(View.VISIBLE);

            answer3.setText(Html.fromHtml(savedInstanceState.getString(ANSWER_3), Html.FROM_HTML_MODE_COMPACT));
            answer3.setOnClickListener(this);
            answer4.setText(Html.fromHtml(savedInstanceState.getString(ANSWER_4), Html.FROM_HTML_MODE_COMPACT));
            answer4.setOnClickListener(this);
        }
        gameplayStepNumber.setText(savedInstanceState.getString(STEP));
    }

    private void initializeDialog() {
        progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setMessage(mainActivity.getResources().getString(R.string.loading_categories));
        progressDialog.show();
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    private void fetchQuestions() {

        GetDataService service =
                RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class); // Get Retrofit instance

        if(type.equals("True/False")) {
            type = "boolean";
        } else if(type.equals("Multiple Choice")) {
            type = "multiple";
        } else {
            type = null;
        }

        if(difficulty.equals("Any Difficulty")) {
            difficulty = null;
        } else {
            difficulty = difficulty.toLowerCase();
        }

        Call<QuestionList> call =
                service.getQuestions(selectedCategory.getId(), difficulty, type);// Get questions request

        call.enqueue(new Callback<QuestionList>() {
            @Override
            public void onResponse(Call<QuestionList> call, Response<QuestionList> response) {

                progressDialog.dismiss();
                mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

                if (response.body() != null) {
                    questionList = (ArrayList<Question>) response.body().getTrivia_questions();
                    populateQuestions(questionList);
                }
            }

            @Override
            public void onFailure(Call<QuestionList> call, Throwable t) {
                progressDialog.dismiss();
                mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        });
    }

    private void populateQuestions(ArrayList<Question> questionList){
        if (questionList.size() == 0){
            errorDialog();
            return;
        }

        answer1.setBackground(mainActivity.getResources().getDrawable(R.drawable.custom_border));
        answer2.setBackground(mainActivity.getResources().getDrawable(R.drawable.custom_border));
        answer3.setBackground(mainActivity.getResources().getDrawable(R.drawable.custom_border));
        answer4.setBackground(mainActivity.getResources().getDrawable(R.drawable.custom_border));

        setLevelLabelTextAndColor(questionList.get(questionIndex).getDifficulty());

        List<String> mixedQuestions = questionList.get(questionIndex).getIncorrect_answers();
        mixedQuestions.add(questionList.get(questionIndex).getCorrect_answer());
        Collections.shuffle(mixedQuestions);

        // Question
        question.setText(Html.fromHtml(questionList.get(questionIndex).getQuestion(),
                Html.FROM_HTML_MODE_COMPACT));

        // Answers
        answer1.setText(Html.fromHtml(mixedQuestions.get(0), Html.FROM_HTML_MODE_COMPACT));
        answer1.setOnClickListener(this);
        answer2.setText(Html.fromHtml(mixedQuestions.get(1), Html.FROM_HTML_MODE_COMPACT));
        answer2.setOnClickListener(this);

        if(questionList.get(questionIndex).getType().equals("boolean")) {
            secondTwoButtons.setVisibility(View.INVISIBLE);
        } else{
            secondTwoButtons.setVisibility(View.VISIBLE);
            answer3.setText(Html.fromHtml(mixedQuestions.get(2), Html.FROM_HTML_MODE_COMPACT));
            answer3.setOnClickListener(this);
            answer4.setText(Html.fromHtml(mixedQuestions.get(3), Html.FROM_HTML_MODE_COMPACT));
            answer4.setOnClickListener(this);
        }

        // Step counter
        int step = questionIndex + 1;
        gameplayStepNumber.setText(step + "/10");

        questionIndex++;
    }

    private void setLevelLabelTextAndColor(String level) {
        if (level == null){
            gameplayDifficultyLevel.setText(mainActivity.getResources().getString(R.string.gameplay_difficulty_medium));
            gameplayDifficultyLevel.setTextColor(mainActivity.getResources().getColor(R.color.orange));
        }else{
            switch (level){
                case "medium":
                    gameplayDifficultyLevel.setText(mainActivity.getResources().getString(R.string.gameplay_difficulty_medium));
                    gameplayDifficultyLevel.setTextColor(mainActivity.getResources().getColor(R.color.orange));
                    break;
                case "hard":
                    gameplayDifficultyLevel.setText(mainActivity.getResources().getString(R.string.gameplay_difficulty_hard));
                    gameplayDifficultyLevel.setTextColor(mainActivity.getResources().getColor(R.color.colorAccentRed));
                    break;
                default:
                    gameplayDifficultyLevel.setText(mainActivity.getResources().getString(R.string.gameplay_difficulty_easy));
                    gameplayDifficultyLevel.setTextColor(mainActivity.getResources().getColor(R.color.green));
            }
        }
    }

    private void updateFirebase(Scores scores){
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataScores");
        addScore(scores.getUserId(), scores.getCategoryName(), scores.getCategoryScore());
    }

    private void addScore(String userId, String categoryName, int categoryScore){
        mFirebaseDatabase.child("ScoresByUser").child(userId).child(categoryName).child("Score").setValue(categoryScore);
    }

    private void errorDialog(){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.gameplay_error_dialog_title))
                .setMessage(mainActivity.getResources().getString(R.string.gameplay_error_dialog_message))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_text))
                .setGifResource(R.drawable.error)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        // TODO
                        //finish();
                    }
                })
                .build();
    }

    private void alertDialogEndGame(final int score){
        isDialogOpen = true;
        String message;
        int gifImage;

        if(score==10){
            message = mainActivity.getResources().getString(R.string.gameplay_endgame_dialog_wow);
            gifImage = R.drawable.wow;
        }
        else if(score<10 && score>=5){
            message = mainActivity.getResources().getString(R.string.gameplay_endgame_dialog_welldone);
            gifImage = R.drawable.welldone;
        }
        else if(score<5 && score>=2){
            message = mainActivity.getResources().getString(R.string.gameplay_endgame_dialog_can_do_better);
            gifImage = R.drawable.meh;
        }
        else{
            message = mainActivity.getResources().getString(R.string.gameplay_endgame_dialog_fail);
            gifImage = R.drawable.fail;
        }

        setUpFancyDialog(score, message, gifImage);
    }

    private void alertDialogExit(){
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_title))
                .setMessage(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_message))
                .setNegativeBtnText(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_negative_button))
                .setPositiveBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_positive_button_color))
                .setPositiveBtnText(mainActivity.getResources().getString(R.string.gameplay_exit_dialog_positive_button))
                .setNegativeBtnBackground(mainActivity.getResources().getString(R.string.gameplay_error_dialog_negative_button_color))
                .setGifResource(R.drawable.cancel)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        // TODO
                        //finish();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        DynamicToast.make(mainActivity,
                                mainActivity.getResources().getString(R.string.gameplay_exit_dialog_keep_going_toast),
                                mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_blue_24dp),
                                mainActivity.getResources().getColor(R.color.colorAccentBlue),
                                mainActivity.getResources().getColor(R.color.textWhite))
                                .show();
                    }
                })
                .build();
    }

    private void setUpFancyDialog(final int score, String message, int gifImage) {
        new FancyGifDialog.Builder(mainActivity)
                .setTitle(message)
                .setMessage("Your score is " + score + "/10")
                .setPositiveBtnBackground("#b80c00")
                .setPositiveBtnText("OK")
                .setGifResource(gifImage)
                .isCancellable(false)
                .OnPositiveClicked(new FancyGifDialogListener() {

                    @Override
                    public void OnClick() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final UserDetails userDetails = mDb.taskDao().loadUserDetails();
                                if(userDetails != null){
                                    mDb.taskDao().insertScore(new Scores(userDetails.getUserId(), gameplayCategoryName.getText().toString(), score));

                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateFirebase(new Scores(userDetails.getUserId(), gameplayCategoryName.getText().toString(), score));
                                        }
                                    });
                                }else{
                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DynamicToast.make(mainActivity, mainActivity.getResources().getString(R.string.gameplay_endgame_dialog_login_toast), getResources()
                                                    .getColor(R.color.orange), mainActivity.getResources()
                                                    .getColor(R.color.textBlack))
                                                    .show();                                        }
                                    });
                                }
                            }
                        }).start();
                        // TODO
                        //finish();
                    }
                }).build();
    }

    private void checkAnswerCorrection(TextView answer) {
        answer1.setBackgroundColor(mainActivity.getResources().getColor(R.color.colorAccentRed));
        answer2.setBackgroundColor(mainActivity.getResources().getColor(R.color.colorAccentRed));
        answer3.setBackgroundColor(mainActivity.getResources().getColor(R.color.colorAccentRed));
        answer4.setBackgroundColor(mainActivity.getResources().getColor(R.color.colorAccentRed));

        String answerText = answer.getText().toString();
        Spanned answerCorrect =
                Html.fromHtml(questionList.get(questionIndex - 1).getCorrect_answer(), Html.FROM_HTML_MODE_COMPACT);

        if(answerText.contentEquals(answerCorrect.toString())){

            answer.setBackgroundColor(mainActivity.getResources().getColor(R.color.green));
            particlesEffect(answer);

            if(mainActivity.getSharedPref().loadVibrateState()) {
                vibe.vibrate(50);
            } else {
                vibe.vibrate(0);
            }
            scoreCorrectAnswers++;

        }else{

            if(mainActivity.getSharedPref().loadVibrateState()) {
                vibe.vibrate(300);
            } else {
                vibe.vibrate(0);
            }

            ArrayList<TextView> questions = new ArrayList<>();
            questions.add(answer1);
            questions.add(answer2);
            questions.add(answer3);
            questions.add(answer4);

            for (int j=0; j<questions.size(); j++){
                if(questions.get(j).getText().toString().equals(answerCorrect.toString())){
                    manageBlinkEffect(questions.get(j));
                }
            }
        }

        // Block UI from touch events and orientation change
        mainActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(questionIndex == 10){
                    alertDialogEndGame(scoreCorrectAnswers);
                    return;
                }

                populateQuestions(questionList);
                mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }, 2000);
    }

    private void particlesEffect(TextView button){
        new ParticleSystem(mainActivity, 10,
                mainActivity.getResources()
                        .getDrawable(R.drawable.ic_star_yellow_24dp), 1000)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(button, 10);
    }

    @SuppressLint("WrongConstant")
    private void manageBlinkEffect(TextView buttonClicked) {
        ObjectAnimator anim = ObjectAnimator.ofInt(buttonClicked,
                "backgroundColor",
                mainActivity.getResources().getColor(R.color.green),
                Color.WHITE,
                mainActivity.getResources().getColor(R.color.green));

        anim.setDuration(500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(2);
        anim.start();
    }

}
