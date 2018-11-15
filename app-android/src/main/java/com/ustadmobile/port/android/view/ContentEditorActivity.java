package com.ustadmobile.port.android.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.ustadmobile.port.android.contenteditor.BottomToolbarView;
import com.ustadmobile.port.android.contenteditor.ContentEditorResourceHandler;
import com.ustadmobile.port.android.contenteditor.ContentFormat;
import com.ustadmobile.port.android.contenteditor.ContentFormattingHelper;
import com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher;
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
import java.util.concurrent.TimeUnit;

import id.zelory.compressor.Compressor;

import static com.ustadmobile.core.controller.CatalogPresenter.SHARED_RESOURCE;
import static com.ustadmobile.port.android.contenteditor.ContentFormattingHelper.FORMATTING_ACTIONS_INDEX;
import static com.ustadmobile.port.android.contenteditor.ContentFormattingHelper.isTobeHighlighted;
import static com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher.ANIMATED_SOFT_KEYBOARD_PANEL;
import static com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher.MAX_SOFT_KEYBOARD_DELAY;
import static com.ustadmobile.port.android.contenteditor.WebContentEditorClient.executeJsFunction;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView,
        WebContentEditorChrome.JsLoadingCallback, BottomToolbarView.OnQuickActionMenuItemClicked,
        ContentFormattingHelper.StateChangeDispatcher , EditorAnimatedViewSwitcher.OnAnimatedViewsClosedListener{

    private static ContentEditorPresenter presenter;

    private EditorAnimatedViewSwitcher viewSwitcher;

    private BottomSheetBehavior formattingBottomSheetBehavior;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private AppBarLayout umBottomToolbarHolder;

    private BottomToolbarView mBottomToolbarView;

    private WebView editorWebView;

    private DrawerLayout mContentPageDrawer;

    private Toolbar toolbar;

    private boolean isEditorInitialized = false;

    private boolean isDocumentEmpty = true;

    private FloatingActionButton startEditing;

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

    private ContentFormattingHelper formattingHelper;


    /**
     * Content formatting adapter which handles formatting sections
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
    public static class FormattingFragment extends Fragment
            implements ContentFormattingHelper.StateChangeDispatcher {

        private FormatsAdapter adapter;

        private int formattingType;

        private ContentFormattingHelper formattingHelper;


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
                mIcon.setImageResource(format.getFormatIcon());
                changeState(mIcon,mLayout,format.isActive());
                if(!isTobeHighlighted(format.getFormatCommand())){
                    changeState(mIcon,mLayout,false);
                }
                ContentFormattingHelper formattingHelper = ContentFormattingHelper.getInstance();
                mLayout.setOnClickListener(v -> {
                    if(!format.getFormatCommand().equals(TEXT_FORMAT_TYPE_FONT)){
                        changeState(mIcon,mLayout,true);
                        formattingHelper.updateOtherJustification(format.getFormatCommand());
                        formattingHelper.updateOtherListOrder(format.getFormatCommand());
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

            private void changeState(ImageView imageIcon,
                                     RelativeLayout iconHolder, boolean isActivated){
                imageIcon.setColorFilter(ContextCompat.getColor(getContext(),
                        isActivated ? R.color.icons:R.color.text_secondary));
                iconHolder.setBackgroundColor(ContextCompat.getColor(getContext(),
                        isActivated ? R.color.content_icon_active:R.color.icons));
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
            formattingHelper = ContentFormattingHelper.getInstance();
            formattingHelper.setStateDispatcher(this);
            adapter.setContentFormats(formattingHelper.getFormatListByType(formattingType));
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(100));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);
            return  rootView;

        }

        @Override
        public void onStateChanged(ContentFormat format) {
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
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


    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_editor);
        formattingBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_sheet_container));
        mediaSourceBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_multimedia_source_sheet_container));

        BottomSheetBehavior contentOptionsBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_content_option_sheet_container));

        toolbar = findViewById(R.id.um_toolbar);
        RelativeLayout mInsertMultimedia = findViewById(R.id.content_option_multimedia);
        RelativeLayout mInsertMultipleChoice = findViewById(R.id.content_option_multiplechoice);
        RelativeLayout mInsertFillBlanks = findViewById(R.id.content_option_filltheblanks);
        mContentPageDrawer = findViewById(R.id.content_page_drawer);
        editorWebView = findViewById(R.id.editor_content);
        progressDialog = findViewById(R.id.progressBar);
        startEditing = findViewById(R.id.btn_start_editing);
        RelativeLayout mFromCamera = findViewById(R.id.multimedia_from_camera);
        RelativeLayout mFromDevice = findViewById(R.id.multimedia_from_device);
        View rootView = findViewById(R.id.coordinationLayout);
        TextView blankDocTitle = findViewById(R.id.blank_doc_title);
        TextView bdClickLabel = findViewById(R.id.click_label);
        TextView bdCreateLabel = findViewById(R.id.editing_label);
        blankDocumentContainer = findViewById(R.id.new_doc_container);
        umBottomToolbarHolder = findViewById(R.id.um_appbar_bottom);
        mBottomToolbarView = findViewById(R.id.um_toolbar_bottom);

        formattingHelper = ContentFormattingHelper.getInstance();
        formattingHelper.setStateDispatcher(this);


        //Set quick action menus above the keyboard when opened
        mBottomToolbarView.inflateMenu(R.menu.menu_content_editor_quick_actions);
        mBottomToolbarView.setOnQuickActionMenuItemClicked(this);

        viewSwitcher = EditorAnimatedViewSwitcher.getInstance()
                        .with(this,this)
                        .setViews(rootView, editorWebView,contentOptionsBottomSheetBehavior,
                                formattingBottomSheetBehavior, mediaSourceBottomSheetBehavior,
                                mContentPageDrawer);



        ViewPager mViewPager = findViewById(R.id.content_types_viewpager);
        TabLayout mTabLayout = findViewById(R.id.content_types_tabs);

        if(toolbar != null){
            toolbar.setTitle("");
            handleBackNavigationIcon();
        }

        handleClipBoardContentChanges();
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


        findViewById(R.id.action_close_tab_formats).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL));
        findViewById(R.id.action_close_tab_multimedia_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL));
        findViewById(R.id.action_close_tab_content_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_CONTENT_OPTION_PANEL));

        mFromDevice.setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
            startFileBrowser();
        });

        startEditing.setOnClickListener(v -> {
            progressDialog.setVisibility(View.VISIBLE);
            executeJsFunction(editorWebView, "ustadEditor.initTinyMceEditor",
                    ContentEditorActivity.this, (String[]) null);
        });

        mFromCamera.setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContentEditorActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                return;
            }
            showMediaTypeDialog();
        });



        mInsertMultimedia.setOnClickListener(v ->
                viewSwitcher.animateView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL));

        mInsertMultipleChoice.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_MULTIPLE_CHOICE_QN,null));

        mInsertFillBlanks.setOnClickListener(v ->
                presenter.handleFormatTypeClicked(CONTENT_INSERT_FILL_THE_BLANKS_QN,null));

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        blankDocTitle.setText(impl.getString(MessageID.content_blank_doc_title,this));
        bdClickLabel.setText(impl.getString(MessageID.content_blank_doc_click_label,this));
        bdCreateLabel.setText(impl.getString(MessageID.content_blank_doc_start_label,this));


        ContentFormattingPagerAdapter adapter =
                new ContentFormattingPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

        WebSettings webSettings = editorWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        editorWebView.setWebChromeClient(new WebContentEditorChrome(this));
        editorWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        editorWebView.clearCache(true);
        editorWebView.clearHistory();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_editor_top_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.content_action_direction)
                .setIcon(formattingHelper.getFormatListByType(FORMATTING_ACTIONS_INDEX)
                        .get(0).getFormatIcon());
        menu.findItem(R.id.content_action_undo).setVisible(isEditorInitialized);
        menu.findItem(R.id.content_action_redo).setVisible(isEditorInitialized);
        menu.findItem(R.id.content_action_format).setVisible(isEditorInitialized);
        menu.findItem(R.id.content_action_direction).setVisible(isEditorInitialized);
        menu.findItem(R.id.content_action_insert).setVisible(isEditorInitialized);
        menu.findItem(R.id.content_action_preview).setVisible(isEditorInitialized);
        return super.onPrepareOptionsMenu(menu);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.content_action_pages) {
            mContentPageDrawer.openDrawer(GravityCompat.END);

        }else if(itemId == android.R.id.home){
            viewSwitcher.closeActivity();

        }else if(itemId == R.id.content_action_format){

            viewSwitcher.animateView(EditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL);

        }else if(itemId == R.id.content_action_preview){
            UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                    args,getApplicationContext());

        }else if(itemId == R.id.content_action_insert){

            viewSwitcher.animateView(EditorAnimatedViewSwitcher.ANIMATED_CONTENT_OPTION_PANEL);

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
                List<ContentFormat> format =
                        formattingHelper.getFormatListByType(FORMATTING_ACTIONS_INDEX);
                format.get(0).setActive(true);
                format.get(0).setFormatIcon(popupItem.getItemId() == R.id.direction_leftToRight ?
                        R.drawable.ic_format_textdirection_l_to_r_white_24dp:
                        R.drawable.ic_format_textdirection_r_to_l_white_24dp);
                presenter.handleFormatTypeClicked(
                        popupItem.getItemId() == R.id.direction_leftToRight ?
                                ACTION_TEXT_DIRECTION_LTR:ACTION_TEXT_DIRECTION_RTL,null);
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
    public void onDestroy() {
        super.onDestroy();
        if(formattingHelper != null){
            formattingHelper.destroy();
        }
    }

    @Override
    public void onStateChanged(ContentFormat format) {
        if(format.getFormatId() != 0){
            mBottomToolbarView.updateMenu(format);
        }
    }

    @Override
    public void onCallbackReceived(String value) {
        if(value.contains("action")){
            WebJsResponse callback = new Gson().fromJson(value,WebJsResponse.class);
            processJsCallLogValues(callback);
        }

    }

    @Override
    public void onAnimatedViewsClosed() {
        if(isEditorInitialized){
            isEditorInitialized = false;
            handleBackNavigationIcon();
            invalidateOptionsMenu();
            handleQuickActions();
            viewSwitcher.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL);
            startEditing.setVisibility(View.VISIBLE);
            loadIndexFile();
        }else{
            if(new File(contentDir,index_temp_file).delete())moveTaskToBack(true);
        }
    }

    @Override
    public void onFocusRequested() {
        requestEditorFocus();
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
     * @param callback object returned
     */
    private void processJsCallLogValues(WebJsResponse callback){

        String content = Base64Coder.decodeString(callback.getContent());

        switch (callback.getAction()){
            //on editor initialized
            case ACTION_INIT_EDITOR:
                isEditorInitialized = Boolean.parseBoolean(callback.getContent());
                if(isEditorInitialized){
                    invalidateOptionsMenu();
                    requestEditorFocus();
                    editorWebView.postDelayed(() -> {
                        viewSwitcher.animateView(ANIMATED_SOFT_KEYBOARD_PANEL);
                    },TimeUnit.SECONDS.toMillis(MAX_SOFT_KEYBOARD_DELAY));
                }
                handleBackNavigationIcon();
                startEditing.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                blankDocumentContainer.setVisibility(View.GONE);
                viewSwitcher.setEditorActivated(isEditorInitialized);
                handleQuickActions();
                break;
            //content changed on the editor
            case ACTION_CONTENT_CHANGED:
                isDocumentEmpty = content.length() <= 0;
                executeJsFunction(editorWebView, "ustadEditor.loadContentForPreview",
                        this, callback.getContent());
                break;

            //Saving content on file after being changed
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

                UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                        content);
                UMFileUtil.writeToFile(new File(contentDir,"index.html"),index.html());
                break;
            //start checking if there is any control activated
            case ACTION_CHECK_ACTIVE_CONTROLS:
                checkActivatedControls();
                break;
            //Callback received after checking which control are activated
            case ACTION_CONTROLS_ACTIVATED:
                String formatCommand = content.split("-")[0];
                String formatStatus = content.split("-")[1];
                ContentFormat format = formattingHelper.getFormatByCommand(formatCommand);
                try{
                    format.setActive(Boolean.parseBoolean(formatStatus));
                    formattingHelper.updateFormat(format);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
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
        executeJsFunction(editorWebView,
                "ustadEditor.insertMedia",this, source,mimeType);
    }


    @Override
    public void onBackPressed() {
       viewSwitcher.closeActivity();
    }

    @Override
    public void onQuickActionClicked(String command) {
        ContentFormat format = formattingHelper.getFormatByCommand(command);
        presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
    }


    private void handleQuickActions(){
        umBottomToolbarHolder.setVisibility(isEditorInitialized ? View.VISIBLE:View.GONE);
    }


    private void requestEditorFocus(){
        executeJsFunction(editorWebView,"ustadEditor.requestFocus",
                this, (String[]) null);
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
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingBold",this, (String[]) null);
    }

    @Override
    public void setContentItalic() {
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingItalic",this, (String[]) null);
    }

    @Override
    public void setContentUnderlined() {
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingUnderline",this, (String[]) null);
    }

    @Override
    public void setContentStrikeThrough() {
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingStrikeThrough",this, (String[]) null);
    }

    @Override
    public void setContentFontSize(String fontSize) {
        executeJsFunction(editorWebView,
                "ustadEditor.setFontSize",this, fontSize);
    }

    @Override
    public void setContentSuperscript() {
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingSuperScript",this, (String[]) null);
    }

    @Override
    public void setContentSubScript() {
        executeJsFunction(editorWebView,
                "ustadEditor.textFormattingSubScript",this, (String[]) null);
    }

    @Override
    public void setContentJustified() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphFullJustification",this, (String[]) null);
    }

    @Override
    public void setContentCenterAlign() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphCenterJustification",this, (String[]) null);
    }

    @Override
    public void setContentLeftAlign() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphLeftJustification",this, (String[]) null);
    }

    @Override
    public void setContentRightAlign() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphRightJustification",this, (String[]) null);
    }

    @Override
    public void setContentOrderedList() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphOrderedListFormatting",this, (String[]) null);
    }

    @Override
    public void setContentUnOrderList() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphUnOrderedListFormatting",this, (String[]) null);
    }

    @Override
    public void setContentIncreaseIndent() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphIndent",this, (String[]) null);
    }

    @Override
    public void setContentDecreaseIndent() {
        executeJsFunction(editorWebView,
                "ustadEditor.paragraphOutDent",this, (String[]) null);
    }

    @Override
    public void setContentRedo() {
        executeJsFunction(editorWebView,
                "ustadEditor.editorActionRedo",this, (String[]) null);
    }

    @Override
    public void setContentUndo() {
        executeJsFunction(editorWebView,
                "ustadEditor.editorActionUndo",this, (String[]) null);
    }

    @Override
    public void setContentTextDirection(boolean isLTR) {
        executeJsFunction(editorWebView,
                !isLTR ? "ustadEditor.textDirectionRightToLeft":
                        "ustadEditor.textDirectionLeftToRight",this, (String[]) null);
        invalidateOptionsMenu();
    }

    @Override
    public void insertMultipleChoiceQuestion() {
        executeJsFunction(editorWebView,
                "ustadEditor.insertMultipleChoiceQuestionTemplate",
                this, (String[]) null);
    }

    @Override
    public void insertFillTheBlanksQuestion() {
        executeJsFunction(editorWebView,
                "ustadEditor.insertFillInTheBlanksQuestionTemplate",
                this, (String[]) null);
    }

    @Override
    public void requestEditorContent() {
        executeJsFunction(editorWebView,
                "ustadEditor.getContent",this, (String[]) null);
    }

    @Override
    public void createNewDocument() {
        UMFileUtil.writeToFile(new File(contentDir,index_file),NEW_DOCUMENT_TEMPLATE);
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
            editorWebView.setWebViewClient(new WebContentEditorClient(
                    this,baseUrl+ EDITOR_ROOT_DIR));
            progressDialog.setVisibility(View.VISIBLE);
            editorWebView.loadUrl(UMFileUtil.joinPaths(baseUrl, EDITOR_ROOT_DIR,index_temp_file));
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
     * Change toolbar navigation icon based on editor state
     */
    private void handleBackNavigationIcon(){
        toolbar.setNavigationIcon(isEditorInitialized ?
                R.drawable.ic_done_white_24dp: R.drawable.ic_arrow_back_white_24dp);
    }

    /**
     * Handle clipboard action completion
     */
    private void handleClipBoardContentChanges(){
        ClipboardManager clipboard =
                (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        assert clipboard != null;
        clipboard.addPrimaryClipChangedListener(() ->
        viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL));
    }

    /**
     * Check if there are active controls
     */
    private void checkActivatedControls(){
        if(isEditorInitialized){
            for(ContentFormat format: formattingHelper.getAllFormats()){
                executeJsFunction(editorWebView, "ustadEditor.checkCurrentActiveControls",
                        this, format.getFormatCommand());
            }
        }
    }

}
