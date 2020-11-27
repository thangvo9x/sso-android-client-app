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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wso2is.androidsample.mgt.AuthStateManager;
import com.wso2is.androidsample.mgt.ConfigManager;
import com.wso2is.androidsample.R;
import com.wso2is.androidsample.models.User;
import com.wso2is.androidsample.oidc.LogoutRequest;
import com.wso2is.androidsample.oidc.UserInfoRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import static com.wso2is.androidsample.utils.Constants.APP_NAME;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        stateManager = AuthStateManager.getInstance(this);
        configuration = ConfigManager.getInstance(this);

        if (configuration.hasConfigurationChanged()) {
            Toast.makeText(this, "Configuration change detected!", Toast.LENGTH_SHORT).show();
            LogoutRequest.getInstance().signOut(this);
            finish();
        } else {
            authService = new AuthorizationService(this, new AppAuthConfiguration.Builder()
                    .setConnectionBuilder(configuration.getConnectionBuilder()).build());
        }
    }

    @Override
    protected void onStart() {

        super.onStart();

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
                    LogoutRequest.getInstance().signOut(this);
                    finish();
                }
            } else {
                Log.e(TAG, "No authorization state retained - re-authorization required.");
                Toast.makeText(this, "Re-authorization required!", Toast.LENGTH_SHORT).show();
                LogoutRequest.getInstance().signOut(this);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        authService.dispose();
    }

    /**
     * Once the access token is obtained the user profile view will be set.
     */
    private void displayAuthorized() {

        AuthState state = stateManager.getCurrentState();
        // Fetches the user information from the userinfo endpoint of the WSO2 IS.
        boolean val = UserInfoRequest.getInstance().fetchUserInfo(accessToken == null ? state.getAccessToken() : accessToken, this, user);

        if (val) {
            setContentView(R.layout.activity_user);
            getSupportActionBar().setTitle(APP_NAME);
            (findViewById(R.id.bLogout)).setOnClickListener((View view) -> {
                LogoutRequest.getInstance().signOut(this);
                finish();
            });

            TextView tvUsername = findViewById(R.id.tvUsername);
            TextView tvEmail = findViewById(R.id.tvEmail);
            tvUsername.setText(user.getUsername());
            tvEmail.setText(user.getEmail());
        } else {
            Toast.makeText(this, "Unable to Fetch User Information", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error while fetching user information.");
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
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
