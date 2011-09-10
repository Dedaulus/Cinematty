package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends Activity implements PictureReceiver {
    private CinemattyApplication mApp;
    boolean mPictureReady = false;
    private ActivityState mState;
    private String mStateId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        mApp = (CinemattyApplication)getApplication();
        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        switch (mState.activityType) {
        case MOVIE_INFO:
            setPicture();
            setCaption();
            setLength();
            setTrailerLink();
            setGenre();
            setActors();
            setDescription();
            setSchedule();
            break;

        default:
            throw new RuntimeException("ActivityType error");
        }
    }

    @Override
    protected void onResume() {
        setActors();

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);

        super.onBackPressed();
    }

    private void setPicture() {
        RelativeLayout progressBar = (RelativeLayout)findViewById(R.id.movie_icon_loading);
        ImageView imageView = (ImageView)findViewById(R.id.movie_icon);

        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        String picId = mState.movie.getPicId();
        if (picId != null) {
            PictureRetriever retriever = mApp.getPictureRetriever();
            Bitmap picture = retriever.getPicture(picId, PictureType.ORIGINAL);
            if (picture != null) {
                imageView.setImageBitmap(picture);
                imageView.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                retriever.addRequest(picId, PictureType.ORIGINAL, this);
            }
        }
    }

    private void setCaption() {
        TextView text = (TextView)findViewById(R.id.movie_caption);
        text.setText(mState.movie.getCaption());
    }

    private void setSchedule() {
        if (mState.cinema != null) {
            TextView text = (TextView)findViewById(R.id.schedule_title);
            text.setText(getString(R.string.schedule_enum) + " " + mState.cinema.getCaption());

            text = (TextView)findViewById(R.id.schedule_enum_for_one_cinema);
            List<Calendar> showTimes = mState.cinema.getShowTimes().get(mState.movie);
            if (showTimes != null) {
                text.setText(DataConverter.showTimesToSpannableString(showTimes));
            }

            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.GONE);
        }
    }

    private void setLength() {
        TextView text = (TextView)findViewById(R.id.movie_length);
        if (mState.movie.getLengthInMinutes() != 0) {
            text.setText(DataConverter.timeInMinutesToTimeHoursAndMinutes(this, mState.movie.getLengthInMinutes()));
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }
    }

    private void setTrailerLink() {
        String caption = getString(R.string.movie_trailer_link);
        SpannableString str = new SpannableString(caption);
        str.setSpan(new UnderlineSpan(), 0, caption.length(), 0);

        TextView textView = (TextView)findViewById(R.id.movie_trailer_url);
        textView.setText(str);
    }

    private void setGenre() {
        TextView text = (TextView)findViewById(R.id.movie_genre);
        if (mState.movie.getGenres().size() != 0) {
            text.setText(DataConverter.genresToString(mState.movie.getGenres()));
            findViewById(R.id.movie_genre_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_genre_panel).setVisibility(View.GONE);
        }
    }

    private void setActors() {
        TextView text = (TextView)findViewById(R.id.movie_actors);
        if (mState.movie.getActors().size() != 0) {
            text.setText(DataConverter.actorsToSpannableString(mState.movie.getActors()));
            findViewById(R.id.movie_actors_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_actors_panel).setVisibility(View.GONE);
        }
    }

    private void setDescription() {
        TextView text = (TextView)findViewById(R.id.movie_description);
        if (mState.movie.getDescription().length() != 0) {
            text.setText(mState.movie.getDescription());
            findViewById(R.id.movie_description_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_description_panel).setVisibility(View.GONE);
        }
    }

    public void onSchedulesClick(View view) {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = mState.clone();
        state.activityType = ActivityState.ActivityType.CINEMA_LIST_W_MOVIE;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, CinemaListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getString(R.string.youtube_search_url));
        buffer.append(" \"\"");
        buffer.insert(buffer.length() - 1, mState.movie.getCaption());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(buffer.toString()));
        startActivity(intent);
    }

    public void onActorsClick(View view) {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = mState.clone();
        state.activityType = ActivityState.ActivityType.ACTOR_LIST_W_MOVIE;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, ActorListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        if (success) {
            runOnUiThread(new Runnable() {
                public void run() {
                    setPicture();
                }
            });
        }
    }

    public void onHomeButtonClick(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}