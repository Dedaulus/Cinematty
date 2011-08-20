package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
            /*
            iconView.setVisibility(View.VISIBLE);
            captionView.setVisibility(View.VISIBLE);
            captionLabel.setText(mState.cinema.getCaption());
            captionView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onCinemaClick(view);
                }
            });
            */
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater.inflate(R.layout.cinema_info, null, false);
            setCinemaHeader(view);

            list.addHeaderView(view, null, false);

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

    private void setCinemaHeader(View view) {
        setCinemaFavourite(view);
        setCinemaCaption(view);
        setCinemaAddress(view);
        setCinemaPhone(view);
        setCinemaUrl(view);
    }

    private void setCinemaFavourite(View view) {
        ImageView favIcon = (ImageView)view.findViewById(R.id.fav_icon_in_cinema_info);

        if (mState.cinema.getFavourite() > 0) {
            favIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favIcon.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void setCinemaCaption(View view) {
        TextView caption = (TextView)view.findViewById(R.id.cinema_caption);
        caption.setText(mState.cinema.getCaption());
    }

    private void setCinemaAddress(View view) {
        View panel = view.findViewById(R.id.cinema_address_panel);
        if (mState.cinema.getAddress() != null) {
            TextView address = (TextView)view.findViewById(R.id.cinema_address);
            address.setText(mState.cinema.getAddress());

            TextView into = (TextView)view.findViewById(R.id.cinema_into);
            if (mState.cinema.getInto() != null) {
                into.setText(mState.cinema.getInto());
                into.setVisibility(View.VISIBLE);
            } else {
                into.setVisibility(View.GONE);
            }

            TextView metro = (TextView)view.findViewById(R.id.cinema_metro);
            if (mState.cinema.getMetro() != null) {
                metro.setText(getString(R.string.metro_near) + ": " + mState.cinema.getMetro());
                metro.setVisibility(View.VISIBLE);
            } else {
                metro.setVisibility(View.GONE);
            }

            panel.setVisibility(View.VISIBLE);
        } else {
            panel.setVisibility(View.GONE);
        }
    }

    private void setCinemaPhone(View view) {
        View panel = view.findViewById(R.id.cinema_phone_panel);
        if (mState.cinema.getPhone() != null) {
            TextView phone = (TextView)view.findViewById(R.id.cinema_phone);
            phone.setText(mState.cinema.getPhone());

            panel.setVisibility(View.VISIBLE);
        }
        else {
            panel.setVisibility(View.GONE);
        }
    }

    private void setCinemaUrl(View view) {
        TextView url = (TextView)view.findViewById(R.id.cinema_url);
        if (mState.cinema.getUrl() != null) {
            StringBuffer buf = new StringBuffer(mState.cinema.getUrl());

            if (mState.cinema.getUrl().startsWith("http://")) {
                buf.delete(0, "http://".length());
            }

            int slashPos = buf.indexOf("/");
            if (slashPos != -1) {
                buf.delete(slashPos, buf.length());
            }

            SpannableString str = new SpannableString(buf.toString());
            str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);

            url.setText(str);

            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }
    }

    public void onCinemaAddressClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=Россия, " + mApp.getCurrentCity().getName() + ", " + mState.cinema.getAddress()));
        startActivity(intent);
    }

    public void onCinemaPhoneClick(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+7" + mState.cinema.getPlainPhone()));
        startActivity(intent);
    }

    public void onCinemaUrlClick(View view) {
        if (mState.cinema.getUrl() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mState.cinema.getUrl()));
            startActivity(intent);
        }
    }

    public void onCinemaFavIconClick(View view) {
        if (mState.cinema.getFavourite() > 0) {
            mState.cinema.setFavourite(false);
            ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
        } else {
            mState.cinema.setFavourite(true);
            ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
        }
    }
}