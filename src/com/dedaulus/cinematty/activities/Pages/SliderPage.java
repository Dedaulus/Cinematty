package com.dedaulus.cinematty.activities.Pages;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:14
 */
public interface SliderPage {
    View getView();
    String getTitle();

    void onResume();
    void onPause();
    void onStop();

    boolean onCreateOptionsMenu(Menu menu);
    boolean onOptionsItemSelected(MenuItem item);
}
