package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 43)
public class PersonGroup {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long groupUid;

    @UmSyncMasterChangeSeqNum
    private long groupMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long groupLocalCsn;

    @UmSyncLastChangedBy
    private int groupLastChangedBy;

    private String groupName;

    public long getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(long groupUid) {
        this.groupUid = groupUid;
    }

    public long getGroupMasterCsn() {
        return groupMasterCsn;
    }

    public void setGroupMasterCsn(long groupMasterCsn) {
        this.groupMasterCsn = groupMasterCsn;
    }

    public long getGroupLocalCsn() {
        return groupLocalCsn;
    }

    public void setGroupLocalCsn(long groupLocalCsn) {
        this.groupLocalCsn = groupLocalCsn;
    }

    public int getGroupLastChangedBy() {
        return groupLastChangedBy;
    }

    public void setGroupLastChangedBy(int groupLastChangedBy) {
        this.groupLastChangedBy = groupLastChangedBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}