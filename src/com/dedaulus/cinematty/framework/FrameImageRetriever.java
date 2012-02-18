package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
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
    
    private String frameNameFormat;
    private String previewNameFormat;
    private ImageRetriever imageRetriever;

    public static interface FrameImageReceivedAction {
        void onImageReceived(boolean downloaded);
    }

    public FrameImageRetriever(String entity, int densityDpi, String remoteFolder, File localFolder) throws ImageRetriever.ObjectAlreadyExists {
        if (!remoteFolder.endsWith("/")) {
            remoteFolder += "/";
        }

        StringBuilder builder = new StringBuilder(remoteFolder);
        int postfixId;
        final int DENSITY_XHIGH = 320; // developer.android.com/reference/android/util/DisplayMetrics.html#DENSITY_XHIGH
        switch (densityDpi) {
            case DENSITY_XHIGH:
                postfixId = 2;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                postfixId = 0;
                break;
            case DisplayMetrics.DENSITY_HIGH:
            default:
                postfixId = 1;
        }
        frameNameFormat = builder.append("%1$/").append(FRAME_PREFIX).append("%2$").append(POSTFIXES[postfixId]).toString();
        previewNameFormat = builder.append("%1$/").append(PREVIEW_PREFIX).append("%2$").append(POSTFIXES[postfixId]).toString();

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
                action.onImageReceived(downloaded);
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
        return String.format(Locale.US, isPreview ? previewNameFormat : frameNameFormat, uid, frameId);
    }
}
