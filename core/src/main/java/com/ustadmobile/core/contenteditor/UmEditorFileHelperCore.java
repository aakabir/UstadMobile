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
     * @param href href of an item to be delete from the docs.
     * @param callback UmCallback.
     */
    void removePage(String href, UmCallback<Boolean> callback);

    /**
     * Update page
     * @param page page to be updated
     * @param callback UmCallback
     */
    void updatePage(EpubNavItem page, UmCallback<Boolean> callback);

    /**
     * Change navigation items order
     * @param pageList new page list order
     * @param callback UmCallback
     */
    void changePageOrder(List<EpubNavItem> pageList, UmCallback<Boolean> callback);

    /**
     * Get next page to be loaded
     * i.e When loaded page deleted, instead of having a blank screen it will load the next page.
     * @return next EpubNavItem
     */
    EpubNavItem getNextPage();

    /**
     * Update an epub title
     * @param title new title to be set
     */
    void updateEpubTitle (String title, UmCallback<Boolean> callback);

    /**
     * Update manifest item list when new media file is added
     * @param filename file to be added to the manifest items
     * @param mimeType file mimetype
     * @param callback UmCallback
     */
    void updateManifestItems (String filename, String mimeType, UmCallback<Boolean> callback);

    /**
     * Get epub navigation document
     * @return EpubNavDocument instance
     */
     EpubNavDocument getEpubNavDocument();

    /**
     * get epub opf document
     * @return OpfDocument instance
     */
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