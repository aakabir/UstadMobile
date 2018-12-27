package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 47)
public class EntityRole {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long erUid;

    @UmSyncMasterChangeSeqNum
    private long erMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long erLocalCsn;

    @UmSyncLastChangedBy
    private int erLastChangedBy;

    private int erTableId;

    private long erEntityUid;

    private long erGroupUid;

    private long erRoleUid;

    //bit flags made of up PERMISSION_ constants
    private long erPermissions;

    public static final long PERMISSION_SELECT = 1;

    public static final long PERMISSION_INSERT = 2;

    public static final long PERMISSION_UPDATE = 4;

    public static final long PERMISSION_CLAZZ_RECORD_ATTENDANCE = 8;

    public static final long PERMISSION_CLAZZ_RECORD_ACTIVITY = 16;

    public static final long PERMISSION_CLAZZ_RECORD_SEL = 32;

    public static final long PERMISSION_CLAZZ_VIEW_ATTENDANCE = 64;

    public static final long PERMISSION_CLAZZ_VIEW_ACTIVITY = 128;

    public static final long PERMISSION_CLAZZ_VIEW_SEL = 256;

    public long getErUid() {
        return erUid;
    }

    public void setErUid(long erUid) {
        this.erUid = erUid;
    }

    public int getErTableId() {
        return erTableId;
    }

    public void setErTableId(int erTableId) {
        this.erTableId = erTableId;
    }

    public long getErEntityUid() {
        return erEntityUid;
    }

    public void setErEntityUid(long erEntityUid) {
        this.erEntityUid = erEntityUid;
    }

    public long getErGroupUid() {
        return erGroupUid;
    }

    public void setErGroupUid(long erGroupUid) {
        this.erGroupUid = erGroupUid;
    }

    public long getErRoleUid() {
        return erRoleUid;
    }

    public void setErRoleUid(long erRoleUid) {
        this.erRoleUid = erRoleUid;
    }

    public long getErMasterCsn() {
        return erMasterCsn;
    }

    public void setErMasterCsn(long erMasterCsn) {
        this.erMasterCsn = erMasterCsn;
    }

    public long getErLocalCsn() {
        return erLocalCsn;
    }

    public void setErLocalCsn(long erLocalCsn) {
        this.erLocalCsn = erLocalCsn;
    }

    public int getErLastChangedBy() {
        return erLastChangedBy;
    }

    public void setErLastChangedBy(int erLastChangedBy) {
        this.erLastChangedBy = erLastChangedBy;
    }

    public long getErPermissions() {
        return erPermissions;
    }

    public void setErPermissions(long erPermissions) {
        this.erPermissions = erPermissions;
    }
}