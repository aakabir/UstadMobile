package com.ustadmobile.core.view;

import com.ustadmobile.core.opf.UstadJSOPFItem;

import java.util.List;

public interface ContentEditorPageListView extends UstadView{
    String TAG = ContentEditorPageListView.class.getSimpleName();

    void updatePageList(List<UstadJSOPFItem> newPageList);

    void addNewPage(UstadJSOPFItem page);

    void removePage(UstadJSOPFItem page);

    void loadPage(UstadJSOPFItem page);

    void updatePage(UstadJSOPFItem page);

}
