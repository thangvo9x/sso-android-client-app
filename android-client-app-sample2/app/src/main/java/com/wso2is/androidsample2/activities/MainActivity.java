package com.wso2is.androidsample2.activities;

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
import android.graphics.Color;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wso2is.androidsample2.R;
import com.wso2is.androidsample2.fragments.Home;
import com.wso2is.androidsample2.mgt.AuthStateManager;
import com.wso2is.androidsample2.mgt.ConfigManager;
import com.wso2is.androidsample2.navigation.CustomViewPager;
import com.wso2is.androidsample2.navigation.ViewPagerAdapter;
import com.wso2is.androidsample2.oidc.AuthRequest;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TabLayout mTabLayout;
    private Toolbar toolbar;

    private int[] mTabsIcons = {
            R.drawable.home,
            R.drawable.list,
            R.drawable.google_maps,
            R.drawable.favorite,
    };
    private AuthorizationService authService;
    private final AtomicReference<CustomTabsIntent> customTabIntent = new AtomicReference<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items_app_bar, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                logIn();
                return true;
            case R.id.action_signup:
                startActivity(new Intent(this, SignUpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logIn() {
        AuthRequest.getInstance(this).doAuth();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AuthStateManager authStateManager = AuthStateManager.getInstance(this);
        AuthState state = authStateManager.getCurrentState();
        ConfigManager configuration = ConfigManager.getInstance(this);



//        Log.i(TAG, "Vao day roi - HTCorp 1 !!!" + new Date(state.getAccessTokenExpirationTime()));
        // If the user is authorized, the UserActivity view will be launched.
//        int compare = new Date().compareTo(new Date(state.getAccessTokenExpirationTime()));
//        Log.i(TAG, String.valueOf(compare));
//        compare != 1
        if (authStateManager.getCurrentState().isAuthorized() && !configuration.hasConfigurationChanged()) {
            Log.i(TAG, "User is already authorized, proceeding to user activity.");
            startActivity(new Intent(this, UserActivity.class));
            finish();
        } else {
            // If the user is not authorized, the LoginActivity view will be launched.
            setContentView(R.layout.activity_main);

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

        }


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Setup the viewPager
        CustomViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        if (viewPager != null) {
            viewPager.setPagingEnabled(false);

            pagerAdapter.addFrag(new Home(), "Home");
            pagerAdapter.addFrag(new Home(), "Categories");
            pagerAdapter.addFrag(new Home(), "Favorites");
            pagerAdapter.addFrag(new Home(), "User");
            viewPager.setAdapter(pagerAdapter);

            mTabLayout = findViewById(R.id.tab_layout);
            mTabLayout.setupWithViewPager(viewPager);
            mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 1:
                            View tabView = ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(1);
                            tabView.setClickable(false);
                            break;
                        case 2:
                            View tabView2 = ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(2);
                            tabView2.setClickable(false);
                            break;
                        case 3:
                            View tabView3 = ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(3);
                            tabView3.setClickable(false);
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            setupTabIcons();
        }

    }


    private void setupTabIcons() {
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setIcon(mTabsIcons[0]);
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setIcon(mTabsIcons[1]);
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setIcon(mTabsIcons[2]);
        Objects.requireNonNull(mTabLayout.getTabAt(3)).setIcon(mTabsIcons[3]);
    }


}
