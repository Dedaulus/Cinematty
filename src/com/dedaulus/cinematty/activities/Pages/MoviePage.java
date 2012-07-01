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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.MovieListActivity;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieImageRetriever;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.*;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 0:46
 */
public class MoviePage implements SliderPage, MovieImageRetriever.MovieImageReceivedAction {
    private List<Pair<MovieActor, ImageView>> favIconHolders;
    private Context context;
    private CinemattyApplication app;
    private ApplicationSettings settings;
    private ActivitiesState activitiesState;
    private ActivityState state;
    private int currentDay;
    private View pageView;
    private Boolean binded = false;
    private boolean visible = false;

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
    public void onPause() {
        settings.saveFavouriteActors();
    }

    @Override
    public void onStop() {}

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = ((SherlockActivity)context).getSupportMenuInflater();

        if (state.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            inflater.inflate(R.menu.select_day_menu, menu);
                        switch (currentDay) {
                case Constants.TODAY_SCHEDULE:
                    menu.findItem(R.id.submenu_select_day_today).setChecked(true);
                    break;

                case Constants.TOMORROW_SCHEDULE:
                    menu.findItem(R.id.submenu_select_day_tomorrow).setChecked(true);
                    break;

                case Constants.AFTER_TOMORROW_SCHEDULE:
                    menu.findItem(R.id.submenu_select_day_after_tomorrow).setChecked(true);
                    break;
            }
        }

        inflater.inflate(R.menu.share_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_day:
                return true;

            case R.id.submenu_select_day_today:
                setCurrentDay(Constants.TODAY_SCHEDULE);
                item.setChecked(true);
                return true;

            case R.id.submenu_select_day_tomorrow:
                setCurrentDay(Constants.TOMORROW_SCHEDULE);
                item.setChecked(true);
                return true;

            case R.id.submenu_select_day_after_tomorrow:
                setCurrentDay(Constants.AFTER_TOMORROW_SCHEDULE);
                item.setChecked(true);
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
                setYearAndCountry();
                setDirector();
                setLength();
                setImdb();
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
        View region = pageView.findViewById(R.id.schedule_region);
        if (state.activityType == ActivityState.MOVIE_INFO_W_SCHED) {
            StringBuilder builder = new StringBuilder();
            switch (currentDay) {
                case Constants.TODAY_SCHEDULE:
                    builder.append(context.getString(R.string.today));
                    break;

                case Constants.TOMORROW_SCHEDULE:
                    builder.append(context.getString(R.string.tomorrow));
                    break;

                case Constants.AFTER_TOMORROW_SCHEDULE:
                    builder.append(context.getString(R.string.after_tomorrow));
                    break;
            }
            builder.append(" в ").append(state.cinema.getName());            
            TextView divider = (TextView)region.findViewById(R.id.schedule_divider).findViewById(R.id.caption);            
            divider.setText(builder.toString().toUpperCase());

            TextView scheduleTextView = (TextView)region.findViewById(R.id.schedule);
            Pair<Movie, List<Calendar>> showTimes = state.cinema.getShowTimes(currentDay).get(state.movie.getName());
            if (showTimes != null) {
                SpannableString schedule = DataConverter.showTimesToSpannableString(context, showTimes.second);
                scheduleTextView.setText(schedule);
            } else {
                scheduleTextView.setText(R.string.unknown_schedule);
            }
            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }
    
    private void setYearAndCountry() {
        TextView textView = (TextView)pageView.findViewById(R.id.movie_year_and_country);
        StringBuilder builder = new StringBuilder();
        int year = state.movie.getYear(); 
        if (year != 0) {
            builder.append(year).append(context.getString(R.string.year)).append(" ");            
        }
        String countries = DataConverter.countriesToString(state.movie.getCountries());
        if (countries.length() != 0) {
            builder.append(countries);
        }
        
        if (builder.length() != 0) {
            textView.setText(builder.toString());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
    
    private void setLength() {
        TextView textView = (TextView)pageView.findViewById(R.id.movie_length);
        if (state.movie.getLength() != 0) {
            textView.setText(DataConverter.timeInMinutesToTimeHoursAndMinutes(context, state.movie.getLength()));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void setImdb() {
        TextView textView = (TextView)pageView.findViewById(R.id.imdb);
        float imdb = state.movie.getImdb();
        if (imdb > 0) {
            textView.setText(DataConverter.imdbToString(imdb).toUpperCase());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
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
        View region = pageView.findViewById(R.id.genre_region);
        String genres = DataConverter.genresToString(state.movie.getGenres().values());
        if (genres.length() != 0) {
            TextView divider = (TextView)region.findViewById(R.id.genre_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.genres_separator));
            TextView genreTextView = (TextView)region.findViewById(R.id.genre);
            genreTextView.setText(genres);
            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }

    private void setDirector() {
        View region = pageView.findViewById(R.id.director_region);
        String directors = DataConverter.directorsToString(state.movie.getDirectors());
        if (directors.length() != 0) {
            TextView divider = (TextView)region.findViewById(R.id.director_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.director_separator));
            TextView directorTextView = (TextView)region.findViewById(R.id.director);
            directorTextView.setText(directors);
            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }

    private void setActors() {
        ViewGroup region = (ViewGroup)pageView.findViewById(R.id.actors_region);
        TreeSet<MovieActor> actors = new TreeSet<MovieActor>(state.movie.getActors().values());
        if (!actors.isEmpty()) {
            TextView divider = (TextView)region.findViewById(R.id.actors_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.actors_separator));
            
            if (favIconHolders == null) {
                favIconHolders = new ArrayList<Pair<MovieActor, ImageView>>(actors.size());
                LayoutInflater inflater = LayoutInflater.from(context);
                for (MovieActor actor : actors) {                                        
                    View actorView = inflater.inflate(R.layout.actor_item, null);
                    TextView caption = (TextView)actorView.findViewById(R.id.actor_caption);
                    ImageView icon = (ImageView)actorView.findViewById(R.id.fav_icon);
                    Pair<MovieActor, ImageView> iconHolder = Pair.create(actor, icon);

                    caption.setText(actor.getName());
                    View actorDataRegion = actorView.findViewById(R.id.actor_data_region);
                    actorDataRegion.setTag(actor);
                    actorDataRegion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onActorClick(view);
                        }
                    });
                    
                    if (actor.getFavourite() > 0) {
                        icon.setImageResource(R.drawable.ic_list_fav_on);
                    } else {
                        icon.setImageResource(R.drawable.ic_list_fav_off);
                    }
                    View favIconRegion = actorView.findViewById(R.id.fav_icon_region);
                    favIconRegion.setTag(actor);
                    favIconRegion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onFavIconClick(view);
                        }
                    });

                    favIconHolders.add(iconHolder);
                    region.addView(actorView);

                    inflater.inflate(R.layout.list_separator, region, true);
                }
            } else {
                for (Pair<MovieActor, ImageView> iconHolder : favIconHolders) {
                    if (iconHolder.first.getFavourite() > 0) {
                        iconHolder.second.setImageResource(R.drawable.ic_list_fav_on);
                    } else {
                        iconHolder.second.setImageResource(R.drawable.ic_list_fav_off);
                    }
                }
            }

            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
        }
    }

    private void setDescription() {
        View region = pageView.findViewById(R.id.description_region);
        String description = state.movie.getDescription();
        if (description != null) {
            TextView divider = (TextView)region.findViewById(R.id.description_divider).findViewById(R.id.caption);
            divider.setText(context.getString(R.string.description_separator));
            TextView descriptionTextView = (TextView)region.findViewById(R.id.description);
            descriptionTextView.setText(description);
            region.setVisibility(View.VISIBLE);
        } else {
            region.setVisibility(View.GONE);
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

    public void onActorClick(View view) {
        MovieActor actor = (MovieActor)view.getTag();
        String cookie = UUID.randomUUID().toString();
        ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_ACTOR, null, null, actor, null);
        activitiesState.setState(cookie, state);

        Intent intent = new Intent(context, MovieListActivity.class);
        intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
        context.startActivity(intent);
    }
    
    private void onFavIconClick(View view) {
        MovieActor actor = (MovieActor)view.getTag();
        ImageView imageView = (ImageView)view.findViewById(R.id.fav_icon);
        if (actor.getFavourite() > 0) {
            actor.setFavourite(false);
            imageView.setImageResource(R.drawable.ic_list_fav_off);
        } else {
            actor.setFavourite(true);
            imageView.setImageResource(R.drawable.ic_list_fav_on);
        }
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
                        try {
                            progressDialog.cancel();
                        } catch (Exception e) {}
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
