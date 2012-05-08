package com.dedaulus.cinematty.framework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    
    private static class ImageWrapper {
        String path;
        Bitmap image;
        boolean useMemoryCache;
        Calendar liveDate;
    }

    private static class Request {
        String url;
        boolean useMemoryCache;
        ImageReceivedAction action;
        
        Request(String url, boolean useMemoryCache, ImageReceivedAction action) {
            this.url = url;
            this.useMemoryCache = useMemoryCache;
            this.action = action;
        }
    }
    
    private final Map<String, ImageWrapper> images;
    private final Queue<Request> requests;

    public static interface ImageReceivedAction {
        void onImageReceived(String url, boolean downloaded);
    }
    
    {
        images = new HashMap<String, ImageWrapper>();
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
        stateFile = new File(localFolder, new StringBuilder(prefix).append(".xml").toString());
        
        restoreState();
        
        new Thread(this).start();
    }
    
    public Bitmap getImage(String url) {
        ImageWrapper wrapper;
        synchronized (images) {
            wrapper = images.get(url);
        }

        if (wrapper != null) {
            if (wrapper.image != null) {
                return wrapper.image;
            } else {
                Bitmap image = loadImage(wrapper.path);
                if (wrapper.useMemoryCache) {
                    wrapper.image = image;
                }
                return image;
            }
        } else {
            return null;
        }
    }
    
    public boolean hasImage(String url) {
        synchronized (images) {
            return images.containsKey(url);
        }
    }
    
    public synchronized void addRequest(String url, boolean useMemoryCache, ImageReceivedAction action) {
        synchronized (images) {
            if (images.containsKey(url)) return;
        }
        
        synchronized (requests) {
            requests.add(new Request(url, useMemoryCache, action));
        }

        notify();
    }
    
    public void saveState() {
        StringBuilder xmlBuffer = new StringBuilder();
        xmlBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><data>");

        synchronized (images) {
            for (Map.Entry<String, ImageWrapper> entry : images.entrySet()) {
                /*
                Calendar liveDate = entry.getValue().liveDate;
                String year = Integer.toString(liveDate.get(Calendar.YEAR));
                String month = Integer.toString(liveDate.get(Calendar.MONTH));
                String day = Integer.toString(liveDate.get(Calendar.DAY_OF_MONTH));
                String dateStr = new StringBuilder().append(year).append(".").append(month).append(".").append(day).toString();
                */
                xmlBuffer.append("<image url=\"").append(entry.getKey()).append("\" path=\"").append(entry.getValue().path).append("\" useMemoryCache=\"").append(entry.getValue().useMemoryCache ? "1" : "0").append("\" liveDate=\"").append(entry.getValue().liveDate.getTimeInMillis()).append("\" />");
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
                    ImageWrapper wrapper = new ImageWrapper();
                    wrapper.path = path;
                    wrapper.liveDate = liveDate;
                    wrapper.useMemoryCache = request.useMemoryCache;
                    if (wrapper.useMemoryCache) {
                        wrapper.image = loadImage(wrapper.path);
                    }
                    synchronized (images) {
                        images.put(request.url, wrapper);
                    }

                    if (request.action != null) {
                        request.action.onImageReceived(request.url, true);
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
                    ImageWrapper wrapper = new ImageWrapper();
                    wrapper.path = attributes.getNamedItem("path").getNodeValue();
                    wrapper.useMemoryCache = Integer.parseInt(attributes.getNamedItem("useMemoryCache").getNodeValue()) != 0;
                    got.setTimeInMillis(Long.parseLong(attributes.getNamedItem("liveDate").getNodeValue()));
                    wrapper.liveDate = got;

                    if (now.before(got)) {
                        File image = new File(wrapper.path);
                        if (image.exists()) {
                            if (wrapper.useMemoryCache) {
                                wrapper.image = loadImage(wrapper.path);
                            }
                            images.put(attributes.getNamedItem("url").getNodeValue(), wrapper);
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

            File localFile = new File(localFolder, new StringBuilder(prefix).append(UUID.randomUUID().toString()).append(".jpg").toString());
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
