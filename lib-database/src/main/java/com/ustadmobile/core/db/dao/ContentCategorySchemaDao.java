package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentCategorySchemaDao implements SyncableDao<ContentCategorySchema, ContentCategorySchemaDao> {

    @UmQuery("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    public abstract ContentCategorySchema findBySchemaUrl(String schemaUrl);

    @UmUpdate
    public abstract void update(ContentCategorySchema entity);

    @UmQuery("SELECT ContentCategorySchema.* FROM ContentCategorySchema")
    public abstract List<ContentCategorySchema> getPublicContentCategorySchemas();

}
