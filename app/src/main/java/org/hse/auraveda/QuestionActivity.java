package org.hse.auraveda;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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

        dbHelper = new DatabaseHelper(this);
        questions = dbHelper.getQuestionsByCardId(cardId);

        if (questions.isEmpty()) {
            Toast.makeText(this, "Вопросы не найдены", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupQuestionIndicators();
        setupNavigationButtons();
        setupAnswerButtonsListeners();
        showQuestion(0);
    }

    private void setupQuestionIndicators() {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        indicatorLayout.removeAllViews();

        for (int i = 0; i < questions.size(); i++) {
            AppCompatButton button = new AppCompatButton(this);
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
            View view = answerLayout.getChildAt(i);
            if (view instanceof AppCompatButton) {
                AppCompatButton button = (AppCompatButton) view;
                button.setOnClickListener(v -> checkAnswer(button));
            }
        }
    }

    private void checkAnswer(AppCompatButton selectedButton) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        String selectedAnswer = selectedButton.getText().toString();

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            // Правильный ответ - зеленый
            selectedButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correct_answer))
            );
        } else {
            // Неправильный ответ - красный
            selectedButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wrong_answer))
            );

            if (!dbHelper.isMistakeExists(currentQuestion.getId())) {
                dbHelper.addMistake(currentQuestion.getId());
            }
        }
        disableAnswerButtons();

        // Показать правильный ответ
        highlightCorrectAnswer(currentQuestion);
    }

    private void highlightCorrectAnswer(Question question) {
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            View view = answerLayout.getChildAt(i);
            if (view instanceof AppCompatButton) {
                AppCompatButton button = (AppCompatButton) view;
                if (button.getText().toString().equals(question.getCorrectAnswer())) {
                    button.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correct_answer))
                    );
                }
            }
        }
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
            View view = answerLayout.getChildAt(i);
            if (view instanceof AppCompatButton) {
                AppCompatButton button = (AppCompatButton) view;
                button.setEnabled(true);
                // Возвращаем исходный цвет кнопки
                button.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.default_button_color))
                );
            }
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
        for (int i = 0; i < answerLayout.getChildCount(); i++) {
            View view = answerLayout.getChildAt(i);
            if (view instanceof AppCompatButton) {
                AppCompatButton button = (AppCompatButton) view;
                switch (i) {
                    case 0: button.setText(question.getOption1()); break;
                    case 1: button.setText(question.getOption2()); break;
                    case 2: button.setText(question.getCorrectAnswer()); break;
                }
            }
        }

        resetAnswerButtons();
        updateQuestionIndicators(index);
    }

    private void updateQuestionIndicators(int currentIndex) {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View view = indicatorLayout.getChildAt(i);
            if (view instanceof AppCompatButton) {
                AppCompatButton button = (AppCompatButton) view;
                if (i == currentIndex) {
                    button.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.current_question))
                    );
                } else {
                    button.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.default_question_indicator))
                    );
                }
            }
        }
    }

    private int getCardId() {
        return getIntent().getIntExtra("CARD_ID", 1);
    }
}