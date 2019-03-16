package com.ustadmobile.port.android.umeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.ustadmobile.port.android.view.ContentEditorPreviewFragment;

/**
 * Custom webView which enables swiping inner content when used in view pager
 *
 * @author kileha3
 */
public class UmEditorWebView extends WebView {
    private ContentEditorPreviewFragment previewFragment;

    public UmEditorWebView(Context context) {
        super(context);
    }

    public UmEditorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UmEditorWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setFragment(ContentEditorPreviewFragment webViewFragment) {
        this.previewFragment = webViewFragment;

    }
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        previewFragment.setPagedEnabled(true);
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            previewFragment.setPagedEnabled(false);
            return true;
        }

        return true;


    }
}
