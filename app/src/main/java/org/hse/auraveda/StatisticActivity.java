package org.hse.auraveda;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class StatisticActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistic);


        View rootView = findViewById(R.id.rootConstraintLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initNavigationButtons();
        loadStatistics();


        Button buttonCard = findViewById(R.id.getCertificate);
        buttonCard.setOnClickListener(v -> showCertificate());
    }


    private void initNavigationButtons() {
        ImageButton buttonHome = findViewById(R.id.bottomHome);
        buttonHome.setOnClickListener(v -> showHome());


        ImageButton buttonSettings = findViewById(R.id.bottomSettings);
        buttonSettings.setOnClickListener(v -> showSettings());
    }


    @SuppressLint("Range")
    private void loadStatistics() {
        LinearLayout container = findViewById(R.id.buttonsContainer);
        container.removeAllViews();


        SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM card_statistic ORDER BY card_id", null);


        if (cursor.getCount() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Нет данных статистики");
            emptyText.setTextColor(ContextCompat.getColor(this, R.color.text));
            emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            emptyText.setGravity(Gravity.CENTER);
            container.addView(emptyText);
            return;
        }


        int cardMargin = dpToPx(16);
        int cardPadding = dpToPx(16);
        int progressHeight = dpToPx(8);
        int textMarginTop = dpToPx(8);


        while (cursor.moveToNext()) {
            int cardId = cursor.getInt(cursor.getColumnIndex("card_id"));
            int score = cursor.getInt(cursor.getColumnIndex("card_score"));
            int bestTry = cursor.getInt(cursor.getColumnIndex("card_best_try"));


            // Создаем карточку статистики
            LinearLayout cardLayout = new LinearLayout(this);
            cardLayout.setOrientation(LinearLayout.VERTICAL);
            cardLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.default_button_color));


            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(cardMargin, cardMargin/2, cardMargin, cardMargin);
            cardLayout.setLayoutParams(cardParams);
            cardLayout.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);


            // Заголовок билета
            TextView title = new TextView(this);
            title.setText("Билет " + cardId);
            title.setTextColor(ContextCompat.getColor(this, R.color.text));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setTypeface(null, Typeface.BOLD);
            cardLayout.addView(title);


            // Прогресс-бар
            LinearLayout progressContainer = new LinearLayout(this);
            progressContainer.setOrientation(LinearLayout.HORIZONTAL);
            progressContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    progressHeight
            ));
            progressContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.answered_question));


            // Заполненная часть прогресс-бара
            View progress = new View(this);
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    score
            );
            progress.setLayoutParams(progressParams);
            progress.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            progressContainer.addView(progress);


            cardLayout.addView(progressContainer);


            // Результат
            TextView scoreText = new TextView(this);
            scoreText.setText("Результат: " + score + "%");
            scoreText.setTextColor(ContextCompat.getColor(this, R.color.text));
            scoreText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            scoreText.setPadding(0, textMarginTop, 0, 0);
            cardLayout.addView(scoreText);


            // Лучшая попытка
            TextView bestTryText = new TextView(this);
            bestTryText.setText("Лучшая попытка: " + bestTry);
            bestTryText.setTextColor(ContextCompat.getColor(this, R.color.text));
            bestTryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            cardLayout.addView(bestTryText);


            container.addView(cardLayout);
        }
        cursor.close();
        db.close();
    }


    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }


    private void showCertificate() {
        startActivity(new Intent(this, CertificateActivity.class));
    }


    private void showHome() {
        startActivity(new Intent(this, MainActivity.class));
    }


    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}




