package com.ustadmobile.core.view;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;

public interface ContentEditorView extends UstadView {

    String VIEW_NAME = "ContentEditor";

    String EDITOR_PREVIEW_PATH = "preview_path";

    String EDITOR_REQUEST_URI = "request_uri";

    String CONTENT_ENTRY_FILE_UID = "content_entry_file_uid";

    /**
     * List of all available text formatting.
     */

    String TEXT_FORMAT_TYPE_BOLD = "Bold";
    String TEXT_FORMAT_TYPE_UNDERLINE= "Underline";
    String TEXT_FORMAT_TYPE_ITALIC = "Italic";
    String TEXT_FORMAT_TYPE_STRIKE = "Strikethrough";
    String TEXT_FORMAT_TYPE_FONT = "FontSize";
    String TEXT_FORMAT_TYPE_SUP = "Superscript";
    String TEXT_FORMAT_TYPE_SUB= "Subscript";

    /**
     * List of all available paragraph formatting.
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

    String ACTION_REDO = "Redo";
    String ACTION_UNDO = "Undo";
    String ACTION_TEXT_DIRECTION_LTR = "mceDirectionLTR";
    String ACTION_TEXT_DIRECTION_RTL = "mceDirectionRTL";
    String ACTION_INSERT_CONTENT = "insertContent";

    /**
     * List of callback received from JS to control native behavior
     */
    String ACTION_INIT_EDITOR ="onInitEditor";
    String ACTION_CONTROLS_ACTIVATED ="onActiveControlCheck";
    String ACTION_SAVE_CONTENT ="onSaveContent";
    String ACTION_SELECT_ALL = "selectAll";
    String ACTION_CONTENT_CUT = "onContentCut";
    String ACTION_CHECK_COMPLETED = "onProtectedElementCheck";

    /**
     * Question templates insert command tag.
     */
    String CONTENT_INSERT_MULTIPLE_CHOICE_QN = "MultipleChoice";
    String CONTENT_INSERT_FILL_THE_BLANKS_QN = "FillTheBlanks";

    /**
     * List of editor core resources.
     */

    String RESOURCE_JS_USTAD_WIDGET = "UmQuestionWidget.js";
    String RESOURCE_JS_USTAD_EDITOR = "UmContentEditorCore.js";
    String RESOURCE_JS_TINYMCE = "tinymce.min.js";
    String RESOURCE_JS_RANGY = "rangy-core.js";
    String RESOURCE_BLANK_DOCUMENT = "umEditorBlankDoc.zip";

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
     * @param command Tinymce command for language directionality.
     */
    void setContentTextDirection(String  command);

    /**
     * Insert multi-choice question template on the content editing area
     */
    void insertMultipleChoiceQuestion();

    /**
     * Insert fill in the blanks template on the content editing area.
     */
    void insertFillTheBlanksQuestion();

    /**
     * Insert content to the editor
     * @param content content to be added to the editor
     */
    void insertContent(String content);

    /**
     * Select all content added to to editor
     */
    void selectAllContent();

    /**
     * Handle all document pages
     */
    void handleSelectedPage();

    /**
     * Get active file helper instance
     * @return ContentEditorFileHelperCoreSE instance
     */
    UmEditorFileHelperCore getFileHelper();

    void showNotFoundErrorMessage();

}
