package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
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

    private String pageTitle = "Sample page title";

    private String videoFile = "BigBuckBunny.mp4";

    private String videoMimeType = "video/mp4";

    private static final int MAX_WAITING_TIME = 20;

    private long contentEntryUid = 1L;


    @Before
    public void setup(){
        Object context =  PlatformTestUtil.getTargetContext();
        umEditorFileHelper = new UmEditorFileHelper();
        umEditorFileHelper.init(context);
        UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

    }


    @Test
    public void givenContentEditorFileHelper_whenCreateDocumentCalled_thenShouldCreateBlankDocument()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();

        umEditorFileHelper.createDocument(contentEntryUid, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                resultRef.set(null);
                mLatch.countDown();
            }
        });

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);


        assertNotNull("Empty file object returned not null", resultRef.get());

        assertTrue("Blank document was created successfully", resultRef.get() != null
                && new File(resultRef.get()).exists());

    }

    @Test
    public void givenContentEditorFileHelper_whenMountDocumentCalled_thenShouldBeAccessibleOnHttp()
            throws IOException, InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();

        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        processBlankDocument(addPageCallback, mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        URL url = new URL(UMFileUtil.joinPaths(umEditorFileHelper.getMountedFileAccessibleUrl(), resultRef.get()));
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.connect();
        int responseCode = urlConn.getResponseCode();
        urlConn.disconnect();


        assertTrue("File was extracted to temporary directory",
                new File(umEditorFileHelper.getDocumentDirPath()).exists());

        assertEquals("File was successfully mounted and can be accessed via HTTP",
                HttpURLConnection.HTTP_OK, responseCode);

    }


    @Test
    public void givenContentAddedToDocument_whenEditing_thenShouldBeInTheDocument()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();


        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };


        processBlankDocument(addPageCallback,mLatch);
        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("Newly created page does exists in the document",
                new File(umEditorFileHelper.getDocumentDirPath(),resultRef.get()).exists());

    }

    @Test
    public void givenActiveEditor_whenNewPageIsAdded_thenShouldUpdateSpineItemsAndNavItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<String> hrefRef = new AtomicReference<>();

        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                hrefRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        processBlankDocument(addPageCallback,mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertNotNull("Spine and nav items were updated ",hrefRef.get());
    }

    @Test
    public void givenActiveEditor_whenPageRemoved_thenShouldUpdateNavItemsAndSpineItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);

        AtomicReference<String> href = new AtomicReference<>();
        AtomicReference<Boolean> resultRef = new AtomicReference<>();


        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                href.set(result);
                //add another page to make sure a page can be deleted - you cant have a document without a page
                umEditorFileHelper.addPage(pageTitle, new UmCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
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
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        processBlankDocument(addPageCallback, mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertNotNull("Page was added successfully ",href.get());

        assertTrue("Nav items and spine items were updated on page removal ",
                resultRef.get());

    }


    @Test
    public void givenActiveEditor_whenMediaContentIsAdded_thenShouldUpdateManifestItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Boolean> manifestUpdateRef = new AtomicReference<>();
        AtomicReference<Boolean> mediaAddRef = new AtomicReference<>();

        UmCallback<String> addPageCallback  = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {

                try{
                    InputStream fileIn =
                            getClass().getResourceAsStream(videoFile);
                    File dest = new File(umEditorFileHelper.getMediaDirectory(), videoFile);
                    if(dest.exists())dest.delete();
                    boolean mediaAdded  = UMFileUtil.copyFile(fileIn, dest);
                    mediaAddRef.set(mediaAdded);
                    if(mediaAdded){
                        umEditorFileHelper.updateManifestItems(videoFile, videoMimeType,
                                new UmCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        manifestUpdateRef.set(result);
                                        mLatch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });
                    }else {
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
        };

        processBlankDocument(addPageCallback, mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("Media content was added successfully", mediaAddRef.get());

        assertTrue("Manifest items were updated successfully", mediaAddRef.get());
    }

    @Test
    public void givenActiveEditor_whenMediaContentIsRemoved_thenShouldUpdateManifestItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Integer> unUsedResourceRef = new AtomicReference<>();
        AtomicReference<Boolean> mediaAddRef = new AtomicReference<>();
        AtomicReference<Boolean> manifestUpdateRef = new AtomicReference<>();


        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try{
                    InputStream fileIn =
                            getClass().getResourceAsStream(videoFile);
                    File dest = new File(umEditorFileHelper.getMediaDirectory(), videoFile);
                    if(dest.exists())dest.delete();
                    boolean mediaAdded  = UMFileUtil.copyFile(fileIn, dest);
                    mediaAddRef.set(mediaAdded);
                    if(mediaAdded){
                        umEditorFileHelper.updateManifestItems(dest.getName(),
                                videoMimeType, new UmCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                if(result){
                                    umEditorFileHelper.removeUnUsedResources(new UmCallback<Integer>(){
                                        @Override
                                        public void onSuccess(Integer result) {
                                            unUsedResourceRef.set(result);
                                            manifestUpdateRef.set(result > 0);
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
        };

        processBlankDocument(addPageCallback, mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("File was added successfully",mediaAddRef.get());

        assertEquals("Unused resource was removed successfully",
                1, (int) unUsedResourceRef.get());

        assertTrue("Manifest items were updates successfully",manifestUpdateRef.get());

    }

    @Test
    public void givenNavigationListItems_whenOrderChanges_thenShouldUpdateNavItems()
            throws InterruptedException {

        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Integer> resultPageRef = new AtomicReference<>();
        AtomicReference<Boolean> orderChangeRef = new AtomicReference<>();

        List<EpubNavItem> newListOrder = new ArrayList<>();
        List<EpubNavItem> oldListOrder = new ArrayList<>();

       UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultPageRef.set(1);
                umEditorFileHelper.addPage(pageTitle, new UmCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        resultPageRef.set(2);
                        oldListOrder.addAll(umEditorFileHelper.getEpubNavDocument()
                                .getToc().getChildren());
                        newListOrder.addAll(oldListOrder);
                        EpubNavItem navItem = newListOrder.remove(0);
                        newListOrder.add(navItem);

                        umEditorFileHelper.changePageOrder(newListOrder, new UmCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                orderChangeRef.set(result);
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
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        processBlankDocument(addPageCallback , mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertEquals("Document had exactly two pages", 2,
                (int) resultPageRef.get());

        assertTrue("Order was updated successfully",orderChangeRef.get());

    }

    @Test
    public void givenActiveEditor_whenNewPageIsAdded_thenPageNumberShouldBeIncremented()
            throws InterruptedException {
        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> firstPageRef = new AtomicReference<>();
        AtomicReference<String> secondPageRef = new AtomicReference<>();

        UmCallback<String> addPageCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                firstPageRef.set(result);
                umEditorFileHelper.addPage(pageTitle, new UmCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        secondPageRef.set(result);
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
        };

        processBlankDocument(addPageCallback, mLatch);

        mLatch.await(MAX_WAITING_TIME, TimeUnit.SECONDS);

        assertTrue("Fist page name was assigned number 1 after (page_) prefix",
                firstPageRef.get().contains("_1"));

        assertTrue("Second page name was assigned number 2 after (page_) prefix",
                secondPageRef.get().contains("_2"));

    }


    private void processBlankDocument(UmCallback<String> addPageCallback, CountDownLatch mLatch){

        UmCallback<Boolean> updateFileCallback = new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result){
                    List<EpubNavItem> pageList =
                            umEditorFileHelper.getEpubNavDocument().getToc().getChildren();
                    if(pageList == null || pageList.size() == 0){
                        umEditorFileHelper.addPage(pageTitle,addPageCallback);
                    }else{
                        mLatch.countDown();
                    }
                }else{
                    mLatch.countDown();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        UmCallback<Void> mountFileCallback = new UmCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                umEditorFileHelper.updateDocumentTitle(pageTitle,true,updateFileCallback);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        UmCallback<String> createDocumentCallback = new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                umEditorFileHelper.mountDocumentDir(result,mountFileCallback);

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        };

        umEditorFileHelper.createDocument(contentEntryUid,createDocumentCallback);
    }

}
