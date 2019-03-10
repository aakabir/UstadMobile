package com.ustadmobile.port.android.view;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.EpubContentPresenter;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.port.android.netwokmanager.EmbeddedHttpdService;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

public class EpubContentActivity extends ZippedContentActivity implements
        ContainerPageFragment.OnFragmentInteractionListener,
        EpubContentView, AppViewChoiceListener, ListView.OnItemClickListener,
        TocListView.OnItemClickListener{


    /** The ViewPager used to swipe between epub pages */
    private ViewPager mPager;

    /** The Page Adapter used to manage swiping between epub pages */
    private ContainerViewPagerAdapter mPagerAdapter;

    private String onpageSelectedJS = "";


    private EpubContentPresenter mEpubContentPresenter;

    private String mBaseURL = null;

    private String mMountedPath;

    //Key when saving state for the current page
    private static final String OUTSTATE_CURRENTITEM = "currentitem";

    //Key when saving state for the current mount point
    private static final String OUTSTATE_MOUNTPOINT = "mountpt";

    private int mSavedPosition = -1;

    private Hashtable mArgs;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private EpubNavDocument navDocument;

    private Vector<Runnable> runWhenContentMounted = new Vector<>();

    private TocListView tocList;

    private String[] spineUrls;

    private ImageView coverImageView;


    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        setContentView(R.layout.activity_container_epubpager);
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.container_drawer_layout);


        InputStream is = null;
        try {
            AssetManager asMgr = getApplicationContext().getAssets();
            is = asMgr.open("http/onpageshow.js");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(is, bout, 1024);
            onpageSelectedJS = "javascript:" + new String(bout.toByteArray(), "UTF-8");
        }catch(IOException e) {
            System.err.println("Error loading javascript for page changing");
            e.printStackTrace();
        }finally {
            UMIOUtils.closeInputStream(is);
        }

        mArgs = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        if(saved != null) {
            if(saved.getInt(OUTSTATE_CURRENTITEM, -1) != -1) {
                mSavedPosition = saved.getInt(OUTSTATE_CURRENTITEM);
            }
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.container_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, R.string.open, R.string.closed) {

            public void onDrawerOpened(View drawerView) {
            }

            public void onDrawerClosed(View drawerView) {
            }
        };


        if(!UstadMobileSystemImpl.getInstance().getAppConfigBoolean(AppConfig.KEY_EPUB_TOC_ENABLED,
                getContext())) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mPager = (ViewPager) findViewById(R.id.container_epubrunner_pager);
        tocList = (TocListView)findViewById(R.id.activity_container_epubpager_toclist);
        coverImageView = (ImageView)findViewById(R.id.item_basepoint_cover_img);

        onpageSelectedJS = onpageSelectedJS.replace("__ASSETSURL__",
                EmbeddedHttpdService.ANDROID_ASSETS_PATH);
        mEpubContentPresenter = new EpubContentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        Hashtable savedHt = UMAndroidUtil.bundleToHashtable(saved);
        mEpubContentPresenter.onCreate(savedHt);
    }

    public String getBaseURL() {
        return mBaseURL;
    }

    public String getXapiQuery() {
        return mEpubContentPresenter.getXAPIQuery();
    }

    /**
     * A runnable posted here will be run when the controller is ready. If the controller is currently
     * ready the method will be run immediately. Otherwise it will be added to a vector of Runnables
     * to run when the controller is ready.
     *
     * @param runnable
     */
    public void runWhenMounted(Runnable runnable) {
        if(mEpubContentPresenter != null) {
            runnable.run();
        }else {
            runWhenContentMounted.add(runnable);
        }
    }

    /**
     * The user was asked to choose from a list of available registrations: handle choice
     *
     * @param commandId The command id that was supplied when using showChoiceDialog
     * @param choice
     */
    @Override
    public void appViewChoiceSelected(int commandId, int choice) {

    }

    /**
     * Override the onCreateOptionsMenu : In Container mode we don't show the standard app menu
     * options like logout, about etc.  We show only a close button in the top right to make things
     * simple
     *
     * @param menu
     *
     * @return true as we will have added items
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_container, menu);
        return true;
    }


    /**
     * Handle when the user has tapped an item from the table of contents on the drawer
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void setSpineUrls(String basePath, String[] spineUrls, String query) {
        this.mBaseURL = basePath;
        this.spineUrls = spineUrls;
        mPagerAdapter = new ContainerViewPagerAdapter(getSupportFragmentManager(),
                basePath, spineUrls, query);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void setPageTitle(String pageTitle) {
        setTitle(pageTitle);
    }

    public String getAutoplayRunJavascript() {
        return onpageSelectedJS;
    }

    public void handlePageTitleUpdated(int index, String title) {
        if(mPager != null && mPager.getCurrentItem() == index && mEpubContentPresenter != null) {
            mEpubContentPresenter.handlePageTitleUpdated(title);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPager != null) {
            outState.putInt(OUTSTATE_CURRENTITEM, mPager.getCurrentItem());
        }

        if(mMountedPath != null) {
            outState.putString(OUTSTATE_MOUNTPOINT, mMountedPath);
        }
    }

    public void onDestroy() {
        mSavedPosition = -1;
        mEpubContentPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_leavecontainer) {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setController(EpubContentPresenter controller) {

    }

    @Override
    public void setContainerTitle(String title) {
        ((TextView)findViewById(R.id.item_basepoint_cover_title)).setText(title);
    }

    @Override
    public void setCoverImage(String imageUrl) {
        Picasso.with(this).load(imageUrl).into((ImageView)findViewById(R.id.item_basepoint_cover_img));
    }

    @Override
    public Object getContext() {
        return this;
    }

    @Override
    public void setTableOfContents(EpubNavItem tocNavItem) {
        tocList.setAdapter(new ContainerTocListAdapter(tocNavItem));
        tocList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(Object item, View view) {
        EpubNavItem navItem = (EpubNavItem)item;
        int hrefIndex = Arrays.asList(spineUrls).indexOf(navItem.getHref());
        if(hrefIndex != -1) {
            mPager.setCurrentItem(hrefIndex, true);
            mDrawerLayout.closeDrawers();
        }
    }

    private class ContainerTocListAdapter extends TocListView.TocListViewAdapter{

        private EpubNavItem rootItem;

        private ContainerTocListAdapter(EpubNavItem rootItem) {
            this.rootItem = rootItem;
        }

        @Override
        public Object getRoot() {
            return rootItem;
        }

        @Override
        public List getChildren(Object node) {
            return ((EpubNavItem)node).getChildren();
        }

        @Override
        public int getNumChildren(Object node) {
            return ((EpubNavItem)node).size();
        }

        @Override
        public View getNodeView(Object node, View recycleView, int depth) {
            if(recycleView == null) {
                LayoutInflater inflater = LayoutInflater.from(EpubContentActivity.this);
                recycleView = inflater.inflate(R.layout.item_epubview_child, null);
            }

            TextView expandedTextView = recycleView.findViewById(R.id.expandedListItem);
            expandedTextView.setText(node.toString());

            return recycleView;
        }
    }



    /**
     * A simple pager adapter that uses an array of urls (as a string
     * array) to generate a fragment that has a webview showing that
     * URL
     *
     */
    private static class ContainerViewPagerAdapter extends FragmentStatePagerAdapter {


        WeakHashMap<Integer, ContainerPageFragment> pagesMap;

        /**
         * Array of the page HREF items to be shown
         */
        private String[] hrefList;

        /**
         * Base URL of pages (directory name)
         */
        private String baseURI;

        /**
         * Query string to append to the end of each page
         */
        private String query;

        public ContainerViewPagerAdapter(FragmentManager fm, String baseURI, String[] hrefList, String query) {
            super(fm);
            this.baseURI = baseURI;
            this.hrefList = hrefList;
            this.query = query;
            this.pagesMap = new WeakHashMap<>();
        }

        @Override
        /**
         * Generate the Fragment for that position
         *
         * @see com.ustadmobile.contentviewpager.ContentViewPagerPageFragment
         *
         * @param position Position in the list of fragment to create
         */
        public Fragment getItem(int position) {
            ContainerPageFragment existingFrag = pagesMap.get(position);

            //something wrong HERE
            if(existingFrag != null) {
                return existingFrag;
            }else {
                ContainerPageFragment frag =
                        ContainerPageFragment.newInstance(hrefList[position], position);

                this.pagesMap.put(position, frag);
                return frag;
            }
        }

        public int getFragmentIndexByHREF(String href) {
            return Arrays.asList(hrefList).indexOf(href);
        }


        public void updatePageProps(String baseURI, String[] hrefList, String query, boolean reload) {
            this.baseURI = baseURI;
            this.hrefList = hrefList;
            this.query = query;

            Iterator<Map.Entry<Integer, ContainerPageFragment>> iterator = pagesMap.entrySet().iterator();
            ContainerPageFragment frag;
            Map.Entry<Integer, ContainerPageFragment> entry;
            while(iterator.hasNext()) {
                entry = iterator.next();
                frag = entry.getValue();
                frag.setBaseURI(baseURI, false);
                frag.setPageHref(hrefList[entry.getKey()], false);
                frag.setQuery(query, reload);
            }
        }

        @Override
        public int getCount() {
            return hrefList.length;
        }
    }


    public String getBaseUrl() {
        return mBaseURL;
    }

    @Override
    public void setAuthorName(String authorName) {
        ((TextView)findViewById(R.id.activity_container_epubpager_auuthor_text))
                .setText(authorName);
    }
}