package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom view pager which enables webview to be scrolled horizontally
 */
public class UmEditorViewPager extends ViewPager {

    private boolean pagingEnabled = true;

    /**
     * Constructor called when creating new instance using context
     * @param context Application context
     */
    public UmEditorViewPager(Context context) {
        super(context);
    }

    /**
     * Constructor used when creating new instance using attrs
     * @param context Application context
     * @param attrs attrs values
     */
    public UmEditorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.pagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        try { return super.onInterceptTouchEvent(event) && this.pagingEnabled; }catch(
                IllegalArgumentException exception){ exception.printStackTrace(); }
        return false;

    }

    /**
     * Set paging control flag
     * @param enabled True when paging is enabled otherwise false.
     *                i.e this flag will be changes when you scroll horizontally
     */
    public void setPagingEnabled(boolean enabled) {
        this.pagingEnabled = enabled;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return this.pagingEnabled && super.canScroll(v, checkV, dx, x, y);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
    }

}
