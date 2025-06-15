package org.hse.auraveda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "auroveda.db"; // Используйте правильное имя
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    private String dbPath;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        copyDatabaseIfNeeded();
    }

    private void copyDatabaseIfNeeded() {
        if (!checkDatabaseExists()) {
            this.getReadableDatabase(); // Создаст пустую базу, если её нет
            try {
                copyDatabaseFromAssets();
            } catch (IOException e) {
                throw new RuntimeException("Error copying database", e);
            }
        }
    }

    private boolean checkDatabaseExists() {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    private void copyDatabaseFromAssets() throws IOException {
        try {
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
            OutputStream outputStream = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("Database", "Database copied from assets");
        } catch (IOException e) {
            Log.e("Database", "Error copying database", e);
            throw e;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаем таблицу ошибок при первом запуске
        createMistakeTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // При обновлении также создаем таблицу
        createMistakeTable(db);
    }

    private void createMistakeTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mistake (" +
                "mistake_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "question_id INTEGER NOT NULL UNIQUE)");
    }


    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Проверка существования таблицы
        if (!isTableExists(db, "my_card")) {
            Log.e("Database", "Table 'my_card' does not exist!");
            return tickets;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM my_card", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                tickets.add(new Ticket(id, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tickets;
    }
    public List<Question> getQuestionsByCardId(int cardId) {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (!isTableExists(db, "questions")) {
            Log.e("Database", "Table 'questions' does not exist!");
            return questions;
        }

        Cursor cursor = db.rawQuery(
                "SELECT * FROM questions WHERE card_id = ?",
                new String[]{String.valueOf(cardId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("question_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("question_name"));
                String answer = cursor.getString(cursor.getColumnIndexOrThrow("question_ans"));
                String option1 = cursor.getString(cursor.getColumnIndexOrThrow("question_option1"));
                String option2 = cursor.getString(cursor.getColumnIndexOrThrow("question_option2"));

                questions.add(new Question(id, name, answer, option1, option2));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questions;
    }


    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean isMistakeExists(int questionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }

        // Создаем таблицу, если ее нет
        createMistakeTable(db);

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM mistake WHERE question_id = ?",
                    new String[]{String.valueOf(questionId)}
            );

            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void addMistake(int questionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null) {
            return;
        }

        // Создаем таблицу, если ее нет
        createMistakeTable(db);

        try {
            ContentValues values = new ContentValues();
            values.put("question_id", questionId);

            // Используем insertWithOnConflict для обработки UNIQUE constraint
            long result = db.insertWithOnConflict(
                    "mistake",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );

            if (result == -1) {
                Log.d("MistakeDB", "Mistake already exists for question: " + questionId);
            } else {
                Log.d("MistakeDB", "Mistake added for question: " + questionId);
            }
        } catch (SQLException e) {
            Log.e("MistakeDB", "Error adding mistake", e);
        }
    }
}