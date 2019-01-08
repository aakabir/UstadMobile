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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentPreviewPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorChromeClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorClient;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class ContentEditorPreviewActivity extends UstadBaseActivity
        implements ContentPreviewView, UmWebContentEditorChromeClient.JsLoadingCallback {

    private WebView contentPreview;

    private ProgressBar progressDialog;

    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        contentPreview = findViewById(R.id.preview_content);
        contentPreview.setBackgroundColor(Color.TRANSPARENT);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        progressDialog = findViewById(R.id.progressBar);
        setToolbar(toolbar);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);

        toolbarTitle.setVisibility(View.VISIBLE);

        WebSettings webSettings = contentPreview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        contentPreview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        contentPreview.clearCache(true);
        contentPreview.clearHistory();

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
                .getString(MessageID.content_preview,this));
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
        contentPreview.setWebViewClient(new UmWebContentEditorClient(this));
        contentPreview.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        progressDialog.setVisibility(View.VISIBLE);
        contentPreview.loadUrl(indexFile);
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

    }
}
