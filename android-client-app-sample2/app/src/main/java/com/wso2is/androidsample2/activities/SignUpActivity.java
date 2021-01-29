package com.wso2is.androidsample2.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wso2is.androidsample2.R;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Log.i(TAG, "vao signUp day" + getApplicationContext().getPackageName());
        WebView wv = (WebView) findViewById(R.id.webview);
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", getApplicationContext().getPackageName());

        wv.canGoBack();
        wv.setVisibility(View.VISIBLE);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDomStorageEnabled(true);

        wv.loadUrl("https://app-profile-dev.hungthinhcorp.com.vn/account/register", extraHeaders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        wv.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView wView, String url) {
//                Log.i(TAG, "message");
//                if (url.startsWith("com.wso2is")) {
//                    Log.w(TAG, "vao day");
//                    Log.w(TAG, wView.toString());
//                    Intent sendIntent = new Intent();
//                    sendIntent.setAction(Intent.ACTION_VIEW);
//
//                    startActivity(sendIntent);
//                    finish();
//
//                    return true;
//
//                }
//                return false;
//            }
//        });

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if(appLinkData != null) {
            Log.i(TAG, appLinkData.toString());
        }

    }
}
