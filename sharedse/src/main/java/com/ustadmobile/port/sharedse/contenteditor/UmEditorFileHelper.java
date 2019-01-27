package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.FileDirectoryHandler;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.SHARED_RESOURCE;

/**
 * Class which is an actual implementation of {@link UmEditorFileHelperCore}
 */
public class UmEditorFileHelper implements UmEditorFileHelperCore {

    private File tempDestinationDir;

    private File contentEntryFile;

    private File sourceFile;

    private File destinationTempDir;

    private File mediaDestinationDir;

    private File opfFileDestination;

    private File epubFileDestination;

    private UmAppDatabase repository;

    private UmAppDatabase umAppDatabase;

    public static final String MEDIA_DIRECTORY = "media/";

    public static final String OEBPS_DIRECTORY = "OEBPS";

    protected Object context;

    private EmbeddedHTTPD embeddedHTTPD;

    private String baseResourceRequestUrl = null;

    private String mountedTempDirBaseUrl = null;

    private ZipFileTaskProgressListener zipTaskListener;

    private static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    private boolean isTestExecution = false;

    private static final String PAGE_PREFIX = "page_";

    private static final String ZIP_FILE_EXTENSION = ".zip";

    private static final String pageMimeType = "text/html";

    private static final int navDocumentDepth = 1;

    private EpubNavItem nextNavItem = null;

    private String assetsDir = String.format("assets-%s",
            new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault()).format(new Date()));

    public static final String EDITOR_BASE_DIR_NAME = "umEditor";

    /**
     * Interface which used to report file zipping progress update
     */
    public interface ZipFileTaskProgressListener {

        /**
         * Invoked when zip task process started
         */
        void onTaskStarted();
        /**
         * Invoked when there is a progress update during zipping process
         * @param progress new progress value
         */
        void onTaskProgressUpdate(int progress);

        /**
         * Invoked when zip task process is completed.
         */
        void onTaskCompleted();
    }

    /**
     * Initialize UmEditorFileHelper
     * @param context activity context
     */
    public void  init(Object context){
        this.context = context;
        startWebServer();
        UMStorageDir[] rootDir =
                UstadMobileSystemImpl.getInstance().getStorageDirs(SHARED_RESOURCE, context);
        File baseContentDir = new File(rootDir[0].getDirURI());
        File tempBaseDir = new File(baseContentDir,"temp/");
        if(!tempBaseDir.exists())tempBaseDir.mkdirs();
        tempDestinationDir = tempBaseDir;
        contentEntryFile = new File(baseContentDir,"UmFile-"+System.currentTimeMillis()+".zip");
        isTestExecution = baseContentDir.getAbsolutePath().startsWith("/var/");
        umAppDatabase = UmAppDatabase.getInstance(context);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
    }

    private void startWebServer() {
        embeddedHTTPD = new EmbeddedHTTPD(0, this);
        try {
            embeddedHTTPD.start();
            baseResourceRequestUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS+embeddedHTTPD.getListeningPort()+"/",
                    assetsDir, EDITOR_BASE_DIR_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set zip task file progress listener
     * @param zipFileTaskProgressListener listener instance
     */
    public void setZipTaskProgressListener(ZipFileTaskProgressListener zipFileTaskProgressListener){
        this.zipTaskListener = zipFileTaskProgressListener;
    }


    @Override
    public void createFile(long contentEntryUid,UmCallback<String> callback) {
        new Thread(() -> {
            String filePath;
            if(isTestExecution){
                filePath = "/com/ustadmobile/port/sharedse/";
            }else{
                filePath = "/http/"+ EDITOR_BASE_DIR_NAME +"/templates";
            }
            filePath = UMFileUtil.joinPaths(filePath,ContentEditorView.RESOURCE_BLANK_DOCUMENT);
            UstadMobileSystemImpl.getInstance().getAsset(context, filePath,
                    new UmCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream result) {
                    try {
                        if(UMFileUtil.copyFile(result,contentEntryFile)){
                            handleFileInDb(contentEntryUid,callback);
                        }else{
                            callback.onFailure(new Throwable("File was not copied"));
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
    public void mountFile(String filePath, UmCallback<Void> callback) {
        new Thread(() -> {
            sourceFile = new File(filePath);
            String mountedToPath = sourceFile.getName().replace(ZIP_FILE_EXTENSION,"");
            File extractToPath = new File(tempDestinationDir,mountedToPath);
            destinationTempDir = extractToPath;
            epubFileDestination = new File(destinationTempDir,OEBPS_DIRECTORY+File.separator);
            opfFileDestination = new File(epubFileDestination,"content.opf");
            mediaDestinationDir = new File(destinationTempDir,
                    UMFileUtil.joinPaths(OEBPS_DIRECTORY,MEDIA_DIRECTORY));
            boolean unZipped = false;
            try {
                unZipped = unZipFile(sourceFile, destinationTempDir);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailure(e);
            }finally {
                if(unZipped){
                    String resourceBaseDir =
                            UMFileUtil.joinPaths(EDITOR_BASE_DIR_NAME ,OEBPS_DIRECTORY);
                    embeddedHTTPD.addRoute(resourceBaseDir+"(.)+",
                            FileDirectoryHandler.class,new File(extractToPath,OEBPS_DIRECTORY));
                    mountedTempDirBaseUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS +
                            embeddedHTTPD.getListeningPort()+"/", EDITOR_BASE_DIR_NAME,OEBPS_DIRECTORY);
                    callback.onSuccess(null);
                }else{
                    callback.onSuccess(null);
                }
            }

        }).start();
    }


    /**
     * Handle file on database level
     * @param contentEntryFileUid Uid of the opened content entry
     * @param callback UmCallback
     */
    private void handleFileInDb(long contentEntryFileUid,UmCallback<String> callback){
        long lastModified = System.currentTimeMillis();
        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setLastModified(lastModified);

        ContentEntryFile mEntryFile = new ContentEntryFile();
        mEntryFile.setContentEntryFileUid(contentEntryFileUid);
        mEntryFile.setFileSize(contentEntryFile.length());
        ContentEntryDao contentEntryDao = repository.getContentEntryDao();
        ContentEntryFileDao fileDao = repository.getContentEntryFileDao();
        ContentEntryFileStatusDao fileStatusDao = umAppDatabase.getContentEntryFileStatusDao();
        ContentEntryContentEntryFileJoinDao joinDao =
                repository.getContentEntryContentEntryFileJoinDao();

        contentEntryDao.insertAsync(contentEntry, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long contentEntryUid) {
                fileDao.insertAsync(mEntryFile, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long fileUid) {
                        ContentEntryContentEntryFileJoin entryFileJoin =
                                new ContentEntryContentEntryFileJoin();
                        entryFileJoin.setCecefjContentEntryFileUid(contentEntryFileUid);
                        entryFileJoin.setCecefjContentEntryUid(contentEntryUid);
                        joinDao.insertAsync(entryFileJoin, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long cecefUid) {
                                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                                fileStatus.setCefsUid(contentEntryUid.intValue());
                                fileStatus.setCefsContentEntryFileUid(contentEntryFileUid);
                                fileStatus.setFilePath(contentEntryFile.getAbsolutePath());
                                fileStatusDao.insertAsync(fileStatus, new UmCallback<Long>() {
                                    @Override
                                    public void onSuccess(Long result) {
                                        callback.onSuccess(contentEntryFile.getAbsolutePath());
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        callback.onFailure(exception);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                callback.onFailure(exception);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }



    @Override
    public void updateFile(UmCallback<Boolean> callback) {
        new Thread(() -> {
            List<File> fileStructure = new ArrayList<>();
            getFileStructure(destinationTempDir,fileStructure);
            zipTaskListener.onTaskStarted();
            boolean zipped = createZipFiles(fileStructure);
            callback.onSuccess(zipped);
        }).start();
    }


    @Override
    public void removeUnUsedResources(UmCallback<Integer> callback) {
        new Thread(() -> {
            try {
                int unUsedFileCounter = 0;
                if(getDestinationMediaDirPath() != null){
                    File [] allResources = new File(getDestinationMediaDirPath()).listFiles();
                    assert allResources != null;
                    for(File resource:allResources){
                        if(!isResourceInUse(resource.getName())
                                && !resource.getName().equals(MEDIA_DIRECTORY)){
                            if(resource.delete()) {
                                String filename = resource.getName();
                                if(removeManifestItem(getFileNameWithoutExtension(filename))){
                                    unUsedFileCounter++;
                                }
                            }
                        }
                    }
                }
                callback.onSuccess(unUsedFileCounter);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Check if resource is being used in ant document tag.
     * @param resourceName Resource name to be checked
     * @return True if in use otherwise false.
     */
    private boolean isResourceInUse(String resourceName){
        try {
            List<EpubNavItem> navItems = getEpubNavDocument().getToc().getChildren()!=null?
                    getEpubNavDocument().getToc().getChildren():new ArrayList<>();
            for(EpubNavItem navItem: navItems){
                Document index = Jsoup.parse(UMFileUtil.readTextFile(
                        new File(getEpubFilesDestination(), navItem.getHref()).getAbsolutePath()));
                Element previewContainer = index.select("#" + EDITOR_BASE_DIR_NAME).first();
                Elements resources = previewContainer.select("img[src],source[src]");

                if(findResource(resourceName,resources)){
                    return true;
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean findResource(String resourceName, Elements resources){
        for (Element resource : resources) {
            String srcUrl = resource.attr("src");
            if (srcUrl.endsWith(resourceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Map file structure as on temp directory
     * @param sourceDir Temporary directory to be zipped
     * @param fileList File structure holder
     */
    private void getFileStructure(File sourceDir,List<File> fileList) {
        File[] files = sourceDir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getFileStructure(file,fileList);
            }
        }
    }

    /**
     * Create a zipped file from temp directory
     * @param fileList Files to be added to the zip
     * @return True if zip file was created successfully
     */
    private boolean createZipFiles(List<File> fileList) {
        int fileCounter = 0;
        try {
            FileOutputStream fos = new FileOutputStream(sourceFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addFileToZip(file, zos);
                    fileCounter++;
                    double progress = fileCounter/(double)fileList.size();
                    zipTaskListener.onTaskProgressUpdate((int) (progress * 100));
                }
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        zipTaskListener.onTaskCompleted();
        return sourceFile.exists();
    }

    /**
     * Add new file to the created zip
     * @param file file to be added to the zip
     * @param zos zip file output stream
     * @throws IOException
     */
    private void addFileToZip(File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String zipFilePath = file.getCanonicalPath()
                .substring(destinationTempDir.getCanonicalPath().length() + 1,
                        file.getCanonicalPath().length());
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    /**
     * Unzip zipped file to a specific destination directory.
     * @param sourceFile Source zipped file
     * @param destDir Directory where zipped file content will be put.
     * @throws IOException Exception thrown when something is wrong
     */
    private boolean unZipFile(File sourceFile, File destDir) throws IOException {
        if(!destDir.exists()) destDir.mkdirs();

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(sourceFile.getAbsoluteFile()));
        ZipEntry zipEntry = zipIn.getNextEntry();

        while(zipEntry != null){

            String fileName = zipEntry.getName();
            File fileToCreate = new File(destDir , fileName);
            fileToCreate.setLastModified(zipEntry.getTime());

            if(!fileToCreate.getParentFile().isDirectory()){
                if(!fileToCreate.getParentFile().mkdirs()) {
                    throw new RuntimeException("Could not create directory to extract to: " +
                            fileToCreate.getParentFile());
                }
            }
            if(!zipEntry.isDirectory()){
                FileOutputStream fos = new FileOutputStream(fileToCreate);
                int read;
                byte[] buffer = new byte[1024];
                while ((read = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                }

                fos.close();
            }

            zipIn.closeEntry();
            zipEntry = zipIn.getNextEntry();
        }

        zipIn.closeEntry();
        zipIn.close();

        return Objects.requireNonNull(destDir.listFiles()).length > 0;
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
    public void updateEpubTitle(String title,boolean newDocument, UmCallback<Boolean> callback) {
        if(newDocument){
            updateOpfMetadataInfo(title, UUID.randomUUID().toString());
        }
        callback.onSuccess(updateOpfMetadataInfo(title,null));
    }

    @Override
    public void updateManifestItems(String filename, String mimeType, UmCallback<Boolean> callback) {
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
            InputStream is = new FileInputStream(new File(getTempDestinationDirPath(),
                    PAGE_TEMPLATE));
            created = UMFileUtil.copyFile(is,new File(getEpubFilesDestination(), href));
            if(created){
                created = addNavItem(href,title) && addManifestItem(href, pageMimeType)
                        && addSpineItem(href,pageMimeType);
            }

        } catch (IOException e) {
            exception = e;
        }finally {
            if(created){
                callback.onSuccess(href);
            }else{
                callback.onFailure(exception);
            }
        }
    }

    @Override
    public void removePage(String  href, UmCallback<Boolean> callback) {

        boolean removed = removeNavItem(href) && removeSpineItem(href)
                && removeManifestItem(getFileNameWithoutExtension(href));

        callback.onSuccess(removed);
    }

    @Override
    public OpfDocument getEpubOpfDocument(){
        try{
            FileInputStream inputStream = new FileInputStream(opfFileDestination);
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
            FileInputStream docIn = new FileInputStream(new File(getEpubFilesDestination(),
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

    private String getFileNameWithoutExtension(String fileName){
        return fileName.replaceFirst("[.][^.]+$", "");
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
        spineItem.setId(getFileNameWithoutExtension(href));
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
            UMFileUtil.writeToFile(opfFileDestination,opfContent);
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
                UMFileUtil.writeToFile(opfFileDestination,opfContent);
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
        EpubNavItem navItem = new EpubNavItem(title,href,null,navDocumentDepth);
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
        tobeDeletedNavIndex = tobeDeletedNavIndex + (tobeDeletedNavIndex == navItem.size()-1 ? 1:-1);
        nextNavItem = navItems.get(tobeDeletedNavIndex);
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
        manifestItem.setId(getFileNameWithoutExtension(href));
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
            UMFileUtil.writeToFile(opfFileDestination,opfContent);
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
                UMFileUtil.writeToFile(opfFileDestination,opfContent);
                metaInfoUpdated = updateOpfMetadataInfo(null,null);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                UMIOUtils.closeQuietly(bout);
            }
        }
        return !getEpubOpfDocument().getManifestItems().containsKey(itemId) && metaInfoUpdated;
    }


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
            UMFileUtil.writeToFile(opfFileDestination,opfContent);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            UMIOUtils.closeQuietly(bout);
        }
        return title == null || getEpubOpfDocument().getTitle().equals(title);
    }

    @Override
    public String getSourceFilePath() {
        return sourceFile.getAbsolutePath();
    }

    @Override
    public String getBaseResourceRequestUrl() {
        return baseResourceRequestUrl;
    }

    @Override
    public String getTempDestinationDirPath() {
        return destinationTempDir.getAbsolutePath();
    }

    @Override
    public String getDestinationMediaDirPath() {
        return mediaDestinationDir.getAbsolutePath();
    }

    @Override
    public String getMountedTempDirRequestUrl() {
        return mountedTempDirBaseUrl;
    }

    @Override
    public String getEpubFilesDestination() {
        return epubFileDestination.getAbsolutePath();
    }

    /**
     * Add base resource request handler
     * @param handlerClass handler class
     */
    public void addBaseAssetHandler(Class handlerClass){
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  handlerClass, context);
    }
    
    private File getNavFile (){
        return new File(getEpubFilesDestination(), getEpubOpfDocument().getNavItem().getHref());
    }

    private EpubNavItem getNavItemByHref(String href,EpubNavItem root){
        for(EpubNavItem navItem: root.getChildren()){
            if(navItem.getHref().equals(href)){
                return navItem;
            }
        }
        return null;
    }

    private OpfItem getOpfItemByHref(String href, List<OpfItem> parentList){
        for(OpfItem opfItem : parentList){
            if(opfItem.getHref().equals(href)){
                return opfItem;
            }
        }
        return null;
    }
}
