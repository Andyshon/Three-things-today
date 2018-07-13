package com.andyshon.three_things_today.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.andyshon.three_things_today.database.ThreeThingsContract.ThreeThingsEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;


public class ThreeThingsDatabaseModel {

    private final String TAG = "DatabaseExport";

    private final ThreeThingsDbHelper dbHelper;

    private final DatabaseCallback callback;

    public ThreeThingsDatabaseModel(Context context, DatabaseCallback callback) {
        dbHelper = new ThreeThingsDbHelper(context);
        this.callback = callback;
    }


    public void close() {
        dbHelper.close();
    }


    private void writeContentValues(ContentValues values) {
        if (values == null) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.replace(ThreeThingsEntry.TABLE_NAME, null, values);
    }


    public Observable<String[]> readThreeThings (int year, int month, int dayOfMonth) {

        return Observable.create(new ObservableOnSubscribe<String[]>() {
            @Override
            public void subscribe(ObservableEmitter<String[]> e) throws Exception {

                // If there are no current results for the selection args, we just return empty things.
                String[] results = {
                        "",
                        "",
                        ""
                };

                String[] projection = {
                        ThreeThingsContract.ThreeThingsEntry.COLUMN_NAME_FIRST_THING,
                        ThreeThingsEntry.COLUMN_NAME_SECOND_THING,
                        ThreeThingsEntry.COLUMN_NAME_THIRD_THING
                };
                String selection = ThreeThingsEntry.COLUMN_NAME_YEAR + " = ? AND " +
                        ThreeThingsEntry.COLUMN_NAME_MONTH + " = ? AND " +
                        ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH + " = ?";
                String[] selectionArgs = {
                        Integer.toString(year),
                        Integer.toString(month),
                        Integer.toString(dayOfMonth)
                };

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        ThreeThingsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);

                if (cursor.moveToFirst()) {
                    results[0] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_FIRST_THING));
                    results[1] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_SECOND_THING));
                    results[2] = cursor.getString(cursor.getColumnIndexOrThrow(ThreeThingsEntry.COLUMN_NAME_THIRD_THING));
                }
                cursor.close();

                if (!e.isDisposed()) {
                    e.onNext(results);
                    e.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Completable writeTask (ContentValues values) {

        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                writeContentValues(values);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public void exportTask (final Context context) {

        final File[] tempFile = new File[1];
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                File tempDir = new File(context.getCacheDir() + File.separator + "database_exports");
                if (!tempDir.exists() && !tempDir.mkdir()) {
                    Log.e(TAG, "Unable to create temporary directory");
                }

                try {
                    tempFile[0] = File.createTempFile("three-things-today-data", ".csv", tempDir);
                    FileWriter fileWriter = new FileWriter(tempFile[0]);
                    fileWriter.append(exportDatabaseToCsvString());
                    fileWriter.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error whilst writing temporary file", e);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onExportFile(tempFile[0]);
                    }
                });
            }
        });
        thread.start();
    }


    private String exportDatabaseToCsvString() {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);
        csvWriter.writeNext(ThreeThingsEntry.COLUMNS);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ThreeThingsEntry.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            List<String> data = new ArrayList<String>(ThreeThingsEntry.COLUMNS.length);
            for (String column : ThreeThingsEntry.COLUMNS) {
                data.add(cursor.getString(cursor.getColumnIndexOrThrow(column)));
            }
            csvWriter.writeNext(data.toArray(new String[0]));
        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            // Ignore
        } finally {
            cursor.close();
        }

        return stringWriter.toString();
    }
}
