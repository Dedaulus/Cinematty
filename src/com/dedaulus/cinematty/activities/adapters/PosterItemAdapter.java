package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Pair;
import android.view.Display;
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
import com.dedaulus.cinematty.framework.MoviePoster;
import com.dedaulus.cinematty.framework.PosterImageRetriever;
import com.dedaulus.cinematty.framework.tools.Constants;
import com.dedaulus.cinematty.framework.tools.DataConverter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private List<Cinema> closestCinemas;
    private boolean showSchedule;
    private LayoutInflater inflater;

    {
        closestCinemas = new ArrayList<Cinema>();
    }

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
        RelativeLayout trailerRegion = (RelativeLayout)overlay.findViewById(R.id.youtube_region);
        View scheduleOverlay = convertView.findViewById(R.id.schedule_overlay);

        MoviePoster poster = posters.get(position);
        textView.setText(poster.getMovie().getName());
        final String trailerUrl = poster.getTrailerUrl();
        if (trailerUrl.length() == 0) {
            trailerRegion.setVisibility(View.GONE);
        } else {
            trailerRegion.setVisibility(View.VISIBLE);
            trailerRegion.setOnClickListener(new View.OnClickListener() {
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

        if (showSchedule) {
            if (closestCinemas.isEmpty()) {
                scheduleOverlay.setVisibility(View.GONE);
            } else {
                Cinema cinema = closestCinemas.get(0);
                TextView scheduleTextView = (TextView)scheduleOverlay.findViewById(R.id.first_schedule);
                Pair<Movie, List<Calendar>> showTimes = cinema.getShowTimes(0).get(poster.getMovie().getName());
                Pair<Calendar, Calendar> timeRange = DataConverter.getTimeRange(Constants.WHOLE_DAY, 0);
                timeRange.first.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
                String showTimesStr = DataConverter.showTimesToString(showTimes == null ? null : showTimes.second, timeRange);
                if (showTimesStr.length() != 0) {
                    scheduleTextView.setText(showTimesStr);
                    scheduleOverlay.setVisibility(View.VISIBLE);
                } else {
                    scheduleOverlay.setVisibility(View.GONE);
                }
            }
        } else {
            scheduleOverlay.setVisibility(View.GONE);
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

    public void setClosestCinemas(List<Cinema> cinemas, boolean showSchedule) {
        closestCinemas = cinemas;
        this.showSchedule = showSchedule;
        notifyDataSetChanged();
    }
}
