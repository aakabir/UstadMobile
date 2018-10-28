package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_PREVIEW;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_REDO;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_TEXT_DIRECTION;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_UNDO;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_INSERT_FILLTHEBLANKS_QN;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_INSERT_MULTIPLE_CHOICE_QN;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_ALIGN_CENTER;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_ALIGN_JUSTIFY;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_ALIGN_LEFT;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_ALIGN_RIGHT;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_INDENT_DECREASE;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_INDENT_INCREASE;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_LIST_ORDERED;
import static com.ustadmobile.core.view.ContentEditorView.PARAGRAPH_FORMAT_LIST_UNORDERED;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_BOLD;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_FONT;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_ITALIC;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_STRIKE;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_SUB;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_SUP;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_UNDERLINE;

public class ContentEditorPresenter extends UstadBaseController<ContentEditorView> {

    private Hashtable args;
    public ContentEditorPresenter(Object context, Hashtable arguments, ContentEditorView view) {
        super(context, arguments, view);
        this.args = arguments;
    }


    public void handleFormatTypeClicked(String formatType, String param){
        view.runOnUiThread(() -> {
            switch (formatType){
                case TEXT_FORMAT_TYPE_BOLD:
                    view.setContentBold();
                    break;
                case TEXT_FORMAT_TYPE_ITALIC:
                    view.setContentItalic();
                    break;
                case TEXT_FORMAT_TYPE_STRIKE:
                    view.setContentStrikeThrough();
                    break;
                case TEXT_FORMAT_TYPE_UNDERLINE:
                    view.setContentUnderlined();
                    break;
                case TEXT_FORMAT_TYPE_SUP:
                    view.setContentSuperscript();
                    break;
                case TEXT_FORMAT_TYPE_SUB:
                    view.setContentSubScript();
                    break;
                case TEXT_FORMAT_TYPE_FONT:
                    view.setContentFontSize(param);
                    break;
                case PARAGRAPH_FORMAT_ALIGN_CENTER:
                    view.setContentCenterAlign();
                    break;
                case PARAGRAPH_FORMAT_ALIGN_LEFT:
                    view.setContentLeftAlign();
                    break;
                case PARAGRAPH_FORMAT_ALIGN_RIGHT:
                    view.setContentRightAlign();
                    break;
                case PARAGRAPH_FORMAT_ALIGN_JUSTIFY:
                    view.setContentJustified();
                    break;
                case PARAGRAPH_FORMAT_LIST_ORDERED:
                    view.setContentOrderedList();
                    break;
                case PARAGRAPH_FORMAT_LIST_UNORDERED:
                    view.setContentUnOrderList();
                    break;
                case PARAGRAPH_FORMAT_INDENT_DECREASE:
                    view.setContentDecreaseIndent();
                    break;
                case PARAGRAPH_FORMAT_INDENT_INCREASE:
                    view.setContentIncreaseIndent();
                    break;
                case ACTION_REDO:
                    view.setContentRedo();
                    break;
                case ACTION_UNDO:
                    view.setContentUndo();
                    break;
                case ACTION_TEXT_DIRECTION:
                    view.setContentTextDirection(Boolean.parseBoolean(param));
                    break;
                case ACTION_PREVIEW:
                    view.saveContentForPreview();
                    break;
                case CONTENT_INSERT_FILLTHEBLANKS_QN:
                    view.insertFillTheBlanksQuestion();
                    break;
                case CONTENT_INSERT_MULTIPLE_CHOICE_QN:
                    view.insertMultipleChoiceQuestion();
                    break;

            }
            view.handleContentMenu();
        });
    }

    public void handleContentPreview(){
        UstadMobileSystemImpl.getInstance()
                .go(ContentPreviewView.VIEW_NAME,args,view.getContext());
    }



    @Override
    public void setUIStrings() {

    }
}
