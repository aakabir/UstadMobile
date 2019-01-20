package com.ustadmobile.port.android.umeditor;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;

/**
 * Wrapper class which used to intercept keyboard actions
 *
 * @author kileha3
 *
 *
 */
public class InputConnectionWrapper implements InputConnection {

    private InputConnection delegate;

    private boolean isProtectedElement = false;

    InputConnectionWrapper(InputConnection delegate) {
        this.delegate = delegate;
    }

    public void setProtectedElement(boolean protectedElement) {
        Log.d("PreventDeletion","set "+protectedElement+" "+String.valueOf(System.currentTimeMillis()));
        this.isProtectedElement = protectedElement;
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        return delegate.getTextBeforeCursor(n, flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        return delegate.getTextAfterCursor(n, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        return delegate.getSelectedText(flags);
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        return delegate.getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return delegate.getExtractedText(request, flags);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if(!isProtectedElement){
            return delegate.deleteSurroundingText(beforeLength, afterLength);
        }
        return true;
    }

    @TargetApi(24)
    @Override
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
       if(!isProtectedElement){
           return delegate.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
       }
       return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return delegate.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        return delegate.setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText() {
        return delegate.finishComposingText();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return delegate.commitText(text, newCursorPosition);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        return delegate.commitCompletion(text);
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return delegate.commitCorrection(correctionInfo);
    }

    @Override
    public boolean setSelection(int start, int end) {
        return delegate.setSelection(start, end);
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        return delegate.performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int id) {
        return delegate.performContextMenuAction(id);
    }

    @Override
    public boolean beginBatchEdit() {
        return delegate.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        return delegate.endBatchEdit();
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        return delegate.sendKeyEvent(event);
    }

    @Override
    public boolean clearMetaKeyStates(int states) {
        return delegate.clearMetaKeyStates(states);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        return delegate.reportFullscreenMode(enabled);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        return delegate.performPrivateCommand(action, data);
    }

    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        return delegate.requestCursorUpdates(cursorUpdateMode);
    }

    @TargetApi(24)
    @Override
    public Handler getHandler() {
        return delegate.getHandler();
    }

    @TargetApi(24)
    @Override
    public void closeConnection() {
        delegate.closeConnection();
    }

    @TargetApi(25)
    @Override
    public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int flags, @Nullable Bundle opts) {
        return delegate.commitContent(inputContentInfo, flags, opts);
    }
}
