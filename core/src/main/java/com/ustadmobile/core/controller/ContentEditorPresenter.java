package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_PREVIEW;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_REDO;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_TEXT_DIRECTION;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_UNDO;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_INSERT_FILLTHEBLANKS_QN;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_INSERT_MULTIPLE_CHOICE_QN;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_ROOT_DIR;
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
import static com.ustadmobile.core.view.ContentPreviewView.FILE_NAME;

public class ContentEditorPresenter extends UstadBaseController<ContentEditorView> {

    private Hashtable args;
    private HashMap<String,File> contentDirMap;
    public ContentEditorPresenter(Object context, Hashtable arguments, ContentEditorView view) {
        super(context, arguments, view);
        this.args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> contentDirMap = view.createContentDir());
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
                    view.requestEditorContent(true);
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

    /**
     * Handle navigation to the preview screen
     * @param fileContent content to be previewed
     */
    public void handleContentPreview(String fileContent){
        args.put(ContentPreviewView.EDITOR_CONTENT,fileContent);
        UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                args,view.getContext());
    }

    /**
     * Write editor content to the file on the disk
     * @param fileContent content to be written
     */
    public void handleSavingStandAloneFile(String fileContent){
        File standAloneFile = new File(contentDirMap.get(CONTENT_ROOT_DIR),
                args.get(FILE_NAME).toString());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(standAloneFile))) {
            writer.write(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void setUIStrings() {

    }
}
