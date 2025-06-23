package org.hse.auraveda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TicketsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tickets);

        // Используем корневой ConstraintLayout из XML
        View rootView = findViewById(R.id.rootConstraintLayout);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Загрузка данных из базы
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Ticket> tickets = dbHelper.getAllTickets();

        File dbFile = getDatabasePath("auroveda.db");
        Log.d("DatabasePath", "Database path: " + dbFile.getAbsolutePath());
        Log.d("DatabasePath", "Database exists: " + dbFile.exists());
        Log.d("DatabasePath", "Database size: " + dbFile.length() + " bytes");

        // Получаем контейнер для кнопок
        LinearLayout buttonsContainer = findViewById(R.id.buttonsContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        // Динамическое создание кнопок
        for (Ticket ticket : tickets) {
            // Инфлейтим макет кнопки
            Button button = (Button) inflater.inflate(
                    R.layout.item_button,
                    buttonsContainer,
                    false
            );

            button.setText(ticket.getName());

            // Обработка нажатия
            button.setOnClickListener(v -> {
                Intent intent = new Intent(TicketsActivity.this, QuestionActivity.class);
                intent.putExtra("CARD_ID", ticket.getId());
                startActivity(intent);
            });


            buttonsContainer.addView(button);
        }

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
        ImageButton buttonSettings = findViewById(R.id.bottomSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings();
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
    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}