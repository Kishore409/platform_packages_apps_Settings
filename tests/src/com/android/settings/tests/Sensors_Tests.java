/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.test.ActivityInstrumentationTestCase2;
import android.app.Instrumentation;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.preference.Preference;

import com.android.settings.R;

import com.android.settings.BAT_Sensor;

public class Sensors_Tests extends ActivityInstrumentationTestCase2<Settings> {
    private static final String TAG = "Sensors_Tests AOSP_Tests";
    private Intent mintent = null;
    private String address, name;
    private Settings mActivity = null;
    private Context mcontext = null;
    private Instrumentation mInst = null;
    private BAT_Sensor msensor = null;
    private Preference mBatteryLevel = null;

    public Sensors_Tests() {
        super("com.android.settings", Settings.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
        mcontext = mInst.getTargetContext();

    }

    public void test_Barometer_pressure_Value() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Barometer_pressure_Value");
        mActivity = getActivity();
        msensor = new BAT_Sensor(Sensor.TYPE_PRESSURE);

        msensor.instantiateSensors(mcontext);
        Thread.sleep(10000);

        Log.v(TAG, "msensor.presure is between 600 and 1500"
                + msensor.verdict);
        assertTrue("msensor.presure is not validly read ", msensor.verdict);

        Log.v(TAG, "Instrumentation test stoped test_Barometer_pressure_Value");
    }

}
