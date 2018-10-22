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
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContentEditorActivity extends UstadBaseActivity implements
        ContentEditorView, FloatingActionMenu.OnMenuToggleListener {

    private static ContentEditorPresenter presenter;
    private BottomSheetBehavior formattingBottomSheetBehavior;
    private BottomSheetBehavior sourceBottomSheetBehavior;
    private FloatingActionMenu mInsertContent;
    private FloatingActionButton mPreviewContent;
    private FloatingActionButton mInsertMultipleChoice;
    private FloatingActionButton mInsertFillBlanks;
    private FloatingActionButton mInsertMultimedia;
    private WebView editorContent;
    private DrawerLayout mContentPageDrawer;
    private boolean isFromFormatting = false;
    public static HashMap<Integer,List<ContentFormat>> mFormatting = new HashMap<>();
    public String returnValue = null;


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
     * Interface class which is used to interact with javascript functions
     */
    private class WebViewInterface {
        @JavascriptInterface
        public void processReturnValue(int index, String value) {
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Returned value from js function: "+value);
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
                        mFormatting.put(formatType, contentFormats);
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
            adapter.setContentFormats(mFormatting.get(formattingType));
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(100));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);
            return  rootView;

        }



        /**
         * Automatically get number of rows to be displayed as per screen size
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
        sourceBottomSheetBehavior =
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

        mInsertContent.setOnClickListener(v -> {
            if(mInsertContent.isOpened()){
                mPreviewContent.hide(true);
            }
        });

        mInsertContent.setOnMenuToggleListener(this);
        sourceBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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
                sourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

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

        editorContent.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        editorContent.clearCache(true);
        editorContent.clearHistory();
        editorContent.addJavascriptInterface(new WebViewInterface(), "android");
        editorContent.loadUrl("file:///android_asset/tinymce/index.html");


    }

    @Override
    public void onStart() {
        super.onStart();
        prepareFormattingTypes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_editor_questions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.content_action_direction)
                .setIcon(mFormatting.get(2).get(0).getFormatIcon());
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.content_action_pages) {
            mContentPageDrawer.openDrawer(GravityCompat.END);
        }else if(itemId == R.id.content_action_format){
            if(mInsertContent.isOpened()){
                isFromFormatting = true;
                mInsertContent.close(true);
            }else{
                boolean isSheetOpened = formattingBottomSheetBehavior.getState()
                        == BottomSheetBehavior.STATE_EXPANDED;
                sourceBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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
                List<ContentFormat> format = mFormatting.get(2);
                format.get(0).setActive(true);
                format.get(0).setFormatIcon(popupItem.getItemId() == R.id.direction_leftToRight ?
                        R.drawable.ic_format_textdirection_l_to_r_white_24dp:
                        R.drawable.ic_format_textdirection_r_to_l_white_24dp);
                mFormatting.put(2,format);
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
        if(mContentPageDrawer.isDrawerOpen(GravityCompat.END)){
            mContentPageDrawer.closeDrawer(GravityCompat.END);
        }else{
            finish();
        }
    }

    @Override
    public void onMenuToggle(boolean opened) {
        if(opened){
            mPreviewContent.hide(true);
        }else{
            if(sourceBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                mPreviewContent.show(true);
            }
            if(isFromFormatting){
                isFromFormatting = false;
                formattingBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @VisibleForTesting
    public BottomSheetBehavior getFormattingBottomSheetBehavior() {
        return formattingBottomSheetBehavior;
    }

    @VisibleForTesting
    public BottomSheetBehavior getSourceBottomSheetBehavior() {
        return sourceBottomSheetBehavior;
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
            returnValue = value;
            UstadMobileSystemImpl.l(UMLog.DEBUG,700,
                    "Value returned from js function "+value);
        });
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

    /**
     * Prepare lis of all formatting types
     */
    private void prepareFormattingTypes(){
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

        ContentFormat directionRtL = new ContentFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                ACTION_TEXT_RECTION,false);

        mDirection.add(directionRtL);
        mFormatting.put(0,mText);
        mFormatting.put(1,mParagraph);
        mFormatting.put(2,mDirection);
    }
}
