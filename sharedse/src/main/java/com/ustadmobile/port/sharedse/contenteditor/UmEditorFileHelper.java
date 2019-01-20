package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.UmEditorFileHelperCore;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.SHARED_RESOURCE;


public class UmEditorFileHelper implements UmEditorFileHelperCore {

    private File tempDestinationDir;

    private File contentEntryFile;

    private File sourceFile;

    private File destinationTempDir;

    private File mediaDestinationDir;

    private UmAppDatabase repository;

    private UmAppDatabase umAppDatabase;

    public static final String MEDIA_DIRECTORY = "media/";

    public static final String OEBPS_DIRECTORY = "OEBPS";

    private String selectedPageIndex = null;

    protected Object context;

    private EmbeddedHTTPD embeddedHTTPD;

    private String baseResourceRequestUrl = null;

    private String mountedTempDirBaseUrl = null;

    private ZipFileTaskProgressListener zipTaskListener;

    private static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    private boolean isTestExecution = false;

    private static String TEMP_MEDIA_FILE = "media-hide.info";

    private static String PAGES_FILE_EXTENSION = ".html";

    private static final String PAGE_PREFIX = "page_";

    private static final String ZIP_FILE_EXTENSION = ".zip";

    private String contentOpfFile = "content.opf";

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
     * @param callback
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
                        if(!isResourceInUse(resource.getName()) && !resource.getName().equals(TEMP_MEDIA_FILE)){
                            if(resource.delete()) unUsedFileCounter++;
                        }
                    }
                }
                callback.onSuccess(unUsedFileCounter);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void addPageToTheDocument(String pageTitle, UmCallback<UmPage> callback) {
        List<UmPage> umPageList = getDocumentPages();
        UmPage umPage = new UmPage();
        int pageNumber = 1;
        if(umPageList.size() > 0){
            //create arrays of page numbers
            int [] pages = new int[umPageList.size()];
            for(int i = 0; i < umPageList.size(); i++){
                pages[i] = umPageList.get(i).getNumber();
            }
            int lastPageIndex = pages.length - 1;
            Arrays.sort(pages);
            pageNumber = pages[lastPageIndex]++;

        }
        umPage.setTitle(pageTitle);
        umPage.setNumber(pageNumber);
        umPage.setIndex(PAGE_PREFIX+pageNumber+PAGES_FILE_EXTENSION);
        boolean pageCreated = false;
        IOException exception = null;
        try{
            InputStream is = new FileInputStream(new File(getTempDestinationDirPath(), PAGE_TEMPLATE));
            pageCreated = UMFileUtil.copyFile(is,new File(getEpubFilesDestination(),
                    umPage.getIndex()));
            if(pageCreated){
                pageCreated = updatePageTitle(umPage.getTitle(), umPage.getIndex()) &&
                        updateSpineInOPF(umPage.getIndex());
            }
        } catch (IOException e) {
            exception = e;
        }finally {
            if(pageCreated){
                callback.onSuccess(umPage);
            }else{
                assert exception != null;
                callback.onFailure(new Throwable("New page was not added "+ exception.getMessage()));
            }
        }
    }

    @Override
    public void removePageFromTheDocument(String pageIndex, UmCallback<UmPage> callback) {
        UmPage deletedPage = null;
        for(UmPage umPage: getDocumentPages()){
            if(umPage.getIndex().equals(pageIndex)){
                deletedPage = umPage;
                break;
            }
        }
        File pagePath = new File(getEpubFilesDestination(), pageIndex);
        if(pagePath.delete()){
            callback.onSuccess(deletedPage);
        }else{
            callback.onFailure(new Throwable("Failed to delete page with index "+pageIndex));
        }
    }

    @Override
    public void setCurrentSelectedPage(String pageIndex) {
        this.selectedPageIndex = pageIndex;
    }

    @Override
    public List<UmPage> getDocumentPages() {
        File [] allFiles = new File(destinationTempDir,OEBPS_DIRECTORY).listFiles();
        String pageNameNumberSeparator = "_";
        int pageNumberIndex = 1;
        List<UmPage> pageList = new ArrayList<>();
        assert allFiles != null;
        for(File file: allFiles){
            if(file.getName().endsWith(PAGES_FILE_EXTENSION)
                    && !file.getName().equals(PAGE_TEMPLATE)){
                UmPage umPage = new UmPage();
                String [] pageName = file.getName().split(pageNameNumberSeparator);
                umPage.setNumber(pageName.length > 1
                        ? Integer.parseInt(pageName[pageNumberIndex]
                        .replace(PAGES_FILE_EXTENSION,"")):1);
                umPage.setIndex(file.getName());
                try {
                    umPage.setTitle(getPageTitle(file.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pageList.add(umPage);
            }
        }
        return pageList;
    }

    @Override
    public boolean updatePageTitle(String pageTitle, String pageIndex) throws IOException {
        File pagePath = new File(getEpubFilesDestination(), pageIndex);
        Document pageDoc =  Jsoup.parse(UMFileUtil.readTextFile(pagePath.getAbsolutePath()));
        pageDoc.select("title").first().text(pageTitle);
        UMFileUtil.writeToFile(pagePath,pageDoc.toString());
        return pageDoc.toString().contains(pageTitle);
    }


    /**
     * Check if resource is being used in ant document tag.
     * @param resourceName Resource name to be checked
     * @return True if in use otherwise false.
     */
    private boolean isResourceInUse(String resourceName){
        try {
            Document index = Jsoup.parse(UMFileUtil.readTextFile(
                    new File(getEpubFilesDestination(), selectedPageIndex).getAbsolutePath()));
            Element previewContainer = index.select("#" + EDITOR_BASE_DIR_NAME).first();
            Elements sources = previewContainer.select("img[src],source[src]");

            for (Element source : sources) {
                String srcUrl = source.attr("src");
                if (srcUrl.endsWith(resourceName)) {
                    return true;
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
        return new File(destinationTempDir,OEBPS_DIRECTORY+File.separator).getAbsolutePath();
    }

    /**
     * Add base resource request handler
     * @param handlerClass handler class
     */
    public void addBaseAssetHandler(Class handlerClass){
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  handlerClass, context);
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

    /**
     * Get page title from the page by it's index.
     * @param pageIndex index used to find a page to get a ttile from.
     * @return title of the page.
     * @throws IOException
     */
    private String getPageTitle (String pageIndex) throws IOException {
        File pagePath = new File(getEpubFilesDestination(), pageIndex);
        Document pageDoc =  Jsoup.parse(UMFileUtil.readTextFile(pagePath.getAbsolutePath()));
        return pageDoc.select("title").first().text();
    }

    /**
     * Update spine items on  opf file
     * @param pageIndex page index to be updates
     * @return true if updated otherwise false.
     * @throws IOException
     */
    private boolean updateSpineInOPF(String pageIndex) throws IOException {
        File pagePath = new File(getEpubFilesDestination(), contentOpfFile);
        Document pageDoc =  Jsoup.parse(UMFileUtil.readTextFile(pagePath.getAbsolutePath()));
        pageDoc.select("spine").append("<itemref idref=\""+pageIndex+"\"/>");
        UMFileUtil.writeToFile(pagePath,pageDoc.toString());
        return pageDoc.toString().contains(pageIndex);
    }


}
