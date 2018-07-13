package com.andyshon.three_things_today.Activity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andyshon.three_things_today.Fragment.DatePickerFragment;
import com.andyshon.three_things_today.R;
import com.andyshon.three_things_today.database.DatabaseCallback;
import com.andyshon.three_things_today.database.ThreeThingsContract;
import com.andyshon.three_things_today.database.ThreeThingsContract.ThreeThingsEntry;
import com.andyshon.three_things_today.database.ThreeThingsDatabaseModel;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener, TextWatcher, DatePickerDialog.OnDateSetListener, DatabaseCallback {


    private ThreeThingsDatabaseModel databaseModel = null;
    private EditText firstThingEditText, secondThingEditText, thirdThingEditText;

    private int mSelectedYear = -1;
    private int mSelectedMonth = -1;
    private int mSelectedDayOfMonth = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseModel = new ThreeThingsDatabaseModel(getApplicationContext(), this);

        firstThingEditText = (EditText) findViewById(R.id.tv_first_edit_text);
        secondThingEditText = (EditText) findViewById(R.id.tv_second_edit_text);
        thirdThingEditText = (EditText) findViewById(R.id.tv_third_edit_text);

        firstThingEditText.setOnFocusChangeListener(this);
        secondThingEditText.setOnFocusChangeListener(this);
        thirdThingEditText.setOnFocusChangeListener(this);

        firstThingEditText.addTextChangedListener(this);
        secondThingEditText.addTextChangedListener(this);
        thirdThingEditText.addTextChangedListener(this);

        final Calendar c = Calendar.getInstance();
        mSelectedYear = c.get(Calendar.YEAR);
        mSelectedMonth = c.get(Calendar.MONTH);
        mSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        updateDateText();

        updateThreeThingsText();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_export_database:
                databaseModel.exportTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        databaseModel.close();
        super.onDestroy();
    }


    public void showDatePickerDialog(View v) {
        DialogFragment fragment = DatePickerFragment.newInstance(this, mSelectedYear, mSelectedMonth, mSelectedDayOfMonth);
        fragment.show(getSupportFragmentManager(), "DatePicker");
    }


    public void submitThreeThings() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        ContentValues values = new ContentValues();
        values.put(ThreeThingsEntry.COLUMN_NAME_YEAR, mSelectedYear);
        values.put(ThreeThingsEntry.COLUMN_NAME_MONTH, mSelectedMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_DAY_OF_MONTH, mSelectedDayOfMonth);
        values.put(ThreeThingsEntry.COLUMN_NAME_FIRST_THING, firstThingEditText.getText().toString().trim());
        values.put(ThreeThingsEntry.COLUMN_NAME_SECOND_THING, secondThingEditText.getText().toString().trim());
        values.put(ThreeThingsEntry.COLUMN_NAME_THIRD_THING, thirdThingEditText.getText().toString().trim());


        Handler handler = new Handler();

        // if we type fast task doesn't have time to store in db with timer()
        databaseModel.writeTask(values)
                //.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }, 500);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, "error:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mSelectedYear = year;
        mSelectedMonth = month;
        mSelectedDayOfMonth = dayOfMonth;

        updateDateText();
        updateThreeThingsText();
    }


    private void updateDateText() {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, mSelectedYear);
        c.set(Calendar.MONTH, mSelectedMonth);
        c.set(Calendar.DAY_OF_MONTH, mSelectedDayOfMonth);

        TextView dateTextView = (TextView) findViewById(R.id.tvDate);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        String str = dateFormat.format(c.getTime()) + ", I ...";
        dateTextView.setText(str);
    }


    private void updateThreeThingsText() {
        databaseModel.readThreeThings(mSelectedYear,mSelectedMonth,mSelectedDayOfMonth)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String[]>() {
                    @Override
                    public void accept(String[] strings) throws Exception {
                        onGetThreeThings(strings);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, "Error! " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            // Focus changed -> write
            submitThreeThings();
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Without implementation
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Without implementation
    }


    @Override
    public void afterTextChanged(Editable s) {
        // Text changed -> write
        submitThreeThings();
    }


    private void onGetThreeThings (String... s) {
        firstThingEditText.setText(s[0]);
        secondThingEditText.setText(s[1]);
        thirdThingEditText.setText(s[2]);
    }


    @Override
    public void onExportFile(File tempFile) {
        if (tempFile == null) {
            Toast.makeText(this, "Невозможно экспортировать базу данных, пожалуйста, повторите позже", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, ThreeThingsContract.FILEPROVIDER, tempFile));
        sendIntent.setType("text/csv");
        this.startActivity(Intent.createChooser(sendIntent, "Экспортировать базу данных в ..."));
    }
}
