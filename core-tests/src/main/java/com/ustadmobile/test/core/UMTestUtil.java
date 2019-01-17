package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mike on 9/13/17.
 */

public class UMTestUtil {

    /**
     * Copy a given test resource into the first (shared) storage directory
     *
     * @return Complete path the resource was copied to
     */
    public static String copyResourceToStorageDir(String resourcePath) throws IOException{
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        OutputStream fileOut = null;
        InputStream entryIn = null;
        IOException ioe  = null;
        String outPath = null;
        try{
            entryIn = UMTestUtil.class.getResourceAsStream(resourcePath);

            Object context = PlatformTestUtil.getTargetContext();
            UMStorageDir[] storageDirs = impl.getStorageDirs(UstadMobileSystemImpl.SHARED_RESOURCE,
                    context);
            String outDir = storageDirs[0].getDirURI();
            if(new File(outDir).isDirectory()) {
                new File(outDir).mkdirs();
            }

            outPath = UMFileUtil.joinPaths(new String[]{outDir,
                    UMFileUtil.getFilename(resourcePath)});

            fileOut = new FileOutputStream(new File(outPath));
            UMIOUtils.readFully(entryIn, fileOut, 8*1024);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(entryIn);
            UMIOUtils.closeOutputStream(fileOut);
            UMIOUtils.throwIfNotNullIO(ioe);
        }

        return outPath;
    }

    /**
     * Test util to determine if the contents of two streams are equal
     * @param expectedStream
     * @param testStream
     * @return
     * @throws IOException
     */
    public static boolean areStreamsEqual(InputStream expectedStream, InputStream testStream) throws IOException {
        if(!(expectedStream instanceof BufferedInputStream)) {
            expectedStream = new BufferedInputStream(expectedStream);
        }

        if(!(testStream instanceof BufferedInputStream)) {
            testStream = new BufferedInputStream(testStream);
        }


        int bExpected;
        int bTest;
        boolean streamsEqual = true;
        do {
            bExpected = expectedStream.read();
            bTest = testStream.read();
            streamsEqual = bTest == bExpected;
        }while(streamsEqual && bExpected != -1);

        return streamsEqual;
    }


}
