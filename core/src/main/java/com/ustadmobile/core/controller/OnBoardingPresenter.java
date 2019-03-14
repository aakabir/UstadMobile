package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.OnBoardingView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ContentEditorView.CONTENT_ENTRY_FILE_UID;
import static com.ustadmobile.core.view.OnBoardingView.PREF_TAG;

public class OnBoardingPresenter extends UstadBaseController<OnBoardingView> {

    public OnBoardingPresenter(Object context, Hashtable arguments, OnBoardingView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> view.setScreenList());
        boolean wasShown = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance()
                .getAppPref(PREF_TAG,view.getContext()));
        if(wasShown){
            handleGetStarted();
        }
    }

    public void handleGetStarted() {
        Hashtable args = getArguments();
        if (args == null) {
            args = new Hashtable();
        }
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_TAG, String.valueOf(true)
                , view.getContext());
        args.put(CONTENT_ENTRY_FILE_UID, String.valueOf(1));
        UstadMobileSystemImpl.getInstance().go(ContentEditorView.VIEW_NAME, args, getContext());
    }
}
