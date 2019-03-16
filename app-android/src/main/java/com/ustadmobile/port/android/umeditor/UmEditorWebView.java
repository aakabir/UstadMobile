package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Custom webview which used input connection to handle the content editor keyboard events
 *
 * @author kileha3
 */
public class UmEditorWebView extends WebView {


    public UmEditorWebView(Context context) {
        super(context);
    }

    public UmEditorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UmEditorWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UmEditorWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
