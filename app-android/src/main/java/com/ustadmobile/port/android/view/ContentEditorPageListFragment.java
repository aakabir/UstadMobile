package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContentEditorPageListFragment extends Fragment {


    public ContentEditorPageListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content_editor_page_list, container, false);
    }

}
