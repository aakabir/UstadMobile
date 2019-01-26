package com.ustadmobile.test.sharedse.impl;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.impl.TestContext;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by mike on 4/25/17.
 */

public class UstadMobileSystemImplTest extends UstadMobileSystemImplSE {

    /**
     * In testing cache dirs are simpyl temporary directories
     */
    private HashMap<Integer, File> cacheDirs;

    /**
     * System base dir will be a temporary directory
     */
    private File testSystemBaseDir;

    private UMTestLogger testLogger;

    public UstadMobileSystemImplTest() {
        cacheDirs = new HashMap<>();
        testSystemBaseDir = makeTempDir("umTestSystemDir", "");
        testSystemBaseDir = makeTempDir("umTestSystemDir", "");
        testLogger = new UMTestLogger();
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
    public void go(String viewName, Hashtable args, Object context) {

    }

    @Override
    public NetworkManager getNetworkManager() {
        return null;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {

    }
    
    public String getCacheDir(int mode, Object context) {
        File tmpDir = cacheDirs.get(mode);
        if(tmpDir == null) {
            tmpDir = makeTempDir("umcache-" + mode, "");
            cacheDirs.put(mode, tmpDir);
        }

        return tmpDir.getAbsolutePath();
    }


    @Override
    public XmlPullParser newPullParser() throws XmlPullParserException {
        return new KXmlParser();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return url.openConnection();
    }

    @Override
    protected String getSystemBaseDir(Object context) {
        TestContext ctx = (TestContext)context;
        return new File(testSystemBaseDir, ctx.getContextName()).getAbsolutePath();
    }

    @Override
    public String getString(int messageCode, Object context) {
        return "";
    }

    @Override
    public String getSharedContentDir(Object context) {
        return testSystemBaseDir.getAbsolutePath();
    }

    @Override
    public String getUserContentDirectory(Object context, String username) {
        return null;
    }


    @Override
    public String getAppPref(String key, Object context) {
        return ((TestContext)context).getAppProps().getProperty(key);
    }

    @Override
    public void setAppPref(String key, String value, Object context) {
        TestContext tContext = (TestContext)context;
        if(value != null) {
            tContext.getAppProps().setProperty(key, value);
        }else {
            tContext.getAppProps().remove(key);
        }

    }

    @Override
    public AppView getAppView(Object context) {
        return null;
    }

    @Override
    public UMLog getLogger() {
        return testLogger;
    }

    @Override
    public String getMimeTypeFromExtension(String extension) {
        return null;
    }

    @Override
    public String getVersion(Object context) {
        return null;
    }

    @Override
    public ContentTypePlugin[] getSupportedContentTypePlugins() {
        return new ContentTypePlugin[]{new EPUBTypePlugin()};
    }

    @Override
    public String getManifestPreference(String key, Object context) {
        //TODO: Implement this
        return null;
    }


    @Override
    public void mountContainer(ContainerMountRequest request, int id, UmCallback callback) {
        //do nothing at the moment
    }

    @Override
    public void getAsset(Object context, String path, UmCallback<InputStream> callback) {
        InputStream inputStream = getClass().getResourceAsStream(path);
        if(inputStream != null){
            callback.onSuccess(inputStream);
        }else{
            callback.onFailure(new NullPointerException("Input stream is null, probably "+path+" doesn't exist"));
        }
    }

    @Override
    public InputStream getAssetSync(Object context, String path) throws IOException {
        return getClass().getResourceAsStream(path);
    }

    @Override
    public void deleteEntriesAsync(Object context, List<String> entryId, boolean recursive, UmCallback<Void> callback) {
        //not implemented here
    }

    @Override
    public void deleteEntries(Object context, List<String> entryId, boolean recursive) {

    }

    @Override
    public long getBuildTimestamp(Object context) {
        return 0;
    }
}
