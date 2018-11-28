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
import com.ustadmobile.port.sharedse.contenteditor.ContentEditorFileHelper;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.FileDirectoryHandler;
import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.ustadmobile.core.controller.CatalogPresenter.SHARED_RESOURCE;

public class ContentEditorFileHelperAndroid extends ContentEditorFileHelper {

    private File destinationDir;

    private File destinationFile;

    private UmAppDatabase repository;

    private UmAppDatabase umAppDatabase;

    private Context context;

    private EmbeddedHTTPD embeddedHTTPD;

    public static final String LOCAL_ADDRESS = "http://127.0.0.1:";

    public ContentEditorFileHelperAndroid(Object context) {
        super(context);
        this.context = (Context) context;
        UMStorageDir[] rootDir =
                UstadMobileSystemImpl.getInstance().getStorageDirs(SHARED_RESOURCE,context);
        destinationDir = new File(rootDir[0].getDirURI());
        destinationFile = new File(destinationDir,ContentEditorView.RESOURCE_BLANK_DOCUMENT);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        umAppDatabase = UmAppDatabase.getInstance(context);
    }

    @Override
    public void createFile(String baseResourceUrl,UmCallback<Long> callback) {
        new Thread(() -> {
            IOException exception = null;
            String resourceUrl = UMFileUtil.joinPaths(baseResourceUrl,
                    "templates/"+ContentEditorView.RESOURCE_BLANK_DOCUMENT);
            ResumableHttpDownload resumableHttpDownload = new ResumableHttpDownload(resourceUrl,
                    destinationFile.getAbsolutePath());
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

    public void setEmbeddedHTTPD(EmbeddedHTTPD embeddedHTTPD){
        this.embeddedHTTPD = embeddedHTTPD;
    }

    private void handleFileInDb(UmCallback<Long> callback){

        long lastModified = System.currentTimeMillis();

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setLastModified(lastModified);

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setLastModified(lastModified);
        contentEntryFile.setFileSize(destinationFile.length());
        contentEntryFile.setMimeType(UmAndroidUriUtil.getMimeType(context,
                Uri.fromFile(destinationFile)));

        repository.getContentEntryDao().insertAsync(contentEntry, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long contentEntryUid) {
                umAppDatabase.getContentEntryFileDao()
                        .insertAsync(contentEntryFile, new UmCallback<Long>() {
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
                                fileStatus.setFilePath(destinationFile.getAbsolutePath());
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

        umAppDatabase.getContentEntryFileStatusDao().findByContentEntryFileUid(contentEntryFileUid,
                        new UmCallback<ContentEntryFileStatus>() {
            @Override
            public void onSuccess(ContentEntryFileStatus fileStatus) {
                String mountedToPath = "temp_"+System.currentTimeMillis()+"/";
                File extractToPath = new File(destinationDir,mountedToPath);
                new Thread(() -> {
                    boolean unZipped = false;
                    try {
                        unZipped = UMFileUtil.unZipFile(new File(fileStatus.getFilePath()), extractToPath);
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
                            callback.onSuccess(baseRequestUrl);
                        }else{
                            callback.onSuccess(null);
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });

    }

    @Override
    public void updateResource(InputStream sourceInputStream, String destinationPath,
                               UmCallback<Void> callback) {

    }

    @Override
    public void removeUnUsedResources(UmCallback<Void> callback) {

    }
}
