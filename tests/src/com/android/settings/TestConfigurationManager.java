/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.Context;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;
import android.util.Log;

/**
 * class contains ini file management functionality
 */

public class TestConfigurationManager {

    private static final String configFileType = ".wifi.properties";
    private static final String TAG = "TestConfigurationManager";
    private Context mTestContext;

    public TestConfigurationManager(Context testContext) {
        // get an instance of assetManager to access config files from /assets
        // in the project root
        mTestContext = testContext;
    }

    private File findFileContainingString(String searchDir, String filePattern,
            File currentFile) {
        if (currentFile.isFile() && currentFile.getAbsolutePath().contains(searchDir)
                && currentFile.getName().contains(filePattern)) {
            return currentFile;
        } else if (currentFile.isDirectory()) {
            for (File file : currentFile.listFiles()) {
                File result = findFileContainingString(searchDir, filePattern, file);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public Properties getProperties(String dirFileName) {
        // return the properties Object containing all configuration data from a
        // certain file which has a property format (key=value on each line)
        Log.d(TAG, "searching " + dirFileName + " for " + configFileType + " files");
        File configProps = findFileContainingString(dirFileName, configFileType,
                new File(dirFileName));
        try {
            Log.d(TAG, "found " + configProps.getAbsolutePath());
            Properties prop = new Properties();
            prop.load(new FileInputStream(configProps.getAbsolutePath()));
            return prop;
        } catch (IOException e) {
            Log.e(TAG, "could not open the .properties file " + dirFileName);
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "could not find any .properties file in " + dirFileName);
            e.printStackTrace();
        }
        return null;
    }
}