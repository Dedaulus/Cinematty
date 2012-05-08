package com.dedaulus.cinematty.activities.Pages;

import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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

    void setVisible(boolean visible);

    boolean onCreateOptionsMenu(Menu menu);
    boolean onOptionsItemSelected(MenuItem item);
}
