package com.ustadmobile.port.android.umeditor;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;

import java.util.List;

public interface UmPageActionListener {

    void onOrderChanged(List<EpubNavItem> newPageList);

    void onPageRemove(String href);

    void onPageCreate(String title);

    void onPageSelected(String pageHref);

    void onPageUpdate(EpubNavItem pageItem);

}
