package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieListActivity;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 04.09.11
 * Time: 20:18
 */
public class GenresPage implements SliderPage {
    private Context mContext;
    private CinemattyApplication mApp;

    public GenresPage(Context context, CinemattyApplication app) {
        mContext = context;
        mApp = app;
    }

    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.genre_list, null, false);

        return bindView(view);
    }

    public String getTitle() {
        return mContext.getString(R.string.genres_caption);
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
        ListView list = (ListView)view.findViewById(R.id.genre_list);
        list.setAdapter(new GenreItemAdapter(mContext, new ArrayList<MovieGenre>(mApp.getGenres())));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onGenreItemClick(adapterView, view, i, l);
            }
        });

        return view;
    }

    private void onGenreItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView textView = (TextView)view.findViewById(R.id.genre_caption_in_genre_list);
        String caption = textView.getText().toString();
        int genreId = mApp.getGenres().indexOf(new MovieGenre(caption));
        if (genreId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = new ActivityState(ActivityState.ActivityType.MOVIE_LIST_W_GENRE, null, null, null, mApp.getGenres().get(genreId));
            mApp.setState(cookie, state);

            Intent intent = new Intent(mContext, MovieListActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            mContext.startActivity(intent);
        }
    }
}
