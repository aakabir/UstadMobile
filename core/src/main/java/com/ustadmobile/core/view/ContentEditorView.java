package com.ustadmobile.core.view;

public interface ContentEditorView extends UstadView {

    String VIEW_NAME = "ContentEditor";
    String EDITOR_CONTENT_FILE = "file_name";

    /**
     * List of all available text formatting types.
     */

    String TEXT_FORMAT_TYPE_BOLD = "Bold";
    String TEXT_FORMAT_TYPE_UNDERLINE= "Underline";
    String TEXT_FORMAT_TYPE_ITALIC = "Italic";
    String TEXT_FORMAT_TYPE_STRIKE = "Strikethrough";
    String TEXT_FORMAT_TYPE_FONT = "FontSize";
    String TEXT_FORMAT_TYPE_SUP = "Superscript";
    String TEXT_FORMAT_TYPE_SUB= "Subscript";

    /**
     * List of all available paragraph formatting types.
     */
    String PARAGRAPH_FORMAT_ALIGN_CENTER = "JustifyCenter";
    String PARAGRAPH_FORMAT_ALIGN_LEFT = "JustifyLeft";
    String PARAGRAPH_FORMAT_ALIGN_RIGHT = "JustifyRight";
    String PARAGRAPH_FORMAT_ALIGN_JUSTIFY = "JustifyFull";
    String PARAGRAPH_FORMAT_LIST_ORDERED = "InsertOrderedList";
    String PARAGRAPH_FORMAT_LIST_UNORDERED = "InsertUnorderedList";
    String PARAGRAPH_FORMAT_INDENT_INCREASE = "Indent";
    String PARAGRAPH_FORMAT_INDENT_DECREASE = "Outdent";

    /**
     * List of all actions to be taken from the editing screen.
     */
    String ACTION_PREVIEW = "Preview";
    String ACTION_REDO = "Redo";
    String ACTION_UNDO = "Undo";
    String ACTION_INIT_EDITOR ="onInitEditor";
    String ACTION_CHECK_ACTIVE_CONTROLS ="onActiveControlCheck";
    String ACTION_CONTROLS_ACTIVATED ="activeControl";
    String ACTION_CONTENT_CHANGED ="onContentChanged";
    String ACTION_SAVE_CONTENT ="onSaveContent";
    String ACTION_TEXT_DIRECTION_LTR = "mceDirectionLTR";
    String ACTION_TEXT_DIRECTION_RTL = "mceDirectionRTL";

    /**
     * Question templates insert command tag.
     */
    String CONTENT_INSERT_MULTIPLE_CHOICE_QN = "MultipleChoice";
    String CONTENT_INSERT_FILL_THE_BLANKS_QN = "FillTheBlanks";

    /**
     * List of files resources to be copied from core tinymce editor dir to external dir.
     */
    String CONTENT_CSS_USTAD = "ustadmobile.css";
    String RESOURCE_CSS_BOOTSTRAP = "bootstrap.min.css";
    String RESOURCE_JS_BOOTSTRAP = "bootstrap.min.js";
    String RESOURCE_JS_USTAD_WIDGET = "UstadWidgets.js";
    String RESOURCE_JS_USTAD_EDITOR = "UstadEditor.js";
    String RESOURCE_JS_JQUERY = "jquery3.3.1.min.js";
    String RESOURCE_JS_TINYMCE = "tinymce.min.js";

    String [] CONTENT_EDITOR_HEAD_RESOURCES = new String[]{
            "<link href=\"css/ustadmobile.css\" rel=\"stylesheet\" />",
            "<link href=\"css/bootstrap.min.css\" rel=\"stylesheet\" />",
            "<script src=\"js/jquery3.3.1.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"js/tinymce.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"js/UstadWidgets.js\" type=\"text/javascript\"></script>",
            "<script src=\"js/UstadEditor.js\" type=\"text/javascript\"></script>"
    };
    String [] CONTENT_EDITOR_BODY_RESOURCES = new String[] {
            "<script src=\"js/bootstrap.min.js\"></script>",
            "<script>\n" +
                    "    $(function() {\n" +
                    "        $('body').css('width', $(window).width());\n" +
                    "        QuestionWidget.handleEditOff();\n" +
                    "    });\n" +
            "</script>"
    };

    String NEW_DOCUMENT_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
            "    <link href=\"css/ustadmobile.css\" rel=\"stylesheet\">\n" +
            "    <link href=\"css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
            "    <script src=\"js/jquery3.3.1.min.js\" type=\"text/javascript\"></script>\n" +
            "    <script src=\"js/tinymce.min.js\" type=\"text/javascript\"></script>\n" +
            "    <script src=\"js/UstadWidgets.js\" type=\"text/javascript\"></script>\n" +
            "    <script src=\"js/UstadEditor.js\" type=\"text/javascript\"></script>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<div class=\"container-fluid default-margin-top\">\n" +
            "    <div class=\"row\" id=\"ustad-preview\">\n" +
            "        <template/>\n" +
            "    </div>\n" +
            "</div>\n" +
            "\n" +
            "<script src=\"js/bootstrap.min.js\" controls></script>\n" +
            "<script>\n" +
            "    $(function() {\n" +
            "        $('body').css('width', $(window).width());\n" +
            "        QuestionWidget.handleEditOff();\n" +
            "    });\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>";

    /**
     * Set bold formatting on selected/focused content
     */
    void setContentBold();

    /**
     *  Set italic formatting on selected/focused content
     */
    void setContentItalic();

    /**
     * Set underline formatting on selected/focused content
     */
    void setContentUnderlined();

    /**
     * Set strike through formatting on selected/focused content
     */
    void setContentStrikeThrough();

    /**
     * Set font size on selected/focused content
     * @param fontSize font size to be set in pt unit.
     */
    void setContentFontSize(String fontSize);

    /**
     * Make selected/focused content a superscript
     */
    void setContentSuperscript();

    /**
     * Make selected/focused content a subscript
     */
    void setContentSubScript();

    /**
     * Justify your selected/focused paragraph content
     */
    void setContentJustified();

    /**
     * Center align your selected/focused paragraph content
     */
    void setContentCenterAlign();

    /**
     * Left align your selected/focused paragraph content
     */
    void setContentLeftAlign();

    /**
     * Right align your selected/focused paragraph content
     */
    void setContentRightAlign();

    /**
     * Insert ordered list on cursor position
     */
    void setContentOrderedList();

    /**
     * Insert unordered list on cursor position
     */
    void setContentUnOrderList();

    /**
     * Increase indentation of the content
     */
    void setContentIncreaseIndent();

    /**
     * Decrease indentation of the content
     */
    void setContentDecreaseIndent();

    /**
     * Redo changes that has been deleted/ undo from the content editing area
     */
    void setContentRedo();

    /**
     * Undo changed which has been introduced to the content editing area.
     */
    void setContentUndo();

    /**
     * Change language directionality (LTR / RTL)
     * @param isLTR True if it is supposed to be changed to LTR which is like a default.
     */
    void setContentTextDirection(boolean isLTR);

    /**
     * Insert multi-choice question template on the content editing area
     */
    void insertMultipleChoiceQuestion();

    /**
     * Insert fill in the blanks template on the content editing area.
     */
    void insertFillTheBlanksQuestion();

    /**
     * Request contents that has been added to the content editing area from tinymCE
     */
    void requestEditorContent();

    /**
     * Create new document in case it wasn't about document update.
     */
    void createNewDocument();

    /**
     * Copy all resources needed for the stand alone preview
     * from the core tinymce to the external storage.
     */
    void handleResources();

    /**
     * Start local web server which will be used for editing purposes.
     */
    void startWebServer();


}
