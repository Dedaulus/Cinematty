package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import com.dedaulus.cinematty.R;

/**
 * User: Dedaulus
 * Date: 05.10.11
 * Time: 2:11
 */
public class AboutActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}