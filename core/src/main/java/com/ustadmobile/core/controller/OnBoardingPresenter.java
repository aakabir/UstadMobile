package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.core.view.OnBoardingView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ContentEditorView.CONTENT_ENTRY_FILE_UID;

public class OnBoardingPresenter extends UstadBaseController<OnBoardingView> {

    public OnBoardingPresenter(Object context, Hashtable arguments, OnBoardingView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> view.setScreenList());
    }


    public void handleGetStarted(){
        Hashtable args = getArguments();
        if(args == null){
            args = new Hashtable();
        }

        args.put(CONTENT_ENTRY_FILE_UID,String.valueOf(1));

        UstadMobileSystemImpl.getInstance().go(ContentEditorView.VIEW_NAME,args,getContext());
    }
}
