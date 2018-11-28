package com.ustadmobile.core.contenteditor;

import com.ustadmobile.core.impl.UmCallback;

import java.io.InputStream;

/**
 * Interface which defines all file operation during content editing process. It is responsible to
 * create file if does't exist, mount zipped file before editing, delete/add resources and
 * remove all unused resources from the zip when deleted on the content file.
 *
 * <b>Operational Flow:</b>
 *
 * <p>
 *     Use {@link ContentEditorFileHelperCore#createFile} to create file if the file doesn't
 *     exists. i.e when new document is created.
 *
 *     Use {@link ContentEditorFileHelperCore#mountFile} to mount zipped file
 *     to the temporary directory so that can be edited.
 *
 *     Use {@link ContentEditorFileHelperCore#updateResource} to add/delete
 *     resources from the temporary directory/zip file when editing.
 *
 *     Use {@link ContentEditorFileHelperCore#removeUnUsedResources} to remove all
 *     unused files which are inside the zip but not referenced on content file.
 * </p>
 *
 * @see UmCallback
 *
 *
 * @author kileha3
 */
public interface  ContentEditorFileHelperCore {


    /**
     * Create new file if the file doesn't
     */
    void createFile(String baseResourceUrl,UmCallback<Long> callback);

    /**
     * Unzip zipped file to a temporary directory for editing purpose.
     * @param contentEntryFileUid Uid which is used to look-up file location from the database
     * @param callback ContentEditorFileHelperCallback
     */
    void mountFile(long contentEntryFileUid, UmCallback<String> callback);

    /**
     * Update resources to the zipped file and temporary directory when editing
     * @param sourceInputStream Source file input stream
     * @param destinationPath Path where it will be copied to.
     */
    void updateResource(InputStream sourceInputStream, String destinationPath,
                                        UmCallback<Void> callback);

    /**
     * Remove all unused resources from the zipped file to make sure all files which
     * lives in there has been used.
     * @param callback UmCallback
     */
    void removeUnUsedResources(UmCallback<Void> callback);


}