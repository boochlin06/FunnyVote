package com.android.heaton.funnyvote.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by heaton on 2016/11/9.
 */

public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    public boolean mControlsVisible = true;
    private int HIDE_THRESHOLD = 100;
    private int mScrolledDistance = 0;

    public HidingScrollListener(int threshold, boolean initVisible) {
        this.HIDE_THRESHOLD = threshold;
    }

    public HidingScrollListener() {
    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        if (firstVisibleItem == 0) {
            if (!mControlsVisible) {
                onShow();
                mControlsVisible = true;
            }
        } else {
            if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                onHide();
                mControlsVisible = false;
                mScrolledDistance = 0;
            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                onShow();
                mControlsVisible = true;
                mScrolledDistance = 0;
            }
        }
        if ((mControlsVisible && dy > 0) || (!mControlsVisible && dy < 0)) {
            mScrolledDistance += dy;
        }
    }

    public void resetScrollDistance() {
        mControlsVisible = true;
        mScrolledDistance = 0;
    }

    public abstract void onHide();

    public abstract void onShow();
}