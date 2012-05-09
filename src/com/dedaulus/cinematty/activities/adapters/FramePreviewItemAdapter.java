package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.FrameImageRetriever;
import com.dedaulus.cinematty.framework.MovieFrameIdsStore;
import com.dedaulus.cinematty.framework.tools.IdleDataSetChangeNotifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * User: Dedaulus
 * Date: 23.02.12
 * Time: 14:34
 */
public class FramePreviewItemAdapter extends BaseAdapter implements StoppableAndResumable {
    private Context context;
    private LayoutInflater inflater;
    private IdleDataSetChangeNotifier notifier;
    private MovieFrameIdsStore frameIdsStore;
    private FrameImageRetriever imageRetriever;
    private int screenWidth;

    private final Map<Pair<String, Integer>, Bitmap> cachedImages;

    {
        cachedImages = new HashMap<Pair<String, Integer>, Bitmap>();
    }

    public FramePreviewItemAdapter(Context context, IdleDataSetChangeNotifier notifier, MovieFrameIdsStore frameIdsStore, FrameImageRetriever imageRetriever) {
        this.context = context;
        this.frameIdsStore = frameIdsStore;
        this.notifier = notifier;
        notifier.setAdapter(this);
        this.imageRetriever = imageRetriever;
        inflater = LayoutInflater.from(context);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
    }

    public int getCount() {
        return frameIdsStore.getFrameIds().size();
    }

    public Object getItem(int position) {
        return frameIdsStore.getFrameIds().get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.frame_preview_item, null);
        }

        final ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
        final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progress);

        final int frameId = frameIdsStore.getFrameIds().get(position);
        final Pair<String, Integer> cachedImageKey = Pair.create(frameIdsStore.getUid(), frameId);
        Bitmap bitmap;
        synchronized (cachedImages) {
            bitmap = cachedImages.get(cachedImageKey);
        }

        if (bitmap == null) {
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            if (imageRetriever.hasImage(frameIdsStore.getUid(), frameId, true)) {
                if (notifier.isIdle()) {
                    try {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Bitmap bitmap = imageRetriever.getImage(frameIdsStore.getUid(), frameId, true);
                                synchronized (cachedImages) {
                                    cachedImages.put(cachedImageKey, bitmap);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                notifier.askForNotifyDataSetChanged();
                            }
                        }.execute();
                    } catch (RejectedExecutionException e) {
                        // crutch
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {}
                                ((Activity)context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        notifier.askForNotifyDataSetChanged();
                                    }
                                });
                            }
                        });
                    }
                } else {
                    notifier.askForNotifyDataSetChanged();
                }
            } else {
                imageRetriever.addRequest(frameIdsStore.getUid(), frameId, true, new FrameImageRetriever.FrameImageReceivedAction() {
                    @Override
                    public void onImageReceived(boolean downloaded) {
                        if (downloaded) {
                            /*
                            synchronized (cachedImages) {
                                Bitmap bitmap = cachedImages.get(cachedImageKey);
                                if (bitmap == null) {
                                    bitmap = imageRetriever.getImage(frameIdsStore.getUid(), frameId, true);
                                    cachedImages.put(cachedImageKey, bitmap);
                                }
                                ((Activity)context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        notifier.askForNotifyDataSetChanged();
                                    }
                                });
                            }
                            */
                            ((Activity)context).runOnUiThread(new Runnable() {
                                public void run() {
                                    notifier.askForNotifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Pair<Integer, Integer> sizeHeightWidth = getProperImageSize(bitmap);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(sizeHeightWidth.second, sizeHeightWidth.first));
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        return convertView;
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
    
    private Pair<Integer, Integer> getProperImageSize(Bitmap bitmap) {
        double heightMultiplier = (double)screenWidth / bitmap.getWidth();
        return Pair.create((int)(bitmap.getHeight() * heightMultiplier), screenWidth);
    }
}
