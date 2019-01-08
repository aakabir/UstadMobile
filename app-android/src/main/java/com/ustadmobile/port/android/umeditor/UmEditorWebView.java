package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

/**
 * Custom webView which handles dispatch all key events to the JS side.
 *
 * @author kileha3
 *
 */
public class UmEditorWebView  extends WebView {

    public UmEditorWebView(Context context) {
        super(context);
    }

    public UmEditorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UmEditorWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * In order for the webView to dispatch all key events, this is a must.
     * @param outAttrs
     * @return
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BaseInputConnection(this, false);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }



}
