package com.ustadmobile.port.android.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.contenteditor.ContentEditorResourceHandler;
import com.ustadmobile.port.android.contenteditor.ContentFormat;
import com.ustadmobile.port.android.contenteditor.ContentFormattingHelper;
import com.ustadmobile.port.android.contenteditor.UmAndroidUriUtil;
import com.ustadmobile.port.android.contenteditor.WebContentEditorChrome;
import com.ustadmobile.port.android.contenteditor.WebContentEditorClient;
import com.ustadmobile.port.android.contenteditor.WebJsResponse;
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

import static com.ustadmobile.core.controller.CatalogPresenter.SHARED_RESOURCE;
import static com.ustadmobile.port.android.contenteditor.ContentFormattingHelper.FORMATTING_ACTIONS_INDEX;
import static com.ustadmobile.port.android.contenteditor.WebContentEditorClient.executeJsFunction;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView,
        FloatingActionMenu.OnMenuToggleListener, WebContentEditorChrome.JsLoadingCallback {

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

    private boolean isEditorInitialized = false;

    private boolean openFormattingBottomSheet = false;

    private boolean isDocumentEmpty = true;

    private boolean isSoftKeyboardActive = false;

    android.support.design.widget.FloatingActionButton startEditing;

    private  String baseUrl = null;

    private Hashtable args = null;

    private Uri cameraMedia;

    private File fileFromCamera;

    private ProgressBar progressDialog;

    public static final int CAMERA_IMAGE_CAPTURE_REQUEST = 900;

    public static final int CAMERA_PERMISSION_REQUEST = 901;

    private static final int FILE_BROWSING_REQUEST = 902;

    private static final String EDITOR_ROOT_DIR = "contentEditor";

    private static final String MEDIA_CONTENT_DIR = "media/";

    private View blankDocumentContainer;

    private  String index_file;

    private  String index_temp_file;

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
     * Fragment to handle formatting type (Text formatting & Paragraph formatting)
     */
    public static class FormattingFragment extends Fragment{

        private FormatsAdapter adapter;

        private int formattingType;

        class FormatsAdapter extends RecyclerView.Adapter<FormatsAdapter.FormatViewHolder>{

            private List<ContentFormat> contentFormats = new ArrayList<>();

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
                    if(!format.getFormatCommand().equals(TEXT_FORMAT_TYPE_FONT)){
                        format.setActive(!format.isActive());
                        ContentFormattingHelper.getInstance().updateFormat(format);
                        presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
                        notifyDataSetChanged();
                    }else{
                        PopupMenu popupMenu = new PopupMenu(getActivity(), holder.itemView);
                        popupMenu.getMenuInflater()
                                .inflate(R.menu.menu_content_font_sizes, popupMenu.getMenu());
                        popupMenu.getMenu().getItem(0).setChecked(true);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            presenter.handleFormatTypeClicked(format.getFormatCommand(),
                                    item.getTitle().toString().replace("pt",""));
                            return true;
                        });

                        popupMenu.show();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return contentFormats != null ? contentFormats.size():0;
            }

            class FormatViewHolder extends RecyclerView.ViewHolder{
                FormatViewHolder(View itemView) {
                    super(itemView);
                }
            }

        }

        private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null){
                    adapter.notifyDataSetChanged();
                }
            }
        };

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
            adapter = new FormatsAdapter();
            adapter.setContentFormats(
                    ContentFormattingHelper.getInstance().getFormatListByType(formattingType));
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(100));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_FORMAT_STATUS_CHANGED);
            LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity()))
                    .registerReceiver(broadcastReceiver,intentFilter);
            return  rootView;

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity()))
                    .unregisterReceiver(broadcastReceiver);
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
        mInsertContent = findViewById(R.id.content_editor_insert);
        mPreviewContent = findViewById(R.id.content_editor_preview);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        mInsertMultimedia = findViewById(R.id.content_type_multimedia);
        mInsertMultipleChoice = findViewById(R.id.content_type_multiple_choice);
        mInsertFillBlanks = findViewById(R.id.content_type_fill_blanks);
        mContentPageDrawer = findViewById(R.id.content_page_drawer);
        editorContent = findViewById(R.id.editor_content);
        progressDialog = findViewById(R.id.progressBar);
        startEditing = findViewById(R.id.btn_start_editing);
        ImageView actionClose = findViewById(R.id.action_close_tab);
        RelativeLayout mFromCamera = findViewById(R.id.multimedia_from_camera);
        RelativeLayout mFromDevice = findViewById(R.id.multimedia_from_device);
        View rootView = findViewById(R.id.coordinationLayout);
        TextView blankDocTitle = findViewById(R.id.blank_doc_title);
        TextView bdClickLabel = findViewById(R.id.click_label);
        TextView bdCreatLabel = findViewById(R.id.editing_label);
        blankDocumentContainer = findViewById(R.id.new_doc_container);

        ViewPager mViewPager = findViewById(R.id.content_types_viewpager);
        TabLayout mTabLayout = findViewById(R.id.content_types_tabs);

        if(toolbar != null){
            toolbar.setTitle("");
            toolbar.setNavigationIcon(R.drawable.ic_check_white_24dp);
        }

        handleKeyboardVisibilityChange(rootView);
        handleClipBoardContentChanges();
        mPreviewContent.hide(false);
        mInsertContent.hideMenuButton(false);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(toolbar != null){
            toolbar.setTitle("");
        }
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        UMStorageDir [] rootDir = UstadMobileSystemImpl.getInstance().getStorageDirs(SHARED_RESOURCE,this);
        contentDir = new File(new File(rootDir[0].getDirURI()), EDITOR_ROOT_DIR);
        if(!contentDir.exists()) contentDir.mkdir();
        args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        index_file = args.get(EDITOR_CONTENT_FILE).toString();
        index_temp_file = "_"+index_file;
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

        actionClose.setOnClickListener(v -> setFormattingBottomSheetBehavior(false));

        mFromDevice.setOnClickListener(v -> {
            setMediaSourceBottomSheetBehavior(false);
            startFileBrowser();
        });

        startEditing.setOnClickListener(v -> {
            progressDialog.setVisibility(View.VISIBLE);
            executeJsFunction(editorContent, "ustadEditor.initTinyMceEditor",
                    ContentEditorActivity.this, (String[]) null);
        });

        mFromCamera.setOnClickListener(v -> {
            setMediaSourceBottomSheetBehavior(false);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContentEditorActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                return;
            }
            showMediaTypeDialog();
        });


        mPreviewContent.setOnClickListener(v ->
                UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                args,getApplicationContext()));

        mInsertContent.setOnMenuToggleListener(this);


        mInsertMultimedia.setOnClickListener(v ->
                setMediaSourceBottomSheetBehavior(true));

        mInsertMultipleChoice.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_MULTIPLE_CHOICE_QN,null));

        mInsertFillBlanks.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_FILL_THE_BLANKS_QN,null));

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        blankDocTitle.setText(impl.getString(MessageID.content_blank_doc_title,this));
        bdClickLabel.setText(impl.getString(MessageID.content_blank_doc_click_label,this));
        bdCreatLabel.setText(impl.getString(MessageID.content_blank_doc_start_label,this));



        formattingBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    mInsertContent.setVisibility(View.GONE);
                    mPreviewContent.setVisibility(View.GONE);
                    mPreviewContent.hide(true);
                    if(openFormattingBottomSheet){
                        openFormattingBottomSheet = false;
                        handleSoftKeyboard(false);
                        setMediaSourceBottomSheetBehavior(false);
                    }
                }

                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    mInsertContent.setVisibility(View.VISIBLE);
                    mPreviewContent.setVisibility(View.VISIBLE);
                    if(isEditorInitialized){
                        mPreviewContent.show(true);
                    }
                    openFormattingBottomSheet = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mediaSourceBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if(newState == BottomSheetBehavior.STATE_EXPANDED){
                            mInsertContent.close(true);
                            mPreviewContent.hide(true);
                            handleSoftKeyboard(false);
                            setFormattingBottomSheetBehavior(false);
                        }

                        if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                            mInsertContent.showMenu(true);
                            if(isEditorInitialized){
                                mPreviewContent.show(true);
                            }
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
        editorContent.setWebChromeClient(new WebContentEditorChrome(this));
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
        menu.findItem(R.id.content_action_direction).setIcon(ContentFormattingHelper.getInstance()
                        .getFormatListByType(FORMATTING_ACTIONS_INDEX).get(0).getFormatIcon());
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
            openFormattingBottomSheet = true;
            if(isSoftKeyboardActive){
                handleSoftKeyboard(false);
            }else{
                setFormattingBottomSheetBehavior(true);
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
                List<ContentFormat> format = ContentFormattingHelper.getInstance()
                        .getFormatListByType(FORMATTING_ACTIONS_INDEX);
                format.get(0).setActive(true);
                format.get(0).setFormatIcon(popupItem.getItemId() == R.id.direction_leftToRight ?
                        R.drawable.ic_format_textdirection_l_to_r_white_24dp:
                        R.drawable.ic_format_textdirection_r_to_l_white_24dp);
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
        }
        return true;
    }

    @Override
    public void onCallbackReceived(String value) {
        processJsCallLogValues(value);
    }

    @Override
    public void onProgressChanged(int newProgress) {
        progressDialog.setProgress(newProgress);
    }

    @Override
    public void onPageFinishedLoading() {
        progressDialog.setVisibility(View.GONE);
    }

    /**
     * Process values returned from JS calls
     * @param value value returned
     */
    private void processJsCallLogValues(String value){
        if(value.contains("action")){
            WebJsResponse callback = new Gson().fromJson(value,WebJsResponse.class);
            String content = Base64Coder.decodeString(callback.getContent());
            switch (callback.getAction()){
                case ACTION_INIT_EDITOR:
                    if(mInsertContent.isMenuButtonHidden()){
                        mInsertContent.setVisibility(View.VISIBLE);
                        mInsertContent.showMenuButton(true);
                    }
                    isEditorInitialized = Boolean.parseBoolean(callback.getContent());
                    if(isEditorInitialized){
                        handleSoftKeyboard(true);
                    }
                    startEditing.setVisibility(View.GONE);
                    progressDialog.setVisibility(View.GONE);
                    blankDocumentContainer.setVisibility(View.GONE);
                    break;

                case ACTION_CONTENT_CHANGED:
                    mPreviewContent.setVisibility(View.VISIBLE);
                    isDocumentEmpty = content.length() <= 0;
                    if(!isDocumentEmpty){
                       if(isEditorInitialized){
                           mPreviewContent.show(true);
                       }
                    }else{
                        mPreviewContent.hide(true);
                    }
                    executeJsFunction(editorContent, "ustadEditor.loadContentForPreview",
                            this, callback.getContent());
                    break;

                case ACTION_SAVE_CONTENT:
                    Document index = getIndexDocument(index_file);
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

                case ACTION_CHECK_ACTIVE_CONTROLS:
                    checkActivatedControls();
                    break;

                case ACTION_CONTROLS_ACTIVATED:
                    UstadMobileSystemImpl.l(UMLog.DEBUG,700,content);
                    String formatCommand = content.split("-")[0];
                    String formatStatus = content.split("-")[1];
                    ContentFormattingHelper mHelper = ContentFormattingHelper.getInstance();
                    ContentFormat format = mHelper.getFormatByCommand(formatCommand);
                    format.setActive(Boolean.parseBoolean(formatStatus));
                    mHelper.updateFormat(format);
                    LocalBroadcastManager.getInstance(this)
                            .sendBroadcast(new Intent(ACTION_FORMAT_STATUS_CHANGED));
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMediaTypeDialog();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case FILE_BROWSING_REQUEST:
                    try{
                        insertMedia(data.getData(),false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMERA_IMAGE_CAPTURE_REQUEST:
                    try{
                        insertMedia(cameraMedia, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void insertMedia(Uri uri, boolean fromCamera) throws IOException {
        progressDialog.setVisibility(View.VISIBLE);
        String mimeType = UmAndroidUriUtil.getMimeType(this,uri);
        File sourceFile, compressedFile;
        if(fromCamera){
            sourceFile = fileFromCamera;
        }else{
             sourceFile = new File(Objects.requireNonNull(UmAndroidUriUtil.getPath(this, uri)));
        }
        compressedFile = sourceFile;
        if(mimeType.contains("image")){
            compressedFile = new Compressor(this)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(sourceFile);
        }

        File destination = new File(contentDir, MEDIA_CONTENT_DIR + compressedFile.
                        getName().replaceAll("\\s+","_"));
        UMFileUtil.copyFile(sourceFile,destination);
        String source = MEDIA_CONTENT_DIR + destination.getName();
        progressDialog.setVisibility(View.GONE);
        executeJsFunction(editorContent,
                "ustadEditor.insertMedia",this, source,mimeType);
    }

    @Override
    public void onBackPressed() {
        handleExpandableViews();
    }

    @Override
    public void onMenuToggle(boolean opened) {
        if(opened){
            mPreviewContent.hide(true);
            handleSoftKeyboard(false);
            setFormattingBottomSheetBehavior(false);
        }else{
            handleFABMenuButton(false);
            if(!isMediaSourceBottomSheetExpanded() && isEditorInitialized){
                mPreviewContent.show(true);
            }
            if(isFromFormatting){
                isFromFormatting = false;
                setFormattingBottomSheetBehavior(true);
            }
        }
    }

    /**
     * Collapse all expandable views before moving out of the activity (User experience)
     */
    private void handleExpandableViews(){
        boolean isDrawerOpen = mContentPageDrawer.isDrawerOpen(GravityCompat.END);

        boolean isFabMenuOpen = mInsertContent.isOpened();

        if(isDrawerOpen){
            mContentPageDrawer.closeDrawer(GravityCompat.END);
        }

        if(isMediaSourceBottomSheetExpanded()){
            setMediaSourceBottomSheetBehavior(false);
        }

        if(isFormattingBottomSheetExpanded()){
            setFormattingBottomSheetBehavior(false);
        }

        if(isFabMenuOpen){
            mInsertContent.close(true);
        }

        if(!isMediaSourceBottomSheetExpanded() && !isFormattingBottomSheetExpanded()
                && !isFabMenuOpen && !isDrawerOpen){
           handleFinishingActivity();
        }
    }

    private void handleFinishingActivity() {
        if(new File(contentDir,index_temp_file).delete()){
            //TODO: change this to finish() after demo.
            moveTaskToBack(true);
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

    @Override
    public void startWebServer(){
        EmbeddedHTTPD embeddedHTTPD = new EmbeddedHTTPD(0, this);
        embeddedHTTPD.addRoute( assetsDir+"(.)+",  AndroidAssetsHandler.class, this);
        embeddedHTTPD.addRoute( EDITOR_ROOT_DIR +"(.)+",  FileDirectoryHandler.class, contentDir);
        try {
            embeddedHTTPD.start();
            if(embeddedHTTPD.isAlive()){
                baseUrl =  "http://127.0.0.1:"+embeddedHTTPD.getListeningPort()+"/";
                args.put(ContentPreviewView.PREVIEW_URL,baseUrl+ EDITOR_ROOT_DIR);
                presenter.handleDocument(new File(contentDir,index_file));
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
    public BottomSheetBehavior getMediaSourceBottomSheetBehavior() {
        return mediaSourceBottomSheetBehavior;
    }

    @Override
    public void setContentBold() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingBold",this, (String[]) null);
    }

    @Override
    public void setContentItalic() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingItalic",this, (String[]) null);
    }

    @Override
    public void setContentUnderlined() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingUnderline",this, (String[]) null);
    }

    @Override
    public void setContentStrikeThrough() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingStrikeThrough",this, (String[]) null);
    }

    @Override
    public void setContentFontSize(String fontSize) {
        executeJsFunction(editorContent,
                "ustadEditor.setFontSize",this, fontSize);
    }

    @Override
    public void setContentSuperscript() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingSuperScript",this, (String[]) null);
    }

    @Override
    public void setContentSubScript() {
        executeJsFunction(editorContent,
                "ustadEditor.textFormattingSubScript",this, (String[]) null);
    }

    @Override
    public void setContentJustified() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphFullJustification",this, (String[]) null);
    }

    @Override
    public void setContentCenterAlign() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphCenterJustification",this, (String[]) null);
    }

    @Override
    public void setContentLeftAlign() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphLeftJustification",this, (String[]) null);
    }

    @Override
    public void setContentRightAlign() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphRightJustification",this, (String[]) null);
    }

    @Override
    public void setContentOrderedList() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphOrderedListFormatting",this, (String[]) null);
    }

    @Override
    public void setContentUnOrderList() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphUnOrderedListFormatting",this, (String[]) null);
    }

    @Override
    public void setContentIncreaseIndent() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphIndent",this, (String[]) null);
    }

    @Override
    public void setContentDecreaseIndent() {
        executeJsFunction(editorContent,
                "ustadEditor.paragraphOutDent",this, (String[]) null);
    }

    @Override
    public void setContentRedo() {
        executeJsFunction(editorContent,
                "ustadEditor.editorActionRedo",this, (String[]) null);
    }

    @Override
    public void setContentUndo() {
        executeJsFunction(editorContent,
                "ustadEditor.editorActionUndo",this, (String[]) null);
    }

    @Override
    public void setContentTextDirection(boolean right) {

        executeJsFunction(editorContent,
                right ? "ustadEditor.textDirectionRightToLeft":
                        "ustadEditor.textDirectionLeftToRight",this, (String[]) null);
        invalidateOptionsMenu();
    }

    @Override
    public void insertMultipleChoiceQuestion() {
        executeJsFunction(editorContent,
                "ustadEditor.insertMultipleChoiceQuestionTemplate",
                this, (String[]) null);
    }

    @Override
    public void insertFillTheBlanksQuestion() {
        executeJsFunction(editorContent,
                "ustadEditor.insertFillInTheBlanksQuestionTemplate",
                this, (String[]) null);
    }

    @Override
    public void requestEditorContent() {
        executeJsFunction(editorContent,
                "ustadEditor.getContent",this, (String[]) null);
    }

    @Override
    public void createNewDocument() {
        UMFileUtil.writeToFile(new File(contentDir,index_file),NEW_DOCUMENT_TEMPLATE);
    }


    @Override
    public void handleContentMenu() {
        if(mInsertContent.isOpened()){
            mInsertContent.close(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == android.view.MotionEvent.ACTION_UP){
            openFormattingBottomSheet = true;
            handleSoftKeyboard(false);

        }
        return super.onTouchEvent(event);
    }

    /**
     * Start copying resources to the local editor directory
     */
    @Override
    public void handleResources() {
        new Thread(() -> {
            ContentEditorResourceHandler resourceHandler = new ContentEditorResourceHandler(
                    contentDir.getAbsolutePath(),UMFileUtil.joinPaths(baseUrl,assetsDir,"tinymce"));
            resourceHandler.with(
                    () -> {
                        UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                                "All resources has been copied to the external dirs");
                        runOnUiThread(this::loadIndexFile);
                    })
                    .startCopying();
        }).start();
    }

    /**
     * Search for all scripts necessary for file editing, if not present then append them
     * to the temp file then direct them to asset handler to be resolved as files.
     */
    private void loadIndexFile() {
        File source = new File(contentDir, index_file);
        File destination = new File(contentDir, index_temp_file);
        try {
            UMFileUtil.copyFile(source,destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document htmlDoc = getIndexDocument(index_temp_file);
        Element docHead = htmlDoc.select("head").first();
        Element docBody = htmlDoc.select("body").first();

        /*Check if the document is empty and show empty document container*/
        boolean hasImageContent = docBody.select("img[src]").size() > 0;
        boolean hasVideoAudioContent = docBody.select("source[src]").size() > 0;
        isDocumentEmpty = docBody.text().length() <= 0 && !hasImageContent && !hasVideoAudioContent;
        blankDocumentContainer.setVisibility(isDocumentEmpty ? View.VISIBLE:View.GONE);

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

        /*Find resources that should be placed under head tag, and make sure to change widget
         to load from assets. If those resources are not there then add them*/
        for (String ref : CONTENT_EDITOR_HEAD_RESOURCES) {
            if(!docHead.html().contains(ref)){
                String resource;
                Element script = Jsoup.parse(ref).select("script[src]").first();
                if(ref.contains(RESOURCE_JS_USTAD_WIDGET)){
                    script.attr("src", baseUrl+assetsDir
                            +"/tinymce/js/plugins/ustadmobile/"+ RESOURCE_JS_USTAD_WIDGET);
                    resource = script.toString();
                }else{
                    resource = ref;
                }
                docHead.append(resource);
            }
        }

        /*Check if resources which are supposed to live on doc body are there, if not add them.*/
        for (String ref : CONTENT_EDITOR_BODY_RESOURCES) {
            if(!docBody.html().contains(ref)){
                docBody.append(ref);
            }
        }

        /*Delete temp index file if exists and write new one*/
        if(destination.exists()) destination.delete();

        UMFileUtil.writeToFile(destination,htmlDoc.html());

        /*Load temp index file*/
        if(baseUrl != null){
            editorContent.setWebViewClient(new WebContentEditorClient(
                    this,baseUrl+ EDITOR_ROOT_DIR));
            progressDialog.setVisibility(View.VISIBLE);
            editorContent.loadUrl(UMFileUtil.joinPaths(baseUrl, EDITOR_ROOT_DIR,index_temp_file));
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

    private boolean isFormattingBottomSheetExpanded(){
         return formattingBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setFormattingBottomSheetBehavior(boolean expanded){
        formattingBottomSheetBehavior.setState(expanded ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private boolean isMediaSourceBottomSheetExpanded(){
        return mediaSourceBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setMediaSourceBottomSheetBehavior(boolean expanded){
        mediaSourceBottomSheetBehavior.setState(expanded ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED);
    }



    /**
     * Prepare lis of all formatting types
     */
    private void prepareFormattingTypeLists(){

    }


    private void handleSoftKeyboard(boolean show){
        if(show){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else{
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

    /**
     * Open camera ans start acquire media content
     * @param isImage True if media is of image type otherwise video.
     */
    private void startCameraIntent(boolean isImage){
        String imageId = String.valueOf(System.currentTimeMillis());
        Intent cameraIntent = new Intent(isImage ?
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE:MediaStore.ACTION_VIDEO_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        fileFromCamera = new File(dir,imageId+ (isImage ? "_image.png":"_video.mp4"));
        cameraMedia = FileProvider.getUriForFile(this,
                getPackageName()+".fileprovider", fileFromCamera);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraMedia);
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST);
    }

    /**
     * Handle choice between video and image from the camera.
     */
    private void showMediaTypeDialog(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(impl.getString(MessageID.content_media_title,this));
        builder.setMessage(impl.getString(MessageID.content_media_message,this));
        builder.setPositiveButton(impl.getString(MessageID.content_media_photo,this),
                (dialog, which) -> startCameraIntent(true));
        builder.setNegativeButton(impl.getString(MessageID.content_media_video,this),
                (dialog, which) -> startCameraIntent(false));
        builder.show();
    }

    /**
     * Handle opening and closing of the soft keyboard
     */
    private void handleKeyboardVisibilityChange(View rootView){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;
            isSoftKeyboardActive = keypadHeight > screenHeight * 0.15;
            if (isSoftKeyboardActive) {
                setFormattingBottomSheetBehavior(false);
                if(mInsertContent.isOpened()){
                    mInsertContent.hideMenuButton(true);
                    mPreviewContent.hide(true);
                }
            }else{
                if(isEditorInitialized && !isDocumentEmpty){
                    mPreviewContent.show(true);
                }
                if(openFormattingBottomSheet){
                    setFormattingBottomSheetBehavior(true);
                }
            }
        });
    }

    /**
     * Handle clipboard action completion
     */
    private void handleClipBoardContentChanges(){
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        assert clipboard != null;
        clipboard.addPrimaryClipChangedListener(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Clipboard text changed");
            setFormattingBottomSheetBehavior(false);
        });
    }


    private void checkActivatedControls(){
        if(isEditorInitialized){
            for(ContentFormat format: ContentFormattingHelper.getInstance().getAllFormats()){
                executeJsFunction(editorContent, "ustadEditor.checkCurrentActiveControls",
                        this, format.getFormatCommand());
            }
        }
    }

}
