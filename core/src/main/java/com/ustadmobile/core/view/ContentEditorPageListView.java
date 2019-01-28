package com.ustadmobile.core.view;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;

import java.util.List;

public interface ContentEditorPageListView extends UstadView {

    String TAG = ContentEditorPageListView.class.getSimpleName();

    void updatePageList(List<EpubNavItem> newPageList);

    void addNewPage();

    void removePage(EpubNavItem page);

    void loadPage(EpubNavItem page);

    void updatePage(EpubNavItem page);

    void setTitle(String title);

    void updateDocumentTitle();

}
