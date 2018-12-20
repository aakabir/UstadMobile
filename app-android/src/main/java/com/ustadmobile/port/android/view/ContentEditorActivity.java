package com.ustadmobile.port.android.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.port.android.contenteditor.ContentFormat;
import com.ustadmobile.port.android.contenteditor.ContentFormattingHelper;
import com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher;
import com.ustadmobile.port.android.contenteditor.UmAndroidUriUtil;
import com.ustadmobile.port.android.contenteditor.UmEditorToolbarView;
import com.ustadmobile.port.android.contenteditor.UmEditorWebView;
import com.ustadmobile.port.android.contenteditor.WebContentEditorChrome;
import com.ustadmobile.port.android.contenteditor.WebContentEditorClient;
import com.ustadmobile.port.android.contenteditor.WebContentEditorInterface;
import com.ustadmobile.port.android.contenteditor.WebJsResponse;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static com.ustadmobile.core.contenteditor.UmEditorFileHelperCore.INDEX_FILE;
import static com.ustadmobile.port.android.contenteditor.ContentFormattingHelper.FORMATTING_ACTIONS_INDEX;
import static com.ustadmobile.port.android.contenteditor.ContentFormattingHelper.isTobeHighlighted;
import static com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher.ANIMATED_CONTENT_OPTION_PANEL;
import static com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher.ANIMATED_SOFT_KEYBOARD_PANEL;
import static com.ustadmobile.port.android.contenteditor.EditorAnimatedViewSwitcher.MAX_SOFT_KEYBOARD_DELAY;
import static com.ustadmobile.port.android.contenteditor.WebContentEditorClient.executeJsFunction;
import static com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper.EDITOR_BASE_DIR_NAME;
import static com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper.MEDIA_DIRECTORY;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView,
        WebContentEditorChrome.JsLoadingCallback, UmEditorToolbarView.OnQuickActionMenuItemClicked,
        ContentFormattingHelper.StateChangeDispatcher ,UmEditorFileHelper.ZipFileTaskProgressListener,
        EditorAnimatedViewSwitcher.OnAnimatedViewsClosedListener {

    private static ContentEditorPresenter presenter;

    private UmEditorFileHelper umEditorFileHelper;

    private EditorAnimatedViewSwitcher viewSwitcher;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private BottomSheetBehavior contentOptionsBottomSheetBehavior;

    private AppBarLayout umBottomToolbarHolder;

    private UmEditorToolbarView umEditorToolbarView;

    private UmEditorWebView umEditorWebView;

    private DrawerLayout mContentPageDrawer;

    private Toolbar toolbar;

    private boolean isEditorInitialized = false;

    private boolean isActivityActive = false;

    private boolean openPreview = false;

    private boolean isMultimediaFilePicker = false;

    private boolean isEditorPreview = false;

    private FloatingActionButton startEditing;

    private Hashtable args = null;

    private Uri cameraMedia;

    private File fileFromCamera;

    private ProgressBar progressDialog;

    public static final int CAMERA_IMAGE_CAPTURE_REQUEST = 900;

    public static final int CAMERA_PERMISSION_REQUEST = 901;

    private static final int FILE_BROWSING_REQUEST = 902;

    private View blankDocumentContainer;

    private ContentFormattingHelper formattingHelper;

    private static final String EDITOR_METHOD_PREFIX = "umContentEditor.";


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
        BottomSheetBehavior formattingBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_sheet_container));
        mediaSourceBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_multimedia_source_sheet_container));

        contentOptionsBottomSheetBehavior = BottomSheetBehavior
                .from(findViewById(R.id.bottom_content_option_sheet_container));

        toolbar = findViewById(R.id.um_toolbar);
        RelativeLayout mInsertMultimedia = findViewById(R.id.content_option_multimedia);
        RelativeLayout mInsertMultipleChoice = findViewById(R.id.content_option_multiplechoice);
        RelativeLayout mInsertFillBlanks = findViewById(R.id.content_option_filltheblanks);
        mContentPageDrawer = findViewById(R.id.content_page_drawer);
        umEditorWebView = findViewById(R.id.editor_content);
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
        umEditorToolbarView = findViewById(R.id.um_toolbar_bottom);

        formattingHelper = ContentFormattingHelper.getInstance();
        formattingHelper.setStateDispatcher(this);

        //Set quick action menus above the keyboard when opened
        umEditorToolbarView.inflateMenu(R.menu.menu_content_editor_quick_actions);
        umEditorToolbarView.setOnQuickActionMenuItemClicked(this);

        viewSwitcher = EditorAnimatedViewSwitcher.getInstance()
                        .with(this,this)
                        .setViews(rootView, umEditorWebView,contentOptionsBottomSheetBehavior,
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


        args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());

        umEditorFileHelper = new UmEditorFileHelper();
        umEditorFileHelper.init(this);
        umEditorFileHelper.setZipTaskProgressListener(this);
        umEditorFileHelper.addBaseAssetHandler(AndroidAssetsHandler.class);
        presenter = new ContentEditorPresenter(this,args,this);

        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        findViewById(R.id.action_close_tab_formats).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL));
        findViewById(R.id.action_close_tab_multimedia_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL));
        findViewById(R.id.action_close_tab_content_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL));

        mFromDevice.setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
            startFileBrowser();
        });

        startEditing.setOnClickListener(v -> {
            progressDialog.setVisibility(View.VISIBLE);
            if(isEditorPreview){
                loadIndexFile();
            }else{
                initializeEditor();
            }

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



        mInsertMultimedia.setOnClickListener(v ->{
            isMultimediaFilePicker = true;
            viewSwitcher.animateView(EditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
        });

        mInsertMultipleChoice.setOnClickListener(v ->{
            presenter.handleFormatTypeClicked(CONTENT_INSERT_MULTIPLE_CHOICE_QN,null);
            viewSwitcher.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL);
        });

        mInsertFillBlanks.setOnClickListener(v ->{
            presenter.handleFormatTypeClicked(CONTENT_INSERT_FILL_THE_BLANKS_QN,null);
            viewSwitcher.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL);
        });

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        blankDocTitle.setText(impl.getString(MessageID.content_blank_doc_title,this));
        bdClickLabel.setText(impl.getString(MessageID.content_blank_doc_click_label,this));
        bdCreateLabel.setText(impl.getString(MessageID.content_blank_doc_start_label,this));


        ContentFormattingPagerAdapter adapter =
                new ContentFormattingPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);

        WebSettings webSettings = umEditorWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        umEditorWebView.setWebChromeClient(new WebContentEditorChrome(this));
        umEditorWebView.addJavascriptInterface(
                new WebContentEditorInterface(this,this),"UmContentEditor");
        umEditorWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        presenter.handleContentEntryFileStatus();

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
           openPreview = true;
           isMultimediaFilePicker = false;
           viewSwitcher.closeActivity();

        }else if(itemId == R.id.content_action_insert){

            viewSwitcher.animateView(ANIMATED_CONTENT_OPTION_PANEL);

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
    public void onStateChanged(ContentFormat format) {
        if(format.getFormatId() != 0){
            umEditorToolbarView.updateMenu();
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
    public void onFocusRequested() {

    }

    @Override
    public void onProgressChanged(int newProgress) {
        progressDialog.setProgress(newProgress);
    }

    @Override
    public void onTaskStarted() {
       runOnUiThread(() -> progressDialog.setVisibility(View.VISIBLE));
    }

    @Override
    public void onTaskProgressUpdate(int progress) {
        runOnUiThread(() -> progressDialog.setProgress(progress));
    }

    @Override
    public void onTaskCompleted() {
        runOnUiThread(() -> progressDialog.setVisibility(View.GONE));
    }

    @Override
    public void onPageFinishedLoading() {
        progressDialog.setVisibility(View.GONE);
        if(isEditorPreview){
            isEditorPreview = false;
        }
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
                    handleWebViewMargin();
                    invalidateOptionsMenu();
                    umEditorWebView.postDelayed(() ->
                            viewSwitcher.animateView(ANIMATED_SOFT_KEYBOARD_PANEL),
                            MAX_SOFT_KEYBOARD_DELAY);
                }
                handleBackNavigationIcon();
                startEditing.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                blankDocumentContainer.setVisibility(View.GONE);
                viewSwitcher.setEditorActivated(isEditorInitialized);
                handleQuickActions();
                break;
            case ACTION_SAVE_CONTENT:
                Document indexFile = getIndexDocument();
                Elements contentContainer = indexFile.select("#"+EDITOR_BASE_DIR_NAME);
                contentContainer.first().html(content);

                UstadMobileSystemImpl.l(UMLog.DEBUG,700, content);
                //Update index.html file
                UMFileUtil.writeToFile(new File(umEditorFileHelper.getDestinationDirPath(),
                        INDEX_FILE),indexFile.html());
                break;

            //Callback received after checking which control are activated
            case ACTION_CONTROLS_ACTIVATED:
                /*String formatCommand = content.split("-")[0];
                String formatStatus = content.split("-")[1];
                ContentFormat format = formattingHelper.getFormatByCommand(formatCommand);
                try{
                    format.setActive(Boolean.parseBoolean(formatStatus));
                    formattingHelper.updateFormat(format);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }*/
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
    public void onAnimatedViewsClosed() {
        if(isActivityActive){
            handleUpdateFile();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewSwitcher.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(formattingHelper != null){
            formattingHelper.destroy();
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

    /**
     * Clean unused files and update zip file.
     */
    private void handleUpdateFile(){
        if(umEditorFileHelper != null){
            umEditorFileHelper.removeUnUsedResources(new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    umEditorFileHelper.updateFile(new UmCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if(result){
                                if(!openPreview || !isMultimediaFilePicker){
                                    postProcessEditor();
                                }else{
                                    if(isActivityActive){
                                        handleUpdateFile();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    });
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
       viewSwitcher.closeActivity();
    }

    @Override
    public void onQuickActionClicked(String command) {
        ContentFormat format = formattingHelper.getFormatByCommand(command);
        if(format != null){
            presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
        }
    }


    /**
     * Show /Hide quick action menus on top of the keyboard
     */
    private void handleQuickActions(){
        umBottomToolbarHolder.setVisibility(isEditorInitialized ? View.VISIBLE:View.GONE);
    }


    @Override
    public UmEditorFileHelper getFileHelper() {
        return umEditorFileHelper;
    }



    @Override
    public void setContentBold() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingBold",this);
    }

    @Override
    public void setContentItalic() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingItalic",this);
    }

    @Override
    public void setContentUnderlined() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingUnderline",this);
    }

    @Override
    public void setContentStrikeThrough() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingStrikeThrough",this);
    }

    @Override
    public void setContentFontSize(String fontSize) {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "setFontSize",this, fontSize);
    }

    @Override
    public void setContentSuperscript() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingSuperScript",this);
    }

    @Override
    public void setContentSubScript() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "textFormattingSubScript",this);
    }

    @Override
    public void setContentJustified() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphFullJustification",this);
    }

    @Override
    public void setContentCenterAlign() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphCenterJustification",this);
    }

    @Override
    public void setContentLeftAlign() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphLeftJustification",this);
    }

    @Override
    public void setContentRightAlign() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphRightJustification",this);
    }

    @Override
    public void setContentOrderedList() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphOrderedListFormatting",this);
    }

    @Override
    public void setContentUnOrderList() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphUnOrderedListFormatting",this);
    }

    @Override
    public void setContentIncreaseIndent() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphIndent",this);
    }

    @Override
    public void setContentDecreaseIndent() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "paragraphOutDent",this);
    }

    @Override
    public void setContentRedo() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "editorActionRedo",this);
    }

    @Override
    public void setContentUndo() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "editorActionUndo",this);
    }

    @Override
    public void setContentTextDirection(boolean isLTR) {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+(
                !isLTR ? "textDirectionRightToLeft":
                        "textDirectionLeftToRight"),this);
        invalidateOptionsMenu();
    }

    @Override
    public void insertMultipleChoiceQuestion() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "insertMultipleChoiceQuestionTemplate", this);
    }

    @Override
    public void insertFillTheBlanksQuestion() {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+
                "insertFillInTheBlanksQuestionTemplate", this);
    }

    @Override
    public void insertContent(String content){
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"executeRawContent",
                this,content);
    }

    @Override
    public void selectAllContent() {
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"selectAll",this);
    }


    @Override
    public void loadIndexFile() {
        umEditorWebView.setWebViewClient(new WebContentEditorClient(
                this,umEditorFileHelper.getBaseResourceRequestUrl()));
        args.put(ContentEditorView.EDITOR_PREVIEW_PATH, UMFileUtil.joinPaths(
                umEditorFileHelper.getMountedTempDirRequestUrl(),INDEX_FILE));
        String urlToLoad = UMFileUtil.joinPaths(umEditorFileHelper.getMountedTempDirRequestUrl(),
                INDEX_FILE);
        umEditorWebView.clearCache(true);
        umEditorWebView.clearHistory();
        umEditorWebView.loadUrl(urlToLoad);
        handleBlankDocumentView();
    }

    /**
     * Show or hide the blank document placeholder view
     */
    private void handleBlankDocumentView(){
        handleWebViewMargin();
        blankDocumentContainer.setVisibility(isDocumentEmpty() ? View.VISIBLE:View.GONE);
    }

    /**
     * Get base index.html file reference
     * @return Index.html file location
     */
    private Document getIndexDocument(){
        try {
            File indexFile = new File(umEditorFileHelper.getDestinationDirPath(),INDEX_FILE);
            return  Jsoup.parse(UMFileUtil.readTextFile(indexFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * Check if the document is empty, this helps to show the empty document placeholder view
     * @return True if the document is empty otherwise false.
     */
    private boolean isDocumentEmpty(){
        Document indexFile = getIndexDocument();
        Elements innerContent = indexFile.select("#"+EDITOR_BASE_DIR_NAME);
        return innerContent.html().length() <= 0;
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
     * Set bottom margin dynamically to the WebView to make sure when editing mode is ON,
     * WebView goes above quick actions toolbar.
     */
    private void handleWebViewMargin(){
        TypedArray attrs = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) attrs.getDimension(0, 0);
        attrs.recycle();
        float marginBottomValue = isEditorInitialized ?  (actionBarSize+8):0;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                findViewById(R.id.umEditorHolder).getLayoutParams();
        params.bottomMargin = (int) marginBottomValue;

    }

    /**
     * Inject tinymce to the currently loaded page
     */
    private void initializeEditor(){
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"initEditor",
                ContentEditorActivity.this, (String[]) null);
    }

    /**
     * Process editor state after handling all the unused files and zip file update
     */
    private void postProcessEditor(){
        runOnUiThread(() -> {
            if(openPreview){
                openPreview = false;
                isActivityActive = false;
                args.put(ContentEditorView.EDITOR_REQUEST_URI,
                        umEditorFileHelper.getBaseResourceRequestUrl());
                UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                        args,getApplicationContext());
            }else{
                if(isEditorInitialized){
                    isEditorInitialized = false;
                    isEditorPreview = true;
                    handleBackNavigationIcon();
                    invalidateOptionsMenu();
                    handleQuickActions();
                    viewSwitcher.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL);
                    startEditing.setVisibility(View.VISIBLE);
                    loadIndexFile();
                }else{
                    if(deleteTempDir(new File(umEditorFileHelper.getDestinationDirPath())))finish();
                }
            }
        });
    }

    /**
     * Insert media file to the editor
     * @param uri Uri of the file to be inserted
     * @param fromCamera Flag to indicate if the file was created from the camera
     * @throws IOException Exception thrown when something is wrong
     */
    private void insertMedia(Uri uri, boolean fromCamera) throws IOException {
        progressDialog.setVisibility(View.VISIBLE);
        String mimeType = UmAndroidUriUtil.getMimeType(this,uri);
        File sourceFile;
        File compressedFile;
        if(fromCamera){
            sourceFile = fileFromCamera;
        }else{
            sourceFile = new File(Objects.requireNonNull(UmAndroidUriUtil.getPath(this, uri)));
        }
        if(mimeType.contains("image")){
            compressedFile = new Compressor(this)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(sourceFile);
        }else{
            compressedFile = sourceFile;
        }

        File destination = new File(umEditorFileHelper.getDestinationMediaDirPath(),
                compressedFile.getName().replaceAll("\\s+","_"));
        UMFileUtil.copyFile(new FileInputStream(compressedFile),destination);
        String source = MEDIA_DIRECTORY + destination.getName();
        progressDialog.setVisibility(View.GONE);
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"insertMediaContent",
                this, source,mimeType);
    }

    private boolean deleteTempDir(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteTempDir(file);
            }
        }
        return dir.delete();
    }


    @VisibleForTesting
    public boolean isEditorInitialized(){
        return  isEditorInitialized;
    }


    @VisibleForTesting
    public BottomSheetBehavior getMediaSourceBottomSheetBehavior() {
        return mediaSourceBottomSheetBehavior;
    }

    @VisibleForTesting
    public BottomSheetBehavior getContentOptionsBottomSheetBehavior() {
        return contentOptionsBottomSheetBehavior;
    }

    @VisibleForTesting
    public void insertTestContent(String content){
        presenter.handleFormatTypeClicked(ACTION_INSERT_CONTENT,content);
    }

    @VisibleForTesting
    public void selectAllTestContent(){
        presenter.handleFormatTypeClicked(ACTION_SELECT_ALL,null);
    }

}
