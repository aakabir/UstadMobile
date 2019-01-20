package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class UmEditorWebView extends WebView {

    private InputConnectionWrapper inputConnectionWrapper;

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

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection superCon = super.onCreateInputConnection(outAttrs);
        inputConnectionWrapper = new InputConnectionWrapper(superCon);
        return superCon != null ? inputConnectionWrapper : null;
    }

    public InputConnectionWrapper getInputConnectionWrapper() {
        return inputConnectionWrapper;
    }
}
