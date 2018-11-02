package com.ustadmobile.port.android.contenteditor;

/**
 * Class which represents response received from Javascript method execution on native android.
 * @author kileha3
 */

public class WebContentEditorJsCallback {

    private String action;

    private String content;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
