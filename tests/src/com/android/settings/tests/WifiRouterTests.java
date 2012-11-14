/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.settings.tests.Manufacturer;
import com.android.settings.tests.Operator;
import com.android.settings.wifi.WifiEnabler;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.util.Properties;
import com.android.settings.Settings;
import com.android.settings.AirplaneModeEnabler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.test.ActivityInstrumentationTestCase2;
import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import android.app.Activity;
import android.content.ContextWrapper;
import com.android.settings.R;
import android.view.Menu;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.List;
import android.view.View;
import android.test.TouchUtils;
import java.io.IOException;
import java.net.UnknownHostException;
import android.widget.TextView;
import android.text.format.Formatter;
import android.os.Environment;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.provider.Settings.System;
import com.android.settings.tests.TestConfigurationManager;

/**
 * Tests for the Settings operator/manufacturer hook.
 * 
 * Running all tests:
 * 
 * adb shell am instrument -e class \
 * com.android.settings.tests.WifiRouterTests#testConnectToWifi -w \
 * com.android.settings.tests/android.test.InstrumentationTestRunner
 */
public class WifiRouterTests extends ActivityInstrumentationTestCase2<Settings> {

    private static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 3;
    private static final int MENU_ID_SCAN = Menu.FIRST + 5;
    private static final int WAIT_FOR_WIFI_OPERATION = 1000;
    private static final int WAIT_FOR_WIFI_MENU = 2000;
    private static final int WAIT_FOR_AP_ADD = 3000;
    private static final int WAIT_FOR_WIFI_FIRST_TIME_ENABLE = 4000;
    private static final int WAIT_FOR_HTTP_CHECK = 10000;
    private static final int WAIT_FOR_WIFI_AP_SCAN = 20000;
    private static final int WIFI_CONNECT_RETRIES = 7;
    private static final int HTTP_CONNECT_RETRIES = 7;
    private static final int HTTP_SUCCESSFUL_RESPONSE_STATUS = 200;
    private static final String TAG = "WifiRouterTests";
    private static final String HTTP_PASS_STATUS = "Pass";
    private static final String OBTAINING_IP = "OBTAINING_IPADDR";
    private static final String CONFIG_DIR_PATH = "/data/app";
    private static final String WEP = "wep";
    private static final String WPA = "wpa";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private AirplaneModeEnabler mAirplaneModeEnabler;
    private TestConfigurationManager mTestConfigurationManager;
    private WifiP2pManager.Channel mChannel;
    private Intent mIntent;
    private Settings mActivity = null;
    private Context mContext;
    private Instrumentation mInst = null;
    private BroadcastReceiver receiver = null;
    private String sNull = "xxx";
    private String mPingHostnameResult;
    private String mHttpClientTestResult;

    public WifiRouterTests() {
        super(Settings.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiP2pManager = (WifiP2pManager) mContext
                .getSystemService(Context.WIFI_P2P_SERVICE);
        Intent mIntent = new Intent();
        mTestConfigurationManager = new TestConfigurationManager(getInstrumentation()
                .getContext());
    }

    public void enableWifi() throws InterruptedException {
        assertTrue(
                "Wi-Fi is in an unknown state. This state will occur when an error happens while enabling or disabling ",
                (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_UNKNOWN));
        if ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
                || (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) {
            Log.v(TAG, "WiFi state : " + mWifiManager.getWifiState());
        } else {
            mWifiManager.setWifiEnabled(true);
            Thread.sleep(WAIT_FOR_WIFI_FIRST_TIME_ENABLE);
            Log.v(TAG, "WiFi enabled : " + mWifiManager.getWifiState());
            assertTrue("Wi-Fi enabled : ",
                    (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED));
        }
    }

    private void cleanConfiguredNetworksIfAvailable() throws InterruptedException {
        List<WifiConfiguration> networksList = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "total number of configured wifi is :  " + networksList.size());

        if (networksList != null) {
            for (int i = 0; i < networksList.size(); i++) {
                Log.v(TAG,
                        "wifi number : " + i + " with network name SSID : "
                                + networksList.get(i).SSID + " netId : " + networksList.get(i).networkId);
                // Remove the specified network from the networksList of configured
                // networks
                boolean isDeleted = mWifiManager.removeNetwork(networksList.get(i).networkId);
                Log.v(TAG, " " + networksList.get(i).SSID + " -> Wifi was deleted : " + isDeleted);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
            }
        }
    }

    private void httpClientTest(String HTTP_TEST_PAGE) {
        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet request = new HttpGet(HTTP_TEST_PAGE);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HTTP_SUCCESSFUL_RESPONSE_STATUS) {
                mHttpClientTestResult = HTTP_PASS_STATUS;
            } else {
                mHttpClientTestResult = "Fail: Code: " + String.valueOf(response);
            }
            request.abort();
        } catch (IOException e) {
            mHttpClientTestResult = "Fail: IOException";
        }
    }

    public void connectToWiFi(Properties wifiProps) throws IOException,
            InterruptedException {
        Log.v(TAG, "Instrumentation test start");

        String wifiApNamne = wifiProps.getProperty("WIFI_AP_NAME");
        String wifiApSecurity = wifiProps.getProperty("WIFI_AP_SECURITY");
        String wifiApPass = wifiProps.getProperty("WIFI_AP_PASSWORD");
        String wifiTestPage = wifiProps.getProperty("HTTP_TEST_PAGE");

        mActivity = getActivity();

        // enable wifi before scan
        enableWifi();
        boolean menuTriggered = mInst.invokeMenuActionSync(mActivity, MENU_ID_SCAN, 0);
        assertTrue("Wifi SCAN menu was not triggered", menuTriggered);

        // wait until wifi SCAN action ends
        Thread.sleep(WAIT_FOR_WIFI_AP_SCAN);

        // Clean all configured networks
        cleanConfiguredNetworksIfAvailable();

        // Verify that WIFI_AP_NAME is found by the scan action, if not throw
        // error - as this is going to be the open wifi to which we try to
        // connect
        List<ScanResult> apnetworksList = mWifiManager.getScanResults();
        Log.v(TAG, "total number of wifi aps found by scan : " + apnetworksList.size());
        assertTrue("Wifi SCAN action returned no result ", (apnetworksList.size() > 0));

        if (apnetworksList != null) {
            boolean found = false;
            for (int i = 0; i < apnetworksList.size(); i++) {
                ScanResult scanResult = apnetworksList.get(i);
                Log.v(TAG, "wifi ap number : " + i + " with network name SSID : "
                        + scanResult.SSID);
                String comp = scanResult.SSID;
                Log.v(TAG, "comp = " + comp + " wifiApName = " + wifiApNamne);
                if (comp.equals(wifiApNamne)) {
                    Log.v(TAG, "found = " + found);
                    found = true;
                }
            }
            Log.v(TAG, "found = " + found);
            assertTrue(
                    "WIFI_AP_NAME was not found by SCAN which means we will not be able to connect to it",
                    found);
        }

        // ADD Network WIFI_AP_NAME to the configured AP
        menuTriggered = mInst.invokeMenuActionSync(mActivity, MENU_ID_ADD_NETWORK, 0);
        assertTrue("Wifi ADD NETWORK menu was not triggered", menuTriggered);
        Thread.sleep(WAIT_FOR_WIFI_MENU);

        // Enter WiFI AP Name
        mInst.sendStringSync(wifiApNamne);
        Thread.sleep(WAIT_FOR_WIFI_OPERATION);

        if (wifiApSecurity.equals(sNull) == false) {
            mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
            Thread.sleep(WAIT_FOR_WIFI_OPERATION);
            mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
            Thread.sleep(WAIT_FOR_WIFI_OPERATION);

            if (wifiApSecurity.toLowerCase().equals(WEP) == true) {
                mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                // enter password
                mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
                mInst.sendStringSync(wifiApPass);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
            }

            if (wifiApSecurity.toLowerCase().equals(WPA) == true) {
                mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
                mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                // enter password
                mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
                mInst.sendStringSync(wifiApPass);
                Thread.sleep(WAIT_FOR_WIFI_OPERATION);
                mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
            }
        } else {
            Log.v(TAG, "Wifi AP has no security enabled : " + wifiApSecurity);
            mInst.sendCharacterSync(KeyEvent.KEYCODE_TAB);
            Thread.sleep(WAIT_FOR_WIFI_OPERATION);

        }

        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(WAIT_FOR_WIFI_OPERATION);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(WAIT_FOR_WIFI_OPERATION);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(WAIT_FOR_AP_ADD);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        List<WifiConfiguration> networksList = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "Asserting that there is at least one configured Network");
        assertNotNull("There are no configured networks", networksList);
        Log.v(TAG, "Configured wifi Number : " + networksList.size() + " SSID : "
                + networksList.get(0).SSID);
        assertTrue(
                "Numer of configured Wifi is not 1 and  SSID is not equal with WIFI name added==",
                ((networksList.size() == 1) && (networksList.get(0).SSID.indexOf(wifiApNamne) > 0)));

        // Because this is the only configured network, device should
        // automaticaly connect to it
        assertTrue("WIFI_AP_NAME was not associated successfuly",
                mWifiManager.enableNetwork(networksList.get(0).networkId, true));
        assertTrue("WIFI_AP_NAME was not associated successfuly",
                mWifiManager.reconnect());

        WifiInfo mWifiInfo = null;
        mWifiInfo = mWifiManager.getConnectionInfo();

        int retries = 1;
        while (mWifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState()).toString() != OBTAINING_IP) {
            if (retries == WIFI_CONNECT_RETRIES) {
                break;
            }
            mWifiInfo = mWifiManager.getConnectionInfo();
            Thread.sleep(WAIT_FOR_WIFI_OPERATION);
            Log.v(TAG,
                    "waiting until ip is retrieved : "
                            + mWifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState())
                            + " retries : " + retries);
            retries++;

        }

        // try to connect to the http  configuration page of the router
        // with 10s delay
        Log.v(TAG, "HTTP check page = " + wifiTestPage);
        httpClientTest(wifiTestPage);
        retries = 1;
        while (retries != HTTP_CONNECT_RETRIES) {
            Log.v(TAG, "mHttpClientTestResult : " + mHttpClientTestResult + " retries : "
                    + retries);
            if (mHttpClientTestResult == HTTP_PASS_STATUS) {
                break;
            }
            Thread.sleep(WAIT_FOR_HTTP_CHECK);
            retries++;
            httpClientTest(wifiTestPage);
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed ", HTTP_PASS_STATUS, mHttpClientTestResult);

        // networksList Wifi Info
        mWifiInfo = mWifiManager.getConnectionInfo();
        Log.v(TAG, "mWifiInfo : " + mWifiInfo);
        networksList = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "WifiConfiguration : " + networksList);

        Log.v(TAG, "Instrumentation test stop");
    }

    public void testConnectToWifi() throws IOException, InterruptedException {
        Properties wifiProps = mTestConfigurationManager.getProperties(CONFIG_DIR_PATH);
        assertNotNull(TAG + " could not load config properties", wifiProps);
        connectToWiFi(wifiProps);
    }

}
