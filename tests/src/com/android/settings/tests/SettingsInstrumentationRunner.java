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

package com.android.settings.tests;

import junit.framework.TestSuite;
import android.os.Bundle;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

public class SettingsInstrumentationRunner extends InstrumentationTestRunner {
    public static String apName;
    public static String apSecurity;
    public static String apPassword;
    public static String apTestPage;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        String tmp;

        tmp = b.getString("apName");
        if (tmp != null) {
            apName = new String(tmp);
        } else
            apName = null;
        tmp = b.getString("apSecurity");
        if (tmp != null) {
            apSecurity = new String(tmp);
        } else
            apSecurity = null;
        tmp = b.getString("apPassword");
        if (tmp != null) {
            apPassword = new String(tmp);
        } else
            apPassword = null;
        tmp = b.getString("apTestPage");
        if (tmp != null) {
            apTestPage = new String(tmp);
        } else
            apTestPage = null;

    }
}
