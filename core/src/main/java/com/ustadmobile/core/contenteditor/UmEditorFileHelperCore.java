package com.ustadmobile.core.contenteditor;

import com.ustadmobile.core.impl.UmCallback;

/**
 * Interface which defines all file operation during content editing process. It is responsible to
 * create file if does't exist, mount zipped file before editing, delete/add resources and
 * remove all unused resources from the zip when deleted on the content file.
 *
 * <b>Operational Flow:</b>
 *
 * <p>
 *     Use {@link UmEditorFileHelperCore#createFile} to create file if the file doesn't
 *     exists. i.e when new document is created.
 *
 *     Use {@link UmEditorFileHelperCore#mountFile} to mount zipped file
 *     to the temporary directory so that can be edited.
 *
 *     Use {@link UmEditorFileHelperCore#updateFile(UmCallback)} to add/delete
 *     resources from the temporary directory/zip file when editing.
 *
 *     Use {@link UmEditorFileHelperCore#removeUnUsedResources} to remove all
 *     unused files which are inside the zip but not referenced on content file.
 * </p>
 *
 * @see UmCallback
 *
 *
 * @author kileha3
 */
public interface UmEditorFileHelperCore {

    String INDEX_FILE = "index.html";

    String INDEX_TEMP_FILE = "index_.html";

    /**
     * Create new file if the file doesn't
     */
    void createFile(UmCallback<String> callback);

    /**
     * Unzip zipped file to a temporary directory for editing purpose.
     * @param filePath local path of the entry file.
     * @param callback ContentEditorFileHelperCallback
     */
    void mountFile(String filePath, UmCallback<Void> callback);

    /**
     * Update resources to the zipped file and temporary directory when editing
     * @param callback
     */
    void updateFile(UmCallback<Boolean> callback);

    /**
     * Remove all unused resources from the zipped file to make sure all files which
     * lives in there has been used.
     * @param callback UmCallback
     */
    void removeUnUsedResources(UmCallback<Integer> callback);

    String getSourceFilePath();

    String getBaseResourceRequestUrl();

    String getDestinationDirPath();

    String getDestinationMediaDirPath();

    String getMountedTempDirRequestUrl();


}