package com.andyshon.three_things_today.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.andyshon.three_things_today.database.ThreeThingsContract.ThreeThingsEntry;


public class ThreeThingsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ThreeThingsToday.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ThreeThingsEntry.TABLE_NAME + " (" +
                    ThreeThingsEntry.COLUMN_NAME_YEAR + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_MONTH + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH + " INTEGER," +
                    ThreeThingsEntry.COLUMN_NAME_FIRST_THING + " TEXT," +
                    ThreeThingsEntry.COLUMN_NAME_SECOND_THING + " TEXT," +
                    ThreeThingsEntry.COLUMN_NAME_THIRD_THING + " TEXT," +
                    "PRIMARY KEY (" +
                    ThreeThingsEntry.COLUMN_NAME_YEAR + ", " +
                    ThreeThingsEntry.COLUMN_NAME_MONTH + ", " +
                    ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH +
                    ")" +
                    ")";


    public ThreeThingsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w("SQLite", "Update from version " + i + " to version " + i1);

        // delete old table and create new
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        // create new table
        onCreate(sqLiteDatabase);
    }
}
