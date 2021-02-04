package com.wso2is.androidsample.activities;

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.browser.customtabs.CustomTabsIntent;

import com.wso2is.androidsample.R;
import com.wso2is.androidsample.mgt.AuthStateManager;
import com.wso2is.androidsample.mgt.ConfigManager;
import com.wso2is.androidsample.oidc.AuthRequest;

import net.openid.appauth.AuthState;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AuthStateManager authStateManager = AuthStateManager.getInstance(this);
        ConfigManager configuration = ConfigManager.getInstance(this);

        // If the user is authorized, the UserActivity view will be launched.
        if (authStateManager.getCurrentState().isAuthorized() && !configuration.hasConfigurationChanged()) {
            Log.i(TAG, "User is already authorized, proceeding to user activity.");
            startActivity(new Intent(this, UserActivity.class));
            finish();
            return;

        } else {
            // If the user is not authorized, the LoginActivity view will be launched.
            setContentView(R.layout.activity_login);

            // Checks if the configuration is valid.
            if (!configuration.isValid()) {
                Log.e(TAG, "Configuration is not valid: " + configuration.getConfigurationError());
                Toast.makeText(this, "Configuration is not valid!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Discards any existing authorization state due to the change of configuration.
                if (configuration.hasConfigurationChanged()) {
                    Log.i(TAG, "Configuration change detected, discarding the old state.");
                    authStateManager.replaceState(new AuthState());
                    // Stores the current configuration as the last known valid configuration.
                    configuration.acceptConfiguration();
                }
            }
            findViewById(R.id.bsignIn).setOnClickListener((View view) -> AuthRequest.getInstance(this).doAuth());
            findViewById(R.id.bsignUp).setOnClickListener((View view) -> {
                startActivity(new Intent(this, SignUpActivity.class));
                finish();
            });


        }

//
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        // Setup the viewPager
//        CustomViewPager viewPager = fis

    }




}
