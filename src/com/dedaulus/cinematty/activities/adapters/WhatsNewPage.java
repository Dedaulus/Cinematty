package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieActivity;
import com.dedaulus.cinematty.framework.MoviePoster;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:17
 */
public class WhatsNewPage implements SliderPage {
    private Context mContext;
    private CinemattyApplication mApp;

    public WhatsNewPage(Context context, CinemattyApplication app) {
        mContext = context;
        mApp = app;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.whats_new, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return mContext.getString(R.string.whats_new_caption);
    }

    public void onResume() {}

    public void onPause() {}

    public void onStop() {}

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private View bindView(View view) {
        GridView whatsNewGrid = (GridView)view.findViewById(R.id.whats_new_grid);
        whatsNewGrid.setAdapter(new PosterItemAdapter(mContext, mApp.getPosters(), mApp.getPictureRetriever()));
        whatsNewGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onPosterItemClick(adapterView, view, i, l);
            }
        });

        return view;
    }

    private void onPosterItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PosterItemAdapter adapter = (PosterItemAdapter)adapterView.getAdapter();
        MoviePoster poster = (MoviePoster)adapter.getItem(i);
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, poster.getMovie(), null, null);
        mApp.setState(cookie, state);

        Intent intent = new Intent(mContext, MovieActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        mContext.startActivity(intent);
    }
}
