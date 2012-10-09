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

package com.android.settings;

import android.test.ActivityInstrumentationTestCase2;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.p2p.WifiP2pManager;
import android.content.Context;
import android.content.Intent;
import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.System;
import android.content.ContentResolver;

import android.text.format.Formatter;
import android.net.wifi.WifiManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Wifi_Settings_Tests extends
        ActivityInstrumentationTestCase2<Settings> {

    private static final String TAG = "Wifi_Settings_Tests AOSP_Tests";
    private WifiManager mWifiManager = null;
    private WifiP2pManager mWifiP2pManager = null;
    private Intent mIntent = null;
    private String address, name;
    private Settings mActivity = null;
    private Context mContext = null;
    private Instrumentation mInst = null;

    private int HTTP_Repeat = 7;
    private String url = "http://www.intel.com";

    private java.lang.System mJSystem;

    private String FileName = "AP.ini";
    private String WIFI_AP_NAME = null;
    private String WIFI_AP_NAME2 = null;

    private String mHttpClientTestResult;

    private Formatter formIp = new Formatter();

    private System mSystem;

    private Environment mEnviroment;
    private File mFile;

    String newIP;

    public Wifi_Settings_Tests() {
        super("com.android.settings", Settings.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();

        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);

        mWifiP2pManager = (WifiP2pManager) mContext
                .getSystemService(Context.WIFI_P2P_SERVICE);

        Intent mIntent = new Intent();

        // Reads the Access Points from FileName. You can add the file manually to
        // /data/data/com.android.browser/files
        // or use the test_write() method. In the file you should add your AP's name one on each
        // line.
        readAP();
    }

    // only for debug purpose
    public void writeAP() throws Exception {
        String WIFI_AP_NAME = "Cisco-WRT160" + "\n";
        String WIFI_AP_NAME2 = "open-arena";
        FileOutputStream fos = mContext.openFileOutput(FileName,
                Context.MODE_WORLD_WRITEABLE);
        fos.write(WIFI_AP_NAME.getBytes());
        fos.write(WIFI_AP_NAME2.getBytes());
        fos.close();
    }

    public void readAP() throws Exception {
        String line = null;
        String result = null;
        FileInputStream fis = mContext.openFileInput(FileName);
        DataInputStream in = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;

        strLine = br.readLine();
        Log.v(TAG, "WIFI_AP_NAME = " + strLine);
        WIFI_AP_NAME = strLine;

        strLine = br.readLine();
        Log.v(TAG, "WIFI_AP_NAME2 = " + strLine);
        WIFI_AP_NAME2 = strLine;
    }

    // only for debug purpose
    /*
     * public void test_write() throws Exception{ Log.v(TAG, "Writing..."); writeAP(); }
     * 
     * public void test_read() throws Exception { Log.v(TAG, "Reading..."); readAP(); }
     */

    public void checkWifiOff() throws Exception {
        int tries = 1;
        while (((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) || (mWifiManager
                .getWifiState() != WifiManager.WIFI_STATE_DISABLED))
                && (tries < 10)) {
            Log.v(TAG, "Waiting for WiFi to be disabled, tried " + tries
                    + " times");
            tries++;
            Thread.sleep(1000);
        }
    }

    public void Enable_Wifi() throws Exception {
        assertTrue(
                "Wi-Fi is in an unknown state. This state will occur when an error happens while enabling or disabling ",
                (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_UNKNOWN));
        if ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
                || (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) {
            Log.v(TAG, "WiFi is enabled state = " + mWifiManager.getWifiState());
        } else {
            Thread.sleep(2000);
            mWifiManager.setWifiEnabled(true);
            Thread.sleep(8000);
            assertTrue(
                    "Wi-Fi is in not enabled which is wrong ",
                    (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED));
            Log.v(TAG, "WiFi is enabled state " + mWifiManager.getWifiState());
        }
    }

    public void Disable_Wifi() throws Exception {
        assertTrue(
                "Wi-Fi is in an unknown state. This state will occur when an error happens while enabling or disabling ",
                (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_UNKNOWN));
        if ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                || (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING)) {
            Log.v(TAG, "WiFi is enabled state " + mWifiManager.getWifiState());
        } else {
            Thread.sleep(2000);
            mWifiManager.setWifiEnabled(false);
            Thread.sleep(5000);
            assertTrue(
                    "Wi-Fi is in not enabled which is wrong ",
                    (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED));
            Log.v(TAG, "WiFi is enabled state " + mWifiManager.getWifiState());
        }
    }

    private void handleScanResultsAvailable() {
        List<ScanResult> list = mWifiManager.getScanResults();
        Log.v(TAG, "Total number of Wi-Fi found by scan is " + list.size());
        assertTrue("Wi-Fi SCAN action returned no result which is wrong ",
                (list.size() > 0));

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ScanResult scanResult = list.get(i);
                Log.v(TAG, "Wi-Fi Number = " + i + " with network name SSID = "
                        + scanResult.SSID + " == " + scanResult.capabilities);
            }
        }
    }

    private void handleConfiguredNetworksAvailable() {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "Total number of configured Wi-Fi is == " + list.size());
        assertTrue("Wi-Fi SCAN action returned no result which is wrong ",
                (list.size() > 0));

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Log.v(TAG, "Wi-Fi number = " + i + " with network name SSID = "
                        + list.get(i).SSID + " netId = "
                        + list.get(i).networkId);
            }
        }
    }

    private void clean_ConfiguredNetworks_IF_Available() throws Exception {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "Total number of configured Wi-Fi is " + list.size());

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Log.v(TAG, "Wi-Fi number " + i + " with network name SSID = "
                        + list.get(i).SSID + " netId = "
                        + list.get(i).networkId);
                // Remove the specified network from the list of configured
                // networks
                mWifiManager.disableNetwork(list.get(i).networkId);
                boolean mAvailable = mWifiManager
                        .removeNetwork(list.get(i).networkId);
                Thread.sleep(3000);
                mWifiManager.saveConfiguration();
                Log.v(TAG, " " + list.get(i).SSID + " -> Wifi was deleted == "
                        + mAvailable);
                Thread.sleep(3000);
            }
        }
    }

    private void httpClientTest() {
        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                mHttpClientTestResult = "Pass";
            } else {
                mHttpClientTestResult = "Fail: Code: "
                        + String.valueOf(response);
            }
            request.abort();
        } catch (IOException e) {
            mHttpClientTestResult = "Fail: IOException";
        }
    }

    private void verify_Scan_Results(String WIFI_AP_NAME) throws Exception {
        // Verify that WIFI_AP_NAME is found by scan action - other way throw
        // error - as this is going to be the open wifi to whom we try to
        // connect
        List<ScanResult> list1 = mWifiManager.getScanResults();
        Log.v(TAG, "Total number of Wi-Fi found by scan is " + list1.size());
        assertTrue("Wi-Fi SCAN action returned no result which is wrong ",
                (list1.size() > 0));
        // Thread.sleep(10000);

        String ssid = "";
        if (list1 != null) {
            boolean Found = false;
            for (int i = 0; i < list1.size(); i++) {
                ScanResult scanResult = list1.get(i);
                Log.v(TAG, "Wi-Fi number = " + i + " with network name SSID = "
                        + scanResult.SSID);
                String comp = scanResult.SSID;
                if (comp.equals(WIFI_AP_NAME)) {
                    Found = true;
                    Log.v(TAG, "Found = " + Found);
                    ssid = scanResult.SSID;
                    Log.d(TAG, "Wi-Fi configuration: "
                            + list1.get(i).toString());
                }
            }
            Log.v(TAG, "Found = " + Found);
            assertTrue(
                    "WIFI_AP_NAME was not found by SCAN which means we will not be able to connect to it, which is wrong == ",
                    Found);
        }
    }

    private void add_Network(String ssid) throws Exception {
        // ADD Network WIFI_APP_Name to the configured AP
        boolean mAvailable1 = false;
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(ssid).concat("\"");
        wfc.priority = 1;
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wfc.status = WifiConfiguration.Status.ENABLED;

        Log.d(TAG, "New Wi-Fi Connection " + wfc.toString());

        WifiManager wfMgr = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        int networkId = wfMgr.addNetwork(wfc);
        Log.d(TAG, "NetworkId= " + networkId);
        wfMgr.enableNetwork(networkId, true);
        wfMgr.reconnect();
        Thread.sleep(10000);
        wfMgr.saveConfiguration();
    }

    // The API equivalent. -- doesn't work
    private void setStatic(String ssid) throws Exception {
        Log.d(TAG, "Changing to static ip: " + newIP);
        WifiManager wfMgr = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        Thread.sleep(2000);
        ContentResolver cr = mActivity.getContentResolver();
        mSystem.putInt(cr, mSystem.WIFI_USE_STATIC_IP, 1);
        mSystem.putString(cr, mSystem.WIFI_STATIC_IP, newIP);
    }

    private void check_Network(String WIFI_AP_NAME) throws Exception {
        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        Log.v(TAG, "Configured Wi-Fi Number == " + list.size() + " SSID== "
                + list.get(0).SSID);
        assertTrue(
                "Numer of configured Wi-Fi is not 1 and  SSID is not equal with WIFI name added ",
                ((list.size() == 1) && (list.get(0).SSID.indexOf(WIFI_AP_NAME) > 0)));

        // Because this is the only configured network, device should
        // automaticaly connect to it
        assertTrue("WIFI_AP_NAME was not associated successfuly",
                mWifiManager.enableNetwork(list.get(0).networkId, true));
        assertTrue("WIFI_AP_NAME was not associated successfuly",
                mWifiManager.reconnect());

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

    }

    public void test_Activate_Wifi_With_Airplane_ON() throws Exception {
        Log.v(TAG,
                "Instrumentation test started test_Activate_Wifi_With_Airplane_ON");
        mActivity = getActivity();
        mContext = mInst.getTargetContext();

        // verify airplane mode is off - if it is not then make it off
        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        if (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true) {

            // make airplane mode off
            mSystem.putString(mContext.getContentResolver(),
                    mSystem.AIRPLANE_MODE_ON, "true");
            Thread.sleep(2000);

            // Post the intent
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", false);
            mContext.sendBroadcast(intent);
            Thread.sleep(2000);
            Log.v(TAG,
                    "AirplaneMode is "
                            + AirplaneModeEnabler.isAirplaneModeOn(mContext));
            assertTrue("AirplaneMode is not OFF which is wrong is ",
                    (AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));
        }

        // enable wifi
        Enable_Wifi();

        // make airplane mode on
        System.putString(mContext.getContentResolver(),
                mSystem.AIRPLANE_MODE_ON, "1");

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        mContext.sendBroadcast(intent);
        Thread.sleep(2000);
        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        assertTrue("AirplaneMode is not ON which is wrong ",
                (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true));

        // check wifi was turned off
        checkWifiOff();

        Log.v(TAG, "WiFi is DISABLED state " + mWifiManager.getWifiState());
        assertTrue(
                "Wi-Fi is in not disabled which is wrong ",
                (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED));

        // make wifi on
        Enable_Wifi();

        Log.v(TAG,
                "Instrumentation test stopped test_Activate_Wifi_With_Airplane_ON");
    }

    public void test_Airplane_Mode_OFF() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Airplane_Mode_OFF");
        mActivity = getActivity();

        // verify airplane mode is off - if it is not then make it off
        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        if (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true) {
            // make airplane mode off
            mSystem.putString(mContext.getContentResolver(),
                    mSystem.AIRPLANE_MODE_ON, "true");
            Thread.sleep(2000);

            // Post the intent
            mIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mIntent.putExtra("state", false);
            mContext.sendBroadcast(mIntent);
            Thread.sleep(2000);

            // check Airplane mode is OFF
            Log.v(TAG,
                    "AirplaneMode is "
                            + AirplaneModeEnabler.isAirplaneModeOn(mContext));
            assertTrue("AirplaneMode is not OFF which is wrong is ",
                    (AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));
        }

        // enable wifi
        Enable_Wifi();

        // make airplane mode on
        System.putString(mContext.getContentResolver(),
                mSystem.AIRPLANE_MODE_ON, "1");
        Thread.sleep(2000);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        mContext.sendBroadcast(intent);
        Thread.sleep(2000);

        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        assertTrue("AirplaneMode is not ON which is wrong is ",
                (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true));

        // check wifi was turned off
        checkWifiOff();

        Log.v(TAG, "WiFi is DISABLED state " + mWifiManager.getWifiState());
        assertTrue(
                "Wi-Fi is in not disabled which is wrong ",
                (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED));

        // make airplane mode off
        mSystem.putString(mContext.getContentResolver(),
                mSystem.AIRPLANE_MODE_ON, "true");
        Thread.sleep(2000);

        // Post the intent
        mIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        mIntent.putExtra("state", false);
        mContext.sendBroadcast(mIntent);
        Thread.sleep(2000);

        // check Airplane mode is OFF
        Log.v(TAG,
                "AirplaneMode "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        assertTrue("AirplaneMode is not OFF which is wrong ",
                (AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));

        // check wifi was turned on automaticaly
        int tries = 1;
        while (((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) || (mWifiManager
                .getWifiState() != WifiManager.WIFI_STATE_ENABLED))
                && (tries < 10)) {
            Log.v(TAG, "waiting for WiFi to be ENABLED ");
            tries++;
            Thread.sleep(1000);
        }

        Log.v(TAG, "WiFi is ENABLED state = " + mWifiManager.getWifiState());
        assertTrue("Wi-Fi is in not ENABLED which is wrong ",
                (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED));

        Log.v(TAG, "Instrumentation test stopped test_Airplane_Mode_OFF");
    }

    public void test_Airplane_Mode_ON() throws Exception {
        Log.v(TAG, "Instrumentation test start test_Airplane_Mode_ON");
        mActivity = getActivity();
        Thread.sleep(2000);

        // verify airplane mode is off - if it is not then make it off
        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        if (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true) {
            // make airplane mode off
            mSystem.putString(mContext.getContentResolver(),
                    mSystem.AIRPLANE_MODE_ON, "true");
            Thread.sleep(2000);

            // Post the intent
            mIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mIntent.putExtra("state", false);
            mContext.sendBroadcast(mIntent);
            Thread.sleep(2000);

            // check Airplane mode is OFF
            Log.v(TAG,
                    "AirplaneMode is "
                            + AirplaneModeEnabler.isAirplaneModeOn(mContext));
            assertTrue("AirplaneMode is not OFF which is wrong ",
                    (AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));
        }

        // enable wifi
        Enable_Wifi();

        // make airplane mode on
        System.putString(mContext.getContentResolver(),
                mSystem.AIRPLANE_MODE_ON, "1");
        Thread.sleep(2000);

        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        mContext.sendBroadcast(intent);
        Thread.sleep(2000);
        Log.v(TAG,
                "AirplaneMode is "
                        + AirplaneModeEnabler.isAirplaneModeOn(mContext));
        assertTrue("AirplaneMode is not ON which is wrong ",
                (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true));

        // check wifi was turned off
        checkWifiOff();

        Log.v(TAG, "WiFi is DISABLED state = " + mWifiManager.getWifiState());
        assertTrue(
                "Wi-Fi is in not disabled which is wrong ",
                (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED));

        Log.v(TAG, "Instrumentation test stopped test_Airplane_Mode_ON");
    }

    public void test_Static_IP_Allocation() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Static_IP_Allocation");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(2000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        // keep IP for later use
        WifiInfo mWifiInfo = null;
        mContext = mInst.getTargetContext();
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        String OldIP = formIp.formatIpAddress(mWifiInfo.getIpAddress());
        Log.v(TAG, "OldIP= " + OldIP);

        // use the ip given by the dhcp
        newIP = OldIP;
        Log.v(TAG, "oldIP= " + OldIP + " newIP = " + newIP);

        // change to static ip
        // setStatic(WIFI_AP_NAME);

        // access Modify Network menu
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(5000);

        // select Show advanced opions
        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER));
        Thread.sleep(ViewConfiguration.get(mContext).getLongPressTimeout());
        // mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP,
        // KeyEvent.KEYCODE_DPAD_CENTER));
        Thread.sleep(3000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(5000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER); // IP setting = Static
        // was selected
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(2000);

        // delete old IP
        for (int i = 1; i <= OldIP.length(); i++) {
            mInst.sendCharacterSync(KeyEvent.KEYCODE_FORWARD_DEL);
        }
        Thread.sleep(3000);
        // set the new IP
        mInst.sendStringSync(newIP);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(8000);
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(false);
        Thread.sleep(5000);
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);
        Thread.sleep(20000);

        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(false);
        Thread.sleep(5000);
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);
        Thread.sleep(20000);

        // Read new Ip from Wifi Configuration to see that it was set
        mWifiInfo = null;
        mContext = mInst.getTargetContext();
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        String ReadIP = formIp.formatIpAddress(mWifiInfo.getIpAddress());
        Log.v(TAG, "ReadIP= " + ReadIP + "  NewIP= " + newIP);
        assertEquals("New IP was not commited to wifi Configs", ReadIP, newIP);

        // try http to www.intel.com to see if wifi got connected - try 4 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        // reset configuration
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(5000);

        // select Show advanced opions
        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER));
        Thread.sleep(ViewConfiguration.get(mContext).getLongPressTimeout());
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(5000);

        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(8000);

        Log.v(TAG, "Instrumentation test stoped test_Static_IP_Allocation");
    }

    public void test_Switch_To_Other_AP() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Switch_To_Other_AP");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        // ADD Network WIFI_AP_NAME2 to the configured AP
        add_Network(WIFI_AP_NAME2);

        // Check that WIFI_AP_NAME2 was added to configured networks - throw
        // error if not
        // check_Network(WIFI_AP_NAME2);

        // disconnect WIFI_AP_NAME

        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER));
        Thread.sleep(ViewConfiguration.get(mContext).getLongPressTimeout());
        Thread.sleep(2000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(5000);

        // try http to www.intel.com to see if wifi WIFI_AP_NAME2 got connected
        // - try 3 times with 10s delay
        httpClientTest();
        iii = 1;
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        clean_ConfiguredNetworks_IF_Available();

        Log.v(TAG, "Instrumentation test stopped test_Switch_To_Other_AP");
    }

    public void test_Remember_Wireless_Networks() throws Exception {
        Log.v(TAG,
                "Instrumentation test started test_Remember_Wireless_Networks");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        WifiInfo mWifiInfo = null;
        mWifiInfo = mWifiManager.getConnectionInfo();

        int jk = 1;
        while (mWifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState())
                .toString() != "OBTAINING_IPADDR") {
            if (jk == HTTP_Repeat) {
                break;
            }
            mWifiInfo = mWifiManager.getConnectionInfo();
            Thread.sleep(1000);
            Log.v(TAG,
                    "wait until ip is retrieved== "
                            + mWifiInfo.getDetailedStateOf(mWifiInfo
                                    .getSupplicantState()) + " jk= " + jk);
            jk++;

        }

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }
        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        // verify if we have DHCP info - which means that wifi was connected
        String dhcp = "";
        dhcp = mWifiManager.getDhcpInfo().toString();
        Log.v(TAG,
                "Wifi Info IP= "
                        + formIp.formatIpAddress(mWifiInfo.getIpAddress()));
        Log.v(TAG, "Wifi aswdasda= " + dhcp);
        assertTrue("dhcp is empty space", (dhcp.length() > 1));
        String[] spliterr;
        spliterr = dhcp.split(" ");
        Log.v(TAG, "ip = " + spliterr[1]);
        assertTrue("ip is empty space", (spliterr[1].length() > 1));

        // disable/enable wifi
        Disable_Wifi();
        Thread.sleep(1000);
        Enable_Wifi();

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        int ii = 1;
        httpClientTest();
        while (ii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " ii= " + ii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            ii++;
            httpClientTest();

        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        Log.v(TAG,
                "Instrumentation test stopped test_Remember_Wireless_Networks");
    }

    public void test_Check_Network_Information() throws Exception {
        Log.v(TAG,
                "Instrumentation test started test_Check_Network_Information");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }
        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        // verify if we have DHCP info - which means that test passed
        String dhcp = "";
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        dhcp = mWifiManager.getDhcpInfo().toString();
        Log.v(TAG,
                "Wifi Info IP= "
                        + formIp.formatIpAddress(mWifiInfo.getIpAddress()));
        Log.v(TAG, "Wifi aswdasda= " + dhcp);
        assertTrue("dhcp is empty space", (dhcp.length() > 1));
        String[] spliterr;
        spliterr = dhcp.split(" ");
        Log.v(TAG, "ip = " + spliterr[1]);
        assertTrue("ip is empty space", (spliterr[1].length() > 1));

        // as desired wifi was connected - it is now the first one in the list
        // so we can press it
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(1000);
        mInst.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        Thread.sleep(2000);

        Log.v(TAG,
                "Instrumentation test stopped test_Check_Network_Information");
    }

    public void test_Connection_To_Hidden_Network() throws Exception {
        Log.v(TAG,
                "Instrumentation test started test_Connection_To_Hidden_Network");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        WifiInfo mWifiInfo = null;
        mWifiInfo = mWifiManager.getConnectionInfo();

        int jk = 1;
        while (mWifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState())
                .toString() != "OBTAINING_IPADDR") {
            if (jk == HTTP_Repeat) {
                break;
            }
            mWifiInfo = mWifiManager.getConnectionInfo();
            Thread.sleep(1000);
            Log.v(TAG,
                    "wait until ip is retrieved== "
                            + mWifiInfo.getDetailedStateOf(mWifiInfo
                                    .getSupplicantState()) + " jk= " + jk);
            jk++;

        }

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        Log.v(TAG,
                "Instrumentation test stopped test_Connection_To_Hidden_Network");
    }

    public void test_Basic_Network_Configuration() throws Exception {
        Log.v(TAG,
                "Instrumentation test start test_Basic_Network_Configuration");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);

        WifiInfo mWifiInfo = null;
        mWifiInfo = mWifiManager.getConnectionInfo();

        long ipSearchStarted1 = mJSystem.currentTimeMillis();
        int jk = 1;
        while (mWifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState())
                .toString() != "OBTAINING_IPADDR") {
            if (jk == HTTP_Repeat) {
                break;
            }
            mWifiInfo = mWifiManager.getConnectionInfo();
            Thread.sleep(1000);
            Log.v(TAG,
                    "wait until ip is retrieved== "
                            + mWifiInfo.getDetailedStateOf(mWifiInfo
                                    .getSupplicantState()) + " jk= " + jk);
            jk++;

        }

        // try http to www.intel.com to see if wifi got connected - try 3 times
        // with 10s delay
        httpClientTest();
        int iii = 1;
        String gog = "Pass";
        while (iii != 7) {
            Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult
                    + " iii= " + iii);
            if (mHttpClientTestResult == gog) {
                break;
            }
            Thread.sleep(10000);
            iii++;
            httpClientTest();
        }

        Log.v(TAG, "mHttpClientTestResult= " + mHttpClientTestResult);
        assertEquals("HTTP test failed which is wrong", "Pass",
                mHttpClientTestResult);

        Log.v(TAG,
                "Instrumentation test stopped test_Basic_Network_Configuration");
    }

    public void test_Verify_ADD_Network_Works() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Verify_ADD_Network_Works");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        // ADD Network WIFI_APP_Name to the configured AP
        add_Network(WIFI_AP_NAME);

        // Check that WIFI_AP_NAME was added to configured networks - throw
        // error if not
        check_Network(WIFI_AP_NAME);
        Log.v(TAG, "Instrumentation test stopped test_Verify_ADD_Network_Works");
    }

    public void test_Verify_SCAN_Works() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Verify_SCAN_Works");
        mActivity = getActivity();

        // enable wifi before scan
        Enable_Wifi();
        boolean mAvailable = mWifiManager.startScan();
        assertTrue("Wifi SCAN menu was not triggered", mAvailable);

        // wait until wifi SCAN action ends
        Thread.sleep(20000);

        // Clean all configured networks
        clean_ConfiguredNetworks_IF_Available();

        // Verify in WIFI_AP_NAME configuration is in scan results
        verify_Scan_Results(WIFI_AP_NAME);

        handleScanResultsAvailable();

        Log.v(TAG, "Instrumentation test stopped test_Verify_SCAN_Works");
    }

    public void test_Enable_Wifi() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Enable_Wifi");

        mActivity = getActivity();
        Thread.sleep(3000);
        Enable_Wifi();

        Log.v(TAG, "Instrumentation test stopped test_Enable_Wifi");
    }

    public void test_Disable_Wifi() throws Exception {
        Log.v(TAG, "Instrumentation test started test_Disable_Wifi");

        mActivity = getActivity();
        Thread.sleep(3000);
        Disable_Wifi();

        Log.v(TAG, "Instrumentation test stopped test_Disable_Wifi");
    }

    public void test_Stress_Enable_Disable_Wifi() throws Exception {
        Log.v(TAG,
                "Instrumentation test started test_Stress_Enable_Disable_Wifi");
        mActivity = getActivity();

        for (int i = 1; i <= 50; i++) {
            Disable_Wifi();
            Thread.sleep(1000);
            Enable_Wifi();
        }

        Log.v(TAG,
                "Instrumentation test stopped test_Stress_Enable_Disable_Wifi");
    }

}
