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
import android.net.Uri;
import android.util.Log;

import com.wso2is.androidsample.mgt.ConfigManager;
import com.wso2is.androidsample.models.User;
import com.wso2is.androidsample.utils.ConnectionBuilderForTesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static com.wso2is.androidsample.activities.UserActivity.userInfoJson;
import static com.wso2is.androidsample.utils.Constants.AUTHORIZATION;
import static com.wso2is.androidsample.utils.Constants.BEARER;

/**
 * This class facilitates the invoking of userinfo endpoint of WSO2 IS in order to obtain user information.
 */
public class UserInfoRequest {

    private static final String TAG = UserInfoRequest.class.getSimpleName();
    private static final AtomicReference<WeakReference<UserInfoRequest>> instance =
            new AtomicReference<>(new WeakReference<UserInfoRequest>(null));

    private final CountDownLatch authIntentLatch = new CountDownLatch(1);

    private ConfigManager configuration;
    private boolean val = false;

    private UserInfoRequest() {

    }

    /**
     * Returns an instance of the UserInfoRequest class.
     *
     * @return UserInfoRequest instance.
     */
    public static UserInfoRequest getInstance() {

        UserInfoRequest userInfoRequest = instance.get().get();
        if (userInfoRequest == null) {
            userInfoRequest = new UserInfoRequest();
            instance.set(new WeakReference<>(userInfoRequest));
        }
        return userInfoRequest;
    }


    private String checkAccessToken(Context context, String oldAccessToken) {
        configuration = ConfigManager.getInstance(context);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        URL introspectEndpointUri;

        try {
            introspectEndpointUri = new URL(configuration.getIntrospectUri().toString());
        } catch (MalformedURLException urlEx) {
            Log.e(TAG, "Failed to construct introspectEndpointUri: ", urlEx);
            return oldAccessToken;
        }

        try {

            HttpURLConnection conn;
            if (configuration.isHttpsRequired()) {
                conn = (HttpURLConnection) introspectEndpointUri.openConnection();
            } else {
                conn = ConnectionBuilderForTesting.INSTANCE.openConnection(Uri.parse(introspectEndpointUri.toString()));
            }

            conn.setRequestProperty(AUTHORIZATION, BEARER + oldAccessToken);
            conn.setRequestMethod("POST");
            Log.d(TAG, "response code AccessToken: " + conn.getResponseCode());

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println("vao roi checkAccessToken: " + response.toString());
            // TODO


//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {


//            } else {
//                System.out.println("GET request not worked checkAccessToken");
//            }
        } catch (IOException ioEx) {
            Log.e(TAG, "Network error when querying userinfo endpoint: ", ioEx);

        }
        return oldAccessToken;
    }

    /**
     * Fetches user information by invoking the userinfo endpoint of the WSO2 IS.
     *
     * @param accessToken Access token of the current session.
     * @param context     Application context.
     * @param user        User object that stores details of the user.
     * @return Returns a boolean to represent success or failure of the user info fetch.
     */
    public boolean fetchUserInfo(String accessToken, Context context, User user) {

        configuration = ConfigManager.getInstance(context);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        URL userInfoEndpoint;

        try {
            userInfoEndpoint = new URL(configuration.getUserInfoEndpointUri().toString());

        } catch (MalformedURLException urlEx) {
            Log.e(TAG, "Failed to construct user info endpoint URL: ", urlEx);
            userInfoJson.set(null);
            return false;
        }


        executor.submit(() -> {
            try {
                HttpURLConnection conn;
                if (configuration.isHttpsRequired()) {
                    conn = (HttpURLConnection) userInfoEndpoint.openConnection();
                } else {
                    conn = ConnectionBuilderForTesting.INSTANCE.openConnection(Uri.parse(userInfoEndpoint.toString()));
                }

                // check access token here
//                String sameAccessToken = checkAccessToken(context, accessToken);
                String sameAccessToken = accessToken;

                conn.setRequestProperty(AUTHORIZATION, BEARER + sameAccessToken);
                conn.setRequestMethod("GET");
                Log.d(TAG, "AccessToken " + sameAccessToken);
                Log.d(TAG, "response code: " + conn.getResponseCode());
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println("vao roi" + response.toString());
                    userInfoJson.set(new JSONObject(response.toString()));
                } else {
                    System.out.println("GET request not worked");
                }

//                String response = Okio.buffer(Okio.source(conn.getInputStream())).readString(Charset.forName(UTF_8));
//                userInfoJson.set(new JSONObject(response));
//                Log.d(TAG, "Response" + userInfoJson.get().toString());

                // Sets values for the user object.
                if (!userInfoJson.get().isNull("sub")) {
                    user.setUsername("username: " + userInfoJson.get().getString("sub"));
                } else {
                    user.setUsername("");
                }

                val = true;

            } catch (IOException ioEx) {
                Log.e(TAG, "Network error when querying userinfo endpoint: ", ioEx);

            } catch (JSONException jsonEx) {
                Log.e(TAG, "Failed to parse userinfo response: ", jsonEx);

            } finally {
                authIntentLatch.countDown();
            }
        });

        try {
            authIntentLatch.await();
        } catch (InterruptedException ex) {
            Log.w(TAG, "Interrupted while waiting for auth intent: ", ex);
        }

        return val;
    }
}
