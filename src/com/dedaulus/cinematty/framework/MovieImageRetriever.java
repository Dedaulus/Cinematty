package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.io.File;

/**
 * User: Dedaulus
 * Date: 11.02.12
 * Time: 0:15
 */
public class MovieImageRetriever {
    private static final String IMAGE_NAME = "pic.jpg";
    private static final String SMALL_IMAGE_NAMES[] = {"pic_m.jpg", "pic_h.jpg", "pic_xh.jpg"};
    private String smallImageName;
    private String remoteFolder;
    private ImageRetriever imageRetriever;

    public static interface MovieImageReceivedAction {
        void onImageReceived(boolean downloaded);
    }

    public MovieImageRetriever(String entity, DisplayMetrics displayMetrics, String remoteFolder, File localFolder) throws ImageRetriever.ObjectAlreadyExists {
        final int DENSITY_XHIGH = 320; // developer.android.com/reference/android/util/DisplayMetrics.html#DENSITY_XHIGH
        switch (displayMetrics.densityDpi) {
            case DENSITY_XHIGH:
                smallImageName = SMALL_IMAGE_NAMES[2];
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                smallImageName = SMALL_IMAGE_NAMES[0];
                break;
            case DisplayMetrics.DENSITY_HIGH:
            default:
                smallImageName = SMALL_IMAGE_NAMES[1];
        }

        this.remoteFolder = remoteFolder;
        if (!this.remoteFolder.endsWith("/")) {
            this.remoteFolder += "/";
        }
        
        imageRetriever = ImageRetriever.create(entity, localFolder);
    }
    
    public Bitmap getImage(String uid, boolean small) {
        return imageRetriever.getImage(createImageUrl(uid, small));
    }
    
    public boolean hasImage(String uid, boolean small) {
        return imageRetriever.hasImage(createImageUrl(uid, small));        
    }
    
    public void addRequest(String uid, boolean small, final MovieImageReceivedAction action) {
        String url = createImageUrl(uid, small);

        imageRetriever.addRequest(url, new ImageRetriever.ImageReceivedAction() {
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
    
    private String createImageUrl(String uid, boolean small) {
        StringBuilder builder = new StringBuilder(remoteFolder);
        builder.append(uid).append("/");
        if (small) {
            builder.append(smallImageName);
        } else {
            builder.append(IMAGE_NAME);
        }
        return builder.toString();
    }
}
