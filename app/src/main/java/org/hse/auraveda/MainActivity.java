package org.hse.auraveda;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Находим кнопку по ID
        Button buttonCard = findViewById(R.id.buttonCard);
        // Устанавливаем обработчик нажатия
        buttonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTickets();
            }
        });

        ImageButton buttonStatistic = findViewById(R.id.bottomStatistic);
        buttonStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatistics();
            }
        });
        Button buttonMistakes = findViewById(R.id.buttonMistakes);
        buttonMistakes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMistakes();
            }
        });
        Button buttonFavourite = findViewById(R.id.buttonFavourite);
        buttonFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFavourite();
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


    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void showTickets() {
        Intent intent = new Intent(this, TicketsActivity.class);
        startActivity(intent);
    }
    private void showStatistics() {
        Intent intent = new Intent(this, StatisticActivity.class);
        startActivity(intent);
    }
    private void showMistakes() {
        Intent intent = new Intent(this, MistakeActivity.class);
        startActivity(intent);
    }
    private void showFavourite() {
        Intent intent = new Intent(this, FavouriteActivity.class);
        startActivity(intent);
    }
}

