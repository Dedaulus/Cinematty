package com.dedaulus.cinematty.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Gallery;
import com.dedaulus.cinematty.ActivitiesState;
import com.dedaulus.cinematty.CinemattyApplication;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.activities.adapters.FrameItemAdapter;
import com.dedaulus.cinematty.framework.SyncStatus;
import com.dedaulus.cinematty.framework.tools.ActivityState;
import com.dedaulus.cinematty.framework.tools.Constants;

import java.util.List;

/**
 * User: Dedaulus
 * Date: 18.03.12
 * Time: 19:13
 */
public class FramesActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frames_gallery);

        CinemattyApplication app = (CinemattyApplication)getApplication();
        if (app.syncSchedule(this, true) != SyncStatus.OK) {
            app.restart();
            finish();
            return;
        }

        ActivitiesState activitiesState = app.getActivitiesState();

        int frameId = getIntent().getIntExtra(Constants.FRAME_ID, -1); 
        String stateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
        ActivityState state = activitiesState.getState(stateId);
        if (state == null) throw new RuntimeException("ActivityState error");
        if (state.activityType != ActivityState.VIEW_FRAME) throw new RuntimeException("ActivityType error");

        Gallery gallery = (Gallery)findViewById(R.id.gallery);
        gallery.setAdapter(new FrameItemAdapter(this, state.movie.getFrameIdsStore(), app.getImageRetrievers().getFrameImageRetriever()));
        
        if (frameId != -1) {
            List<Integer> frameIds = state.movie.getFrameIdsStore().getFrameIds();
            int position = 0;
            for (int id : frameIds) {
                if (id == frameId) {
                    gallery.setSelection(position);
                    break;
                }
                ++position;
            }
        }
    }
}