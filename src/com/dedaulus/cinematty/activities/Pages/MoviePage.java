package com.dedaulus.cinematty.activities.Pages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.ActorListActivity;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 0:46
 */
public class MoviePage implements SliderPage, MovieImageRetriever.MovieImageReceivedAction {
    private Context context;
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private ActivityState state;
    private int currentDay;
    private View pageView;
    private Boolean binded = false;
    
    public MoviePage(Context context, CinemattyApplication app, ActivityState state) {
        this.context = context;
        this.app = app;
        this.state = state;

        settings = app.getSettings();
        activitiesState = app.getActivitiesState();
    }
    
    @Override
    public View getView() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        pageView = layoutInflater.inflate(R.layout.movie_info, null, false);
        bindView();
        return pageView;
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.description_caption);
    }

    @Override
    public void onResume() {
        if (binded) {
            if (state.activityType == ActivityState.MOVIE_INFO_W_SCHED && currentDay != settings.getCurrentDay()) {
                setCurrentDay(settings.getCurrentDay());
            }
            setActors();
        }
    }

    @Override
    public void onPause() {}

    @Override
    public void onStop() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((Activity) context).getMenuInflater();

        if (state.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            if (state.cinema.getPhone() != null) {
                inflater.inflate(R.menu.call_menu, menu);
            }

            inflater.inflate(R.menu.select_day_menu, menu);
            if (currentDay == Constants.TODAY_SCHEDULE) {
                menu.findItem(R.id.menu_day).setTitle(R.string.tomorrow);
            } else {
                menu.findItem(R.id.menu_day).setTitle(R.string.today);
            }
        }

        if (state.movie.getActors() != null) {
            inflater.inflate(R.menu.show_actors_menu, menu);
        }

        inflater.inflate(R.menu.share_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+7" + state.cinema.getPlainPhone()));
                context.startActivity(intent);
                return true;

            case R.id.menu_day:
                setCurrentDay(currentDay == Constants.TODAY_SCHEDULE ? Constants.TOMORROW_SCHEDULE : Constants.TODAY_SCHEDULE);
                return true;

            case R.id.menu_show_actors:
                onActorsClick();
                return true;

            case R.id.menu_share:
                onShareButtonClick();
                return true;

            default:
                return true;
        }
    }

    private void bindView() {
        switch (state.activityType) {
            case ActivityState.MOVIE_INFO_W_SCHED:
                currentDay = settings.getCurrentDay();

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

        binded = true;
        onResume();
    }

    private void setPicture() {
        RelativeLayout progressBar = (RelativeLayout)pageView.findViewById(R.id.movie_icon_loading);
        ImageView imageView = (ImageView)pageView.findViewById(R.id.movie_icon);

        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);

        String picId = state.movie.getPicId();
        if (picId != null) {
            MovieImageRetriever retriever = app.getImageRetrievers().getMovieImageRetriever();
            Bitmap image = retriever.getImage(picId, false);
            if (image != null) {
                imageView.setImageBitmap(image);
                imageView.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onImageClick();
                    }
                });
            } else {
                progressBar.setVisibility(View.VISIBLE);
                retriever.addRequest(picId, false, this);
            }
        }
    }

    private void setCaption() {
        TextView text = (TextView)pageView.findViewById(R.id.movie_caption);
        text.setText(state.movie.getName());
        
        View headView = pageView.findViewById(R.id.movie_head);
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUrlClick();
            }
        });
    }

    private void setSchedule() {
        if (state.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            TextView text = (TextView)pageView.findViewById(R.id.schedule_title);
            text.setText(context.getString(R.string.schedule_enum) + " " + state.cinema.getName());

            text = (TextView)pageView.findViewById(R.id.schedule_enum_for_one_cinema);
            Pair<Movie, List<Calendar>> showTimes = state.cinema.getShowTimes(settings.getCurrentDay()).get(state.movie.getName());
            if (showTimes != null) {
                text.setText(DataConverter.showTimesToSpannableString(context, showTimes.second));
            }

            pageView.findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.movie_schedule_enum_panel).setVisibility(View.GONE);
        }
    }

    private void setImdb() {
        float imdb = state.movie.getImdb();
        if (imdb > 0) {
            String imdbString = String.format(" %.1f", imdb);
            TextView imdbView = (TextView)pageView.findViewById(R.id.imdb);
            imdbView.setText(imdbString);
            pageView.findViewById(R.id.rating).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.rating).setVisibility(View.GONE);
        }
    }

    private void setLength() {
        TextView text = (TextView)pageView.findViewById(R.id.movie_length);
        if (state.movie.getLength() != 0) {
            text.setText(DataConverter.timeInMinutesToTimeHoursAndMinutes(context, state.movie.getLength()));
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }
    }

    private void setTrailerLink() {
        String caption = context.getString(R.string.movie_trailer_link);
        SpannableString str = new SpannableString(caption);
        str.setSpan(new UnderlineSpan(), 0, caption.length(), 0);

        TextView textView = (TextView)pageView.findViewById(R.id.movie_trailer_url);
        textView.setText(str);
    }

    private void setGenre() {
        TextView text = (TextView)pageView.findViewById(R.id.movie_genre);
        if (state.movie.getGenres().size() != 0) {
            text.setText(DataConverter.genresToString(state.movie.getGenres().values()));
            pageView.findViewById(R.id.movie_genre_panel).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.movie_genre_panel).setVisibility(View.GONE);
        }
    }

    private void setActors() {
        TextView text = (TextView)pageView.findViewById(R.id.movie_actors);
        if (state.movie.getActors().size() != 0) {
            text.setText(DataConverter.actorsToSpannableString(state.movie.getActors().values()));
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onActorsClick();
                }
            });
            pageView.findViewById(R.id.movie_actors_panel).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.movie_actors_panel).setVisibility(View.GONE);
        }
    }

    private void setDescription() {
        TextView text = (TextView)pageView.findViewById(R.id.movie_description);
        String description = state.movie.getDescription();
        if (description != null) {
            text.setText(state.movie.getDescription());
            pageView.findViewById(R.id.movie_description_panel).setVisibility(View.VISIBLE);
        } else {
            pageView.findViewById(R.id.movie_description_panel).setVisibility(View.GONE);
        }
    }

    public void onUrlClick() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(context.getString(R.string.youtube_search_url));
        buffer.append(" \"\"");
        buffer.insert(buffer.length() - 1, state.movie.getName());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(buffer.toString()));
        context.startActivity(intent);
    }

    public void onImageClick() {
        StringBuilder url = new StringBuilder();
        url.append(context.getString(R.string.image_search_url)).append(" ").append(state.movie.getName()).append("#i=1");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url.toString()));
        context.startActivity(intent);
    }

    public void onActorsClick() {
        String cookie = UUID.randomUUID().toString();
        ActivityState state = this.state.clone();
        state.activityType = ActivityState.ACTOR_LIST_W_MOVIE;
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, ActorListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }

    public void onImageReceived(boolean success) {
        if (success) {
            ((Activity)context).runOnUiThread(new Runnable() {
                public void run() {
                    setPicture();
                    app.getImageRetrievers().getMovieImageRetriever().saveState();
                }
            });
        }
    }

    public void onShareButtonClick() {
        final boolean isScheduled = state.activityType == ActivityState.MOVIE_INFO_W_SCHED;

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getString(R.string.generate_link));
        progressDialog.setCancelable(true);
        progressDialog.show();

        final ActivityState state = this.state;
        final Context context = this.context;

        new Thread(new Runnable() {
            public void run() {
                String url = isScheduled ? state.movie.getSharedPageUrl(settings.getCurrentCity(), state.cinema, currentDay) : state.movie.getSharedPageUrl(settings.getCurrentCity());
                final String shortUrl = url != null ? DataConverter.longUrlToShort(url) : null;
                ((Activity)context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.cancel();
                        if (shortUrl != null) {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, shortUrl);
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, state.movie.getName());
                            context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.send_link)));
                        } else {
                            Toast.makeText(context, context.getString(R.string.generate_link_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void setCurrentDay(int day) {
        settings.setCurrentDay(day);
        currentDay = day;
        setSchedule();
    }
}
