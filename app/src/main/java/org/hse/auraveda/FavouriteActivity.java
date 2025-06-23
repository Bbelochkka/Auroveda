package org.hse.auraveda;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class FavouriteActivity extends AppCompatActivity {


    private List<Question> favouriteQuestions = new ArrayList<>();
    private int[] answerResults; // -1=не отвечен, 0=ошибка, 1=верно
    private String[] selectedAnswers;
    private int currentQuestionIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourite);


        View rootView = findViewById(R.id.rootConstraintLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        showFavouriteQuestions();
    }


    private void showFavouriteQuestions() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        favouriteQuestions = dbHelper.getFavouriteQuestions();


        if (favouriteQuestions.isEmpty()) {
            Toast.makeText(this, "Нет избранных вопросов", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Инициализируем массивы для хранения результатов
        answerResults = new int[favouriteQuestions.size()];
        Arrays.fill(answerResults, -1);
        selectedAnswers = new String[favouriteQuestions.size()];


        // Получаем view из layout
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        TextView questionLabel = findViewById(R.id.questionLabel);
        TextView questionText = findViewById(R.id.questionText);
        LinearLayout answerOptions = findViewById(R.id.answerOptions);
        Button nextButton = findViewById(R.id.nextQuestionButton);
        TextView favorite = findViewById(R.id.favorite);


        // Очищаем индикаторы
        indicatorLayout.removeAllViews();


        // Создаем индикаторы вопросов
        for (int i = 0; i < favouriteQuestions.size(); i++) {
            Button indicator = new Button(this);
            indicator.setText(String.valueOf(i + 1));
            indicator.setBackgroundColor(ContextCompat.getColor(this, R.color.default_question_indicator));
            indicator.setTextColor(ContextCompat.getColor(this, R.color.text));


            int sizeInDp = 40;
            float density = getResources().getDisplayMetrics().density;
            int sizeInPx = (int) (sizeInDp * density);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    sizeInPx,
                    sizeInPx
            );
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);


            final int index = i;
            indicator.setOnClickListener(v -> showQuestion(index));
            indicatorLayout.addView(indicator);
        }


        // Настройка кнопки "Следующий вопрос"
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < favouriteQuestions.size() - 1) {
                showQuestion(currentQuestionIndex + 1);
            } else {
                Toast.makeText(this, "Это последний вопрос", Toast.LENGTH_SHORT).show();
            }
        });


        // Показываем первый вопрос
        showQuestion(0);
    }


    private void showQuestion(int index) {
        currentQuestionIndex = index;
        Question question = favouriteQuestions.get(index);


        // Обновляем индикаторы
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


        // Устанавливаем текст вопроса
        ((TextView) findViewById(R.id.questionLabel)).setText("Избранное " + (index + 1) + " из " + favouriteQuestions.size());
        ((TextView) findViewById(R.id.questionText)).setText(question.getName());


        // Очищаем и добавляем варианты ответов
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        answerLayout.removeAllViews();


        List<String> options = new ArrayList<>();
        options.add(question.getOption1());
        options.add(question.getOption2());
        options.add(question.getCorrectAnswer());


        // Перемешиваем только если вопрос еще не отвечен
        if (answerResults[currentQuestionIndex] == -1) {
            Collections.shuffle(options);
        }


        for (String option : options) {
            AppCompatButton button = new AppCompatButton(this);
            button.setText(option);


            // Устанавливаем цвет в зависимости от состояния ответа
            if (answerResults[currentQuestionIndex] != -1) {
                // Вопрос уже отвечен
                if (option.equals(question.getCorrectAnswer())) {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.correct_answer)));
                } else if (option.equals(selectedAnswers[currentQuestionIndex])) {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.wrong_answer)));
                } else {
                    button.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.default_button_color)));
                }
                button.setEnabled(false);
            } else {
                // Вопрос еще не отвечен
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.default_button_color)));
                button.setOnClickListener(v -> checkAnswer(button, question));
            }


            button.setTextColor(ContextCompat.getColor(this, R.color.text));


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            button.setLayoutParams(params);


            answerLayout.addView(button);
        }
    }


    private void checkAnswer(AppCompatButton selectedButton, Question question) {
        String selectedAnswer = selectedButton.getText().toString();
        selectedAnswers[currentQuestionIndex] = selectedAnswer;


        boolean isCorrect = selectedAnswer.equals(question.getCorrectAnswer());
        answerResults[currentQuestionIndex] = isCorrect ? 1 : 0;


        // Обновляем отображение ответов
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            AppCompatButton button = (AppCompatButton) answerLayout.getChildAt(i);
            String optionText = button.getText().toString();


            if (optionText.equals(question.getCorrectAnswer())) {
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.correct_answer)));
            } else if (optionText.equals(selectedAnswer)) {
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, isCorrect ? R.color.correct_answer : R.color.wrong_answer)));
            }
            button.setEnabled(false);
        }


        // Обновляем индикаторы
        showQuestion(currentQuestionIndex);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // При выходе очищаем результаты
        if (answerResults != null) {
            Arrays.fill(answerResults, -1);
        }
        if (selectedAnswers != null) {
            Arrays.fill(selectedAnswers, null);
        }
    }
}




