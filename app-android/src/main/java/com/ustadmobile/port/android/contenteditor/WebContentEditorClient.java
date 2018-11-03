package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import java.io.InputStream;

import static com.ustadmobile.core.view.ContentEditorView.RESOURCE_JS_USTAD_WIDGET;

/**
 * Class which handles HTTP request from WebView and native-to-js client interaction
 *
 * <b>Note: Operation Flow</b>
 *
 *  Use {@link WebContentEditorClient#shouldInterceptRequest } to intercept
 *  requested resources via HTTP.
 *
 *  Use {@link WebContentEditorClient#executeJsFunction} to execute Javascript
 *  function from native android and wait for the callback if execution
 *  log a message or return value
 *
 * @author kileha3
 */
public class WebContentEditorClient extends WebViewClient {

    private Context context;

    private String [] resourceTag = new String[]{
            "plugin",
            "skin",
            "theme",
            "font",
            "templates"
    };

    private String localUrl;

    /**
     * @param context application context
     * @param localUrl local base url
     */
    public WebContentEditorClient(Context context,String localUrl){
        this.context = context;
        this.localUrl = localUrl;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        InputStream inputStream;
        String resourceUri = request.getUrl().toString();
        String mimeType = UmAndroidUriUtil.getMimeType(context,request.getUrl());
        if(isInnerResource(resourceUri)){
            try {
                String resourcePath = UMFileUtil.joinPaths("http",
                        "/tinymce/"+resourceUri.replace(localUrl+"/",""));
                inputStream = context.getAssets().open(resourcePath);
                return new WebResourceResponse(mimeType,"utf-8", 200,
                        "OK", request.getRequestHeaders(),inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.shouldInterceptRequest(view, request);
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
     * Execute js function from native android
     *
     * @param mWeb Current active WebView instance
     * @param function name of the function to be executed
     * @param callback listener
     * @param params params to be passed to the function
     */
    public static void executeJsFunction(WebView mWeb, String function,
                                         WebContentEditorChrome.JsLoadingCallback callback, String ...params){
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
