package com.ustadmobile.core.view;

public interface ContentEditorPreviewFragmentView extends UstadView {

    String PAGE_ITEM = "page_item";

    String BASE_URL = "base_url";

    void setViewPager(Object object);

    void setPagedEnabled(boolean enabled);

    void setRequestBaseUri(String requestBaseUri);
}
