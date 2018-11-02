package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.contenteditor.ContentFormat;
import com.ustadmobile.port.android.contenteditor.UmAndroidUriUtil;
import com.ustadmobile.port.android.contenteditor.WebContentEditorClient;
import com.ustadmobile.port.android.contenteditor.WebContentEditorJsCallback;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.FileDirectoryHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ContentEditorActivity extends UstadBaseActivity implements
        ContentEditorView, FloatingActionMenu.OnMenuToggleListener {

    private static ContentEditorPresenter presenter;

    private BottomSheetBehavior formattingBottomSheetBehavior;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private FloatingActionMenu mInsertContent;

    private FloatingActionButton mPreviewContent;

    private FloatingActionButton mInsertMultipleChoice;

    private FloatingActionButton mInsertFillBlanks;

    private FloatingActionButton mInsertMultimedia;

    private WebView editorContent;

    private DrawerLayout mContentPageDrawer;

    private boolean isFromFormatting = false;

    private boolean isEditorInialized = false;

    private static final int FORMATTING_TEXT_INDEX = 0;

    private static final int FORMATTING_PARAGRAPH_INDEX = 1;

    private static final int FORMATTING_ACTIONS_INDEX = 2;

    private static SparseArray<List<ContentFormat>> formattingList = new SparseArray<>();

    private  String baseUrl = null;

    private Hashtable args = null;

    private ProgressDialog progressDialog;

    public static final int CAMERA_IMAGE_CAPTURE_REQUEST = 900;

    public static final int CAMERA_PERMISSION_REQUEST = 901;

    private static final int FILE_BROWSING_REQUEST = 902;

    private static final String TEM_EDITING_DIR = "contents";

    private static final String MEDIA_CONTENT_DIR = "media/";

    private static final String INDEX_FILE = "index.html";
    private static final String INDEX_TEMP_FILE = "_index.html";

    private String assetsDir = "assets-" +
            new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "";

    private File contentDir;



    /**
     * UI implementation of formatting type as pager.
     */
    private class ContentFormattingPagerAdapter extends FragmentStatePagerAdapter {

        String [] contentFormattingType = new String[]{
          getResources().getString(R.string.content_format_text),
          getResources().getString(R.string.content_format_paragraph)
        };

        ContentFormattingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new FormattingFragment().newInstance(position);
        }

        @Override
        public int getCount() {
            return contentFormattingType.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return contentFormattingType[position];
        }
    }


    /**
     * Web chrome client
     */
    private class WebChrome extends WebChromeClient{
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Consoled a message "+consoleMessage.message());
            if(consoleMessage.message().contains("action")){
                processJsCallLogValues(consoleMessage.message());
            }
            return true;
        }
    }


    /**
     * Fragment to handle formatting type (Text formatting & Paragraph formatting)
     */
    public static class FormattingFragment extends Fragment{

        private FormatsAdapter adapter;

        private int formattingType;

        class FormatsAdapter extends RecyclerView.Adapter<FormatsAdapter.FormatViewHolder>{

            private List<ContentFormat> contentFormats = new ArrayList<>();
            private int formatType;

            FormatsAdapter(int formatType){
                this.formatType = formatType;
            }

            void setContentFormats(List<ContentFormat> contentFormats) {
                this.contentFormats = contentFormats;
                notifyDataSetChanged();
            }

            @NonNull
            @Override
            public FormatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_content_formatting_type,parent,false);
                return new FormatViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull FormatViewHolder holder, int position) {
                ContentFormat format = contentFormats.get(position);
                ImageView mIcon = holder.itemView.findViewById(R.id.format_icon);
                RelativeLayout mLayout = holder.itemView.findViewById(R.id.format_holder);

                mIcon.setColorFilter(ContextCompat.getColor(getActivity(),
                        format.isActive() ? R.color.icons:R.color.text_secondary));
                mLayout.setBackgroundColor(ContextCompat.getColor(getActivity(),
                        format.isActive() ? R.color.content_icon_active:R.color.icons));
                mIcon.setImageResource(format.getFormatIcon());
                mLayout.setOnClickListener(v -> {
                    if(!format.getFormatTag().equals(TEXT_FORMAT_TYPE_FONT)){
                        format.setActive(!format.isActive());
                        formattingList.put(formatType, contentFormats);
                        presenter.handleFormatTypeClicked(format.getFormatTag(),null);
                        notifyDataSetChanged();
                    }else{
                        PopupMenu popupMenu = new PopupMenu(getActivity(), holder.itemView);
                        popupMenu.getMenuInflater()
                                .inflate(R.menu.menu_content_font_sizes, popupMenu.getMenu());
                        popupMenu.getMenu().getItem(0).setChecked(true);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            presenter.handleFormatTypeClicked(format.getFormatTag(),
                                    item.getTitle().toString().replace("pt",""));
                            return true;
                        });

                        popupMenu.show();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return contentFormats.size();
            }

            class FormatViewHolder extends RecyclerView.ViewHolder{
                FormatViewHolder(View itemView) {
                    super(itemView);
                }
            }

        }

        public FormattingFragment newInstance(int formatType) {
            this.formattingType = formatType;
            return this;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_content_formatting,
                    container, false);
            RecyclerView mRecyclerView = rootView.findViewById(R.id.formats_list);
            adapter = new FormatsAdapter(formattingType);
            adapter.setContentFormats(formattingList.get(formattingType));
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(100));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);
            return  rootView;

        }


        /**
         * Automatically gets number of rows to be displayed as per screen size
         * @param width Width of a single item
         * @return number of columns
         */
        public int getSpanCount(@NonNull Integer width){
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            float density = getActivity().getResources().getDisplayMetrics().density;
            float dpWidth = outMetrics.widthPixels / density;
            return Math.round(dpWidth/width);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_editor);
        formattingBottomSheetBehavior =
                BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_container));
        mediaSourceBottomSheetBehavior =
                BottomSheetBehavior.from(findViewById(R.id.bottom_multimedia_source_sheet_container));
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        mInsertContent = findViewById(R.id.content_editor_insert);
        mPreviewContent = findViewById(R.id.content_editor_preview);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        mInsertMultimedia = findViewById(R.id.content_type_multimedia);
        mInsertMultipleChoice = findViewById(R.id.content_type_multiple_choice);
        mInsertFillBlanks = findViewById(R.id.content_type_fill_blanks);
        mContentPageDrawer = findViewById(R.id.content_page_drawer);
        editorContent = findViewById(R.id.editor_content);
        RelativeLayout mFromCamera = findViewById(R.id.multimedia_from_camera);
        RelativeLayout mFromDevice = findViewById(R.id.multimedia_from_device);

        ViewPager mViewPager = findViewById(R.id.content_types_viewpager);
        TabLayout mTabLayout = findViewById(R.id.content_types_tabs);

        if(toolbar != null){
            toolbar.setTitle("");
            toolbar.setNavigationIcon(R.drawable.ic_check_white_24dp);
        }
        mPreviewContent.hide(true);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarTitle.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(UstadMobileSystemImpl
                .getInstance().getString(MessageID.content_prepare_file,this));
        contentDir = new File(Environment.getExternalStorageDirectory(),"contents");
        args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        args.put(ContentEditorView.CONTENT_ROOT_DIR,contentDir.getAbsolutePath());
        presenter = new ContentEditorPresenter(this,args,this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        mInsertContent.setOnMenuButtonClickListener(v -> {
            mPreviewContent.hide(true);
            handleFABMenuButton(!mInsertContent.isOpened());
            if(mPreviewContent.isHidden()){
                mInsertContent.open(true);
            }

            if(mInsertContent.isOpened()){
                mInsertContent.close(true);
            }
        });

        mFromDevice.setOnClickListener(v -> {
            mediaSourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startFileBrowser();
        });

        mPreviewContent.setOnClickListener(v ->
                UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                args,getApplicationContext()));

        mInsertContent.setOnMenuToggleListener(this);

        mediaSourceBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    mInsertContent.close(true);
                    mPreviewContent.hide(true);
                }

                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    mInsertContent.showMenu(true);
                    mPreviewContent.show(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mInsertMultimedia.setOnClickListener(v ->
                mediaSourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        mInsertMultipleChoice.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_MULTIPLE_CHOICE_QN,null));

        mInsertFillBlanks.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_FILLTHEBLANKS_QN,null));


        formattingBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    mInsertContent.setVisibility(View.GONE);
                    mPreviewContent.hide(true);
                }

                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    mInsertContent.setVisibility(View.VISIBLE);
                    mPreviewContent.show(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ContentFormattingPagerAdapter adapter =
                new ContentFormattingPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

        WebSettings webSettings = editorContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        editorContent.setWebChromeClient(new WebChrome());
        editorContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        editorContent.clearCache(true);
        editorContent.clearHistory();
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareFormattingTypeLists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_editor_questions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.content_action_direction)
                .setIcon(formattingList.get(FORMATTING_ACTIONS_INDEX).get(0).getFormatIcon());
        menu.findItem(R.id.content_action_editor).setVisible(!isEditorInialized);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.content_action_pages) {
            mContentPageDrawer.openDrawer(GravityCompat.END);

        }else if(itemId == android.R.id.home){
            handleExpandableViews();

        }else if(itemId == R.id.content_action_format){
            if(mInsertContent.isOpened()){
                isFromFormatting = true;
                mInsertContent.close(true);
            }else{
                boolean isSheetOpened = formattingBottomSheetBehavior.getState()
                        == BottomSheetBehavior.STATE_EXPANDED;
                mediaSourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                formattingBottomSheetBehavior.setState(isSheetOpened ?
                        BottomSheetBehavior.STATE_COLLAPSED:BottomSheetBehavior.STATE_EXPANDED);
            }

        }else if(itemId == R.id.content_action_undo){
            presenter.handleFormatTypeClicked(ACTION_UNDO,null);

        }else if(itemId == R.id.content_action_redo){
            presenter.handleFormatTypeClicked(ACTION_REDO,null);

        }else if(itemId == R.id.content_action_direction){
            View menuItemView = findViewById(R.id.content_action_direction);
            PopupMenu popupMenu = new PopupMenu(this, menuItemView);
            popupMenu.inflate(R.menu.menu_content_text_direction);
            popupMenu.getMenu().getItem(0).setChecked(true);
            popupMenu.setOnMenuItemClickListener(popupItem -> {
                List<ContentFormat> format = formattingList.get(FORMATTING_ACTIONS_INDEX);
                format.get(0).setActive(true);
                format.get(0).setFormatIcon(popupItem.getItemId() == R.id.direction_leftToRight ?
                        R.drawable.ic_format_textdirection_l_to_r_white_24dp:
                        R.drawable.ic_format_textdirection_r_to_l_white_24dp);
                formattingList.put(FORMATTING_ACTIONS_INDEX,format);
                presenter.handleFormatTypeClicked(ACTION_TEXT_DIRECTION,
                        popupItem.getItemId() == R.id.direction_leftToRight ? "false":"true");
                return true;
            });
             @SuppressLint("RestrictedApi")
             MenuPopupHelper menuHelper =
                    new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), menuItemView);
            menuHelper.setForceShowIcon(true);
            menuHelper.setGravity(Gravity.END);
            menuHelper.show();
        }else if(itemId == R.id.content_action_editor){
            progressDialog.show();
            callJavaScriptFunction("ustadEditor.initTinyMceEditor", (String) null);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case FILE_BROWSING_REQUEST:
                    try{
                        progressDialog.show();
                        Uri uri = data.getData();
                        String mimeType = UmAndroidUriUtil.getMimeType(this,uri);
                        String filePath =
                                Objects.requireNonNull(UmAndroidUriUtil.getPath(this, uri));
                        File compressedFile = new File(filePath);
                        if(mimeType.contains("image")){
                            compressedFile = new Compressor(this)
                                    .setQuality(75)
                                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                    .compressToFile(new File(filePath));
                        }

                        File destination = new File(contentDir, MEDIA_CONTENT_DIR +compressedFile.
                                        getName().replaceAll("\\s+","_"));
                        UMFileUtil.copyFile(compressedFile,destination);
                        String source = MEDIA_CONTENT_DIR +destination.getName();
                        progressDialog.dismiss();
                        callJavaScriptFunction("ustadEditor.insertMedia",
                                source,mimeType);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        handleExpandableViews();
    }

    @Override
    public void onMenuToggle(boolean opened) {
        if(opened){
            mPreviewContent.hide(true);
        }else{
            handleFABMenuButton(false);
            if(mediaSourceBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                mPreviewContent.show(true);
            }
            if(isFromFormatting){
                isFromFormatting = false;
                formattingBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    /**
     * Collapse all expandable views before moving out of the activity (User experience)
     */
    private void handleExpandableViews(){
        boolean isSourceExpanded = mediaSourceBottomSheetBehavior.getState() ==
                BottomSheetBehavior.STATE_EXPANDED;

        boolean isFormattingExpanded = formattingBottomSheetBehavior.getState() ==
                BottomSheetBehavior.STATE_EXPANDED;

        boolean isDrawerOpen = mContentPageDrawer.isDrawerOpen(GravityCompat.END);

        boolean isFabMenuOpen = mInsertContent.isOpened();

        if(isDrawerOpen){
            mContentPageDrawer.closeDrawer(GravityCompat.END);
        }

        if(isSourceExpanded){
            mediaSourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if(isFormattingExpanded){
            formattingBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if(isFabMenuOpen){
            mInsertContent.close(true);
        }

        if(!isSourceExpanded && !isFormattingExpanded && !isFabMenuOpen && !isDrawerOpen){
            finish();
        }
    }

    /**
     * Show and hide FAB Menu according to the state of the editor
     * @param show Show when TRUE otherwise hide menus
     */
    private void handleFABMenuButton(boolean show){
        mInsertMultimedia.setVisibility(show ? View.VISIBLE:View.GONE);
        mInsertMultipleChoice.setVisibility(show ? View.VISIBLE:View.GONE);
        mInsertFillBlanks.setVisibility(show ? View.VISIBLE:View.GONE);
    }

    /**
     * Start local web server which serves purpose of editing contents
     */
    @Override
    public void startWebServer(){

        EmbeddedHTTPD embeddedHTTPD = new EmbeddedHTTPD(0, this);
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  AndroidAssetsHandler.class, this);
        embeddedHTTPD.addRoute( TEM_EDITING_DIR +"(.)+",  FileDirectoryHandler.class, contentDir);
        try {
            embeddedHTTPD.start();
            if(embeddedHTTPD.isAlive()){
                baseUrl =  "http://127.0.0.1:"+embeddedHTTPD.getListeningPort()+"/";
                args.put(ContentPreviewView.PREVIEW_URL,baseUrl+ TEM_EDITING_DIR);
                loadIndexFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    public BottomSheetBehavior getFormattingBottomSheetBehavior() {
        return formattingBottomSheetBehavior;
    }

    @VisibleForTesting
    public static SparseArray<List<ContentFormat>> getFormatting(){
        return formattingList;
    }

    @VisibleForTesting
    public BottomSheetBehavior getMediaSourceBottomSheetBehavior() {
        return mediaSourceBottomSheetBehavior;
    }

    @Override
    public void setContentBold() {
        callJavaScriptFunction("ustadEditor.textFormattingBold",
                (String[]) null);
    }

    @Override
    public void setContentItalic() {
        callJavaScriptFunction("ustadEditor.textFormattingItalic",
                (String) null);
    }

    @Override
    public void setContentUnderlined() {
        callJavaScriptFunction("ustadEditor.textFormattingUnderline",
                (String) null);
    }

    @Override
    public void setContentStrikeThrough() {
        callJavaScriptFunction("ustadEditor.textFormattingStrikeThrough",
                (String) null);
    }

    @Override
    public void setContentFontSize(String fontSize) {
        callJavaScriptFunction("ustadEditor.setFontSize", fontSize);
    }

    @Override
    public void setContentSuperscript() {
        callJavaScriptFunction("ustadEditor.textFormattingSuperScript",
                (String) null);
    }

    @Override
    public void setContentSubScript() {
        callJavaScriptFunction("ustadEditor.textFormattingSubScript",
                (String) null);
    }

    @Override
    public void setContentJustified() {
        callJavaScriptFunction("ustadEditor.paragraphFullJustification",
                (String) null);
    }

    @Override
    public void setContentCenterAlign() {
        callJavaScriptFunction("ustadEditor.paragraphCenterJustification",
                (String) null);
    }

    @Override
    public void setContentLeftAlign() {
        callJavaScriptFunction("ustadEditor.paragraphLeftJustification",
                (String) null);
    }

    @Override
    public void setContentRightAlign() {
        callJavaScriptFunction("ustadEditor.paragraphRightJustification",
                (String) null);
    }

    @Override
    public void setContentOrderedList() {
        callJavaScriptFunction("ustadEditor.paragraphOrderedListFormatting",
                (String) null);
    }

    @Override
    public void setContentUnOrderList() {
        callJavaScriptFunction("ustadEditor.paragraphUnOrderedListFormatting",
                (String) null);
    }

    @Override
    public void setContentIncreaseIndent() {
        callJavaScriptFunction("ustadEditor.paragraphIndent", (String) null);
    }

    @Override
    public void setContentDecreaseIndent() {
        callJavaScriptFunction("ustadEditor.paragraphOutDent", (String) null);
    }

    @Override
    public void setContentRedo() {
        callJavaScriptFunction("ustadEditor.editorActionRedo", (String) null);
    }

    @Override
    public void setContentUndo() {
        callJavaScriptFunction("ustadEditor.editorActionUndo", (String) null);
    }

    @Override
    public void setContentTextDirection(boolean right) {
        callJavaScriptFunction(right ? "ustadEditor.textDirectionRightToLeft":
                "ustadEditor.textDirectionLeftToRight", (String) null);
        invalidateOptionsMenu();
    }

    @Override
    public void insertMultipleChoiceQuestion() {
        callJavaScriptFunction("ustadEditor.insertMultipleChoiceQuestionTemplate",
                (String) null);
    }

    @Override
    public void insertFillTheBlanksQuestion() {
        callJavaScriptFunction("ustadEditor.insertFillInTheBlanksQuestionTemplate",
                (String) null);
    }

    @Override
    public void requestEditorContent() {
        callJavaScriptFunction("ustadEditor.getContent", (String) null);
    }


    @Override
    public void handleContentMenu() {
        if(mInsertContent.isOpened()){
            mInsertContent.close(true);
        }
    }

    /*@Override
    public void handleEditorResources(HashMap<String, File> directories) {
        new Thread(() -> {
            ContentEditorResourceHandler resourceHandler =
                    new ContentEditorResourceHandler(directories,baseUrl);
            resourceHandler.with(
                    () -> UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "All resources has been copied to the external dirs"))
                    .startCopying();
        }).start();
    }*/


    /**
     * Search for all scripts necessary for file editing, if not present then append them
     * to the temp file then direct them to asset handler to be resolved as files.
     */
    private void loadIndexFile() {

        File source = new File(contentDir,INDEX_FILE);
        File destination = new File(contentDir,INDEX_TEMP_FILE);
        try {
            UMFileUtil.copyFile(source,destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document htmlDoc = getIndexDocument(INDEX_TEMP_FILE);
        Element docHead = htmlDoc.select("head").first();
        Element docBody = htmlDoc.select("body").first();

        if(docHead != null){
            Elements headResources = docHead.children();

            for(Element resource: headResources){

                String resourceLink = null;
                if(resource.tagName().equals("link")){
                    resourceLink = resource.attr("href");
                }

                if(resource.tagName().equals("script")){
                    resourceLink = resource.attr("src");
                }
                if(resourceLink != null && presenter.isUstadResource(resourceLink)){
                    resource.remove();
                }
            }
        }

        if(docBody != null){
            Elements headResources = docBody.children();

            for(Element resource: headResources){
                String resourceLink = null;
                if(resource.tagName().equals("script")){
                    resourceLink = resource.attr("src");
                }
                if(resourceLink !=null && presenter.isUstadResource(resourceLink)){
                    resource.remove();
                }
            }
        }

        for (String ref : CONTENT_EDITOR_HEAD_RESOURCES) {
            if(!docHead.html().contains(ref)){
                String resource;
                Element script = Jsoup.parse(ref).select("script[src]").first();
                if(ref.contains(CONTENT_JS_USTAD_WIDGET)){
                    script.attr("src",baseUrl+assetsDir+"/tinymce/"+script.attr("src"));
                    resource = script.toString();
                }else{
                    resource = ref;
                }
                docHead.append(resource);
            }
        }

        for (String ref : CONTENT_EDITOR_BODY_RESOURCES) {
            if(!docBody.html().contains(ref)){
                docBody.append(ref);
            }
        }

        UMFileUtil.writeToFile(destination,htmlDoc.html());

        /*Load temp index file*/
        if(baseUrl != null){
            editorContent.setWebViewClient(
                    new WebContentEditorClient(this,baseUrl+TEM_EDITING_DIR));
            editorContent.loadUrl(baseUrl+ TEM_EDITING_DIR +"/"+INDEX_TEMP_FILE);
        }
    }

    private Document getIndexDocument(String fileIndex){
        try {
            File indexFile = new File(contentDir,fileIndex);
            return  Jsoup.parse(UMFileUtil.readTextFile(indexFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
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
        editorContent.evaluateJavascript(call, this::processJsCallLogValues);
    }



    /**
     * Process values returned from JS calls
     * @param value value returned
     */
    private void processJsCallLogValues(String value){
        if(value.contains("action")){
            WebContentEditorJsCallback callback = new Gson().fromJson(value,WebContentEditorJsCallback.class);
            String content = Base64Coder.decodeString(callback.getContent());
            switch (callback.getAction()){
                case ACTION_INIT_EDITOR:
                    isEditorInialized = Boolean.parseBoolean(callback.getContent());
                    progressDialog.dismiss();
                    invalidateOptionsMenu();
                    break;

                case ACTION_CONTENT_CHANGED:
                    if(content.length() > 0){
                        mPreviewContent.show(true);
                    }else{
                        mPreviewContent.hide(true);
                    }
                    callJavaScriptFunction("ustadEditor.loadContentForPreview",
                            callback.getContent());
                    break;

                case ACTION_SAVE_CONTENT:
                    Document index = getIndexDocument(INDEX_FILE);
                    Elements docContainer = index.select(".container-fluid");
                    if(docContainer.size() > 0){
                        docContainer.first().html(content);
                    }else{
                        String wrapped = "<div class=\"container-fluid\">"+content+"</div>";
                        Element bodyElement = index.select("body").first();
                        bodyElement.html(wrapped);
                    }
                    UMFileUtil.writeToFile(new File(contentDir,"index.html"),index.html());
                    break;

            }
        }

    }


    /**
     * Prepare lis of all formatting types
     */
    private void prepareFormattingTypeLists(){
        List<ContentFormat> mText = new ArrayList<>();
        List<ContentFormat> mDirection = new ArrayList<>();
        List<ContentFormat> mParagraph = new ArrayList<>();
        mText.add(new ContentFormat(R.drawable.ic_format_bold_black_24dp,
                TEXT_FORMAT_TYPE_BOLD,false));
        mText.add(new ContentFormat(R.drawable.ic_format_italic_black_24dp,
                TEXT_FORMAT_TYPE_ITALIC,false));
        mText.add(new ContentFormat(R.drawable.ic_format_strikethrough_black_24dp,
                TEXT_FORMAT_TYPE_STRIKE,false));
        mText.add(new ContentFormat(R.drawable.ic_format_underlined_black_24dp,
                TEXT_FORMAT_TYPE_UNDERLINE,false));
        mText.add(new ContentFormat(R.drawable.ic_format_size_black_24dp,
                TEXT_FORMAT_TYPE_FONT,false));
        mText.add(new ContentFormat(R.drawable.ic_number_superscript,
                TEXT_FORMAT_TYPE_SUP,false));
        mText.add(new ContentFormat(R.drawable.ic_number_subscript,
                TEXT_FORMAT_TYPE_SUB,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_justify_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_JUSTIFY, false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_right_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_RIGHT,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_center_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_CENTER,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_left_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_LEFT,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_list_numbered_black_24dp,
                PARAGRAPH_FORMAT_LIST_ORDERED,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_list_bulleted_black_24dp,
                PARAGRAPH_FORMAT_LIST_UNORDERED,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_increase_black_24dp,
                PARAGRAPH_FORMAT_INDENT_INCREASE,false));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_decrease_black_24dp,
                PARAGRAPH_FORMAT_INDENT_DECREASE,false));
        mDirection.add(new ContentFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                ACTION_TEXT_DIRECTION,false));

        formattingList.put(FORMATTING_TEXT_INDEX,mText);
        formattingList.put(FORMATTING_PARAGRAPH_INDEX,mParagraph);
        formattingList.put(FORMATTING_ACTIONS_INDEX,mDirection);
    }


    /**
     * Open device default file explorer for user to pick file
     */
    private void startFileBrowser(){
        String[] mimeTypes = {"image/*","video/*","audio/*"};
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        } else {
            StringBuilder mimeTypesStr = new StringBuilder();
            for (String mimeType : mimeTypes) {
                mimeTypesStr.append(mimeType).append("|");
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,
                UstadMobileSystemImpl.getInstance().getString(
                        MessageID.content_choose_file,this)), FILE_BROWSING_REQUEST);
    }
}
