package com.ustadmobile.core.view;

public interface ContentEditorView extends UstadView {
    String VIEW_NAME = "ContentEditor";
    String TEXT_FORMAT_TYPE_BOLD = "bold";
    String TEXT_FORMAT_TYPE_UNDERLINE= "underline";
    String TEXT_FORMAT_TYPE_ITALIC = "italic";
    String TEXT_FORMAT_TYPE_STRIKE = "strikeThrough";
    String TEXT_FORMAT_TYPE_FONT = "font";
    String TEXT_FORMAT_TYPE_SUP = "superscript";
    String TEXT_FORMAT_TYPE_SUB= "subscript";

    String PARAGRAPH_FORMAT_ALIGN_CENTER = "align_center";
    String PARAGRAPH_FORMAT_ALIGN_LEFT = "align_left";
    String PARAGRAPH_FORMAT_ALIGN_RIGHT = "align_right";
    String PARAGRAPH_FORMAT_ALIGN_JUSTIFY = "align_justify";
    String PARAGRAPH_FORMAT_LIST_ORDERED = "list_ordered";
    String PARAGRAPH_FORMAT_LIST_UNORDERED = "list_unordered";
    String PARAGRAPH_FORMAT_INDENT_INCREASE = "increate_indent";
    String PARAGRAPH_FORMAT_INDENT_DECREASE = "decrease_indent";


    void setBoldFormatting();
}
