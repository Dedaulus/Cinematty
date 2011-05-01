package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.GenreItemAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 02.04.11
 * Time: 5:12
 */
public class GenreListActivity extends Activity {
    CinemattyApplication mApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre_list);

        mApp = (CinemattyApplication)getApplication();
        Movie movie = mApp.getCurrentMovie();
        TextView movieLabel = (TextView)findViewById(R.id.movie_caption_in_genre_list);
        ListView list = (ListView)findViewById(R.id.genre_list);

        if (movie == null) {
            movieLabel.setVisibility(View.GONE);

            list.setAdapter(new GenreItemAdapter(this, new ArrayList<MovieGenre>(mApp.getGenres())));

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onGenreItemClick(adapterView, view, i, l);
                }
            });
        }
    }

    private void onGenreItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String caption = ((TextView)view).getText().toString();
        int genreId = mApp.getGenres().indexOf(new MovieGenre(caption));
        if (genreId != -1) {
            mApp.setCurrentGenre(mApp.getGenres().get(genreId));
            Intent intent = new Intent(this, MovieListActivity.class);
            startActivity(intent);
        }
    }
}