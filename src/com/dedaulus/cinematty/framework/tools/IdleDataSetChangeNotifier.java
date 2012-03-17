package com.dedaulus.cinematty.framework.tools;

import android.widget.AbsListView;
import android.widget.BaseAdapter;

/**
 * User: Dedaulus
 * Date: 17.03.12
 * Time: 0:52
 */
public class IdleDataSetChangeNotifier implements AbsListView.OnScrollListener {
    private volatile boolean asked;
    private volatile boolean idle = true;
    private BaseAdapter adapter;

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    public void askForNotifyDataSetChanged() {
        if (adapter == null) throw new RuntimeException("Adapter not set");
        if (idle) {
            adapter.notifyDataSetChanged();
        } else {
            asked = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (adapter == null) throw new RuntimeException("Adapter not set");
        idle = scrollState == SCROLL_STATE_IDLE;
        if (idle) {
            if (asked) {
                asked = false;
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
}
