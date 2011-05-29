package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.DataConverter;
import com.dedaulus.cinematty.framework.tools.PictureReceiver;
import com.dedaulus.cinematty.framework.tools.PictureType;
import com.dedaulus.cinematty.framework.tools.UpdatableByNeed;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MovieItemWithScheduleAdapter extends BaseAdapter implements PictureReceiver, UpdatableByNeed {
    private Context mContext;
    private List<Movie> mMovies;
    private Cinema mCinema;
    private PictureRetriever mPictureRetriever;
    private boolean mPicturesUpdated = false;

    private AsyncTask<UpdatableByNeed, UpdatableByNeed, Void> mPictureUpdater;

    public MovieItemWithScheduleAdapter(Context context, List<Movie> movies, Cinema cinema, PictureRetriever pictureRetriever) {
        mContext = context;
        mMovies = movies;
        mCinema = cinema;
        mPictureRetriever = pictureRetriever;

        mPictureUpdater = new AsyncTask<UpdatableByNeed, UpdatableByNeed, Void>() {
            @Override
            protected Void doInBackground(UpdatableByNeed... updatableByNeeds) {
                while (true) {
                    if (updatableByNeeds[0].isUpdateNeeded()) {
                        publishProgress(updatableByNeeds[0]);
                    }

                    try {
                        Thread.sleep(2000);
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

    public int getCount() {
        return mMovies.size();
    }

    public Object getItem(int i) {
        return i >= 0 && i < mMovies.size() ? mMovies.get(i) : null;
    }

    public long getItemId(int i) {
        return i;
    }

    private View newView(Context context, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.movie_item_w_schedule, parent, false);
    }

    private void bindView(int position, View view) {
        Movie movie = mMovies.get(position);

        RelativeLayout progressBar = (RelativeLayout)view.findViewById(R.id.movie_list_icon_loading);
        ImageView imageView = (ImageView)view.findViewById(R.id.movie_list_icon);

        imageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        String picId = movie.getPicId();
        if (picId != null) {
            Bitmap picture = mPictureRetriever.getPicture(picId, PictureType.LIST_BIG);
            if (picture != null) {
                imageView.setImageBitmap(picture);
                imageView.setVisibility(View.VISIBLE);
            } else {
                mPictureRetriever.addRequest(picId, PictureType.LIST_BIG, this);
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_blank_movie);
            imageView.setVisibility(View.VISIBLE);
        }

        TextView captionView = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        captionView.setText(movie.getCaption());

        TextView genreView = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (movie.getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : movie.getGenres()) {
                genres.append(genre.getGenre() + "/");
            }

            genres.delete(genres.length() - 1, genres.length());
            genreView.setText(genres.toString());

            genreView.setVisibility(View.VISIBLE);
        } else {
            genreView.setVisibility(View.GONE);
        }

        TextView scheduleView = (TextView)view.findViewById(R.id.movie_schedule_in_movie_list);
        TextView timeLeftView = (TextView)view.findViewById(R.id.time_left_in_movie_list);

        List<Calendar> showTimes = mCinema.getShowTimes().get(movie);
        if (showTimes != null) {
            String showTimesStr = DataConverter.showTimesToString(showTimes);
            if (showTimesStr.length() != 0) {
                scheduleView.setText(showTimesStr);
                scheduleView.setVisibility(View.VISIBLE);
            } else {
                scheduleView.setVisibility(View.GONE);
            }

            SpannableString timeLeftString = null;
            if (showTimes.size() != 0) {
                Calendar now = Calendar.getInstance();
                Calendar closestTime = getClosestTime(showTimes, now);
                if (closestTime == null) {
                    timeLeftString = new SpannableString(mContext.getString(R.string.no_schedule) + " ");
                    timeLeftString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, mContext.getString(R.string.no_schedule).length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (closestTime.equals(now)) {
                    timeLeftString = new SpannableString(mContext.getString(R.string.schedule_now));
                    timeLeftString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, mContext.getString(R.string.schedule_now).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    Calendar leftTime = (Calendar)closestTime.clone();
                    leftTime.add(Calendar.HOUR_OF_DAY, -now.get(Calendar.HOUR_OF_DAY));
                    leftTime.add(Calendar.MINUTE, -now.get(Calendar.MINUTE));

                    String str = DataConverter.timeToTimeLeft(mContext, leftTime);
                    timeLeftString = new SpannableString(str.toString());
                    timeLeftString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                timeLeftString = new SpannableString(mContext.getString(R.string.no_schedule) + " ");
                timeLeftString.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, mContext.getString(R.string.no_schedule).length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            timeLeftView.setText(timeLeftString);

        } else {
            scheduleView.setVisibility(View.GONE);
            timeLeftView.setText(mContext.getString(R.string.no_schedule));
        }

    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = null;

        if (view != null) {
            myView = view;
        } else {
            myView = newView(mContext, viewGroup);
        }

        bindView(i, myView);

        return myView;
    }

    private Calendar getClosestTime(List<Calendar> showTimes, Calendar time) {
        int id = Collections.binarySearch(showTimes, time, new Comparator<Calendar>() {
            public int compare(Calendar o1, Calendar o2) {
                int day1 = o1.get(Calendar.DAY_OF_YEAR);
                int day2 = o2.get(Calendar.DAY_OF_YEAR);

                if (day1 < day2) return -1;
                else if (day1 > day2) return 1;
                else {
                    int hour1 = o1.get(Calendar.HOUR_OF_DAY);
                    int hour2 = o2.get(Calendar.HOUR_OF_DAY);

                    if (hour1 < hour2) return -1;
                    else if (hour1 > hour2) return 1;
                    else {
                        int minute1 = o1.get(Calendar.MINUTE);
                        int minute2 = o2.get(Calendar.MINUTE);

                        if (minute1 < minute2) return -1;
                        else if (minute1 > minute2) return 1;
                        else return 0;
                    }
                }
            }
        });

        if (id >= 0) return time;
        else {
            id = -(id + 1);
            if (id == showTimes.size()) return null;
            else return showTimes.get(id);
        }
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        mPicturesUpdated = success;
    }

    public boolean isUpdateNeeded() {
        return mPicturesUpdated;
    }

    public void update() {
        notifyDataSetChanged();
        mPicturesUpdated = false;
    }
}
