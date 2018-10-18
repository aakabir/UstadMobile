package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class ContentEditorActivity extends UstadBaseActivity implements
        ContentEditorView, FloatingActionMenu.OnMenuToggleListener {

    private static ContentEditorPresenter presenter;
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionMenu mInsertContent;
    private FloatingActionButton mPreviewContent;
    private FloatingActionButton mInsertMultipleChoice;
    private FloatingActionButton mInsertFillBlanks;
    private FloatingActionButton mInsertMultimedia;
    private DrawerLayout mContentPageDrawer;
    private boolean isFromFormatting = false;

    private FormattingType [] textFormatting = new FormattingType[]{
            new FormattingType(R.drawable.ic_format_bold_black_24dp,TEXT_FORMAT_TYPE_BOLD),
            new FormattingType(R.drawable.ic_format_italic_black_24dp,TEXT_FORMAT_TYPE_ITALIC),
            new FormattingType(R.drawable.ic_format_strikethrough_black_24dp,TEXT_FORMAT_TYPE_STRIKE),
            new FormattingType(R.drawable.ic_format_underlined_black_24dp,TEXT_FORMAT_TYPE_UNDERLINE),
            new FormattingType(R.drawable.ic_format_size_black_24dp,TEXT_FORMAT_TYPE_FONT),
            new FormattingType(R.drawable.ic_number_superscript,TEXT_FORMAT_TYPE_SUP),
            new FormattingType(R.drawable.ic_number_subscript,TEXT_FORMAT_TYPE_SUB)
    };

    private FormattingType [] paragraphFormatting = new FormattingType[]{
            new FormattingType(R.drawable.ic_format_align_justify_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_JUSTIFY),
            new FormattingType(R.drawable.ic_format_align_right_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_RIGHT),
            new FormattingType(R.drawable.ic_format_align_center_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_CENTER),
            new FormattingType(R.drawable.ic_format_align_left_black_24dp,
                    PARAGRAPH_FORMAT_ALIGN_LEFT),
            new FormattingType(R.drawable.ic_format_list_numbered_black_24dp,
                    PARAGRAPH_FORMAT_LIST_ORDERED),
            new FormattingType(R.drawable.ic_format_list_bulleted_black_24dp,
                    PARAGRAPH_FORMAT_LIST_UNORDERED),
            new FormattingType(R.drawable.ic_format_indent_increase_black_24dp,
                    PARAGRAPH_FORMAT_INDENT_INCREASE),
            new FormattingType(R.drawable.ic_format_indent_decrease_black_24dp,
                    PARAGRAPH_FORMAT_INDENT_DECREASE)
    };



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
            return new FormattingFragment().with(position ==
                    0 ? textFormatting:paragraphFormatting);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_editor);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_container));
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        mInsertContent = findViewById(R.id.content_editor_insert);
        mPreviewContent = findViewById(R.id.content_editor_preview);
        Toolbar toolbar = findViewById(R.id.um_toolbar);
        mInsertMultimedia = findViewById(R.id.content_type_multimedia);
        mInsertMultipleChoice = findViewById(R.id.content_type_multiple_choice);
        mInsertFillBlanks = findViewById(R.id.content_type_fill_blanks);
        mContentPageDrawer = findViewById(R.id.content_page_drawer);
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
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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


    }



    @Override
    public void setBoldFormatting() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content_editor_questions,menu);
        return super.onCreateOptionsMenu(menu);
    }

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
                boolean isSheetOpened =
                        bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;

                bottomSheetBehavior.setState(isSheetOpened ?
                        BottomSheetBehavior.STATE_COLLAPSED:BottomSheetBehavior.STATE_EXPANDED);
            }
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
            mPreviewContent.show(true);
            if(isFromFormatting){
                isFromFormatting = false;
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private class FormattingType{
        private int formatIcon;

        private String formatTag;

        FormattingType(int formatIcon, String formatTag) {
            this.formatIcon = formatIcon;
            this.formatTag = formatTag;
        }

        String getFormatTag() {
            return formatTag;
        }
    }


    public static class FormattingFragment extends Fragment{

        class FormatsAdapter extends RecyclerView.Adapter<FormatsAdapter.FormatViewHolder>{

            private FormattingType [] formattingTypes;

            FormatsAdapter(FormattingType [] formattingTypes){
                this.formattingTypes = formattingTypes;
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
                FormattingType formattingType = formattingTypes[position];
                Log.d("Formattype",formattingType.getFormatTag());
                ((ImageView)holder.itemView.findViewById(R.id.format_icon))
                        .setImageResource(formattingType.formatIcon);
                holder.itemView.findViewById(R.id.format_holder).setOnClickListener(
                        v -> presenter.handleFormattTypeClicked(formattingType.getFormatTag()));
            }

            @Override
            public int getItemCount() {
                return formattingTypes.length;
            }

            class FormatViewHolder extends RecyclerView.ViewHolder{
                FormatViewHolder(View itemView) {
                    super(itemView);
                }
            }

        }


        private FormattingType [] formattingTypes;

        public FormattingFragment with(FormattingType[] formattingTypes) {
            this.formattingTypes = formattingTypes;
            return this;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_content_formatting,
                    container, false);
            RecyclerView mRecyclerView = rootView.findViewById(R.id.formats_list);
            FormatsAdapter adapter = new FormatsAdapter(formattingTypes);
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(),
                    getSpanCount(Objects.requireNonNull(getActivity()),100));
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(adapter);
            return  rootView;

        }
    }

    public static int getSpanCount(Activity activity, @NonNull Integer width){
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = activity.getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        return Math.round(dpWidth/width);
    }
}
