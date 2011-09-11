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
    private Boolean mStarted = false;
    private Boolean mStopped = false;

    public MoviePictureReceiver(OnPictureReceiveAction action, Activity activity) {
        mAction = action;
        mActivity = activity;
    }

    public void start() {
        synchronized (mStarted) {
            if (mStarted) return;
        }

        synchronized (mStopped) {
            mStopped = false;
        }

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (mStopped) {
                        if (mStopped) break;
                    }

                    synchronized (mPictureReceived) {
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

        synchronized (mStarted) {
            mStarted = true;
        }
    }

    public void stop() {
        synchronized (mStopped) {
            mStopped = true;
        }

        synchronized (mStarted) {
            mStarted = false;
        }
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        synchronized (mPictureReceived) {
            mPictureReceived = true;
        }
    }
}
