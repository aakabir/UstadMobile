package com.ustadmobile.port.android.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorPreviewFragmentView;
import com.ustadmobile.port.android.umeditor.UmEditorViewPager;
import com.ustadmobile.port.android.umeditor.UmEditorWebView;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorChromeClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorInterface;
import com.ustadmobile.port.android.umeditor.UmWebJsResponse;

import static com.ustadmobile.core.view.ContentEditorView.ACTION_PAGE_LOADED;
import static com.ustadmobile.port.android.umeditor.UmEditorUtil.getCurrentLocale;
import static com.ustadmobile.port.android.umeditor.UmEditorUtil.getDirectionality;
import static com.ustadmobile.port.android.umeditor.UmWebContentEditorClient.executeJsFunction;
import static com.ustadmobile.port.android.view.ContentEditorActivity.EDITOR_METHOD_PREFIX;

/**
 * Fragment which displays individual page previews using webview
 *
 * @see UmEditorWebView
 *
 * @author kileha3
 */
public class ContentEditorPreviewFragment extends UstadBaseFragment
        implements ContentEditorPreviewFragmentView,
        UmWebContentEditorChromeClient.JsLoadingCallback {


    public ContentEditorPreviewFragment() {
        // Required empty public constructor
    }

    private UmEditorWebView mWebView;

    private UmEditorViewPager mViewPager;

    private String requestBaseUri;

    private ContentEditorPreviewActivity previewActivity;

    public static ContentEditorPreviewFragment newInstance(EpubNavItem pageItem){
        ContentEditorPreviewFragment fragment = new ContentEditorPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PAGE_ITEM,pageItem);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ContentEditorPreviewActivity){
            previewActivity = (ContentEditorPreviewActivity) context;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_content_editor_preview, container,
                false);
        mWebView = rootView.findViewById(R.id.preview_content);

        assert getArguments() != null;
        EpubNavItem pageItem = (EpubNavItem) getArguments().getSerializable(PAGE_ITEM);

        mWebView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        mWebView.addJavascriptInterface(
                new UmWebContentEditorInterface(getActivity(),this),"UmEditor");
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.setFragment(this);

        previewActivity.progressDialog.setMax(100);
        previewActivity.progressDialog.setProgress(0);


        mWebView.setWebViewClient(new UmWebContentEditorClient(getContext(), true));
        mWebView.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        previewActivity.progressDialog.setVisibility(View.VISIBLE);
        assert pageItem != null;
        mWebView.loadUrl(UMFileUtil.joinPaths(requestBaseUri, pageItem.getHref()));

        return rootView;
    }


    @Override
    public void onProgressChanged(int newProgress) {
        previewActivity.progressDialog.setProgress(newProgress);
    }

    @Override
    public void onPageFinishedLoading() {
        previewActivity.progressDialog.setVisibility(View.GONE);
    }

    @Override
    public void onCallbackReceived(String value) {
        if(value.contains("action")){
            UmWebJsResponse callback = new Gson().fromJson(value,UmWebJsResponse.class);
            if(callback.getAction().equals(ACTION_PAGE_LOADED)){
                executeJsFunction(mWebView,
                        EDITOR_METHOD_PREFIX + "onCreate", this,
                        getCurrentLocale(previewActivity), getDirectionality(previewActivity));
            }
        }
    }


    @Override
    public void setViewPager(Object object) {
        mViewPager = (UmEditorViewPager) object;
    }

    @Override
    public void setPagedEnabled(boolean enabled) {
        mViewPager.setPagingEnabled(enabled);
    }

    @Override
    public void setRequestBaseUri(String requestBaseUri) {
        this.requestBaseUri = requestBaseUri;
    }
}
