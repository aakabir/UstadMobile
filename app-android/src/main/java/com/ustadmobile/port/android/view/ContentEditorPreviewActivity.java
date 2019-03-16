package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentPreviewPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorChromeClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorInterface;
import com.ustadmobile.port.android.umeditor.UmWebJsResponse;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_PAGE_LOADED;
import static com.ustadmobile.port.android.umeditor.UmEditorUtil.getCurrentLocale;
import static com.ustadmobile.port.android.umeditor.UmEditorUtil.getDirectionality;
import static com.ustadmobile.port.android.umeditor.UmWebContentEditorClient.executeJsFunction;
import static com.ustadmobile.port.android.view.ContentEditorActivity.EDITOR_METHOD_PREFIX;

public class ContentEditorPreviewActivity extends UstadBaseActivity
        implements ContentPreviewView, UmWebContentEditorChromeClient.JsLoadingCallback {

    private WebView mWebView;

    private ProgressBar progressDialog;

    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        mWebView = findViewById(R.id.preview_content);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        progressDialog = findViewById(R.id.progressBar);
        setToolbar(toolbar);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);

        toolbarTitle.setVisibility(View.VISIBLE);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        mWebView.addJavascriptInterface(
                new UmWebContentEditorInterface(this,this),"UmEditor");
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.clearCache(true);
        mWebView.clearHistory();



        ContentPreviewPresenter presenter = new ContentPreviewPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }


    private void setToolbar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if(toolbar != null){
            toolbar.setTitle("");
        }
        toolbarTitle.setText(UstadMobileSystemImpl.getInstance()
                .getString(MessageID.content_preview_title,this));
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

    @Override
    public void startPreviewing(String baseRequestUri, String indexFile) {
        mWebView.setWebViewClient(new UmWebContentEditorClient(this, true));
        mWebView.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        progressDialog.setVisibility(View.VISIBLE);
        mWebView.loadUrl(indexFile);
    }

    @Override
    public void onProgressChanged(int newProgress) {
        progressDialog.setProgress(newProgress);
    }

    @Override
    public void onPageFinishedLoading() {
        progressDialog.setVisibility(View.GONE);
    }

    @Override
    public void onCallbackReceived(String value) {
        if(value.contains("action")){
            UmWebJsResponse callback = new Gson().fromJson(value,UmWebJsResponse.class);
            if(callback.getAction().equals(ACTION_PAGE_LOADED)){
                executeJsFunction(mWebView,
                        EDITOR_METHOD_PREFIX + "onCreate", this,
                        getCurrentLocale(this), getDirectionality(this));
            }
        }
    }
}
