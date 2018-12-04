package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link UmEditorFileHelper} to make sure it behaves as expected
 * when editing/Creating files.
 *
 * @author kileha3
 */
public class UmEditorFileHelperTest {

    private UmEditorFileHelper mockedHelper;

    private UmAppDatabase umAppDatabase;

    private UmAppDatabase repository;

    private ContentEntryDao mockedEntryDao;

    private ContentEntryFileDao mockedEntryFileDao;



    @Before
    public void setUpSpy(){
        Object context =  PlatformTestUtil.getTargetContext();
        mockedHelper = spy(UmEditorFileHelper.class);
        mockedHelper.init(context);
        UmAppDatabase mockedDb = mock(UmAppDatabase.class);
        umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

        mockedEntryDao = mock(ContentEntryDao.class);
        mockedEntryFileDao = mock(ContentEntryFileDao.class);
        UmAppDatabase.setInstance(mockedDb);
        when(mockedDb.getContentEntryDao()).thenReturn(mockedEntryDao);
        when(mockedDb.getContentEntryFileDao()).thenReturn(mockedEntryFileDao);

    }


    @Test
    public void givenContentEditorFIleHelper_whenCreateFileCalled_thenShouldCreateBlankFile(){
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();
        //mockedHelper.createFile();

    }

    @Test
    public void givenContentEditorFileHelper_whenMountFileCalled_thenShouldBeAccessibleOnHttp(){
        CountDownLatch mLatch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();

        mockedHelper.mountFile(0, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                mLatch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                mLatch.countDown();
            }
        });

        try { mLatch.await(20, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        String localHost = "12.0.0.1";
        assertTrue("Returned the right group information",
                resultRef.get() != null && resultRef.get().contains(localHost));
    }

    @Test
    public void givenContentEditorFileHelper_whenResourceAdded_thenShouldUpdateFileResources(){

    }

    @Test
    public void givenContentEditorFileHelper_whenUnUsedResourceFound_thenShouldBeRemoved(){

    }
}
