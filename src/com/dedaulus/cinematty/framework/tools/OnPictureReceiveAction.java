package com.dedaulus.cinematty.framework.tools;

/**
 * User: Dedaulus
 * Date: 11.09.11
 * Time: 18:19
 */
public interface OnPictureReceiveAction {
    void OnPictureReceive(String picId, int pictureType, boolean success);
}
