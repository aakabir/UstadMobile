package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class UmEditorFileHelper implements UmEditorFileHelperCore {

    protected Object context;

    protected EmbeddedHTTPD embeddedHTTPD;

    protected String baseRequestUrl = null;

    protected ZipFileTaskProgressListener zipTaskListener;

    protected static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    protected String assetsDir = "assets-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "";

    /**
     * Interface which used to report file zipping progress update
     */
    public interface ZipFileTaskProgressListener {

        /**
         * Invoked when zip task process started
         */
        void onTaskStarted();
        /**
         * Invoked when there is a progress update during zipping process
         * @param progress new progress value
         */
        void onTaskProgressUpdate(int progress);

        /**
         * Invoked when zip task process is completed.
         */
        void onTaskCompleted();
    }

    /**
     * Initialize UmEditorFileHelper
     * @param context activity context
     */
    public void  init(Object context){
        this.context = context;
        startWebServer();
    }

    private void startWebServer() {
        embeddedHTTPD = new EmbeddedHTTPD(0, this);
        try {
            embeddedHTTPD.start();
            baseRequestUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS+embeddedHTTPD.getListeningPort()+"/",
                    assetsDir,"tinymce");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
