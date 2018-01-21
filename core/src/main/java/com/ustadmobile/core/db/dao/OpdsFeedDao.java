package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsFeed;

/**
 * Created by mike on 1/13/18.
 */

public abstract class OpdsFeedDao {

    @UmQuery("Select * From \"opds_feed\" WHERE \"url\" = :url ")
    public abstract UmLiveData<OpdsFeed> getFeedByUrl(String url);

    @UmInsert
    public abstract long insert(OpdsFeed feed);

    public abstract void update(OpdsFeed feed);

}