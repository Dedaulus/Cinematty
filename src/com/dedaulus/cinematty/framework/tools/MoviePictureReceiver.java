package com.dedaulus.cinematty.framework.tools;

import android.app.Activity;

/**
 * User: Dedaulus
 * Date: 11.09.11
 * Time: 18:16
 */
public class MoviePictureReceiver implements PictureReceiver {
    private OnPictureReceiveAction mAction;
    private Activity mActivity;
    private Boolean mPictureReceived = false;
    private Boolean mStopped = true;

    private final Object mPictureReceivedMutex = new Object();
    private final Object mStoppedMutex = new Object();

    public MoviePictureReceiver(OnPictureReceiveAction action, Activity activity) {
        mAction = action;
        mActivity = activity;
    }

    public void start() {
        synchronized (mStoppedMutex) {
            if (!mStopped) return;
            mStopped = false;
        }

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (mStoppedMutex) {
                        if (mStopped) break;
                    }

                    synchronized (mPictureReceivedMutex) {
                        if (mPictureReceived) {
                            mPictureReceived = false;
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    mAction.OnPictureReceive(null, 0, true);
                                }
                            });
                        }
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    public void stop() {
        synchronized (mStoppedMutex) {
            mStopped = true;
        }
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        synchronized (mPictureReceivedMutex) {
            mPictureReceived = true;
        }
    }
}
