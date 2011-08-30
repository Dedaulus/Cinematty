package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MoviePoster;
import com.dedaulus.cinematty.framework.PictureRetriever;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 22.08.11
 * Time: 3:46
 */
public class PosterItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<MoviePoster> mPosters;
    private PictureRetriever mPictureRetriever;
    private static final int EXTRA_SPACE = 8;

    public PosterItemAdapter(Context c, List<MoviePoster> posters, PictureRetriever pictureRetriever) {
        mContext = c;
        mPosters = posters;
        mPictureRetriever = pictureRetriever;
    }

    public int getCount() {
        return mPosters.size();
    }

    public Object getItem(int position) {
        return mPosters.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setAdjustViewBounds(false);

            Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = width / 3;

            imageView.setLayoutParams(new GridView.LayoutParams(width, height));
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.img_loading);

        final MoviePoster poster = mPosters.get(position);
        new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = mPictureRetriever.downloadPicture(poster.getPosterPath());
                imageView.post(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();

        return imageView;
    }
}
