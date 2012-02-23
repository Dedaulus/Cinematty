package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.FrameImageRetriever;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 14:34
 */
public class FrameItemAdapter extends BaseAdapter implements StoppableAndResumable {
    private static class ImageRequest implements FrameImageRetriever.FrameImageReceivedAction {
        FrameImageRetriever imageRetriever;
        String uid;
        int id;
        ImageView imageView;
        int desiredWidth;
        final Map<Pair<String, Integer>, Bitmap> cachedImages;
        Activity activity;
        
        ImageRequest(FrameImageRetriever imageRetriever, String uid, int id, Map<Pair<String, Integer>, Bitmap> cachedImages, ImageView imageView, int desiredWidth, Activity activity) {
            this.imageRetriever = imageRetriever;
            this.uid = uid;
            this.id = id;
            this.cachedImages = cachedImages;
            this.imageView = imageView;
            this.desiredWidth = desiredWidth;
            this.activity = activity;
        }

        @Override
        public void onImageReceived(final Bitmap image) {
            final FrameImageRetriever.FrameImageReceivedAction action = this;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (image != null) {
                        Bitmap newImage = createScaledBitmap(image, desiredWidth);
                        synchronized (cachedImages) {
                            cachedImages.put(Pair.create(uid, id), newImage);
                        }
                        imageView.setImageBitmap(newImage);
                    } else {
                        imageRetriever.addRequest(uid, id, true, action);
                    }
                }
            });
        }
    }
    
    private Context context;
    private MovieFrameIdsStore frameIdsStore;
    private FrameImageRetriever imageRetriever;
    private int imageWidth;
    
    private final Map<Pair<String, Integer>, Bitmap> cachedImages;

    {
        cachedImages = new HashMap<Pair<String, Integer>, Bitmap>();
    }

    public FrameItemAdapter(Context context, MovieFrameIdsStore frameIdsStore, FrameImageRetriever imageRetriever) {
        this.context = context;
        this.frameIdsStore = frameIdsStore;
        this.imageRetriever = imageRetriever;

        int columns;
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            columns = 1;
        } else {
            columns = 2;
        }

        imageWidth = display.getWidth() / columns;
    }

    public int getCount() {
        return frameIdsStore.getFrameIds().size();
    }

    public Object getItem(int position) {
        return null;
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
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }
        
        int frameId = frameIdsStore.getFrameIds().get(position);

        Bitmap bitmap;
        synchronized (cachedImages) {
            bitmap = cachedImages.get(Pair.create(frameIdsStore.getUid(), frameId));
        }

        if (bitmap == null) {
            bitmap = imageRetriever.getImage(frameIdsStore.getUid(), frameId, true);
            if (bitmap == null) {
                imageRetriever.addRequest(frameIdsStore.getUid(), frameId, true, new ImageRequest(imageRetriever, frameIdsStore.getUid(), frameId, cachedImages, imageView, imageWidth, (Activity)context));
                imageView.setImageResource(R.drawable.img_loading);
            } else {
                bitmap = createScaledBitmap(bitmap, imageWidth);
                synchronized (cachedImages) {
                    cachedImages.put(Pair.create(frameIdsStore.getUid(), frameId), bitmap);
                }
                imageView.setImageBitmap(bitmap);
            }
        } else {
            imageView.setImageBitmap(bitmap);
        }

        return imageView;
    }

    @Override
    public void onStop() {
        imageRetriever.saveState();
        synchronized (cachedImages) {
            cachedImages.clear();
        }
    }

    @Override
    public void onResume() {}
    
    private static Bitmap createScaledBitmap(Bitmap image, int desiredWidth) {
        int height = image.getHeight();
        int width = image.getWidth();
        double multiplier = (double)desiredWidth / width;

        return Bitmap.createScaledBitmap(image, (int)(width * multiplier), (int)(height * multiplier), false);
    }
}
