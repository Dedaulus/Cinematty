package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 04.01.12
 * Time: 0:47
 */
public class SyncStatus {
    private int statusCode;

    private SyncStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public static final SyncStatus OK = new SyncStatus(0);

    public static final SyncStatus UPDATE_NEEDED = new SyncStatus(1);

    public static final SyncStatus NO_RESPONSE = new SyncStatus(2);

    public static final SyncStatus BAD_RESPONSE = new SyncStatus(3);

    private static final SyncStatus[] STATS = {OK, UPDATE_NEEDED, NO_RESPONSE, BAD_RESPONSE};

    public static SyncStatus valueOf(int statusCode) {
        return STATS[statusCode];
    }
}
