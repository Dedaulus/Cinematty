package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.io.File;

/**
 * User: Dedaulus
 * Date: 11.02.12
 * Time: 16:15
 */
public class PosterImageRetriever {
    private static final String IMAGE_NAMES[] = {"pic_m.jpg", "pic_h.jpg", "pic_xh.jpg"};

    private static final int MEDIUM_SIZE = 700;
    private static final int BIG_SIZE    = 1000;

    private String remoteFolder;
    private String imageName;
    private ImageRetriever imageRetriever;

    public static interface PosterImageReceivedAction {
        void onImageReceived(boolean downloaded);
    }

    public PosterImageRetriever(String entity, DisplayMetrics displayMetrics, String remoteFolder, File localFolder) throws ImageRetriever.ObjectAlreadyExists {
        if (!remoteFolder.endsWith("/")) {
            remoteFolder += "/";
        }
        this.remoteFolder = remoteFolder;

        int maxSize = displayMetrics.heightPixels;
        if (maxSize < displayMetrics.widthPixels) maxSize = displayMetrics.widthPixels;

        if (maxSize < MEDIUM_SIZE) {
            imageName = IMAGE_NAMES[0];
        } else if (maxSize < BIG_SIZE) {
            imageName = IMAGE_NAMES[1];
        } else {
            imageName = IMAGE_NAMES[2];
        }

        imageRetriever = ImageRetriever.create(entity, localFolder);
    }

    public Bitmap getImage(String uid) {
        return imageRetriever.getImage(createImageUrl(uid));
    }

    public boolean hasImage(String uid) {
        return imageRetriever.hasImage(createImageUrl(uid));
    }

    public void addRequest(String uid, final PosterImageReceivedAction action) {
        String url = createImageUrl(uid);

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

    private String createImageUrl(String uid) {
        return new StringBuilder(remoteFolder).append(uid).append("/").append(imageName).toString();
    }
}
