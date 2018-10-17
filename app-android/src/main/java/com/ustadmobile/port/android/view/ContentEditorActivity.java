package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.github.ag.floatingactionmenu.OptionsFabLayout;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPresenter;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class ContentEditorActivity extends UstadBaseActivity implements ContentEditorView {

    private ContentEditorPresenter presenter;
    private OptionsFabLayout layoutContentTypes;
    private BottomSheetBehavior bottomSheetBehavior;
    private int minFabColors[] =new int[] {
            android.R.color.white,
            android.R.color.white,
            android.R.color.white,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_editor);
        layoutContentTypes = findViewById(R.id.insert_content_types);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_container));

        layoutContentTypes.setMiniFabsColors(minFabColors);
        layoutContentTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutContentTypes.isOptionsMenuOpened();
            }
        });


        presenter = new ContentEditorPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }
}
