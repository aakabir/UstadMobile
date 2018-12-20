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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.ustadmobile.core.controller.CatalogPresenter.SHARED_RESOURCE;

public class UmEditorFileHelper implements UmEditorFileHelperCore {

    private File tempDestinationDir;

    private File contentEntryFile;

    private File sourceFile;

    private File destinationTempDir;

    private File mediaDestinationDir;

    private UmAppDatabase repository;

    private UmAppDatabase umAppDatabase;

    public static final String MEDIA_DIRECTORY = "media/";

    protected Object context;

    private EmbeddedHTTPD embeddedHTTPD;

    private String baseResourceRequestUrl = null;

    private String mountedTempDirBaseUrl = null;

    private ZipFileTaskProgressListener zipTaskListener;

    private static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    private boolean isTestExecution = false;

    private String assetsDir = "assets-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "";

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
            String mountedToPath = sourceFile.getName().replace(".zip","");
            File extractToPath = new File(tempDestinationDir,mountedToPath);
            destinationTempDir = extractToPath;
            mediaDestinationDir = new File(destinationTempDir,MEDIA_DIRECTORY);
            boolean unZipped = false;
            try {
                unZipped = unZipFile(sourceFile, destinationTempDir);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailure(e);
            }finally {
                if(unZipped){
                    embeddedHTTPD.addRoute( EDITOR_BASE_DIR_NAME +"(.)+",
                            FileDirectoryHandler.class,extractToPath);
                    mountedTempDirBaseUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS +
                            embeddedHTTPD.getListeningPort()+"/", EDITOR_BASE_DIR_NAME);
                    callback.onSuccess(null);
                }else{
                    callback.onSuccess(null);
                }
            }
        }).start();
    }



    private void handleFileInDb(long contentEntryFileUid,UmCallback<String> callback){

        long lastModified = System.currentTimeMillis();
        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setLastModified(lastModified);

        ContentEntryFile mEntryFile = new ContentEntryFile();
        mEntryFile.setContentEntryFileUid(contentEntryFileUid);
        mEntryFile.setFileSize(this.contentEntryFile.length());
        ContentEntryDao contentEntryDao = repository.getContentEntryDao();
        ContentEntryFileDao fileDao = umAppDatabase.getContentEntryFileDao();
        ContentEntryFileStatusDao fileStatusDao = umAppDatabase.getContentEntryFileStatusDao();
        ContentEntryContentEntryFileJoinDao joinDao =
                umAppDatabase.getContentEntryContentEntryFileJoinDao();

        contentEntryDao.insertAsync(contentEntry, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long contentEntryUid) {
                fileDao.insertAsync(mEntryFile, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long contentEntryFileUid) {
                        ContentEntryContentEntryFileJoin entryFileJoin =
                                new ContentEntryContentEntryFileJoin();
                        entryFileJoin.setCecefjContentEntryFileUid(contentEntryFileUid);
                        entryFileJoin.setCecefjContentEntryUid(contentEntryUid);
                        joinDao.insertAsync(entryFileJoin, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long cecefUid) {
                                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                                fileStatus.setCefsUid(contentEntryUid);
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
            int unUsedFileCounter = 0;
            File [] allResources = mediaDestinationDir.listFiles();
            for(File resource:allResources){
                if(!isResourceInUse(resource.getName())){
                    if(resource.delete()) unUsedFileCounter++;
                }
            }
            callback.onSuccess(unUsedFileCounter);
        }).start();
    }


    private boolean isResourceInUse(String resourceName){
        try{
            //Get all media which are in use from the editor
            Elements resourceInUse = new Elements();
            Document index = Jsoup.parse(UMFileUtil.readTextFile(
                    new File(destinationTempDir,INDEX_FILE).getAbsolutePath()));
            Element previewContainer = index.select("#"+EDITOR_BASE_DIR_NAME).first();
            if(previewContainer.select("img[src]").size() > 0){
                resourceInUse.addAll(previewContainer.select("img[src]"));
            }
            if(previewContainer.select("source[src]").size() > 0){
                resourceInUse.addAll(previewContainer.select("source[src]"));
            }

            for(Element resource: resourceInUse){
                if(resource.toString().contains(resourceName)){
                    return true;
                }
            }

        } catch (NullPointerException | IOException e){
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
    public String getDestinationDirPath() {
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
        if(sourceFile.exists())sourceFile.delete();
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
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(sourceFile));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            File newFile = new File(UMFileUtil.joinPaths(destDir.getAbsolutePath(),entry.getName()));
            newFile.setLastModified(entry.getTime());
            if (!entry.isDirectory()) {
                BufferedOutputStream bos =
                        new BufferedOutputStream(new FileOutputStream(newFile.getAbsolutePath()));
                byte[] bytesIn = new byte[1024];
                int read;
                while ((read = zipIn.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
            } else {
                newFile.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        return destDir.list().length > 0;
    }

}
