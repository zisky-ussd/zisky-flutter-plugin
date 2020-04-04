package com.zisky.zisky;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ") {
        public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
            StringBuffer toFix = super.format(date, toAppendTo, pos);
            return toFix.insert(toFix.length() - 2, ':');
        }
    };
    public static Date MIN = new Date(2000, 1, 1);

    public static Date add(int field, int amount) {
        return add(now(), field, amount);
    }

    public static Date add(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static Calendar calendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    public static Date date(Calendar calendar) {
        return calendar.getTime();
    }

    public static long epochMins() {
        return epochSecs() / 60;
    }

    public static long epochSecs() {
        return System.currentTimeMillis() / 1000;
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.format(date);
    }

    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    public static Date from(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return date(calendar);
    }

    public static Date from(Long epoch) {
        return new Date(epoch * 1000);
    }

    public static Date now() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }


    public static Date parse(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Date) {
            return (Date) object;
        }
        if (object instanceof Calendar) {
            return ((Calendar) object).getTime();
        }
        try {
            return DATE_FORMAT.parse(object.toString());
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parse(Object object, String format) {
        try {

            if (TextUtils.isEmpty(format)) {

                return parse(object);
            }

            return new SimpleDateFormat(format).parse(object.toString());
        } catch (ParseException e) {
            return null;
        }
    }


    public static String stringFrom(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        return format(date(calendar));
    }

    public static String stringFrom(int year, int month, int day, String format) {

        return format(dateFrom(year, month, day), format);
    }

    public static Date dateFrom(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return date(calendar);
    }

}
