package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.ustadmobile.core.view.ContentEditorView.RESOURCE_JS_TINYMCE;
import static com.ustadmobile.core.view.ContentEditorView.RESOURCE_JS_USTAD_EDITOR;
import static com.ustadmobile.core.view.ContentEditorView.RESOURCE_JS_USTAD_WIDGET;
import static com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper.EDITOR_BASE_DIR_NAME;

/**
 * Class which handles HTTP request from WebView and native-to-js client interaction
 *
 * <b>Note: Operation Flow</b>
 *
 *  Use {@link UmWebContentEditorClient#shouldInterceptRequest } to intercept
 *  requested resources via HTTP.
 *
 *  Use {@link UmWebContentEditorClient#executeJsFunction} to execute Javascript
 *  function from native android and wait for the callback if execution
 *  log a message or return value
 *
 * @author kileha3
 *
 */
public class UmWebContentEditorClient extends WebViewClient {

    private Context context;

    private String [] resourceTag = new String[]{
            "plugin",
            "skin",
            "theme",
            "font",
            "templates",
            "locale",
            "material-icon"
    };

    /**
     * @param context application context
     */
    public UmWebContentEditorClient(Context context){
        this.context = context;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        InputStream inputStream;
        String resourceUri = request.getUrl().toString();
        String mimeType = UmEditorUtil.getMimeType(context,request.getUrl());
        if(mimeType == null && resourceUri.endsWith(".json")){
            mimeType = "application/json";
        }

        if(isInnerResource(resourceUri) || isUmEditorResource(resourceUri)){
            try{
                String resourcePath = UMFileUtil.joinPaths("http",
                        EDITOR_BASE_DIR_NAME,getResourcePath(resourceUri));
                inputStream = context.getAssets().open(resourcePath);
                return new WebResourceResponse(mimeType,"utf-8", 200,
                        "OK", request.getRequestHeaders(),inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.shouldInterceptRequest(view, request);
    }

    private String getResourcePath(String requestUri){
        String [] parts = requestUri.split("/");
        ArrayList<String> newParts = new ArrayList<>();
        for(int i = 0; i< parts.length;i++){
            if(i > 3){
                newParts.add(parts[i]);
            }
        }
        String[] url = new String[newParts.size()];
        url = newParts.toArray(url);
        return UMFileUtil.joinPaths(url);
    }


    /**
     * Check if the resource is from plugin calls.
     * @param uri requested resource uri
     * @return true if are tinymce calls otherwise false.
     */
    private boolean isInnerResource(String uri){
        for(String resource: resourceTag){
            if(uri.contains(resource) && !uri.contains(RESOURCE_JS_USTAD_WIDGET)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the resource is one of the Editor core resource.
     * @param uri requested resource uri
     * @return true if are editor core resource otherwise false.
     */
    private boolean isUmEditorResource(String uri){
        return uri.contains(RESOURCE_JS_TINYMCE)
                || uri.contains(RESOURCE_JS_USTAD_EDITOR);
    }

    /**
     * Execute js function from native android
     *
     * @param mWeb Current active WebView instance
     * @param function name of the function to be executed
     * @param callback listener
     * @param params params to be passed to the function
     */
    public static void executeJsFunction(WebView mWeb, String function,
                                         UmWebContentEditorChromeClient.JsLoadingCallback callback,
                                         String ...params){
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.append("javascript:try{");
        mBuilder.append(function);
        mBuilder.append("(");
        String separator = "";
        if(params != null && params.length > 0){
            for (String param : params) {
                mBuilder.append(separator);
                separator = ",";
                if(param != null){
                    mBuilder.append("\"");
                }
                mBuilder.append(param);
                if(param != null){
                    mBuilder.append("\"");
                }
            }
        }
        mBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = mBuilder.toString();
        mWeb.evaluateJavascript(call, callback::onCallbackReceived);
    }
}
