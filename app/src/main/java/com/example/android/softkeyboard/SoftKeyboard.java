/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.softkeyboard;
import android.app.Dialog;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    static final boolean DEBUG = false;

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private InputMethodManager mInputMethodManager;
    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder(); //texto a ser construido. .tostring() retorna o conteudo
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;

    private LatinKeyboard mCurKeyboard;

    private String mWordSeparators;

    InputStream novoInputStream;

    List<String> sugestões = new ArrayList<String>(Arrays.asList("死亡", "私"));

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {
        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        setLatinKeyboard(mQwertyKeyboard);
        return mInputView;
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
        final boolean shouldSupportLanguageSwitchKey =
                mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        mInputView.setKeyboard(nextKeyboard);
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        Log.i(TAG, "onStartInput: ");
        updateCandidates1();
        updateCandidates2();
        updateCandidates3();
        updateCandidates4();
        updateCandidates5();
        updateCandidates6();
        updateCandidates7();
        updateCandidates8();
        updateCandidates9();
        updateCandidates10();
        updateCandidates11();
        updateCandidates12();
        //updateCandidates13();
        //updateCandidates14();
        //updateCandidates15();
        //updateCandidates16();
        //updateCandidates17();
        //updateCandidates18();
        //updateCandidates19();
        //updateCandidates20();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override
    public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        Log.i(TAG, "onFinishInput: ");
        updateCandidates1();
        updateCandidates2();
        updateCandidates3();
        updateCandidates4();
        updateCandidates5();
        updateCandidates6();
        updateCandidates7();
        updateCandidates8();
        updateCandidates9();
        updateCandidates10();
        updateCandidates11();
        updateCandidates12();
        //updateCandidates13();
        //updateCandidates14();
        //updateCandidates15();
        //updateCandidates16();
        //updateCandidates17();
        //updateCandidates18();
        //updateCandidates19();
        //updateCandidates20();

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        mCurKeyboard = mQwertyKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        setLatinKeyboard(mCurKeyboard);
        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            InputConnection ic = getCurrentInputConnection();
            updateCandidates1();
            updateCandidates2();
            updateCandidates3();
            updateCandidates4();
            updateCandidates5();
            updateCandidates6();
            updateCandidates7();
            updateCandidates8();
            updateCandidates9();
            updateCandidates10();
            updateCandidates11();
            updateCandidates12();
            //updateCandidates13();
            //updateCandidates14();
            //updateCandidates15();
            //updateCandidates16();
            //updateCandidates17();
            //updateCandidates18();
            //updateCandidates19();
            //updateCandidates20();
            if (ic != null) {
                ic.finishComposingText();
                Log.i(TAG, "onUpdateSelection: seleção mudou");
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    // Parece que nunca é lançado
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "translateKeyDown: ");
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;
        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);
            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown: ");
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;


            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        Log.i(TAG, "onKeyUp: ");
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates1();
            updateCandidates2();
            updateCandidates3();
            updateCandidates4();
            updateCandidates5();
            updateCandidates6();
            updateCandidates7();
            updateCandidates8();
            updateCandidates9();
            updateCandidates10();
            updateCandidates11();
            updateCandidates12();
            //updateCandidates13();
            //updateCandidates14();
            //updateCandidates15();
            //updateCandidates16();
            //updateCandidates17();
            //updateCandidates18();
            //updateCandidates19();
            //updateCandidates20();
            Log.i(TAG, "commitTyped (depois de já ter cometido): " + mComposing);
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        Log.i(TAG, "isAlphabet: ");
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        Log.i(TAG, "keyDownUp: ");
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        Log.i(TAG, "sendKey: ");
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener
    // Envia tecla nó texto, menos no espaço???
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.i(TAG, "onKey: ");
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) {
            Log.i(TAG, "KEYCODE_MODE_CHANGE: ");
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                setLatinKeyboard(mQwertyKeyboard);
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
                mSymbolsKeyboard.setShifted(false);
            }
        } else if (primaryCode == 21){
          sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
        } else if (primaryCode ==22){
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
        } else {
            Log.i(TAG, "caratér: " + primaryCode);
            handleCharacter(primaryCode, keyCodes);
        }
    }

    public void onText(CharSequence text) {
        Log.i(TAG, "onText: ");
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates1() {
        Log.i(TAG, "updateCandidates: começou, mcomposing:" + mComposing.toString());
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("b")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("b", "布"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("c")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("c", "克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("d")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("d", "德"), "德", "予")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("e")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("e", "与"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("f")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("f", "夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("g")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("g", "葛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("h")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("h", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("i")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("i", "意"), "意", "工", "伊", "工", "意")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("l")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("l", "尔"), "尔", "讀")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("o")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("o", "个"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("r")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("r", "尔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("s")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("s", "士"), "士", "們", "斯", "知", "們", "遵", "昇")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("t")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("t", "有"), "有", "達", "特")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("u")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("u", "得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("v")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("v", "來"), "來", "檢")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("x")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("x", "❌ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("á")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("á", "阿"), "阿", "乍")), true, true);}

            }
        } else {
            setSuggestions(null, false, false);
            Log.i(TAG, "updateCandidates: se mcomposing.length = 0");
        }
    }


    private void updateCandidates2() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("ad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ad", "增"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ah")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ah", "哈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("al")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("al", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ai", "那里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ap")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ap", "應"), "應", "應用")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ar", "空"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("at")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("at", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ba", "巴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("be")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("be", "寶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bi", "美"), "美", "碧", "比")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bs", "原聲音乐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bu", "布"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ca", "貨"), "貨", "卡", "可")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ce", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cu", "尻"), "尻", "克", "族")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("da")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("da", "於♀"), "於♀", "達", "了")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("de")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("de", "['丶白"), "['丶白", "德")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ri", "里"), "里", "利")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("di")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("di", "低"), "低", "迪")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("do")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("do", "献"), "献", "了")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("é")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("é", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("el")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("el", "他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("em")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("em", "於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("en")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("en", "恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("es", "語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eu", "我"), "我", "我")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ex", "元"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fa", "迷"), "迷", "做", "制")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fi", "翡"), "翡", "菲", "得", "留")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fo", "焦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fu", "佛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("go")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("go", "悟"), "悟", "御")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ha", "啊"), "啊", "在")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hh", "っ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hi", "希"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ia", "亚"), "亚", "哉", "學")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("in")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("in", "殘"), "殘", "无")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ir", "行"), "行", "往")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("is")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("is", "國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iu", "祐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iz", "生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ja", "既"), "既", "已經")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("je")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("je", "傑"), "傑", "耶")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ji")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ji", "姬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jo", "舟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ju")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ju", "祖"), "祖", "舉", "據")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("la")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("la", "拉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("le")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("le", "黎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("li")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("li", "利"), "利", "裡")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lo", "洛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lu", "魯"), "魯", "錄")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "邁"), "邁", "麼", "嗎", "馬", "軟", "魔", "吗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("me")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("me", "以我"), "以我", "梅")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mo", "磨"), "磨", "默")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("na")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("na", "華"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ne")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ne", "恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "泥"), "泥", "於", "尼", "妮", "級")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "名"), "名", "之", "的", "的")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ns", "不知"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nu", "努"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ó")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ó", "御"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oi", "嘿"), "嘿", "喂")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ok")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ok", "是	副詞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("or")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("or", "者"), "者", "監")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ou", "或"), "或", "了")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pe", "足"), "足", "請")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ra", "拉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rd", "德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("re")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("re", "再"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rs", "😂 "), "😂", "笑")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ru")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ru", "魯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa", "出"), "出", "薩", "撒")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("se", "如果"), "如果", "斯", "燥", "燥")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("so")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("so", "蘇"), "蘇", "響")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sr", "氏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ss", "斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("su")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("su", "素"), "素", "俗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ta", "田"), "田", "有á", "塔", "宅")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("te")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("te", "以你"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ti", "蒂"), "蒂", "體", "緹")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("to")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("to", "奏"), "奏", "特", "触")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tr", "帶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ts")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ts", "配樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tu", "你"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tz", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ue", "衛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ui", "威"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("um")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("um", "1"), "1", "一")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("un")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("un", "聯"), "聯", "運")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("us")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("us", "斯"), "斯", "使")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("va")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("va", "去行"), "去行", "了", "去吧")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vi", "维"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vs", "對"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xi", "戲"), "戲", "希")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ze")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ze", "澤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("zi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("zi", "季"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("zo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("zo", "索"))), true, true);}

            } else {
                    setSuggestions(null, false, false);
                }
            }
        }



    private void updateCandidates3() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("abr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abr", "开"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ach")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ach", "思"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aco", "钢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adi", "整合開發環境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aí")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aí", "那里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ali")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ali", "盟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("and")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("and", "🚶"), "🚶", " 走 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ano", "琴"), "琴", "年")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cai", "堕"), "堕", "坠")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("art")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("art", "藝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("asa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asa", "习"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ass", "烤"), "烤", "組")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ata", "攻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atu", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ndo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ndo", "著"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ave")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ave", "雀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("avo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avo", "祖母"), "祖母", "祖父")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bat", "打"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bé")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bé", "貝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("beb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beb", "飲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bem", "很 "), "很 ", " 善 ", "👍")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("boa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boa", "好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom", "👍"), "👍", " 好 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bou", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cab", "康健"), "康健", "合")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cac", "狩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cal", "默"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("can")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("can", "坎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cao", "犬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("car")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("car", "字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cas", "娶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cav", "掘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ceg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceg", "盲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cem", "百"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ceu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceu", "天"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cha", "茶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chi", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cho", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chu", "修"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coc", "搔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("com")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("com", "和"), "和", "跟", "務員", "食")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("con")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("con", "进"), "进", "含")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cor", "色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cou", "科"), "科", "行")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coz", "烧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cri", "造"), "造", "創")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cru")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cru", "生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cur", "愈"), "愈", "治")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dai", "呆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dar", "与"), "与", "給")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("des")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("des", "卸	接頭語"), "卸	接頭語", "卸", "劣")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dia", "日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dir", "方"), "方", "電台")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("div")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("div", "乐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diz", "言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dns", "域名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dor", "痛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dqm")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dqm", "無論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edo", "具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edu", "育"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ela", "她"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ele", "男也"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("els")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("els", "他們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enc", "終曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("era")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("era", "紀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ero", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("err")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("err", "誤"), "誤", "錯")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esc", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("és")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("és", "是"), "是", "东")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("est")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("est", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eua", "合区國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fal", "話"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fan", "芳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fei", "廢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fim", "末"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fin", "芬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fio", "紗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fiz", "作了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fod", "肏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("foi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foi", "行了"), "行了", "去了")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fon", "峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("for")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("for", "选上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fra", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fug", "逃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fui", "去了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fum")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fum", "抽煙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gai", "同性恋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gan", "根"), "根", "梗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gas", "气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("geo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geo", "地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("glu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("glu", "吨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gou", "毫"), "毫", "夠")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("han")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("han", "韓"), "韓", "漢")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hou", "方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iei", "耶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iin", "應"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilh", "享"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ips")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ips", "幀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iue", "粵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iui", "唯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jei", "詹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jia", "駕"), "駕", "假")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jin", "津"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jor", "喬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jou", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jua", "抓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jur", "誓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lai", "萊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lan", "兰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lav", "洗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lei", "律"), "律", "雷")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ler")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ler", "读"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lev", "拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lha", "以她"), "以她", "拉")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lhe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lhe", "以他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lig", "接"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lin", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("loc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loc", "所"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("log")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("log", "質"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lon", "隆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lua", "🌛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lun")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lun", "倫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("luz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luz", "光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mac", "軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mae")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mae", "妈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mal", "恶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("man")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("man", "万"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mao", "手 "), "手 ", "✋")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mar", "海"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mas", "但"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mat", "殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mau", "弊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mdf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mdf", "背景音乐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("med")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("med", "測"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mei", "梅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mel", "🍯"), "🍯", " 蜜 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mes", "月"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meu", "我的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mex", "动"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mil", "千"), "千", "千")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mio", "澪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mir", "狙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mom", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mov", "动"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mud", "變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nad", "泳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nao", "不"), "不", "不")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("neg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neg", "尚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nem", "否"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nen")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nen", "年"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nev", "☃️"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nin", "忍"), "忍", "宁")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("niu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("niu", "妞"), "妞", "紐")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nó")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nó", "結"), "結", "的")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nor", "北"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nos", "己"), "己", "我們")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oca", "岡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ola", "迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olh", "見"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("opc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opc", "自選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovo", "🥚"), "🥚", " 卵 ", " 蛋 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pag", "払"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pai", "爹"), "爹", "父")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pam", "噴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pan", "班"), "班", "噴", "盤", "潘")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pao", "🍞"), "🍞", " 麵包 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("par")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("par", "止"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pau", "棒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paz", "☮ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ped")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ped", "求"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peg", "拾"), "拾", "付")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pel", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("per")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("per", "要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pir", "去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pis", "踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pó")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pó", "尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pod", "可"), "可", "可以")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("por")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("por", "砵"), "砵", "为", "葡", "緣")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ppt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ppt", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pra", "銀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pre", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pro", "專"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pub", "広告"), "広告", "公")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pux")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pux", "引"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "麼"), "麼", "克", "科", "卷", "啥")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ra~")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ra~", "蛙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rai", "萊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ran")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ran", "燃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ref")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ref", "典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rei", "王"), "王", "雷", "王")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rep")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rep", "代"), "代", "配")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rez", "祈🙏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rin", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rir", "笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ro7")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ro7", "隆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rra", "琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rug", "咆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rui", "瑞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sa~")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa~", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sai", "去"), "去", "賽", "去出")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sao", "是"), "是", "聖", "騷")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seg", "秒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sei", "知道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sem", "无"), "无", "毫无")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ser")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ser", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seu", "您的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sim", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sin", "交響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("só")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("só", "僅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soa", "響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sol", "☀ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tom", "音"), "音", "音", "採")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sou", "送"), "送", "我是", "是")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ssu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ssu", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sua", "您的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sub", "亚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sug", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suj")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suj", "汚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sul", "南"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tag", "簽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tao", "甚至"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tem", "有"), "有", "天")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ter", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teu", "你的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tex", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tia", "叔母"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tic")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tic", "抖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tim", "丁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tio", "父的哥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("top")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("top", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rod", "旋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("env")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("env", "送"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tro", "替"), "替", "交")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tss", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tua", "妳"), "妳", "你的")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uai", "威"), "威", "懷")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uma", "一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uns", "些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uso", "用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vai", "去行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vem", "雲"), "雲", "来您")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ver", "視"), "視", "看")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vez", "時間"), "時間", "倍")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("via")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("via", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vir", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("viv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viv", "住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vos", "以你們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vou", "行我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("voz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voz", "聲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xiu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xiu", "默"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xou", "翔"))), true, true);}


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates4() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("aban")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aban", "煽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rico", "丰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("abus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abus", "虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acab", "終"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ador", "喜爱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afog")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afog", "溺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agit", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agri", "農"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agua", "水"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ajud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ajud", "助"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alá")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alá", "齋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alho", "蒜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alma", "魂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alto", "高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alug", "租"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amar", "爱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amor", "爱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anda", "走"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ando")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ando", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anjo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anjo", "天使"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anos", "岁"), "岁", "年")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anot")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anot", "記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anti", "反"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anus", "肛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apag", "刪除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apoi", "援"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aqui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aqui", "这里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arco", "弓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arte", "術"), "術", "藝")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("asas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asas", "羽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ativ")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ativ", "啟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("auge")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("auge", "巔峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aujo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aujo", "巔峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("azul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azul", "藍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baba", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bala", "彈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cana", "竹"), "竹", "杖")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bang")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bang", "榜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("base")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("base", "基"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bege")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bege", "米"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("beje")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beje", "米色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bela", "美"), "美", "美麗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bens", "資"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bili")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bili", "哔哩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bloq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloq", "厂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("boca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boca", "👄"), "👄", " 嘴 ", " 口 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bola", "球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bora", "上去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brin", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("buda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("buda", "佛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caca", "猟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cacu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cacu", "獲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cada", "各"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cafe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cafe", "☕ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cama", "床"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cans")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cans", "困"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cant", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tsao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tsao", "曹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caos", "沌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caps")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caps", "包"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("casa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("casa", "家"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caso", "案"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cedo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cedo", "早"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ceia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceia", "夜食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cele", "賽魯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cena", "事"), "事", "先拿", "景")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cham")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cham", "稱呼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chao", "土"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cheg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheg", "到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("até")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("até", "至"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chor", "哭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cima", "↑"), "↑", "上")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cina", "先拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cliq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cliq", "点擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cobr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobr", "覆"), "覆", "蓋")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("como")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("como", "怎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comp", "買"), "買", "集")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cona")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cona", "屄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conf", "亂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cont")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cont", "含"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cool")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cool", "良"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("copa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("copa", "盃 "), "盃 ", "🏆")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corr", "🏃"), "🏃", " 跑 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cort")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cort", "切"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coub", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("crav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crav", "刺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cuba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cuba", "🇨🇺 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cubo", "立方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("culp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culp", "責備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cute")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cute", "可愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("danc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("danc", "舞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dano", "害"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("data")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("data", "数据"), "数据", "日期")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dedo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dedo", "指"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deit", "寝"), "寝", "伏")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deix", "残"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dela", "她的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dele", "他的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("depe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depe", "立"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deus", "神"), "神", "真主")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dici")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dici", "辞典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("disp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disp", "肯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("disu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disu", "低俗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dobr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobr", "彎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doce", "甜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doer", "痛感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dois", "2"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dorm")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dorm", "寝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dose")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dose", "番"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("duro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("duro", "固"), "固", "辛")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ecra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ecra", "屏幕"), "屏幕", "屏")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edif")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edif", "建"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edit", "編"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eixo", "軸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("elas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elas", "她們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eles", "男也們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("elev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elev", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("embr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embr", "裝r"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("endo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("endo", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enfi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enfi", "穿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entr", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("erro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erro", "誤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("erva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erva", "草"), "草", "艹")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eses", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rasp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rasp", "蹭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espi", "覗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("essa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("essa", "那"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esse", "那"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esta", "这♀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("este")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("este", "这"), "这", "东")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rota", "路"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("euro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("euro", "€"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("evol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evol", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("expo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expo", "展"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("faca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("faca", "🔪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("face")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("face", "面"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fala", "話吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("falt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falt", "缺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fase")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fase", "相"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fava")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fava", "豆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fech")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fech", "閉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feia", "醜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feio", "醜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fito", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fixe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fixe", "帅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flor", "花 "), "花 ", " 🌼 ", " 華 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flui", "流"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fofa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofa", "可爱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fofo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofo", "可爱"), "可爱", "蓬松", "暄", "柔軟")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fogo", "🔥 "), "🔥 ", " 火 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fome")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fome", "飢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fone")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fone", "筒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fong")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fong", "鋒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fora", "外"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("foto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foto", "攝"), "攝", "照片")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("freq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("freq", "頻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frio", "寒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fucu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fucu", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fumo", "煙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gacu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gacu", "學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gaja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaja", "奴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gajo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gajo", "禺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ganh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganh", "稼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gast")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gast", "費"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gato", "貓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gelo", "冫"), "冫", "氷")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peca", "剧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("giga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("giga", "巨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gost")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gost", "欢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gram")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gram", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gran")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gran", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grao", "粒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grau", "度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("greg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("greg", "葛瑞格"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grit", "叫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guar", "衛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hino", "國歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hoje")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hoje", "今日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("homo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("homo", "同士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hora", "時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iang")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iang", "央"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iano", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iaoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iaoi", "㚻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilha", "島"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ilud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilud", "妄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imov", "移動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inbi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inbi", "硬幣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inch")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inch", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("info")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("info", "報"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("invo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("invo", "召"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irse", "去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("isca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isca", "餌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ismo", "主義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("isol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isol", "獨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("isso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isso", "那♂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("isto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isto", "这♂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("item")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("item", "件"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iues", "粵語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jato", "噴出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jaze")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jaze", "爵士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jeva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jeva", "爪哇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("joao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joao", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogo", "賽"), "賽", "戏")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("john")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("john", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jone")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jone", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("judo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("judo", "杉本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("junt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("junt", "合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("juve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juve", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lado", "方"), "方", "旁")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ladr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladr", "吠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lago")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lago", "湖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lanc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lanc", "癶投"), "癶投", "擲")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("larg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("larg", "堕"), "堕", "离")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lata", "缶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leal", "忠誠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leao", "獅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leoa", "獅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leve", "輕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("liga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("liga", "聯賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limp", "拭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lind")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lind", "美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lixo", "圾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lobo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lobo", "狼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("logo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("logo", "标志"), "标志", "趕快")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("loja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loja", "店"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("loli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loli", "蘿莉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("louv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louv", "讃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lupa", "🔎 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("luva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luva", "手套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("luxo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luxo", "贅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maca", "🍎 "), "🍎 ", " 苹果 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mais", "添"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mama", "妈妈"), "妈妈", "母")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mand")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mand", "令"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mant", "持"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mapa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mapa", "図"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("praz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("praz", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medi", "医"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medo", "怖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mega")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mega", "兆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meig", "優"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meio", "半"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mian")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mian", "緬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("miau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miau", "喵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mini")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mini", "小"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("moda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moda", "時裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("modo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("modo", "模"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soja", "醬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mori")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mori", "森"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morr", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("muro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muro", "牆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nada", "莫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nasc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nasc", "産"), "産", "產")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("neto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neto", "孫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("neve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neve", "雪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nojo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nojo", "厭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nome")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nome", "名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nota", "记"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nova", "新"), "新", "若")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("novo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("novo", "若"), "若", "新")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ocup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ocup", "忙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("odio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("odio", "怨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oleo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oleo", "油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "👁️"), "👁️", "目")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("onda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onda", "波 "), "波 ", " 🌊 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("onde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onde", "哪里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oper", "操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oque", "因此"), "因此", "啥")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("todo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todo", "整"), "整", "全")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("osso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("osso", "骨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("otxi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("otxi", "陥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ouro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouro", "金"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("outr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outr", "也"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pág")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pág", "頁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pais", "國"), "國", "亲", "鄉村")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("para")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("para", "至"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parc", "似"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("part")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("part", "折"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pass", "通"), "通", "過")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peco", "請我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pela", "为"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pele", "皮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pelo", "毛"), "毛", "为")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pena", "ン"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pens", "🤔 "), "🤔 ", " 想 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pera")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pera", "梨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perd", "掉"), "掉", "負", "丟")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peru")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peru", "🇵🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peso", "重"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pesq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pesq", "搜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pess")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pess", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("raro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("raro", "僻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pila")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pila", "屌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pino", "釵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pint")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pint", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pior", "陥"), "陥", "更坏")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pipa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pipa", "琵琶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plan", "計"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pode")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pode", "可"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pose")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pose", "姿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("post")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("post", "稿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poup", "貯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("povo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("povo", "民"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ppot")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ppot", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pres", "逮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prev", "預覽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pura", "純"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("puro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("puro", "純"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("puta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("puta", "婊子"), "婊子", "賤人")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("qual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qual", "哪"), "哪", "何")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("que7")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que7", "啥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quei", "凯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quem", "誰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quiu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiu", "究"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rabo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rabo", "臀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("raca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("raca", "人種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("raiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("raiz", "艮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rapt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapt", "拐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rato", "鼠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("real")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("real", "實"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("refl")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("refl", "映"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rest")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rest", "剩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("riso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("riso", "笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rock")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rock", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roda", "輪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rolo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rolo", "卷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("romp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("romp", "破"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rosa", "粉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rosn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rosn", "哮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roub", "盗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roxo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roxo", "紫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sabe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabe", "知彼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saco", "袋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saga", "篇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sair")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sair", "出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sala", "室"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("salv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salv", "救"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sapo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapo", "蟾蜍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("scit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("scit", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seco", "荒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("semi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semi", "準"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sent", "坐"), "坐", "覺")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sexi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sexi", "性感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sexo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sexo", "性交"), "性交", "性")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sino", "鈴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soar", "響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobe", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("solt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solt", "鬆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soni", "索尼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sono", "睡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sopr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sopr", "吹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ssel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ssel", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suma", "總"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sumo", "汁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suor", "汗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suxi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suxi", "🍣 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("taca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taca", "🏆 "), "🏆 ", " 盃 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("taro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taro", "芋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("taxa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taxa", "率"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tche")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tche", "扯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tchi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchi", "吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teia", "網"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teim", "頑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tema", "題"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teta", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ting")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ting", "染"), "染", "亭")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tipo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tipo", "種"), "種", "類")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tiro", "射"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tofu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tofu", "豆腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("toma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toma", "苫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("torc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("torc", "絞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("torn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("torn", "成"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trai", "叛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tras", "背"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trem", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tres", "3"), "3", "三")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubo", "管"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tudo", "一切"), "一切", "全")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("urso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("urso", "熊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("usar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("usar", "使用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("util")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("util", "用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vaca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vaca", "牛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vale")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vale", "谷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vara", "竿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vaso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vaso", "脈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("veio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veio", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("venc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venc", "胜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vend", "賣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("venh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venh", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("verd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verd", "真"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("verg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verg", "恥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("viag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viag", "撇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vice")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vice", "副"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vida", "命"), "命", "人生")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("viol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viol", "提琴"), "提琴", "犯")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("voar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voar", "飞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("voce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voce", "您"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("volt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("volt", "帰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xiao", "蕭"))), true, true);}


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates5() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("xtre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xtre", "極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("abrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abrev", "略"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("abuso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abuso", "虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acada", "毎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acido", "酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acolh", "歡迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acord")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acord", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adeus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adeus", "以神"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adopt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adopt", "飼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afiar", "研"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afund")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afund", "沉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agarr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarr", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agora", "今"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aguia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguia", "鵰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ainda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ainda", "尚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alarg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alarg", "拡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aleat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleat", "随機"), "随機", "随")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alinh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alinh", "排"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alter", "変"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aluno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aluno", "徒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amais", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ambos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ambos", "雙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amiga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amiga", "友"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amigo", "友"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("andar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("andar", "走"), "走", "樓")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anima", "运畫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("antes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antes", "先"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apert", "締"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apanh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apanh", "攫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apont")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apont", "指"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apost")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apost", "賭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("areia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("areia", "石尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arroz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arroz", "米"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("artes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artes", "術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aspir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspir", "抽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atode")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atode", "办"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atomo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atomo", "原子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atras", "遲"), "遲", "背后")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aviso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aviso", "注意 "), "注意 ", " 提醒 ", " ⚠ ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("axila")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("axila", "脇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bafer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bafer", "緩衝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baile")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baile", "舞会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baixa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixa", "下"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixo", "下"), "下", "↓", "悄", "低")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banbu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banbu", "竿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banco", "銀行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banda", "乐队"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banho", "浴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("barco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barco", "艇 "), "艇 ", " 🚤 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bebed")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bebed", "醉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bejes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bejes", "米人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("birus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("birus", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bolha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolha", "泡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bomba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bomba", "爆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bonit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonit", "麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bosta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bosta", "粪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("braco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("braco", "腕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brilh", "輝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bruta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruta", "凶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bruto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruto", "凶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bruxa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruxa", "萬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("burro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burro", "驴"), "驴", "笨")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("butao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("butao", "按鈕"), "按鈕", "釦")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cacar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cacar", "狩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cache")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cache", "緩存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("calmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calmo", "穏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("calor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calor", "暑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campo", "田"), "田", "野")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canal", "頻道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canto", "唱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carga", "荷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carma", "业"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carne")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carne", "肉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nobre", "贵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carro", "車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carta", "牌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("catar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catar", "🇶🇦 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cauda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cauda", "尾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caule")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caule", "莖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("causa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("causa", "因"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("certo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("certo", "✅ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cesto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cesto", "籃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chave")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chave", "鍵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cheio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheio", "滿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chiis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiis", "🇨🇳 "), "🇨🇳 ", " 中國 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chile")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chile", "🇨🇱 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chilr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chilr", "鳴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("china")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("china", "中華 "), "中華 ", " 🇨🇳 ", " 🇹🇼 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chris")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chris", "克里斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chupa", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chuva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chuva", "☔ "), "☔ ", " 雨 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cienc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cienc", "科"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cinza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cinza", "灰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("clara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clara", "明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("claro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("claro", "明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("clube")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clube", "體育會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobra", "蛇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cocar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cocar", "搔çar"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coisa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coisa", "物"), "物", "事")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coiso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coiso", "個"), "個", "事")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colar", "粘貼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colet", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comec", "始"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comer", "食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("condu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("condu", "騎車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conta", "賬户"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corda", "弦"), "弦", "繩")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coroa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coroa", "👑 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corpo", "身"), "身", "身体", "體", "体")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corro", "蝕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("costa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("costa", "背"), "背", "岸")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("couro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("couro", "革"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cover")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cover", "翻唱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cozer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozer", "熟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("credo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("credo", "信條"), "信條", "教條")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("criar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criar", "作成"), "作成", "制")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("criti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criti", "批"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cruel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cruel", "酷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("culpa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culpa", "責"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("curar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curar", "治"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("curso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curso", "講"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dados")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dados", "信息"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("danmu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("danmu", "彈幕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("decid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decid", "決"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("decol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decol", "脱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("decor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decor", "飾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deita", "伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("delas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delas", "她們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deles", "他們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dente", "牙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desab", "墜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desde", "以上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desej")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desej", "欲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("destr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destr", "破"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("detet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("detet", "偵"), "偵", "檢")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deusa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deusa", "神"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dever")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dever", "應該"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diabo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diabo", "魔鬼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("digam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("digam", "言達吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dizer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dizer", "言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobra", "配音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doido", "狂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("drama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("drama", "劇集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("droga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("droga", "乐药"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dupla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dupla", "兩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("email")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("email", "电郵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emdir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emdir", "轉播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("engan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("engan", "骗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enrol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enrol", "卷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entao", "就"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entra", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entre", "介"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("subir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subir", "升"), "升", "昇")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escal", "登"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escov", "擦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esper", "待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estar", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("etnia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("etnia", "民族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ezato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ezato", "確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exced")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exced", "越"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("excit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excit", "活"), "活", "兴奮")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exist", "存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("expli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expli", "解釋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extre", "極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("facao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("facao", "刀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("falar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falar", "話"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("falha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falha", "失"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("falso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falso", "偽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fará")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fará", "請"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("favor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favor", "頼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fazer", "作"), "作", "做")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("febre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("febre", "病"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feder", "臭"), "臭", "聯合")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feira", "曜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feliz", "喜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fenix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fenix", "鳳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feroz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feroz", "猛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ferro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferro", "鉄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("filme")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filme", "电影"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("filtr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filtr", "濾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("final")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("final", "決賽"), "決賽", "底")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fluor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluor", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fluxo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluxo", "流"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("foder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foder", "肏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("folha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("folha", "葉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fonia", "樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("forca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forca", "力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("forma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forma", "形"), "形", "樣")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("forte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forte", "強"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fosse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fosse", "居让"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fosso", "堀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fraco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fraco", "弱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frade", "修士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frase")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frase", "闩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fruta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fruta", "果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fruto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fruto", "果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fuder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fuder", "操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fundo", "深"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ganda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganda", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ganza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganza", "大麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("garfo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garfo", "叉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("garra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garra", "爪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gente", "人人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("geral")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geral", "一般"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("giria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("giria", "俚語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gordo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gordo", "胖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("graus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("graus", "度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grelh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grelh", "炙烤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grupo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grupo", "群"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guerr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerr", "戰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guine", "🇬🇳 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("habit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habit", "慣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hanes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hanes", "韓國語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hanis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hanis", "🇰🇷 "), "🇰🇷 ", " 韓國 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("heroi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("heroi", "英雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hidro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hidro", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hiper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hiper", "巨大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hoije")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hoije", "今日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("homem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("homem", "男"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hotel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hotel", "宾館"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hunon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hunon", "糊弄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ianos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ianos", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("idade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("idade", "年齡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ideia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ideia", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("igual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igual", "同"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("india")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("india", "🇮🇳 "), "🇮🇳 ", " 天竺 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inger")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inger", "吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inser")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inser", "插"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("insta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("insta", "即"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("intro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intro", "入場曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iquii")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iquii", "爱奇藝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irma~")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irma~", "姐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irrit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irrit", "煩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("islao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("islao", "回教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("japao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japao", "🇯🇵 "), "🇯🇵 ", " 日本 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("joana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joana", "瓢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jogar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogar", "玩"), "玩", "玩耍")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jovem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jovem", "子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("junto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("junto", "合"), "合", "共")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("justo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("justo", "正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ladra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladra", "賊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lagoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagoa", "池"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lapis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lapis", "✏ "), "✏ ", " 笔 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("larga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("larga", "広"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("largo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largo", "広"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("latim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("latim", "拉丁字母"), "拉丁字母", "拉丁")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lavar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lavar", "洗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leite", "奶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lembr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lembr", "記憶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lenta", "遲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lento", "遲"), "遲", "慢")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leque", "扇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lider")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lider", "首"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("linha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linha", "緣"), "緣", "线")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lirio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lirio", "百合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("livre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livre", "自由"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("livro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livro", "書本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("local")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("local", "所"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("longa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("longa", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("longe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("longe", "遥"), "遥", "遠")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lorde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lorde", "主"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lotus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lotus", "莲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("louco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louco", "狂乱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lugar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lugar", "席"), "席", "位")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lutar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lutar", "搏鬥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("macho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macho", "雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("macio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macio", "軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("magia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magia", "魔術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("magra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magra", "薄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("magro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magro", "薄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maike")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maike", "邁克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maior", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mamas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamas", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("manga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manga", "芒果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("manha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manha", "晨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("manta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manta", "毯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maoem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maoem", "扌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maona")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maona", "扌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maono", "扌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marca", "標"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marco", "記載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maria", "瑪麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("massa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("massa", "質量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("matar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("matar", "殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("media")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("media", "平均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medio", "平均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("melao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melao", "瓜 "), "瓜 ", " 🍈 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meloa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meloa", "瓜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("menos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menos", "減"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mente", "然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("merda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("merda", "💩 "), "💩 ", " 粪 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meses", "朋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mesma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mesma", "刚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mesmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mesmo", "刚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("metro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metro", "地铁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("micro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("micro", "微"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("minha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("minha", "我的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mirar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mirar", "狙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("moeda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moeda", "銭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morre", "去死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morte", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morto", "死了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mostr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mostr", "示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("motiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("motiv", "促動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mudar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mudar", "變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("muito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muito", "挺"), "挺", "多", "夥")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mundo", "界"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("murch")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murch", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("museu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("museu", "博物館"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mutuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mutuo", "互"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naboa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naboa", "閑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nacao", "邦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naoou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naoou", "沒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nariz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nariz", "吸动力器 "), "吸动力器 ", " 👃 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("natal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natal", "聖诞節"), "聖诞節", "乡")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("navio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("navio", "艦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nebul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nebul", "曇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("negro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("negro", "暗人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nervo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervo", "神經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nevoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nevoa", "霧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ninho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ninho", "巢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("noite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noite", "🌃 "), "🌃 ", " 夜 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("noiva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noiva", "嫁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("norma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("norma", "規範"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("norte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("norte", "北"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nosso", "我們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("numer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numer", "数"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nuvem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nuvem", "☁ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obstr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obstr", "礙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obvio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obvio", "显"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("odeio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("odeio", "憎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oeste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oeste", "西"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olhos", "👀 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("omais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omais", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ondul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ondul", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ontem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ontem", "昨日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("opior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opior", "最低"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ordem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordem", "秩"), "秩", "令")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("orgao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orgao", "官"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oscil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oscil", "摆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("otaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("otaco", "御宅族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ouvir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouvir", "聽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pagar", "払"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("palma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palma", "掌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("panda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panda", "熊貓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parar", "止"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parec", "亡"), "亡", "似")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parte", "分"), "分", "發")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passo", "步"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pasta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pasta", "漿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("patio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("patio", "庭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pavao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pavao", "孔雀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pavor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pavor", "戦慄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pedir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedir", "請"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pedra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedra", "石"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peido", "屁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peito", "胸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("peixe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peixe", "魚 "), "魚 ", " 🐟 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("penis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("penis", "男根"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pente", "梳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perna")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perna", "腿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perto", "近"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessg", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("piano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("piano", "钢琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pilar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pilar", "柱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pilha", "堆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pinta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pinta", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pisar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pisar", "踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plano", "平"), "平", "计畫", "片")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pobre", "貧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poder", "力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("podre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("podre", "腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponto", "点"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porca", "🐷 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porco", "🐷 "), "🐷 ", " 豕 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porra", "慘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porta", "戶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porte", "提"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poste", "柱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pouca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pouca", "少"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pouco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pouco", "少"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("praga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("praga", "疫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prata", "銀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prazo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prazo", "截"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("preco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preco", "價"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prego", "釘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prend", "逮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("presa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presa", "獵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("press")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("press", "圧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("preto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preto", "黑"), "黑", "黑人")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prova", "證"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("punho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("punho", "拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quebr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quebr", "斷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("queda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("queda", "落"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quimi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimi", "化"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quion", "阿虛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quiou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiou", "境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quiuu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiuu", "究"), "究", "級")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rampa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rampa", "坂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rapid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapid", "速"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("razao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("razao", "理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("receb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("receb", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("refaz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("refaz", "改"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("regra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("regra", "規"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reino", "王國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relax")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relax", "愈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relig", "宗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("remar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("remar", "漕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("renov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("renov", "續"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reset")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reset", "重置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resid", "居"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ressu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ressu", "复"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resto", "遺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("retir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retir", "退"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("revel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revel", "揭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("risos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("risos", "😂 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ritmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ritmo", "拍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roque", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubo", "略奪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roupa", "衣"), "衣", "衣服")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ruque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ruque", "魯克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("russo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("russo", "俄語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sabao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabao", "石鹸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saber")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saber", "知"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sabor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabor", "味"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saite", "網站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saiya")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saiya", "菜野"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("salao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salao", "堂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("salto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salto", "跳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("salvo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salvo", "安"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("santa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("santa", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("santo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("santo", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saude")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saude", "健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("segur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segur", "持"), "持", "安")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seita", "教條"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("senti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("senti", "感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("serio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serio", "忍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("setsu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("setsu", "說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("siria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("siria", "🇸🇾 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sitio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sitio", "站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobre", "关于"), "关于", "關於")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sodio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sodio", "鈉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("solto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solto", "緩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("somos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("somos", "我們是"), "我們是", "是")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sonho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sonho", "夢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soque", "但"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sorte", "运气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suchi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suchi", "🍣 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sugar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sugar", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suica", "🇨🇭 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suite", "套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("super")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("super", "超"), "超", "越")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sutil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sutil", "微"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tarde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tarde", "晚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tecla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tecla", "鍵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tecno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tecno", "科技"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("telha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("telha", "瓦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tempo", "时"), "时", "天气")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tenda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tenda", "⛺ "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("terra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terra", "🌍 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tesao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tesao", "勃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teste", "试"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tetas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tetas", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("texto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("texto", "📃 "), "📃 ", " 闫 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ticia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ticia", "聞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tique", "習慣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tirar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tirar", "取"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tocar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tocar", "触"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("todos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todos", "皆"), "皆", "全", "大家")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tomar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomar", "服"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("toque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toque", "提示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("total")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("total", "够"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tradu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tradu", "譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trapo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trapo", "巾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trein")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trein", "煉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tribo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tribo", "民族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tumor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tumor", "腫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("turno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("turno", "班"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ueder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ueder", "偉瑟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uniao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uniao", "聯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unica", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unico", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unido", "聯合"), "聯合", "联合")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vacuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vacuo", "虛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vaice")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vaice", "罪惡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("valor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("valor", "値"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vapor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vapor", "汽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vasto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vasto", "浩"), "浩", "洋洋")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("veado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veado", "鹿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("velha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velha", "古"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("velho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velho", "古"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("veloc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veloc", "速"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vento", "几 "), "几 ", " 🍃 ", " 風 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("verde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verde", "翠"), "翠", "綠")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vezes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vezes", "倍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("video")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("video", "动圖"), "动圖", "視頻")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vigor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vigor", "势"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vinda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vinda", "迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vindo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vindo", "來着"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("virar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virar", "翻"), "翻", "向")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("visao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visao", "目光"), "目光", "視")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("visit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visit", "访"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vista", "観"), "観", "視圖")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("visto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visto", "閲覧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vital", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("viver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viver", "住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vivid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vivid", "濃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("voces")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voces", "您們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("volta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("volta", "回"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vomit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vomit", "吐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vosso", "你們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xtrem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xtrem", "極"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates6() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("perca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perca", "丟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olhar", "貝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("abanar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abanar", "振"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acabar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acabar", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acabei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acabei", "終了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carreg", "承"), "承", "置")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acucar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acucar", "糖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("acumul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acumul", "積"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adulto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adulto", "大人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("advert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("advert", "警"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afogar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afogar", "溺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afroux")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afroux", "弛"), "弛", "緩")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarra", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agenda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agenda", "議程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alcool")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alcool", "酒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aldeia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aldeia", "庄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alemao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alemao", "德文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alface")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alface", "生菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alguem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alguem", "某人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alguns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alguns", "些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("almoco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("almoco", "昼食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alteza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alteza", "陛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("altura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("altura", "高度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aluzar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aluzar", "暮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amanha", "明日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amarga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amarga", "少糖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amargo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amargo", "少糖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ameixa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ameixa", "李"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amolar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amolar", "砥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amostr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amostr", "表"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("angola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("angola", "🇦🇴 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("angulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("angulo", "角"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anoite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anoite", "夕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anotac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anotac", "記得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("antigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antigo", "舊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cancel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cancel", "消"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apenas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apenas", "只"), "只", "僅")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apesar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apesar", "虽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apreci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apreci", "贊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aprend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprend", "學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aquela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aquela", "那個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aquele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aquele", "那個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arranh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arranh", "掻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("artigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artigo", "条"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arvore")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvore", "木"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ascend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ascend", "登"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aspera")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspera", "粗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aspero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspero", "粗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assist", "帮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assust", "怯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atalho", "近道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ataque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ataque", "击"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aument")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aument", "增"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("avolta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avolta", "囲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bocaba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bocaba", "吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("backup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("backup", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bairro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bairro", "區"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baixar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixar", "下載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("balanc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balanc", "振"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("balouc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balouc", "揮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banana", "香蕉 "), "香蕉 ", " 🍌 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("batata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batata", "🥔 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("batida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batida", "鼓動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("biblia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("biblia", "聖經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("biruss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("biruss", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("birusu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("birusu", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bocaeu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bocaeu", "哦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bonita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonita", "綺麗"), "綺麗", "漂亮")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bonito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonito", "漂亮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("branca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("branca", "白"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("branco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("branco", "白"), "白", "白人")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brasil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brasil", "🇧🇷 "), "🇧🇷 ", " 巴西 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brutal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brutal", "給力"), "給力", "真棒", "厉害")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("buraco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("buraco", "穴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cabeca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabeca", "头"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cabelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabelo", "髪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cabrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabrao", "王八蛋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caixao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caixao", "棺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("calcul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calcul", "算"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parque", "場"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cancao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cancao", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cancro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cancro", "癌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caneca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caneca", "杯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canesa", "韓國人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caneta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caneta", "🖊 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canhao", "砲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantar", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cantor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantor", "歌手"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carvao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carvao", "炭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("castig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("castig", "懲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("catana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catana", "刀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cavalo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cavalo", "马 "), "马 ", " 🐎 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("centro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("centro", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cereja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cereja", "櫻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chamar", "呼 "), "呼 ", " 📣 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chapeu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chapeu", "帽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("charla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("charla", "談"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chegar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chegar", "到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chichi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chichi", "尿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chines")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chines", "中華語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("choque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("choque", "惊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chorar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chorar", "哭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("church")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("church", "教会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cidade", "市"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("classe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("classe", "類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("clicar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clicar", "選擇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("clique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clique", "点擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cmcmus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cmcmus", "始曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cobrir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobrir", "網羅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("codigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("codigo", "码"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coelho", "兔"), "兔", "兎")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colaps")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colaps", "崩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colega")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colega", "相"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colher", "🥄 "), "🥄 ", " 勺 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comeco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comeco", "開始"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comigo", "和我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comite", "委"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comose")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comose", "似乎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("compar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compar", "比"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("compil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compil", "輯"), "輯", "集")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confus", "混亂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("congel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("congel", "凍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conseg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conseg", "可能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("consol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consol", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("constr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constr", "築"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contar", "数"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conter", "广"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contra", "對"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("convid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convid", "邀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cooper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cooper", "協"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("copiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("copiar", "复制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cordel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cordel", "紐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coreia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coreia", "高麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("correr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correr", "跑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corrig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corrig", "订正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coruja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coruja", "梟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cozido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozido", "熟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cresci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cresci", "成長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cristo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cristo", "基督"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("culhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culhao", "㞗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dantes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dantes", "从前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("defend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("defend", "防"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("defesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("defesa", "守備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deitar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitar", "伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deixar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deixar", "交給"), "交給", "讓")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demais", "太"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demoni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demoni", "妖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("denovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("denovo", "重新"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentro", "个中"), "个中", "内")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("depe10")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depe10", "辛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("depois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depois", "后"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("derret")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derret", "融"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("descai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descai", "垂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desejo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desejo", "願望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desenh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenh", "描"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desliz", "摺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("destes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destes", "這些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("detudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("detudo", "都"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("devast")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devast", "驅逐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diante", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diario", "日記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("difere")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("difere", "別"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("direto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direto", "直"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("discut")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("discut", "論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dispar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dispar", "發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("disper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disper", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("divert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("divert", "乐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dobrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobrar", "彎"), "彎", "配音")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doenca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doenca", "病"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dormir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dormir", "寝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doutor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutor", "博士"), "博士", "医者")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dragao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dragao", "🐉 "), "🐉 ", " 龙 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("duvida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("duvida", "疑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("efeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("efeito", "效"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ejacul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ejacul", "漏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("elabor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elabor", "講"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eletro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eletro", "电"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("elimin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elimin", "淘汰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("embora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embora", "虽然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emcada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emcada", "毎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emocao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emocao", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("empurr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empurr", "推"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("empate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empate", "平局"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("encerr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encerr", "鎖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("encolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encolh", "縮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("endura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("endura", "忍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enredo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enredo", "腳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ensaio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ensaio", "訓練"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ensino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ensino", "教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entend", "理解"), "理解", "解")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enterr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enterr", "埋"), "埋", "葬")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entert", "娛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entrar", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entreg", "納"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entret")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entret", "娛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("equipa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equipa", "隊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escada", "梯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escola", "校"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escolh", "選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escova", "刷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escrav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrav", "隸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrev", "✍ "), "✍ ", " 寫 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escudo", "盾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escuro", "闇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esforc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esforc", "努"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esfreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esfreg", "擦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espaco", "間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espant", "驚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espero", "希望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espert", "賢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espeto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espeto", "串"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espiar", "覗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esporr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esporr", "漏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esposa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esposa", "妻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estava")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estava", "在了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estilo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estilo", "範"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estrit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrit", "严"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eterno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eterno", "永"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("europa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("europa", "歐洲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ezamin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ezamin", "检"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("modelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("modelo", "模"), "模", "型")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exaust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exaust", "尽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("expalh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expalh", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("explod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explod", "爆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("explor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explor", "探"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("export")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("export", "匯出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extend", "張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extens", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("falcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falcao", "隼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fantas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fantas", "奇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("favela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favela", "貧民窟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fedido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fedido", "臭了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fenixa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fenixa", "凰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("figado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("figado", "肝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("finais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("finais", "总决賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fiquei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fiquei", "得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fisica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fisica", "物理學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flache")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flache", "閃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flecha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flecha", "箭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("floide")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floide", "佛洛伊德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fodido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fodido", "苦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fornec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fornec", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fostes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fostes", "行您了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frango")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frango", "鶏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frecam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frecam", "頻道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frente", "前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fresca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fresca", "鮮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fresco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fresco", "鮮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("funcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("funcao", "作用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("futuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futuro", "未來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gaiola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaiola", "籠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gananc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gananc", "貪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gaviao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaviao", "鷹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gelado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gelado", "冷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("genero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("genero", "類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gentil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gentil", "友好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gerais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gerais", "一般"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gloria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gloria", "榮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gostar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gostar", "欢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grande")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grande", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gratis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gratis", "無償"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grecia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grecia", "🇬🇷 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grossa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grossa", "厚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grosso", "厚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guerra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerra", "戰爭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("habito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habito", "癖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hancar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hancar", "漢字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("histor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("histor", "史"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("humano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("humano", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("idiota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("idiota", "蠢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("igreja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igreja", "教堂"), "教堂", "寺")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ilumin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilumin", "照"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imagem", "圖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imperi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperi", "帝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("implor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implor", "乞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imprim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imprim", "印"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inclin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inclin", "傾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("infame")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("infame", "臭名昭著"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inform")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inform", "告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ingles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ingles", "英語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inicio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inicio", "初"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inseto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inseto", "虫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("instal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instal", "載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("invent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("invent", "开明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irmaos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmaos", "兄弟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("isolar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isolar", "獨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("italia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("italia", "🇮🇹 "), "🇮🇹 ", " 伊國 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("janela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("janela", "窗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jantar", "夕食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jardim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jardim", "庭"), "庭", "园")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("joelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joelho", "膝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jordao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jordao", "佐敦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("juntos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juntos", "共"), "共", "一起")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("labios")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("labios", "唇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ladrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladrao", "賊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lament")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lament", "哀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("largar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largar", "离"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("levant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("levant", "起"), "起", "舉")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("libert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("libert", "放"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limite", "限"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limpar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limpar", "拭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lingua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lingua", "👅 "), "👅 ", " 言語 ", " 舌 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("linque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linque", "📥 "), "📥 ", " 🖇 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("livros")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livros", "冊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("louvar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louvar", "讃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("macaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macaco", "猿 "), "猿 ", " 🐒 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maique", "邁克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mamilo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamilo", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maquin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquin", "機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("margem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("margem", "余裕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marido", "夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mascul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mascul", "雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mastig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mastig", "噛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medico", "医者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("menina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menina", "女児"), "女児", "幼女")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("menino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menino", "男児"), "男児", "幼児")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("merece")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("merece", "該"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mestre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mestre", "師"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("metodo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metodo", "法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mexico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mexico", "🇲🇽 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("miguel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miguel", "米高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("milhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("milhao", "百万"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mistur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mistur", "混"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morrer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morrer", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("morreu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morreu", "死了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("semove")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semove", "运"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("muitas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitas", "多"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mulher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mulher", "女"), "女", "婦")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("murcha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murcha", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("murcho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murcho", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("musica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musica", "曲 "), "曲 ", " 🎶 ", " 音乐 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naaodo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naaodo", "沒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nervos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervos", "神經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("normal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("normal", "普通"), "普通", "普", "正常")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nucleo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nucleo", "核"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("numero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numero", "号"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("objeto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("objeto", "象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oculos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oculos", "眼鏡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("omaior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omaior", "最高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("opcoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opcoes", "選項"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("opniao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opniao", "議"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oposto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oposto", "幸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("orbita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orbita", "軌道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("orelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orelha", "耳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("origem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("origem", "源"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("outono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outono", "秋"), "秋", "秋季")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ouvido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouvido", "耳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ovelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovelha", "羊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pacote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pacote", "包"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pagina", "门"), "门", "頁")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paixao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paixao", "戀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("panama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panama", "🇵🇦 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("panpon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panpon", "盤點"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parede")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parede", "壁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partir", "折"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("patrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("patrao", "司"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pegada", "蹤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pendur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pendur", "吊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perceb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perceb", "懂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perigo", "危"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("permit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("permit", "允"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perola", "珠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perseg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perseg", "追"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessei", "人生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoa", "亻"), "亻", "人")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("planej")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planej", "企"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("planta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planta", "植"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plasti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plasti", "塑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poeira", "尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ponder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponder", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exclui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exclui", "除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porque", "为啥"), "为啥", "为什么", "因為")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("portas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portas", "門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poster")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poster", "隔人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poupar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poupar", "貯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pralma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pralma", "銀魂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prazer", "愉快"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("precis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("precis", "当"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("premio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("premio", "賞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prepar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prepar", "準備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("presas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presas", "獠齒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("privad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("privad", "私"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("procur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("procur", "尋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("produz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produz", "産"), "産", "造")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proibi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proibi", "🚫 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("propag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("propag", "傳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proteg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proteg", "守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("psique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("psique", "靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pulmao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pulmao", "肺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quando")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quando", "哪時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quanto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quanto", "幾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quarto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quarto", "屋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quatro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quatro", "4"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quente", "熱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("querer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("querer", "望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quimoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimoi", "恶心"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quioco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quioco", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quiocu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiocu", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ranfan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ranfan", "兰芳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ranque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ranque", "排行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rapido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapido", "快"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reacao", "反応"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("recife")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recife", "暗礁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("recolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recolh", "募"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("regiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("regiao", "域"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rejeit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rejeit", "拒"), "拒", "悶")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relevo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relevo", "显示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("repete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("repete", "再"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("report")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("report", "告報"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reprim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reprim", "抑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reprov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reprov", "斥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reserv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reserv", "訂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resist", "耐"), "耐", "抗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resumo", "概"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("retorc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorc", "捻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("retorn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorn", "歸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rotina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rotina", "課"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roubar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubar", "盗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("russia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("russia", "🇷🇺 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sancao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sancao", "制裁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sangue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sangue", "血"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sapata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapata", "束"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sapato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapato", "鞋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("satisf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("satisf", "滿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saudad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saudad", "憧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("secret")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("secret", "泌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seculo", "世紀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seguro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguro", "安"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("semana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semana", "週"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sempre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sempre", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("senhor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("senhor", "君"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sentar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentar", "坐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sessao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sessao", "屆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("severo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("severo", "厳重"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("social")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("social", "社"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sofrer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sofrer", "遭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("solido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solido", "固況"), "固況", "固")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sonora", "音的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("spamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("spamar", "詐欺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("subita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subita", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("subito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subito", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("subtra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subtra", "減"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suecia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suecia", "🇸🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suport")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suport", "支"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suprim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suprim", "抑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("susurr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("susurr", "囁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tabaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tabaco", "煙草"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("talvez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("talvez", "也許"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tambem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tambem", "亦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tchuan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchuan", "串"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teimos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teimos", "頑固"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("templo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("templo", "殿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("temzem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temzem", "天然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("temzen")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temzen", "天然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tensao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tensao", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("termin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("termin", "終"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tibete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tibete", "藏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tingir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tingir", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tomate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomate", "🍅 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("topico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("topico", "題"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tratar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tratar", "治"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trazer", "拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("treino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treino", "訓練"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tremer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tremer", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tripla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tripla", "三分"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("triste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("triste", "悲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trofia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trofia", "肥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tubara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubara", "鮫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vagina", "膣"), "膣", "屄")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vencer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vencer", "胜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vender", "賣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("verifi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verifi", "检"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("versao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("versao", "版"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vestir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vestir", "穿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("viajar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viajar", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("virgem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virgem", "童貞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("volver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("volver", "轉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("xtremo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xtremo", "極"))), true, true);}


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates7() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("abandon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abandon", "弃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("absolut")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("absolut", "絶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adicion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adicion", "加"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adormec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adormec", "眠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aescuta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aescuta", "房"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agarrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarrar", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agencia", "庁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alcunha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alcunha", "称"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alegrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alegrar", "喜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("algures")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("algures", "某地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alianca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alianca", "聯盟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aliment")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aliment", "餵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alinhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alinhar", "揃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amarelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amarelo", "黃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("america")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("america", "美州"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amostra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amostra", "例示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("antecip")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antecip", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apaixon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apaixon", "惚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apelido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apelido", "昵稱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apodrec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apodrec", "腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aprovar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprovar", "默認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("armarse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("armarse", "逞强"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("artesao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artesao", "匠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aseguir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aseguir", "次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assento", "席"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assinal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinal", "任"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assunto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assunto", "臣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atraido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atraido", "惹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atropel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atropel", "轹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("austria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("austria", "🇦🇹 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("avancar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avancar", "前进"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("balanco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balanco", "餘額"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("barriga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barriga", "腹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("batalha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batalha", "鬥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bateria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bateria", "电池"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("beji-ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beji-ta", "比達"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("belgica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("belgica", "🇧🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bemvind")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bemvind", "歡迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bolinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolinha", "圓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brigada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brigada", "團"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brincar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brincar", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cadaver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadaver", "屍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cadeado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadeado", "鎖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cadeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadeira", "椅子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caixote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caixote", "桶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("camarao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camarao", "蝦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caminho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminho", "道"), "道", "径")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campeao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeao", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campeos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeos", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canabis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canabis", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canhamo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canhamo", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cansada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cansada", "累"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cansado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cansado", "累了"), "累了", "累")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("capital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("capital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carater")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carater", "字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caralho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caralho", "膣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carbono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carbono", "碳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("castelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("castelo", "城"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("centimo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("centimo", "分錢"), "分錢", "分€")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("central")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("central", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cerebro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cerebro", "腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("certeza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("certeza", "必"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chaomau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaomau", "坏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cheiode")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheiode", "漫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chiises")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiises", "中pais人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cigarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarra", "蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cigarro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarro", "吸煙管"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cintura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cintura", "腰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("circulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circulo", "陣"), "陣", "◯")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ciument")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ciument", "睨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cocoras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cocoras", "蹲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coentro", "香菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coletar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coletar", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comboio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comboio", "列車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comedia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comedia", "喜劇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comemor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comemor", "祝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("compara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compara", "比"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("complet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("complet", "完成"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comprar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comprar", "買"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("compree")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compree", "認識"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("concavo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concavo", "凹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("concord")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concord", "同意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confiar", "任"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confund")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confund", "惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("congrat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("congrat", "拜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conhece")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhece", "会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("constit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constit", "憲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contact")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contact", "絡"), "絡", "連絡")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("continu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continu", "进"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("control")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("control", "控"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("convers")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convers", "聊"), "聊", "說")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("convert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convert", "換"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("convexo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convexo", "凸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("convite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convite", "招待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coracao", "♡"), "♡", "心")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coragem", "勇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("correct")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correct", "妥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("machado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("machado", "斤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cortina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cortina", "幕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("costuma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("costuma", "曾經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cozinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozinha", "厨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("crianca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crianca", "孩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("croacia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("croacia", "🇭🇷 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cumprir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cumprir", "遵守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("senhora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("senhora", "姬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estadia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estadia", "留"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demanda", "需"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demonio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demonio", "恶靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deposit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deposit", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("derrota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derrota", "败"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desafio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desafio", "挑戰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("descasc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasc", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desejar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desejar", "欲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("despesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("despesa", "経費"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deueine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deueine", "德懷恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("devagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devagar", "慢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("devolta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devolta", "返回"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dificil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dificil", "难"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("direcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direcao", "方"), "方", "芳")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("direita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direita", "→"), "→", "右")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("direito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direito", "權"), "權", "直")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("disparo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disparo", "發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diversa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diversa", "杂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dobrado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobrado", "配音了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dominar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dominar", "支配"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edepois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edepois", "然後"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emagrec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emagrec", "瘦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emaranh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emaranh", "絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("embrulh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embrulh", "裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emissao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emissao", "播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emprego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprego", "職"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("empresa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empresa", "企業"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emprest")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprest", "貸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("energia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("energia", "能量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entanto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entanto", "而"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enticar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enticar", "誘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("equador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equador", "🇪🇨 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("erotico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erotico", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escolha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escolha", "挑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escovar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escovar", "擦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escrita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrita", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrito", "書"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esguich")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esguich", "噴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esmagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esmagar", "潰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espanha", "🇪🇸 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especie")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especie", "種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espelho", "鏡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esperar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperar", "待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esperma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperma", "精"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espinha", "脊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espinho", "刺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espreit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espreit", "傅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estacao", "季"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estadio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estadio", "🏟 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estoque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estoque", "株"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estrada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrada", "道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estreit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estreit", "狹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estrela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrela", "星"), "星", "☆")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exemplo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exemplo", "例"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("existir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("existir", "存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("experma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experma", "精液"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extenso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extenso", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extremo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extremo", "極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fabrica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fabrica", "廠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("familia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("familia", "族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feitico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feitico", "咒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("feriado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feriado", "节日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("filosof")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filosof", "哲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("financa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("financa", "金融"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("replica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("replica", "假"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fitness")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fitness", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("floresc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floresc", "咲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flutuar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flutuar", "浮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fortuna")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fortuna", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("formato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formato", "格式"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("formosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formosa", "🇹🇼 "), "🇹🇼 ", " 台灣 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("formula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formula", "式"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frangan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frangan", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("futebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futebol", "⚽ "), "⚽ ", " 足球 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("galinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("galinha", "雞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ganbare")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganbare", "加油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("garagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garagem", "輸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gautama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gautama", "釈迦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("talento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("talento", "才"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("geracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geracao", "世"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gigante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gigante", "巨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gordura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gordura", "脂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("governo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("governo", "政府"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gradual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gradual", "漸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grafica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grafica", "圖形"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gramado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gramado", "芝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grelhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grelhar", "炙烤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("grindar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grindar", "錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guardar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guardar", "衛"), "衛", "保存")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("haneses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("haneses", "韓國人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hermita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hermita", "仙人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("holanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("holanda", "🇳🇱 "), "🇳🇱 ", " 荷兰 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("horario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("horario", "日程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hungria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hungria", "🇭🇺 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imperio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperio", "帝國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imposto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imposto", "稅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("infeliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("infeliz", "不歡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inimigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inimigo", "敵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iningue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iningue", "裏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inscrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inscrev", "申"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("insecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("insecto", "虫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inserir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inserir", "插"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("instant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instant", "瞬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inverno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inverno", "冬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irjusto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irjusto", "征"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irlanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irlanda", "🇮🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irritar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irritar", "慪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jamaica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jamaica", "🇯🇲 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("japones")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japones", "日本人"), "日本人", "日本語")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jornada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jornada", "旅程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("jusetsu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jusetsu", "據說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("juvenil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juvenil", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lagarto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagarto", "蜥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lampada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lampada", "灯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("laranja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("laranja", "橙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("largura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largura", "寬度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("latente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("latente", "潛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lendido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lendido", "彩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("letonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("letonia", "🇱🇻 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ligacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ligacao", "⛓️"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ligeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ligeiro", "温馨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limpeza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limpeza", "掃除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lingjia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lingjia", "凌駕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("liquido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("liquido", "液"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maconha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maconha", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maioria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maioria", "大抵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maisque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maisque", "以上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maneira", "方法"), "方法", "法")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maquilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquilh", "妆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maquina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquina", "機器"), "機器", "機械")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marcial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marcial", "武"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("masturb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("masturb", "自慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medicao", "測定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medroso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medroso", "胆小鬼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meiodia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meiodia", "午"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("melodia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melodia", "旋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("memoria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("memoria", "内存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mentira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mentira", "謊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mescara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mescara", "臉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mianmar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mianmar", "緬甸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("miseria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miseria", "蕭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mistura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mistura", "配"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("momento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("momento", "候"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("monstro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("monstro", "兽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mundial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mundial", "界"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("natural")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natural", "然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("negativ")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("negativ", "負"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("negocio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("negocio", "商"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nervosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervosa", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nervoso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervoso", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nigeria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nigeria", "🇳🇬 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ninguem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ninguem", "虛人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("noticia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noticia", "讯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("numerar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numerar", "数"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("numeros")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numeros", "🔢 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obsceno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obsceno", "猥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obscuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obscuro", "玄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oficial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oficial", "官"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olhohan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olhohan", "ㅎ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("omelhor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omelhor", "最好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("onlaine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onlaine", "網上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("orgasmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orgasmo", "淫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oriente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oriente", "东"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("orvalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orvalho", "露"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("palacio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palacio", "宮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("palavra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavra", "词"), "词", "句", "詞")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pantano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pantano", "沼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pantazu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pantazu", "沼津"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paragem", "停"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pareceu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pareceu", "亡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("parente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parente", "亲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partido", "党"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partilh", "分享"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passada", "祖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passado", "祖"), "祖", "了")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passaro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passaro", "鳥 "), "鳥 ", " 🐤 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("present")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("present", "奉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pequeno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pequeno", "小"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perciso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perciso", "需要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perdoar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perdoar", "許"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pereira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pereira", "梨木"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pescoco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pescoco", "頸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pesquis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pesquis", "搜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessego", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("picante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("picante", "辣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pimenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pimenta", "椒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pintura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pintura", "畫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("planeta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planeta", "行星"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plantar", "植"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("policia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("policia", "警察"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("polonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("polonia", "🇵🇱 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("popular")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("popular", "人气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("porisso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porisso", "所以說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("posicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("posicao", "位"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("postura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("postura", "態"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("praguia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("praguia", "銀鷹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pratica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pratica", "実践"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("preench")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preench", "塗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pressao", "壓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("primata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primata", "霊長類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("process")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("process", "处"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("produto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produto", "積"), "積", "価")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("projeto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("projeto", "志意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prologo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prologo", "序"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proprio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proprio", "自"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proxima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proxima", "次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proximo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proximo", "次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("punicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("punicao", "懲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("purpura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("purpura", "紫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quimica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimica", "化學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rafeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rafeiro", "狗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reajust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reajust", "修"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("receber")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("receber", "受"), "受", "接到")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relacao", "關"), "關", "関")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("renunci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("renunci", "辞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reserva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reserva", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("residuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("residuo", "廢物"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("respond")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("respond", "答"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("retorno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorno", "再臨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reverso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reverso", "🔄 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("revisao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revisao", "修正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("revista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revista", "杂志"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("risonho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("risonho", "笑容"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saifora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saifora", "干"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("saiteji")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saiteji", "網址"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("salpico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salpico", "潑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("samurai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("samurai", "士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("secreto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("secreto", "秘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("segunda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segunda", "第二次"), "第二次", "乙")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("segundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segundo", "乙"), "乙", "秒")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("selecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("selecao", "精選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("semente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semente", "種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sentido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentido", "意義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("serdono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serdono", "飼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("servico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("servico", "服務"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("simbolo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("simbolo", "符"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sistema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sistema", "系"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("soldado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soldado", "兵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("somente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("somente", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sorriso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sorriso", "微笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sozinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sozinho", "孤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("superar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("superar", "越"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("surpres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("surpres", "😮 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tchihan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchihan", "吃飯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teclado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teclado", "⌨ "), "⌨ ", " 鍵盘 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tesouro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tesouro", "宝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tiquete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tiquete", "票"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("toranja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toranja", "柚子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tortura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tortura", "拷問"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("traicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traicao", "叛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esturpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esturpo", "奸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("treiler")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treiler", "預告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("treinad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treinad", "督"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tubarao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubarao", "鮫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tunisia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tunisia", "🇹🇳 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ucrania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ucrania", "🇺🇦 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("umcerto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umcerto", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unidade", "单"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uruguai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uruguai", "🇺🇾 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("usuario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("usuario", "用户"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vacante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vacante", "缺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("valente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("valente", "勇敢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sumario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sumario", "綜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vegetal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vegetal", "菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("verdade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verdade", "真"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("violino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("violino", "🎻 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("virilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virilha", "股"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vontade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vontade", "志"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vitoria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vitoria", "勝利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("residir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("residir", "居"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates8() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("acompanh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acompanh", "伴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afrouxar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afrouxar", "緩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aindanao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aindanao", "未"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alemanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alemanha", "🇩🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("almofada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("almofada", "枕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("alquemia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alquemia", "煉金術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amaldico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amaldico", "呪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ambiente", "容"), "容", "環境")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amedontr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amedontr", "惧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("androide")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("androide", "機器人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("animacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("animacao", "运畫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aniverso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aniverso", "誕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aparatus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aparatus", "装置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apertado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apertado", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apresent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apresent", "紹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("apressar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apressar", "突進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arvoredo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvoredo", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assuntos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assuntos", "務"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atirador", "癶投者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atributo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atributo", "性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aventura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aventura", "冒險"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bacteria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bacteria", "菌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baixinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixinho", "悄悄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bandeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bandeira", "旗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banheira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banheira", "呂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("banquete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banquete", "宴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bardoque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bardoque", "巴達克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("barreira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barreira", "障碍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("beisebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beisebol", "野球"), "野球", "棒球")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bloquear")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloquear", "塞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("boasorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boasorte", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bochecha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bochecha", "臉頰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bolafora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolafora", "邪球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bolibert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolibert", "播放"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bomtempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bomtempo", "涼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bulgaria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bulgaria", "🇧🇬 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("camarada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camarada", "同志"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("camaroes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camaroes", "🇨🇲 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeiro", "農夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campeoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeoes", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("capitulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("capitulo", "章"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carmesim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carmesim", "茜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carnaval")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carnaval", "狂歡節"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carreira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carreira", "生涯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chaoboca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaoboca", "吉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cigarras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarras", "蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coleccao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coleccao", "集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("colombia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colombia", "🇨🇴 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("combater")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("combater", "反对"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comercio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comercio", "貿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comprido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comprido", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conceito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conceito", "概"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conectar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conectar", "係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conflito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conflito", "葛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conforto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conforto", "安慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("construi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("construi", "作"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contente", "嬉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contesto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contesto", "大賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("continuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continuo", "连"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contribu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contribu", "貢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("correcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correcao", "正確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("corrente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corrente", "當前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dizplano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dizplano", "評"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("crocante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crocante", "脆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deacordo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deacordo", "據"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deitfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitfora", "棄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demonstr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demonstr", "表"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("descasqu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasqu", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desculpa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desculpa", "歉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desistir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desistir", "諦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desporto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desporto", "运动"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("destruid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destruid", "砕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diametro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diametro", "径"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diarreia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diarreia", "下痢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dinheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dinheiro", "貨"), "貨", "錢")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diospiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diospiro", "柿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carropro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carropro", "轉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("discurso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("discurso", "演說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("distrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distrito", "区"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ditadura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ditadura", "独裁主義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diversos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diversos", "杂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dizerpeg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dizerpeg", "討"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dobragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobragem", "配音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("émelhor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("émelhor", "要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("edificio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edificio", "🏢 "), "🏢 ", " 且 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("elefante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elefante", "象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eletrico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eletrico", "电"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("embrulha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embrulha", "裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("empurrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empurrar", "邁進"), "邁進", "推")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("encontro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encontro", "会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entender", "解"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entregar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entregar", "送"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entrelac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entrelac", "縛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("equivale")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equivale", "等"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ervanova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ervanova", "薪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escorreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escorreg", "滑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espanhol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espanhol", "西語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especial", "特"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espirito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espirito", "靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esporrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esporrar", "絶頂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esquecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquecer", "忘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esquerda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquerda", "←"), "←", "左")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("essencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("essencia", "精"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("establec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("establec", "制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estacion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estacion", "停"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estetica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estetica", "美學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estranho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estranho", "怪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estupida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estupida", "胸大無腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estupido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estupido", "笨"), "笨", "愚")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("evolucao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evolucao", "进化"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exclusiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exclusiv", "専"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("exercito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exercito", "軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("explosao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explosao", "炸裂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extender", "拡張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fantasma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fantasma", "鬼 "), "鬼 ", " 👻 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fascinar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fascinar", "魅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ferencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferencia", "差別"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("festival")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("festival", "节"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ficheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ficheiro", "文件"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("financas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("financas", "金融"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("floresta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floresta", "森"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fluencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluencia", "流量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("folclore")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("folclore", "謡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fornecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fornecer", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gabinete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gabinete", "閣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("garganta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garganta", "喉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gengibre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gengibre", "薑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gigantes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gigantes", "巨人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("glandula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("glandula", "腺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gregorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gregorio", "葛瑞格爾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guitarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guitarra", "吉他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("honduras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("honduras", "🇭🇳 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hospital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hospital", "医院"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("importar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("importar", "輸入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("impostar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("impostar", "定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("incentiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incentiv", "誘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("incepcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incepcao", "全面啟動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("incrivel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incrivel", "厉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inferior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inferior", "底"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inflatar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inflatar", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inspecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspecao", "验"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inspirou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspirou", "影響了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("instavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instavel", "暴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("intencao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intencao", "意思"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("internet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("internet", "網絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("interrog")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("interrog", "調"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("investig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("investig", "究"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irmaozao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmaozao", "兄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("juntomao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juntomao", "拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lagrimas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagrimas", "泪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lealdade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lealdade", "忠実"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leiquers")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leiquers", "湖人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lembrete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lembrete", "提醒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("leopardo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leopardo", "豹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("levantai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("levantai", "起來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limitada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limitada", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("limitado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limitado", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("linhagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linhagem", "系"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lituania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lituania", "🇱🇹 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("macarrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macarrao", "麵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mamifero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamifero", "哺乳類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maportas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maportas", "🇲🇴 "), "🇲🇴 ", " 澳門 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marciais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marciais", "武"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("material")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("material", "材"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medecina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medecina", "医學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mediario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mediario", "媒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medicina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medicina", "医學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mensagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mensagem", "消息"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("metafora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metafora", "比喩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ministro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ministro", "臣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("moldavia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moldavia", "🇲🇩 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("montanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("montanha", "山"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("multipli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("multipli", "乘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naochama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naochama", "🔇 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naofazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naofazer", "勿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("naogosto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naogosto", "👎 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("redecima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redecima", "網上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nascente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nascente", "泉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("natureza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natureza", "自然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nutricao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nutricao", "栄養"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("objetivo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("objetivo", "目標"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obrigado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obrigado", "谢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ocupacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ocupacao", "佔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olhoraiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olhoraiz", "眼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("operacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("operacao", "操作"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("opressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opressao", "弾圧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arvore10")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvore10", "本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("original")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("original", "原"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("outravez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outravez", "又"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("painatal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("painatal", "聖诞老人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("panponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panponto", "盤點"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paraguai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paraguai", "🇵🇾 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passardo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passardo", "過"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pecuaria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pecuaria", "畜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pendente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pendente", "懸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pepacote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pepacote", "跑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("percioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("percioso", "尊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("percurso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("percurso", "途"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perguica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perguica", "怠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pergunta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pergunta", "问"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perpetuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perpetuo", "永世"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pesquisa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pesquisa", "研"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoaeu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoaeu", "俄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pisadela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pisadela", "踐踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plastico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plastico", "塑料"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("polegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("polegada", "寸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("politica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("politica", "策"), "策", "政")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ponderam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponderam", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ponderar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponderar", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ponteiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponteiro", "指針"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("portugal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugal", "葡國 "), "葡國 ", " 🇵🇹 ", " 葡萄牙 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("precioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("precioso", "尊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("predicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("predicao", "予言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("presente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presente", "贈"), "贈", "現")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("primeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeira", "第一次"), "第一次", "甲")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("primeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeiro", "甲"), "甲", "第一")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("principe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("principe", "王子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("problema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("problema", "問"), "問", "問題")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("produtos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produtos", "品"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("programa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programa", "程序"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("projetar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("projetar", "映"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("protecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("protecao", "護"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proteger")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proteger", "守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("qualquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qualquer", "哪任"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("párames")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("párames", "肯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rapariga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapariga", "少女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rebeldia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rebeldia", "叛變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("recomend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recomend", "推荐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("redecada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redecada", "絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("refeicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("refeicao", "餐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reformar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reformar", "職場"), "職場", "改革")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relacoes", "關係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("relativo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relativo", "的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("religiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("religiao", "宗教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("rentavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rentavel", "儲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("respeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("respeito", "敬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("restante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("restante", "余"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sabonete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabonete", "石鹸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seguidor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguidor", "徒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seguinte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguinte", "次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sentenca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentenca", "刑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("serpente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serpente", "蛇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("servidor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("servidor", "服務器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("silencio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("silencio", "靜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("situacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("situacao", "状"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobrepor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrepor", "疊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sporting")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sporting", "士砵亭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suastica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suastica", "卍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("submundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("submundo", "幽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("substitu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("substitu", "代"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suspeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suspeito", "疑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("susurrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("susurrar", "囁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tardinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tardinha", "昼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tentacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tentacao", "誘惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("terminal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terminal", "端末"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("terrivel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terrivel", "可怕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trabalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trabalho", "工"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("traducao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traducao", "翻譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("traduzir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traduzir", "翻譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tranquil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tranquil", "寧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transfer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transfer", "传"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transmit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transmit", "播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trascima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trascima", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trespass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trespass", "伐"), "伐", "斬")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("turquesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("turquesa", "碧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tutorial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tutorial", "教程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uebsaite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uebsaite", "網頁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("uindmill")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uindmill", "大風車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ultimato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ultimato", "究極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("umacerta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umacerta", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deinicio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deinicio", "本"))), true, true);}


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates9() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("vermelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vermelho", "赤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("veterano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veterano", "先生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vinganca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vinganca", "复仇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("abdominal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abdominal", "腹筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("adormecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adormecer", "眠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("afundanco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afundanco", "扣籃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aguaceiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguaceiro", "雰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aleatorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleatorio", "随機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("amortecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amortecer", "緩衝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anosatras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anosatras", "昔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aparencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aparencia", "貌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aplicacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aplicacao", "應用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("argentina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("argentina", "🇦🇷 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("argumento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("argumento", "討"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aromatico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aromatico", "芳香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arquiteto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arquiteto", "建築士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("asescutas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asescutas", "房"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assinalar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinalar", "📥 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aterrador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aterrador", "恐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("atirar-se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atirar-se", "撲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("autocarro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("autocarro", "公共汽车"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("azulclaro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulclaro", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("batimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batimento", "臟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bluetooth")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bluetooth", "藍牙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bolasfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolasfora", "邪球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bom-ponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom-ponto", "正論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brilhante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brilhante", "灿烂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("brinquedo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brinquedo", "玩具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("burocrata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burocrata", "僚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cabimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabimento", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caboverde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caboverde", "🇨🇻 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campainha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campainha", "鐘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carbonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carbonato", "碳酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("casamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("casamento", "婚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cavaleiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cavaleiro", "骑士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chocolate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chocolate", "🍫 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("clavicula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clavicula", "鎖骨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("companhia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("companhia", "社"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conclusao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conclusao", "結論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confirmar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confirmar", "確認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confortar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confortar", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conhecido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhecido", "仲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("consegues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consegues", "挺住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contrario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contrario", "倒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("crocodilo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crocodilo", "鱷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("decoracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decoracao", "飾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("delicioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delicioso", "好吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demasiado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demasiado", "太"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("depressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depressao", "鬱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("derepente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derepente", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("regressiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("regressiv", "倒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("descascar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descascar", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desinstal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desinstal", "卸載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desmascar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desmascar", "揭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diferente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diferente", "異"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("disciplin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disciplin", "惩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dispersar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dispersar", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("distancia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distancia", "距離"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("distribui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distribui", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doutorada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutorada", "博士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("doutorado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutorado", "博士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("efeitosom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("efeitosom", "音效"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emdirecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emdirecto", "轉播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emprestar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprestar", "貸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("encarnado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encarnado", "紅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("encontrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encontrar", "找"), "找", "会")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("enfrentar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enfrentar", "面对"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escorpiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escorpiao", "🦂 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escoteiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escoteiro", "偵察"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escritura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escritura", "經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eslovenia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eslovenia", "🇸🇮 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esmeralda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esmeralda", "翠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("espantado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espantado", "惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esperanca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperanca", "希"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esquadrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquadrao", "團"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estrututa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrututa", "構造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("evacuacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evacuacao", "撤退"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("excelente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excelente", "优"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("excepcion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excepcion", "除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("favoravel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favoravel", "利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("federacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("federacao", "聯合會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("filipinas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filipinas", "🇵🇭 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fragancia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fragancia", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frequente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frequente", "頻繁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("genecolog")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("genecolog", "婦科"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ginastica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ginastica", "體操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gravidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gravidade", "重力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("guerreiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerreiro", "戰士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hiobsceno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hiobsceno", "卑猥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imperador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperador", "皇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("implement")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implement", "議"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("impressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("impressao", "印象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("individuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("individuo", "件"), "件", "人")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("indonesia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("indonesia", "🇮🇩 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("interesse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("interesse", "趣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irritante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irritante", "煩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("labirinto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("labirinto", "迷宮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ladoalado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladoalado", "並"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lagueiros")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagueiros", "湖人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("magnetica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magnetica", "磁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("magnetico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magnetico", "磁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maistarde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maistarde", "稍後"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mandibula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandibula", "顎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("marijuana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marijuana", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meditacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meditacao", "瞑想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prototipo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prototipo", "範"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("motivacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("motivacao", "促動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("municipio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("municipio", "県"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nascalmas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nascalmas", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("navegador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("navegador", "瀏覽器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nicaragua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nicaragua", "🇳🇮 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nostalgia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nostalgia", "懐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obediente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obediente", "従順"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("obstaculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obstaculo", "障"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ordinario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordinario", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paquistao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paquistao", "🇵🇰 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pareceque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pareceque", "似乎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("particula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("particula", "微尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partirem2")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partirem2", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partitura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partitura", "谱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pauzinhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pauzinhos", "箸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("populacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("populacao", "人口"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("portugues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugues", "葡語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pressagio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pressagio", "緣起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prestigio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prestigio", "譽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("primavera")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primavera", "春"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("principal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("principal", "主"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proclamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proclamar", "布告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("professor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("professor", "教師"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prudencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prudencia", "謀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("qualidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qualidade", "質"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reputacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reputacao", "譽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("retardado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retardado", "笨蛋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("romantico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("romantico", "浪漫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roqueroll")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roqueroll", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sabedoria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabedoria", "慧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("seguranca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguranca", "安"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sepultura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sepultura", "墓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("singapura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("singapura", "🇸🇬 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sojamolho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sojamolho", "醬油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("solitario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solitario", "寂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aguardepe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguardepe", "泣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sucessivo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sucessivo", "連続"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tartaruga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tartaruga", "🐢 "), "🐢 ", " 龜 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("telemovel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("telemovel", "手機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("televisao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("televisao", "電視"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("temporada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temporada", "期"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tendencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tendencia", "看点"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inspecion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspecion", "查"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("terrestre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terrestre", "陆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("testemunh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("testemunh", "訴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tomaconta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomaconta", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transform")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transform", "化"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("umaooutro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umaooutro", "互"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unificado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unificado", "統一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("unscertos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unscertos", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("utensilio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("utensilio", "機器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("venezuela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venezuela", "🇻🇪 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("visitante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visitante", "亭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sorrateir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sorrateir", "偷"))), true, true);}


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates10() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("abdominais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abdominais", "腹筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("agricultor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agricultor", "農夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aguarlorde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguarlorde", "注"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aplicacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aplicacoes", "應用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arrepender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arrepender", "後悔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("artificial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artificial", "人造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arvoreraiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvoreraiz", "根"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assinatura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinatura", "签✍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("automatico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("automatico", "自动"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("azulescuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulescuro", "蒼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("balconista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balconista", "係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("calendario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calendario", "历"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("campeonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeonato", "冠軍的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cantoneses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantoneses", "广东人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("catastrofe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catastrofe", "災"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("cientifica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cientifica", "科學的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comentario", "評論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("computador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("computador", "电脑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comunidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comunidade", "社區"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("continente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continente", "州"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("delineador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delineador", "眼線"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dependente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dependente", "依"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("descasquei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasquei", "剥了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dividirem2")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dividirem2", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entardecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entardecer", "夕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("esfregarse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esfregarse", "蹭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("eslovaquia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eslovaquia", "🇸🇰 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especifica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especifica", "特定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especifico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especifico", "特定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ezatamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ezatamente", "正確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("existencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("existencia", "存在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extreminio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extreminio", "虐殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fazercresc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fazercresc", "養"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("felicidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("felicidade", "幸福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ferramenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferramenta", "具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fimdalinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fimdalinha", "絶体絶命"), "絶体絶命", "窮途末路")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("gorduradir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gorduradir", "脂肪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("habilidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habilidade", "能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hidrogenio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hidrogenio", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("identidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("identidade", "素性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("igualmente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igualmente", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imaginacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imaginacao", "想像"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("informacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("informacao", "告闫"), "告闫", "報")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inspiracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspiracao", "影響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("instamorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instamorte", "秒殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("intrometer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intrometer", "妨礙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irmaozinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmaozinho", "弟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("irma~zinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irma~zinha", "妹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("iuserneime")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iuserneime", "賬號"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("julgamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("julgamento", "審"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("lamentavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lamentavel", "可哀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("luxemburgo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luxemburgo", "🇱🇺 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("paimachado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paimachado", "斧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maiorparte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maiorparte", "大半"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mandarfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandarfora", "舍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("manuscrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manuscrito", "巻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("masoquismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("masoquismo", "自虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mauritania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mauritania", "🇲🇷 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("melancolia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melancolia", "憂鬱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mensageiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mensageiro", "送言者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mocambique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mocambique", "🇲🇿 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mulherengo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mulherengo", "淫棍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("multi-usos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("multi-usos", "多用途"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("passatempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passatempo", "趣味"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pervertido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pervertido", "好色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pinocabelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pinocabelo", "釵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plataforma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plataforma", "台"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("prefeitura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prefeitura", "県"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("presidente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presidente", "總"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("propaganda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("propaganda", "宣傳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("provisorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("provisorio", "儚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("quantidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quantidade", "量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("queprovoca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("queprovoca", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("recomendar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recomendar", "推荐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("reconhecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reconhecer", "認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("referencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("referencia", "典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roubarloja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubarloja", "万引"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sentimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentimento", "感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comersobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comersobra", "餕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobrepesca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrepesca", "乱獲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("socialista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("socialista", "社会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("substituir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("substituir", "替換"), "替換", "変換")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("suficiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suficiente", "够"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tempestade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tempestade", "嵐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("temporario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temporario", "暫时"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tigrevento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tigrevento", "虎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("timorleste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("timorleste", "🇹🇱 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("protetorex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("protetorex", "完"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tomarconta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomarconta", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("preinforma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preinforma", "預告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transferir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transferir", "下載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trapaceiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trapaceiro", "弊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("umascertas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umascertas", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("velocidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velocidade", "速度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vulneravel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vulneravel", "脆弱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("ziguezague")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ziguezague", "蛇行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("consecutiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consecutiv", "连"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anderteiker")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anderteiker", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aocontrario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aocontrario", "逆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arquitetura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arquitetura", "建築"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("assistencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assistencia", "收視率"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bandasonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bandasonora", "原聲音樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("basquetebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("basquetebol", "籃球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bicarbonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bicarbonato", "氫碳酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("carengueijo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carengueijo", "🦀 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chaodepeolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaodepeolh", "境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("compensacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compensacao", "賠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("concentrado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concentrado", "濃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("confirmacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confirmacao", "確認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("continental")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continental", "大陸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("curiosidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curiosidade", "好奇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dentehumano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentehumano", "齿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desenvolver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolver", "开發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desperdicar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desperdicar", "粗末"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("devagarinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devagarinho", "慢慢地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("diariamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diariamente", "今日的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emprincipio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprincipio", "從"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("emprogresso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprogresso", "途中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("escritoamao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escritoamao", "手寫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estemomento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estemomento", "此時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estrangeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrangeiro", "外國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("experiencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experiencia", "實驗"), "實驗", "試驗")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("flurescente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flurescente", "蛍光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("hancaracter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hancaracter", "漢字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("implementar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implementar", "議"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("imprudencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imprudencia", "無謀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inflacionar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inflacionar", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("localizacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("localizacao", "位置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maisoumenos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maisoumenos", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("manhafresca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manhafresca", "🇰🇵 "), "🇰🇵 ", " 朝鮮 ")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maonopacote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maonopacote", "抱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maoprojetil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maoprojetil", "投"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maquilhagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquilhagem", "妆"), "妆", "粧")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("medirforcas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medirforcas", "摔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("moinhovento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moinhovento", "大風車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("muitasvezes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitasvezes", "屡々"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aguarovelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguarovelha", "洋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("omnipotente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omnipotente", "全能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("onomatopeia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onomatopeia", "擬音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("oqueprovoca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oqueprovoca", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("originalsom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("originalsom", "原聲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bocamachado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bocamachado", "听"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoacurva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoacurva", "仏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("portugueses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugueses", "葡人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poucoapouco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poucoapouco", "段段"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("programacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programacao", "闫機工"), "闫機工", "程序設計")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("programador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programador", "程序者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("publicidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("publicidade", "広告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("redefinicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redefinicao", "重置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("repositorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("repositorio", "府"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resistencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resistencia", "抵抗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("semqualquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semqualquer", "毫无"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("significado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("significado", "意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobrancelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrancelha", "眉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("systemprint")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("systemprint", "💻🎫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("temproblema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temproblema", "搞錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("teracerteza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teracerteza", "見定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trabalhador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trabalhador", "工人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("tradicional")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tradicional", "繁體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transformar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transformar", "改造"))), true, true);}



            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates11() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("aindaporcima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aindaporcima", "且"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aleatoridade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleatoridade", "随機性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("anderteiquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anderteiquer", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aquantotempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aquantotempo", "久"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caminhofacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminhofacao", "辺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baixotirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixotirador", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("canamespoder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canamespoder", "筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baterpunheta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baterpunheta", "手淫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bloqueadorde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloqueadorde", "广"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("conhecimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhecimento", "智"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("constituicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constituicao", "憲法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("contraataque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contraataque", "反擊"), "反擊", "回擊")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("decumentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decumentario", "紀錄片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deitaraochao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitaraochao", "堕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("demonstracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demonstracao", "祭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("denteamarelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("denteamarelo", "歯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deslumbrante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deslumbrante", "眩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desprevenido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desprevenido", "油断"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("determinismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("determinismo", "決定論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("distribuicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distribuicao", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("documentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("documentario", "紀錄片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("durabilidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("durabilidade", "耐久"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especialista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especialista", "名人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("estatisticas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estatisticas", "統計"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("experimental")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experimental", "實驗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fluorescente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluorescente", "蛍光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inteligencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inteligencia", "慧"), "慧", "情報")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("interessante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("interessante", "有趣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mamasgrandes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamasgrandes", "波霸"), "波霸", "巨乳")), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mandarabaixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandarabaixo", "辛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("meioambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meioambiente", "容"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("necessidades")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("necessidades", "便"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("palavrapasse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavrapasse", "密碼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("partiremdois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partiremdois", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("perservativo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perservativo", "套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoaescura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoaescura", "黒坊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("plaistaichon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plaistaichon", "遊戲站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("planejamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planejamento", "企画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pleisteichon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pleisteichon", "遊戲站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("podermilitar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("podermilitar", "武力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("poroutrolado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poroutrolado", "而"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("properiadade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("properiadade", "屬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("proximovisor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proximovisor", "資"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("redefacaocor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redefacaocor", "絕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("roupadebaixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roupadebaixo", "裾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("simplificado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("simplificado", "簡體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobrevivente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrevivente", "幸存活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("transparente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transparente", "透明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("trilhasonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trilhasonora", "配乐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("vistadeolhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vistadeolhos", "一覧"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates12() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contentEquals("departamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("departamento", "部門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arabiasaudita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arabiasaudita", "🇸🇦 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("armazenamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("armazenamento", "存儲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("deinicioaofim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deinicioaofim", "迄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bambumespoder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bambumespoder", "筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bompresidente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bompresidente", "王道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desenvolvedor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolvedor", "开發者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dividiremdois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dividiremdois", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entardcigarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entardcigarra", "暮蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("especialmente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especialmente", "尤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("extraodinario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extraodinario", "非常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chuvaconjunto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chuvaconjunto", "霍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("internacional")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("internacional", "國际"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("malaoambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("malaoambiente", "乱開発"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mausentimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mausentimento", "恶心"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("muitoobrigado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitoobrigado", "多謝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nacionalidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nacionalidade", "國籍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nomededominio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nomededominio", "域名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("nomedeusuario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nomedeusuario", "賬號"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("olharparacima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olharparacima", "仰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("piscardeolhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("piscardeolhos", "瞬間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("processamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("processamento", "处理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("racionalidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("racionalidade", "理性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("sobrevivencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrevivencia", "存活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("comparabranco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comparabranco", "皆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("arvorepolegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvorepolegada", "村"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("azulesverdeado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulesverdeado", "碧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("circunstancias")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circunstancias", "状況"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("debaixotirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("debaixotirador", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desflorestacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desflorestacao", "乱伐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("entretenimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entretenimento", "娛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("fisiculturismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fisiculturismo", "健美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("frequentemente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frequentemente", "頻繁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("resplandecente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resplandecente", "光明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoaesquerda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoaesquerda", "佐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("4linhasprojetil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("4linhasprojetil", "设"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("circuitovirtual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circuitovirtual", "回線"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desenvolvimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolvimento", "發展"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("direitoshumanos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direitoshumanos", "人権"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("forcanascanelas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forcanascanelas", "加油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("futuropromissor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futuropromissor", "前途洋洋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("localdetrabalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("localdetrabalho", "職場"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maonoaindadente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maonoaindadente", "撐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("calardizerninja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calardizerninja", "默認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pessoapesopoder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoapesopoder", "働"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("chaoprotetorfava")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaoprotetorfava", "壹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("consequentemente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consequentemente", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("inexpectadamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inexpectadamente", "不意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("processojudicial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("processojudicial", "訴訟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("dequalquermaneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dequalquermaneira", "無論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("bloqueadorpuxaagua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloqueadorpuxaagua", "康"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("baldecentro10poder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baldecentro10poder", "勒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caminhoumbocacampo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminhoumbocacampo", "逼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("aprovadopelogoverno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprovadopelogoverno", "官方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("desperdicardinheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desperdicardinheiro", "無駄遣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("maonaarvorerecebida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maonaarvorerecebida", "採"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("pedrapapeloutesoura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedrapapeloutesoura", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("propriocampomesinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("propriocampomesinha", "鼻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("redetampacurvapegas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redetampacurvapegas", "統"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("republicadominicana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("republicadominicana", "🇩🇴 "))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("mamasgrandesmasburra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamasgrandesmasburra", "胸大無腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("namelhordashipoteses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("namelhordashipoteses", "精々"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("caminhoolhoumabocaroupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminhoolhoumabocaroupa", "還"))), true, true);}
                else if (mComposing.toString().toLowerCase().contentEquals("coisaregiaoumarrozgrande")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coisaregiaoumarrozgrande", "奧"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    /*private void updateCandidates13() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {


            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates14() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates15() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates16() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates17() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {

                } else {
                    setSuggestions(null, false, false);
                }
            }
        }
    }

    private void updateCandidates18() {
        if (!mCompletionOn) {

        }
    }

    private void updateCandidates19() {
        if (!mCompletionOn) {

            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates20() {
        if (!mCompletionOn) {

            else {
                setSuggestions(null, false, false);
            }
        }
    }*/


    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
            try {Log.i(TAG, "setSuggestions(suggestions" + suggestions.toString() + ", completions " + completions  + ", typedWordValid " + typedWordValid + "): se mcandidateview != nula");}
            catch (Exception NullPointerException) {
                Log.i(TAG, "sugestões: nada, + completions " + completions + " typedWordValid " + typedWordValid +  "  Não há sugestões");
            }
        }
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates1();
            updateCandidates2();
            updateCandidates3();
            updateCandidates4();
            updateCandidates5();
            updateCandidates6();
            updateCandidates7();
            updateCandidates8();
            updateCandidates9();
            updateCandidates10();
            updateCandidates11();
            updateCandidates12();
            //updateCandidates13();
            //updateCandidates14();
            //updateCandidates15();
            //updateCandidates16();
            //updateCandidates17();
            //updateCandidates18();
            //updateCandidates19();
            //updateCandidates20();
            Log.i(TAG, "handleBackspace: se length > 1");
        } else if (length > 0) {
            mComposing.setLength(length - 1); ///(0)
            getCurrentInputConnection().commitText("", 0);
            updateCandidates1();
            updateCandidates2();
            updateCandidates3();
            updateCandidates4();
            updateCandidates5();
            updateCandidates6();
            updateCandidates7();
            updateCandidates8();
            updateCandidates9();
            updateCandidates10();
            updateCandidates11();
            updateCandidates12();
            //updateCandidates13();
            //updateCandidates14();
            //updateCandidates15();
            //updateCandidates16();
            //updateCandidates17();
            //updateCandidates18();
            //updateCandidates19();
            //updateCandidates20();
            Log.i(TAG, "handleBackspace: se length > 0, mcomposing.length:" + mComposing.length());
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }
    private void handleShift() {
        Log.i(TAG, "handleShift: ");
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            setLatinKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }
///abre os candidatos quando põe o caracter
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates1();
            updateCandidates2();
            updateCandidates3();
            updateCandidates4();
            updateCandidates5();
            updateCandidates6();
            updateCandidates7();
            updateCandidates8();
            updateCandidates9();
            updateCandidates10();
            updateCandidates11();
            updateCandidates12();
            //updateCandidates13();
            //updateCandidates14();
            //updateCandidates15();
            //updateCandidates16();
            //updateCandidates17();
            //updateCandidates18();
            //updateCandidates19();
            //updateCandidates20(); ///este é o primeiro
            Log.i(TAG, "handleCharacter: primarycode:" + primaryCode + "mpredictioOn:" + mPredictionOn);
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
    }
    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }
    private IBinder getToken() {
        Log.i(TAG, "getToken: ");
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }
    private void handleLanguageSwitch() {
        mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }
    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        Log.i(TAG, "getWordSeparators: ");
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        Log.i(TAG, "isWordSeparator: ");
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }
    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {

            ///commitTyped(getCurrentInputConnection()); <- cometeria o texto normal
            if (mCandidateView != null) { // se os candidatos não está nulo
                getCurrentInputConnection().commitText( // cometer texto que é
                        mCandidateView.getSuggestion(index), //vai buscar o index da lista
                        mCandidateView.getSuggestion(index).length()); //int
                mComposing.setLength(0); //termina de compor
                updateCandidates1();
                updateCandidates2();
                updateCandidates3();
                updateCandidates4();
                updateCandidates5();
                updateCandidates6();
                updateCandidates7();
                updateCandidates8();
                updateCandidates9();
                updateCandidates10();
                updateCandidates11();
                updateCandidates12();
                //updateCandidates13();
                //updateCandidates14();
                //updateCandidates15();
                //updateCandidates16();
                //updateCandidates17();
                //updateCandidates18();
                //updateCandidates19();
                //updateCandidates20(); //mcomposing em else
                Log.i(TAG, "pickSuggestionManually: candidatos posto em nulo");
            }
        }
    }

    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        handleBackspace();
    }
    public void swipeDown() {
        handleClose();
    }
    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }
}