package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.FrameImageRetriever;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;

import java.util.BitSet;

/**
 * User: Dedaulus
 * Date: 18.03.12
 * Time: 21:08
 */
public class FrameItemAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    MovieFrameIdsStore frameIdsStore;
    FrameImageRetriever imageRetriever;
    private int screenWidth;
    private int screenHeight;

    public FrameItemAdapter(Context context, MovieFrameIdsStore frameIdsStore, FrameImageRetriever imageRetriever) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.frameIdsStore = frameIdsStore;
        this.imageRetriever = imageRetriever;

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }

    @Override
    public int getCount() {
        return frameIdsStore.getFrameIds().size();
    }

    @Override
    public Object getItem(int position) {
        return frameIdsStore.getFrameIds().get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.frame_item, null);
        }

        final ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
        final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progress);

        final int frameId = frameIdsStore.getFrameIds().get(position);

        Bitmap bitmap = imageRetriever.getImage(frameIdsStore.getUid(), frameId, false);
        if (bitmap == null) {
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            imageRetriever.addRequest(frameIdsStore.getUid(), frameId, false, new FrameImageRetriever.FrameImageReceivedAction() {
                @Override
                public void onImageReceived(boolean downloaded) {
                    if (downloaded) {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        } else {
            Pair<Integer, Integer> sizeHeightWidth = getProperImageSize(bitmap);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(sizeHeightWidth.second, sizeHeightWidth.first));
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
        
        return convertView;
    }

    private Pair<Integer, Integer> getProperImageSize(Bitmap bitmap) {
        double heightMultiplier = (double)screenWidth / bitmap.getWidth();
        int height = (int)(bitmap.getHeight() * heightMultiplier);
        if (height < screenHeight) {
            return Pair.create(height, screenWidth);
        }
        double widthMultiplier = (double)screenHeight / height;
        int width = (int)(screenWidth * widthMultiplier);
        return Pair.create(screenHeight, width);
    }
}
