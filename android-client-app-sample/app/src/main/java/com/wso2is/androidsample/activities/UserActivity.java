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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wso2is.androidsample.R;
import com.wso2is.androidsample.fragments.Categories;
import com.wso2is.androidsample.fragments.Home;
import com.wso2is.androidsample.mgt.AuthStateManager;
import com.wso2is.androidsample.mgt.ConfigManager;
import com.wso2is.androidsample.models.User;
import com.wso2is.androidsample.navigation.CustomViewPager;
import com.wso2is.androidsample.navigation.ViewPagerAdapter;
import com.wso2is.androidsample.oidc.LogoutRequest;
import com.wso2is.androidsample.oidc.UserInfoRequest;
import com.wso2is.androidsample.utils.SetupScreenCustom;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This activity will exchange the authorization code for an access token if not
 * already authorized and redirect to the user profile view.
 */
public class UserActivity extends AppCompatActivity {

    public static final AtomicReference<JSONObject> userInfoJson = new AtomicReference<>();

    private final String TAG = UserActivity.class.getSimpleName();
    private final User user = new User();

    private static AuthStateManager stateManager;
    private static String accessToken;

    public static String idToken;
    public static String state;

    private AuthorizationService authService;
    private ConfigManager configuration;
    private Toolbar toolbar;
    private SetupScreenCustom setupScreenCustom;

    private TabLayout mTabLayout;
    private int[] mTabsIcons = {
            R.drawable.home,
            R.drawable.list,
            R.drawable.google_maps,
            R.drawable.favorite,
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode== KeyEvent.KEYCODE_BACK)
//            Toast.makeText(getApplicationContext(), "back press",
//                    Toast.LENGTH_LONG).show();

        return false;
        // Disable back button..............
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        stateManager = AuthStateManager.getInstance(this);
        configuration = ConfigManager.getInstance(this);


        if (configuration.hasConfigurationChanged()) {
                Toast.makeText(this, "Configuration change detected!", Toast.LENGTH_SHORT).show();
            LogoutRequest.getInstance().signOutSSO(this);
            finish();
        } else {
            authService = new AuthorizationService(this, new AppAuthConfiguration.Builder()
                    .setConnectionBuilder(configuration.getConnectionBuilder()).build());
        }

        // Custom TabBar Here
        setContentView(R.layout.activity_try_user);
//        getSupportActionBar().setTitle(APP_NAME);

        CustomViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(viewPager != null) {
            viewPager.setPagingEnabled(false);

            pagerAdapter.addFrag(new com.wso2is.androidsample.fragments.User(), "Home");
            pagerAdapter.addFrag(new Categories(), "Categories");
            pagerAdapter.addFrag(new com.wso2is.androidsample.fragments.Map(), "Favorites");
            pagerAdapter.addFrag(new com.wso2is.androidsample.fragments.Map(), "User");
            viewPager.setAdapter(pagerAdapter);

            mTabLayout = findViewById(R.id.tab_layout);
            mTabLayout.setupWithViewPager(viewPager);

            mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 1:
                            ViewGroup tabItem = (ViewGroup) ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(0);
                            tabItem.setClickable(false);
                            break;
                        case 2:
                            ViewGroup tabItem2 = (ViewGroup) ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(2);
                            tabItem2.setClickable(false);
                            break;
                        case 3:
                            ViewGroup tabItem3 = (ViewGroup) ((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(3);
                            tabItem3.setClickable(false);
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart " + user.getHtId());
        if (stateManager.getCurrentState().isAuthorized()) {
            displayAuthorized();
        } else {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
            AuthorizationException ex = AuthorizationException.fromIntent(getIntent());

            if (response != null || ex != null) {
                stateManager.updateAfterAuthorization(response, ex);
                if (response != null && response.authorizationCode != null) {
                    // Authorization code exchange is required.
                    exchangeAuthorizationCode(response);
                } else if (ex != null) {
                    Log.e(TAG, "Authorization request failed: " + ex.getMessage());
                    Toast.makeText(this, "Authorization request failed!", Toast.LENGTH_SHORT).show();
                    LogoutRequest.getInstance().signOutSSO(this);
                    finish();
                }
            } else {
                Log.e(TAG, "No authorization state retained - re-authorization required.");
                Toast.makeText(this, "Re-authorization required!", Toast.LENGTH_SHORT).show();

                AuthStateManager authStateManager = AuthStateManager.getInstance(this);
                AuthState currentState = authStateManager.getCurrentState();
                AuthState clearedState = new AuthState(currentState.getAuthorizationServiceConfiguration());
                if (currentState.getLastRegistrationResponse() != null) {
                    clearedState.update(currentState.getLastRegistrationResponse());
                }

                Intent login = new Intent(this, MainActivity.class);
                startActivity(login);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authService.dispose();
        authService = null;
    }

    private void setupTabIcons() {
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setIcon(mTabsIcons[0]);
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setIcon(mTabsIcons[1]);
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setIcon(mTabsIcons[2]);
        Objects.requireNonNull(mTabLayout.getTabAt(3)).setIcon(mTabsIcons[3]);
    }

    /**
     * Once the access token is obtained the user profile view will be set.
     */
    private void displayAuthorized() {

        AuthState state = stateManager.getCurrentState();
        // Fetches the user information from the userInfo endpoint of the WSO2 IS.
        boolean val = UserInfoRequest.getInstance().fetchUserInfo(accessToken == null ? state.getAccessToken() : accessToken, this, user);

        if (val) {

            (findViewById(R.id.bLogout)).setOnClickListener((View view) -> {
                LogoutRequest.getInstance().signOutSSO(this);
                finish();
            });

//            TextView tvUsername = findViewById(R.id.tvUsername);
//            tvUsername.setText(user.getUsername());

            TextView tvHtID = findViewById(R.id.tvHtID);
            tvHtID.setText(user.getHtId());

        } else {
//            Toast.makeText(this, "Unable to Fetch User Information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error while fetching user information.");
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }
    }

    /**
     * Exchanging the authorization code for a token.
     *
     * @param authorizationResponse Response of the authorization request.
     */
    private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {

        // Makes the extra parameters for access token request.
        Map<String, String> additionalParameters = new HashMap<>();
//        additionalParameters.put("client_secret", configuration.getClientSecret());

        // State is stored to generate the logout endpoint request.
        state = authorizationResponse.state;

        performTokenRequest(authorizationResponse.createTokenExchangeRequest(additionalParameters),
                this::handleCodeExchangeResponse);
    }

    /**
     * Performing the token request.
     *
     * @param request  Token request.
     * @param callback Token response callback.
     */
    private void performTokenRequest(TokenRequest request,
                                     AuthorizationService.TokenResponseCallback callback) {

        ClientAuthentication clientAuthentication;

        try {
            clientAuthentication = stateManager.getCurrentState().getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.e(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s).", ex);
            return;
        }
        authService.performTokenRequest(request, clientAuthentication, callback);
    }

    /**
     * Processing of the token response.
     *
     * @param tokenResponse Response of the token request.
     * @param authException Exception returned by the token request.
     */
    private void handleCodeExchangeResponse(@Nullable TokenResponse tokenResponse,
                                            @Nullable AuthorizationException authException) {

        stateManager.updateAfterTokenResponse(tokenResponse, authException);

        if (!stateManager.getCurrentState().isAuthorized()) {
            Log.e(TAG, "Authorization code exchange failed.");
        } else {
            idToken = tokenResponse.idToken;
            accessToken = tokenResponse.accessToken;
            runOnUiThread(this::displayAuthorized);
        }
    }
}
