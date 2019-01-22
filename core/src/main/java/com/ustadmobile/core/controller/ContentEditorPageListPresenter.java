package com.ustadmobile.core.controller;

import com.ustadmobile.core.opf.UstadJSOPFItem;
import com.ustadmobile.core.view.ContentEditorPageListView;

import java.util.Hashtable;
import java.util.List;

public class ContentEditorPageListPresenter
        extends UstadBaseController<ContentEditorPageListView> {

    public ContentEditorPageListPresenter(Object context, Hashtable arguments, ContentEditorPageListView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    @Override
    public void setUIStrings() {

    }

    public void handleReOrderPages(List<UstadJSOPFItem> pageList){
        view.runOnUiThread(() -> view.updatePageList(pageList));
    }

    public void handlePageSelected(UstadJSOPFItem page){
        view.runOnUiThread(() -> view.loadPage(page));
    }

    public void handleRemovePage(UstadJSOPFItem page){
        view.runOnUiThread(() -> view.removePage(page));
    }

    public void handleAddPage(UstadJSOPFItem page){
        view.runOnUiThread(() -> view.addNewPage(page));
    }

    public void handleUpdatePage(UstadJSOPFItem page){
        view.runOnUiThread(() -> view.updatePage(page));
    }
}
