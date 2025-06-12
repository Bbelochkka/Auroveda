package org.hse.auraveda;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

        // Получаем контейнер для кнопок
        LinearLayout buttonsContainer = findViewById(R.id.buttonsContainer); // Убедитесь, что в XML есть этот ID

        File dbFile = getDatabasePath("auroveda.db");
        Log.d("DatabasePath", "Database path: " + dbFile.getAbsolutePath());
        Log.d("DatabasePath", "Database exists: " + dbFile.exists());
        Log.d("DatabasePath", "Database size: " + dbFile.length() + " bytes");

        // Динамическое создание кнопок
        for (Ticket ticket : tickets) {
            Button button = new Button(this);
            button.setText(ticket.getName());
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            button.setPadding(16, 16, 16, 16);
            button.setOnClickListener(v -> {
                // Обработка нажатия на кнопку
            });
            buttonsContainer.addView(button);
        }
    }
}