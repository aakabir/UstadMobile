package com.ustadmobile.port.android.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEditorPageListView;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.ContentPreviewView;
import com.ustadmobile.lib.util.Base64Coder;
import com.ustadmobile.port.android.impl.http.AndroidAssetsHandler;
import com.ustadmobile.port.android.umeditor.UmAndroidUriUtil;
import com.ustadmobile.port.android.umeditor.UmEditorActionView;
import com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher;
import com.ustadmobile.port.android.umeditor.UmEditorPopUpView;
import com.ustadmobile.port.android.umeditor.UmEditorWebView;
import com.ustadmobile.port.android.umeditor.UmFormat;
import com.ustadmobile.port.android.umeditor.UmFormatStateChangeListener;
import com.ustadmobile.port.android.umeditor.UmGridSpacingItemDecoration;
import com.ustadmobile.port.android.umeditor.UmPageActionListener;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorChromeClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorClient;
import com.ustadmobile.port.android.umeditor.UmWebContentEditorInterface;
import com.ustadmobile.port.android.umeditor.UmWebJsResponse;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import id.zelory.compressor.Compressor;

import static com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.ANIMATED_CONTENT_OPTION_PANEL;
import static com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.ANIMATED_SOFT_KEYBOARD_PANEL;
import static com.ustadmobile.port.android.umeditor.UmEditorAnimatedViewSwitcher.MAX_SOFT_KEYBOARD_DELAY;
import static com.ustadmobile.port.android.umeditor.UmWebContentEditorClient.executeJsFunction;
import static com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper.EDITOR_BASE_DIR_NAME;
import static com.ustadmobile.port.sharedse.contenteditor.UmEditorFileHelper.MEDIA_DIRECTORY;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView,
        UmWebContentEditorChromeClient.JsLoadingCallback, UmEditorActionView.OnQuickActionMenuItemClicked,
        UmFormatStateChangeListener,UmEditorFileHelper.ZipFileTaskProgressListener,
        UmEditorAnimatedViewSwitcher.OnAnimatedViewsClosedListener, UmPageActionListener {

    private static ContentEditorPresenter presenter;

    private UmEditorFileHelper umEditorFileHelper;

    private UmEditorAnimatedViewSwitcher viewSwitcher;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private BottomSheetBehavior contentOptionsBottomSheetBehavior;

    private AppBarLayout umBottomToolbarHolder;

    private UmEditorActionView umEditorActionView;

    private UmEditorWebView umEditorWebView;

    private  ContentEditorPageListFragment pageListFragment;

    private View docNotFoundView;

    private UmEditorActionView toolbar;

    private FloatingActionButton startEditing;

    private Hashtable args = null;

    private Uri cameraMedia;

    private File fileFromCamera;

    private static final String PAGE_NAME_TAG = "page_name";

    private static  int displayWidth = 0;

    private ProgressBar progressDialog;

    public static final int CAMERA_IMAGE_CAPTURE_REQUEST = 900;

    public static final int CAMERA_PERMISSION_REQUEST = 901;

    private static final int FILE_BROWSING_REQUEST = 902;

    private View blankDocumentContainer;

    private UmFormatHelper umFormatHelper;

    private static final String EDITOR_METHOD_PREFIX = "UmContentEditorCore.";


    /**
     * Class which represent a format control state
     */
    private class UmFormatState {
        //Format Command
        private String command;

        //Format command state (Activated / Deactivate)
        private boolean status;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public boolean isActive() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }


    /**
     * Class which handles all formatting from preparing and updating them when necessary.
     */
    public class UmFormatHelper {

        /**
         * Flag to indicate all text formats
         */
        private static final int FORMATTING_TEXT_INDEX = 0;

        /**
         * Flag to indicate all paragraph formats
         */
        private static final int FORMATTING_PARAGRAPH_INDEX = 1;

        /**
         * Flag to indicate all font formats
         */
        private static final int FORMATTING_FONT_INDEX = 2;

        /**
         * Flag to indicate all toolbar action formats
         */
        public static final int ACTIONS_TOOLBAR_INDEX = 3;

        /**
         * Flag to indicate all language directionality formats
         */
        private static final int LANGUAGE_DIRECTIONALITY = 4;

        private List<UmFormat> formatList;

        private List<UmFormat> quickActionList;

        private List<UmFormatStateChangeListener> dispatcherList = new CopyOnWriteArrayList<>();

        UmFormatHelper(){
            formatList = prepareFormattingList();
            quickActionList = prepareQuickActionFormats();
        }

        /**
         * Prepare all UmEditor format
         * @return list of all formats.
         */
        private List<UmFormat> prepareFormattingList(){
            List<UmFormat> mText = new ArrayList<>();
            List<UmFormat> mDirection = new ArrayList<>();
            List<UmFormat> mParagraph = new ArrayList<>();
            List<UmFormat> mToolbar = new ArrayList<>();

            List<UmFormat> mFont = new ArrayList<>();

            mText.add(new UmFormat(R.drawable.ic_format_bold_black_24dp, TEXT_FORMAT_TYPE_BOLD,
                    false,FORMATTING_TEXT_INDEX, R.id.content_action_bold));
            mText.add(new UmFormat(R.drawable.ic_format_italic_black_24dp, TEXT_FORMAT_TYPE_ITALIC,
                    false,FORMATTING_TEXT_INDEX,R.id.content_action_italic));
            mText.add(new UmFormat(R.drawable.ic_format_underlined_black_24dp,
                    TEXT_FORMAT_TYPE_UNDERLINE,false,FORMATTING_TEXT_INDEX,
                    R.id.content_action_underline));
            mText.add(new UmFormat(R.drawable.ic_format_strikethrough_black_24dp,
                    TEXT_FORMAT_TYPE_STRIKE,false,FORMATTING_TEXT_INDEX,
                    R.id.content_action_strike_through));
            mText.add(new UmFormat(R.drawable.ic_number_superscript,
                    TEXT_FORMAT_TYPE_SUP,false,FORMATTING_TEXT_INDEX));
            mText.add(new UmFormat(R.drawable.ic_number_subscript,
                    TEXT_FORMAT_TYPE_SUB,false,FORMATTING_TEXT_INDEX));
            mText.add(new UmFormat(R.drawable.ic_format_size_black_24dp,
                    TEXT_FORMAT_TYPE_FONT,false,FORMATTING_TEXT_INDEX));

            mParagraph.add(new UmFormat(R.drawable.ic_format_align_justify_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_JUSTIFY, false,FORMATTING_PARAGRAPH_INDEX));
            mParagraph.add(new UmFormat(R.drawable.ic_format_align_right_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_RIGHT,false,FORMATTING_PARAGRAPH_INDEX));
            mParagraph.add(new UmFormat(R.drawable.ic_format_align_center_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_CENTER,false,FORMATTING_PARAGRAPH_INDEX));
            mParagraph.add(new UmFormat(R.drawable.ic_format_align_left_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_LEFT,false,FORMATTING_PARAGRAPH_INDEX));
            mParagraph.add(new UmFormat(R.drawable.ic_format_list_numbered_black_24dp,
                    PARAGRAPH_FORMAT_LIST_ORDERED,false,FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_ordered_list));
            mParagraph.add(new UmFormat(R.drawable.ic_format_list_bulleted_black_24dp,
                    PARAGRAPH_FORMAT_LIST_UNORDERED,false,FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_uordered_list));
            mParagraph.add(new UmFormat(R.drawable.ic_format_indent_increase_black_24dp,
                    PARAGRAPH_FORMAT_INDENT_INCREASE,false,FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_indent));
            mParagraph.add(new UmFormat(R.drawable.ic_format_indent_decrease_black_24dp,
                    PARAGRAPH_FORMAT_INDENT_DECREASE,false,FORMATTING_PARAGRAPH_INDEX,
                    R.id.content_action_outdent));

            mDirection.add(new UmFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                    ACTION_TEXT_DIRECTION_LTR,true,LANGUAGE_DIRECTIONALITY,
                    R.id.direction_leftToRight, R.string.content_direction_ltr));
            mDirection.add(new UmFormat(R.drawable.ic_format_textdirection_r_to_l_white_24dp,
                    ACTION_TEXT_DIRECTION_RTL,false,LANGUAGE_DIRECTIONALITY,
                    R.id.direction_rightToLeft,R.string.content_direction_rtl));

            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 8,R.string.content_font_8));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 10,R.string.content_font_10));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 12,R.string.content_font_12));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 14,R.string.content_font_14));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 18, R.string.content_font_18));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 24,R.string.content_font_24));
            mFont.add(new UmFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                    FORMATTING_FONT_INDEX, 36,R.string.content_font_36));


            mToolbar.add(new UmFormat(R.drawable.ic_undo_white_24dp,
                    ACTION_UNDO,false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_undo));
            mToolbar.add(new UmFormat(R.drawable.ic_redo_white_24dp,
                    ACTION_REDO,false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_redo));
            mToolbar.add(new UmFormat(R.drawable.ic_text_format_white_24dp,
                    "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_format));
            mToolbar.add(new UmFormat(R.drawable.fab_add,
                    "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_insert));
            mToolbar.add(new UmFormat(R.drawable.ic_document_preview,
                    "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_preview));
            mToolbar.add(new UmFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                    ACTION_TEXT_DIRECTION_LTR,false,ACTIONS_TOOLBAR_INDEX,
                    R.id.content_action_direction));
            mToolbar.add(new UmFormat(R.drawable.ic_piled_pages,
                    "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_pages));

            List<UmFormat> allFormats = new ArrayList<>();
            allFormats.addAll(mText);
            allFormats.addAll(mParagraph);
            allFormats.addAll(mDirection);
            allFormats.addAll(mToolbar);
            allFormats.addAll(mFont);
            return allFormats;
        }

        private List<UmFormat> prepareQuickActionFormats(){
            List<UmFormat> quickActions = new ArrayList<>();
            try{
                quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_BOLD));
                quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_ITALIC));
                quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_UNDERLINE));
                quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_STRIKE));
                quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_LIST_ORDERED));
                quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_LIST_UNORDERED));
                quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_INDENT_INCREASE));
                quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_INDENT_DECREASE));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            return quickActions;
        }


        /**
         * Construct quick action menu items
         * @return list of all quick action menus
         */
        public List<UmFormat> getQuickActions(){
            return quickActionList;
        }


        /**
         * Get list of all formats by type
         * @param formatType type to be found
         * @return found list of all formats of
         */
        public List<UmFormat> getFormatListByType(int formatType) {
            List<UmFormat> formats = new ArrayList<>();
            for(UmFormat format: formatList){
                if(format.getFormatType() == formatType){
                    formats.add(format);
                }
            }
            return formats;
        }

        /**
         * Get toolbar UmFormat from the list by its ID and type, some
         * @param formatId Format unique id
         * @return UmFormat found
         */
        UmFormat getFormatById(int formatId) {
            UmFormat umFormat = null;
            for(UmFormat format: getFormatListByType(ACTIONS_TOOLBAR_INDEX)){
                if(format.getFormatId() == formatId){
                    umFormat = format;
                    break;
                }
            }
            return umFormat;
        }

        /**
         * Get UmFormat by its command
         * @param command formatting command to be found
         * @return found content format
         */
        UmFormat getFormatByCommand(String command){
            UmFormat umFormat = null;
            for(UmFormat format: formatList){
                if(format.getFormatCommand().equals(command)){
                    umFormat = format;
                    break;
                }
            }
            return umFormat;
        }

        /**
         * Update content format status
         * @param umFormat updated content format
         */
        void updateFormat(UmFormat umFormat) {
            for(UmFormat format: formatList){
                if(format.getFormatCommand().equals(umFormat.getFormatCommand())){
                    int formatIndex = formatList.indexOf(format);
                    formatList.get(formatIndex).setActive(umFormat.isActive());
                    break;
                }
            }
            dispatchStateChangeUpdate(umFormat);
        }


        /**
         * Get list of all directionality formatting
         * @param activeFormat Directionality format to be activated
         * @return List of all formats
         */
        List<UmFormat> getLanguageDirectionalityList(UmFormat activeFormat){
            List<UmFormat> directionality = getFormatListByType(LANGUAGE_DIRECTIONALITY);
            for(UmFormat format: directionality){
                if(activeFormat != null){
                    directionality.get(directionality.indexOf(format))
                            .setActive(format.getFormatId() == activeFormat.getFormatId());

                }
            }
            return directionality;
        }

        /**
         * Get all font format list
         * @param activeFormat Font format to be activated
         * @return List of all fonts
         */
        List<UmFormat> getFontList(UmFormat activeFormat){
            List<UmFormat> fontList = getFormatListByType(FORMATTING_FONT_INDEX);
            for(UmFormat format: fontList){
                if(activeFormat != null){
                    fontList.get(fontList.indexOf(format))
                            .setActive(format.getFormatId() == activeFormat.getFormatId());
                }
            }
            return fontList;
        }

        /**
         * Prevent all justification to be active at the same time, only one type at a time.
         * @param command current active justification command.
         */
        void updateOtherJustificationFormatState(String command){
            String mTag = "Justify";
            List<UmFormat> paragraphFormatList = getFormatListByType(FORMATTING_PARAGRAPH_INDEX);
            for(UmFormat format: paragraphFormatList){
                if(format.getFormatCommand().contains(mTag) && command.contains(mTag)
                        && !format.getFormatCommand().equals(command)){
                    int index = paragraphFormatList.indexOf(format);
                    format.setActive(false);
                    formatList.set(index, format);
                }
            }
        }

        /**
         * Prevent all list types to active at the same time, only one at a time.
         * @param command current active list type command.
         */
        void updateOtherListFormatState(String command){
            String mTag = "List";
            List<UmFormat> listOrdersTypes = listOrderFormats();
            for(UmFormat format: listOrdersTypes){
                if(format.getFormatCommand().contains(mTag) && command.contains(mTag)
                        && !format.getFormatCommand().equals(command)){
                    int index = listOrdersTypes.indexOf(format);
                    format.setActive(false);
                    formatList.set(index, format);
                }
            }
        }

        private List<UmFormat> listOrderFormats(){
            List<UmFormat> listOrders = new ArrayList<>();
            for(UmFormat format: formatList){
                if(format.getFormatCommand().contains("List")){
                    listOrders.add(format);
                }
            }
            return listOrders;
        }

        void setStateChangeListener(UmFormatStateChangeListener stateDispatcher){
            if(dispatcherList != null){
                dispatcherList.add(stateDispatcher);
            }
        }

        /**
         * Dispatch update to both content formatting panels and quick actions.
         * @param format updated format to be dispatched
         */
        private void dispatchStateChangeUpdate(UmFormat format){
            for(UmFormatStateChangeListener dispatcher: dispatcherList){
                dispatcher.onStateChanged(format);
            }
        }

        /**
         * Delete all listeners from the list on activity destroy
         */
        public void destroy(){
            if(dispatcherList != null && dispatcherList.size()>0){
                dispatcherList.clear();
            }
        }


        /**
         * Check if the format is valid for highlight or not, for those format which
         * deals with increment like indentation they are not fit for active state
         * @param command format command to be checked for validity
         * @return True if valid otherwise False.
         */
        public boolean isTobeHighlighted(String command){
            return !command.equals(TEXT_FORMAT_TYPE_FONT)
                    && !command.equals(PARAGRAPH_FORMAT_INDENT_DECREASE)
                    && !command.equals(PARAGRAPH_FORMAT_INDENT_INCREASE);
        }
    }


    /**
     * Content formatting adapter which handles formatting sections
     */
    private class ContentFormattingPagerAdapter extends FragmentStatePagerAdapter {

        String [] contentFormattingTypeLabel = new String[]{
          getResources().getString(R.string.content_format_text),
          getResources().getString(R.string.content_format_paragraph)
        };

        ContentFormattingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FormattingFragment fragment = new FormattingFragment().newInstance(position);
            fragment.setUmFormatHelper(umFormatHelper);
            return fragment;
        }

        @Override
        public int getCount() {
            return contentFormattingTypeLabel.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return contentFormattingTypeLabel[position];
        }
    }


    /**
     * Fragment to handle bottom formatting type  UI (Text formatting & Paragraph formatting)
     */
    public static class FormattingFragment extends Fragment implements UmFormatStateChangeListener {

        private FormatsAdapter adapter;

        private int formattingType;

        private UmFormatHelper umFormatHelper;


        class FormatsAdapter extends RecyclerView.Adapter<FormatsAdapter.FormatViewHolder>{

            private List<UmFormat> umFormats = new ArrayList<>();

            void setUmFormats(List<UmFormat> umFormats) {
                this.umFormats = umFormats;
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
                UmFormat format = umFormats.get(position);
                ImageView mIcon = holder.itemView.findViewById(R.id.format_icon);
                RelativeLayout mLayout = holder.itemView.findViewById(R.id.format_holder);
                mIcon.setImageResource(format.getFormatIcon());
                changeState(mIcon,mLayout,format.isActive());
                if(!umFormatHelper.isTobeHighlighted(format.getFormatCommand())){
                    changeState(mIcon,mLayout,false);
                }
                mLayout.setOnClickListener(v -> {
                    if(!format.getFormatCommand().equals(TEXT_FORMAT_TYPE_FONT)){
                        changeState(mIcon,mLayout,true);
                        umFormatHelper.updateOtherJustificationFormatState(format.getFormatCommand());
                        umFormatHelper.updateOtherListFormatState(format.getFormatCommand());
                        presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
                        notifyDataSetChanged();
                    }else{

                        UmEditorPopUpView  popUpView =
                                new UmEditorPopUpView(getActivity(), holder.itemView)
                                        .setMenuList(umFormatHelper.getFontList(null))
                                        .showIcons(false)
                                        .setWidthDimen(displayWidth,false);
                        popUpView.showWithListener(menu -> {
                            presenter.handleFormatTypeClicked(menu.getFormatCommand(),
                                    String.valueOf(menu.getFormatId()));
                            popUpView.setMenuList(umFormatHelper.getFontList(menu));
                        });
                    }
                });
            }

            /**
             * Change state of the view based status.
             */
            private void changeState(ImageView imageIcon, RelativeLayout iconHolder,
                                     boolean isActivated){
                imageIcon.setColorFilter(ContextCompat.getColor(getContext(),
                        isActivated ? R.color.icons:R.color.text_secondary));
                iconHolder.setBackgroundColor(ContextCompat.getColor(getContext(),
                        isActivated ? R.color.content_icon_active:R.color.icons));
            }

            @Override
            public int getItemCount() {
                return umFormats != null ? umFormats.size():0;
            }

            class FormatViewHolder extends RecyclerView.ViewHolder{
                FormatViewHolder(View itemView) {
                    super(itemView);
                }
            }

        }

        /**
         * Create new instance of a content formatting fragment
         */
        public FormattingFragment newInstance(int formatType) {
            this.formattingType = formatType;
            return this;
        }

        public void setUmFormatHelper(UmFormatHelper umFormatHelper){
            this.umFormatHelper = umFormatHelper;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
             umFormatHelper.setStateChangeListener(this);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_content_formatting,
                    container, false);
            RecyclerView mRecyclerView = rootView.findViewById(R.id.formats_list);
            adapter = new FormatsAdapter();
            adapter.setUmFormats(umFormatHelper.getFormatListByType(formattingType));
            int itemWidth = 60;
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(itemWidth));
            mRecyclerView.addItemDecoration(new UmGridSpacingItemDecoration(getSpanCount(itemWidth)
                    ,dpToPx(10), true));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);
            return  rootView;

        }

        @Override
        public void onStateChanged(UmFormat format) {
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }


        /**
         * Automatically gets number of rows to be displayed as per screen size
         * @param width Width of a single item
         * @return number of columns which will fit the screen
         */
        int getSpanCount(@NonNull Integer width){
            Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            float density = getActivity().getResources().getDisplayMetrics().density;
            float dpWidth = outMetrics.widthPixels / density;
            return Math.round(dpWidth/width);
        }

        /**
         * Convert density pixel dimension to pixel
         * @param dp dimension in density pixels
         * @return Pixel dimension equivalent to the density pixels.
         */
        int dpToPx(int dp) {
            try{
                Resources r = Objects.requireNonNull(getActivity()).getResources();
                return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dp, r.getDisplayMetrics()));
            }catch (NullPointerException e){
                return dp;
            }
        }

    }


    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
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
        umEditorWebView = findViewById(R.id.editor_content);
        progressDialog = findViewById(R.id.progressBar);
        startEditing = findViewById(R.id.btn_start_editing);
        docNotFoundView = findViewById(R.id.doc_not_found);
        RelativeLayout mFromCamera = findViewById(R.id.multimedia_from_camera);
        RelativeLayout mFromDevice = findViewById(R.id.multimedia_from_device);
        View rootView = findViewById(R.id.coordinationLayout);
        TextView blankDocTitle = findViewById(R.id.blank_doc_title);
        TextView bdClickLabel = findViewById(R.id.click_label);
        TextView bdCreateLabel = findViewById(R.id.editing_label);
        blankDocumentContainer = findViewById(R.id.new_doc_container);
        umBottomToolbarHolder = findViewById(R.id.um_appbar_bottom);
        umEditorActionView = findViewById(R.id.um_toolbar_bottom);

        umFormatHelper = new UmFormatHelper();
        umFormatHelper.setStateChangeListener(this);
        umEditorWebView.setBackgroundColor(Color.TRANSPARENT);


        displayWidth = getDisplayWidth();
        viewSwitcher = UmEditorAnimatedViewSwitcher.getInstance()
                        .with(this,this)
                        .setViews(rootView, umEditorWebView,contentOptionsBottomSheetBehavior,
                                formattingBottomSheetBehavior, mediaSourceBottomSheetBehavior);
        viewSwitcher.closeActivity(false);

        ViewPager mViewPager = findViewById(R.id.content_types_viewpager);
        TabLayout mTabLayout = findViewById(R.id.content_types_tabs);


        umEditorFileHelper = new UmEditorFileHelper();
        umEditorFileHelper.init(this);
        umEditorFileHelper.setZipTaskProgressListener(this);
        umEditorFileHelper.addBaseAssetHandler(AndroidAssetsHandler.class);

        args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        presenter = new ContentEditorPresenter(this,args,this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
        presenter.handleContentEntryFileStatus();

        if(toolbar != null){
            toolbar.setTitle("");
            handleBackNavigationIcon();

            //Set action menus above the keyboard when opened
            toolbar.setUmFormatHelper(umFormatHelper);
            toolbar.inflateMenu(R.menu.menu_content_editor_top_actions,false);
            toolbar.setQuickActionMenuItemClickListener(this);
            toolbar.setMenuVisible(false);
            toolbar.setNavigationOnClickListener(v -> {
                if(presenter.isFileNotFound()){
                    finish();
                }else{
                    viewSwitcher.closeActivity(true);
                }
            });
            umEditorActionView.setUmFormatHelper(umFormatHelper);
            umEditorActionView.inflateMenu(R.menu.menu_content_editor_quick_actions,true);
            umEditorActionView.setQuickActionMenuItemClickListener(this);
        }



        handleClipBoardContentChanges();

        progressDialog.setMax(100);
        progressDialog.setProgress(0);

        findViewById(R.id.action_close_tab_formats).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL));
        findViewById(R.id.action_close_tab_multimedia_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL));
        findViewById(R.id.action_close_tab_content_options).setOnClickListener(v ->
            viewSwitcher.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL));

        mFromDevice.setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
            startFileBrowser();
        });

        startEditing.setOnClickListener(v -> {
            progressDialog.setVisibility(View.VISIBLE);
            if(presenter.isInEditorPreview()){
                handleSelectedPage();
            }else{
                initializeEditor();
            }

        });

        findViewById(R.id.content_option_page).setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(ANIMATED_CONTENT_OPTION_PANEL);
        });

        mFromCamera.setOnClickListener(v -> {
            viewSwitcher.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContentEditorActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                return;
            }
            showMediaTypeDialog();
        });


        mInsertMultimedia.setOnClickListener(v ->{
            presenter.setMultimediaFilePicker(true);
            viewSwitcher.animateView(UmEditorAnimatedViewSwitcher.ANIMATED_MEDIA_TYPE_PANEL);
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
        umEditorWebView.setWebChromeClient(new UmWebContentEditorChromeClient(this));
        umEditorWebView.addJavascriptInterface(
                new UmWebContentEditorInterface(this,this),"UmContentEditor");
        umEditorWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContentEditorPageListFragment) {
            ContentEditorPageListFragment headlinesFragment
                    = (ContentEditorPageListFragment) fragment;
            headlinesFragment.setPageActionListener(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_content_editor_top_actions, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PAGE_NAME_TAG, presenter.getSelectedPageToLoad());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(presenter != null){
            presenter.setSelectedPageToLoad(savedInstanceState.getString(PAGE_NAME_TAG));
        }
    }

    @Override
    public void onStateChanged(UmFormat format) {
        if(format.getFormatId() != 0){
            umEditorActionView.updateMenu();
        }
    }

    @Override
    public void onCallbackReceived(String value) {
        if(value.contains("action")){
            UmWebJsResponse callback = new Gson().fromJson(value,UmWebJsResponse.class);
            processJsCallLogValues(callback);
        }
    }


    @Override
    public void onAnimatedViewsClosed(boolean closed) {
        if(closed){
            if(presenter.isEditingModeOn()){
                handleUpdateFile();
            }
        }
    }

    @Override
    public void onFocusRequested() { }

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
        runOnUiThread(() -> {
            setDefaultLanguage();
            progressDialog.setVisibility(View.GONE);
        });

    }

    @Override
    public void onPageFinishedLoading() {
        progressDialog.setVisibility(View.GONE);
        if(presenter.isInEditorPreview()){
            presenter.setInEditorPreview(false);
        }

        if(presenter.isEditorInitialized()){
            initializeEditor();
        }
    }

    /**
     * Process values returned from JS calls
     * @param callback object returned
     */
    private void processJsCallLogValues(UmWebJsResponse callback){

        String content = Base64Coder.decodeString(callback.getContent());

        switch (callback.getAction()){
            //on editor initialized
            case ACTION_INIT_EDITOR:
                presenter.setEditorInitialized(Boolean.parseBoolean(callback.getContent()));
                if(presenter.isEditorInitialized()){

                    handleWebViewMargin();
                    toolbar.setMenuVisible(true);
                    umEditorWebView.postDelayed(() ->
                            viewSwitcher.animateView(ANIMATED_SOFT_KEYBOARD_PANEL),
                            MAX_SOFT_KEYBOARD_DELAY);
                   umEditorWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_BACK));
                }
                handleBackNavigationIcon();
                startEditing.setVisibility(View.GONE);
                progressDialog.setVisibility(View.GONE);
                blankDocumentContainer.setVisibility(View.GONE);
                viewSwitcher.setEditorActivated(presenter.isEditorInitialized());
                handleQuickActions();
                break;
            case ACTION_SAVE_CONTENT:
                try{
                    Document indexFile = getLoadedPageContent();
                    Elements contentContainer = indexFile.select("#"+EDITOR_BASE_DIR_NAME);
                    contentContainer.first().html(content);
                    String utf8Content = URLDecoder.decode(content,"UTF-8");
                    UstadMobileSystemImpl.l(UMLog.DEBUG,700, utf8Content);
                    //Update index.html file
                    UMFileUtil.writeToFile(new File(umEditorFileHelper.getEpubFilesDirectoryPath(),
                            presenter.getSelectedPageToLoad()),indexFile.html());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            //Callback received upon completion of all controls status check
            case ACTION_CONTROLS_ACTIVATED:
                Gson gson = new Gson();
                UmFormatState[] statuses = gson.fromJson(content,UmFormatState[].class);
                for(UmFormatState status: statuses){
                    UmFormat format = umFormatHelper.getFormatByCommand(status.getCommand());
                    if(format != null){
                        format.setActive(status.isActive());
                        umFormatHelper.updateFormat(format);
                    }
                }

                break;
            case ACTION_CONTENT_CUT:
                try{
                    String utf8Content = URLDecoder.decode(content,"UTF-8");
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(EDITOR_BASE_DIR_NAME, utf8Content);
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_CHECK_COMPLETED:
                if(umEditorWebView.getInputConnectionWrapper() != null){
                    umEditorWebView.getInputConnectionWrapper().setProtectedElement(Boolean.valueOf(content));
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
    protected void onResume() {
        super.onResume();
        presenter.setEditingModeOn(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewSwitcher.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL);
    }

    @Override
    public void onDestroy() {
        if(umFormatHelper != null){
            umFormatHelper.destroy();
        }
        if(viewSwitcher != null){
            viewSwitcher.closeActivity(true);
        }
        super.onDestroy();
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
                                if((!presenter.isOpenPreview()
                                        || !presenter.isMultimediaFilePicker())
                                        && presenter.isEditorInitialized()){
                                    postProcessEditor();
                                }else{
                                    if(presenter.isEditingModeOn()
                                            && presenter.isEditorInitialized()){
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
                public void onFailure(Throwable exception) { }
            });
        }
    }

    @Override
    public void onBackPressed() {
       viewSwitcher.closeActivity(true);
    }

    @Override
    public void onQuickMenuItemClicked(String command) {
        UmFormat format = umFormatHelper.getFormatByCommand(command);
        if(format != null){
            presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onQuickMenuViewClicked(int itemId) {
        if (itemId == R.id.content_action_pages) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            pageListFragment = new ContentEditorPageListFragment();
            pageListFragment.setPageList(umEditorFileHelper.
                    getEpubNavDocument().getToc().getChildren());
            pageListFragment.setDocumentTitle(umEditorFileHelper.getEpubOpfDocument().getTitle());
            pageListFragment.show(transaction, ContentEditorPageListView.TAG);

        }else if(itemId == R.id.content_action_format){

            viewSwitcher.animateView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL);

        }else if(itemId == R.id.content_action_preview){
            presenter.setOpenPreview(true);
            presenter.setMultimediaFilePicker(false);
            viewSwitcher.closeActivity(true);

        }else if(itemId == R.id.content_action_insert){
            viewSwitcher.animateView(ANIMATED_CONTENT_OPTION_PANEL);

        }else if(itemId == R.id.content_action_undo){
            presenter.handleFormatTypeClicked(ACTION_UNDO,null);

        }else if(itemId == R.id.content_action_redo){
            presenter.handleFormatTypeClicked(ACTION_REDO,null);

        }else if(itemId == R.id.content_action_direction){
            View menuItemView = findViewById(R.id.content_action_direction);
            UmEditorPopUpView  popUpView = new UmEditorPopUpView(this, menuItemView)
                    .setMenuList(umFormatHelper.getLanguageDirectionalityList(null))
                    .setWidthDimen(displayWidth,true);
            popUpView.showWithListener(format -> {
                presenter.handleFormatTypeClicked(format.getFormatCommand(),null);
                popUpView.setMenuList(umFormatHelper.getLanguageDirectionalityList(format));
                UmFormat toolBarAction = umFormatHelper.getFormatById(itemId);
                if(toolBarAction != null){
                    toolBarAction.setFormatIcon(format.getFormatIcon());
                    toolbar.updateMenu(toolBarAction);
                }
            });

        }
    }

    /**
     * Show /Hide quick action menus on top of the keyboard
     */
    private void handleQuickActions(){
        umBottomToolbarHolder.setVisibility(presenter.isEditorInitialized() ? View.VISIBLE:View.GONE);
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
    private Document getLoadedPageContent(){
        try {
            File indexFile = new File(umEditorFileHelper.getEpubFilesDirectoryPath(),
                    presenter.getSelectedPageToLoad());
            return  Jsoup.parse(UMFileUtil.readTextFile(indexFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * Check if the document is empty, this helps to showWithListener the empty document placeholder view
     * @return True if the document is empty otherwise false.
     */
    private boolean isDocumentEmpty(){
        Document loadedPage = getLoadedPageContent();
        Elements innerContent = loadedPage.select("#"+EDITOR_BASE_DIR_NAME);
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
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
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

    private int getDisplayWidth(){
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        return Math.round(outMetrics.widthPixels / density);
    }


    /**
     * Change toolbar navigation icon based on editor state
     */
    private void handleBackNavigationIcon(){
        toolbar.setNavigationIcon(presenter.isEditorInitialized() ?
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
                viewSwitcher.closeAnimatedView(UmEditorAnimatedViewSwitcher.ANIMATED_FORMATTING_PANEL));
    }


    /**
     * Set bottom margin dynamically to the WebView to make sure it starts on top of the quick
     * action menus when editing mode is ON
     */
    private void handleWebViewMargin(){
        TypedArray attrs = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) attrs.getDimension(0, 0);
        attrs.recycle();
        float marginBottomValue = presenter.isEditorInitialized() ?  (actionBarSize+8):0;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                findViewById(R.id.umEditorHolder).getLayoutParams();
        params.bottomMargin = (int) marginBottomValue;

    }

    /**
     * Inject tinymce to the currently loaded page
     */
    private void initializeEditor(){
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"initEditor",
                ContentEditorActivity.this);
    }

    /**
     * Process editor state after handling all the unused files and zip file update
     */
    private void postProcessEditor(){
        runOnUiThread(() -> {
            if(presenter.isOpenPreview()){
                presenter.setOpenPreview(false);
                presenter.setEditingModeOn(false);
                args.put(ContentEditorView.EDITOR_REQUEST_URI,
                        umEditorFileHelper.getResourceAccessibleUrl());
                UstadMobileSystemImpl.getInstance().go(ContentPreviewView.VIEW_NAME,
                        args,getApplicationContext());
            }else{
                if(presenter.isEditorInitialized()){
                    presenter.setEditorInitialized(false);
                    presenter.setInEditorPreview(true);
                    handleBackNavigationIcon();
                    invalidateOptionsMenu();
                    handleQuickActions();
                    toolbar.setMenuVisible(false);
                    viewSwitcher.closeAnimatedView(ANIMATED_SOFT_KEYBOARD_PANEL);
                    startEditing.setVisibility(View.VISIBLE);
                    handleSelectedPage();
                }else{
                    if(deleteTempDir(new File(umEditorFileHelper.getMountedFileTempDirectoryPath()))){
                        finish();
                    }
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

        File destination = new File(umEditorFileHelper.getMediaDirectory(),
                compressedFile.getName().replaceAll("\\s+","_").replace("-","_"));
        if(UMFileUtil.copyFile(new FileInputStream(compressedFile),destination)){
            String source = MEDIA_DIRECTORY + destination.getName();
            progressDialog.setVisibility(View.GONE);
            executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"insertMediaContent",
                    this, source,mimeType);
        }
    }

    /**
     * Delete temporary direction in which zip was mounted
     * @param dir File directory.
     * @return true if the directory was deleted successfully otherwise false
     */
    private boolean deleteTempDir(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteTempDir(file);
                }
                if (!file.delete()) {
                    success = false;
                }
            }
        }
        if(dir.exists()){
            success = dir.delete();
        }
        return success;
    }


    @Override
    public UmEditorFileHelper getFileHelper() {
        return umEditorFileHelper;
    }

    @Override
    public void showNotFoundErrorMessage() {
        umEditorWebView.setVisibility(View.GONE);
        docNotFoundView.setVisibility(View.VISIBLE);
        startEditing.setVisibility(View.GONE);
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
    public void setContentTextDirection(String command) {
        executeJsFunction(umEditorWebView,EDITOR_METHOD_PREFIX+(
                command.equals(ACTION_TEXT_DIRECTION_RTL) ? "textDirectionRightToLeft":
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
    public void handleSelectedPage() {
        umEditorWebView.setWebViewClient(new UmWebContentEditorClient(this));
        String urlToLoad = UMFileUtil.joinPaths(umEditorFileHelper.getMountedFileAccessibleUrl(),
                presenter.getSelectedPageToLoad());
        args.put(ContentEditorView.EDITOR_PREVIEW_PATH, urlToLoad);
        umEditorWebView.clearCache(true);
        umEditorWebView.clearHistory();
        umEditorWebView.loadUrl(urlToLoad);

        if(!presenter.isEditorInitialized()){
            handleBlankDocumentView();
        }
    }


    public void setDefaultLanguage() {
        executeJsFunction(umEditorWebView, EDITOR_METHOD_PREFIX+"setDefaultLanguage",this,"en","false");
    }

    @VisibleForTesting
    public boolean isEditorInitialized(){
        return  presenter.isEditorInitialized();
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


    @Override
    public void onOrderChanged(List<EpubNavItem> newPageList) {
        umEditorFileHelper.changePageOrder(newPageList, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailure(Throwable exception) {
                if(exception != null){
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPageSelected(String pageHref) {
        presenter.setSelectedPageToLoad(pageHref);
        handleSelectedPage();

    }

    @Override
    public void onPageUpdate(EpubNavItem pageItem) {
        umEditorFileHelper.updatePage(pageItem, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                pageListFragment.setPageList(umEditorFileHelper.
                        getEpubNavDocument().getToc().getChildren());
            }

            @Override
            public void onFailure(Throwable exception) {
                if(exception != null){
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDocumentTitleUpdate(String title) {
        umEditorFileHelper.updateEpubTitle(title, false, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result){
                    pageListFragment.setDocumentTitle(title);
                    handleSelectedPage();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void onDeleteFailure(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPageRemove(String href) {
        umEditorFileHelper.removePage(href, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                pageListFragment.setPageList(umEditorFileHelper.
                        getEpubNavDocument().getToc().getChildren());
                pageListFragment.setDocumentTitle(umEditorFileHelper
                        .getEpubOpfDocument().getTitle());
                if(presenter.getSelectedPageToLoad().equals(href)){
                    presenter.setSelectedPageToLoad(href);
                    handleSelectedPage();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
               if(exception != null){
                   exception.printStackTrace();
               }
            }
        });
    }

    @Override
    public void onPageCreate(String title) {
        umEditorFileHelper.addPage(title, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                pageListFragment.setPageList(umEditorFileHelper.
                        getEpubNavDocument().getToc().getChildren());
            }

            @Override
            public void onFailure(Throwable exception) {
                if(exception != null){
                    exception.printStackTrace();
                }
            }
        });
    }

    @VisibleForTesting
    public void selectAllTestContent(){
        presenter.handleFormatTypeClicked(ACTION_SELECT_ALL,null);
    }

    @VisibleForTesting
    public UmFormatHelper getUmFormatHelper(){
        return umFormatHelper;
    }

}
