package com.ustadmobile.port.android.contenteditor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

public class EditorAnimatedViewSwitcher {

    private static EditorAnimatedViewSwitcher viewSwitcher;

    private BottomSheetBehavior formattingBottomSheetBehavior;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private BottomSheetBehavior contentOptionsBottomSheetBehavior;

    private DrawerLayout drawerLayout;

    private boolean isKeyboardActive;

    private boolean openKeyboard = false;

    private boolean openFormatPanel = false;

    private boolean openContentPanel = false;

    private boolean openMediaPanel = false;

    private Activity activity;

    public static final String PANEL_FORMATTING_CONTENT = "formatting_content";

    public static final String PANEL_INSERT_CONTENT ="insert_content";

    public static final String PANEL_MEDIA_CONTENT = "media_content";

    public static final String PANEL_SOFT_KEYBOARD = "soft_keyboard";


    private View rootView;

    public static  EditorAnimatedViewSwitcher getInstance(){
        if(viewSwitcher == null){
            viewSwitcher = new EditorAnimatedViewSwitcher();
        }
        return viewSwitcher;
    }

    public EditorAnimatedViewSwitcher with(Activity activity){
        this.activity = activity;
        return this;
    }

    public EditorAnimatedViewSwitcher setViews(View rootView, BottomSheetBehavior insertContentSheet,
                                               BottomSheetBehavior formatSheet,
                                               BottomSheetBehavior mediaSheet,
                                               DrawerLayout drawerLayout){
        this.contentOptionsBottomSheetBehavior = insertContentSheet;
        this.formattingBottomSheetBehavior = formatSheet;
        this.mediaSourceBottomSheetBehavior = mediaSheet;
        this.rootView = rootView;
        this.drawerLayout = drawerLayout;
        initializeSwitcher();
        return this;
    }

    private void initializeSwitcher(){
        contentOptionsBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    if(openKeyboard){
                        handleSoftKeyboard(true);
                    }else if(openFormatPanel){
                        setFormattingBottomSheetBehavior(true);
                    }else if(openMediaPanel){
                        setMediaSourceBottomSheetBehavior(true);
                    }
                }else if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    openContentPanel = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        formattingBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    if(openKeyboard){
                        handleSoftKeyboard(true);
                    }else if(openContentPanel){
                        setContentOptionBottomSheetBehavior(true);
                    }else if(openMediaPanel){
                        setMediaSourceBottomSheetBehavior(true);
                    }
                }else if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    openFormatPanel = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mediaSourceBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    if(openKeyboard){
                        handleSoftKeyboard(true);
                    }else if(openFormatPanel){
                        setFormattingBottomSheetBehavior(true);
                    }else if(openContentPanel){
                        setContentOptionBottomSheetBehavior(true);
                    }
                }else if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    openMediaPanel = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;
            isKeyboardActive = keypadHeight > screenHeight * 0.15;
            if(isKeyboardActive){
                openKeyboard = false;
            }else{
                if(openFormatPanel){
                    setFormattingBottomSheetBehavior(true);
                }else if(openMediaPanel){
                    setMediaSourceBottomSheetBehavior(true);
                }else if(openContentPanel){
                    setContentOptionBottomSheetBehavior(true);
                }
            }
        });
    }

    public void animateView(String currentKey){

        switch (currentKey){
            case PANEL_FORMATTING_CONTENT:
                if(isFormattingBottomSheetExpanded()){
                    openKeyboard = true;
                    setFormattingBottomSheetBehavior(false);
                }else{
                    if(isKeyboardActive){
                        openFormatPanel = true;
                        handleSoftKeyboard(true);
                    }else if(isContentOptionsBottomSheetExpanded()){
                        openFormatPanel = true;
                        setContentOptionBottomSheetBehavior(false);
                    }else if(isMediaSourceBottomSheetExpanded()){
                        openFormatPanel = true;
                        setMediaSourceBottomSheetBehavior(false);
                    }else{
                        setFormattingBottomSheetBehavior(true);
                    }
                }
                break;

            case PANEL_INSERT_CONTENT:

                if(isContentOptionsBottomSheetExpanded()){
                    openKeyboard = true;
                    setContentOptionBottomSheetBehavior(false);
                }else{
                    if(isKeyboardActive){
                        openContentPanel = true;
                        handleSoftKeyboard(true);
                    }else if(isFormattingBottomSheetExpanded()){
                        openContentPanel = true;
                        setFormattingBottomSheetBehavior(false);
                    }else if(isMediaSourceBottomSheetExpanded()){
                        openContentPanel = true;
                        setMediaSourceBottomSheetBehavior(false);
                    }else{
                        setMediaSourceBottomSheetBehavior(true);
                    }
                }

                break;
            case PANEL_MEDIA_CONTENT:

                if(isMediaSourceBottomSheetExpanded()){
                    openKeyboard = true;
                    setMediaSourceBottomSheetBehavior(false);
                }else{
                    if(isKeyboardActive){
                        openMediaPanel = true;
                        handleSoftKeyboard(true);
                    }else if(isFormattingBottomSheetExpanded()){
                        openContentPanel = true;
                        setFormattingBottomSheetBehavior(false);
                    }else if(isContentOptionsBottomSheetExpanded()){
                        openMediaPanel = true;
                        setContentOptionBottomSheetBehavior(false);
                    }else{
                        setMediaSourceBottomSheetBehavior(true);
                    }
                }


                break;
            case PANEL_SOFT_KEYBOARD:
                if(isFormattingBottomSheetExpanded()){
                    openKeyboard = true;
                    setFormattingBottomSheetBehavior(false);
                }else if(isContentOptionsBottomSheetExpanded()){
                    openKeyboard = true;
                    setContentOptionBottomSheetBehavior(false);
                }else if(isMediaSourceBottomSheetExpanded()){
                    openKeyboard = true;
                    setMediaSourceBottomSheetBehavior(false);
                }else{
                    handleSoftKeyboard(true);
                }
                break;
        }
    }

    public void closeAnimatedView(String viewKey){

        switch (viewKey){
            case PANEL_FORMATTING_CONTENT:
                setFormattingBottomSheetBehavior(false);
                break;

            case PANEL_INSERT_CONTENT:
                setContentOptionBottomSheetBehavior(false);
                break;

            case PANEL_MEDIA_CONTENT:
                setMediaSourceBottomSheetBehavior(false);
                break;

            case PANEL_SOFT_KEYBOARD:
                handleSoftKeyboard(false);
                break;
        }
    }


    private void handleSoftKeyboard(boolean show){
        if(show){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else{
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Collapse all expandable views before moving out of the activity (User experience)
     */
    public void closeActivity(){
        boolean isDrawerOpen = drawerLayout.isDrawerOpen(GravityCompat.END);

        if(isDrawerOpen){
            drawerLayout.closeDrawer(GravityCompat.END);
        }

        if(isMediaSourceBottomSheetExpanded()){
            setMediaSourceBottomSheetBehavior(false);
        }

        if(isFormattingBottomSheetExpanded()){
            setFormattingBottomSheetBehavior(false);
        }

        if(isContentOptionsBottomSheetExpanded()){
            setContentOptionBottomSheetBehavior(false);
        }

        if(!isMediaSourceBottomSheetExpanded() && !isFormattingBottomSheetExpanded()
                && !isDrawerOpen && !isContentOptionsBottomSheetExpanded()){
           //TODO: send event
        }
    }


    private boolean isFormattingBottomSheetExpanded(){
        return formattingBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setFormattingBottomSheetBehavior(boolean expanded){
        formattingBottomSheetBehavior.setState(expanded ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private boolean isContentOptionsBottomSheetExpanded(){
        return contentOptionsBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setContentOptionBottomSheetBehavior(boolean expanded){
        contentOptionsBottomSheetBehavior.setState(expanded ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED);
    }

    private boolean isMediaSourceBottomSheetExpanded(){
        return mediaSourceBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void setMediaSourceBottomSheetBehavior(boolean expanded){
        mediaSourceBottomSheetBehavior.setState(expanded ? BottomSheetBehavior.STATE_EXPANDED
                : BottomSheetBehavior.STATE_COLLAPSED);
    }


}
