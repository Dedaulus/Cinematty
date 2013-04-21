package com.dedaulus.cinematty.framework;

/**
 * User: Dedaulus
 * Date: 04.01.12
 * Time: 0:47
 */
public class SyncStatus {
    private int statusCode;

    public Movie movie;
    public int day;
    public Cinema cinema;

    private SyncStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public static SyncStatus createSharedPageStatus(Movie movie, int day, Cinema cinema) {
        SHARED_PAGE.movie = movie;
        SHARED_PAGE.day = day;
        SHARED_PAGE.cinema = cinema;
        return SHARED_PAGE;
    }

    public static final SyncStatus OK = new SyncStatus(0);

    public static final SyncStatus UPDATE_NEEDED = new SyncStatus(1);

    public static final SyncStatus NO_RESPONSE = new SyncStatus(2);

    public static final SyncStatus BAD_RESPONSE = new SyncStatus(3);

    public static final SyncStatus OUT_OF_DATE = new SyncStatus(4);

    public static final SyncStatus SHARED_PAGE_IN_WEBVIEW = new SyncStatus(5);

    public static final SyncStatus SHARED_PAGE = new SyncStatus(6);

    private static final SyncStatus[] STATS = {
            OK,
            UPDATE_NEEDED,
            NO_RESPONSE,
            BAD_RESPONSE,
            OUT_OF_DATE,
            SHARED_PAGE_IN_WEBVIEW,
            SHARED_PAGE};

    public static SyncStatus valueOf(int statusCode) {
        return STATS[statusCode];
    }
}
