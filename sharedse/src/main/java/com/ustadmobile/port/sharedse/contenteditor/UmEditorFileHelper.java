package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.FileDirectoryHandler;
import com.ustadmobile.port.sharedse.util.UmZipUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import static com.ustadmobile.lib.db.entities.ContainerEntryFile.COMPRESSION_NONE;

/**
 * Class which is an actual implementation of {@link UmEditorFileHelperCore}
 *
 * @author kileha3
 */
public class UmEditorFileHelper implements UmEditorFileHelperCore {

    private File documentDir;

    private File mediaDirectory;

    private File documentsRootDir;

    private File opfFile;

    private UmAppDatabase umRepo;

    private UmAppDatabase umDb;

    public static final String MEDIA_DIRECTORY = "media/";

    protected Object context;

    private EmbeddedHTTPD embeddedHTTPD;

    private String baseResourceRequestUrl = null;

    private String mountedFileAccessibleUrl = null;

    private static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    private boolean isTestExecution = false;

    private static final String PAGE_PREFIX = "page_";

    private static final String DEFAULT_PAGE_MIME_TYPE = "text/html";

    private static final int DEFAULT_NAVDOC_DEPTH = 1;

    private long currentEntryUid = 0L;

    private EpubNavItem nextNavItem = null;

    private String assetsDir = String.format("assets-%s",
            new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault()).format(new Date()));

    public static final String EDITOR_BASE_DIR_NAME = "umEditor";


    @Override
    public void  init(Object context){
        this.context = context;
        UstadMobileSystemImpl.getInstance()
                .getStorageDirs(context, result -> {
                    File baseContentDir = new File(result.get(0).getDirURI());
                    documentsRootDir = new File(baseContentDir,"documents/");
                    if(!documentsRootDir.exists())documentsRootDir.mkdirs();
                    isTestExecution = baseContentDir.getAbsolutePath().startsWith("/var/");
                });
        startWebServer();
        umDb = UmAppDatabase.getInstance(context);
        umRepo = UmAccountManager.getRepositoryForActiveAccount(context);
    }

    /**
     * Start HTTPD server
     */
    private void startWebServer() {
        embeddedHTTPD = new EmbeddedHTTPD(0, this);
        try {
            embeddedHTTPD.start();
            baseResourceRequestUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS +
                            embeddedHTTPD.getListeningPort()+"/", assetsDir, EDITOR_BASE_DIR_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void createDocument(long entryUid, UmCallback<String> callback) {
        this.currentEntryUid = entryUid;
        documentDir = new File(documentsRootDir,"Umdoc-untitled-"+System.currentTimeMillis());
        updateOpfAndMediaDirs();
        new Thread(() -> {
            String filePath;
            if(isTestExecution){
                filePath = "/com/ustadmobile/port/sharedse/";
            }else{
                filePath = "/http/" + EDITOR_BASE_DIR_NAME +"/templates";
            }
            filePath = UMFileUtil.joinPaths(filePath,ContentEditorView.RESOURCE_BLANK_DOCUMENT);
            UstadMobileSystemImpl.getInstance().getAsset(context, filePath,
                    new UmCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream result) {
                    try {
                        UmZipUtils.unzip(result,documentDir);

                        if(documentDir.exists()){
                            handleContainerEntryFiles(callback);

                        }else{
                            callback.onFailure(new Throwable("Files was not copied to the " +
                                    "intended destination"));
                        }
                    } catch (IOException e) {
                        callback.onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                }
            });
        }).start();
    }



    @Override
    public void mountDocumentDir(String entryPath, UmCallback<Void> callback) {
        documentDir = new File(entryPath);
        updateOpfAndMediaDirs();
        new Thread(() -> {
            String resourceBaseRouterUrl = EDITOR_BASE_DIR_NAME + "(.)+";
            embeddedHTTPD.removeRoute(resourceBaseRouterUrl);
            embeddedHTTPD.addRoute(resourceBaseRouterUrl, FileDirectoryHandler.class, documentDir);
            mountedFileAccessibleUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS +
                    embeddedHTTPD.getListeningPort() + "/", EDITOR_BASE_DIR_NAME);
            callback.onSuccess(null);
        }).start();

    }


    private void updateOpfAndMediaDirs() {
        opfFile = new File(getDocumentDirPath(),"content.opf");
        mediaDirectory = new File(getDocumentDirPath(), MEDIA_DIRECTORY);
        if(!mediaDirectory.exists()){
            mediaDirectory.mkdirs();
        }
    }


    /**
     * Handle container, containerEntry and ContainerEntryFiles on database level
     * @param callback UmCallback
     */
    private void handleContainerEntryFiles(UmCallback<String> callback){

        Container container = new Container();
        container.setContainerContentEntryUid(currentEntryUid);
        container.setLastModified(System.currentTimeMillis());
        container.setContainerUid(umRepo.getContainerDao().insert(container));

        File [] allDocFiles = documentDir.listFiles();

        assert allDocFiles != null;
        for(File file : allDocFiles){

            File fileEntry = new File(documentDir,file.getName());
            ContainerEntryFile entryFile = new ContainerEntryFile();
            entryFile.setCefPath(fileEntry.getPath());
            entryFile.setCeCompressedSize(0);
            entryFile.setCeTotalSize(fileEntry.getTotalSpace());
            entryFile.setCompression(COMPRESSION_NONE);
            entryFile.setCefUid(umDb.getContainerEntryFileDao().insert(entryFile));

            umDb.getContainerEntryDao().insert(new ContainerEntry(documentDir.getPath(),
                    container,entryFile));
        }
        callback.onSuccess(documentDir.getAbsolutePath());
    }



    @Override
    public void removeUnUsedResources(UmCallback<Integer> callback) {
        new Thread(() -> {
            try {
                int unUsedFileCounter = 0;
                if(getMediaDirectory() != null){
                    File [] allResources = new File(getMediaDirectory()).listFiles();
                    assert allResources != null;
                    for(File resource:allResources){
                        if(!isResourceInUse(resource.getName())
                                && !resource.getName().equals(MEDIA_DIRECTORY)){
                            if(resource.delete()) {
                                String filename = resource.getName();
                                if(removeManifestItem(filename)){
                                    unUsedFileCounter++;
                                }
                            }
                        }
                    }
                }
                callback.onSuccess(unUsedFileCounter);
            }catch (NullPointerException e){
                callback.onFailure(e);
            }
        }).start();
    }


    /**
     * Check if resource is being used in ant document tag.
     * @param resourceName Resource name to be checked
     * @return True if in use otherwise false.
     */
    private boolean isResourceInUse(String resourceName){
        boolean resourceInUse = false;
        try {
            List<EpubNavItem> navItems = getEpubNavDocument().getToc().getChildren()!=null?
                    getEpubNavDocument().getToc().getChildren():new ArrayList<>();
            for(EpubNavItem navItem: navItems){
                Document index = Jsoup.parse(UMFileUtil.readTextFile(
                        new File(getDocumentDirPath(), navItem.getHref()).getAbsolutePath()));
                Element previewContainer = index.select(".um-editor").first();
                Elements resources = previewContainer.select("img[src],source[src]");

                if(findResource(resourceName,resources)){
                    resourceInUse = true;
                    break;
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        return resourceInUse;
    }

    /**
     * Find resources accross all files
     * @param resourceName resource to be found
     * @param pageResources list of all pages from where resource will be found
     * @return True if resource was used to either of pages otherwise it wasn't used.
     */
    private boolean findResource(String resourceName, Elements pageResources){
        for (Element resource : pageResources) {
            String srcUrl = resource.attr("src");
            if (srcUrl.endsWith(resourceName)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void updatePage(EpubNavItem page, UmCallback<Boolean> callback) {
        Exception exception = null;
        EpubNavDocument document = getEpubNavDocument();
        EpubNavItem parent = document.getToc();

        EpubNavItem navItem = getNavItemByHref(page.getHref(),document.getToc());
        int itemIndex = parent.getChildren().indexOf(navItem);
        if (navItem != null) {
            navItem.setTitle(page.getTitle());
        }
        parent.getChildren().set(itemIndex,navItem);

        ByteArrayOutputStream bout = null;
        boolean metaInfoUpdated = false;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            document.serialize(serializer);
            bout.flush();
            bout.flush();
            String navContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(getNavFile(),navContent);
            metaInfoUpdated = updateOpfMetadataInfo(null,null);
        }catch (IOException e) {
            exception = e;
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        if(exception != null){
            callback.onFailure(exception);
        }
        boolean updated = getEpubNavDocument().getNavById(page.getHref()) == null
                && metaInfoUpdated;
        callback.onSuccess(updated);
    }


    @Override
    public void changePageOrder(List<EpubNavItem> pageList, UmCallback<Boolean> callback){

        Exception exception = null;
        EpubNavDocument document = getEpubNavDocument();
        document.getToc().getChildren().clear();
        document.getToc().getChildren().addAll(pageList);

        ByteArrayOutputStream bout = null;
        boolean metaInfoUpdated = false;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            document.serialize(serializer);
            bout.flush();
            String navContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(getNavFile(),navContent);
            metaInfoUpdated = updateOpfMetadataInfo(null,null);
        } catch (IOException e) {
            exception = e;
        }finally {
            UMIOUtils.closeQuietly(bout);
        }

        if(exception != null){
            callback.onFailure(exception);
        }
        callback.onSuccess(metaInfoUpdated);
    }

    @Override
    public EpubNavItem getNextPage() {
        return nextNavItem;
    }

    @Override
    public void updateDocumentTitle(String documentTitle, boolean isNeDocument,
                                    UmCallback<String> callback) {
        updateOpfAndMediaDirs();
        if(isNeDocument){
            updateOpfMetadataInfo(documentTitle, UUID.randomUUID().toString());
        }

        if(updateOpfMetadataInfo(documentTitle,null)){
            //add new page
            if(isNeDocument){
                String pageTitle = UstadMobileSystemImpl.getInstance()
                        .getString(MessageID.content_untitled_page, context);
                addPage(pageTitle, new UmCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }else{
                callback.onSuccess(documentTitle);
            }
        }else{
            callback.onFailure(new Throwable("Failed to update document title"));
        }
    }

    @Override
    public void updateManifestItems(String filename, String mimeType, UmCallback<Boolean> callback){
        callback.onSuccess(addManifestItem(filename,mimeType));
    }


    @Override
    public void addPage(String title, UmCallback<String> callback) {
        Exception exception = null;
        boolean created = false;
        String href = null;
        int pageNumberIndex = 1, nextPageNumber = 1;

        try{
            List<EpubNavItem>  navItems = getEpubNavDocument().getToc().getChildren() != null ?
                    getEpubNavDocument().getToc().getChildren():new ArrayList<>();
            List<Integer> pageNumbers = new ArrayList<>();
            for(EpubNavItem navItem: navItems){
                pageNumbers.add(Integer.parseInt(getFileNameWithoutExtension(navItem.getHref()
                        .split("_")[pageNumberIndex])));
            }
            if(pageNumbers.size() > 0){
                int lastPageIndex = pageNumbers.size() - 1;
                Collections.sort(pageNumbers);
                nextPageNumber = pageNumbers.get(lastPageIndex) + 1;
            }

            href = PAGE_PREFIX + nextPageNumber + ".html";
            InputStream is = new FileInputStream(new File(documentDir,
                    PAGE_TEMPLATE));
            created = UMFileUtil.copyFile(is,new File(getDocumentDirPath(), href));
            if(created){
                created = addNavItem(href,title) && addManifestItem(href, DEFAULT_PAGE_MIME_TYPE)
                        && addSpineItem(href, DEFAULT_PAGE_MIME_TYPE);
            }

        } catch (IOException e) {
            exception = e;
        }finally {
            if(created){
                callback.onSuccess(href);
            }
            if(exception != null){
                callback.onFailure(exception);
            }
        }
    }

    @Override
    public void removePage(String  href, UmCallback<Boolean> callback) {
        boolean removed = removeNavItem(href) && removeSpineItem(href)
                && removeManifestItem(href);
        callback.onSuccess(removed);
    }

    @Override
    public OpfDocument getEpubOpfDocument(){
        try{
            FileInputStream inputStream = new FileInputStream(opfFile);
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(inputStream);
            OpfDocument opfDocument = new OpfDocument();
            opfDocument.loadFromOPF(xpp);
            return opfDocument;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public EpubNavDocument getEpubNavDocument(){
        try{
            FileInputStream docIn = new FileInputStream(new File(getDocumentDirPath(),
                    getEpubOpfDocument().getNavItem().getHref()));
            EpubNavDocument navDocument = new EpubNavDocument();
            navDocument.load(UstadMobileSystemImpl.getInstance().newPullParser(docIn,
                    "UTF-8"));
            return navDocument;
        }catch (XmlPullParserException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Add spine items on  opf file
     * @param href page index to be updates
     * @return true if updated otherwise false.
     */
    private boolean addSpineItem(String href,String mimeType) {
        OpfDocument opfDocument = getEpubOpfDocument();
        OpfItem spineItem = new OpfItem();
        spineItem.setHref(href);
        spineItem.setMimeType(mimeType);
        spineItem.setId(href);
        opfDocument.getSpine().add(spineItem);
        ByteArrayOutputStream bout = null;
        boolean metaInfoUpdated = false;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            opfDocument.serialize(serializer);
            bout.flush();
            String opfContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(opfFile,opfContent);
            metaInfoUpdated = updateOpfMetadataInfo(null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return getEpubOpfDocument().getLinearSpinePositionByHREF(href) != -1 && metaInfoUpdated;
    }

    /**
     * Remove spine item from the opf document
     * @param href spine href to be removed
     * @return True if a spine item was removed otherwise it wasn't
     */
    private boolean removeSpineItem(String href){
        OpfDocument opfDocument = getEpubOpfDocument();
        OpfItem spineItem = opfDocument.getSpine()
                .remove(opfDocument.getLinearSpinePositionByHREF(href));
        boolean metaInfoUpdated = false;
        if(spineItem != null){
            ByteArrayOutputStream bout = null;
            try{
                bout = new ByteArrayOutputStream();
                XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
                serializer.setOutput(bout, "UTF-8");
                opfDocument.serialize(serializer);
                bout.flush();
                String opfContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
                UMFileUtil.writeToFile(opfFile,opfContent);
                metaInfoUpdated = updateOpfMetadataInfo(null,null);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                UMIOUtils.closeQuietly(bout);
            }
        }
        return getEpubOpfDocument().getLinearSpinePositionByHREF(href) == -1 && metaInfoUpdated;
    }

    /**
     * Add an item to the navigation file.
     * @param href item ref
     * @param title item title.
     * @return true if added otherwise false.
     */
    private boolean addNavItem(String href,String title){
        EpubNavDocument document = getEpubNavDocument();
        EpubNavItem navItem = new EpubNavItem(title,href,null, DEFAULT_NAVDOC_DEPTH);
        document.getToc().addChild(navItem);
        ByteArrayOutputStream bout = null;
        boolean metaInfoUpdated = false;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            document.serialize(serializer);
            bout.flush();
            String navContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(getNavFile(),navContent);
            metaInfoUpdated = updateOpfMetadataInfo(null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return getNavItemByHref(href,getEpubNavDocument().getToc()) != null && metaInfoUpdated;
    }

    /**
     * Remove navigation item from nav document
     * @param href href of an item to be removed
     * @return True if it was successfully removed otherwise it wasn't
     */
    private boolean removeNavItem(String href){
        EpubNavDocument document = getEpubNavDocument();
        EpubNavItem navItem = getNavItemByHref(href,document.getToc());
        List<EpubNavItem> navItems = document.getToc().getChildren();
        int tobeDeletedNavIndex = navItems.indexOf(navItem);
        int nextNavItemIndex = tobeDeletedNavIndex == navItems.size() - 1
                ? navItems.size() - 2 : tobeDeletedNavIndex + 1;
        nextNavItem = navItems.get(nextNavItemIndex);
        boolean metaInfoUpdated = false;
        if(document.getToc().getChildren().remove(navItem)){
            ByteArrayOutputStream bout = null;
            try{
                bout = new ByteArrayOutputStream();
                XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
                serializer.setOutput(bout, "UTF-8");
                document.serialize(serializer);
                bout.flush();
                String navContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
                UMFileUtil.writeToFile(getNavFile(),navContent);
                metaInfoUpdated = updateOpfMetadataInfo(null,null);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                UMIOUtils.closeQuietly(bout);
            }
        }

        return getNavItemByHref(href,getEpubNavDocument().getToc()) == null && metaInfoUpdated;
    }


    /**
     * Add manifest item on opf file
     * @param href manifest item ref
     * @param mimeType manifest item mime type
     * @return true if an item was added otherwise false.
     */
    private boolean addManifestItem(String href,String mimeType){
        boolean metaInfoUpdated = false;
        OpfDocument opfDocument = getEpubOpfDocument();
        OpfItem manifestItem = new OpfItem();
        manifestItem.setHref(href);
        manifestItem.setId(href);
        manifestItem.setMimeType(mimeType);
        opfDocument.getManifestItems().put(manifestItem.getId(),manifestItem);
        ByteArrayOutputStream bout = null;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            opfDocument.serialize(serializer);
            bout.flush();
            String opfContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(opfFile,opfContent);
            metaInfoUpdated = updateOpfMetadataInfo(null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return getEpubOpfDocument().getManifestItems().containsKey(manifestItem.getId())
                && metaInfoUpdated;
    }

    /**
     * Remove manifest item
     * @param itemId Id of an item to be removed
     * @return True if an item was removed otherwise it wasn't
     */
    private boolean removeManifestItem(String itemId){
        OpfDocument opfDocument = getEpubOpfDocument();
        OpfItem opfItem = opfDocument.getManifestItems().remove(itemId);
        boolean metaInfoUpdated = false;
        if(opfItem != null){
            ByteArrayOutputStream bout = null;
            try{
                bout = new ByteArrayOutputStream();
                XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
                serializer.setOutput(bout, "UTF-8");
                opfDocument.serialize(serializer);
                bout.flush();
                String opfContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
                UMFileUtil.writeToFile(opfFile,opfContent);
                metaInfoUpdated = updateOpfMetadataInfo(null,null);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                UMIOUtils.closeQuietly(bout);
            }
        }
        return !getEpubOpfDocument().getManifestItems().containsKey(itemId) && metaInfoUpdated;
    }


    /**
     * Update opf document meta data iformation
     * @param title new opf document title
     * @param uuid epub pub-id
     * @return True if meta data were updated successfully otherwise they were not update.
     */
    private boolean updateOpfMetadataInfo(String title,String uuid){
        OpfDocument opfDocument = getEpubOpfDocument();
        opfDocument.setTitle(title == null ? opfDocument.getTitle():title);
        opfDocument.setId(uuid == null ? opfDocument.getId():uuid);
        DateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String lastUpdateDateTime = formatter.format(new Date(System.currentTimeMillis()));
        ByteArrayOutputStream bout = null;
        try{
            bout = new ByteArrayOutputStream();
            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            opfDocument.serialize(serializer);
            bout.flush();
            String opfContent = new String(bout.toByteArray(), StandardCharsets.UTF_8);
            UMFileUtil.writeToFile(opfFile,opfContent);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return title == null || getEpubOpfDocument().getTitle().equals(title);
    }

    @Override
    public String getDocumentDirPath() {
        return documentDir.getAbsolutePath();
    }

    @Override
    public String getMediaDirectory() {
        return mediaDirectory.getAbsolutePath();
    }

    @Override
    public String getMountedFileAccessibleUrl() {
        return mountedFileAccessibleUrl;
    }


    /**
     * Add base resource request handler
     * @param handlerClass handler class
     */
    public void addBaseAssetHandler(Class handlerClass){
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  handlerClass, context);
    }

    /**
     * Get navigation document file reference
     * @return file object
     */
    private File getNavFile (){
        return new File(getDocumentDirPath(), getEpubOpfDocument().getNavItem().getHref());
    }

    /**
     * Get navigation Item by its href.
     * @param href href to be fund
     * @param parentNavItem parent nav item
     * @return EpubNavItem if found otherwise NULL.
     */
    private EpubNavItem getNavItemByHref(String href,EpubNavItem parentNavItem){
        for(EpubNavItem navItem: parentNavItem.getChildren()){
            if(navItem.getHref().equals(href)){
                return navItem;
            }
        }
        return null;
    }

    /**
     * Remove file extension
     * @param fileName full file name i.e with extension
     * @return file name without extension.
     */
    private String getFileNameWithoutExtension(String fileName){
        return fileName.replaceFirst("[.][^.]+$", "");
    }
}
