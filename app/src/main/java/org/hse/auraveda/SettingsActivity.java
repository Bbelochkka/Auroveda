package org.hse.auraveda;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);


        // Используем корневой ConstraintLayout из XML
        View rootView = findViewById(R.id.rootConstraintLayout);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Обработчик для кнопки "Обнулить ошибки"
        Button deleteMistakesBtn = findViewById(R.id.deleteMistakes);
        deleteMistakesBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Вы уверены, что хотите очистить все ошибки?")
                    .setPositiveButton("Да", (dialog, which) -> dbHelper.clearMistakes())
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        // Обработчик для кнопки "Обнулить статистику"
        Button clearStatisticsBtn = findViewById(R.id.clearStatistics);
        clearStatisticsBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Вы уверены, что хотите очистить всю статистику?")
                    .setPositiveButton("Да", (dialog, which) -> dbHelper.clearStatistics())
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        // Обработчик для кнопки "Обнулить избранное"
        Button clearFavouriteBtn = findViewById(R.id.clearFavourite);
        clearFavouriteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Вы уверены, что хотите очистить все избранное?")
                    .setPositiveButton("Да", (dialog, which) -> dbHelper.clearFavorites())
                    .setNegativeButton("Отмена", null)
                    .show();
        });


        // Находим кнопку по ID
        ImageButton buttonHome = findViewById(R.id.bottomHome);
        // Устанавливаем обработчик нажатия
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHome();
            }
        });

        // Находим кнопку по ID
        ImageButton buttonStatistic = findViewById(R.id.bottomStatistic);
        // Устанавливаем обработчик нажатия
        buttonStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatistics();
            }
        });

    }
    private void showHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void showStatistics() {
        Intent intent = new Intent(this, StatisticActivity.class);
        startActivity(intent);
    }

}




