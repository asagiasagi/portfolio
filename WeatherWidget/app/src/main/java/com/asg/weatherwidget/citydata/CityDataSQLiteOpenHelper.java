package com.asg.weatherwidget.citydata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CityDataSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    // データーベース情報を変数に格納
    private static final String DATABASE_NAME = "CityData.db";
    private static final String TABLE_NAME = "citydata";
    private static final String COLUMN_CITY_ID_INTEGER_PRIMARY = "id";
    private static final String COLUMN_CITY_NAME_TEXT = "name";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_CITY_ID_INTEGER_PRIMARY + " INTEGER PRIMARY KEY," +
                    COLUMN_CITY_NAME_TEXT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public CityDataSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void saveData(SQLiteDatabase db, int id, String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_ID_INTEGER_PRIMARY, id);
        values.put(COLUMN_CITY_NAME_TEXT, name);

        db.replace(TABLE_NAME, null, values);
    }

    public String readData(SQLiteDatabase db, int id) {
        String name = null;

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_CITY_ID_INTEGER_PRIMARY, COLUMN_CITY_NAME_TEXT},
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            if (cursor.getInt(0) == id) {
                name = cursor.getString(1);
                break;
            }
        }

        cursor.close();

        return name;
    }
}
