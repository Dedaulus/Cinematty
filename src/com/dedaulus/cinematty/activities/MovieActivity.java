package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.*;
import android.widget.*;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.*;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 16.03.11
 * Time: 22:28
 */
public class MovieActivity extends Activity implements PictureReceiver {
    private CinemattyApplication mApp;
    private ActivityState mState;
    private String mStateId;
    private int mCurrentDay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        mApp = (CinemattyApplication)getApplication();
        if (!mApp.isDataActual()) {
            boolean b = false;
            try {
                b = mApp.retrieveData(true);
            } catch (Exception e) {}
            if (!b) {
                mApp.restart();
                finish();
                return;
            }
        }

        mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        mState = mApp.getState(mStateId);
        if (mState == null) throw new RuntimeException("ActivityState error");

        switch (mState.activityType) {
        case ActivityState.MOVIE_INFO_W_SCHED:
            mCurrentDay = mApp.getCurrentDay();
            changeTitleBar();
            TextView textView = (TextView)findViewById(R.id.titlebar_caption);
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    registerForContextMenu(view);
                    view.showContextMenu();
                }
            });
        case ActivityState.MOVIE_INFO:
            setPicture();
            setCaption();
            setImdb();
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

    private void changeTitleBar() {
        findViewById(R.id.movie_title_day).setVisibility(View.VISIBLE);
        TextView text = (TextView)findViewById(R.id.titlebar_caption);
        switch (mApp.getCurrentDay()) {
        case Constants.TODAY_SCHEDULE:
            text.setText(R.string.today);
            break;
        case Constants.TOMORROW_SCHEDULE:
            text.setText(R.string.tomorrow);
            break;
        }
    }

    @Override
    protected void onResume() {
        if (mState.activityType == ActivityState.MOVIE_INFO_W_SCHED && mCurrentDay != mApp.getCurrentDay()) {
            setCurrentDay(mApp.getCurrentDay());
        }
        setActors();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mApp.dumpData();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mApp.removeState(mStateId);
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.home_menu, menu);

        if (mState.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            if (mState.cinema.getPhone() != null) {
                inflater.inflate(R.menu.call_menu, menu);
            }

            inflater.inflate(R.menu.select_day_menu, menu);
            if (mCurrentDay == Constants.TODAY_SCHEDULE) {
                menu.findItem(R.id.menu_day).setTitle(R.string.tomorrow);
            } else {
                menu.findItem(R.id.menu_day).setTitle(R.string.today);
            }
        }

        if (mState.movie.getActors() != null) {
            inflater.inflate(R.menu.show_actors_menu, menu);
        }

        inflater.inflate(R.menu.share_menu, menu);

        inflater.inflate(R.menu.about_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_home:
            mApp.goHome(this);
            return true;

        case R.id.menu_call:
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+7" + mState.cinema.getPlainPhone()));
            startActivity(intent);
            return true;

        case R.id.menu_day:
            setCurrentDay(mCurrentDay == Constants.TODAY_SCHEDULE ? Constants.TOMORROW_SCHEDULE : Constants.TODAY_SCHEDULE);
            return true;

        case R.id.menu_show_actors:
            onActorsClick(null);
            return true;

        case R.id.menu_share:
            onShareButtonClick(null);
            return true;

        case R.id.menu_about:
            mApp.showAbout(this);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_day_submenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.submenu_select_day_today:
            if (mCurrentDay != Constants.TODAY_SCHEDULE) {
                setCurrentDay(Constants.TODAY_SCHEDULE);
            }
            return true;
        case R.id.submenu_select_day_tomorrow:
            if (mCurrentDay != Constants.TOMORROW_SCHEDULE) {
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
            }
            return true;
        default:
            return super.onContextItemSelected(item);
        }
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
        if (mState.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            TextView text = (TextView)findViewById(R.id.schedule_title);
            text.setText(getString(R.string.schedule_enum) + " " + mState.cinema.getCaption());

            text = (TextView)findViewById(R.id.schedule_enum_for_one_cinema);
            Map<Movie, List<Calendar>> map = mState.cinema.getShowTimes(mApp.getCurrentDay());
            List<Calendar> showTimes = map != null ? map.get(mState.movie) : null;
            text.setText(DataConverter.showTimesToSpannableString(this, showTimes));

            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.GONE);
        }
    }

    private void setImdb() {
        float imdb = mState.movie.getImdb();
        if (imdb > 0) {
            String imdbString = String.format(" %.1f", imdb);
            TextView imdbView = (TextView)findViewById(R.id.imdb);
            imdbView.setText(imdbString);
            findViewById(R.id.rating).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rating).setVisibility(View.GONE);
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
        state.activityType = ActivityState.CINEMA_LIST_W_MOVIE;
        mApp.setState(cookie, state);

        Intent intent = new Intent(this, CinemaWithScheduleListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        startActivity(intent);
    }

    public void onUrlClick(View view) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getString(R.string.youtube_search_url));
        buffer.append(" \"\"");
        buffer.insert(buffer.length() - 1, mState.movie.getCaption());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(buffer.toString()));
        startActivity(intent);
    }

    public void onPictureClick(View view) {
        StringBuilder url = new StringBuilder();
        url.append(getString(R.string.image_search_url)).append(" ").append(mState.movie.getCaption()).append("#i=1");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url.toString()));
        startActivity(intent);
    }

    public void onActorsClick(View view) {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = mState.clone();
        state.activityType = ActivityState.ACTOR_LIST_W_MOVIE;
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
        mApp.goHome(this);
    }

    public void onDayButtonClick(View view) {
        registerForContextMenu(view);
        view.showContextMenu();
    }

    public void onShareButtonClick(View view) {
        final boolean isScheduled = mState.activityType == ActivityState.MOVIE_INFO_W_SCHED;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.generate_link));
        progressDialog.setCancelable(true);
        progressDialog.show();

        final CinemattyApplication app = mApp;
        final ActivityState state = mState;
        final Context ctx = this;

        new Thread(new Runnable() {
            public void run() {
                String url = isScheduled ? app.getSharedPageUrl(state.movie, mState.cinema, mCurrentDay) : app.getSharedPageUrl(state.movie, null, null);
                final String shortUrl = url != null ? DataConverter.longUrlToShort(url) : null;
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.cancel();
                        if (shortUrl != null) {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, shortUrl);
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, mState.movie.getCaption());
                            startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_link)));
                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.generate_link_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void setCurrentDay(int day) {
        mApp.setCurrentDay(day);
        mCurrentDay = day;
        changeTitleBar();
        setSchedule();
    }
}