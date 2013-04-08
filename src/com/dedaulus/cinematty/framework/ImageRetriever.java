package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * User: Dedaulus
 * Date: 19.01.12
 * Time: 14:18
 */
public class ImageRetriever implements Runnable {
    public static class ObjectAlreadyExists extends Exception {
        public ObjectAlreadyExists(String text) {
            super(text);
        }
    }
    
    private static final int LIVE_DAYS = 7;
    private static final String PREPREFIX = "image_retriever_";

    private static final Map<String, ImageRetriever> entities = new HashMap<String, ImageRetriever>();

    private String entity;
    private String prefix;
    private File localFolder;
    private File stateFile;
    private volatile boolean stopped;
    private volatile boolean paused;
    
    private static class ImageMeta {
        String path;
        long liveDate;
    }

    private static class Request {
        String url;
        ImageReceivedAction action;
        
        Request(String url, ImageReceivedAction action) {
            this.url = url;
            this.action = action;
        }
    }
    
    private final Map<String, ImageMeta> imageMetas;
    private LruCache<String, Bitmap> images;
    private final Queue<Request> requests;

    public static interface ImageReceivedAction {
        void onImageReceived(String url, boolean downloaded);
    }
    
    {
        imageMetas = new HashMap<String, ImageMeta>();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        images = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                //return bitmap.getByteCount() / 1024;
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        requests = new LinkedList<Request>();
    }

    public static ImageRetriever create(String entity, File localFolder) {
        if (entities.containsKey(entity)) {
            return entities.get(entity);
        }

        ImageRetriever retriever = new ImageRetriever(entity, localFolder);
        entities.put(entity, retriever);
        return retriever;
    }
    
    private ImageRetriever(String entity, File localFolder) {
        this.entity = entity;
        prefix = PREPREFIX + entity;
        this.localFolder = localFolder;
        stateFile = new File(localFolder, prefix + ".xml");
        
        restoreState();
        
        new Thread(this).start();
    }
    
    public Bitmap getImage(String url) {
        ImageMeta wrapper;
        synchronized (imageMetas) {
            wrapper = imageMetas.get(url);
        }

        if (wrapper != null) {
            Bitmap image = images.get(wrapper.path);
            if (image != null) {
                return image;
            }
        }

        return null;
    }
    
    public boolean hasImage(String url) {
        synchronized (imageMetas) {
            return imageMetas.containsKey(url);
        }
    }
    
    public synchronized void addRequest(String url, ImageReceivedAction action) {
        synchronized (requests) {
            requests.add(new Request(url, action));
        }

        notify();
    }
    
    public void saveState() {
        StringBuilder xmlBuffer = new StringBuilder();
        xmlBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><data>");

        synchronized (imageMetas) {
            for (Map.Entry<String, ImageMeta> entry : imageMetas.entrySet()) {
                xmlBuffer.append("<image url=\"").append(entry.getKey()).append("\" path=\"").append(entry.getValue().path).append("\" liveDate=\"").append(entry.getValue().liveDate).append("\" />");
            }
        }

        xmlBuffer.append("</data>");

        try {
            Writer output = new BufferedWriter(new FileWriter(stateFile));
            output.write(xmlBuffer.toString());
            output.close();
        } catch (IOException e){}
    }

    @Override
    public void run() {
        while (!stopped) {
            while (paused && !stopped) {
                try{
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {}
            }

            if (stopped) break;

            Request request;
            synchronized (requests) {
                request = requests.poll();
            }

            if (request == null) {
                try{
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {}
            } else {
                if (!hasImage(request.url)) {
                    String path = downloadImage(request.url);
                    if (path == null) {
                        if (request.action != null) {
                            request.action.onImageReceived(request.url, false);
                        }
                        continue;
                    }
                    Calendar liveDate = Calendar.getInstance();
                    liveDate.add(Calendar.DAY_OF_YEAR, LIVE_DAYS);
                    ImageMeta wrapper = new ImageMeta();
                    wrapper.path = path;
                    wrapper.liveDate = liveDate.getTimeInMillis();

                    synchronized (imageMetas) {
                        imageMetas.put(request.url, wrapper);
                    }

                    Bitmap image = loadImage(wrapper.path);
                    if (image != null) {
                        images.put(wrapper.path, image);
                    }

                    if (request.action != null) {
                        request.action.onImageReceived(request.url, image != null);
                    }
                } else {
                    ImageMeta meta = imageMetas.get(request.url);
                    Bitmap image = images.get(meta.path);
                    if (image == null) {
                        image = loadImage(meta.path);
                        if (image != null) {
                            images.put(meta.path, image);
                        }
                    }

                    if (request.action != null) {
                        request.action.onImageReceived(request.url, image != null);
                    }
                }
            }
        }

        synchronized (entities) {
            entities.remove(entity);
        }
    }

    public synchronized void stop() {
        stopped = true;
        notify();
    }

    public synchronized void pause() {
        paused = true;
        notify();
    }

    public synchronized void resume() {
        paused = false;
        notify();
    }

    private void restoreState() {
        if (stateFile.exists()) {
            Calendar now = Calendar.getInstance();
            Calendar got = Calendar.getInstance();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(stateFile);
                Element root = dom.getDocumentElement();
                NodeList items = root.getElementsByTagName("image");
                for (int i = 0; i < items.getLength(); i++) {
                    NamedNodeMap attributes = items.item(i).getAttributes();
                    ImageMeta wrapper = new ImageMeta();
                    wrapper.path = attributes.getNamedItem("path").getNodeValue();
                    wrapper.liveDate = Long.parseLong(attributes.getNamedItem("liveDate").getNodeValue());

                    got.setTimeInMillis(wrapper.liveDate);
                    if (now.before(got)) {
                        File image = new File(wrapper.path);
                        if (image.exists()) {
                            imageMetas.put(attributes.getNamedItem("url").getNodeValue(), wrapper);
                        }
                    } else {
                        new File(wrapper.path).delete();
                    }
                }
            } catch (Exception e) {}
        }
    }
    
    private Bitmap loadImage(String path) {
        return BitmapFactory.decodeFile(path);
    }

    private String downloadImage(String urlStr) {
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            connection.connect();

            File localFile = new File(localFolder, prefix + UUID.randomUUID().toString() + ".jpg");
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(localFile);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            return localFile.getPath();
        } catch (Exception e) {
            return null;
        }
    }
}
