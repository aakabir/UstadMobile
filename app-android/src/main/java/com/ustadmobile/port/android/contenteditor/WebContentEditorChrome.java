package com.ustadmobile.port.android.contenteditor;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 * Class which is responsible to handle all page loads and native-to-js client interaction.
 */
public class WebContentEditorChrome  extends WebChromeClient {

    private JsLoadingCallback callback;

    /**
     * Constructor which is used when creating an instance of this class.
     * @param callback Callback to handle all page loads
     */
    public WebContentEditorChrome (JsLoadingCallback callback){
        this.callback = callback;
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if(consoleMessage.message().contains("action")){
           callback.onCallbackReceived(consoleMessage.message());
        }
        return true;
    }

    /**
     * Listen for the page load progress change and notifies the UI
     * @param view WebView in which the page is currently loading
     * @param newProgress new progress percentage
     */
    public void onProgressChanged(WebView view, int newProgress){
        if(newProgress == 100){
            callback.onPageFinishedLoading();
        }

        callback.onProgressChanged(newProgress);

    }


    /**
     * Interface which listens for the page loads and values when js function are executed.
     */
    public interface JsLoadingCallback{
        /**
         * Invoked when page loading progress changes
         * @param newProgress new progress value
         */
        void onProgressChanged(int newProgress);

        /**
         * Invoked when page has finished loading
         */
        void onPageFinishedLoading();

        /**
         * Invoked when return value or console message is created.
         * @param value valued to be passed to the native android
         */
        void onCallbackReceived(String value);

    }


}
