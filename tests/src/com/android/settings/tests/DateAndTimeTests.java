/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.test.ActivityInstrumentationTestCase2;

import android.content.Context;
import android.content.Intent;
import android.app.Instrumentation;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;

public class DateAndTimeTests extends
        ActivityInstrumentationTestCase2<Settings> {

    private static final String TAG = "DateAndTimeTests AOSP_Tests";
    private final String DATE_FORMAT = "date_format";

    private final String FORMAT0 = "";
    private final String FORMAT1 = "MM-dd-yyyy";
    private final String FORMAT2 = "dd-MM-yyyy";
    private final String FORMAT3 = "yyyy-MM-dd";

    private Instrumentation mInstrumentation = null;
    private System mSystem = null;
    private Settings mSettings = null;
    private Context mContext = null;

    public DateAndTimeTests() {
        super("com.android.settings", Settings.class);
    }

    public void setUp() {

        mInstrumentation = getInstrumentation();
        mSettings = getActivity();
        mContext = mInstrumentation.getTargetContext();
    }

    public void dateFormatScenario(String format) throws Exception {
        // change the date format
        boolean mChanged = System.putString(mContext.getApplicationContext()
                .getContentResolver(), System.DATE_FORMAT, format);
        assertTrue(
                "Can't change format from system preference. Aborting test.",
                mChanged);
        mSettings = getActivity();

        // get current date and convert it using the selected format
        Calendar mCalendar = Calendar.getInstance();
        java.text.DateFormat mShortDateFormat = DateFormat
                .getDateFormat(mContext.getApplicationContext());
        String currentDate = mShortDateFormat.format(mCalendar.getTime());
        Log.d(TAG, "Current  " + currentDate);

        // verify that the selected format is correct
        if (format.equals(FORMAT1)) {
            assertTrue("Format has not changed ",
                    currentDate.equals(new SimpleDateFormat("MM/dd/yyyy")
                            .format(new Date())));
            Log.d(TAG, new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
        } else if (format.equals(FORMAT2)) {
            assertTrue("Format has not changed ",
                    currentDate.equals(new SimpleDateFormat("dd/MM/yyyy")
                            .format(new Date())));
            Log.d(TAG, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        } else if (format.equals(FORMAT3)) {
            assertTrue("Format has not changed ",
                    currentDate.equals(new SimpleDateFormat("yyyy/MM/dd")
                            .format(new Date())));
            Log.d(TAG, new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
        } else if (format.equals(FORMAT0)) {
            assertTrue("Format has not changed ",
                    currentDate.equals(new SimpleDateFormat("M/d/yyyy")
                            .format(new Date())));
            Log.d(TAG, new SimpleDateFormat("M/d/yyyy").format(new Date()));
        }

    }

    // actual tests
    public void testDateFormat1() throws Exception {
        Log.d(TAG, "Instrumentation test started testDateFormat1");
        dateFormatScenario(FORMAT1);
        Log.d(TAG, "Instrumentation test stopped testDateFormat1");
    }

    public void testDateFormat2() throws Exception {
        Log.d(TAG, "Instrumentation test started testDateFormat2");
        dateFormatScenario(FORMAT2);
        Log.d(TAG, "Instrumentation test stopped testDateFormat2");
    }

    public void testDateFormat3() throws Exception {
        Log.d(TAG, "Instrumentation test started testDateFormat3");
        dateFormatScenario(FORMAT3);
        Log.d(TAG, "Instrumentation test stopped testDateFormat3");
    }

    public void testDateFormat0() throws Exception {
        Log.d(TAG, "Instrumentation test started testDateFormat4");
        dateFormatScenario(FORMAT0);
        Log.d(TAG, "Instrumentation test stopped testDateFormat4");
    }

    public void testSetTime() throws Exception {
        Log.d(TAG, "Instrumentation test started testSetTime");
        Calendar mCalendar = Calendar.getInstance();
        int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = mCalendar.get(Calendar.MINUTE);
        Log.d(TAG, "Current time is " + mHour + ":" + mMinute);
        new DateTimeSettings().setTime(mContext, 0, 0);
        mCalendar = Calendar.getInstance();
        assertTrue(
                "Hour was not changed " + mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.HOUR_OF_DAY) == 0);
        assertTrue("Minute was not changed " + mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.MINUTE) == 0);
        new DateTimeSettings().setTime(mContext, mHour, mMinute);
        Log.d(TAG, "Instrumentation test stopped testSetTime");
    }

    public void testSetDate() throws Exception {
        Log.d(TAG, "Instrumentation test started testSetDate");
        Calendar mCalendar = Calendar.getInstance();
        int mYear = mCalendar.get(Calendar.YEAR);
        int mMonth = mCalendar.get(Calendar.MONTH) + 1;
        int MDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "Current date is " + MDay + " " + mMonth + " " + mYear);
        new DateTimeSettings().setDate(mContext, 2000, 10, 20);
        mCalendar = Calendar.getInstance();
        assertTrue("Year was not changed " + mCalendar.get(Calendar.YEAR),
                (mCalendar.get(Calendar.YEAR)) == 2000);
        assertTrue("Month was not changed " + mCalendar.get(Calendar.MONTH),
                (mCalendar.get(Calendar.MONTH)) == 10);
        assertTrue(
                "Day was not changed " + mCalendar.get(Calendar.DAY_OF_MONTH),
                (mCalendar.get(Calendar.DAY_OF_MONTH)) == 20);
        new DateTimeSettings().setDate(mContext, mYear, mMonth - 1, MDay);
        Log.d(TAG, "Instrumentation test stopped testSetDate");
    }

    public void test24HourFormat() throws Exception {
        Log.d(TAG, "Instrumentation test started test24HourFormat");

        Calendar mCalendar = Calendar.getInstance();
        // enable 24 hour format
        mSystem.putString(mContext.getContentResolver(),
                android.provider.Settings.System.TIME_12_24, "24");
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        mContext.sendBroadcast(timeChanged);

        // change time to 14:00
        int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = mCalendar.get(Calendar.MINUTE);
        new DateTimeSettings().setTime(mContext, 14, 00);

        // disable 24 hour format
        mSystem.putString(mContext.getContentResolver(),
                android.provider.Settings.System.TIME_12_24, "12");
        mContext.sendBroadcast(timeChanged);

        mCalendar = Calendar.getInstance();
        int mNewHour = mCalendar.get(Calendar.HOUR);
        int mMeridian = mCalendar.get(Calendar.AM_PM);

        // after changing to 12h format it should be 2 o'clock
        Log.d(TAG, "Current hour " + mNewHour);
        assertTrue("Hour format did not change " + mNewHour, (mNewHour == 2)
                && (mMeridian == 1));

        // revert to old time
        new DateTimeSettings().setTime(mContext, mHour, mMinute);
        mContext.sendBroadcast(timeChanged);
        Log.d(TAG, "Instrumentation test stopped test24HourFormat");

    }

}
