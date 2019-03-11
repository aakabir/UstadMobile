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
        Log.d("KeyboardEventWrapper", "Input connection wrapper instantiated");
    }

    public void setProtectedElement(boolean protectedElement) {
        Log.d("KeyboardEventWrapper", "setProtectedElement() - " +
                "Protection status changed to "+protectedElement);
        this.isProtectedElement = protectedElement;
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        Log.d("KeyboardEventWrapper", "getTextBeforeCursor() - " +
                "Get text before cursor with flag ="+flags);
        return delegate.getTextBeforeCursor(n, flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        Log.d("KeyboardEventWrapper", "getTextAfterCursor() - " +
                "Get text after cursor with flag ="+flags);
        return delegate.getTextAfterCursor(n, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        Log.d("KeyboardEventWrapper", "getSelectedText() - " +
                "Get selected text with flag ="+flags);
        return delegate.getSelectedText(flags);
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        Log.d("KeyboardEventWrapper", "getCursorCapsMode() - " +
                "Get cursor caps mode  ="+reqModes);
        return delegate.getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        Log.d("KeyboardEventWrapper", "getExtractedText() - Get extracted txt "
                +request.toString());
        return delegate.getExtractedText(request, flags);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {

        if(!isProtectedElement){
            Log.d("KeyboardEventWrapper", "deleteSurroundingText() - " +
                    "Delete allowed: surroundingText, before ="
                    + beforeLength + " chars  after=" +afterLength + " chars");
            return delegate.deleteSurroundingText(beforeLength, afterLength);
        }

        Log.d("KeyboardEventWrapper", "deleteSurroundingText() - " +
                "Delete not allowed: surroundingText, before ="
                + beforeLength + " chars  after=" +afterLength + " chars");
        return true;

    }

    @TargetApi(24)
    @Override
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        Log.d("KeyboardEventWrapper", "deleteSurroundingTextInCodePoints() - " +
                "Delete surrounding text in code points called start="
                +beforeLength+" end="+afterLength);
       if(!isProtectedElement){
           Log.d("KeyboardEventWrapper", "deleteSurroundingTextInCodePoints() - " +
                   "Delete allowed - surroundingTextInCode, before ="
                   + beforeLength + " chars  after=" +afterLength + " chars");
           return delegate.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
       }
        Log.d("KeyboardEventWrapper", "deleteSurroundingTextInCodePoints() - " +
                "Delete not allowed :surroundingTextInCode, before ="
                + beforeLength + " chars  after=" +afterLength + " chars");
       return true;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        Log.d("KeyboardEventWrapper", "setComposingText() - Set composing text");
        return delegate.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        Log.d("KeyboardEventWrapper", "setComposingRegion() - Set composing region");
        return delegate.setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText() {
        Log.d("KeyboardEventWrapper", "finishComposingText() - Finish composing text");
        return delegate.finishComposingText();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        Log.d("KeyboardEventWrapper", "commitText() - Change committed at "
                + newCursorPosition + " text =" +text);
        return delegate.commitText(text, newCursorPosition);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        Log.d("KeyboardEventWrapper", "commitCompletion() - Done committing "+text.getText()
                + " at "+text.getPosition());
        return delegate.commitCompletion(text);
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        Log.d("KeyboardEventWrapper", "commitCorrection() - Commit corrected at " +
                correctionInfo.getOffset() + " with "+correctionInfo.getNewText());
        return delegate.commitCorrection(correctionInfo);
    }

    @Override
    public boolean setSelection(int start, int end) {
        Log.d("KeyboardEventWrapper", "setSelection() - Selection changed start="
                +start + " end="+end);

        if(!isProtectedElement){
            Log.d("KeyboardEventWrapper", "setSelection() - Allowed to set selection " +
                    ", start = " +start + " end="+end);
            return delegate.setSelection(start, end);
        }

        Log.d("KeyboardEventWrapper", "setSelection() - Not allowed to set selection ," +
                " start = " +start + " end="+end);
        return true;
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        Log.d("KeyboardEventWrapper", "performEditorAction() - " +
                "Editor action performed, action=" +editorAction);
        return delegate.performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int id) {
        Log.d("KeyboardEventWrapper", "performContextMenuAction() - " +
                "Context menu action performed, id=" +id);
        return delegate.performContextMenuAction(id);
    }

    @Override
    public boolean beginBatchEdit() {
        Log.d("KeyboardEventWrapper", "beginBatchEdit() - Batch editing started");
        return delegate.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        Log.d("KeyboardEventWrapper", "endBatchEdit() - Batch editing ended");
        return delegate.endBatchEdit();
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if(event.getKeyCode() != KeyEvent.KEYCODE_DEL || !isProtectedElement) {
            Log.d("KeyboardEventWrapper", "sendKeyEvent() - " +
                    "Key Pressed key action=" + event.getAction() + " code=" + event.getKeyCode());
            return delegate.sendKeyEvent(event);
        }else {
            Log.d("KeyboardEventWrapper", "sendKeyEvent() - not allowed " +
                    "Key Pressed key action=" + event.getAction() + " code=" + event.getKeyCode());
            return true;

        }


    }

    @Override
    public boolean clearMetaKeyStates(int states) {
        Log.d("KeyboardEventWrapper", "clearMetaKeyStates() - " +
                "MetaKeys cleared state=" + states);
        return delegate.clearMetaKeyStates(states);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        Log.d("KeyboardEventWrapper", "reportFullscreenMode() - " +
                "Full screen mode reported enable ? " +enabled);
        return delegate.reportFullscreenMode(enabled);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        Log.d("KeyboardEventWrapper", "performPrivateCommand() - " +
                "Custom command performed , action=" + action + " extras="+data.toString());
        return delegate.performPrivateCommand(action, data);
    }

    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        Log.d("KeyboardEventWrapper", "requestCursorUpdates() - " +
                "Cursor updates requested mode=" +cursorUpdateMode);
        return delegate.requestCursorUpdates(cursorUpdateMode);
    }

    @TargetApi(24)
    @Override
    public Handler getHandler() {
        Log.d("KeyboardEventWrapper", "getHandler() - Connection handler requested");
        return delegate.getHandler();
    }

    @TargetApi(24)
    @Override
    public void closeConnection() {
        Log.d("KeyboardEventWrapper", "closeConnection() - Connection closed");
        delegate.closeConnection();
    }

    @TargetApi(25)
    @Override
    public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int flags, @Nullable Bundle opts) {
        Log.d("KeyboardEventWrapper", "commitContent() - Content committed from"
                + inputContentInfo.getContentUri());
        return delegate.commitContent(inputContentInfo, flags, opts);
    }


}
