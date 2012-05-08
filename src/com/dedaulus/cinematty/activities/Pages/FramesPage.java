package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.FramesActivity;
import com.dedaulus.cinematty.activities.adapters.FramePreviewItemAdapter;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 14:29
 */
public class FramesPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private ActivityState state;
    private FramePreviewItemAdapter itemAdapter;
    private MovieFrameIdsStore frameIdsStore;
    private Boolean binded = false;
    private boolean visible = false;

    public FramesPage(Context context, CinemattyApplication app, MovieFrameIdsStore frameIdsStore, ActivityState state) {
        this.context = context;
        this.app = app;
        this.frameIdsStore = frameIdsStore;
        this.state = state;
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
    public void setVisible(boolean visible) {
        this.visible = visible;
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
        itemAdapter = new FramePreviewItemAdapter(context, notifier, frameIdsStore, app.getImageRetrievers().getFrameImageRetriever());
        GridView framesGrid = (GridView)view.findViewById(R.id.frames_grid);
        framesGrid.setAdapter(itemAdapter);
        framesGrid.setOnScrollListener(notifier);
        framesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //onFrameClick(adapterView, view, i, l);
            }
        });

        binded = true;
        onResume();

        return view;
    }

    private void onFrameClick(AdapterView<?> adapterView, View view, int i, long l) {
        FramePreviewItemAdapter adapter = (FramePreviewItemAdapter)adapterView.getAdapter();
        int frameId = (Integer)adapter.getItem(i);

        String cookie = UUID.randomUUID().toString();

        ActivityState state = this.state.clone();
        state.activityType = ActivityState.VIEW_FRAME;
        app.getActivitiesState().setState(cookie, state);

        Intent intent = new Intent(context, FramesActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        intent.putExtra(Constants.FRAME_ID, frameId);
        context.startActivity(intent);
    }
}
