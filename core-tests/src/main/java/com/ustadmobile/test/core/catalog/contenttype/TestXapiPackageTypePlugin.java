package com.ustadmobile.test.core.catalog.contenttype;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.test.core.UMTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test done against prototypes from:
 *  https://experienceapi.com/download-prototypes/
 *
 * Created by mike on 9/13/17.
 */

public class TestXapiPackageTypePlugin {

    String xapiFilePath;

    @Before
    public void extractFile() throws IOException{
        xapiFilePath = UMTestUtil.copyResourceToStorageDir("/com/ustadmobile/test/core/JsTetris_TCAPI.zip");
    }

    @Test
    public void testXapiPackageTypePlugin() {
//        XapiPackageTypePlugin plugin = new XapiPackageTypePlugin();
//        UstadJSOPDSFeed feed = plugin.getEntry(xapiFilePath, null).getFeed();
//        Assert.assertNotNull("Feed is not null", feed);
//        UstadJSOPDSEntry entry=  feed.getEntryById("http://id.tincanapi.com/activity/tincan-prototypes/tetris");
//        Assert.assertNotNull("Feed has entry matching id", entry);
//        UmOpdsLink entryLinks = entry.getFirstAcquisitionLink(null);
//        Assert.assertEquals("Mime type is application/zip", "application/zip",
//                entryLinks.getMimeType());
//        Assert.assertEquals("Title is as expected", "Tin Can Tetris Example", entry.getTitle());
    }

    @After
    public void removeFile() {
        if(xapiFilePath != null)
            new File(xapiFilePath).delete();
    }

}
