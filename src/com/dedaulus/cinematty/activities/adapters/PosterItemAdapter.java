package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.dedaulus.cinematty.R;

/**
 * User: Dedaulus
 * Date: 22.08.11
 * Time: 3:46
 */
public class PosterItemAdapter extends BaseAdapter {
    private Context mContext;
    private static final int EXTRA_SPACE = 8;

    public PosterItemAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 5;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            //LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            //imageView = (ImageView)layoutInflater.inflate(R.layout.poster_item, null, false);
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            int picWidth;
            int picHeight;

            if (height > width) {
                picWidth = width - EXTRA_SPACE;
                picHeight = picWidth / 2 - EXTRA_SPACE / 2;
            } else {
                picWidth = width / 2 - EXTRA_SPACE;
                picHeight = picWidth / 2 - EXTRA_SPACE / 2;
            }
            //imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(picWidth, picHeight));
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.hitman);
        return imageView;
    }
}
