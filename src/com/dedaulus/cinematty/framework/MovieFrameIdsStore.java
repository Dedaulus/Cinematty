package com.dedaulus.cinematty.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 05.01.12
 * Time: 3:18
 */
public class MovieFrameIdsStore {
    private String uid;
    private List<Integer> frameIds;
    
    {
        frameIds = new ArrayList<Integer>();
    }
    
    public MovieFrameIdsStore(String id) {
        this.uid = id;
    }
    
    public String getUid() {
        return uid;
    }

    public void addFrameId(int id) {
        frameIds.add(id);
    }
    
    public List<Integer> getFrameIds() {
        return frameIds;
    }
}
