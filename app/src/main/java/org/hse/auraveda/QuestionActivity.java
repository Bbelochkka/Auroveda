package org.hse.auraveda;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question);

        // Используем корневой ConstraintLayout из XML
        View rootView = findViewById(R.id.rootConstraintLayout);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        int cardId = getIntent().getIntExtra("CARD_ID", -1);
        if (cardId == -1) {
            Toast.makeText(this, "Ошибка: билет не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Загружаем вопросы для данного билета
        dbHelper = new DatabaseHelper(this); // Убрали тип DatabaseHelper
        questions = dbHelper.getQuestionsByCardId(cardId);

        if (questions.isEmpty()) {
            Toast.makeText(this, "Вопросы не найдены", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Настройка UI
        setupQuestionIndicators();
        setupNavigationButtons();
        setupAnswerButtonsListeners();
        showQuestion(0);
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

            // Применяем стиль
            button.setTextAppearance(this, R.style.QuestionIndicatorButton);

            final int index = i;
            button.setOnClickListener(v -> showQuestion(index));
            indicatorLayout.addView(button);
        }
    }

    private void setupNavigationButtons() {
        Button prevButton = findViewById(R.id.previousQuestionButton);
        Button nextButton = findViewById(R.id.nextQuestionButton);

        prevButton.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                showQuestion(currentQuestionIndex - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                showQuestion(currentQuestionIndex + 1);
            }
        });
    }
    private void setupAnswerButtonsListeners() {
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            button.setOnClickListener(v -> checkAnswer(button));
        }
    }

    private void checkAnswer(Button selectedButton) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        String selectedAnswer = selectedButton.getText().toString();

        Log.d("AnswerCheck", "Selected answer: " + selectedAnswer);
        Log.d("AnswerCheck", "Correct answer: " + currentQuestion.getCorrectAnswer());
        Log.d("AnswerCheck", "Question ID: " + currentQuestion.getId());

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            Log.d("AnswerCheck", "Answer is correct");
        } else {
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
            Log.d("AnswerCheck", "Answer is wrong");

            // Проверяем и добавляем ошибку в базу
            if (!dbHelper.isMistakeExists(currentQuestion.getId())) {
                Log.d("MistakeDB", "Adding mistake for question: " + currentQuestion.getId());
                dbHelper.addMistake(currentQuestion.getId());
            } else {
                Log.d("MistakeDB", "Mistake already exists for question: " + currentQuestion.getId());
            }
        }
        disableAnswerButtons();
    }

    private void disableAnswerButtons() {
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            answerLayout.getChildAt(i).setEnabled(false);
        }
    }
    private void resetAnswerButtons() {
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            Button button = (Button) answerLayout.getChildAt(i);
            button.setEnabled(true);
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.default_button_color));
        }
    }

    private void showQuestion(int index) {
        currentQuestionIndex = index;
        Question question = questions.get(index);

        TextView questionLabel = findViewById(R.id.questionLabel);
        questionLabel.setText("Билет " + getCardId() + ", вопрос " + (index + 1));

        TextView questionText = findViewById(R.id.questionText);
        questionText.setText(question.getName());

        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        ((Button) answerLayout.getChildAt(0)).setText(question.getOption1());
        ((Button) answerLayout.getChildAt(1)).setText(question.getOption2());
        ((Button) answerLayout.getChildAt(2)).setText(question.getCorrectAnswer());

        resetAnswerButtons(); // Сбрасываем состояние кнопок
        updateQuestionIndicators(index);
    }

    private void updateQuestionIndicators(int currentIndex) {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            Button button = (Button) indicatorLayout.getChildAt(i);
            if (i == currentIndex) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.current_question));
            } else {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.default_question_indicator));
            }
        }
    }

    private int getCardId() {
        return getIntent().getIntExtra("CARD_ID", 1);
    }

}