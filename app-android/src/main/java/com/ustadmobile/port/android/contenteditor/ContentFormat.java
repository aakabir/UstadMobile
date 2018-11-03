package com.ustadmobile.port.android.contenteditor;

/**
 * Class which represents the content formatting type,
 * this handles the content styling on the active editor
 *
 * @author kileha3
 */
public class ContentFormat {

    private int formatIcon;

    private String formatTag;

    private boolean active;

    private int formatId;

    public ContentFormat(int formatIcon, String formatTag, boolean active) {
        this.formatIcon = formatIcon;
        this.formatTag = formatTag;
        this.active = active;
    }

    public String getFormatTag() {
        return formatTag;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setFormatId(int formatId) {
        this.formatId = formatId;
    }

    public int getFormatId() {
        return formatId;
    }

    public void setFormatIcon(int formatIcon) {
        this.formatIcon = formatIcon;
    }

    public int getFormatIcon() {
        return formatIcon;
    }
}
