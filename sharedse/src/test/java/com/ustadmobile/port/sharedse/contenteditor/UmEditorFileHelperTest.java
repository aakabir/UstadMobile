package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class which tests {@link UmEditorFileHelper} to make sure it behaves as expected
 * when creating, mounting , zipping files , updating opf and nav documents.
 *
 * @author kileha3
 */
public class UmEditorFileHelperTest {

    private UmEditorFileHelper umEditorFileHelper;

    private String indexFile = "template_page.html";

    private String pageTitle = "Sample page title";

    private static final int MAX_WAITING_TIME = 20;


    @Before
    public void setup(){
        Object context =  PlatformTestUtil.getTargetContext();
        umEditorFileHelper = new UmEditorFileHelper();
        umEditorFileHelper.init(context);
        umEditorFileHelper.setZipTaskProgressListener(
                new UmEditorFileHelper.ZipFileTaskProgressListener() {
            @Override
            public void onTaskStarted() { }

            @Override
            public void onTaskProgressUpdate(int progress) { }

            @Override
            public void onTaskCompleted() { }
        });
        UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

    }


    @Test
    public void givenContentEditorFIleHelper_whenCreateFileCalled_thenShouldCreateBlankFile()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();

        umEditorFileHelper.createFile(1L,new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);


        assertNotNull("Empty file object returned not null", resultRef.get());

        assertTrue("Empty file was created successfully", resultRef.get() != null
                && resultRef.get().endsWith(".zip") && new File(resultRef.get()).exists());

    }

    @Test
    public void givenContentEditorFileHelper_whenMountFileCalled_thenShouldBeAccessibleOnHttp()
            throws IOException, InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        umEditorFileHelper.createFile(1L,new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        mLatch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        URL url = new URL(UMFileUtil.joinPaths(umEditorFileHelper.getMountedTempDirRequestUrl(),
                indexFile));
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.connect();
        int responseCode = urlConn.getResponseCode();
        urlConn.disconnect();


        assertTrue("File was extracted to temporary directory",
                new File(umEditorFileHelper.getTempDestinationDirPath()).exists());

        assertEquals("File was successfully mounted and can be accessed via HTTP",
                HttpURLConnection.HTTP_OK, responseCode);

    }

    @Test
    public void givenContentEditorFileHelper_whenUnUsedResourceFound_thenShouldBeRemoved()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Integer> resultRef = new AtomicReference<>();
        AtomicReference<Boolean> copyResultRef = new AtomicReference<>();

        //create new file
        umEditorFileHelper.createFile(1L, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //mount newly created file
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //copy index_.html file to media directory which won't be used at all
                        try {
                            InputStream is = new FileInputStream(new File(umEditorFileHelper
                                    .getTempDestinationDirPath(), indexFile));
                            File dest = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                                    indexFile);
                            boolean copied = UMFileUtil.copyFile(is,dest) ;
                            copyResultRef.set(copied);

                            if(copied){
                                //remove unused files
                                umEditorFileHelper.removeUnUsedResources(new UmCallback<Integer>(){
                                    @Override
                                    public void onSuccess(Integer result) {
                                        resultRef.set(result);
                                        mLatch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });
                            }else{
                                mLatch.countDown();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("File was added successfully",copyResultRef.get());

        assertEquals("Unused resource was removed successfully",
                1, (int) resultRef.get());
    }


    @Test
    public void givenContentUpdated_whenTmpDirZipped_thenShouldBeUpdatedInZip()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<Boolean> zipResultRef = new AtomicReference<>();
        AtomicReference<Boolean> copyResultRef = new AtomicReference<>();

        //create new file
        umEditorFileHelper.createFile(1L, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //mount newly created file
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //update media directory with temp file
                        try {
                            InputStream is = new FileInputStream(new File(umEditorFileHelper
                                    .getTempDestinationDirPath(), indexFile));
                            File dest = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                                    indexFile);
                            boolean copied = UMFileUtil.copyFile(is,dest);
                            copyResultRef.set(copied);

                            if(copied){
                                //zip temp dir
                                umEditorFileHelper.updateFile(new UmCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        zipResultRef.set(result);
                                        mLatch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {

                                    }
                                });
                            }else{
                                mLatch.countDown();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("File was added successfully",copyResultRef.get());

        assertTrue("Temp directory was zipped successfully",zipResultRef.get());
    }

    @Test
    public void givenContentAddedToTmpDir_whenTmpDirZipped_thenShouldBeInZip()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<Boolean> zipResultRef = new AtomicReference<>();
        AtomicReference<Boolean> zipCheckRef = new AtomicReference<>();
        AtomicReference<Boolean> updateResultRef = new AtomicReference<>();

        //create new file
        umEditorFileHelper.createFile(1L,new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //mount newly created file
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //update media directory with temp file
                        try {
                            InputStream is = new FileInputStream(new File(umEditorFileHelper
                                    .getTempDestinationDirPath(), indexFile));
                            File dest = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                                    indexFile);
                            boolean copied = UMFileUtil.copyFile(is,dest) ;
                            updateResultRef.set(copied);

                            if(copied){
                                //zip temp dir
                                umEditorFileHelper.updateFile(new UmCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        zipResultRef.set(result);

                                        umEditorFileHelper.mountFile(
                                                umEditorFileHelper.getSourceFilePath(),
                                                new UmCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                File fileInZip = new File(umEditorFileHelper
                                                        .getDestinationMediaDirPath(), indexFile);
                                                zipCheckRef.set(fileInZip.exists());
                                                mLatch.countDown();
                                            }

                                            @Override
                                            public void onFailure(Throwable exception) {
                                                exception.printStackTrace();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });
                            }else{
                                mLatch.countDown();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);


        assertTrue("Temporary directory was updated successfully",updateResultRef.get());

        assertTrue("Temporary directory was zipped successfully",zipResultRef.get());

        assertTrue("Zipped temporary directory has newly added file",zipCheckRef.get());

    }

    @Test
    public void givenActiveEditor_whenNewPageIsAdded_thenShouldUpdateSpineItemsAndNavItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<String> hrefRef = new AtomicReference<>();

        umEditorFileHelper.addPage(pageTitle, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hrefRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });


        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertNotNull("Spine and nav items were updated ",hrefRef.get());
    }

    @Test
    public void givenActiveEditor_whenPageRemoved_thenShouldUpdateNavItemsAndSpineItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<String> href = new AtomicReference<>();
        AtomicReference<Boolean> resultRef = new AtomicReference<>();

        umEditorFileHelper.addPage(pageTitle, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                href.set(result);
                umEditorFileHelper.removePage(result, new UmCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean removed) {
                        resultRef.set(removed);
                        mLatch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertNotNull("Page was added successfully ",href.get());

        assertTrue("Nav items and spine items were updated on page removal ",
                resultRef.get());

    }


    @Test
    public void givenActiveEditor_whenMediaContentIsAdded_thenShouldUpdateManifestItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<Boolean> resultRef = new AtomicReference<>();

        String filename = "sampleFile.mp4", fileMimeType = "video/mp4";

        umEditorFileHelper.updateManifestItems(filename, fileMimeType, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("Manifest items were updated successfully",
                resultRef.get());
    }

    @Test
    public void givenActiveEditor_whenMediaContentIsRemoved_thenShouldUpdateManifestItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Integer> resultRef = new AtomicReference<>();
        AtomicReference<Boolean> copyResultRef = new AtomicReference<>();
        AtomicReference<Boolean> manifestRef = new AtomicReference<>();


        //create new file
        umEditorFileHelper.createFile(1L, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //mount newly created file
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //copy index_.html file to media directory which won't be used at all
                        try {
                            InputStream is = new FileInputStream(new File(umEditorFileHelper
                                    .getTempDestinationDirPath(), indexFile));
                            File dest = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                                    indexFile);
                            boolean copied = UMFileUtil.copyFile(is,dest) ;
                            copyResultRef.set(copied);

                            if(copied){
                                //remove unused files
                                umEditorFileHelper.removeUnUsedResources(new UmCallback<Integer>(){
                                    @Override
                                    public void onSuccess(Integer result) {
                                        resultRef.set(result);
                                        manifestRef.set(result > 0);
                                        mLatch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });
                            }else{
                                mLatch.countDown();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("File was added successfully",copyResultRef.get());

        assertEquals("Unused resource was removed successfully",
                1, (int) resultRef.get());

        assertTrue("Manifest items were updates successfully",manifestRef.get());

    }

    @Test
    public void givenNavigationListItems_whenOrderChanges_thenShouldUpdateNavItems()
            throws XmlPullParserException, IOException, InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<Boolean> resultRef = new AtomicReference<>();

        List<EpubNavItem> oldListOrder = getEpubNavDoc().getToc().getChildren();

        List<EpubNavItem> newListOrder = new ArrayList<>(oldListOrder);
        EpubNavItem navItem = newListOrder.remove(0);
        newListOrder.add(navItem);

        umEditorFileHelper.changePageOrder(newListOrder, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        List<EpubNavItem> navItems = getEpubNavDoc().getToc().getChildren();

        assertTrue("Nav items order was successfully changed",
                resultRef.get());

        assertEquals("New order is exactly as nav doc current order",
                newListOrder,navItems);

    }


    private EpubNavDocument getEpubNavDoc() throws XmlPullParserException, IOException {
        EpubNavDocument navDoc = new EpubNavDocument();
        InputStream docIn = getClass().getResourceAsStream("EpubNavDocument.xhtml");

        navDoc.load(UstadMobileSystemImpl.getInstance().newPullParser(docIn, "UTF-8"));
        return navDoc;
    }
}
