package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.DummyView;

import java.util.Hashtable;

public class DummyActivity extends UstadBaseActivity implements DummyView {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        setUMToolbar(R.id.entry_toolbar);

        /*ContentLibraryViewPagerFragment currentFrag = new ContentLibraryViewPagerFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit();
        }*/

        Hashtable args = new Hashtable();
        args.put(ContentEditorView.CONTENT_ENTRY_FILE_UID,"1");
        UstadMobileSystemImpl.getInstance().go(ContentEditorView.VIEW_NAME,args, this);

    }
}
