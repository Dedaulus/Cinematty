package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.Calendar;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends Activity implements PictureReceiver, UpdatableByNeed {
    private CinemattyApplication mApp;
    private CurrentState mCurrentState;
    boolean mPictureReady = false;
    private AsyncTask<UpdatableByNeed, UpdatableByNeed, Void> mPictureUpdater;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        mApp = (CinemattyApplication)getApplication();
        mCurrentState = mApp.getCurrentState();

        setPicture();

        setCaption();

        setLength();

        setTrailerLink();

        setGenre();

        setActors();

        setDescription();

        setSchedule();

        Button btn = (Button)findViewById(R.id.show_schedules_button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onSchedulesBtnClick(view);
            }
        });

        btn.setText(getString(R.string.look_for_schedule));
    }

    @Override
    protected void onResume() {
        mCurrentState = mApp.getCurrentState();

        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mApp.revertCurrentState();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setPicture() {
        RelativeLayout progressBar = (RelativeLayout)findViewById(R.id.movie_icon_loading);
        ImageView imageView = (ImageView)findViewById(R.id.movie_icon);

        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        String picId = mCurrentState.movie.getPicId();
        if (picId != null) {
            PictureRetriever retriever = mApp.getPictureRetriever();
            Bitmap picture = retriever.getPicture(picId, PictureType.ORIGINAL);
            if (picture != null) {
                imageView.setImageBitmap(picture);
                imageView.setVisibility(View.VISIBLE);
            } else {
                retriever.addRequest(picId, PictureType.ORIGINAL, this);
                progressBar.setVisibility(View.VISIBLE);

                mPictureUpdater = new AsyncTask<UpdatableByNeed, UpdatableByNeed, Void>() {
                    @Override
                    protected Void doInBackground(UpdatableByNeed... updatableByNeeds) {
                        while (true) {
                            if (updatableByNeeds[0].isUpdateNeeded()) {
                                publishProgress(updatableByNeeds[0]);
                                return null;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    @Override
                    protected void onProgressUpdate(UpdatableByNeed... values) {
                        values[0].update();
                    }
                }.execute(this);
            }
        }
    }

    private void setCaption() {
        TextView text = (TextView)findViewById(R.id.movie_caption);
        text.setText(mCurrentState.movie.getCaption());
    }

    private void setSchedule() {
        if (mCurrentState.cinema != null) {
            TextView text = (TextView)findViewById(R.id.schedule_title);
            text.setText(getString(R.string.schedule_enum) + " " + mCurrentState.cinema.getCaption());

            text = (TextView)findViewById(R.id.schedule_enum_for_one_cinema);
            List<Calendar> showTimes = mCurrentState.cinema.getShowTimes().get(mCurrentState.movie);
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
        if (mCurrentState.movie.getLengthInMinutes() != 0) {
            text.setText(DataConverter.timeInMinutesToTimeHoursAndMinutes(this, mCurrentState.movie.getLengthInMinutes()));

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
        if (mCurrentState.movie.getGenres().size() != 0) {
            text.setText(DataConverter.genresToString(mCurrentState.movie.getGenres()));

            findViewById(R.id.movie_genre_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_genre_panel).setVisibility(View.GONE);
        }
    }

    private void setActors() {
        TextView text = (TextView)findViewById(R.id.movie_actors);
        if (mCurrentState.movie.getActors().size() != 0) {
            //text.setText(DataConverter.actorsToString(mCurrentState.movie.getActors()));
            text.setText(DataConverter.actorsToSpannableString(mCurrentState.movie.getActors()));

            findViewById(R.id.movie_actors_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_actors_panel).setVisibility(View.GONE);
        }
    }

    private void setDescription() {
        TextView text = (TextView)findViewById(R.id.movie_description);
        if (mCurrentState.movie.getDescription().length() != 0) {
            text.setText(mCurrentState.movie.getDescription());

            findViewById(R.id.movie_description_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_description_panel).setVisibility(View.GONE);
        }
    }

    private void onSchedulesBtnClick(View view) {
        mApp.setCurrentState(mCurrentState.clone());

        Intent intent = new Intent(this, CinemaListActivity.class);
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getString(R.string.youtube_search_url));
        buffer.append(" \"\"");
        buffer.insert(buffer.length() - 1, mCurrentState.movie.getCaption());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(buffer.toString()));
        startActivity(intent);
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        mPictureReady = success;
    }

    public boolean isUpdateNeeded() {
        return mPictureReady;
    }

    public void update() {
        mPictureReady = false;
        setPicture();
    }
}