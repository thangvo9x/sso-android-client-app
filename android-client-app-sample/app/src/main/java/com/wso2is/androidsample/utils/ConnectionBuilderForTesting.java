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

package com.wso2is.androidsample.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.net.Uri;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.openid.appauth.connectivity.ConnectionBuilder;
import net.openid.appauth.Preconditions;

import static com.wso2is.androidsample.utils.Constants.HTTP;
import static com.wso2is.androidsample.utils.Constants.HTTPS;
import static com.wso2is.androidsample.utils.Constants.PROTOCOL;

/**
 * This class can be used in a testing scenario, where the WSO2 IS is hosted in
 * the local machine, to trust all SSL certificates.
 */
public class ConnectionBuilderForTesting implements ConnectionBuilder {

    public static final ConnectionBuilderForTesting INSTANCE = new ConnectionBuilderForTesting();
    private static final String TAG = ConnectionBuilderForTesting.class.getSimpleName();
    private static final int CONNECTION_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(15);
    private static final int READ_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);

    // Default trust manager is overridden for testing purposes of the sample application.
    @SuppressLint("TrustAllX509TrustManager")
    private static final TrustManager[] ANY_CERT_MANAGER = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {

            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {

        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {

        }
    }};

    // Hostname verification is turned off for testing purposes of the sample application.
    @SuppressLint("BadHostnameVerifier")
    private static final HostnameVerifier ANY_HOSTNAME_VERIFIER = (hostname, session) -> true;

    @Nullable
    private static final SSLContext TRUSTING_CONTEXT;

    static {
        SSLContext context;
        try {
            context = SSLContext.getInstance(PROTOCOL);
        } catch (NoSuchAlgorithmException ex) {
            Log.e(TAG, "Unable to acquire SSL context: ", ex);
            context = null;
        }

        SSLContext initializedContext = null;
        if (context != null) {
            try {
                context.init(null, ANY_CERT_MANAGER, new java.security.SecureRandom());
                initializedContext = context;
            } catch (KeyManagementException ex) {
                Log.e(TAG, "Failed to initialize trusting SSL context: ", ex);
            }
        }

        TRUSTING_CONTEXT = initializedContext;
    }

    /**
     * Returns an instance of the HttpURLConnection after opening a connection with the received URI.
     *
     * @param uri The URI to be sent using the http connection.
     * @return An instance of the HttpURLConnection.
     * @throws IOException Exception for handling failed input or output operation.
     */
    @NonNull
    @Override
    public HttpURLConnection openConnection(@NonNull Uri uri) throws IOException {

        Preconditions.checkNotNull(uri, "URL must not be null");
        Preconditions.checkArgument(HTTP.equals(uri.getScheme()) || HTTPS.equals(uri.getScheme()),
                "Scheme or URI must be HTTP or HTTPS");

        HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setInstanceFollowRedirects(false);

        if (conn instanceof HttpsURLConnection && TRUSTING_CONTEXT != null) {
            HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
            httpsConn.setSSLSocketFactory(TRUSTING_CONTEXT.getSocketFactory());
            httpsConn.setHostnameVerifier(ANY_HOSTNAME_VERIFIER);
        }

        return conn;
    }
}
