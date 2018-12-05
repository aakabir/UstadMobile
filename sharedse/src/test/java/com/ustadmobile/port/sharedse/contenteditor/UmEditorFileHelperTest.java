package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Test class which tests {@link UmEditorFileHelper} to make sure it behaves as expected
 * when editing/Creating files.
 *
 * @author kileha3
 */
public class UmEditorFileHelperTest {

    private UmEditorFileHelper umEditorFileHelper;

    private String indexTempFile = "index_.html";

    private String indexFile = "index.html";


    @Before
    public void setUpSpy(){
        Object context =  PlatformTestUtil.getTargetContext();
        umEditorFileHelper = new UmEditorFileHelper();
        umEditorFileHelper.init(context);
        UmAppDatabase umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

    }


    @Test
    public void givenContentEditorFIleHelper_whenCreateFileCalled_thenShouldCreateBlankFile(){
        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();
        umEditorFileHelper.createFile(new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        try { mLatch.await(20, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Empty file was created successfully", resultRef.get() != null
                && resultRef.get().endsWith(".zip") && new File(resultRef.get()).exists());

    }

    @Test
    public void givenContentEditorFileHelper_whenMountFileCalled_thenShouldBeAccessibleOnHttp(){
        CountDownLatch mLatch = new CountDownLatch(1);
        umEditorFileHelper.createFile(new UmCallback<String>() {
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

        try { mLatch.await(20, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("File was extracted to temporary directory",
                new File(umEditorFileHelper.getDestinationDirPath()).exists());
        try{

            URL url = new URL(UMFileUtil.joinPaths(umEditorFileHelper.getMountedTempDirRequestUrl(),
                    indexFile));
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();

            assertEquals("File was successfully mounted and can be accessed via HTTP",
                    HttpURLConnection.HTTP_OK, urlConn.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void givenContentEditorFileHelper_whenResourceAdded_thenShouldUpdateFileResources(){

    }

    @Test
    public void givenContentEditorFileHelper_whenUnUsedResourceFound_thenShouldBeRemoved(){
        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<Integer> resultRef = new AtomicReference<>();

        //create new file
        umEditorFileHelper.createFile(new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //mount newly created file
                umEditorFileHelper.mountFile(result, new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //copy index_.html file to media directory which won't be used at all
                        try {
                            InputStream is = new FileInputStream(new File(umEditorFileHelper
                                    .getDestinationDirPath(), indexTempFile));
                            File dest = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                                            indexTempFile);
                            boolean copied = UMFileUtil.copyFile(is,dest) ;

                            assertTrue("File was copied successfully",copied);
                            if(copied){
                                //remove unused files
                                umEditorFileHelper.removeUnUsedResources(new UmCallback<Integer>() {
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

        try { mLatch.await(20, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals("Un used file was removed successfully",
                1, (int) resultRef.get());
    }
}
