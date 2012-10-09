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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Activity mActivity;
    private static final String TAG = "WiFiDirectBroadcastReceiver AOSP_Tests";
    public static int state;

    /**
     * @param mManager
     *            WifiP2pManager system service
     * @param channel
     *            Wifi p2p channel
     * @param mActivity
     *            mActivity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager,
            Activity mactivity) {
        super();
        this.mManager = mManager;
        this.mActivity = mactivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                Log.v(TAG, "IsWifiP2pEnabled is enabled");
            } else {
                Log.v(TAG, "IsWifiP2pEnabled is disabled");

            }
            Log.d(TAG, "P2P state changed:  " + state);
        }

    }
}
