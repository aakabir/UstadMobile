package com.ustadmobile.core.view;

public interface ContentEditorView extends UstadView {
    String VIEW_NAME = "ContentEditor";
    String TEXT_FORMAT_TYPE_BOLD = "Bold";
    String TEXT_FORMAT_TYPE_UNDERLINE= "Underline";
    String TEXT_FORMAT_TYPE_ITALIC = "Italic";
    String TEXT_FORMAT_TYPE_STRIKE = "Strikethrough";
    String TEXT_FORMAT_TYPE_FONT = "FontSize";
    String TEXT_FORMAT_TYPE_SUP = "Superscript";
    String TEXT_FORMAT_TYPE_SUB= "Subscript";

    String PARAGRAPH_FORMAT_ALIGN_CENTER = "JustifyCenter";
    String PARAGRAPH_FORMAT_ALIGN_LEFT = "JustifyLeft";
    String PARAGRAPH_FORMAT_ALIGN_RIGHT = "JustifyRight";
    String PARAGRAPH_FORMAT_ALIGN_JUSTIFY = "JustifyFull";
    String PARAGRAPH_FORMAT_LIST_ORDERED = "InsertOrderedList";
    String PARAGRAPH_FORMAT_LIST_UNORDERED = "InsertUnorderedList";
    String PARAGRAPH_FORMAT_INDENT_INCREASE = "Indent";
    String PARAGRAPH_FORMAT_INDENT_DECREASE = "Outdent";

    String ACTION_PREVIEW = "Preview";
    String ACTION_REDO = "Redo";
    String ACTION_UNDO = "Undo";
    String ACTION_TEXT_DIRECTION = "mceDirectionLTR";
    String CONTENT_INSERT_MULTIPLE_CHOICE_QN = "MultipleChoice";
    String CONTENT_INSERT_FILLTHEBLANKS_QN = "FillTheBlanks";

    void setContentBold();

    void setContentItalic();

    void setContentUnderlined();

    void setContentStrikeThrough();

    void setContentFontSize(String fontSize);

    void setContentSuperscript();

    void setContentSubScript();

    void setContentJustified();

    void setContentCenterAlign();

    void setContentLeftAlign();

    void setContentRightAlign();

    void setContentOrderedList();

    void setContentUnOrderList();

    void setContentIncreaseIndent();

    void setContentDecreaseIndent();

    void setContentRedo();

    void setContentUndo();

    void setContentTextDirection(boolean right);

    void insertMultipleChoiceQuestion();

    void insertFillTheBlanksQuestion();

    void startContentPreview();

    void handleContentMenu();


}
