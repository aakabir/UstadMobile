package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentPreviewPresenter;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class ContentPreviewActivity extends UstadBaseActivity implements ContentPreviewView {

    private WebView contentPreview;
    private ContentPreviewPresenter presenter;
    private Toolbar toolbar;

    private class WebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(presenter.isPreviewIndex()){
                presenter.setPreviewIndex(false);
                presenter.loadContentToPreview();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        contentPreview = findViewById(R.id.preview_content);
        toolbar = findViewById(R.id.um_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebSettings webSettings = contentPreview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        contentPreview.setWebViewClient(new WebClient());
        contentPreview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        contentPreview.clearCache(true);
        contentPreview.clearHistory();

        presenter = new ContentPreviewPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }

    @Override
    public void loadPreviewPage(String pageUrl) {
        contentPreview.loadUrl(pageUrl);
    }

    @Override
    public void startPreviewing(String content) {
        callJavaScriptFunction("ustadEditor.loadContentForPreview", content);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * Invoke javascript call from android
     * @param functionName name of the javascript function
     * @param params javascript function params
     */
    public void callJavaScriptFunction(String functionName,@Nullable String ...params){
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.append("javascript:try{");
        mBuilder.append(functionName);
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
        contentPreview.evaluateJavascript(call, value -> { });
    }


    @Override
    public void setTitle(String title) {
        if(toolbar != null){
            toolbar.setTitle(title +" Previewing");
        }
    }
}
