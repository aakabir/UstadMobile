package com.ustadmobile.port.android.view;

import android.os.Bundle;

public class ContentLibraryViewPagerFragment extends UstadBaseFragment {

    public static ContentLibraryViewPagerFragment newInstance() {
        ContentLibraryViewPagerFragment fragment = new ContentLibraryViewPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}
