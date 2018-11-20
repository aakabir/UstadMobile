package com.ustadmobile.port.android.contenteditor;

import android.webkit.JavascriptInterface;

/**
 * Class which listen for all the calls that javascript will make to the native side.
 *
 * <b>Operational flow</b>
 * <p>
 *     Use {@link WebContentEditorInterface#onContentChanged(String)}
 *     to pass current content editor content.
 *
 *     Use {@link WebContentEditorInterface#onTextSelected(String)}
 *     to listen for text highlighting.
 *
 *     On JS side make sure you call the method with UmCotentEditor,
 *     i.e UmCotentEditor.onContentChanged
 * </p>
 */
public class WebContentEditorInterface {

    private WebContentEditorChrome.JsLoadingCallback callback;

    /** Instantiate the interface and set the context */
    public WebContentEditorInterface(WebContentEditorChrome.JsLoadingCallback callback) {
        this.callback = callback;
    }

    /**
     * Listen for value changes on the javascript to the native android.
     * @param callbackValue Value passed from the JS side
     */
    @JavascriptInterface
    public void onContentChanged(String callbackValue){
        try{
            callback.onCallbackReceived(callbackValue);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void onTextSelected(String callbackValue){
        try{
            callback.onCallbackReceived(callbackValue);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void onClickEvent(String callbackValue){
        try{
            callback.onCallbackReceived(callbackValue);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void onControlActivatedCheck(String callbackValue){
        try{
            callback.onCallbackReceived(callbackValue);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void onInitEditor(String callbackValue){
        try{
            callback.onCallbackReceived(callbackValue);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
