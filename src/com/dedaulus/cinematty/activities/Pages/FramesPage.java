package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.FrameItemAdapter;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 14:29
 */
public class FramesPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private FrameItemAdapter itemAdapter;
    MovieFrameIdsStore frameIdsStore;
    private Boolean binded = false;

    public FramesPage(Context context, CinemattyApplication app, MovieFrameIdsStore frameIdsStore) {
        this.context = context;
        this.app = app;
        this.frameIdsStore = frameIdsStore;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.frames, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return context.getString(R.string.frames_caption);
    }

    public void onResume() {
        if (binded) {
            itemAdapter.onResume();
        }
    }

    public void onPause() {}

    public void onStop() {
        itemAdapter.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
        itemAdapter = new FrameItemAdapter(context, notifier, frameIdsStore, app.getImageRetrievers().getFrameImageRetriever());
        GridView framesGrid = (GridView)view.findViewById(R.id.frames_grid);
        framesGrid.setAdapter(itemAdapter);
        framesGrid.setOnScrollListener(notifier);

        binded = true;
        onResume();

        return view;
    }
}
