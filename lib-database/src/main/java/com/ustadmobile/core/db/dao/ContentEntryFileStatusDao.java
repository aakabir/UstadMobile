package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

@UmDao
public abstract class ContentEntryFileStatusDao implements BaseDao<ContentEntryFileStatus> {

    @UmQuery("SELECT * FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract void findByContentEntryFileUid(long cefsContentEntryFileUid, UmCallback<ContentEntryFileStatus> callback);

    @UmQuery("DELETE FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract void deleteByContentEntryFileUid(long cefsContentEntryFileUid);
}
