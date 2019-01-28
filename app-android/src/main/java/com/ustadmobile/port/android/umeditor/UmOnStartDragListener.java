package com.ustadmobile.port.android.umeditor;

import android.support.v7.widget.RecyclerView;

/**
 * Interface which listen for the drag event on recycler view holder
 *
 * @author kileha3
 */
public interface UmOnStartDragListener {

    /**
     * invoked when a view is requesting a drag start.
     *
     * @param viewHolder View holder to grad
     */
    void onDragStarted(RecyclerView.ViewHolder viewHolder);
}
