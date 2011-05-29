package com.dedaulus.cinematty.framework.tools;

/**
 * User: Dedaulus
 * Date: 08.05.11
 * Time: 18:58
 */
public interface PictureReceiver {
    void onPictureReceive(String picId, int pictureType, boolean success);
}
