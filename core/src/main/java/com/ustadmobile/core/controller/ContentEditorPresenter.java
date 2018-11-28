package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ContentEditorView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_PREVIEW;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_REDO;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_TEXT_DIRECTION_LTR;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_TEXT_DIRECTION_RTL;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_UNDO;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_ENTRY_FILE_UID;
import static com.ustadmobile.core.view.ContentEditorView.CONTENT_INSERT_FILL_THE_BLANKS_QN;
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

    private String tinyMceBaseUrl;

    private String mountedFileBaseUrl;


    public ContentEditorPresenter(Object context, Hashtable arguments, ContentEditorView view) {
        super(context, arguments, view);
        this.args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    public void handleFiles(String baseResourceRequestUrl){
        tinyMceBaseUrl = baseResourceRequestUrl;
        args.put(ContentEditorView.EDITOR_REQUEST_URI,tinyMceBaseUrl);
        if(args.get(CONTENT_ENTRY_FILE_UID).equals("0")){
            view.getFileHelper().createFile(baseResourceRequestUrl,
                    new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long contentEntryFileUid) {
                            mountFile(contentEntryFileUid);
                        }

                        @Override
                        public void onFailure(Throwable exception) { }
                    });
        }else{
            long contentEntryFileUid = Long.parseLong(String.valueOf(args.get(CONTENT_ENTRY_FILE_UID)));
            mountFile(contentEntryFileUid);
        }
    }


    private void mountFile(long contentEntryFileUid){
        view.getFileHelper().mountFile(contentEntryFileUid, new UmCallback<String>() {
            @Override
            public void onSuccess(String requestUrl) {
                mountedFileBaseUrl = requestUrl;
                view.runOnUiThread(() -> view.injectTinyMce());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    public String getTinyMceBaseUrl(){
        return tinyMceBaseUrl;
    }

    public String getMountedFileBaseUrl(){
        return mountedFileBaseUrl;
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

                case ACTION_TEXT_DIRECTION_LTR:
                    view.setContentTextDirection(true);
                    break;

                case ACTION_TEXT_DIRECTION_RTL:
                    view.setContentTextDirection(false);
                    break;

                case ACTION_PREVIEW:
                    view.requestEditorContent();
                    break;

                case CONTENT_INSERT_FILL_THE_BLANKS_QN:
                    view.insertFillTheBlanksQuestion();
                    break;

                case CONTENT_INSERT_MULTIPLE_CHOICE_QN:
                    view.insertMultipleChoiceQuestion();
                    break;

            }
        });
    }


    @Override
    public void setUIStrings() {

    }
}
