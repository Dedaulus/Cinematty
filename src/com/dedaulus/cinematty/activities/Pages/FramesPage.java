package com.dedaulus.cinematty.activities.Pages;

import android.content.Context;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.FrameItemAdapter;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 14:29
 */
public class FramesPage implements SliderPage {
    private Context context;
    private CinemattyApplication app;
    private ActivitiesState activitiesState;
    private FrameItemAdapter itemAdapter;
    MovieFrameIdsStore frameIdsStore;
    private Boolean binded = false;

    public FramesPage(Context context, CinemattyApplication app, MovieFrameIdsStore frameIdsStore) {
        this.context = context;
        this.app = app;
        this.frameIdsStore = frameIdsStore;

        activitiesState = app.getActivitiesState();
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
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        GridView framesGrid = (GridView)view.findViewById(R.id.frames_grid);
        itemAdapter = new FrameItemAdapter(context, frameIdsStore, app.getImageRetrievers().getFrameImageRetriever());
        framesGrid.setAdapter(itemAdapter);
        //framesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //        onPosterItemClick(adapterView, view, i, l);
        //    }
        //});
        binded = true;
        onResume();

        return view;
    }

    private void onPosterItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //PosterItemAdapter adapter = (PosterItemAdapter)adapterView.getAdapter();
        //MoviePoster poster = (MoviePoster)adapter.getItem(i);
        //String cookie = UUID.randomUUID().toString();
        //ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, poster.getMovie(), null, null);
        //activitiesState.setState(cookie, state);

        //Intent intent = new Intent(context, MovieActivity.class);
        //intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        //context.startActivity(intent);
    }
}
