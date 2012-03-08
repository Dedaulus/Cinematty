package com.dedaulus.cinematty.activities;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.dedaulus.cinematty.R;

/**
 * User: Dedaulus
 * Date: 05.10.11
 * Time: 2:11
 */
public class AboutActivity extends SherlockActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}