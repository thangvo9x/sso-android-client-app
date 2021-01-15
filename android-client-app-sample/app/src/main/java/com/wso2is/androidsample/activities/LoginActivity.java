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

package com.wso2is.androidsample.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.wso2is.androidsample.R;
import com.wso2is.androidsample.mgt.AuthStateManager;
import com.wso2is.androidsample.mgt.ConfigManager;
import com.wso2is.androidsample.oidc.AuthRequest;

import static com.wso2is.androidsample.utils.Constants.APP_NAME;

import net.openid.appauth.AuthState;

import java.util.HashMap;
import java.util.Map;

/**
 * This activity will handle the login view of the application.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();


    ServiceConnection m_service;
    boolean isBound = false;

    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            m_service = null;
            isBound = false;
        }
    };


    @SuppressLint("RestrictedApi")
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
        } else {
            // If the user is not authorized, the LoginActivity view will be launched.
            setContentView(R.layout.activity_login);
            getSupportActionBar().setTitle(APP_NAME);

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
            findViewById(R.id.bLogin).setOnClickListener((View view) -> AuthRequest.getInstance(this).doAuth());

            /* --- CUSTOM CODE --- */
            WebView wv = (WebView) findViewById(R.id.wv);
            Map<String, String> extraHeaders = new HashMap<String, String>();
            extraHeaders.put("Referer", "http://www.example.com");
            findViewById(R.id.bSignup).setOnClickListener((View view) -> {
//                AuthRequest.getInstance(this).warmUpBrowserWithSignup();
                wv.canGoBack();
                wv.setVisibility(1);
                wv.loadUrl("http://172.19.23.27:8082/account/register", extraHeaders);
                wv.getSettings().setJavaScriptEnabled(true);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView wView, String url) {

                        if (url.startsWith("com.wso2is")) {
                            Log.w(TAG, "vao day");
                            Log.w(TAG, wView.toString());
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_VIEW);
//                            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//                            sendIntent.setType("text/plain");

                            startActivity(sendIntent);
                            finish();

                            return true;

                        }
                        return false;
                    }
                });
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, LoginActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
