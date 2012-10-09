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

import android.app.Activity;
import android.os.Bundle;
import android.test.LaunchPerformanceBase;
import android.util.Log;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Debug;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.ActivityInstrumentationTestCase2;

public class Settings_LaunchTime extends
        ActivityInstrumentationTestCase2<Settings> {

    public static final String LOG_TAG = "Settings_LaunchTime AOSP_Tests";
    public long SettingsAppLaunchTime = 0;

    public Settings_LaunchTime() {
        super("com.android.settings", Settings.class);
    }

    @Override
    protected void setUp() throws Exception {
        long beforeStart = System.currentTimeMillis();
        getActivity();
        long SettingsStarted = System.currentTimeMillis();
        SettingsAppLaunchTime = SettingsStarted - beforeStart;
        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test_Settings_App_LaunchTime() throws Exception {
        Log.v(LOG_TAG,
                "Instrumentation test start test_Settings_App_LaunchTime");

        Log.v(LOG_TAG,
                "This test will determine Settings app Launch Time. Will fail if it is > 700ms");
        Log.v(LOG_TAG, "Settings startup time SettingsAppLaunchTime: "
                + SettingsAppLaunchTime);
        assertFalse("SettingsAppLaunchTime is > 700ms which is wrong",
                (SettingsAppLaunchTime > 700));

        Log.v(LOG_TAG,
                "Instrumentation test stopped test_Settings_App_LaunchTime");
    }

}
