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

import com.android.settings.BReceiver;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.app.Instrumentation;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.view.KeyEvent;
import android.app.Activity;
import android.view.Menu;
import android.provider.Settings.System;

/**
 * Tests for the Settings operator/manufacturer hook.
 * 
 * Running all tests:
 * 
 * make SettingsTests adb push SettingsTests.apk /system/app/SettingsTests.apk
 * adb shell am instrument \ -w
 * com.android.settings.tests/android.test.InstrumentationTestRunner
 */
public class Bluetooth_Settings_Tests
		extends
			ActivityInstrumentationTestCase2<Settings> {

	private static final String TAG = "Bluetooth_Settings_Tests AOSP_Tests";

	private BluetoothAdapter mBluetoothAdapter = null;
	private BReceiver mBReceiver = null;
	private AirplaneModeEnabler mAirplaneModeEnabler = null;
	private Instrumentation mInstrumentation = null;
	private System mSystem = null;
	private Settings mSettings = null;
	private Activity mBluetooth = null;
	private Context mContext = null;

	String address, name;
	public String verdict = ":failed:";
	private boolean mHasBluetooth = false;

	private static final int ENABLE_TIMEOUT = 10000; // ms timeout for BT enable
	private static final int POLL_TIME = 1500; // ms to poll BT state

	public Bluetooth_Settings_Tests() {
		super("com.android.settings", Settings.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mInstrumentation = getInstrumentation();
		mSettings = getActivity();

	}

	public void enableBT() throws Exception {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ((BluetoothAdapter.STATE_OFF == mBluetoothAdapter.getState())) {
			Log.v(TAG, "Enabling mBluetoothAdapter ");
			mBluetoothAdapter.enable();
			for (int i = 0; i < ENABLE_TIMEOUT / POLL_TIME; i++) {
				if (mBluetoothAdapter.isEnabled()) {
					address = mBluetoothAdapter.getAddress();
					name = mBluetoothAdapter.getName();
					Log.v(TAG, "Bluetooth preferences: " + address + " " + name);
					break;
				}
				try {
					Thread.sleep(POLL_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void disableBT() throws Exception {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if ((BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState())) {
			Log.v(TAG, "Disabling mBluetoothAdapter ");
			mBluetoothAdapter.disable();
			for (int i = 0; i < ENABLE_TIMEOUT / POLL_TIME; i++) {
				if (!mBluetoothAdapter.isEnabled()) {
					address = mBluetoothAdapter.getAddress();
					name = mBluetoothAdapter.getName();
					Log.v(TAG, "Bluetooth preferences: " + address + " " + name);
					break;
				}
				try {
					Thread.sleep(POLL_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void startBluetoothTab() throws InterruptedException {
		Intent settingsIntent = new Intent(
				android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mBluetooth = null;
		mBluetooth = mInstrumentation.startActivitySync(settingsIntent);
		assertTrue("Intent to open Bluetooth tab failed ", (mBluetooth != null));
		Thread.sleep(5000);
	}

	public void makeAirplaneOff() throws Exception {
		// verify airplane mode is off - if it is not then make it off
		Log.v(TAG,
				"AirplaneMode is "
						+ AirplaneModeEnabler.isAirplaneModeOn(mContext));
		if (AirplaneModeEnabler.isAirplaneModeOn(mContext) == true) {
			// make airplane mode off
			mSystem.putString(mContext.getContentResolver(),
					mSystem.AIRPLANE_MODE_ON, "true");
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", true);
			mContext.sendBroadcast(intent);
			Thread.sleep(2000);
			Log.v(TAG,
					"AirplaneMode is "
							+ AirplaneModeEnabler.isAirplaneModeOn(mContext));
			assertTrue(
					"AirplaneMode mode was not switched OFF after 2 seconds. Aborting tests. ",
					(AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));
		}

		// enable BT
		enableBT();
		Thread.sleep(4000);
		int state1 = mBluetoothAdapter.getState();
		assertTrue(
				"BlueTooth is not enabled after 4 tries which is wrong. Aborting tests. ",
				(BluetoothAdapter.STATE_ON == state1));

		// make airplane mode on
		mSystem.putString(mContext.getContentResolver(),
				mSystem.AIRPLANE_MODE_ON, "1");
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		mContext.sendBroadcast(intent);
		Thread.sleep(2000);
		Log.v(TAG,
				"AirplaneMode is "
						+ AirplaneModeEnabler.isAirplaneModeOn(mContext));
		assertTrue(
				"AirplaneMode mode was not switched ON after 2 seconds. Aborting tests.",
				(AirplaneModeEnabler.isAirplaneModeOn(mContext) == true));

		// check BT was turned off
		int i = 0;
		while ((mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF)
				&& (i != 4)) {
			i++;
			Log.v(TAG, mBluetoothAdapter.getState() + " i is " + i);
			Thread.sleep(4000);
		}
		assertEquals(
				"BlueTooth is not disabled after 4 tries which is wrong. Aborting tests.",
				BluetoothAdapter.STATE_OFF, mBluetoothAdapter.getState());
	}

	public void test_Activate_BT_With_Airplane_ON() throws Exception {
		Log.v(TAG,
				"Instrumentation test started test_Activate_BT_With_Airplane_ON");

		mSettings = getActivity();
		mContext = mInstrumentation.getTargetContext();
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
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", false);
			mContext.sendBroadcast(intent);

			Thread.sleep(2000);
			Log.v(TAG,
					"AirplaneMode is "
							+ AirplaneModeEnabler.isAirplaneModeOn(mContext));
			assertTrue(
					"AirplaneMode mode was not switched OFF after 2 seconds. Aborting tests. ",
					(AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));
		}

		// enable BT
		enableBT();
		Thread.sleep(4000);
		int state1 = mBluetoothAdapter.getState();
		assertTrue(
				"Bluetooth was not switched ON after 4 seconds. Aborting tests. ",
				(BluetoothAdapter.STATE_ON == state1));

		// make airplane mode on
		mSystem.putString(mContext.getContentResolver(),
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
		assertTrue(
				"AirplaneMode mode was not switched ON after 2 seconds. Aborting tests. ",
				(AirplaneModeEnabler.isAirplaneModeOn(mContext) == true));

		// check BlueTooth was turned off
		int i = 0;
		while ((mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF)
				&& (i != 4)) {
			i++;
			Log.v(TAG, "i is " + i);
			Thread.sleep(4000);
		}
		assertEquals(
				"BlueTooth is not disabled after 4 tries which is wrong. Aborting tests. ",
				BluetoothAdapter.STATE_OFF, mBluetoothAdapter.getState());

		// enable BT
		enableBT();
		i = 0;
		while ((mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON)
				&& (i != 4)) {
			i++;
			Log.v(TAG, "i is " + i);
			Thread.sleep(4000);
		}

		// check BT was enabled
		assertEquals(
				"BlueTooth is not enabled after 4 tries which is wrong. Aborting tests. ",
				BluetoothAdapter.STATE_ON, mBluetoothAdapter.getState());
		Log.v(TAG,
				"Instrumentation test stopped test_Activate_BT_With_Airplane_ON");
	}

	public void test_Airplane_Mode_OFF() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Airplane_Mode_OFF");

		mSettings = getActivity();
		Thread.sleep(2000);
		mContext = mInstrumentation.getTargetContext();

		makeAirplaneOff();

		// make airplane mode off
		mSystem.putString(mContext.getContentResolver(),
				mSystem.AIRPLANE_MODE_ON, "true");
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		mContext.sendBroadcast(intent);
		Thread.sleep(2000);
		Log.v(TAG,
				"AirplaneMode is "
						+ AirplaneModeEnabler.isAirplaneModeOn(mContext));
		assertTrue(
				"AirplaneMode mode was not switched OFF after 2 seconds. Aborting tests. ",
				(AirplaneModeEnabler.isAirplaneModeOn(mContext) == false));

		// check BT was turned on
		int i = 0;
		while ((mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON)
				&& (i != 4)) {
			i++;
			Log.v(TAG, mBluetoothAdapter.getState() + " i is " + i);
			Thread.sleep(4000);
		}
		assertEquals(
				"BlueTooth is not enabled after 4 tries which is wrong. Aborting tests.",
				BluetoothAdapter.STATE_ON, mBluetoothAdapter.getState());

		Log.v(TAG, "Instrumentation test stopped test_Airplane_Mode_OFF");
	}

	public void test_Airplane_Mode_ON() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Airplane_Mode_ON");

		mSettings = getActivity();
		Thread.sleep(2000);
		mContext = mInstrumentation.getTargetContext();

		makeAirplaneOff();

		Log.v(TAG, "Instrumentation test stopped test_Airplane_Mode_ON");
	}

	public void test_Stress_Enable_Disable_BT() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Stress_Enable_Disable_BT");
		Log.v(TAG,
				"This test will enable/disable BT device 50 times. Will faill if one of the opperations fails");

		for (int i = 0; i <= 50; i++) {
			enableBT();
			Thread.sleep(2000);
			int state1 = mBluetoothAdapter.getState();
			assertTrue(
					"BlueTooth is not enabled after 2 seconds is wrong. Aborting tests. ",
					(BluetoothAdapter.STATE_ON == state1));

			Log.v(TAG, "Iteration i " + i + " BlueTooth enable state is "
					+ state1);
			assertTrue("Bluetooth is disabled. Aborting tests. ",
					mBluetoothAdapter.disable());
			int state2 = mBluetoothAdapter.getState();
			while ((state2 == BluetoothAdapter.STATE_TURNING_OFF)
					|| (state2 == state1)) {
				Log.v(TAG, "Waiting for BlueTooth to be disabled. ");
				Thread.sleep(3000);
				state2 = mBluetoothAdapter.getState();
			}
			Log.v(TAG, "Iteration i" + i + " BlueTooth disable state is "
					+ state2);
			assertTrue(
					"BlueTooth is not disabled which is wrong. Aborting tests. ",
					((state1 != state2) && (BluetoothAdapter.STATE_OFF == state2)));
		}

		Log.v(TAG, "Instrumentation test stopped test_Stress_Enable_Disable_BT");
	}

	public void test_Enable_Disable_BT() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Enable_Disable_BT");
		Instrumentation mInstrumentation = getInstrumentation();
		// enable BT
		enableBT();

		Thread.sleep(4000);
		int state1 = mBluetoothAdapter.getState();
		Log.v(TAG, "State should be on " + state1);

		assertEquals(
				"BlueTooth is not enabled which is wrong. Aborting tests. ",
				BluetoothAdapter.STATE_ON, state1);

		// disable BT
		disableBT();
		Thread.sleep(4000);
		int state2 = mBluetoothAdapter.getState();
		Log.v(TAG, "State should be on " + state2);

		assertEquals(
				"BlueTooth is not disabled which is wrong. Aborting tests. ",
				BluetoothAdapter.STATE_OFF, state2);

		// enable BT
		enableBT();

		Thread.sleep(4000);
		int state3 = mBluetoothAdapter.getState();
		Log.v(TAG, "State should be on " + state3);

		assertEquals(
				"BlueTooth is not enabled which is wrong. Aborting tests. ",
				BluetoothAdapter.STATE_ON, state3);

		Log.v(TAG, "Instrumentation test stopped test_Enable_Disable_BT");
	}

	public void test_Search_For_Devices() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Search_For_Devices");

		Instrumentation mInstrumentation = getInstrumentation();
		mContext = mInstrumentation.getTargetContext();
		mSettings = getActivity();

		Thread.sleep(1000);
		// start the activity in the bluetooth tab
		startBluetoothTab();

		// enable BT if it is not so we clan search for devices
		enableBT();

		Thread.sleep(2000);
		assertTrue(
				"BlueTooth is not enabled after 2 seconds is wrong. Aborting tests. ",
				(BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()));

		// make airplanemode off
		mSystem.putString(mContext.getContentResolver(),
				mSystem.AIRPLANE_MODE_ON, "true");
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		mContext.sendBroadcast(intent);
		Thread.sleep(2000);

		boolean isDiscovering = mBluetoothAdapter.isDiscovering();
		assertTrue(
				"Initial scan when bluetooth tab is seleceted was not triggered",
				isDiscovering);
		while (isDiscovering) {
			Log.v(TAG, "Discovering...");
			Thread.sleep(1000);
			isDiscovering = mBluetoothAdapter.isDiscovering();
		}

		// register Receivers
		mBReceiver = new BReceiver();

		// list devices
		mBReceiver.Bt_list_Scan_Devices(mContext);

		// scan
		boolean mVisibility = mInstrumentation.invokeMenuActionSync(mBluetooth,
				Menu.FIRST, 0);
		Log.v(TAG, "Start scan " + mVisibility);
		assertTrue("Scan was not triggered", mVisibility);

		Thread.sleep(3000);
		assertTrue("Start discovery proccess was not successful ",
				mBluetoothAdapter.startDiscovery());
		Thread.sleep(2000);

		isDiscovering = mBluetoothAdapter.isDiscovering();
		Thread.sleep(2000);
		// check BT searches for devices
		Log.v(TAG, "isDiscovering state " + isDiscovering);
		assertTrue("Bluetooth state is not Discovering", isDiscovering);

		// check that BT scan found > 0 devices
		Log.v(TAG, "mBReceiver.NumberOfBT " + mBReceiver.NumberOfBT);
		assertTrue(
				"Number of devices found by BlueTooth scan is not > 0  which is wrong",
				(mBReceiver.NumberOfBT > 0));
		Thread.sleep(8000);
		mSettings.finish();
		mBluetooth.finish();
		Log.v(TAG, "Instrumentation test stopped test_Search_For_Devices");

	}

	public void test_Rename_Tablet() throws Exception {
		Log.v(TAG, "Instrumentation test started test_Rename_Tablet");

		Instrumentation mInstrumentation = getInstrumentation();
		mContext = mInstrumentation.getTargetContext();
		mSettings = getActivity();

		// start the activity in the bluetooth tab
		startBluetoothTab();
		// enable BT if it is not so we clan search for devices
		enableBT();
		Thread.sleep(2000);

		assertTrue("BlueTooth is not enabled which is wrong",
				(BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()));

		String mOldName = mBluetoothAdapter.getName();
		Log.v(TAG, "Current Name " + mOldName);

		int textId = Menu.FIRST + 1;
		Log.v(TAG, "textId is " + textId);
		boolean mChanged = mInstrumentation.invokeMenuActionSync(mBluetooth,
				textId, 0);
		Thread.sleep(2000);
		Log.v(TAG, "mChanged is " + mChanged);
		assertTrue("Rename tablet menu was not triggered", mChanged);
		mInstrumentation.sendStringSync("TabletName");
		Thread.sleep(1000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
		Thread.sleep(1000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
		Thread.sleep(1000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
		Thread.sleep(1000);

		String mNewName = mBluetoothAdapter.getName();
		Log.v(TAG, "TabletName" + mOldName + " " + mNewName);
		assertTrue("Bluetooth names was not changed which is wrong",
				mNewName.equals("TabletName" + mOldName));

		mSettings.finish();
		mBluetooth.finish();
		Log.v(TAG, "Instrumentation test stopped test_Rename_Tablet");
	}

	public void test_Make_BT_Visible_To_other_Devices() throws Exception {
		Log.v(TAG,
				"Instrumentation test started test_Make_BT_Visible_To_other_Devices");

		Instrumentation mInstrumentation = getInstrumentation();
		mSettings = getActivity();
		mContext = mInstrumentation.getTargetContext();

		// start the activity in the bluetooth tab
		startBluetoothTab();

		// enable BT if it is not so we can make it visible tablet
		enableBT();
		Thread.sleep(1000);
		assertTrue("BlueTooth is not enabled which is wrong",
				(BluetoothAdapter.STATE_ON == mBluetoothAdapter.getState()));

		int mOldName = mBluetoothAdapter.getScanMode();

		while (mOldName == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Log.v(TAG, "BlueTooth is already discoverable ");
			Thread.sleep(10000);
			mOldName = mBluetoothAdapter.getScanMode();
		}
		Log.v(TAG, "mOldName is " + mOldName);

		int textId = Menu.FIRST + 2;
		Log.v(TAG, "textId " + textId);
		boolean mVisibility = mInstrumentation.invokeMenuActionSync(mBluetooth,
				textId, 0);
		assertTrue("Visibility timeout menu was not triggered", mVisibility);
		Thread.sleep(2000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
		Thread.sleep(2000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
		Thread.sleep(5000);

		int mNewName = mBluetoothAdapter.getScanMode();
		Log.v(TAG, "Bluetooth.getScanMode() " + mNewName);
		assertTrue(
				"Bluetooth visibility was not changed which is wrong",
				((mOldName != mNewName) && (mNewName == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)));

		mSettings.finish();
		mBluetooth.finish();
		Log.v(TAG,
				"Instrumentation test stopped test_Make_BT_Visible_To_other_Devices");
	}

	public void testBTMakeDiscoverable() throws Exception {
		Log.v(TAG, "Instrumentation test started testBTMakeDiscoverabled ");

		Instrumentation mInstrumentation = getInstrumentation();
		mSettings = getActivity();

		enableBT();
		KeyEvent focusEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_ENTER);
		Intent discoverableIntent = new Intent();

		discoverableIntent
				.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 100);

		discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Activity act = mInstrumentation.startActivitySync(discoverableIntent);

		mInstrumentation.sendKeySync(focusEvent);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_DPAD_RIGHT);
		Thread.sleep(1000);
		mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
		act.finish();

		try {
			Thread.sleep(POLL_TIME);
		}

		catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.v(TAG,
				"BAT_SettingsBTdiscoverable " + mBluetoothAdapter.getScanMode());

		if (BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE == mBluetoothAdapter
				.getScanMode()) {
			verdict = ":passed:";
			Log.v(TAG, "BAT_SettingsBTdiscoverable " + verdict);

		} else {
			verdict = ":failed:";
			Log.v(TAG, "BAT_SettingsBTdiscoverable " + verdict);
			assertEquals("BAT_SettingsBTdiscoverable failed: Verdict =",
					":passed:", verdict);
		}
		Log.v(TAG, "Instrumentation test stopped testBTMakeDiscoverable");
	}
}
