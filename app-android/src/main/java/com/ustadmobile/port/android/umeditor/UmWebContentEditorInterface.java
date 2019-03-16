package com.ustadmobile.port.android.umeditor;

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
 *
 * @author kileha3
 *
 */
public class UmWebContentEditorInterface {

    private UmWebContentEditorChromeClient.JsLoadingCallback callback;

    private Activity activity;

    /** Instantiate the interface and set the context */
    public UmWebContentEditorInterface(Activity activity,
            UmWebContentEditorChromeClient.JsLoadingCallback callback) {
        this.callback = callback;
        this.activity = activity;
    }

    /**
     * Listen for the window load finish event
     * @param callbackValue value passed from JS side
     */
    @JavascriptInterface
    public void onWindowLoad(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for tinymce editor configurations into a webpage
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onCreate(String callbackValue){
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for editor mode change to ON
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onEditingModeOn(String callbackValue){
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

    /**
     * Listen for content cut event
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onContentCut(String callbackValue) {
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Listen for link property request event
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onLinkPropRequested(String callbackValue) {
        try{
            activity.runOnUiThread(() -> callback.onCallbackReceived(callbackValue));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
