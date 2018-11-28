package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;

import java.util.Hashtable;

public class ContentPreviewPresenter extends UstadBaseController<ContentPreviewView> {

    private Hashtable args;

    public ContentPreviewPresenter(Object context, Hashtable arguments, ContentPreviewView view) {
        super(context, arguments, view);
        args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> {
            String requestUri = args.get(ContentEditorView.EDITOR_REQUEST_URI).toString();
            String indexFile = args.get(ContentEditorView.EDITOR_PREVIEW_PATH).toString();
            view.startPreviewing(requestUri, indexFile);
        });

    }


    @Override
    public void setUIStrings() {

    }
}
