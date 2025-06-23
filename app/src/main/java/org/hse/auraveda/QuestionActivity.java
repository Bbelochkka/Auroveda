package org.hse.auraveda;


import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class QuestionActivity extends AppCompatActivity {
    private static final String KEY_CURRENT_INDEX = "currentQuestionIndex";
    private static final String KEY_TIME_LEFT = "timeLeftMillis";
    private static final String KEY_ANSWER_RESULTS = "answerResults";
    private static final String KEY_SELECTED_ANSWERS = "selectedAnswers";
    private static final String KEY_CORRECT_ANSWERS = "correctAnswers";
    private static final String KEY_WRONG_ANSWERS = "wrongAnswers";

    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private DatabaseHelper dbHelper;
    private CountDownTimer timer;
    private long timeLeftMillis = 600000; // 10 минут
    private int[] answerResults; // -1=не отвечен, 0=ошибка, 1=верно
    private String[] selectedAnswers;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private Button finishTestButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);


        if (savedInstanceState != null) {
            // Восстанавливаем сохраненное состояние
            currentQuestionIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX);
            timeLeftMillis = savedInstanceState.getLong(KEY_TIME_LEFT);
            answerResults = savedInstanceState.getIntArray(KEY_ANSWER_RESULTS);
            selectedAnswers = savedInstanceState.getStringArray(KEY_SELECTED_ANSWERS);
            correctAnswers = savedInstanceState.getInt(KEY_CORRECT_ANSWERS);
            wrongAnswers = savedInstanceState.getInt(KEY_WRONG_ANSWERS);
        }


        int cardId = getIntent().getIntExtra("CARD_ID", -1);
        if (cardId == -1) {
            Toast.makeText(this, "Ошибка: билет не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        finishTestButton = findViewById(R.id.finishTestButton);
        finishTestButton.setOnClickListener(v -> finishTest());


        dbHelper = new DatabaseHelper(this);
        questions = dbHelper.getQuestionsByCardId(cardId);

        if (answerResults == null || answerResults.length != questions.size()) {
            answerResults = new int[questions.size()];
            Arrays.fill(answerResults, -1);
        }

        if (selectedAnswers == null || selectedAnswers.length != questions.size()) {
            selectedAnswers = new String[questions.size()];
        }


        if (questions.isEmpty()) {
            Toast.makeText(this, "Вопросы не найдены", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        startTimer();
        setupQuestionIndicators();
        setupNavigationButtons();

        showQuestion(0);
        setUpFavouriteListener();


    }

    private void setUpFavouriteListener(){
        TextView favourite = findViewById(R.id.favorite);
        Question currentQuestion = questions.get(currentQuestionIndex);
        favourite.setOnClickListener(v -> dbHelper.addFavourite(currentQuestion.getId()));
        showQuestion(currentQuestionIndex);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_INDEX, currentQuestionIndex);
        outState.putLong(KEY_TIME_LEFT, timeLeftMillis);
        outState.putIntArray(KEY_ANSWER_RESULTS, answerResults);
        outState.putStringArray(KEY_SELECTED_ANSWERS, selectedAnswers);
        outState.putInt(KEY_CORRECT_ANSWERS, correctAnswers);
        outState.putInt(KEY_WRONG_ANSWERS, wrongAnswers);
    }


    // Остальные методы остаются без изменений
    private void startTimer() {
        timer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerText();
            }


            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateTimerText();
                finishTest();
            }
        }.start();
    }



    private void updateTimerText() {
        int minutes = (int) (timeLeftMillis / 1000) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;
        String timeLeft = String.format("%02d:%02d", minutes, seconds);
        ((TextView) findViewById(R.id.time)).setText(timeLeft);
    }


    private void setupQuestionIndicators() {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        indicatorLayout.removeAllViews();


        for (int i = 0; i < questions.size(); i++) {
            Button button = new Button(this);
            button.setText(String.valueOf(i + 1));
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            button.setTextAppearance(this, R.style.QuestionIndicatorButton);


            final int index = i;
            button.setOnClickListener(v -> showQuestion(index));
            indicatorLayout.addView(button);
        }
    }


    private void setupNavigationButtons() {
        Button nextButton = findViewById(R.id.nextQuestionButton);
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                showQuestion(currentQuestionIndex + 1);
            } else {
                checkTestCompletion();
            }
        });
    }



    private void showQuestion(int index) {
        currentQuestionIndex = index;
        Question question = questions.get(index);

        ((TextView) findViewById(R.id.questionLabel)).setText("Билет " + getCardId() + ", вопрос " + (index + 1));
        ((TextView) findViewById(R.id.questionText)).setText(question.getName());

        // Создаем список вариантов ответов и перемешиваем их (только для новых вопросов)
        List<String> options = new ArrayList<>();
        options.add(question.getOption1());
        options.add(question.getOption2());
        options.add(question.getCorrectAnswer());

        // Перемешиваем только если вопрос еще не отвечен
        if (answerResults[currentQuestionIndex] == -1) {
            Collections.shuffle(options);
        } else {
            // Для отвеченных вопросов сохраняем оригинальный порядок
            String selectedAnswer = selectedAnswers[currentQuestionIndex];
            options.remove(selectedAnswer);
            options.add(0, selectedAnswer);
            options.remove(question.getCorrectAnswer());
            options.add(question.getCorrectAnswer());
        }

        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            button.setText(options.get(i));

            // Сбрасываем состояние кнопки только для неотвеченных вопросов
            if (answerResults[currentQuestionIndex] == -1) {
                button.setEnabled(true);
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.default_button_color)));

                button.setOnClickListener(v -> {
                    Button clickedButton = (Button) v;
                    checkAnswerImmediately(clickedButton, question);
                });
            } else {
                // Для отвеченных вопросов показываем сохраненное состояние
                button.setEnabled(false);
                String optionText = button.getText().toString();

                if (optionText.equals(question.getCorrectAnswer())) {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.correct_answer)));
                } else if (optionText.equals(selectedAnswers[currentQuestionIndex])) {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, answerResults[currentQuestionIndex] == 1 ?
                                    R.color.correct_answer : R.color.wrong_answer)));
                } else {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.default_button_color)));
                }
            }
        }

        updateNavigationButtons();
        updateQuestionIndicators();
        //setUpFavouriteListener();
    }



    private void checkAnswerImmediately(Button selectedButton, Question question) {
        // Сбрасываем цвета всех кнопок текущего вопроса перед обработкой нового ответа
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            button.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.default_button_color)));
        }

        String selectedAnswer = selectedButton.getText().toString();
        selectedAnswers[currentQuestionIndex] = selectedAnswer;

        boolean isCorrect = selectedAnswer.equals(question.getCorrectAnswer());

        if (isCorrect) {
            answerResults[currentQuestionIndex] = 1;
            correctAnswers++;
            // Подсвечиваем выбранную кнопку зеленым
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.correct_answer)));
        } else {
            answerResults[currentQuestionIndex] = 0;
            wrongAnswers++;
            // Подсвечиваем выбранную кнопку красным
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.wrong_answer)));


            if (!dbHelper.isMistakeExists(question.getId())) {
                dbHelper.addMistake(question.getId());
            }
        }

        // Показываем правильный ответ (подсвечиваем зеленым)
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            String optionText = button.getText().toString();

            if (optionText.equals(question.getCorrectAnswer())) {
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.correct_answer)));
            }
        }

        // Отключаем все кнопки после выбора ответа
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            button.setEnabled(false);
        }

        updateQuestionIndicators();
        updateNavigationButtons();
    }


    private void showCorrectAnswers(Question question) {
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            String optionText = button.getText().toString();

            if (optionText.equals(question.getCorrectAnswer())) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            }
            button.setEnabled(false);
        }
    }



    private void updateNavigationButtons() {
        Button nextButton = findViewById(R.id.nextQuestionButton);
        nextButton.setEnabled(currentQuestionIndex < questions.size() - 1);
        finishTestButton.setEnabled(allQuestionsAnswered());
    }


    private void updateQuestionIndicators() {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            Button button = (Button) indicatorLayout.getChildAt(i);
            int colorId;
            if (i == currentQuestionIndex) {
                colorId = R.color.current_question;
            } else if (answerResults[i] == 1) {
                colorId = R.color.correct_answer;
            } else if (answerResults[i] == 0) {
                colorId = R.color.wrong_answer;
            } else {
                colorId = R.color.default_question_indicator;
            }
            button.setBackgroundColor(ContextCompat.getColor(this, colorId));
        }
    }




    private boolean allQuestionsAnswered() {
        for (int result : answerResults) {
            if (result == -1) return false;
        }
        return true;
    }


    private void checkTestCompletion() {
        if (allQuestionsAnswered()) {
            finishTest();
        }
    }


    private void finishTest() {
        if (timer != null) {
            timer.cancel();
        }


        double percentage = (double) correctAnswers / questions.size() * 100;
        String message = String.format("Тест завершен!\n\nПравильных ответов: %d\nОшибок: %d\nУспешность: %.1f%%\n\n%s",
                correctAnswers, wrongAnswers, percentage, getMotivationalMessage(percentage));


        new AlertDialog.Builder(this)
                .setTitle("Результаты теста")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }


    private String getMotivationalMessage(double percentage) {
        if (percentage >= 90) return "Отличный результат! Вы настоящий эксперт!";
        if (percentage >= 70) return "Хорошая работа! Продолжайте в том же духе!";
        if (percentage >= 50) return "Неплохо, но есть куда расти!";
        return "Вам нужно больше практики! Не сдавайтесь!";
    }


    private int getCardId() {
        return getIntent().getIntExtra("CARD_ID", 1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}




