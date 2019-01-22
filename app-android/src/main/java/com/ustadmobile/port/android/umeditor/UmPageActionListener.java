package com.ustadmobile.port.android.umeditor;

import com.ustadmobile.core.opf.UstadJSOPFItem;

import java.util.List;

public interface UmPageActionListener {

    void onOrderChanged(List<UstadJSOPFItem> newPageList);

    void onPageRemove(UstadJSOPFItem pageItem);

    void onPageCreate(UstadJSOPFItem pageItem);

    void onPageSelected(String pageHref);

    void onPageUpdate(UstadJSOPFItem pageItem);

}
