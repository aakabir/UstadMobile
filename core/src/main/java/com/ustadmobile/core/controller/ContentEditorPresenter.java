package com.ustadmobile.core.controller;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_INSERT_CONTENT;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_REDO;
import static com.ustadmobile.core.view.ContentEditorView.ACTION_SELECT_ALL;
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

    private boolean isEditorInitialized = false;

    private boolean isEditingModeOn = false;

    private boolean openPreview = false;

    private boolean isMultimediaFilePicker = false;

    private boolean fileNotFound = false;

    private boolean isInEditorPreview = false;

    private String selectedPageToLoad = null;


    public ContentEditorPresenter(Object context, Hashtable arguments, ContentEditorView view) {
        super(context, arguments, view);
        this.args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    /**
     * Check if content entry has a local file associated with it, if yes edit otherwise create new one.
     */
    public void handleContentEntryFileStatus(){
        long contentEntryFileUid = Long.parseLong(String.valueOf(args.get(CONTENT_ENTRY_FILE_UID)));
        ContentEntryFileStatusDao fileStatusDao =
                UmAppDatabase.getInstance(context).getContentEntryFileStatusDao();
        fileStatusDao.findByContentEntryFileUid(contentEntryFileUid,
                new UmCallback<ContentEntryFileStatus>() {
            @Override
            public void onSuccess(ContentEntryFileStatus result) {
                if(result == null){
                    view.getFileHelper().createFile(contentEntryFileUid,new UmCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            mountFile(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });

                }else{
                    if(!new File(result.getFilePath()).exists()){
                        view.runOnUiThread(() -> {
                            fileNotFound = true;
                            view.showNotFoundErrorMessage();
                        });
                    }else{
                        mountFile(result.getFilePath());
                    }
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    private void mountFile(String filePath){
        view.getFileHelper().mountFile(filePath, new UmCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if(filePath.length() > 0){
                    List<EpubNavItem> pageList = view.getFileHelper()
                            .getEpubNavDocument().getToc().getChildren();
                    if(pageList.size() == 0){
                        String pageTitle = UstadMobileSystemImpl.getInstance()
                                .getString(MessageID.content_untitled_page, view.getContext());
                        view.getFileHelper().addPage(pageTitle,
                                new UmCallback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result != null){
                                            selectedPageToLoad = result;
                                            view.runOnUiThread(() -> view.handleSelectedPage());
                                        }
                                    }
                                    @Override
                                    public void onFailure(Throwable exception) {

                                    }
                                });
                    }
                }else{
                    view.runOnUiThread(() -> view.showNotFoundErrorMessage());
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
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
                    view.setContentTextDirection(formatType);
                    break;

                case ACTION_TEXT_DIRECTION_RTL:
                    view.setContentTextDirection(formatType);
                    break;

                case CONTENT_INSERT_FILL_THE_BLANKS_QN:
                    view.insertFillTheBlanksQuestion();
                    break;

                case CONTENT_INSERT_MULTIPLE_CHOICE_QN:
                    view.insertMultipleChoiceQuestion();
                    break;
                case ACTION_INSERT_CONTENT:
                    view.insertContent(param);
                    break;
                case ACTION_SELECT_ALL:
                    view.selectAllContent();
                    break;

            }
        });
    }

    /**
     * Check if tinymce is initialized on currenlty loaded page
     * @return initialization status.
     */
    public boolean isEditorInitialized() {
        return isEditorInitialized;
    }

    /**
     * Set tinymce initialization status
     * @param editorInitialized True if tinymce is inialized on the currently loaded page potherwise
     *                          false.
     */
    public void setEditorInitialized(boolean editorInitialized) {
        isEditorInitialized = editorInitialized;
    }

    /**
     * Check if the editing mode is on.
     * @return mode status
     */
    public boolean isEditingModeOn() {
        return isEditingModeOn;
    }

    /**
     * Set editing mode status
     * @param editingModeOn True if the edditing mode is on, otherwise the editor
     *                      is not in editing mode.
     */
    public void setEditingModeOn(boolean editingModeOn) {
        isEditingModeOn = editingModeOn;
    }

    /**
     * Check if you are about to navigate to preview activity
     * @return preview status flag.
     */
    public boolean isOpenPreview() {
        return openPreview;
    }

    /**
     * Set preview activity navigation status
     * @param openPreview True if about to navigate to the preview activity
     */
    public void setOpenPreview(boolean openPreview) {
        this.openPreview = openPreview;
    }

    /**
     * Check if the multimedia picker is opened or not
     * @return Picker status
     */
    public boolean isMultimediaFilePicker() {
        return isMultimediaFilePicker;
    }

    /**
     * Set multimedia picker opened status
     * @param multimediaFilePicker True if the picker is opened otherwise the picker isn't opened.
     */
    public void setMultimediaFilePicker(boolean multimediaFilePicker) {
        isMultimediaFilePicker = multimediaFilePicker;
    }

    /**
     * Check if file does exists on device
     * @return True if file exists on device otherwise the file has been deleted by the user.
     */
    public boolean isFileNotFound() {
        return fileNotFound;
    }

    /**
     * Set file existence's status
     * @param fileNotFound True if the file is on user device otherwise false.
     */
    public void setFileNotFound(boolean fileNotFound) {
        this.fileNotFound = fileNotFound;
    }

    /**
     * Check if the content is being previewed on the editor
     * @return
     */
    public boolean isInEditorPreview() {
        return isInEditorPreview;
    }

    /**
     * Set if the preview is done on the editor itself.
     * @param inEditorPreview True if is editor preview otherwise false.
     */
    public void setInEditorPreview(boolean inEditorPreview) {
        isInEditorPreview = inEditorPreview;
    }

    public String getSelectedPageToLoad() {
        return selectedPageToLoad;
    }

    public void setSelectedPageToLoad(String selectedPageToLoad) {
        this.selectedPageToLoad = selectedPageToLoad;
    }

}
