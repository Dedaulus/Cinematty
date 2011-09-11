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
    private Boolean mStopped = false;

    public MoviePictureReceiver(OnPictureReceiveAction action, Activity activity) {
        mAction = action;
        mActivity = activity;

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (mStopped) {
                        if (mStopped) break;
                    }

                    synchronized (mPictureReceived) {
                        if (mPictureReceived) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    mAction.OnPictureReceive(null, 0, true);
                                }
                            });
                            mPictureReceived = false;
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
        synchronized (mStopped) {
            mStopped = true;
        }
    }

    public void onPictureReceive(String picId, int pictureType, boolean success) {
        synchronized (mPictureReceived) {
            mPictureReceived = true;
        }
    }
}
