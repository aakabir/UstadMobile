package com.ustadmobile.core.controller;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;

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

    private boolean openPreviewRequest = false;

    private boolean isInEditorPreview = false;

    private String selectedPageToLoad = null;

    private boolean pageManagerOpen = false;


    public ContentEditorPresenter(Object context, Hashtable arguments, ContentEditorView view) {
        super(context, arguments, view);
        this.args = arguments;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }


    /**
     * Check container status, if exists pull its files and start editing
     * them otherwise create new one.
     */
    public void handleContainerStatus(){
        long entryUid = Long.parseLong(String.valueOf(args.get(CONTENT_ENTRY_FILE_UID)));
        UmAccountManager.getRepositoryForActiveAccount(context)
                .getContainerDao().getMostRecentContainerForContentEntryAsync(entryUid,
                new UmCallback<Container>() {
            @Override
            public void onSuccess(Container result) {
                if(result == null){
                    view.getFileHelper().createDocument(entryUid, new UmCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            mountDocumentDir(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }else{
                    new Thread(() -> {
                        ContainerEntry entry = UmAppDatabase.getInstance(context)
                                .getContainerEntryDao().findByUid(entryUid);
                        mountDocumentDir(entry.getCePath());
                    }).start();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    private void mountDocumentDir(String entryPath){

        UmCallback<String> updateDocumentCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result != null){
                    selectedPageToLoad = result;
                    view.runOnUiThread(() ->
                            view.handleSelectedPage());
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };


        view.getFileHelper().mountDocumentDir(entryPath, new UmCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if(!entryPath.isEmpty()){

                    List<EpubNavItem> pageList = view.getFileHelper()
                            .getEpubNavDocument().getToc().getChildren();

                    if(pageList == null || pageList.size() == 0){

                        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                        String documentTitle = impl.getString(MessageID.content_untitled_document,
                                view.getContext());

                        view.getFileHelper().updateDocumentTitle(documentTitle, true,
                                updateDocumentCallback);
                    }else{
                        selectedPageToLoad = view.getFileHelper()
                                .getEpubNavDocument().getToc().getChild(0).getHref();
                        view.runOnUiThread(() -> view.handleSelectedPage());
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


    /**
     * Handle all formatting action performed on UI
     * @param formatType Type of the format clicked
     * @param param parameter needed ie. font size when you want to change text font size
     */
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
    public boolean isOpenPreviewRequest() {
        return openPreviewRequest;
    }

    /**
     * Set preview activity navigation status
     * @param openPreviewRequest True if about to navigate to the preview activity
     */
    public void setOpenPreviewRequest(boolean openPreviewRequest) {
        this.openPreviewRequest = openPreviewRequest;
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

    /**
     * Get currently selected page
     * @return loaded to the editor
     */
    public String getSelectedPageToLoad() {
        return selectedPageToLoad;
    }

    /**
     * Set page to be loaded to the editor
     * @param selectedPageToLoad page href
     */
    public void setSelectedPageToLoad(String selectedPageToLoad) {
        this.selectedPageToLoad = selectedPageToLoad;
    }

    /**
     * Check if page manager opening was initiated
     * @return true if was iniatiated otherwise it wasn't
     */
    public boolean isPageManagerOpen() {
        return pageManagerOpen;
    }

    /**
     * Set frag to indicate when page manager is active.
     * @param pageManagerOpen flag to indicate if the page manager was opened or not
     */
    public void setPageManagerOpen(boolean pageManagerOpen) {
        this.pageManagerOpen = pageManagerOpen;
    }
}
