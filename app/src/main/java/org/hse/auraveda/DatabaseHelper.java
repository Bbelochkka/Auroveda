package org.hse.auraveda;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "auroveda.db"; // Используйте правильное имя
    private static final int DATABASE_VERSION = 2;
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM mistakes WHERE question_id = ? LIMIT 1",
                new String[]{String.valueOf(questionId)}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void addMistake(int questionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        logTableStructure();
        if (isMistakeExists(questionId)) {
            Toast.makeText(context, "Вопрос уже в ошибках", Toast.LENGTH_LONG).show();
        }
        else {
            ContentValues values = new ContentValues();
            values.put("question_id", questionId);

            long result = db.insert("mistakes",null, values);
            if (result == -1){
                Toast.makeText(context,"ошибка", Toast.LENGTH_LONG).show();
            }
            else {Toast.makeText(context,"Вопрос добавлен в ошибки", Toast.LENGTH_SHORT).show();
                Cursor cursor = db.rawQuery("SELECT * FROM mistakes", null);
                Log.d("MISTAKES_LOG", "Содержимое таблицы mistakes:");

                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int favId = cursor.getInt(cursor.getColumnIndex("mistake_id"));
                        @SuppressLint("Range") int questId = cursor.getInt(cursor.getColumnIndex("question_id"));
                        Log.d("MISTAKES_LOG", "favourite_id: " + favId + ", question_id: " + questId);
                    } while (cursor.moveToNext());
                } else {
                    Log.d("MISTAKES_LOG", "Таблица favourite пуста");
                }
                cursor.close();}
        }
    }
    public void logTableStructure() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(mistakes)", null);

        Log.d("TABLE_DEBUG", "Структура таблицы 'mistakes':");
        int version = db.getVersion();
        Log.d("TABLE_DEBUG", "Текущая версия БД: " + version);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
            Log.d("TABLE_DEBUG", "Колонка: " + name + ", Тип: " + type);
        }
        cursor.close();
    }
    public boolean isQuestionInFavorites(int questionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (!isTableExists(db, "favourite")) {
            return false;
        }

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM favourite WHERE question_id = ?",
                new String[]{String.valueOf(questionId)}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public  void addFavourite(int question_id){
        SQLiteDatabase db = this.getWritableDatabase();
        //logTableStructure();
        if (isQuestionInFavorites(question_id)){
            Toast.makeText(context, "Вопрос уже в избранном", Toast.LENGTH_LONG).show();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put("question_id", question_id);

            long result = db.insert("favourite", null, cv);

            if (result == -1) {
                Toast.makeText(context, "Ошибка добавления", Toast.LENGTH_LONG).show();}
            else {
                Toast.makeText(context, "Вопрос добавлен в избранное", Toast.LENGTH_SHORT).show();

                // Логирование содержимого таблицы
                Cursor cursor = db.rawQuery("SELECT * FROM favourite", null);
                Log.d("FAVOURITE_LOG", "Содержимое таблицы favourite:");

                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int favId = cursor.getInt(cursor.getColumnIndex("favourite_id"));
                        @SuppressLint("Range") int questId = cursor.getInt(cursor.getColumnIndex("question_id"));
                        Log.d("FAVOURITE_LOG", "favourite_id: " + favId + ", question_id: " + questId);
                    } while (cursor.moveToNext());
                } else {
                    Log.d("FAVOURITE_LOG", "Таблица favourite пуста");
                }
                cursor.close();
            }
        }
    }
    public List<Question> getMistakes() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (!isTableExists(db, "mistakes")) {
            Log.e("Database", "Table 'mistakes' does not exist!");
            return questions;
        }

        // Получаем question_id из таблицы favourite
        Cursor favCursor = db.rawQuery("SELECT question_id FROM mistakes", null);
        List<Integer> questionIds = new ArrayList<>();
        if (favCursor.moveToFirst()) {
            do {
                questionIds.add(favCursor.getInt(0));
            } while (favCursor.moveToNext());
        }
        favCursor.close();

        // Получаем полные данные вопросов по их ID
        for (int id : questionIds) {
            Cursor questCursor = db.rawQuery(
                    "SELECT * FROM questions WHERE question_id = ?",
                    new String[]{String.valueOf(id)}
            );

            if (questCursor.moveToFirst()) {
                String name = questCursor.getString(questCursor.getColumnIndexOrThrow("question_name"));
                String answer = questCursor.getString(questCursor.getColumnIndexOrThrow("question_ans"));
                String option1 = questCursor.getString(questCursor.getColumnIndexOrThrow("question_option1"));
                String option2 = questCursor.getString(questCursor.getColumnIndexOrThrow("question_option2"));
                questions.add(new Question(id, name, answer, option1, option2));
            }
            questCursor.close();
        }
        return questions;
    }
    public List<Question> getFavouriteQuestions() {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (!isTableExists(db, "favourite")) {
            Log.e("Database", "Table 'favourite' does not exist!");
            return questions;
        }

        // Получаем question_id из таблицы favourite
        Cursor favCursor = db.rawQuery("SELECT question_id FROM favourite", null);
        List<Integer> questionIds = new ArrayList<>();
        if (favCursor.moveToFirst()) {
            do {
                questionIds.add(favCursor.getInt(0));
            } while (favCursor.moveToNext());
        }
        favCursor.close();

        // Получаем полные данные вопросов по их ID
        for (int id : questionIds) {
            Cursor questCursor = db.rawQuery(
                    "SELECT * FROM questions WHERE question_id = ?",
                    new String[]{String.valueOf(id)}
            );

            if (questCursor.moveToFirst()) {
                String name = questCursor.getString(questCursor.getColumnIndexOrThrow("question_name"));
                String answer = questCursor.getString(questCursor.getColumnIndexOrThrow("question_ans"));
                String option1 = questCursor.getString(questCursor.getColumnIndexOrThrow("question_option1"));
                String option2 = questCursor.getString(questCursor.getColumnIndexOrThrow("question_option2"));
                questions.add(new Question(id, name, answer, option1, option2));
            }
            questCursor.close();
        }
        return questions;
    }
}