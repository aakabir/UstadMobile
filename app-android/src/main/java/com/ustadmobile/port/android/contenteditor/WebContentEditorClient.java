package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;

import java.io.IOException;
import java.io.InputStream;

import static com.ustadmobile.core.view.ContentEditorView.CONTENT_JS_THEME;

/**
 * Class which intercepts HTTP request and redirect reource to the asset dir.
 *
 * @author kileha3
 */
public class WebContentEditorClient extends WebViewClient {

    private Context context;

    private String [] resourceTag = new String[]{
            "plugin",
            "skin",
            "theme",
            "font"
    };

    private String localUrl;

    public WebContentEditorClient(Context context,String localUrl){
        this.context = context;
        this.localUrl = localUrl;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        InputStream inputStream;
        String resourceUri = request.getUrl().toString();
        if(isInnerResource(resourceUri)){
            try {
                String resourcePath = UMFileUtil.joinPaths("http",
                        "/tinymce/"+resourceUri.replace(localUrl+"/",""));
                inputStream = context.getAssets().open(resourcePath);
                String mimeType = UmAndroidUriUtil.getMimeType(context,request.getUrl());
                return new WebResourceResponse(mimeType,"utf-8", 200,
                        "OK", request.getRequestHeaders(),inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.shouldInterceptRequest(view, request);
    }

    private boolean isInnerResource(String uri){
        for(String resource: resourceTag){
            if(uri.contains(resource) && !uri.contains(ContentEditorView.CONTENT_JS_USTAD_WIDGET)){
                return true;
            }
        }
        return false;
    }
}
