package com.ustadmobile.port.sharedse.contenteditor;

import com.ustadmobile.core.contenteditor.ContentEditorFileHelperCore;

public abstract class ContentEditorFileHelper implements ContentEditorFileHelperCore {

    private Object context;

    public ContentEditorFileHelper(Object context){
        this.context = context;
    }
}
