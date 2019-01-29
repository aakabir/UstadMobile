package com.ustadmobile.port.android.umeditor;

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;

import java.util.List;

/**
 * Interface which handles actions performed on ContentEditorPageListFragment
 *
 * @author kileha3
 */
public interface UmPageActionListener {

    /**
     * Invoked when EpubNavItem list order changes
     * @param newPageList new EpubNavItem list order to be used
     */
    void onOrderChanged(List<EpubNavItem> newPageList);

    /**
     * Invoked when page deletion is confirmed.
     * @param href href of a page to be deleted
     */
    void onPageRemove(String href);

    /**
     * Invoked when page creation is confirmed
     * @param title title of the page to be created
     */
    void onPageCreate(String title);

    /**
     * Invoked when page to load is selected from the list
     * @param pageHref href of a page to be loaded to the editor
     */
    void onPageSelected(String pageHref);

    /**
     * Invoked when page update is confirmed
     * @param pageItem updated EpubNavItem
     */
    void onPageUpdate(EpubNavItem pageItem);

    /**
     * Invoked when document title change is confirmed
     * @param title new title to be used.
     */
    void onDocumentTitleUpdate(String title);

    /**
     * Invoked when user tries to delete a page from a list of 1 page.
     * @param message message to be displayed.
     */
    void onDeleteFailure(String message);

    /**
     * invoked when the dialog is closed
     */
    void onPageManagerClosed();

}
