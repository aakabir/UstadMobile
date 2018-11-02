package com.ustadmobile.port.android.contenteditor;

import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContentEditorResourceHandler {

    private class EditorResource{

        private String resourceSource;

        private File resourceDestination;

        EditorResource(String resourceSource, File resourceDestination) {
            this.resourceSource = resourceSource;
            this.resourceDestination = resourceDestination;
        }

    }

    private ContentEditorResourceCopyListener handlerListener;

    private HashMap<String, File> directories;

    private String requestBaseUrl;

    private List<EditorResource> resourceList;

    private int resourceCounter = 1;

    private int resourceMaxCounter = 4;


    public ContentEditorResourceHandler(HashMap<String, File> directories, String baseUrl){
        this.directories = directories;
        this.requestBaseUrl = baseUrl;
        resourceList = new ArrayList<>();
    }

    public ContentEditorResourceHandler with(ContentEditorResourceCopyListener listener){
        this.handlerListener = listener;
        File cssDir = directories.get(ContentEditorView.CONTENT_CSS_DIR);
        File jsDir = directories.get(ContentEditorView.CONTENT_JS_DIR);

        //resources
        EditorResource bootstrapCss = new EditorResource(
                requestBaseUrl+"styles/"+ContentEditorView.CONTENT_CSS_BOOTSTRAP,
                new File(cssDir.getAbsolutePath(),ContentEditorView.CONTENT_CSS_BOOTSTRAP));

        EditorResource ustdmobileCss = new EditorResource(
                requestBaseUrl+"styles/"+ContentEditorView.CONTENT_CSS_USTAD,
                new File(cssDir.getAbsolutePath(),ContentEditorView.CONTENT_CSS_USTAD));

        EditorResource bootstrapJs= new EditorResource(
                requestBaseUrl+"js/"+ContentEditorView.CONTENT_JS_BOOTSTRAP,
                new File(jsDir.getAbsolutePath(),ContentEditorView.CONTENT_JS_BOOTSTRAP));

        EditorResource ustdmobileJs = new EditorResource(
                requestBaseUrl+"js/plugins/ustadmobile/"+ContentEditorView.CONTENT_JS_USTAD_WIDGET,
                new File(jsDir.getAbsolutePath(),ContentEditorView.CONTENT_JS_USTAD_WIDGET));

        EditorResource jquery = new EditorResource(
                requestBaseUrl+"js/"+ContentEditorView.CONTENT_JS_JQUERY,
                new File(jsDir.getAbsolutePath(),ContentEditorView.CONTENT_JS_JQUERY));

        resourceList.add(bootstrapCss);
        resourceList.add(bootstrapJs);
        resourceList.add(ustdmobileCss);
        resourceList.add(ustdmobileJs);
        resourceList.add(jquery);
        return this;
    }

    public void startCopying(){
        if(resourceList.size() > 0){
            EditorResource resource = resourceList.remove(0);
            boolean isDownloaded;
            if(!resource.resourceDestination.exists()){
                ResumableHttpDownload resumableHttpDownload = new ResumableHttpDownload(resource.resourceSource,
                        resource.resourceDestination.getAbsolutePath());
                isDownloaded = false;
                try{
                     isDownloaded = resumableHttpDownload.download();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {

                    if(isDownloaded){
                        resumableHttpDownload.cleanup();
                        resourceCounter++;
                    }

                    if(resourceList.size() > 0){
                        startCopying();
                    }

                    if(resourceCounter == resourceMaxCounter){
                        handlerListener.onResouceReady();
                        resourceCounter = 1;
                    }
                }
            }
        }else{
            resourceCounter++;
            if(resourceCounter == resourceMaxCounter){
                handlerListener.onResouceReady();
                resourceCounter = 1;
            }else {
                startCopying();
            }

        }
    }




}
