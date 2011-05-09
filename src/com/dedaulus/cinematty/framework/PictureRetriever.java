package com.dedaulus.cinematty.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;
import com.dedaulus.cinematty.framework.tools.PictureReceiver;
import com.dedaulus.cinematty.framework.tools.PictureType;
import com.dedaulus.cinematty.framework.tools.UniqueSortedList;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * User: Dedaulus
 * Date: 08.05.11
 * Time: 18:40
 */
public class PictureRetriever implements Runnable {
    private static final int APROX_PICS_COUNT = 80; // This is a nearly count of moscow movies at once

    Context mContext;
    String mRemotePictureFolder;
    String mLocalPictureFolder;

    Queue<Pair<Pair<String, Integer>, PictureReceiver>> mTaskQueue = new LinkedList<Pair<Pair<String, Integer>, PictureReceiver>>();
    List<Pair<String, Integer>> mReadyPictures = new UniqueSortedList<Pair<String, Integer>>(APROX_PICS_COUNT, new Comparator<Pair<String, Integer>>() {
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            if (o1.equals(o2)) return 0;
            else {
                int cmp = o1.first.compareTo(o2.first);
                if (cmp != 0) return cmp;
                else return o1.second.compareTo(o2.second);
            }
        }
    });

    public PictureRetriever(Context context, String remotePictureFolder, String localPictureFolder) {
        mContext = context;
        mRemotePictureFolder = remotePictureFolder;
        mLocalPictureFolder = localPictureFolder;

        restoreState();

        new Thread(this).start();
    }

    public void setRemotePictureFolder(String remotePictureFolder) {
        synchronized (mRemotePictureFolder) {
            mRemotePictureFolder = remotePictureFolder;
        }
    }

    public boolean hasPicture(String picId, int pictureType) {
        synchronized (mReadyPictures) {
            return mReadyPictures.contains(new Pair<String, Integer>(picId, pictureType));
        }
    }

    public Bitmap getPicture(String picId, int pictureType) {
        synchronized (mReadyPictures) {
            int id = mReadyPictures.indexOf(new Pair<String, Integer>(picId, pictureType));
            if (id == -1) return null;

            File picturePath = getLocalPicturePath(picId, pictureType);
            InputStream is = null;
            try {
                is = new FileInputStream(picturePath);
            } catch (FileNotFoundException e) {

                mReadyPictures.remove(id);
                return null;
            }

            return BitmapFactory.decodeStream(is);
        }
    }

    public synchronized void addRequest(String picId, int pictureType, PictureReceiver receiver) {
        synchronized (mTaskQueue) {
            mTaskQueue.add(new Pair<Pair<String, Integer>, PictureReceiver>(new Pair<String, Integer>(picId, pictureType), receiver));
            notify();
        }
    }

    private void restoreState() {
        String[] fileNames = mContext.getCacheDir().list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("pic_")) return true;
                return false;
            }
        });

        for (String name : fileNames) {
            int picIdStart = "pic_".length();
            int picIdEnd = name.indexOf("_", picIdStart + 1);

            String picId = name.substring(picIdStart, picIdEnd);
            int pictureType = postfixToPictureType(name.substring(picIdEnd + 1, name.indexOf(".", picIdEnd)));

            mReadyPictures.add(new Pair<String, Integer>(picId, pictureType));
        }
    }

    private int postfixToPictureType(String postfix) {
        if (postfix.compareTo("h") == 0)      return PictureType.LIST_BIG;
        else if (postfix.compareTo("m") == 0) return PictureType.LIST_MEDIUM;
        else if (postfix.compareTo("l") == 0) return PictureType.LIST_SMALL;
        else return PictureType.ORIGINAL;
    }

    private String getPicturePostfix(int pictureType) {
        String postfix = null;
        switch (pictureType) {
        case PictureType.LIST_SMALL:
            postfix = "_s.jpg";
            break;
        case PictureType.LIST_MEDIUM:
            postfix = "_m.jpg";
            break;
        case PictureType.LIST_BIG:
            postfix = "_h.jpg";
            break;
        case PictureType.ORIGINAL:
        default:
            postfix = "_.jpg";
            break;
        }

        return postfix;
    }

    private String getRemotePicturePath(String picId, int pictureType) {
        String name = "pic" + getPicturePostfix(pictureType);

        synchronized (mRemotePictureFolder) {
            StringBuffer buffer = new StringBuffer(mRemotePictureFolder.length() + 1 + picId.length() + 1 + name.length());
            return (buffer.append(mRemotePictureFolder).append("/").append(picId).append("/").append(name)).toString();
        }
    }

    private File getLocalPicturePath(String picId, int pictureType) {
        //File path = mContext.getDir(mLocalPictureFolder, Context.MODE_PRIVATE);
        File path = mContext.getCacheDir();
        return new File(path, "pic_" + picId + getPicturePostfix(pictureType));
    }

    private boolean downloadPicture(String loadFrom, File saveTo) {
        try {
            URL url = new URL(loadFrom);
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(saveTo);

            byte data[] = new byte[1024];

            int count = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void run() {
        while (true) {
            Pair<Pair<String, Integer>, PictureReceiver> task = null;
            synchronized (mTaskQueue) {
                task = mTaskQueue.poll();
            }

            if (task == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    // TODO: shit, but what to do?
                }
            } else {
                String picId = task.first.first;
                int pictureType = task.first.second;
                PictureReceiver receiver = task.second;

                String from = getRemotePicturePath(picId, pictureType);

                File to = getLocalPicturePath(picId, pictureType);

                if (downloadPicture(from, to)) {
                    synchronized (mReadyPictures) {
                        mReadyPictures.add(new Pair<String, Integer>(picId, pictureType));
                    }
                    task.second.onPictureReceive(picId, pictureType);
                    //if (mTaskQueue.peek() == null) {
                    //    task.second.onPictureReceive(picId, pictureType);
                    //}
                }
            }
        }
    }
}
