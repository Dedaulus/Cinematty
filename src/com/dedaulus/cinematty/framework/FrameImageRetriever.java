package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import com.dedaulus.cinematty.framework.ImageRetriever;

import java.io.File;
import java.util.Locale;

/**
 * User: Dedaulus
 * Date: 12.02.12
 * Time: 1:55
 */
public class FrameImageRetriever {
    private static String FRAME_PREFIX = "frame_";
    private static String PREVIEW_PREFIX = "preview_";
    private static String POSTFIXES[] = {"_m.jpg", "_h.jpg", "_xh.jpg"};
    
    private static final int MEDIUM_SIZE = 700;
    private static final int BIG_SIZE    = 1000;

    private String remoteFolder;
    private int postfixId;
    private ImageRetriever imageRetriever;

    public static interface FrameImageReceivedAction {
        void onImageReceived(Bitmap image);
    }

    public FrameImageRetriever(String entity, DisplayMetrics displayMetrics, String remoteFolder, File localFolder) throws ImageRetriever.ObjectAlreadyExists {
        if (!remoteFolder.endsWith("/")) {
            remoteFolder += "/";
        }
        this.remoteFolder = remoteFolder;
        
        int maxSize = displayMetrics.heightPixels;
        if (maxSize < displayMetrics.widthPixels) maxSize = displayMetrics.widthPixels;
        
        if (maxSize < MEDIUM_SIZE) {
            postfixId = 0;
        } else if (maxSize < BIG_SIZE) {
            postfixId = 1;
        } else {
            postfixId = 2;
        }

        imageRetriever = new ImageRetriever(entity, localFolder);
    }

    public Bitmap getImage(String uid, int frameId, boolean isPreview) {
        return imageRetriever.getImage(createUrl(uid, frameId, isPreview));
    }

    public boolean hasImage(String uid, int frameId, boolean isPreview) {
        return imageRetriever.hasImage(createUrl(uid, frameId, isPreview));
    }

    public void addRequest(String uid, int frameId, boolean isPreview, final FrameImageReceivedAction action) {
        imageRetriever.addRequest(createUrl(uid, frameId, isPreview), false, new ImageRetriever.ImageReceivedAction() {
            @Override
            public void onImageReceived(String url, boolean downloaded) {
                action.onImageReceived(imageRetriever.getImage(url));
            }
        });
    }

    public void saveState() {
        imageRetriever.saveState();
    }

    public void stop() {
        imageRetriever.stop();
    }

    public void pause() {
        imageRetriever.pause();
    }

    public void resume() {
        imageRetriever.resume();
    }
    
    private String createUrl(String uid, int frameId, boolean isPreview) {
        StringBuilder builder = new StringBuilder(remoteFolder).append(uid).append("/").append(isPreview ? PREVIEW_PREFIX : FRAME_PREFIX).append(frameId).append(POSTFIXES[postfixId]);
        return builder.toString();
    }
}
