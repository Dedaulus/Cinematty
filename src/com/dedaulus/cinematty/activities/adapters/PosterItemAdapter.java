package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
    private int imageHeight;
    private int imageWidth;
    private LayoutInflater inflater;

    public PosterItemAdapter(Context context, ArrayList<MoviePoster> posters, PosterImageRetriever imageRetriever) {
        this.context = context;
        this.posters = posters;
        this.imageRetriever = imageRetriever;
        inflater = LayoutInflater.from(context);

        int columns;
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        if (display.getWidth() < display.getHeight()) {
            columns = 1;
        } else {
            columns = 2;
        }

        imageWidth = display.getWidth() / columns;
        imageHeight = imageWidth / 3;
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.poster_item, null);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(imageWidth, imageHeight));
        View overlay = convertView.findViewById(R.id.overlay);
        TextView textView = (TextView)overlay.findViewById(R.id.caption);
        ImageView trailerIcon = (ImageView)overlay.findViewById(R.id.youtube);

        MoviePoster poster = posters.get(position);
        textView.setText(poster.getMovie().getName());
        final String trailerUrl = poster.getTrailerUrl();
        if (trailerUrl.length() == 0) {
            trailerIcon.setVisibility(View.GONE);
        } else {
            trailerIcon.setVisibility(View.VISIBLE);
            trailerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(trailerUrl));
                    context.startActivity(intent);
                }
            });
        }

        Bitmap bitmap = imageRetriever.getImage(poster.getPicId());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            overlay.setVisibility(View.VISIBLE);
        } else {
            imageRetriever.addRequest(poster.getPicId(), this);
            imageView.setImageResource(R.drawable.img_loading);
            overlay.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void onImageReceived(boolean success) {
        Activity activity = (Activity)context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        imageRetriever.saveState();
    }

    @Override
    public void onResume() {}
}
