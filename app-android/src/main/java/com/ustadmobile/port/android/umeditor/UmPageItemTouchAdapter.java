package com.ustadmobile.port.android.umeditor;

/**
 * Interface to notify a {@link android.support.v7.widget.RecyclerView.Adapter}
 * of moving and dismissal event from a
 * {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}.
 *
 * @author kileha3
 */
public interface UmPageItemTouchAdapter {

    /**
     * Invoked when an item has been dragged far enough to trigger a move.
     * This is called every time an item is shifted, and not at the end of a "drop" event.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then end position of the moved item.
     */

    void onPageItemMove(int fromPosition, int toPosition);

}
