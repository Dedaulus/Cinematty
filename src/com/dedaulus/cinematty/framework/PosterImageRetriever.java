package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;

import java.io.File;

/**
 * User: Dedaulus
 * Date: 11.02.12
 * Time: 16:15
 */
public class PosterImageRetriever {
    private ImageRetriever imageRetriever;

    public static interface PosterImageReceivedAction {
        void onImageReceived(boolean downloaded);
    }

    public PosterImageRetriever(String entity, File localFolder) throws ImageRetriever.ObjectAlreadyExists {
        imageRetriever = new ImageRetriever(entity, localFolder);
    }

    public Bitmap getImage(String url) {
        return imageRetriever.getImage(url);
    }

    public boolean hasImage(String url) {
        return imageRetriever.hasImage(url);
    }

    public void addRequest(String url, final PosterImageReceivedAction action) {
        imageRetriever.addRequest(url, true, new ImageRetriever.ImageReceivedAction() {
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
}
