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
            String localUri = args.get(ContentPreviewView.PREVIEW_URL).toString();
            String indexFile = args.get(ContentEditorView.EDITOR_CONTENT_FILE).toString();
            view.loadPreviewPage(localUri, indexFile);
        });

    }


    @Override
    public void setUIStrings() {

    }
}
