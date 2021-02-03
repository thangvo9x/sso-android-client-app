package com.wso2is.androidsample.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wso2is.androidsample.activities.MainActivity;
import com.wso2is.androidsample.R;
import android.app.ActionBar;
import java.util.HashMap;
import java.util.Map;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SignUpActivity extends Activity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private ImageView button;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @SuppressLint("ResourceType")
    public void onCreate(Bundle savedInstanceState) {

        final Context context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        button = (ImageButton) findViewById(R.id.buttonUrl);
        button.setImageResource(R.drawable.cancel);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }

        });

//        Log.i(TAG, "vao signUp day" + getApplicationContext().getPackageName());
        WebView wv = (WebView) findViewById(R.id.webview);
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", getApplicationContext().getPackageName());

        wv.canGoBack();
        wv.setVisibility(View.VISIBLE);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDomStorageEnabled(true);

        wv.loadUrl("https://app-profile-dev.hungthinhcorp.com.vn/account/register", extraHeaders);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wView, String url) {
                Log.i(TAG, "message");
                if (url.startsWith("com.wso2is")) {
                    Log.w(TAG, "vao day");
                    Log.w(TAG, wView.toString());
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_VIEW);

                    startActivity(sendIntent);
                    finish();

                    return true;

                }
                return false;
            }
        });

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null) {
            Log.i(TAG, appLinkData.toString());
        }

    }
}
