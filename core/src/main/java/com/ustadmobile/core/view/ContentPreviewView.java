package com.ustadmobile.core.view;

/**
 * @author kileha3
 */
public interface ContentPreviewView extends UstadView {

    String VIEW_NAME = "ContentPreview";
    String BASE_URL = "base_request_uri";
    String FILE_NAME = "file_name";
    String EDITOR_CONTENT = "editor_content";

    void loadPreviewPage(String pageUrl);

    void startPreviewing(String tempContent);

    void setTitle(String title);

}
