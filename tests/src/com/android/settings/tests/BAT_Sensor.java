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
import android.util.Log;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.content.Context;

public class BAT_Sensor implements SensorEventListener {

	final String TAG = "BAT_Sensor AOSP_Tests";
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	public float pressure;
	SensorEvent sensorEvent = null;
	public boolean verdict = false;
	public int SensorType1;

	public BAT_Sensor(int SensorType) {
		SensorType1 = SensorType;
	}

	public void instantiateSensors(Context mcontext) throws Exception {
		mSensorManager = (SensorManager) mcontext
				.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(SensorType1);
		enableSensor();

	}

	public void enableSensor() {
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onSensorChanged(SensorEvent sensorEvent) {

		if (sensorEvent.sensor.getType() == SensorType1) {

			float pressure = sensorEvent.values[0];
			if ((pressure < 1500) && (pressure > 600)) {
				Log.i(TAG, "Pressure " + pressure);
				verdict = true;
			} else {
				Log.i(TAG, "Pressure " + pressure);
				verdict = false;

			}
		} else {

			Log.i(TAG, "No pressure sensor");
			verdict = false;

		}
	}
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
