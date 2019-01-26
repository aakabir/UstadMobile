package com.ustadmobile.core.contenteditor;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.impl.UmCallback;

import java.util.List;

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


    String PAGE_TEMPLATE = "template_page.html";

    /**
     * Create new file if the file doesn't
     */
    void createFile(long contentEntryUid,UmCallback<String> callback);

    /**
     * Unzip zipped file to a temporary directory for editing purpose.
     * @param filePath local path of the entry file.
     * @param callback ContentEditorFileHelperCallback
     */
    void mountFile(String filePath, UmCallback<Void> callback);

    /**
     * Update resources to the zipped file and temporary directory when editing
     * @param callback UmCallback
     */
    void updateFile(UmCallback<Boolean> callback);

    /**
     * Remove all unused resources from the zipped file to make sure all files which
     * lives in there has been used.
     * @param callback UmCallback
     */
    void removeUnUsedResources(UmCallback<Integer> callback);

    /**
     * Create new page to the document.
     * @param title Title of the page to be created
     * @param callback UmCallback
     */
    void addPage(String title, UmCallback<String> callback);

    /**
     * Delete a page from the document
     * @param page Page to be deleted from the doc.
     * @param callback UmCallback return deleted page.
     */
    void removePage(EpubNavItem page, UmCallback<Boolean> callback);

    void updatePage(EpubNavItem page, UmCallback<Boolean> callback);


    void changePageOrder(List<EpubNavItem> pageList, UmCallback<Boolean> callback);

    /**
     * Get all document pages.
     * @return list of all document pages.
     */
     EpubNavDocument getEpubNavDocument();


     OpfDocument getEpubOpfDocument();

    /**
     * Get source file path i.e zipped file
     * @return file path
     */
    String getSourceFilePath();

    /**
     * Get base tinymce resource request url
     * @return tinymce localhost address
     */
    String getBaseResourceRequestUrl();

    /**
     * Get file temporary directory path
     * @return directory path
     */
    String getTempDestinationDirPath();

    /**
     * Get media directory path after unzipping the file.
     * @return media path
     */
    String getDestinationMediaDirPath();

    /**
     * Get base request for the mounted file temporary directory
     * @return localhost address
     */
    String getMountedTempDirRequestUrl();

    /**
     * Get epub resources directory path.
     * @return path to epub file resources
     */
    String getEpubFilesDestination();


}