package com.ustadmobile.port.android.umeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Class which handles all animated view switching, we have BottomSheets and keyboard
 * to bring seamless experience these views has to be switched accordingly.
 *
 * <b>Operational Flow:</b>
 * <p>
 *     Use {@link UmEditorAnimatedViewSwitcher#with(Activity, OnAnimatedViewsClosedListener)}
 *     to set content editor activity instance and listener for listening all animation
 *     closing event,.
 *
 *     Use {@link UmEditorAnimatedViewSwitcher#setViews)} to set all animated views and root view
 *     which will be used to listen for the keyboard events.
 *
 *     Use {@link UmEditorAnimatedViewSwitcher#animateView(String)} to send request to open a certain
 *     animated view depending on the view key passed into it.
 *
 *     Use {@link UmEditorAnimatedViewSwitcher#closeAnimatedView(String)} to send request to close a
 *     certain animated view depending on the view key passed into it.
 *
 *     Use {@link UmEditorAnimatedViewSwitcher#closeActivity(boolean)} to handle activity closing task
 *     which will close all the activity views before shutting down.
 * </p>
 *
 * @author kileha3
 */
public class UmEditorAnimatedViewSwitcher {

    @SuppressLint("StaticFieldLeak")
    private static UmEditorAnimatedViewSwitcher viewSwitcher;

    private BottomSheetBehavior formattingBottomSheetBehavior;

    private BottomSheetBehavior mediaSourceBottomSheetBehavior;

    private BottomSheetBehavior contentOptionsBottomSheetBehavior;

    private DrawerLayout drawerLayout;

    private WebView editorView;

    private OnAnimatedViewsClosedListener closedListener;

    private GestureDetector gestureDetector;

    private boolean isKeyboardActive;

    private boolean openKeyboard = false;

    private boolean openFormatPanel = false;

    private boolean openContentPanel = false;

    private boolean openMediaPanel = false;

    private Activity activity;

    /**
     * Key which represent the formatting options BottomSheet
     */
    public static final String ANIMATED_FORMATTING_PANEL = "formatting_panel";

    /**
     * Key which represents the content option BottomSheet
     */
    public static final String ANIMATED_CONTENT_OPTION_PANEL ="content_option_panel";

    /**
     * Key which represents the media source BottomSheet
     */
    public static final String ANIMATED_MEDIA_TYPE_PANEL = "media_type_panel";

    /**
     * Key which represents the device soft keyboard
     */
    public static final String ANIMATED_SOFT_KEYBOARD_PANEL = "soft_keyboard";


    public static  final  long MAX_SOFT_KEYBOARD_DELAY = TimeUnit.SECONDS.toMillis(1);

    private View rootView;

    private boolean editorActivated = false;


    /**
     * Gesture listener to listen for long press an clicks on the webview
     * for the implicitly keyboard open
     */
    private GestureDetector.SimpleOnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    requestFocusOpenKeyboard();
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    requestFocusOpenKeyboard();
                }
            };

    /**
     * Get UmEditorAnimatedViewSwitcher singleton instance.
     * @return UmEditorAnimatedViewSwitcher instance
     */
    public static UmEditorAnimatedViewSwitcher getInstance(){
        if(viewSwitcher == null){
            viewSwitcher = new UmEditorAnimatedViewSwitcher();
        }
        return viewSwitcher;
    }

    /**
     * Set activity instance to be used and listener to listen when all animated views are closed.
     * @param activity Activity under watcher
     * @param listener Listener to be set for listening animated view closing events.
     * @return UmEditorAnimatedViewSwitcher instance.
     */
    public UmEditorAnimatedViewSwitcher with(Activity activity,
                                             OnAnimatedViewsClosedListener listener){
        this.activity = activity;
        this.closedListener = listener;
        return this;
    }


    /**
     * Set animated views to be monitored from the root activity
     * @param rootView Root view of the activity
     * @param insertContentSheet content option bottom sheet view
     * @param formatSheet Formats types bottom sheet view
     * @param mediaSheet Media sources bottom sheet view
     * @param drawerLayout Drawer layout view
     * @return UmEditorAnimatedViewSwitcher instance.
     */
    public UmEditorAnimatedViewSwitcher setViews(View rootView, WebView editorView,
                                                 BottomSheetBehavior insertContentSheet,
                                                 BottomSheetBehavior formatSheet,
                                                 BottomSheetBehavior mediaSheet,
                                                 DrawerLayout drawerLayout){
        this.contentOptionsBottomSheetBehavior = insertContentSheet;
        this.formattingBottomSheetBehavior = formatSheet;
        this.mediaSourceBottomSheetBehavior = mediaSheet;
        this.rootView = rootView;
        this.editorView = editorView;
        this.drawerLayout = drawerLayout;
        initializeSwitcher();
        return this;
    }

    /**
     * Initialize all views and callback listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initializeSwitcher(){
        gestureDetector = new GestureDetector(activity, onGestureListener);
        editorView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
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
                if(isFormattingBottomSheetExpanded()){
                    setFormattingBottomSheetBehavior(false);
                }else if(isContentOptionsBottomSheetExpanded()){
                    setContentOptionBottomSheetBehavior(false);
                }else if(isMediaSourceBottomSheetExpanded()){
                    setMediaSourceBottomSheetBehavior(false);
                }
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

    /**
     * Animate specific animated view
     * @param currentKey Key of the view to be animated (opened)
     */
    public void animateView(String currentKey){

        switch (currentKey){
            case ANIMATED_FORMATTING_PANEL:
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

            case ANIMATED_CONTENT_OPTION_PANEL:

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
                        setContentOptionBottomSheetBehavior(true);
                    }
                }

                break;
            case ANIMATED_MEDIA_TYPE_PANEL:

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
            case ANIMATED_SOFT_KEYBOARD_PANEL:
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
                    if(!isKeyboardActive){
                        handleSoftKeyboard(true);
                    }
                }
                break;
        }
    }

    /**
     * Set flag to indicate editing mode of the main editor
     * @param editorActivated True when editing mode is ON otherwise Editing mode will be OFF
     */
    public void setEditorActivated(boolean editorActivated){
        this.editorActivated = editorActivated;
    }

    /**
     * Close specific animated view.
     * @param viewKey Key of the animated view to be closed.
     */
    public void closeAnimatedView(String viewKey){
        switch (viewKey){
            case ANIMATED_FORMATTING_PANEL:
                setFormattingBottomSheetBehavior(false);
                break;

            case ANIMATED_CONTENT_OPTION_PANEL:
                setContentOptionBottomSheetBehavior(false);
                break;

            case ANIMATED_MEDIA_TYPE_PANEL:
                setMediaSourceBottomSheetBehavior(false);
                break;

            case ANIMATED_SOFT_KEYBOARD_PANEL:
                handleSoftKeyboard(false);
                break;
        }
    }

    /**
     * IMplicitly open and close soft keyboard.
     * @param show Open when true is passed otherwise close it.
     */
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
     * Close all animated view before destroying the activity.
     */
    public void closeActivity(boolean finishActivity){
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

        handleSoftKeyboard(false);

        new android.os.Handler().postDelayed(() -> {
            if(!isMediaSourceBottomSheetExpanded() && !isFormattingBottomSheetExpanded()
                    && !isDrawerOpen && !isContentOptionsBottomSheetExpanded() && !isKeyboardActive){

                if(closedListener != null){
                    editorActivated = false;
                    closedListener.onAnimatedViewsClosed(finishActivity);
                }
            }
        },MAX_SOFT_KEYBOARD_DELAY);
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

    private void requestFocusOpenKeyboard(){
        closedListener.onFocusRequested();
        editorView.postDelayed(() -> {
            if(editorActivated){
                if(!isKeyboardActive){
                    handleSoftKeyboard(true);
                }
            }
        },MAX_SOFT_KEYBOARD_DELAY);
    }

    /**
     * Interface which listen for the closing event of all views
     * and webview focus on click, tap and press events.
     */
    public interface OnAnimatedViewsClosedListener {
        /**
         * Invoked when all animated views are closed
         * @param finishActivity flag to indicate whether action will result to activity finish or not.
         */
        void onAnimatedViewsClosed(boolean finishActivity);

        /**
         * Invoked when WebView requests a focus.
         */
        void onFocusRequested();
    }


}
