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
import com.dedaulus.cinematty.framework.PosterImageRetriever;

import java.util.ArrayList;

/**
 * User: Dedaulus
 * Date: 22.08.11
 * Time: 3:46
 */
public class PosterItemAdapter extends BaseAdapter implements PosterImageRetriever.PosterImageReceivedAction, StoppableAndResumable {
    private Context context;
    private ArrayList<MoviePoster> posters;
    private PosterImageRetriever imageRetriever;

    public PosterItemAdapter(Context context, ArrayList<MoviePoster> posters, PosterImageRetriever imageRetriever) {
        this.context = context;
        this.posters = posters;
        this.imageRetriever = imageRetriever;
    }

    public int getCount() {
        return posters.size();
    }

    public Object getItem(int position) {
        return posters.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setAdjustViewBounds(false);

            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = width / 3;

            imageView.setLayoutParams(new GridView.LayoutParams(width, height));
        } else {
            imageView = (ImageView) convertView;
        }

        MoviePoster poster = posters.get(position);
        Bitmap bitmap = imageRetriever.getImage(poster.getPosterPath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageRetriever.addRequest(poster.getPosterPath(), this);
            imageView.setImageResource(R.drawable.img_loading);
        }

        return imageView;
    }

    public void onImageReceived(boolean success) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        imageRetriever.pause();
        imageRetriever.saveState();
    }

    @Override
    public void onResume() {
        imageRetriever.resume();
    }
}
