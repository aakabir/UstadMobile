package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.ContentPreviewView;

import java.util.Hashtable;

public class ContentPreviewPresenter extends UstadBaseController<ContentPreviewView> {

    private Hashtable args;

    private boolean isPreviewIndex = false;

    public ContentPreviewPresenter(Object context, Hashtable arguments, ContentPreviewView view) {
        super(context, arguments, view);
        args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> {
            isPreviewIndex = true;
            view.setTitle(args.get(ContentPreviewView.FILE_NAME).toString());
            view.loadPreviewPage(args.get(ContentPreviewView.BASE_URL) +"preview.html");
        });

    }

    public boolean isPreviewIndex() {
        return isPreviewIndex;
    }

    public void setPreviewIndex(boolean previewIndex) {
        isPreviewIndex = previewIndex;
    }

    public void loadContentToPreview(){
        view.runOnUiThread(() ->
                view.startPreviewing(args.get(ContentPreviewView.EDITOR_CONTENT).toString()));
    }

    @Override
    public void setUIStrings() {

    }
}
