package com.ustadmobile.port.android.contenteditor;

import android.app.Activity;
import android.webkit.JavascriptInterface;


/**
 * Class which listen for all the calls that javascript will make to the native side.
 *
 * <b>Operational flow</b>
 * <p>
 *    On JS side make sure you call the method with UmCotentEditor,
 *     i.e UmCotentEditor.onContentChanged so that below methods can
 *     be invoked on android native.
 * </p>
 */
public class WebContentEditorInterface {

    private WebContentEditorChrome.JsLoadingCallback callback;

    private Activity activity;

    /** Instantiate the interface and set the context */
    public WebContentEditorInterface(Activity activity,
                                     WebContentEditorChrome.JsLoadingCallback callback) {
        this.callback = callback;
        this.activity = activity;
    }

    /**
     * Listen for tinymce injection to the webpage
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onInitEditor(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Listen for value changes on the javascript to the native android.
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onSaveContent(String callbackValue){
        try{
            activity.runOnUiThread(() ->
                    callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for text selection on active editor
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onTextSelected(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for activity editor click events
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onClickEvent(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for the active control checks
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onControlsStateChanged(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}