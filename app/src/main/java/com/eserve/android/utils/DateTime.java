package com.eserve.android.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateTime {


    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public static int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int getMin() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static int getFormat() {
        return Calendar.getInstance().get(Calendar.AM_PM);
    }

    public static String getCurrentDate() {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yy", Locale.getDefault());
        return dateFormat.format(time);
    }

    public static String getCurrentTime() {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(time);
    }
}
