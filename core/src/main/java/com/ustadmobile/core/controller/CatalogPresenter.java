package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.opds.OpdsEndpoint;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogView;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Standard pattern catalog presenter
 *
 *
 *
 * Created by mike on 9/30/17.
 */

public class CatalogPresenter extends BaseCatalogPresenter implements UstadJSOPDSItem.OpdsItemLoadCallback, AcquisitionListener {

    private CatalogView mView;

    private UstadJSOPDSFeed feed;

    public static final String ARG_URL = "url";

    public static final String ARG_HTTPUSER = "httpu";

    public static final String ARG_HTTPPPASS = "httpp";

    public static final String ARG_RESMOD = "resmod";

    public static final String ARG_BOTTOM_BUTTON_URL = "browesbtnu";

    /**
     * Constant that can be used in the buildconfig to set the bottom button to be a download all
     * button instead of a link to any other catalog
     */
    public static final String FOOTER_BUTTON_DOWNLOADALL = "downloadall";

    public static final String PREFKEY_STORAGE_DIR_CHECKTIME = "storagedir_lastchecked";

    public static final int STATUS_ACQUIRED = 0;

    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;

    public static final int STATUS_NOT_ACQUIRED = 2;

    public static final int STATUS_AVAILABLE_LOCALLY = 3;

    /**
     * Save/retrieve resource from user specific directory
     */
    public static final int USER_RESOURCE = 2;


    /**
     * Save/retrieve resource from shared directory
     */
    public static final int SHARED_RESOURCE = 4;

    public static final int ALL_RESOURCES = USER_RESOURCE | SHARED_RESOURCE;

    /**
     * Prefix used for pref keys that are used to store entry info
     */
    private static final String PREFIX_ENTRYINFO = "e2ei-";


    private int resourceMode;

    private String footerButtonUrl;

    private Hashtable args;

    public CatalogPresenter(Object context, CatalogView view) {
        super(context);
        this.mView = view;
    }


    public void onCreate(Hashtable args, Hashtable savedState) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        this.args = args;

        if(args.containsKey(ARG_RESMOD)){
            resourceMode = ((Integer)args.get(ARG_RESMOD)).intValue();
        }else{
            resourceMode = SHARED_RESOURCE;
        }

        if(args.containsKey(ARG_BOTTOM_BUTTON_URL)) {
            String footerButtonUrl = (String)args.get(ARG_BOTTOM_BUTTON_URL);
            setFooterButtonUrl(footerButtonUrl);
            mView.setFooterButtonVisible(true);
            int footerButtonLabel = footerButtonUrl.equals(FOOTER_BUTTON_DOWNLOADALL)
                    ? MessageID.download_all : MessageID.browse_feeds;
            mView.setFooterButtonLabel(impl.getString(footerButtonLabel, getContext()));
        }else {
            mView.setFooterButtonVisible(false);
        }

        feed = new UstadJSOPDSFeed();

        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
        initEntryStatusCheck();
        //TODO: What should happen here is that we should load the catalog async, and at the same time
        // scan the file system. If something new is discovered, we can fire an event.
//        Hashtable feedLoadHeaders = new Hashtable();
//        String opdsUrl = (String)args.get(ARG_URL);
//        feed.loadFromUrlAsync(opdsUrl, feedLoadHeaders, this);
    }

    public void initEntryStatusCheck(final boolean httpCacheMustRevalidate) {
        Thread initEntryCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String lastCheckedDir = UstadMobileSystemImpl.getInstance().getAppPref(PREFKEY_STORAGE_DIR_CHECKTIME,
                        getContext());
                long timeNow = new Date().getTime();
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                if(lastCheckedDir == null || timeNow - Long.parseLong(lastCheckedDir) > 500) {
                    try {
                        UstadJSOPDSFeed deviceFeed = (UstadJSOPDSFeed) OpdsEndpoint.getInstance().loadItem(
                                OpdsEndpoint.OPDS_PROTO_DEVICE, null, context, null);
                    } catch (IOException e) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 79, null, e);
                    }
                }

                Hashtable feedLoadHeaders = new Hashtable();
                if(httpCacheMustRevalidate) {
                    feedLoadHeaders.put("cache-control", "must-revalidate");
                }


                String opdsUrl = (String)args.get(ARG_URL);
                feed.loadFromUrlAsync(opdsUrl, feedLoadHeaders, getContext(), CatalogPresenter.this);

            }
        });
        initEntryCheckThread.start();
    }

    public void initEntryStatusCheck() {
        initEntryStatusCheck(false);
    }

    @Override
    public void onEntryLoaded(final int position, final UstadJSOPDSEntry entry) {
        mView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentIndex = mView.indexOfEntry(entry.id);

                if(currentIndex == -1) {
                    mView.addEntry(entry);
                }else if(currentIndex != -1 && mView.indexOfEntry(entry.id) == currentIndex){
                    //same position - just refresh it
                    mView.setEntryAt(currentIndex, entry);
                }else {
                    mView.removeEntry(entry);//get rid of it from wherever it was before
                    mView.addEntry(currentIndex, entry);
                }

                String[] thumbnailLinks = entry.getThumbnailLink(false);
                if(thumbnailLinks != null)
                    mView.setEntrythumbnail(entry.id, UMFileUtil.resolveLink(entry.getHref(),
                            thumbnailLinks[UstadJSOPDSItem.ATTR_HREF]));

                CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(entry.id,
                        CatalogPresenter.SHARED_RESOURCE | CatalogPresenter.USER_RESOURCE,
                        getContext());
                if(entryInfo != null) {
                    mView.setEntryStatus(entry.id, entryInfo.acquisitionStatus);
                    mView.setDownloadEntryProgressVisible(entry.id,
                            entryInfo.acquisitionStatus == STATUS_ACQUISITION_IN_PROGRESS);
                }
            }
        });
    }

    @Override
    public void onDone() {
        mView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mView.setRefreshing(false);
            }
        });
    }

    @Override
    public void onError(Throwable cause) {

    }

    public int getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(int resourceMode) {
        this.resourceMode = resourceMode;
    }

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @return The OPDS URL for the browse button; null if there is none (default)
     */
    public String getFooterButtonUrl() {
        return footerButtonUrl;
    }

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @param footerButtonUrl OPDS URL for the browse button: null for none (default)
     */
    public void setFooterButtonUrl(String footerButtonUrl) {
        this.footerButtonUrl = footerButtonUrl;
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    protected void onDownloadStarted() {

    }

    @Override
    protected void onEntriesRemoved() {

    }


    /**
     * Triggered when the user selects an entry from the catalog. This could
     * be another OPDS catalog Feed to display or it could be a container
     * entry.
     *
     * @param entryId
     */
    public void handleClickEntry(final String entryId) {
        UstadJSOPDSEntry entry = feed.getEntryById(entryId);
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!entry.parentFeed.isAcquisitionFeed()) {
            //we are loading another opds catalog
            Vector entryLinks = entry.getLinks(null, UstadJSOPDSItem.TYPE_ATOMFEED,
                    true, true);

            if(entryLinks.size() > 0) {
                String[] firstLink = (String[])entryLinks.elementAt(0);
                handleCatalogSelected(UMFileUtil.resolveLink(entry.parentFeed.getHref(),
                        firstLink[UstadJSOPDSItem.ATTR_HREF]));
            }
        }else {
            //Go to the entry view
            handleOpenEntryView(entry);
        }
    }

    protected void handleCatalogSelected(String url) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_URL, url);

        if(impl.getActiveUser(getContext()) != null) {
            args.put(ARG_HTTPUSER, impl.getActiveUser(getContext()));
            args.put(ARG_HTTPPPASS, impl.getActiveUserAuth(getContext()));
        }

        UstadMobileSystemImpl.getInstance().go(CatalogView.VIEW_NAME, args,
                getContext());
    }

    public void handleClickFooterButton() {
        if(FOOTER_BUTTON_DOWNLOADALL.equals(footerButtonUrl)) {
            handleClickDownloadAll();
        }else {
            handleCatalogSelected(footerButtonUrl);
        }
    }


    /**
     * Triggered by the view when the user has selected the download all button
     * for this feed
     *
     */
    public void handleClickDownloadAll() {
        handleClickDownload(feed, feed.getAllEntries());
    }

    @Override
    public void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status) {
        UstadJSOPDSEntry entry=  feed.getEntryById(entryId);
        if(entry != null) {
            float progress = (float)((double)status.getDownloadedSoFar() / (double)status.getTotalSize());
            mView.updateDownloadEntryProgress(entryId, progress, formatDownloadStatusText(status));
        }
    }

    @Override
    public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
        switch(status.getStatus()){
            case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                mView.setEntryStatus(entryId, CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS);
                mView.setDownloadEntryProgressVisible(entryId, true);
                break;
            case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                mView.setDownloadEntryProgressVisible(entryId, false);
                mView.setEntryStatus(entryId, CatalogPresenter.STATUS_ACQUIRED);
                break;
        }
    }

    public void handleRefresh() {
        mView.setRefreshing(true);
        initEntryStatusCheck(true);
    }

    /**
     * Generates a String preference key for the given entryID.  Used to map
     * in the form of entryID -> EntryInfo (serialized)
     *
     * @param entryID
     * @return
     */
    private static String getEntryInfoKey(String entryID) {
        return PREFIX_ENTRYINFO + entryID;
    }

    /**
     * Save the info we need to know about a given entry using CatalogEntryInfo
     * object which can be encoded as a String then saved as an app or user
     * preference
     *
     * @param entryID the OPDS ID of the entry in question
     * @param info CatalogEntryInfo object with required info about entry
     * @param resourceMode  USER_RESOURCE or SHARED_RESOURCE to be set as a user or shared preference
     * Use USER_RESOURCE when the file is in the users own directory, SHARED_RESOURCE otherwise
     */
    public static void setEntryInfo(String entryID, CatalogEntryInfo info, int resourceMode, Object context) {
        UstadMobileSystemImpl.getInstance().setPref(resourceMode == USER_RESOURCE,
                getEntryInfoKey(entryID), info != null? info.toString(): null, context);
    }

    /**
     * Get info about a given entryID; if known by the device.  Will return a
     * CatalogEntryInfo that was serialized as a String.
     *
     * @param entryID The OPDS ID in question
     * @param resourceMode BitMask - valid values are USER_RESOURCE and SHARED_RESOURCE
     * eg. to get both - use USER_RESOURCE | SHARED_RESOURCE
     * @deprecated
     * @return CatalogEntryInfo for the given ID, or null if not found
     */
    public static CatalogEntryInfo getEntryInfo(String entryID, int resourceMode, Object context) {
        String prefKey = getEntryInfoKey(entryID);
        String entryInfoStr = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            entryInfoStr = impl.getUserPref(prefKey, context);
        }

        if(entryInfoStr == null && (resourceMode & SHARED_RESOURCE) ==SHARED_RESOURCE) {
            entryInfoStr = impl.getAppPref(prefKey, context);
        }

        if(entryInfoStr != null) {
            return CatalogEntryInfo.fromString(entryInfoStr);
        }else {
            return null;
        }
    }

    public static String sanitizeIDForFilename(String id) {
        char c;
        int len = id.length();
        StringBuffer retVal = new StringBuffer();
        for(int i = 0; i < len; i++) {
            c = id.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '*' || c == '_') {
                retVal.append(c);
            }else if(c == ' ' || c == '\t' || c == '\n'){
                retVal.append('_');
            }else {
                retVal.append("_").append(Integer.toHexString((int)c));
            }
        }
        return retVal.toString();
    }

    /**
     * Delete the given entry
     *
     * @param entryID
     * @param resourceMode
     */
    public static void removeEntry(String entryID, int resourceMode, Object context) {
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            actionRemoveEntry(entryID, USER_RESOURCE, context);
        }

        if((resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            actionRemoveEntry(entryID, SHARED_RESOURCE, context);
        }
    }

    private static void actionRemoveEntry(String entryID, int resourceMode, Object context) {
        CatalogEntryInfo entry = getEntryInfo(entryID, resourceMode, context);
        if(entry != null && entry.acquisitionStatus == STATUS_ACQUIRED) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.getLogger().l(UMLog.INFO, 520, entry.fileURI);
            impl.removeFile(entry.fileURI);
            setEntryInfo(entryID, null, resourceMode, context);
        }
    }

}