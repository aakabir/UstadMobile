package com.ustadmobile.port.android.umeditor;

/**
 * Class which represents response received from Javascript method
 * execution or console message on native android.
 *
 * @author kileha3
 */

public class UmWebJsResponse {

    private String action;

    private String content;

    /**
     * Get action command to be executed on android native
     */
    public String getAction() {
        return action;
    }

    /**
     * Set action command to be executed on android native
     * @param action command to be set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get content received from JS
     * @return received content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set content to be received to the android native
     * @param content content to be set
     */
    public void setContent(String content) {
        this.content = content;
    }

}
