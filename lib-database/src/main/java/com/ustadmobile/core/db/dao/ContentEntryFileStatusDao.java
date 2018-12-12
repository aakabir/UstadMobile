package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryFileStatusDao implements SyncableDao<ContentEntryFileStatus, ContentEntryFileStatusDao> {

    @UmQuery("SELECT * FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract void findByContentEntryFileUid(long cefsContentEntryFileUid, UmCallback<ContentEntryFileStatus> callback);
}
