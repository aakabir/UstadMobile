package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.controller.ContentPreviewPresenter;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.umeditor.UmEditorViewPager;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

public class ContentEditorPreviewActivity extends UstadBaseActivity
        implements ContentPreviewView{

    private UmEditorViewPager mViewPager;

    private static List<EpubNavItem> pages;

    private static String selectedPage;

    public static void initPreview(List<EpubNavItem> pageList, String currentSelection){
        selectedPage = currentSelection;
        pages = pageList;
    }

    protected ProgressBar progressDialog;

    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        mViewPager = findViewById(R.id.previewPager);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        progressDialog = findViewById(R.id.progressBar);
        setToolbar(toolbar);

        toolbarTitle.setVisibility(View.VISIBLE);


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
    public void startPreviewing(String requestUri) {
        PagedPreviewAdapter adapter = new PagedPreviewAdapter(mViewPager,requestUri,
                getSupportFragmentManager());
        int pageIndex = getSelectedPageIndex();
        setToolbarTitle(pageIndex);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(pageIndex, true);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                setToolbarTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void setToolbarTitle(int position){
        toolbarTitle.setText(String.format("%d / %d - %s", position + 1,
                pages.size(), pages.get(position).getTitle()));
    }

    private int getSelectedPageIndex(){
        for(EpubNavItem item : pages){
            if(item.getHref().equals(selectedPage)){
                return pages.indexOf(item);
            }
        }
        return 0;
    }

    private class PagedPreviewAdapter extends FragmentPagerAdapter {

        private UmEditorViewPager umEditorViewPager;

        private String requestBaseUri;

        PagedPreviewAdapter(UmEditorViewPager umEditorViewPager, String requestBaseUri, FragmentManager fm) {
            super(fm);
            this.umEditorViewPager = umEditorViewPager;
            this.requestBaseUri = requestBaseUri;
        }

        @Override
        public Fragment getItem(int position) {

            ContentEditorPreviewFragment fragment =
                    ContentEditorPreviewFragment.newInstance(pages.get(position));
            fragment.setViewPager(umEditorViewPager);
            fragment.setRequestBaseUri(requestBaseUri);
            return fragment;
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }
}
