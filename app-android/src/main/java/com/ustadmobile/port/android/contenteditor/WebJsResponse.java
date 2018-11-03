package com.ustadmobile.port.android.contenteditor;

/**
 * Class which represents response received from Javascript method
 * execution or console message on native android.
 *
 * @author kileha3
 */

public class WebJsResponse {

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
