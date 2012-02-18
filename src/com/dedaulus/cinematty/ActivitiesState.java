package com.dedaulus.cinematty;

import com.dedaulus.cinematty.framework.tools.ActivityState;

/**
 * User: Dedaulus
 * Date: 17.12.11
 * Time: 11:31
 */
public interface ActivitiesState {
    ActivityState getState(String cookie);

    void setState(String cookie, ActivityState state);

    void removeState(String cookie);

    void dump();
}
