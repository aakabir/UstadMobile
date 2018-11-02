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
    String ACTION_INIT_EDITOR ="onInitEditor";
    String ACTION_CONTENT_CHANGED ="onContentChanged";
    String ACTION_SAVE_CONTENT ="onSaveContent";
    String ACTION_TEXT_DIRECTION = "mceDirectionLTR";
    String CONTENT_INSERT_MULTIPLE_CHOICE_QN = "MultipleChoice";
    String CONTENT_INSERT_FILLTHEBLANKS_QN = "FillTheBlanks";

    String CONTENT_ROOT_DIR = "root_dir";
    String CONTENT_CSS_DIR = "css_dir";
    String CONTENT_JS_DIR = "js_dir";
    String CONTENT_MEDIA_DIR = "media_dir";
    String CONTENT_CSS_USTAD = "ustadmobile.css";
    String CONTENT_CSS_BOOTSTRAP = "bootstrap.min.css";
    String CONTENT_JS_BOOTSTRAP = "bootstrap.min.css";
    String CONTENT_JS_USTAD_WIDGET = "UstadWidgets.js";
    String CONTENT_JS_USTAD_EDITOR = "UstadEditor.js";
    String CONTENT_JS_JQUERY = "jquery3.3.1.min.js";
    String CONTENT_JS_THEME = "js/themes/modern/theme.min.js";

    String [] CONTENT_EDITOR_HEAD_RESOURCES = new String[]{
            "<link href=\"css/ustadmobile.css\" rel=\"stylesheet\" />",
            "<link href=\"css/bootstrap.min.css\" rel=\"stylesheet\" />",
            "<script src=\"js/jquery3.3.1.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"js/tinymce.min.js\" type=\"text/javascript\"></script>",
            "<script src=\"js/plugins/ustadmobile/UstadWidgets.js\" type=\"text/javascript\"></script>",
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

    void requestEditorContent();

    void handleContentMenu();

    void startWebServer();


}
