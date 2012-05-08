package com.dedaulus.cinematty.framework;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 17.12.11
 * Time: 13:55
 */
public class WebServerTalker {
    //private static final int OK            = 0;
    //private static final int UPDATE_NEEDED = 1;
    //private static final int NO_RESPONSE   = 2;

    private static final String STATUS = "status";
    
    private String url;
    private int appVersion;
    private Map<String, String> response;

    {
        response = new HashMap<String, String>();
    }
    
    public WebServerTalker(String url, int appVersion) {
        this.url = url;
        this.appVersion = appVersion;
    }
    
    public SyncStatus connect() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("version", appVersion);

        JSONArray postJson = new JSONArray();
        postJson.put(json);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("json", json.toString());
        httpPost.getParams().setParameter("jsonpost", postJson);
        HttpClient httpClient = new DefaultHttpClient();

        SyncStatus syncStatus = SyncStatus.NO_RESPONSE;
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse != null) {
                InputStream is = httpResponse.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                is.close();

                JSONObject jsonObject = new JSONObject(builder.toString());
                Iterator keysIterator = jsonObject.keys();
                while (keysIterator.hasNext()) {
                    String key = (String)keysIterator.next();
                    if (key.equalsIgnoreCase(STATUS)) {
                        syncStatus = SyncStatus.valueOf(jsonObject.getInt(key));
                    } else {
                        response.put(key, jsonObject.getString(key));
                    }
                }
            }
        } catch (IOException e) {
            return SyncStatus.NO_RESPONSE;
        }
        
        return syncStatus;
    }
    
    public Map<String, String> getResponse() {
        return Collections.unmodifiableMap(response);
    }
}
