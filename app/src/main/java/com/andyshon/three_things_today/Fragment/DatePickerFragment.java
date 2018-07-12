package com.andyshon.three_things_today.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;


public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mOnDateSetListener = null;
    private int mInitialYear = -1;
    private int mInitialMonth = -1;
    private int mInitialDayOfMonth = -1;


    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener, int initialYear, int initialMonth, int initialDayOfMonth) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setOnDateSetListener(listener);
        fragment.setInitialDate(initialYear, initialMonth, initialDayOfMonth);

        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog mDialog = new DatePickerDialog(getActivity(), mOnDateSetListener, mInitialYear, mInitialMonth, mInitialDayOfMonth);

        Calendar maxCal = Calendar.getInstance();
        maxCal.set(Calendar.HOUR, getRemainingHours());
        maxCal.set(Calendar.MINUTE, 59);
        maxCal.set(Calendar.SECOND, 59);
        mDialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());

        return mDialog;
    }


    private int getRemainingHours () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return 24 - LocalDateTime.now().getHour();
        }
        else {
            return 24 - new Date().getHours();
        }
    }


    private void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        mOnDateSetListener = listener;
    }


    private void setInitialDate(int initialYear, int initialMonth, int initialDayOfMonth) {
        mInitialYear = initialYear;
        mInitialMonth = initialMonth;
        mInitialDayOfMonth = initialDayOfMonth;
    }
}
