package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.impl.UmCallback;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test class which tests {@link ContentEditorFileHelper} to make sure it behaves as expected
 * when editing/Creating files.
 *
 * @author kileha3
 */
public class ContentEditorFileHelperTest {

    @Test
    public void givenContentEditorFIleHelper_whenCreateFileCalled_thenShouldCreateBlankFile(){

    }

    @Test
    public void givenContentEditorFileHelper_whenMountFileCalled_thenShouldBeAccessibleOnHttp(){
        CountDownLatch latch = new CountDownLatch(1);

        ContentEditorFileHelper helper = null;
        AtomicReference<String> resultRef = new AtomicReference<>();

        helper.mountFile(0, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                latch.countDown();
            }
        });

        try { latch.await(60, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void givenContentEditorFileHelper_whenResourceAdded_thenShouldUpdateFileResources(){

    }

    @Test
    public void givenContentEditorFileHelper_whenUnUsedResourceFound_thenShouldBeRemoved(){

    }
}
