package com.ustadmobile.port.android.contenteditor;

/**
 * Interface which listen for core editor resources copy task.
 * Resources include css and js files.
 *
 * Task: Copying resources from assets to the editor temporary directory.
 *
 * @author kileha3
 */
public interface ContentEditorResourceCopyListener {

    /**
     * Invoked when all resources have been copied.
     */
    void onResouceReady();

}
