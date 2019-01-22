package com.ustadmobile.port.android.umeditor;

/**
 * Interface to notify an item ViewHolder callbacks from {@link
 * android.support.v7.widget.helper.ItemTouchHelper.Callback}.
 *
 * @author kileha3
 */

public interface UmPageItemTouchViewHolder {
    /**
     * Called when the {@link android.support.v7.widget.helper.ItemTouchHelper}
     * first registers an item as being dragged.
     */
    void onPageItemSelected();


    /**
     * Called when the {@link android.support.v7.widget.helper.ItemTouchHelper}
     * has completed move, and the active item state should be cleared.
     */
    void onPageItemClear();
}
