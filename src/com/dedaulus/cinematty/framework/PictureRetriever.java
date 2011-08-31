package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import com.dedaulus.cinematty.framework.tools.PictureReceiver;

/**
 * User: Dedaulus
 * Date: 28.08.11
 * Time: 23:10
 */
public interface PictureRetriever {
    void setRemotePictureFolder(String remotePictureFolder);
    void addRequest(String picId, int pictureType, PictureReceiver receiver);
    Bitmap downloadPicture(String picId, int pictureType);
    Bitmap downloadPicture(String remotePicturePath);
    boolean hasPicture(String picId, int pictureType);
    Bitmap getPicture(String picId, int pictureType);
}
