package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;

/**
 * User: dedaulus
 * Date: 21.04.13
 * Time: 20:51
 */
public class SharedPageActivity extends SherlockActivity {
    public static final String URL_ID = "url_id";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.shared_page);

        final ActionBarSherlock actionBar = getSherlock();
        actionBar.setProgressBarIndeterminateVisibility(true);

        WebView webView = (WebView)findViewById(R.id.web);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                actionBar.setProgressBarIndeterminateVisibility(false);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getIntent().getStringExtra(URL_ID));
    }

    @Override
    public void onBackPressed() {
        ((CinemattyApplication)getApplication()).getLocationState().stopLocationListening();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}