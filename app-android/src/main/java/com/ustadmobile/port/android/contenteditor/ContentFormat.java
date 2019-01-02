package com.ustadmobile.port.android.contenteditor;

/**
 * Class which represents the content formatting object, this handles the
 * content styling on the active editor.
 *
 * @author kileha3
 */
public class ContentFormat {

    private int formatIcon;

    private String formatCommand;

    private boolean active;

    private int formatType;

    private int formatTitle;

    private int formatId = 0;

    /**
     * Constructor which will be used to create an instance of content formatting.
     * @param formatIcon Formatting icon
     * @param formatCommand Formatting executable command
     * @param active Flag to indicate if the format is active or not
     * @param formatType Flag which shows which type of the formatting is.
     */
    public ContentFormat(int formatIcon, String formatCommand, boolean active, int formatType) {
        this.formatIcon = formatIcon;
        this.formatCommand = formatCommand;
        this.active = active;
        this.formatType = formatType;
    }

    /**
     * Constructor which will be used to create an instance of content formatting.
     * @param formatIcon Formatting icon
     * @param formatCommand Formatting executable command
     * @param active Flag to indicate if the format is active or not
     * @param formatType Flag which shows which type of the formatting is.
     * @param formatId formatting id.
     */
    public ContentFormat(int formatIcon, String formatCommand, boolean active,
                         int formatType,int formatId) {
        this.formatIcon = formatIcon;
        this.formatCommand = formatCommand;
        this.active = active;
        this.formatType = formatType;
        this.formatId = formatId;
    }

    public ContentFormat(int formatIcon, String formatCommand, boolean active,
                         int formatType,int formatId,int formatTitle) {
        this.formatIcon = formatIcon;
        this.formatCommand = formatCommand;
        this.active = active;
        this.formatType = formatType;
        this.formatId = formatId;
        this.formatTitle = formatTitle;
    }

    /**
     * Get formatting executable format command
     * @return executable command
     */
    public String getFormatCommand() {
        return formatCommand;
    }

    /**
     * Change formatting state
     * @param active True if it is activated otherwise false.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if the formatting has been activated
     * @return formatting state.
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Set formatting Icon
     * @param formatIcon resource drawable id.
     */
    public void setFormatIcon(int formatIcon) {
        this.formatIcon = formatIcon;
    }

    /**
     * Get formatting icon
     * @return resource drawable id
     */
    public int getFormatIcon() {
        return formatIcon;
    }

    /**
     * Set formatting executable command
     * @param formatCommand command to be executed.
     *
     * @see com.ustadmobile.core.view.ContentPreviewView for the list of all possible commands.
     */
    public void setFormatCommand(String formatCommand) {
        this.formatCommand = formatCommand;
    }

    /**
     * Get type of the formatting (Text / Paragraph)
     * @return Type of the formatting.
     */
    public int getFormatType() {
        return formatType;
    }

    /**
     * Set type of content formatting (Text / Paragraph)
     * @param formatType formatting type.
     */
    public void setFormatType(int formatType) {
        this.formatType = formatType;
    }

    public int getFormatId() {
        return formatId;
    }

    public void setFormatId(int formatId) {
        this.formatId = formatId;
    }

    public int getFormatTitle() {
        return formatTitle;
    }

    public void setFormatTitle(int formatTitle) {
        this.formatTitle = formatTitle;
    }
}
