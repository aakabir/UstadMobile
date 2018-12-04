package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.net.Uri;

import com.ustadmobile.core.db.UmAppDatabase;
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
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.FileDirectoryHandler;
import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.ustadmobile.core.controller.CatalogPresenter.SHARED_RESOURCE;

public class UmEditorFileHelperAndroid extends UmEditorFileHelper {

    private File tempDestinationDir;

    private File contentEntryFile;

    private File sourceFile;

    private File destinationTempDir;

    private File mediaDestinationDir;

    private UmAppDatabase repository;

    private UmAppDatabase umAppDatabase;

    private EmbeddedHTTPD embeddedHTTPD;

    public static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    public static final String INDEX_FILE = "index.html";

    public static final String INDEX_TEMP_FILE = "index_.html";

    public static final String MEDIA_DIRECTORY = "media/";

    private int usedResourceCounter = 0;

    private String baseRequestUrl = null;

    @Override
    public void init(Object context) {
        super.init(context);
        UMStorageDir[] rootDir =
                UstadMobileSystemImpl.getInstance().getStorageDirs(SHARED_RESOURCE,context);
        File baseContentDir = new File(rootDir[0].getDirURI());
        File tempBaseDir = new File(baseContentDir,"temp/");
        if(!tempBaseDir.exists())tempBaseDir.mkdir();
        tempDestinationDir = tempBaseDir;
        contentEntryFile = new File(baseContentDir,"UmFile-"+System.currentTimeMillis()+".zip");
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        umAppDatabase = UmAppDatabase.getInstance(context);
        startWebServer();
    }



    /**
     * Set zip task file progress listener
     * @param zipFileTaskProgressListener listener instance
     */
    public void setZipFileProgressListener(ZipFileTaskProgressListener zipFileTaskProgressListener) {
        this.zipTaskListener = zipFileTaskProgressListener;
    }

    @Override
    public boolean startWebServer() {
        embeddedHTTPD = new EmbeddedHTTPD(0, this);
        boolean started = false;
        String assetsDir = "assets-" +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "";
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  AndroidAssetsHandler.class, this);
        try {
            embeddedHTTPD.start();
             baseRequestUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS+embeddedHTTPD.getListeningPort()+"/",
                     assetsDir,"tinymce");
             started = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return started;
    }

    @Override
    public void createFile(UmCallback<Long> callback) {
        new Thread(() -> {
            IOException exception = null;
            String resourceUrl = UMFileUtil.joinPaths(baseRequestUrl,
                    "templates/"+ContentEditorView.RESOURCE_BLANK_DOCUMENT);
            ResumableHttpDownload resumableHttpDownload = new ResumableHttpDownload(resourceUrl,
                    contentEntryFile.getAbsolutePath());
            boolean isDownloaded = false;
            try{
                isDownloaded = resumableHttpDownload.download();
            } catch (IOException e) {
                exception = e;
            }finally {
                if(isDownloaded){
                    handleFileInDb(callback);
                }else{
                    callback.onFailure(exception);
                }
            }
        }).start();
    }

    private void handleFileInDb(UmCallback<Long> callback){

        long lastModified = System.currentTimeMillis();

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setLastModified(lastModified);

        ContentEntryFile mEntryFile = new ContentEntryFile();
        mEntryFile.setLastModified(lastModified);
        mEntryFile.setFileSize(this.contentEntryFile.length());
        mEntryFile.setMimeType(UmAndroidUriUtil.getMimeType((Context) context,
                Uri.fromFile(this.contentEntryFile)));

        repository.getContentEntryDao().insertAsync(contentEntry, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long contentEntryUid) {
                umAppDatabase.getContentEntryFileDao()
                        .insertAsync(mEntryFile, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long contentEntryFileUid) {
                        ContentEntryContentEntryFileJoin entryFileJoin =
                                new ContentEntryContentEntryFileJoin();
                        entryFileJoin.setCecefjContentEntryFileUid(contentEntryFileUid);
                        entryFileJoin.setCecefjContentEntryUid(contentEntryUid);
                        umAppDatabase.getContentEntryContentEntryFileJoinDao()
                                .insertAsync(entryFileJoin, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long cecefUid) {
                                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                                fileStatus.setCefsContentEntryFileUid(contentEntryFileUid);
                                fileStatus.setFilePath(contentEntryFile.getAbsolutePath());
                                umAppDatabase.getContentEntryFileStatusDao()
                                        .insertAsync(fileStatus, new UmCallback<Long>() {
                                    @Override
                                    public void onSuccess(Long result) {
                                        callback.onSuccess(contentEntryFileUid);
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
    public void mountFile(long contentEntryFileUid, UmCallback<String> callback) {

        new Thread(() ->
                umAppDatabase.getContentEntryFileStatusDao()
                        .findByContentEntryFileUid(contentEntryFileUid,
                new UmCallback<ContentEntryFileStatus>() {
                    @Override
                    public void onSuccess(ContentEntryFileStatus fileStatus) {
                        sourceFile = new File(fileStatus.getFilePath());
                        String mountedToPath = new File(fileStatus.getFilePath())
                                .getName().replace(".zip","");
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
                                String mountedPathPrefix = "umEditor";
                                embeddedHTTPD.addRoute( mountedPathPrefix+"(.)+",
                                        FileDirectoryHandler.class,extractToPath);
                                String baseRequestUrl = UMFileUtil.joinPaths(LOCAL_ADDRESS +
                                        embeddedHTTPD.getListeningPort()+"/", mountedPathPrefix);
                                try {
                                    UMFileUtil.copyFile(new File(destinationTempDir,INDEX_FILE),
                                            new File(destinationTempDir,INDEX_TEMP_FILE));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                callback.onSuccess(baseRequestUrl);
                            }else{
                                callback.onSuccess(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                })).start();


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
    public void removeUnUsedResources(UmCallback<Boolean> callback) {
        new Thread(() -> {
            int unUsedFileCounter = 0;
            File [] allResources = mediaDestinationDir.listFiles();
            for(File resource:allResources){
                if(!isResourceInUse(resource.getName())){
                    if(resource.delete()) unUsedFileCounter++;
                }
            }
            callback.onSuccess(unUsedFileCounter ==
                    (allResources.length - usedResourceCounter) || unUsedFileCounter == 0);
        }).start();
    }


    private boolean isResourceInUse(String resourceName){
        try{
            //Get all media which are in use from the editor
            Elements resourceInUse = new Elements();
            Document index = Jsoup.parse(UMFileUtil.readTextFile(
                    new File(destinationTempDir,INDEX_FILE).getAbsolutePath()));
            Element previewContainer = index.select("#umPreview").first();
            if(previewContainer.select("img[src]").size() > 0){
                resourceInUse.addAll(previewContainer.select("img[src]"));
            }
            if(previewContainer.select("source[src]").size() > 0){
                resourceInUse.addAll(previewContainer.select("source[src]"));
            }

            usedResourceCounter = resourceInUse.size();

            for(Element resource: resourceInUse){
                if(resource.toString().contains(resourceName)){
                    return true;
                }
            }

        } catch (IOException e){
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
        return baseRequestUrl;
    }

    @Override
    public String getDestinationDirPath() {
        return destinationTempDir.getAbsolutePath();
    }

    @Override
    public String getDestinationMediaDirPath() {
        return mediaDestinationDir.getAbsolutePath();
    }

    /**
     * Map file structure as on temp directory
     * @param sourceDir Temporary directory to be zipped
     * @param fileList File struture holder
     */
    private void getFileStructure(File sourceDir,List<File> fileList) {
        File[] files = sourceDir.listFiles();
        for (File file : files) {
            if(!file.getName().endsWith(INDEX_TEMP_FILE)){
                fileList.add(file);
            }
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
