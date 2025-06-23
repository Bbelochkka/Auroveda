package org.hse.auraveda;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourite); // Исправлено на правильный layout

        View rootView = findViewById(R.id.rootConstraintLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showFavourite();
    }

    private void showFavourite() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Question> favouriteQuestions = dbHelper.getFavouriteQuestions();

        TextView textView = findViewById(R.id.textView);
        if (favouriteQuestions.isEmpty()) {
            textView.setText("Нет избранных вопросов");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Question question : favouriteQuestions) {
            sb.append("Вопрос: ").append(question.getName()).append("\n\n");
            sb.append("Правильный ответ: ").append(question.getCorrectAnswer()).append("\n");
            sb.append("Вариант 1: ").append(question.getOption1()).append("\n");
            sb.append("Вариант 2: ").append(question.getOption2()).append("\n\n");
            sb.append("--------------------------------\n\n");
        }
        textView.setText(sb.toString());
    }
}