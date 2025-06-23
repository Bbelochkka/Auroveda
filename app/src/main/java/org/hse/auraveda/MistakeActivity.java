package org.hse.auraveda;




import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;




import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;




import java.util.List;


public class MistakeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mistake);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        showMistakes();
    }
    private void showMistakes() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        final List<Question> mistakes = dbHelper.getMistakes(); // Добавляем final


        if (mistakes.isEmpty()) {
            Toast.makeText(this, "Нет вопросов с ошибками", Toast.LENGTH_SHORT).show();
            return;
        }


        // Получаем view из layout
        final LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        TextView questionLabel = findViewById(R.id.questionLabel);
        TextView questionText = findViewById(R.id.questionText);
        final LinearLayout answerOptions = findViewById(R.id.answerOptions);
        Button nextButton = findViewById(R.id.nextQuestionButton);
        TextView favorite = findViewById(R.id.favorite);


        // Очищаем индикаторы
        indicatorLayout.removeAllViews();


        // Создаем индикаторы вопросов
        for (int i = 0; i < mistakes.size(); i++) {
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
            indicator.setOnClickListener(v -> showQuestion(mistakes, index));
            indicatorLayout.addView(indicator);
        }


        // Добавляем обработчик для кнопки "Следующий вопрос"
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentIndex = getCurrentQuestionIndex();
                if (currentIndex < mistakes.size() - 1) {
                    showQuestion(mistakes, currentIndex + 1);
                } else {
                    Toast.makeText(MistakeActivity.this, "Это последний вопрос", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Показываем первый вопрос
        showQuestion(mistakes, 0);
    }


    private void showQuestion(List<Question> mistakes, int index) {
        Question question = mistakes.get(index);


        // Обновляем индикаторы
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            Button button = (Button) indicatorLayout.getChildAt(i);
            button.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this,
                            i == index ? R.color.current_question : R.color.default_question_indicator)
            ));
        }


        // Устанавливаем текст вопроса
        ((TextView) findViewById(R.id.questionLabel)).setText("Ошибка " + (index + 1) + " из " + mistakes.size());
        ((TextView) findViewById(R.id.questionText)).setText(question.getName());


        // Очищаем и добавляем варианты ответов
        LinearLayout answerLayout = findViewById(R.id.answerOptions);
        answerLayout.removeAllViews();


        List<String> options = Arrays.asList(
                question.getOption1(),
                question.getOption2(),
                question.getCorrectAnswer()
        );
        Collections.shuffle(options);


        for (String option : options) {
            AppCompatButton button = new AppCompatButton(this);
            button.setText(option);


            // Устанавливаем стиль в зависимости от правильности ответа
            if (option.equals(question.getCorrectAnswer())) {
                // Для правильного ответа
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.correct_answer)
                ));
            } else {
                // Для неправильных ответов
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.default_button_color)
                ));
            }


            button.setTextColor(ContextCompat.getColor(this, R.color.text));


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            button.setLayoutParams(params);


            // Делаем кнопки нефункциональными (только просмотр)
            button.setOnClickListener(v -> {
                // Ничего не делаем при нажатии
            });


            answerLayout.addView(button);
        }
    }


    private int getCurrentQuestionIndex() {
        LinearLayout indicatorLayout = findViewById(R.id.questionIndicator);
        int currentColor = ContextCompat.getColor(this, R.color.current_question);


        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            Button button = (Button) indicatorLayout.getChildAt(i);
            if (button.getBackgroundTintList() != null &&
                    button.getBackgroundTintList().getDefaultColor() == currentColor) {
                return i;
            }
        }
        return 0;
    }








}




