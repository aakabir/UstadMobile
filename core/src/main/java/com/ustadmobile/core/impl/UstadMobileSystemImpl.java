/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.core.impl;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;

import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.util.MessagesHashtable;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.view.AppView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;


/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike
 */
public abstract class UstadMobileSystemImpl {

    protected static UstadMobileSystemImpl mainInstance;

    /**
     * Suggested name to create for content on Devices
     */
    public static final String DEFAULT_CONTENT_DIR_NAME = "ustadmobileContent";

    private MessagesHashtable messages;

    /**
     * The direction - either 0 for LTR or 1 for RTL
     */
    private int direction;

    private boolean initRan;

    /**
     * The currently active locale
     */
    private String locale;

    /**
     * Get an instance of the system implementation - relies on the platform
     * specific factory method
     * Indicates the number of bytes downloaded so far in a download
     */
    public static final int IDX_DOWNLOADED_SO_FAR = 0;

    /**
     * Indicates the total number of bytes in a download
     */
    public static final int IDX_BYTES_TOTAL = 1;

    /**
     * Indicates the status of a download (e.g. complete, failed, queued, etc)
     */
    public static final int IDX_STATUS = 2;


    /**
     * Flag to indicate a download requested was successful.
     *
     * Same value as android.app.DownloadManager.STATUS_SUCCESSFUL
     */
    public static final int DLSTATUS_SUCCESSFUL = 8;

    /**
     * Flag to indicate a download requested has failed
     *
     * Same value as android.app.DownloadManager.STATUS_FAILED
     */
    public static final int DLSTATUS_FAILED = 16;

    /**
     * Flag to indicate a download requested is retrying
     */
    public static final int DLSTATUS_RETRYING = 20;

    /**
     * Flag to indicate download is pending
     *
     * Same value as android.app.DownloadManager.STATUS_PENDING
     */
    public static final int DLSTATUS_PENDING = 1;

    /**
     * Flag to indicate download is running now
     *
     * Same value as android.app.DownloadManager.STATUS_RUNNING
     */
    public static final int DLSTATUS_RUNNING = 2;

    /**
     * Flag to indicate download is paused (e.g. waiting to retry etc)
     *
     * Same value as android.app.DownloadManager.STATUS_PENDING
     */
    public static final int DLSTATUS_PAUSED = 4;

    /**
     * Indicates that a download has not actually started yet
     */
    public static final int DLSTATUS_NOT_STARTED = 0;


    /**
     * The return value from getLocale when the user has said to use the system's locale
     */
    public static final String LOCALE_USE_SYSTEM = "";



    protected static Hashtable MIME_TYPES = new Hashtable();

    protected static Hashtable MIME_TYPES_REVERSE = new Hashtable();

    /**
     * Ported from old CatalogPresenter
     *
     * Save/retrieve resource from user specific directory
     */
    public static final int USER_RESOURCE = 2;


    /**
     * Ported from old CatalogPresenter
     *
     * Save/retrieve resource from shared directory
     */
    public static final int SHARED_RESOURCE = 4;

    public static final int STATUS_ACQUIRED = 0;

    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;

    public static final int STATUS_NOT_ACQUIRED = 2;

    public static final int STATUS_AVAILABLE_LOCALLY = 3;



    static {
        MIME_TYPES.put("image/jpg", "jpg");
        MIME_TYPES.put("image/jpeg", "jpg");
        MIME_TYPES.put("image/png", "png");
        MIME_TYPES.put("image/gif", "gif");
        MIME_TYPES.put("image/svg", "svg");
        MIME_TYPES.put("application/epub+zip", "epub");

        MIME_TYPES_REVERSE = UMUtil.flipHashtable(MIME_TYPES);
    }

    /**
     * Get an instance of the system implementation - relies on the platform
     * specific factory method
     *
     * @return A singleton instance
     */
    public static UstadMobileSystemImpl getInstance() {
        if(mainInstance == null) {

            mainInstance = UstadMobileSystemImplFactory.makeSystemImpl();
        }

        return mainInstance;
    }

    /**
     * Only for testing purposes (e.g. to use a mockito spy)
     */
    public static void setMainInstance(UstadMobileSystemImpl instance) {
        mainInstance = instance;
    }

    /**
     * Convenience shortcut for logging
     * @see UMLog#l(int, int, java.lang.String)
     *
     * @param level log level
     * @param code log code
     * @param message message to log
     */
    public static void l(int level, int code, String message) {
        getInstance().getLogger().l(level, code, message);
    }

    /**
     * Convenience shortcut for logging
     * @see UMLog#l(int, int, java.lang.String, java.lang.Exception)
     *
     * @param level log level
     * @param code log code
     * @param message log message
     * @param exception exception that occurred to log
     */
    public static void l(int level, int code, String message, Exception exception) {
        getInstance().getLogger().l(level, code, message, exception);
    }

    /**
     * Do any required startup operations: init will be called on creation
     *
     * This must make the shared content directory if it does not already exist
     */
    public void init(Object context) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 519, null);
        //We don't need to do init again
        if(initRan) {
            return;
        }

        initRan = true;
    }

    /**
     * Go to a new view : This is simply a convenience wrapper for go(viewName, args, context):
     * it will parse the a destination into the viewname and arguments, and then build a hashtable
     * to pass on.
     *
     * @param destination Destination name in the form of ViewName?arg1=val1&arg2=val2 etc.
     * @param context System context object
     */
    public void go(String destination, Object context) {
        Hashtable argsTable = null;
        int destinationQueryPos = destination.indexOf('?');
        if(destinationQueryPos == -1) {
            go(destination, null, context);
        }else {
            go(destination.substring(0, destinationQueryPos), UMFileUtil.parseURLQueryString(
                    destination), context);
        }
    }

    /**
     * The main method used to go to a new view. This is implemented at the platform level. On
     * Android this involves starting a new activity with the arguments being turned into an
     * Android bundle. On J2ME it creates a new Form and shows it, on iOS it looks up the related
     * UIViewController.
     *
     * @param viewName The name of the view to go to: This should match the view's interface .VIEW_NAME constant
     * @param args (Optional) Hahstable of arguments for the new view (e.g. catalog/container url etc)
     * @param context System context object
     */
    public abstract void go(String viewName, Hashtable args, Object context);

    /**
     * Provides the currently active locale
     *
     * @return The currently active locale code, or a blank "" string meaning the locale is the system default.
     */
    public String getLocale(Object context) {
        return locale;
    }

    /**
     * Provides the language code of the currently active locale. This is different to getLocale. If
     * the locale is currently set to LOCALE_USE_SYSTEM then that language will be resolved and the
     * code returned.
     *
     * @param context
     *
     * @return The locale as the user sees it.
     */
    public String getDisplayedLocale(Object context) {
        String locale = getLocale(context);
        if(locale.equals(LOCALE_USE_SYSTEM))
            locale = getSystemLocale(context);

        return locale;
    }

    public void setLocale(String locale, Object context) {
        this.locale = locale;
    }

    /**
     * Check on whether or not the locale string pack has been loaded or not
     * @return
     */
    public boolean isLocaleLoaded() {
        return messages != null;
    }

    /**
     * Starts the user interface for the app
     */
    public void startUI(Object context) {
        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);


        if(getAppConfigBoolean(AppConfig.KEY_FIRST_DEST_LOGIN_REQUIRED, context)
                && activeAccount == null) {
            go(Login2View.VIEW_NAME, null, context);
        }else {
            go(getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context), context);
        }
    }

    /**
     * Save anything that should be written to disk
     */
    public synchronized void handleSave() {

    }

    /**
     * Get a string for use in the UI
     */
    public abstract String getString(int messageCode, Object context);


    /**
     * Gets the direction of the UI
     *
     * @see UstadMobileConstants#DIR_LTR
     * @see UstadMobileConstants#DIR_RTL
     *
     * @return Direction int flag - 0 for LTR or 1 for RTL
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Get the name of the platform implementation being used
     *
     * @return the name of the platform (used constructing views etc) e.g. "J2ME", "Android", etc
     */
    public abstract String getImplementationName();

    /**
     * Gets the cache directory for the platform for either user specific
     * cache contents / shared cache contents
     *
     * @param mode USER_RESOURCE or SHARED_RESOURCE
     * @return String filepath to the cache dir for that mode
     */
    public abstract String getCacheDir(int mode, Object context);

    /**
     * Get storage directories
     *
     * @param mode bitmask flag of USER_RESOURCE or SHARED_RESOURCE
     * @return Array of storage
     */
    public abstract UMStorageDir[] getStorageDirs(int mode, Object context);

    /**
     * Provides the path to the shared content directory
     *
     * @deprecated - Use getStorageDirs and getCacheDirinstead
     * @return URI of the shared content directory
     */
    public abstract String getSharedContentDir(Object context);

    /**
     * Provides the path to content directory for a given user
     *
     * @param username
     * @deprecated use getStorageDirs and getCacheDir instead
     *
     * @return URI of the given users content directory
     */
    public abstract String getUserContentDirectory(Object context, String username);


    /**
     * Must provide the system's default locale (e.g. en_US.UTF-8)
     *
     * @return System locale
     */
    public abstract String getSystemLocale(Object context);


    /**
     * Get an asset (from files that are in core/src/flavorName/assets)
     *
     */
    public abstract void getAsset(Object context, String path, UmCallback<InputStream> callback);

    /**
     * Get a preference for the app
     *
     * @param key preference key as a string
     * @return value of that preference
     */
    public abstract String getAppPref(String key, Object context);

    /**
     * Get a list of preferences currently set for the app itself
     *
     * @return String array list of app preference keys
     */
    public abstract String[] getAppPrefKeyList(Object context);

    /**
     * Get a preference for the app.  If not set, return the provided defaultVal
     *
     * @param key preference key as string
     * @param defaultVal default value to return if not set
     * @return value of the preference if set, defaultVal otherwise
     */
    public String getAppPref(String key, String defaultVal, Object context) {
        String valFound = getAppPref(key, context);
        return valFound != null ? valFound : defaultVal;
    }

    /**
     * Set a preference for the app
     * @param key preference that is being set
     * @param value value to be set
     *
     */
    public abstract void setAppPref(String key, String value, Object context);


    /**
     * Make an asynchronous http request. This can (on platforms with a filesystem) rely on the
     * caching directory.
     *
     * @param request request to make
     * @param responseListener response listener to receive response when ready
     */
    public abstract UmHttpCall makeRequestAsync(UmHttpRequest request,
                                                UmHttpResponseCallback responseListener);


    /**
     * Directly send an asynchronous http request. This must *NOT* rely on the httpcachedir, as it
     * will be used by HttpCacheDir as the underlying implementation to retrieve data from the network.
     *
     * @param request
     * @param responseListener
     * @return
     */
    public abstract UmHttpCall sendRequestAsync(UmHttpRequest request,
                                                   UmHttpResponseCallback responseListener);

    /**
     * Directly send a synchronous request. THIS IS NOT FOR NORMAL USAGE. It is intended only to be
     * used by the cache so requests can be pumped through the system http library, if present on
     * that implementation. As http libraries like okhttp
     *
     * It must *NOT* be used directly by presenters etc.
     *
     * @param request
     * @return
     */
    protected abstract UmHttpResponse sendRequestSync(UmHttpRequest request) throws IOException;


    public abstract UmHttpResponse makeRequestSync(UmHttpRequest request) throws IOException;


    /**
     * Mount a container (e.g. epub, xapi package, etc) so it can be accessed using makeRequest. This
     * normally means making the contents of a zip file accessible over http , e.g.
     * mount /path/file.zip will provide a base url, e.g. http://127.0.0.1:65000/file.zip and contents
     * can be accessed (e.g. http://127.0.0.1:65000/file.zip/some/file.xhtml )
     *
     * @param request The request to make
     * @param id The id used provided when the callback is called
     * @param callback Callback to call when the mount is completed or failed
     */
    public abstract void mountContainer(ContainerMountRequest request, int id, UmCallback callback);


    /**
     * Make a new instance of an XmlPullParser (e.g. Kxml).  This is added as a
     * method in the implementation instead of using the factory API because
     * it enables the J2ME version to use the minimal jar
     *
     * @return A new default options XmlPullParser
     */
    public abstract XmlPullParser newPullParser() throws XmlPullParserException;

    /**
     * Make a new instance of an XmlSerializer (org.xmlpull.v1.XmlSerializer)
     *
     * @return New instance of an XML Serializer
     */
    public abstract XmlSerializer newXMLSerializer();

    /**
     * Make a new XmlPullParser from a given inputstream
     * @param in InputStream to read from
     * @param encoding Encoding to be used e.g. UTF-8
     *
     * @return a new XmlPullParser with set with the given inputstream
     */
    public XmlPullParser newPullParser(InputStream in, String encoding) throws XmlPullParserException {
        l(UMLog.DEBUG, 523, encoding);
        XmlPullParser xpp = newPullParser();
        xpp.setInput(in, encoding);
        return xpp;
    }




    /**
     * Make a new XmlPullParser from a given inputstream assuming UTF-8 encoding
     * @param in InputStream to read from
     * @return a new XmlPullParser with set with the given inputstream
     * @throws XmlPullParserException
     */
    public XmlPullParser newPullParser(InputStream in) throws XmlPullParserException {
        return newPullParser(in, UstadMobileConstants.UTF8);
    }

    /**
     * Get access to the App View to do common UI activities (e.g. show
     * progress dialog, flash message, etc)
     *
     * @return Platform AppView
     */
    public abstract AppView getAppView(Object context);

    /**
     * Get access to the logger to use on this implementation
     *
     * @return Platform logger
     */
    public abstract UMLog getLogger();

    /**
     * When selecting a link to download we can use the mime type parameter
     * x-umprofile to determine the type of device the link is intended for
     * e.g. x-umprofile=micro for files with reduced size images and 3gp
     * video
     *
     * Currently supports only null (no specific profile) or micro
     *
     * @return profile name for this system e.g. null or "micro"
     */
    public abstract String getUMProfileName();

    /**
     * Return the mime type for the given extension
     *
     * @param extension the extension without the leading .
     *
     * @return The mime type if none; or null if it's not known
     */
    public String getMimeTypeFromExtension(String extension) {
        if(MIME_TYPES_REVERSE.containsKey(extension))
            return (String)MIME_TYPES_REVERSE.get(extension);

        return null;
    }

    /**
     * Return the extension of the given mime type
     *
     * @param mimeType The mime type
     *
     * @return File extension for the mime type without the leading .
     */
    public String getExtensionFromMimeType(String mimeType) {
        if(MIME_TYPES.containsKey(mimeType)) {
            return (String)MIME_TYPES.get(mimeType);
        }

        return null;
    }

    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    public abstract String getVersion(Object context);

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    public abstract long getBuildTimestamp(Object context);

    /**
     * Perform a one way hash of an authentication parameter
     *
     * @param context System context
     * @param auth Authentication secret to be hashed
     * @return The authentication secret hashed
     */
    public abstract String hashAuth(Object context, String auth);

    /**
     * Indicates whether or not this platform/device supports WiFi Direct (aka P2P WiFi)
     * @return
     */
    public boolean isWiFiP2PSupported() {
        return false;
    }


    public NetworkManagerCore getNetworkManager() {

        return null;
    }

    /**
     * Returns whether or not the init method has already been run
     *
     * @return true if init has been called with a first context used to load certain resources,
     * false otherwise
     */
    protected boolean isInitialized() {
        return initRan;
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     * @param callback callback to call when complete or if any error occurs.
     * @return String: file absolute path
     */
    public abstract void getAppSetupFile(Object context, boolean zip, UmCallback callback);



    /**
     * Provides a list of the content types which are supported on this platform.
     *
     * @return Array of Class objects representing the ContentTypePlugin
     */
    public abstract ContentTypePlugin[] getSupportedContentTypePlugins();

    /**
     * Format the given integer to use , seperators as per the locale in use
     *
     * @param integer
     *
     * @return
     */
    public abstract String formatInteger(int integer);

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, null otherwise
     */
    public abstract String getManifestPreference(String key, Object context);

    /**
     * Wrapper to retrieve preference keys from the system Manifest.
     *
     * On Android: uses meta-data elements on the application element in AndroidManifest.xml
     * On J2ME: uses the jad file
     *
     * @param key The key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     *
     * @return The value of the manifest preference key if found, otherwise the default value
     */
    public String getManifestPreference(String key, String defaultVal, Object context) {
        String val = getManifestPreference(key, context);
        if(val != null) {
            return val;
        }else {
            return defaultVal;
        }
    }


    /**
     * Lookup a value from the app runtime configuration. These come from a properties file loaded
     * from the assets folder, the path of which is set by the manifest preference
     * com.sutadmobile.core.appconfig .
     *
     * @param key The config key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context Systme context object
     *
     * @return The value of the key if found, if not, the default value provided
     */
    public abstract String getAppConfigString(String key, String defaultVal, Object context);

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value to return if the key is not found
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise the default value
     */
    public boolean getAppConfigBoolean(String key, boolean defaultVal, Object context) {
        String strVal = getAppConfigString(key, null, context);
        if(strVal == null)
            return defaultVal;
        else
            return Boolean.parseBoolean(strVal);
    }

    /**
     * Get a boolean from the app configuration. App config is stored as a string, so this is
     * converted to a boolean using Boolean.parseBoolean
     *
     * @param key The preference key to lookup
     * @param context System context object
     * @return The boolean value of the given preference key if found, otherwise false
     */
    public boolean getAppConfigBoolean(String key, Object context) {
        return getAppConfigBoolean(key, false, context);
    }

    /**
     * Get an integer from the app configuration.
     *
     * @param key The preference key to lookup
     * @param defaultVal The default value if the preference key is not found
     * @param context System context object
     * @return The integer value of the value if found, otherwise the default value
     */
    public int getAppConfigInt(String key, int defaultVal, Object context) {
        return Integer.parseInt(getAppConfigString(key, ""+defaultVal, context));
    }

    public abstract String convertTimeToReadableTime(long time);

    /**
     * Determine if the two given locales are the same as far as what the user will see.
     *
     * @param oldLocale
     *
     * @return
     */
    public boolean hasDisplayedLocaleChanged(String oldLocale, Object context) {
        String currentlyDisplayedLocale = getDisplayedLocale(context);
        if(currentlyDisplayedLocale != null && oldLocale != null
                && oldLocale.substring(0, 2).equals(currentlyDisplayedLocale.substring(0,2))) {
            return false;
        }else {
            return true;
        }
    }

    protected final String getContentDirName(Object context) {
        return getAppConfigString(AppConfig.KEY_CONTENT_DIR_NAME, DEFAULT_CONTENT_DIR_NAME, context);
    }

    /**
     * Delete a given set of entries from the system.
     *
     * @param context Context object
     * @param entryId List of entry Ids that should be deleted
     * @param recursive true if all children of the given entryIds should be deleted, false otherwise
     * @param callback callback to be called when the operation is completed
     */
    public abstract void deleteEntriesAsync(Object context, List<String> entryId, boolean recursive,
                                            UmCallback<Void> callback);

    public abstract void deleteEntries(Object context, List<String> entryId, boolean recursive);

    public abstract OpdsAtomFeedRepository getOpdsAtomFeedRepository(Object context);

}


