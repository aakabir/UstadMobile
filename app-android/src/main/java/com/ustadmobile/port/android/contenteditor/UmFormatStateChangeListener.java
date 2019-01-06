package com.ustadmobile.port.android.contenteditor;

/**
 * Interface to listen for the state change of the formatting item
 */
public interface UmFormatStateChangeListener {
    /**
     * Invoked when formatting item has been updated
     * @param format updated content format
     */
    void onStateChanged(UmFormat format);
}
