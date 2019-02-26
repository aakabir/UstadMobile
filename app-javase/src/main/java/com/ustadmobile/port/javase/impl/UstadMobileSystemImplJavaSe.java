package com.ustadmobile.port.javase.impl;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.catalog.contenttype.ScormTypePlugin;
import com.ustadmobile.core.catalog.contenttype.XapiPackageTypePlugin;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by mike on 10/17/17.
 */

public class UstadMobileSystemImplJavaSe extends UstadMobileSystemImplSE {

    private UMLogJavaSe logJavaSe;

    private File systemDir;


    public static final ContentTypePlugin[] SUPPORTED_CONTENT_TYPES = new ContentTypePlugin[] {
            new EPUBTypePlugin(), new XapiPackageTypePlugin(), new ScormTypePlugin()};

    public UstadMobileSystemImplJavaSe() {
        logJavaSe = new UMLogJavaSe();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return url.openConnection();
    }

    @Override
    protected String getSystemBaseDir(Object context) {
        if(systemDir == null) {
            systemDir = makeTempDir("tmp-test-javase", "");
        }

        return systemDir.getAbsolutePath();
    }

    @Override
    public void go(String viewName, Hashtable args, Object context, int flags) {

    }

    @Override
    public String getString(int messageCode, Object context) {
        return ""+messageCode;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return null;
    }

    @Override
    public NetworkManagerBle getNetworkManagerBle() {
        return null;
    }

    @Override
    public String getSharedContentDir(Object context) {
        return null;
    }

    @Override
    public String getUserContentDirectory(Object context, String username) {
        return null;
    }

    @Override
    public String getAppPref(String key, Object context) {
        return null;
    }

    @Override
    public void setAppPref(String key, String value, Object context) {

    }

    @Override
    public AppView getAppView(Object context) {
        return null;
    }

    @Override
    public UMLog getLogger() {
        return logJavaSe;
    }

    @Override
    public String getVersion(Object context) {
        return null;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {

    }

    @Override
    public ContentTypePlugin[] getSupportedContentTypePlugins() {
        return SUPPORTED_CONTENT_TYPES;
    }

    @Override
    public String getManifestPreference(String key, Object context) {
        return null;
    }

    @Override
    public void mountContainer(ContainerMountRequest request, int id, UmCallback callback) {

    }

    protected File makeTempDir(String prefix, String suffix) {
        File tmpDir = null;
        try {
            tmpDir = File.createTempFile(prefix, suffix);
            tmpDir.delete();
            tmpDir.mkdir();
            tmpDir.deleteOnExit();
        }catch(IOException e) {
            System.err.println("Exception with makeTempDir");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return tmpDir;
    }

    @Override
    public void getAsset(Object context, String path, UmCallback<InputStream> callback) {
        if(!path.startsWith("/"))
            path = '/' + path;

        try {
            callback.onSuccess(getClass().getResourceAsStream(path));
        }catch(Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public InputStream getAssetSync(Object context, String path) throws IOException {
        if(!path.startsWith("/"))
            path = '/' + path;
        return getClass().getResourceAsStream(path);
    }

    @Override
    public void deleteEntriesAsync(Object context, List<String> entryId, boolean recursive, UmCallback<Void> callback) {
        //not implemented
    }

    @Override
    public void deleteEntries(Object context, List<String> entryId, boolean recursive) {

    }

    @Override
    public long getBuildTimestamp(Object context) {
        return 0;//not implemented
    }
}
