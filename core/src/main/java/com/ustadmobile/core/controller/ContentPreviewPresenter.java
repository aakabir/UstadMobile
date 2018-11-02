package com.ustadmobile.core.controller;

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
            view.setTitle(args.get(ContentPreviewView.FILE_NAME).toString());
            view.loadPreviewPage(args.get(ContentPreviewView.PREVIEW_URL).toString());
        });

    }


    @Override
    public void setUIStrings() {

    }
}
