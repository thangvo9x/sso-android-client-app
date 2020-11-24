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

package com.wso2is.androidsample.oidc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import com.wso2is.androidsample.mgt.AuthStateManager;
import com.wso2is.androidsample.mgt.ConfigManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import net.openid.appauth.AuthState;

import static com.wso2is.androidsample.activities.UserActivity.idToken;
import static com.wso2is.androidsample.activities.UserActivity.state;

/**
 * This class facilitates the logout function of the application.
 * User's state will be reset and will be logged out from the WSO2 IS.
 */
public class LogoutRequest {

    private static final AtomicReference<WeakReference<LogoutRequest>> instance = new AtomicReference<>
            (new WeakReference<LogoutRequest>(null));

    private LogoutRequest() {

    }

    /**
     * Returns an instance of the LogoutRequest class.
     *
     * @return LogoutRequest instance.
     */
    public static LogoutRequest getInstance() {

        LogoutRequest logoutRequest = instance.get().get();
        if (logoutRequest == null) {
            logoutRequest = new LogoutRequest();
            instance.set(new WeakReference<>(logoutRequest));
        }
        return logoutRequest;
    }

    /**
     * The user will be signed out from the application and the WSO2 IS.
     *
     * @param context Current context of the application.
     */
    public void signOut(Context context) {

        AuthStateManager authStateManager = AuthStateManager.getInstance(context);
        ConfigManager configuration = ConfigManager.getInstance(context);

        // Discards the authorization and token states, but retains the configuration.
        AuthState currentState = authStateManager.getCurrentState();
        AuthState clearedState = new AuthState(currentState.getAuthorizationServiceConfiguration());

        if (currentState.getLastRegistrationResponse() != null) {
            clearedState.update(currentState.getLastRegistrationResponse());
        }

        authStateManager.replaceState(clearedState);

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
        customTabsIntent.launchUrl(context, Uri.parse(url.toString()));
    }
}
