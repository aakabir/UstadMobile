package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;


/**
 * Deprecated: this si being replaced with Container which support de-duplicating entries
 */
@UmDao
@Deprecated
public abstract class ContentEntryFileStatusDao implements BaseDao<ContentEntryFileStatus> {

    @UmQuery("SELECT * FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract void findByContentEntryFileUid(long cefsContentEntryFileUid, UmCallback<ContentEntryFileStatus> callback);


    @UmQuery("UPDATE ContentEntryFileStatus SET filePath = :newContentEntryFilePath WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract void updateContentEntryFilePath(long cefsContentEntryFileUid, String newContentEntryFilePath, UmCallback<Integer> callback);

    @UmQuery("DELETE FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid IN(:cefsContentEntryFileUid)")
    public abstract void deleteByFileUids(List<Long> cefsContentEntryFileUid);

    @UmQuery("DELETE FROM ContentEntryFileStatus WHERE cefsUid = :statusUid")
    public abstract void deleteByUid(int statusUid);

}
