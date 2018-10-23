package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private static final int FORMATTING_TEXT_INDEX = 0;
    private static final int FORMATTING_PARAGRAPH_INDEX = 1;
    private static final int FORMATTING_ACTIONS_INDEX = 2;
    private static SparseArray<List<ContentFormat>> formattingList = new SparseArray<>();
    private  String baseUrl = null;

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
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Consoled a message "+result.toString());
            return super.onJsAlert(view, url, message, result);
        }
    }

    private class WebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //request focus to the editor after the page has loaded 100%
            requestFocus();
        }
    }

    /**
     * Class which represents a single formatting type
     */
    public class ContentFormat {

        private int formatIcon;

        private String formatTag;

        private boolean active;

        private int formatId;

        ContentFormat(int formatIcon, String formatTag, boolean active) {
            this.formatIcon = formatIcon;
            this.formatTag = formatTag;
            this.active = active;
        }

        String getFormatTag() {
            return formatTag;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        public void setFormatId(int formatId) {
            this.formatId = formatId;
        }

        public int getFormatId() {
            return formatId;
        }

        public void setFormatIcon(int formatIcon) {
            this.formatIcon = formatIcon;
        }

        public int getFormatIcon() {
            return formatIcon;
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
                mIcon.setImageResource(format.formatIcon);
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

        ViewPager mViewPager = findViewById(R.id.content_types_viewpager);
        TabLayout mTabLayout = findViewById(R.id.content_types_tabs);

        if(toolbar != null){
            toolbar.setTitle("");
        }
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarTitle.setVisibility(View.GONE);


        presenter = new ContentEditorPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
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

        mPreviewContent.setOnClickListener(v -> previewContent());

        mInsertContent.setOnMenuToggleListener(this);

        mediaSourceBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

        formattingBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
        editorContent.setWebViewClient(new WebClient());
        editorContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        editorContent.clearCache(true);
        editorContent.clearHistory();

        startWebServer();

        if(baseUrl != null){
            editorContent.loadUrl(baseUrl+"index.html");
            requestFocus();
        }
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
                presenter.handleFormatTypeClicked(ACTION_TEXT_RECTION,
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
        return super.onOptionsItemSelected(item);
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
    private void startWebServer(){
        EmbeddedHTTPD embeddedHTTPD = new EmbeddedHTTPD(0, this);
        String assetsPath = "/assets-" +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + '/';
        embeddedHTTPD.addRoute( assetsPath+"(.)+",  AndroidAssetsHandler.class, this);
        try {
            embeddedHTTPD.start();
            if(embeddedHTTPD.isAlive()){
                baseUrl =  "http://127.0.0.1:"+embeddedHTTPD.getListeningPort()+assetsPath+"tinymce/";
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
        editorContent.evaluateJavascript(call, value -> {
            processReturnedValue(value);
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Value returned from js function "+value);
        });
    }



    private void processReturnedValue(String returnValue){
        if(returnValue.length() > 5){
            editorContent.loadDataWithBaseURL(null, returnValue,
                    "text/html", "UTF-8", null);
        }
    }


    @Override
    public void setContentBold() {
        callJavaScriptFunction("formatting.textFormattingBold", (String[]) null);
    }

    @Override
    public void setContentItalic() {
        callJavaScriptFunction("formatting.textFormattingItalic", (String) null);
    }

    @Override
    public void setContentUnderlined() {
        callJavaScriptFunction("formatting.textFormattingUnderline", (String) null);
    }

    @Override
    public void setContentStrikeThrough() {
        callJavaScriptFunction("formatting.textFormattingStrikeThrough", (String) null);
    }

    @Override
    public void setContentFontSize(String fontSize) {
        callJavaScriptFunction("formatting.setFontSize", fontSize);
    }

    @Override
    public void setContentSuperscript() {
        callJavaScriptFunction("formatting.textFormattingSuperScript", (String) null);
    }

    @Override
    public void setContentSubScript() {
        callJavaScriptFunction("formatting.textFormattingSubScript", (String) null);
    }

    @Override
    public void setContentJustified() {
        callJavaScriptFunction("formatting.paragraphFullJustification",
                (String) null);
    }

    @Override
    public void setContentCenterAlign() {
        callJavaScriptFunction("formatting.paragraphCenterJustification", (String) null);
    }

    @Override
    public void setContentLeftAlign() {
        callJavaScriptFunction("formatting.paragraphLeftJustification", (String) null);
    }

    @Override
    public void setContentRightAlign() {
        callJavaScriptFunction("formatting.paragraphRightJustification", (String) null);
    }

    @Override
    public void setContentOrderedList() {
        callJavaScriptFunction("formatting.paragraphOrderedListFormatting", (String) null);
    }

    @Override
    public void setContentUnOrderList() {
        callJavaScriptFunction("formatting.paragraphUnOrderedListFormatting", (String) null);
    }

    @Override
    public void setContentIncreaseIndent() {
        callJavaScriptFunction("formatting.paragraphIndent", (String) null);
    }

    @Override
    public void setContentDecreaseIndent() {
        callJavaScriptFunction("formatting.paragraphOutDent", (String) null);
    }

    @Override
    public void setContentRedo() {
        callJavaScriptFunction("formatting.editorActionRedo", (String) null);
    }

    @Override
    public void setContentUndo() {
        callJavaScriptFunction("formatting.editorActionUndo", (String) null);
    }

    @Override
    public void setContentTextDirection(boolean right) {
        callJavaScriptFunction(right ? "formatting.textDirectionRightToLeft":
                "formatting.textDirectionLeftToRight", (String) null);
        invalidateOptionsMenu();
    }

    private void previewContent(){
        callJavaScriptFunction("formatting.getContent", (String) null);
    }

    private void requestFocus(){
        callJavaScriptFunction("formatting.requestFocus", (String) null);
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
                ACTION_TEXT_RECTION,false));

        formattingList.put(FORMATTING_TEXT_INDEX,mText);
        formattingList.put(FORMATTING_PARAGRAPH_INDEX,mParagraph);
        formattingList.put(FORMATTING_ACTIONS_INDEX,mDirection);
    }
}
