package com.dedaulus.cinematty.activities.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.*;
import android.widget.*;
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
public class FrameItemAdapter extends BaseAdapter implements StoppableAndResumable, AbsListView.OnScrollListener {
    private Context context;
    LayoutInflater inflater;
    private MovieFrameIdsStore frameIdsStore;
    private FrameImageRetriever imageRetriever;
    private int screenWidth;
    private volatile boolean notifyDataSetChangedAsked;
    private volatile boolean idle = true;

    private final Map<Pair<String, Integer>, Bitmap> cachedImages;

    {
        cachedImages = new HashMap<Pair<String, Integer>, Bitmap>();
    }

    public FrameItemAdapter(Context context, MovieFrameIdsStore frameIdsStore, FrameImageRetriever imageRetriever) {
        this.context = context;
        this.frameIdsStore = frameIdsStore;
        this.imageRetriever = imageRetriever;
        inflater = LayoutInflater.from(context);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
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

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.frame_item, null);
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
                        //notifyDataSetChanged();
                        askForNotifyDataSetChanged();
                    }
                }.execute();
            } else {
                imageRetriever.addRequest(frameIdsStore.getUid(), frameId, true, new FrameImageRetriever.FrameImageReceivedAction() {
                    @Override
                    public void onImageReceived(boolean downloaded) {
                        if (downloaded) {
                            synchronized (cachedImages) {
                                Bitmap bitmap = cachedImages.get(cachedImageKey);
                                if (bitmap == null) {
                                    bitmap = imageRetriever.getImage(frameIdsStore.getUid(), frameId, true);
                                    cachedImages.put(cachedImageKey, bitmap);
                                }
                                ((Activity)context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        //notifyDataSetChanged();
                                        askForNotifyDataSetChanged();
                                    }
                                });
                            }
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

    private void askForNotifyDataSetChanged() {
        if (idle) {
            notifyDataSetChanged();
        } else {
            notifyDataSetChangedAsked = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        idle = scrollState == SCROLL_STATE_IDLE;
        if (idle) {
            if (notifyDataSetChangedAsked) {
                notifyDataSetChangedAsked = false;
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
