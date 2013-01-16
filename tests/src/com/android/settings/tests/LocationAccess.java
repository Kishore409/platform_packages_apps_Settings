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

import android.test.ActivityInstrumentationTestCase2;

import android.app.Instrumentation;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.location.LocationManager;
import android.location.Location;
import android.provider.Settings.Secure;

import java.lang.Exception;
import java.lang.Throwable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

public class LocationAccess extends ActivityInstrumentationTestCase2<Settings> {
    private String TAG = "Geolocation AOSP_tests";

    private Secure mSecure = null;
    private Instrumentation mInstrumentation = null;
    private Context mContext = null;
    private SharedPreferences mPreferences = null;
    private Intent mIntent = null;
    private Activity mSettings = null;

    public LocationAccess() {
        super(Settings.class);
    }

    public void testGeolocation() throws Exception {
        mInstrumentation = getInstrumentation();
        mSettings = getActivity();
        mContext = mInstrumentation.getTargetContext();

        // set all providers to disable
        mSecure.setLocationProviderEnabled(mSettings.getContentResolver(),
                LocationManager.GPS_PROVIDER, false);
        mSecure.setLocationProviderEnabled(mSettings.getContentResolver(),
                LocationManager.NETWORK_PROVIDER, false);

        // get the list of all available providers (should be empty)
        String mProviders = mSecure.getString(mSettings.getContentResolver(),
                mSecure.LOCATION_PROVIDERS_ALLOWED);
        Log.d(TAG, "All available providers: " + mProviders);
        assertTrue("The list of provider should be empty",
                !mProviders.contains("network"));

        // enable network provider, confirm anonymous collection of data
        mSecure.setLocationProviderEnabled(mSettings.getContentResolver(),
                LocationManager.NETWORK_PROVIDER, true);
        Thread.sleep(1000);
        mInstrumentation
                .sendCharacterSync(android.view.KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(1000);
        mInstrumentation.sendCharacterSync(android.view.KeyEvent.KEYCODE_ENTER);
        Thread.sleep(1000);

        // get the list of available providers, only network provider should be
        // listed
        mProviders = mSecure.getString(mSettings.getContentResolver(),
                mSecure.LOCATION_PROVIDERS_ALLOWED);
        Log.d(TAG, "All available providers: " + mProviders);
        assertTrue(
                "The list of provider should include only the network provider",
                mProviders.contains("network"));

        // print the actual location
        Location mLocation;
        LocationManager mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> mAllProviders = mLocationManager.getAllProviders();
        Log.d(TAG, "Number of providers is " + mAllProviders.size());
        for (int i = 0; i < mAllProviders.size(); i++) {
            Log.d(TAG, "Provider: " + mAllProviders.get(i));
            mLocation = mLocationManager.getLastKnownLocation(mAllProviders
                    .get(i));
            Log.d(TAG, "" + mLocation.toString());
        }

        // disable network provider
        mSecure.setLocationProviderEnabled(mSettings.getContentResolver(),
                LocationManager.NETWORK_PROVIDER, false);
    }

}

