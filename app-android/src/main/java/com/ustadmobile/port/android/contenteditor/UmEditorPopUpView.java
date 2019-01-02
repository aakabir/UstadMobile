package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.PopupMenu;
import android.view.View;

public class UmPopUpMenuView extends PopupMenu {
    public UmPopUpMenuView(Context context, View anchor) {
        super(context, anchor);
    }

    public UmPopUpMenuView(Context context, View anchor, int gravity) {
        super(context, anchor, gravity);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public UmPopUpMenuView(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        super(context, anchor, gravity, popupStyleAttr, popupStyleRes);
    }

    public void inflate(int menuRes,boolean isToolBar){
        inflate(menuRes);
    }

    @Override
    public void inflate(int menuRes) {
        super.inflate(menuRes);
    }
}
