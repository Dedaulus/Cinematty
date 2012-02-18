package com.dedaulus.cinematty.framework.tools;

import android.app.Activity;
import com.dedaulus.cinematty.framework.MovieImageRetriever;

/**
 * User: Dedaulus
 * Date: 11.09.11
 * Time: 18:16
 */
public class MovieImageReceivedActionHandler implements MovieImageRetriever.MovieImageReceivedAction {
    private MovieImageRetriever.MovieImageReceivedAction action;
    private Activity activity;
    private volatile Boolean imageReceived = false;
    private volatile Boolean stopped = true;

    public MovieImageReceivedActionHandler(MovieImageRetriever.MovieImageReceivedAction action, Activity activity) {
        this.action = action;
        this.activity = activity;
    }

    public synchronized void start() {
        if (!stopped) return;
        stopped = false;

        new Thread(new Runnable() {
            public void run() {
                while (!stopped) {
                    if (imageReceived) {
                        imageReceived = false;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                action.onImageReceived(true);
                            }
                        });
                    }


                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    public void stop() {
        stopped = true;
    }

    public void onImageReceived(boolean success) {
        imageReceived = true;
    }
}
