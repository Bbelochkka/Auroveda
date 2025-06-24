package org.hse.auraveda;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CertificateActivity extends AppCompatActivity {
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_CERTIFICATE_GENERATED = "certificateGenerated";


    private EditText firstNameInput, lastNameInput;
    private ImageView certificatePreview;
    private LinearLayout certificateButtons;
    private Bitmap certificateBitmap;
    private boolean isCertificateGenerated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);


        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        certificatePreview = findViewById(R.id.certificatePreview);
        certificateButtons = findViewById(R.id.certificateButtons);


        // Восстановление состояния при повороте экрана
        if (savedInstanceState != null) {
            firstNameInput.setText(savedInstanceState.getString(KEY_FIRST_NAME));
            lastNameInput.setText(savedInstanceState.getString(KEY_LAST_NAME));
            isCertificateGenerated = savedInstanceState.getBoolean(KEY_CERTIFICATE_GENERATED, false);


            if (isCertificateGenerated) {
                String firstName = firstNameInput.getText().toString();
                String lastName = lastNameInput.getText().toString();
                if (!firstName.isEmpty() && !lastName.isEmpty()) {
                    certificateBitmap = createCertificateBitmap(firstName, lastName);
                    certificatePreview.setImageBitmap(certificateBitmap);
                    certificatePreview.setVisibility(View.VISIBLE);
                    certificateButtons.setVisibility(View.VISIBLE);
                }
            }
        }



        firstNameInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                lastNameInput.requestFocus();
                return true;
            }
            return false;
        });


        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(v -> generateCertificate());

        Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(v -> shareCertificate());

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveCertificate());

        // Находим кнопку по ID
        ImageButton buttonHome = findViewById(R.id.bottomHome);
        // Устанавливаем обработчик нажатия
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHome();
            }
        });
        ImageButton buttonStatistic = findViewById(R.id.bottomStatistic);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FIRST_NAME, firstNameInput.getText().toString());
        outState.putString(KEY_LAST_NAME, lastNameInput.getText().toString());
        outState.putBoolean(KEY_CERTIFICATE_GENERATED, isCertificateGenerated);
    }


    private void generateCertificate() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();


        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите имя и фамилию", Toast.LENGTH_SHORT).show();
            return;
        }


        certificateBitmap = createCertificateBitmap(firstName, lastName);
        certificatePreview.setImageBitmap(certificateBitmap);
        certificatePreview.setVisibility(View.VISIBLE);
        certificateButtons.setVisibility(View.VISIBLE);
        isCertificateGenerated = true;
    }


    private Bitmap createCertificateBitmap(String firstName, String lastName) {
        // Размеры сертификата
        int width = 1200;
        int height = 1000;


        // Создаем пустой bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Фон сертификата
        canvas.drawColor(Color.WHITE);

        // Добавляем логотип на фон
        Drawable logoDrawable = ContextCompat.getDrawable(this, R.drawable.lotus);
        if (logoDrawable != null) {
            // Создаем bitmap из drawable
            Bitmap logoBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas logoCanvas = new Canvas(logoBitmap);
            logoDrawable.setBounds(0, 0, width, height);
            logoDrawable.draw(logoCanvas);


            // Создаем полупрозрачную версию логотипа
            Paint alphaPaint = new Paint();
            alphaPaint.setAlpha(100); //Чтобы менять прозрачность


            // Рисуем логотип на основном canvas
            canvas.drawBitmap(logoBitmap, 0, 0, alphaPaint);
        }


        // Рисуем зеленую рамку
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#1A6C3F"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(20);
        canvas.drawRect(10, 10, width - 10, height - 10, borderPaint);


        // Текст "ДИПЛОМ"
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(80);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("ДИПЛОМ", width / 2f, 200, titlePaint);


        // Основной текст
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);


        String fullName = firstName + " " + lastName;
        String line1 = "Настоящий диплом свидетельствует о том, что";
        String line2 = fullName;
        String line3 = "прошел(а) курс по теме \"Аюрведа\" на";
        String line4 = "65%";


        canvas.drawText(line1, width / 2f, 350, textPaint);
        canvas.drawText(line2, width / 2f, 450, textPaint);
        canvas.drawText(line3, width / 2f, 550, textPaint);

        // Процент выполнения
        Paint percentPaint = new Paint();
        percentPaint.setColor(Color.parseColor("#1A6C3F"));
        percentPaint.setTextSize(100);
        percentPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        percentPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(line4, width / 2f, 700, percentPaint);
        return bitmap;
    }


    private void shareCertificate() {
        if (certificateBitmap == null) return;


        try {
            File file = saveCertificateToCache();
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file);


            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/png");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);



            startActivity(Intent.createChooser(shareIntent, "Поделиться сертификатом"));
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка при создании файла", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    private void saveCertificate() {
        if (certificateBitmap == null) return;


        try {
            File file = saveCertificateToExternalStorage();
            Toast.makeText(this, "Сертификат сохранен: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private File saveCertificateToCache() throws IOException {
        File cachePath = new File(getCacheDir(), "images");
        cachePath.mkdirs();
        File file = new File(cachePath, "certificate.png");


        FileOutputStream stream = new FileOutputStream(file);
        certificateBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();


        return file;
    }


    private File saveCertificateToExternalStorage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "AYURVEDA_CERTIFICATE_" + timeStamp + ".png";


        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir, imageFileName);


        FileOutputStream stream = new FileOutputStream(file);
        certificateBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();


        // Обновляем галерею
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);


        return file;
    }
}












