package com.ustadmobile.port.android.contenteditor;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which handle copying of core resources to the editor temporary directory
 * for preview / editing purposes.
 *
 * <b>Operational flow</b>
 *  If resources are already in their respective directory then copy listener will
 *  invoke the completion method otherwise it will download them and save to their
 *  respective directories.
 * <p>
 *     Use {@link ContentEditorResourceHandler#with} to set copy listener to the task
 *
 *     Use {@link ContentEditorResourceHandler#startCopying()} to start copying resources
 *     from assets dir to the local dir
 *
 * </p>
 */
public class ContentEditorResourceHandler {

    private class EditorResource{

        private String resourceSource;

        private File resourceDestination;

        EditorResource(String resourceSource, File resourceDestination) {
            this.resourceSource = resourceSource;
            this.resourceDestination = resourceDestination;
        }

    }

    private ResourceCopyTaskListener handlerListener;

    private String temMountDir;

    private String requestBaseUrl;

    private List<EditorResource> resourceList;

    private int resourceCounter = 0;

    private int resourceMaxCounter = 7;


    /**
     *@param tempMountDir Root dir where counted were mounted
     * @param localUrl HTTP route url to the directory
     */
    public ContentEditorResourceHandler(String tempMountDir, String localUrl){
        this.temMountDir = tempMountDir;
        this.requestBaseUrl = localUrl;
        resourceList = new ArrayList<>();
    }

    /**
     *  Set task listener
     * @param listener listen for task completion
     * @return ContentEditorResourceHandler instance
     */
    public ContentEditorResourceHandler with(ResourceCopyTaskListener listener){
        this.handlerListener = listener;
        File stylesDir = new File(temMountDir,"css/");
        File scriptsDir = new File(temMountDir,"js/");
        File mediaDir = new File(temMountDir,"media/");

        //Create directories if don't exists
        if(!stylesDir.exists()) stylesDir.mkdir();

        if(!scriptsDir.exists()) scriptsDir.mkdir();

        if(!mediaDir.exists()) mediaDir.mkdir();

        //resources
        EditorResource cssBootstrap = new EditorResource(
                "css/" + ContentEditorView.RESOURCE_CSS_BOOTSTRAP,
                new File(stylesDir.getAbsolutePath(),ContentEditorView.RESOURCE_CSS_BOOTSTRAP));

        EditorResource cssUstad = new EditorResource(
                "css/" + ContentEditorView.CONTENT_CSS_USTAD,
                new File(stylesDir.getAbsolutePath(),ContentEditorView.CONTENT_CSS_USTAD));

        EditorResource jsBootstrap = new EditorResource(
                "js/" + ContentEditorView.RESOURCE_JS_BOOTSTRAP,
                new File(scriptsDir.getAbsolutePath(),ContentEditorView.RESOURCE_JS_BOOTSTRAP));

        EditorResource jsUstadWidget = new EditorResource(
                "js/plugins/ustadmobile/"+ ContentEditorView.RESOURCE_JS_USTAD_WIDGET,
                new File(scriptsDir.getAbsolutePath(),ContentEditorView.RESOURCE_JS_USTAD_WIDGET));
        EditorResource jsUstadEditor = new EditorResource(
                "js/" + ContentEditorView.RESOURCE_JS_USTAD_EDITOR,
                new File(scriptsDir.getAbsolutePath(),ContentEditorView.RESOURCE_JS_USTAD_EDITOR));

        EditorResource jsTinyMCE = new EditorResource(
                "js/" + ContentEditorView.RESOURCE_JS_TINYMCE,
                new File(scriptsDir.getAbsolutePath(),ContentEditorView.RESOURCE_JS_TINYMCE));

        EditorResource jsJQuery = new EditorResource(
                "js/" + ContentEditorView.RESOURCE_JS_JQUERY,
                new File(scriptsDir.getAbsolutePath(),ContentEditorView.RESOURCE_JS_JQUERY));


        resourceList.add(jsBootstrap);
        resourceList.add(jsUstadWidget);
        resourceList.add(jsUstadEditor);
        resourceList.add(jsTinyMCE);
        resourceList.add(jsJQuery);
        resourceList.add(cssBootstrap);
        resourceList.add(cssUstad);
        return this;
    }

    /**
     * Start copying resources to local directory
     */
    public void startCopying(){
        if(resourceList.size() > 0){
            EditorResource resource = resourceList.remove(0);
            boolean isDownloaded;
            String resourceUrl = UMFileUtil.joinPaths(requestBaseUrl,resource.resourceSource);
            if(!resource.resourceDestination.exists()){
                ResumableHttpDownload resumableHttpDownload = new ResumableHttpDownload(resourceUrl,
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
                        handlerListener.onResourcesReady();
                        resourceCounter = 0;
                    }
                }
            }else{
                checkNext();
            }
        }else{
            checkNext();

        }
    }

    /**
     * Check next resource if can be downloaded
     */
    private void checkNext(){
        resourceCounter++;
        if(resourceCounter == resourceMaxCounter){
            handlerListener.onResourcesReady();
            resourceCounter = 0;
        }else {
            startCopying();
        }
    }
}
