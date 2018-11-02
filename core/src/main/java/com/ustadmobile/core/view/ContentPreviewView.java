package com.ustadmobile.core.view;

/**
 * @author kileha3
 */
public interface ContentPreviewView extends UstadView {

    String VIEW_NAME = "ContentPreview";
    String PREVIEW_URL = "base_request_uri";
    String FILE_NAME = "file_name";

    void loadPreviewPage(String pageUrl);

    void setTitle(String title);

}
