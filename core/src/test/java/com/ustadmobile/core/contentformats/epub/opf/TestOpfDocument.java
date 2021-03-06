package com.ustadmobile.core.contentformats.epub.opf;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 10/17/17.
 */

public class TestOpfDocument {

    @Test
    public void givenValidOpf_whenLoaded_thenShouldHavePropertiesFromOpfFile() throws IOException, XmlPullParserException{
        InputStream opfIn = getClass().getResourceAsStream("TestOpfDocument-valid.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        OpfDocument opf = new OpfDocument();
        opf.loadFromOPF(parser);
        Assert.assertEquals("Title as expected", "The Little Chicks", opf.title);
        Assert.assertEquals("Id as expected", "202b10fe-b028-4b84-9b84-852aa766607d", opf.id);
        Assert.assertTrue("Spine loaded", opf.getSpine().size() > 0);
        Assert.assertEquals("Language loaded", "en-US", opf.getLanguages().get(0));
        Assert.assertEquals("Cover image as expected", "cover.png", opf.getCoverImage(null).href);
        Assert.assertEquals("Loaded author 1 as expected", "Benita Rowe",
                opf.getCreator(0).getCreator());
        Assert.assertEquals("Loaded author 1 as expected -id", "author1",
                opf.getCreator(0).getId());
        Assert.assertEquals("Loaded author 2 as expected", "Mike Dawson",
                opf.getCreator(1).getCreator());
        Assert.assertEquals("Loaded mime type as expected for page", "application/xhtml+xml",
                opf.getMimeType("Page_1.xhtml"));
    }

    @Test
    public void givenOpfLoaded_whenSerializedThenLoaded_shouldBeEqual() throws IOException, XmlPullParserException {
        InputStream opfIn = getClass().getResourceAsStream("TestOpfDocument-valid.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        OpfDocument opf = new OpfDocument();
        opf.loadFromOPF(parser);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        serializer.setOutput(bout, "UTF-8");
        opf.serialize(serializer);
        bout.flush();

        OpfDocument loadedOpf = new OpfDocument();
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                new ByteArrayInputStream(bout.toByteArray()), "UTF-8");
        loadedOpf.loadFromOPF(xpp);

        Assert.assertEquals("Original and reserialized title is the same", opf.getTitle(),
                loadedOpf.getTitle());
        Assert.assertEquals("Original and reserialized id is the same", opf.getId(),
                loadedOpf.getId());
        Assert.assertEquals("Original and reserialized opf has same number of manifest entries",
                opf.getManifestItems().size(), loadedOpf.getManifestItems().size());
        for(OpfItem item : opf.getManifestItems().values()) {
            OpfItem loadedItem = loadedOpf.getManifestItems().get(item.getId());
            Assert.assertNotNull("Manifest item id #" + item.getId() +
                    " present in reserialized manifest", loadedItem);
            Assert.assertEquals("Item id # " + item.getId() + " same href",
                    item.getHref(), loadedItem.getHref());
            Assert.assertEquals("Item id #" + item.getId() + " same mime type",
                    item.getMediaType(), loadedItem.getMediaType());
        }

        Assert.assertEquals("Original and reserialized TOC has same navitem",
                opf.getNavItem().getId(), loadedOpf.getNavItem().getId());

    }

}
