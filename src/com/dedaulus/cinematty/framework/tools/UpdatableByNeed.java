package com.dedaulus.cinematty.framework.tools;

/**
 * User: Dedaulus
 * Date: 09.05.11
 * Time: 18:51
 */
public interface UpdatableByNeed {
    boolean isUpdateNeeded();
    void update();
}
