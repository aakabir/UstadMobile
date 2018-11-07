package com.ustadmobile.port.android.contenteditor;

/**
 * Class which represents the content formatting type,
 * this handles the content styling on the active editor
 *
 * @author kileha3
 */
public class ContentFormat {

    private int formatIcon;

    private String formatCommand;

    private boolean active;

    private int formatType;

    public ContentFormat(int formatIcon, String formatCommand, boolean active, int formatType) {
        this.formatIcon = formatIcon;
        this.formatCommand = formatCommand;
        this.active = active;
        this.formatType = formatType;
    }

    public String getFormatCommand() {
        return formatCommand;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }


    public void setFormatIcon(int formatIcon) {
        this.formatIcon = formatIcon;
    }

    public int getFormatIcon() {
        return formatIcon;
    }

    public void setFormatCommand(String formatCommand) {
        this.formatCommand = formatCommand;
    }

    public int getFormatType() {
        return formatType;
    }

    public void setFormatType(int formatType) {
        this.formatType = formatType;
    }
}
