package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;

public abstract class UmEditorFileHelper implements UmEditorFileHelperCore {

    public Object context;

    protected ZipFileTaskProgressListener zipTaskListener;

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
     * Constructor used when creating instance of UmEditorFileHelper
     * @param context activity context object
     */
    public UmEditorFileHelper(Object context){
        this.context = context;
    }
}
