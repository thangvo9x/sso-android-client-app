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

import java.util.Objects;

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
//            case R.id.action_language:
//                Toast.makeText(this, "Change Language was clicked", Toast.LENGTH_LONG).show();
//                return true;
            case R.id.action_login:
                logIn();
                return true;
            case R.id.action_signup:
                signUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logIn() {
        AuthRequest.getInstance(this).doAuth();
    }

    private void signUp() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AuthStateManager authStateManager = AuthStateManager.getInstance(this);
        ConfigManager configuration = ConfigManager.getInstance(this);

        Log.i(TAG, "Vao day roi - HTCorp 2 !!!");
        // If the user is authorized, the UserActivity view will be launched.
        if (authStateManager.getCurrentState().isAuthorized() && !configuration.hasConfigurationChanged()) {
            Log.i(TAG, "Vao day roi - HTCorp 1 - O tren !!!");
            Log.i(TAG, "User is already authorized, proceeding to user activity.");
            startActivity(new Intent(this, UserActivity.class));
            finish();
        } else {
            Log.i(TAG, "Vao day roi - HTCorp 1 - O duoi !!!");
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
//            findViewById(R.id.action_login).setOnClickListener((View view) -> AuthRequest.getInstance(this).doAuth());
        }


//        setContentView(R.layout.activity_main);

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
            setupTabIcons();
        }



        /* --- REMOVE CLICKABLE ON OTHERS TAB BAR --- */
        LinearLayout tabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            int finalI = i;
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (finalI > 0)
                        return true;
                    return false;
                }
            });
        }

    }

    private void setupTabIcons() {
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setIcon(mTabsIcons[0]);
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setIcon(mTabsIcons[1]);
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setIcon(mTabsIcons[2]);
        Objects.requireNonNull(mTabLayout.getTabAt(3)).setIcon(mTabsIcons[3]);
    }


}