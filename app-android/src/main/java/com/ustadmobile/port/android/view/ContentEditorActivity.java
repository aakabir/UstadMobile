package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView, FloatingActionMenu.OnMenuToggleListener {

    private ContentEditorPresenter presenter;
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionMenu mInsertContent;
    private FloatingActionButton mPreviewContent;
    private FloatingActionButton mInsertMultipleChoice;
    private FloatingActionButton mInsertFillBlanks;
    private FloatingActionButton mInsertMultimedia;
    private DrawerLayout mContentPageDrawer;

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
        }
    }
}
