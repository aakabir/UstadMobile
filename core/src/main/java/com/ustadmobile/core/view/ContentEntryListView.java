package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;

import java.util.List;
import java.util.Map;

public interface ContentEntryListView extends UstadView {

    String VIEW_NAME = "ContentEntryList";

    void setContentEntryProvider(UmProvider<ContentEntryWithContentEntryStatus> entryProvider);

    void setToolbarTitle(String title);

    void showError();

    void setCategorySchemaSpinner(Map<Long, List<DistinctCategorySchema>> spinnerData);

    void setLanguageOptions(List<Language> result);
}
