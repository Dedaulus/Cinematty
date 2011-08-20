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
import com.dedaulus.cinematty.activities.adapters.MovieItemAdapter;
import com.dedaulus.cinematty.activities.adapters.MovieItemWithScheduleAdapter;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:37
 */
public class MovieListActivity extends Activity {
    CinemattyApplication mApp;
    UniqueSortedList<Movie> mScopeMovies;
    ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list);

        findViewById(R.id.movie_list_title_arrow_left).setVisibility(View.GONE);
        findViewById(R.id.movie_list_title_arrow_right).setVisibility(View.GONE);
        findViewById(R.id.movie_list_title_home).setVisibility(View.VISIBLE);

        mApp = (CinemattyApplication)getApplication();
        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        View captionView = findViewById(R.id.cinema_panel_in_movie_list);
        TextView captionLabel = (TextView)findViewById(R.id.cinema_caption_in_movie_list);
        View iconView = findViewById(R.id.select_cinema_ico);
        ListView list = (ListView)findViewById(R.id.movie_list);

        switch (mState.activityType) {
        case MOVIE_LIST:
            iconView.setVisibility(View.GONE);
            captionView.setVisibility(View.GONE);
            mScopeMovies = mApp.getMovies();
            list.setAdapter(new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies), mApp.getPictureRetriever()));
            break;

        case MOVIE_LIST_W_CINEMA:
            iconView.setVisibility(View.VISIBLE);
            captionView.setVisibility(View.VISIBLE);
            captionLabel.setText(mState.cinema.getCaption());
            captionView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onCinemaClick(view);
                }
            });
            mScopeMovies = mState.cinema.getMovies();
            list.setAdapter(new MovieItemWithScheduleAdapter(this, new ArrayList<Movie>(mScopeMovies), mState.cinema, mApp.getPictureRetriever()));
            break;

        case MOVIE_LIST_W_ACTOR:
            iconView.setVisibility(View.GONE);
            captionView.setVisibility(View.VISIBLE);
            captionLabel.setText(mState.actor.getActor());
            mScopeMovies = mState.actor.getMovies();
            list.setAdapter(new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies), mApp.getPictureRetriever()));
            break;

        case MOVIE_LIST_W_GENRE:
            iconView.setVisibility(View.GONE);
            captionView.setVisibility(View.VISIBLE);
            captionLabel.setText(mState.genre.getGenre());
            mScopeMovies = mState.genre.getMovies();
            list.setAdapter(new MovieItemAdapter(this, new ArrayList<Movie>(mScopeMovies), mApp.getPictureRetriever()));
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onMovieItemClick(view);
            }
        });
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);

        super.onBackPressed();
    }

    private void onCinemaClick(View view) {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = mState.clone();
        state.activityType = ActivityState.ActivityType.CINEMA_INFO;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, CinemaActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    private void onMovieItemClick(View view) {
        TextView textView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        String caption = textView.getText().toString();
        int movieId = mScopeMovies.indexOf(new Movie(caption));
        if (movieId != -1) {
            String cookie = UUID.randomUUID().toString();
            ActivityState state = mState.clone();
            state.movie = mScopeMovies.get(movieId);
            state.activityType = ActivityState.ActivityType.MOVIE_INFO;
            mApp.setState(cookie, state);

            Intent intent = new Intent(this, MovieActivity.class);
            intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
            startActivity(intent);
        }
    }

    public void onHomeButtonClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}