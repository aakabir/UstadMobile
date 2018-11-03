package com.ustadmobile.core.view;

/**
 * @author kileha3
 */
public interface ContentPreviewView extends UstadView {

    String VIEW_NAME = "ContentPreview";
    String PREVIEW_URL = "base_request_uri";

    void loadPreviewPage(String localUri,String indexFile);

}
