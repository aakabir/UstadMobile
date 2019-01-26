package com.ustadmobile.core.controller;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
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


    public void handleReOrderPages(List<EpubNavItem> pageList){
        view.runOnUiThread(() -> view.updatePageList(pageList));
    }

    public void handlePageSelected(EpubNavItem page){
        view.runOnUiThread(() -> view.loadPage(page));
    }

    public void handleRemovePage(EpubNavItem page){
        view.runOnUiThread(() -> view.removePage(page));
    }

    public void handleAddPage(){
        view.runOnUiThread(() -> view.addNewPage());
    }

    public void handleUpdatePage(EpubNavItem page){
        view.runOnUiThread(() -> view.updatePage(page));
    }
}
