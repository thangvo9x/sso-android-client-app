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

package com.wso2is.androidsample2.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.wso2is.androidsample2.R;
import com.wso2is.androidsample2.mgt.AuthStateManager;
import com.wso2is.androidsample2.mgt.ConfigManager;
import com.wso2is.androidsample2.models.User;
import com.wso2is.androidsample2.oidc.LogoutRequest;
import com.wso2is.androidsample2.oidc.UserInfoRequest;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.EndSessionRequest;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This activity will exchange the authorization code for an access token if not
 * already authorized and redirect to the user profile view.
 */
public class UserActivity extends AppCompatActivity {

    public static final AtomicReference<JSONObject> userInfoJson = new AtomicReference<>();
    private static final int END_SESSION_REQUEST_CODE = 911;
    private static final String KEY_USER_INFO = "userInfo";
    private final String TAG = UserActivity.class.getSimpleName();
    private final User user = new User();

    private static AuthStateManager stateManager;
    private static String accessToken;

    public static String idToken;
    public static String state;

    private AuthorizationService authService;
    private ConfigManager configuration;
    private ExecutorService executorService;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode== KeyEvent.KEYCODE_BACK)
//            Toast.makeText(getApplicationContext(), "back press",
//                    Toast.LENGTH_LONG).show();

        return false;
        // Disable back button..............
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        // user info is retained to survive activity restarts, such as when rotating the
        // device or switching apps. This isn't essential, but it helps provide a less
        // jarring UX when these events occur - data does not just disappear from the view.
        if (userInfoJson.get() != null) {
            state.putString(KEY_USER_INFO, userInfoJson.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        stateManager = AuthStateManager.getInstance(this);
        configuration = ConfigManager.getInstance(this);


        if (configuration.hasConfigurationChanged()) {
            Toast.makeText(this, "Configuration change detected!", Toast.LENGTH_SHORT).show();
            this.logout();
            finish();
        } else {
            authService = new AuthorizationService(this, new AppAuthConfiguration.Builder()
                    .setConnectionBuilder(configuration.getConnectionBuilder()).build());
        }

        // Custom TabBar Here
//        setContentView(R.layout.activity_try_user);


        if (savedInstanceState != null) {
            try {
                userInfoJson.set(new JSONObject(savedInstanceState.getString(KEY_USER_INFO)));
            } catch (JSONException ex) {
                Log.e(TAG, "Failed to parse saved user info JSON, discarding", ex);
            }
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
                    finish();
                }
            } else {
                Log.e(TAG, "No authorization state retained - re-authorization required.");
                Toast.makeText(this, "Re-authorization required!", Toast.LENGTH_SHORT).show();

                this.logout();
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


    /**
     * Once the access token is obtained the user profile view will be set.
     */
    private void displayAuthorized() {

//        AuthState state = stateManager.getCurrentState();
        // Fetches the user information from the userInfo endpoint of the WSO2 IS.
        boolean val = UserInfoRequest.getInstance().fetchUserInfo(stateManager.getCurrentState().getAccessToken(), this, user);

        if (val) {
            setContentView(R.layout.activity_try_user);
//            (findViewById(R.id.bLogout)).setOnClickListener((View view) -> {
//                this.logout();
//                finish();
//            });
            (findViewById(R.id.imageView)).setOnClickListener((View view) -> {
                this.logout();
                finish();
            });


            TextView tvHtID = findViewById(R.id.tvHtID);
//            Log.i(TAG, "vao day roi" + user.getHtId());
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

    private void logout() {

        AuthState currentState = stateManager.getCurrentState();
        AuthState clearedState = new AuthState(currentState.getAuthorizationServiceConfiguration());

        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }
        stateManager.replaceState(clearedState);


        Log.i(TAG, stateManager.getCurrentState().getAuthorizationServiceConfiguration().toString());
        Log.i(TAG, idToken + configuration.getEndSessionUri().toString());
        Log.i(TAG, "accessToken:" + accessToken);

//        Log.i(TAG, "getLogoutEndpointUri:" +  configuration.getLogoutEndpointUri().toString());
        Log.i(TAG, "redirect:" +  configuration.getRedirectUri().toString());
        Log.i(TAG, "post_logout_redirect_uri:" +  configuration.getLogoutEndpointUri().toString());

//        if (idToken != null) {
//            Intent endSessionIntent = authService.getEndSessionRequestIntent(
//                    new EndSessionRequest.Builder(
//                            stateManager.getCurrentState().getAuthorizationServiceConfiguration(),
//                            idToken,
//                            configuration.getRedirectUri()).build());
//            startActivityForResult(endSessionIntent, END_SESSION_REQUEST_CODE);
//        } else {
//            Intent mainIntent = new Intent(this, MainActivity.class);
//            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivityForResult(mainIntent, END_SESSION_REQUEST_CODE);
//            finish();
//        }
//

        String logout_uri = configuration.getLogoutEndpointUri().toString();
        String redirect = configuration.getRedirectUri().toString();
        StringBuffer url = new StringBuffer();
        url.append(logout_uri);
        url.append("?id_token_hint=");
        url.append(idToken);
        url.append("&post_logout_redirect_uri=");
        url.append(redirect);
        url.append("&state=");
        url.append(state);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        customTabsIntent.launchUrl(this, Uri.parse(url.toString()));


//c
    }
}
