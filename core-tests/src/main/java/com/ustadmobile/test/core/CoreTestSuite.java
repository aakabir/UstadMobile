package com.ustadmobile.test.core;

import com.ustadmobile.core.db.entities.TestOpdsFeed;
import com.ustadmobile.test.core.catalog.contenttype.TestXapiPackageTypePlugin;
import com.ustadmobile.test.core.impl.TestUstadJSOPF;
import com.ustadmobile.test.core.scorm.TestScormManifest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/17/17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestImageLoader.class,
        TestXapiPackageTypePlugin.class,
//        TestUstadJSOPDSFeed.class,
//        TestUstadJSOPDSEntry.class,
        TestUstadJSOPF.class,
        TestUmCalendarUtil.class,
        TestScormManifest.class,
        TestOpdsFeed.class
})
public abstract class CoreTestSuite {
}
