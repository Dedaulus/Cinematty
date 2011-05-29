package com.dedaulus.cinematty.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieGenre;
import com.dedaulus.cinematty.framework.PictureRetriever;
import com.dedaulus.cinematty.framework.tools.PictureReceiver;
import com.dedaulus.cinematty.framework.tools.PictureType;
import com.dedaulus.cinematty.framework.tools.UpdatableByNeed;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 14.03.11
 * Time: 23:40
 */
public class MovieItemAdapter extends BaseAdapter implements PictureReceiver, UpdatableByNeed {
    private Context mContext;
    private List<Movie> mMovies;
    private PictureRetriever mPictureRetriever;
    private boolean mPicturesUpdated = false;

    private AsyncTask<UpdatableByNeed, UpdatableByNeed, Void> mPictureUpdater;

    public MovieItemAdapter(Context context, List<Movie> movies, PictureRetriever pictureRetriever) {
        mContext = context;
        mMovies = movies;
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
        return layoutInflater.inflate(R.layout.movie_item, parent, false);
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

        TextView text = (TextView)view.findViewById(R.id.movie_caption_in_movie_list);
        text.setText(movie.getCaption());

        text = (TextView)view.findViewById(R.id.movie_genre_in_movie_list);
        if (movie.getGenres().size() != 0) {
            StringBuilder genres = new StringBuilder();
            for (MovieGenre genre : movie.getGenres()) {
                genres.append(genre.getGenre() + "/");
            }
            genres.delete(genres.length() - 1, genres.length());
            text.setText(genres.toString());

            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
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
