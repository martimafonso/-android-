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
        updateCandidates13();
        updateCandidates14();
        updateCandidates15();
        updateCandidates16();
        updateCandidates17();
        updateCandidates18();
        updateCandidates19();
        updateCandidates20();

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
        updateCandidates13();
        updateCandidates14();
        updateCandidates15();
        updateCandidates16();
        updateCandidates17();
        updateCandidates18();
        updateCandidates19();
        updateCandidates20();

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
            updateCandidates13();
            updateCandidates14();
            updateCandidates15();
            updateCandidates16();
            updateCandidates17();
            updateCandidates18();
            updateCandidates19();
            updateCandidates20();
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
            updateCandidates13();
            updateCandidates14();
            updateCandidates15();
            updateCandidates16();
            updateCandidates17();
            updateCandidates18();
            updateCandidates19();
            updateCandidates20();
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
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
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
    public void onKey(int primaryCode, int[] keyCodes) {
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
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                setLatinKeyboard(mQwertyKeyboard);
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
                mSymbolsKeyboard.setShifted(false);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }

    public void onText(CharSequence text) {
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
                if (mComposing.toString().toLowerCase().contains("b")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("b", "布"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("c")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("c", "克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("d")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("d", "德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("e")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("e", "与	短縮よみ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("e")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("e", "與"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("f")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("f", "夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("g")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("g", "葛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("h")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("h", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("i")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("i", "意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("i")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("i", "工"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("i")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("i", "伊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("I")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("I", "工"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("I")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("I", "意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("l")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("l", "尔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("l")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("l", "讀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("o")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("o", "个"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("o")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("o", "哦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("p")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("p", "乗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("r")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("r", "兒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("s")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("s", "士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("s")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("s", "們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("s")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("s", "斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("S")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("S", "們	接尾一般"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("t")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("t", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("t")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("t", "達"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("t")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("t", "特"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("u")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("u", "得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("v")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("v", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("v")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("v", "檢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("x")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("x", "❌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("a'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("a'", "阿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("a'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("a'", "乍"))), true, true);}

            }
        } else {
            setSuggestions(null, false, false);
            Log.i(TAG, "updateCandidates: se mcomposing.length = 0");
        }
    }


    private void updateCandidates2() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("ad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ad", "增"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ah")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ah", "呵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ah")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ah", "哈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ai", "那里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ai", "艾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("al")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("al", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Al")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Al", "齋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("an")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("an", "案"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ap")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ap", "應"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ap")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ap", "應用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ar", "空"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ba", "巴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ba", "吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("be")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("be", "寶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bi", "美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bi", "碧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bi", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bi", "比"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bs", "原聲音樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bu", "布"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ca", "貨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ca", "卡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Ca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Ca", "可"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ce", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cu", "尻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cu", "克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cu", "族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("da")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("da", "屬於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("da")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("da", "達"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("da")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("da", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("de")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("de", "关於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("de")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("de", "屬於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("de")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("de", "德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("di")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("di", "低"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("di")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("di", "迪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("do")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("do", "献"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("do")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("do", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("e'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("e'", "是	短縮よみ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ei", "得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("el")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("el", "他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("em")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("em", "於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("en")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("en", "恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("es", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("es", "語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("es", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("es", "東"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Es")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Es", "語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eu", "我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Eu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Eu", "我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ex", "元"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fa", "迷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fa", "做"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fa", "制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fi", "翡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fi", "菲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fi", "得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fo", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fu", "佛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("go")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("go", "悟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("go")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("go", "御"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ha", "啊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ha", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hh", "っ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hi", "希"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hu", "胡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ia", "亞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ia", "哉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ia", "學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("in")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("in", "殘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("in")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("in", "無"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ir", "行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ir", "往"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("is")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("is", "國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iu", "祐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iz", "生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ja", "既"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ja", "已經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("je")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("je", "傑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Je")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Je", "耶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ji")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ji", "吉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ji")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ji", "姬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jo", "舟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ju")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ju", "祖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ju")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ju", "舉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ju")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ju", "據"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("la")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("la", "拉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("le")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("le", "黎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("li")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("li", "利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("li")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("li", "裡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lo", "洛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lu", "魯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lu", "錄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "邁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "麼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "嗎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "馬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ma", "軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Ma", "魔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Ma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Ma", "嗎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("me")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("me", "我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("me")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("me", "梅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mo", "默"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("na")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("na", "華"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ne")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ne", "恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "泥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "尼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "妮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ni", "級"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "結"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "之"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no", "的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("No")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("No", "的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ns", "不知"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nu", "努"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("o'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("o'", "御"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oi", "嘿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ok")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ok", "是	副詞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("or")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("or", "者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("or")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("or", "監"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ou", "或"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ou", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ou", "奧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pe", "足"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pe", "求"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pi", "僻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ra", "拉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rd", "德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("re")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("re", "再"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ri", "里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ri", "解"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ri", "利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ro", "羅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rs", "😂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rs", "笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ru")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ru", "魯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa", "出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa", "薩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa", "撒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("se", "如果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("se", "斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("se", "燥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Se", "燥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("so")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("so", "蘇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("so")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("so", "響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sr", "氏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ss", "斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("su")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("su", "泣	形動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("su")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("su", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("su")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("su", "俗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ta", "田"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ta", "有á"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ta", "塔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ta", "宅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("te")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("te", "你"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ti", "蒂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("TI")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("TI", "體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("TI")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("TI", "緹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("to")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("to", "奏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("to")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("to", "特"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("to")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("to", "触"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tr", "帶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ts")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ts", "配樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tu", "妳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tu", "你"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tz", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ue", "衛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ui", "威"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("um")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("um", "1"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("um")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("um", "一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("um")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("um", "壹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("un")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("un", "聯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Un")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Un", "運"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uo", "越"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("us")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("us", "斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("us")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("us", "使"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("va")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("va", "去行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("va")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("va", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("va")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("va", "去吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vi", "維"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vs", "對"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xi", "戲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xi", "希"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ze")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ze", "翠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("zi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("zi", "季"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("zo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("zo", "索"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates3() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("abr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abr", "开"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ach")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ach", "思"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aco", "鋼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("act")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("act", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adi", "整合開發環境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ali")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ali", "那邊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ali")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ali", "那里"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ali")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ali", "盟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("and")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("and", "🚶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("and")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("and", "走"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ano", "琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ano", "年"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("art")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("art", "藝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("asa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asa", "习"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ass", "烤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ass", "組"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ata", "攻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ate", "迄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atu", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ave")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ave", "雀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("avo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avo", "祖母"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("avo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avo", "祖父"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bat", "打"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("be'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("be'", "貝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bem", "很"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bem", "善"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boa", "良"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boa", "優"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom", "👍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom", "好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bou", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cab", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cab", "合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cac", "狩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cai", "坠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cal", "靜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("can")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("can", "廣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("can")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("can", "坎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cao", "逆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cao", "犬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cao", "曹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("car")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("car", "字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cas", "婚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cau", "門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cav", "掘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ceg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceg", "盲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cem", "百"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ceu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceu", "天"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cha", "茶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chi", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chi", "戲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chi", "師"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chu", "修"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coc", "搔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cod", "碼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("com")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("com", "和"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("com")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("com", "跟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("com")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("com", "務員"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("com")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("com", "食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("con")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("con", "进"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("con")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("con", "含"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cor", "色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cou", "科"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cou", "行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coz", "焼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cri", "造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cri", "創"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cur", "癒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cur", "治"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dai", "呆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dar", "与"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dar", "給"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("des")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("des", "卸	接頭語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("des")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("des", "卸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("des")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("des", "劣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dia", "日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dif")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dif", "變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dir", "方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dir", "電台"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("div")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("div", "樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dns", "域名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dor", "痛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dou", "動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dqm")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dqm", "無論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edo", "具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edu", "育"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ela", "她"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ele", "男也"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("els")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("els", "他們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emp", "推"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enc", "終曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("era")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("era", "紀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("era")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("era", "存了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ero", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("err")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("err", "誤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("est")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("est", "會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("est")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("est", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eua", "合衆國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fal", "話"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fan", "芳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fei", "廢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fim", "終了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fin", "芬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fio", "紗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fix", "直"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fiz", "作了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fod", "肏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foi", "行了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foi", "去了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fon", "峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("for")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("for", "选上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fra", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fug", "逃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fui", "去了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fum")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fum", "抽煙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gai", "同性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gan", "根"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gan", "梗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gas", "气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("geo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geo", "地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("glu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("glu", "吨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gou", "毫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gou", "夠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("han")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("han", "韓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("han")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("han", "漢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hou", "方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iei", "耶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iin", "應"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilh", "享"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ips")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ips", "幀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iue", "粵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iui", "唯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jia", "駕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jia", "假"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jin", "津"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jor", "喬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jou", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jua", "抓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jur", "誓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lai", "萊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lan", "蘭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lav", "洗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lei", "律"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lei", "雷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ler")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ler", "讀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lev", "拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lha", "他女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lha", "拉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lhe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lhe", "他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lig", "接"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lin", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("loc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loc", "所"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("log")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("log", "質"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lon", "隆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lua", "🌛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lun")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lun", "倫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("luz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luz", "光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mac", "軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mac", "猿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mae")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mae", "母親"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mal", "惡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("man")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("man", "万"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mao", "手"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mao", "✋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mar", "海"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mas", "但"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mat", "殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mau", "弊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mdf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mdf", "背景音樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("med")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("med", "測"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mei", "沒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mei", "梅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mel", "🍯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mel", "蜜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mes", "月"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meu", "我的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mil", "千"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Mil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Mil", "千"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mio", "澪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mir", "狙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mom", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mov", "動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mud", "變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nao", "不"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Nao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Nao", "不"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("neg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neg", "尚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nem", "否"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nen")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nen", "年"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nev", "☃️"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nin", "忍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nin", "宁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("niu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("niu", "妞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("niu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("niu", "紐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("no'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("no'", "結"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nor", "北"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nos", "己"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nos", "我們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oca", "岡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oes", "西"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ola", "迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olh", "見"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("opc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opc", "自選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovo", "🥚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovo", "卵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovo", "蛋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pag", "頁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pag", "払"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pai", "爹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pai", "父"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pam", "噴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pan", "班"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pan", "噴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pan", "盤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pan", "潘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pao", "🍞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pao", "麵包"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("par")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("par", "止"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pau", "奉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pau", "棒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("paz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paz", "☮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ped")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ped", "求"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peg", "拾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peg", "付"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pel", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("per")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("per", "要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pir", "去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pis", "踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("po'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("po'", "尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pod", "可"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("por")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("por", "砵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("por")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("por", "為"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("por")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("por", "葡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ppt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ppt", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pra", "銀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pre", "前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pro", "專"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pub", "広告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pub", "公"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pun")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pun", "懲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pux")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pux", "引"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "甚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "怪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "科"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "卷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("que")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("que", "啥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("qui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qui", "啟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ra~")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ra~", "蛙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rai", "萊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ran")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ran", "燃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ref")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ref", "典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rei", "行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rei", "王"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rei", "雷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("REI")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("REI", "王"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rep")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rep", "代"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rez", "🙏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rin", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rir", "笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ro7")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ro7", "隆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rou", "偷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rra", "琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rug", "咆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rui", "瑞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sa~")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sa~", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sai", "去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sai", "賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sai", "去出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sao", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sao", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sao", "騷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seg", "秒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sei", "知道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sem", "無"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sem", "毫无"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ser")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ser", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seu", "您的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sim", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sin", "交響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("so'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("so'", "就"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("soa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soa", "響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sol", "☀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sol", "太陽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("som")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("som", "聲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sou", "送"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sou", "我是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sou", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ssu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ssu", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sua", "您的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sub", "亞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sub", "昇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sug", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suj")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suj", "汚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sul", "南"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tag", "簽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tao", "甚至"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tem", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tem", "天"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ter", "有"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teu", "你的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tex")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tex", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tia", "叔母"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tic")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tic", "抖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tim", "丁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tio", "叔父"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tom", "音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tom", "採"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("top")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("top", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tro", "替"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tss", "茲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tua", "你的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uai", "威"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uai", "懷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uei", "韋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uma", "壹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uma", "一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uns", "些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uso", "使用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vai", "去行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("van")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("van", "凡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vao", "行你們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vas", "去您"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vem", "雲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vem", "来您"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ver", "視"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ver", "看"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ver", "佛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vez", "時間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vez", "倍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("via")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("via", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vir", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viv", "住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vos", "您們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vou", "行我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("voz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voz", "声"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xou", "翔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("zen")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("zen", "怎"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates4() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("aban")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aban", "煽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("abus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abus", "虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acab", "終"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afog")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afog", "溺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agit", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agri", "農"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agua", "水"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ajud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ajud", "助"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alma", "魂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alto", "高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alug")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alug", "租"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amar", "愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amor", "愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anda", "走"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ando")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ando", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anjo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anjo", "天使"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anos", "歳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anos", "年"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anot")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anot", "筆記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anti", "反"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anus", "肛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apoi", "援"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aqui")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aqui", "此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arco", "弓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arte", "術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arte", "藝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("asas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asas", "羽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("auge")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("auge", "巔峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aujo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aujo", "巔峰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("azul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azul", "藍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baba", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bala", "彈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bang")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bang", "榜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("base")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("base", "基"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bege")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bege", "米"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("beje")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beje", "米色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bela", "美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bela", "美麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bens", "資"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bili")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bili", "哔哩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bloq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloq", "厂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boca", "👄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boca", "嘴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boca", "口"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bola", "球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bora", "上去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brin", "玩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("buda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("buda", "佛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caca", "猟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cacu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cacu", "獲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cada", "各"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cafe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cafe", "☕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cama", "床"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cana", "杖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cans")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cans", "困"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cant", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caos", "沌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caps")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caps", "包"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caro", "貴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("casa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("casa", "🏠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("casa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("casa", "家"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caso", "案"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cedo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cedo", "早"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ceia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ceia", "夜食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cele", "賽魯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cena", "事"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cena", "先拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cena", "景"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cham")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cham", "稱呼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chao", "土"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cheg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheg", "到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chiu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiu", "默"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("choc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("choc", "震驚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chor", "哭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cima", "↑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cima", "上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cina", "先拿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cliq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cliq", "点擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cobr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobr", "覆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cobr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobr", "蓋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("como")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("como", "怎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comp", "買"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comp", "集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cona")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cona", "屄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conf", "亂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cont")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cont", "含"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cool")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cool", "良"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("copa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("copa", "盃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("copa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("copa", "🏆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corr", "🏃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corr", "跑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cort")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cort", "切"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coub", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("crav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crav", "刺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cuba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cuba", "🇨🇺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cubo", "立方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("culp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culp", "責備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cute")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cute", "可愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dama", "姬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dano", "害﻿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("data")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("data", "数据"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("data")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("data", "日期"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deci", "決"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deit", "寝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deit", "伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deix", "留"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deix", "残"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dela", "她的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dele", "他的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("depe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depe", "立"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deus", "神"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Deus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Deus", "真主"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dici")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dici", "辞典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("disp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disp", "願"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("disu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disu", "低俗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobr", "彎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doce", "甜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doer", "痛感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dois", "2"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dorm")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dorm", "寝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dose")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dose", "番"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("duro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("duro", "固"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ecra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ecra", "屏幕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ecra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ecra", "屏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edif")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edif", "建"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edit", "編"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eixo", "軸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("elas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elas", "她們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eles", "男也們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("elev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elev", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("embr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embr", "裝r"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("endo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("endo", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enfi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enfi", "穿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entr", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erri")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erri", "誤解"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erro", "誤植"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erva", "大麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erva", "草"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erva", "艹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eses", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espi", "覗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("essa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("essa", "那"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esse", "那"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esta", "這"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("este")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("este", "這部"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("este")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("este", "東"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("euro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("euro", "€"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("evol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evol", "演"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("expo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expo", "展"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("faca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("faca", "🔪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("face")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("face", "面"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fala", "話吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("falt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falt", "缺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fase")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fase", "相"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fava")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fava", "豆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fech")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fech", "閉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feia", "醜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feio", "醜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fito", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flor", "花"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flor", "🌼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flor", "華"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foda", "滲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fofa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofa", "可愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fofo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofo", "可愛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fofo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofo", "蓬松"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fofo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofo", "暄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fofo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fofo", "柔軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fogo", "🔥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fogo", "火"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fome")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fome", "飢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fone")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fone", "筒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fong")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fong", "鋒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fora", "外"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foto", "攝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foto", "照片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("freq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("freq", "頻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frio", "寒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fucu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fucu", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fumo", "煙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gacu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gacu", "學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gaja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaja", "奴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gajo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gajo", "禺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ganh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganh", "稼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gast")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gast", "費"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gato", "貓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gelo", "冫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gelo", "氷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("geni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geni", "天才"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("giga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("giga", "巨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gost")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gost", "好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("graf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("graf", "理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gram")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gram", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gran")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gran", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grao", "粒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grau", "度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("greg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("greg", "葛瑞格"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grit", "叫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guar", "衛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hino", "國歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hoje")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hoje", "今日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("homo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("homo", "同士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hora", "時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iang")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iang", "樣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iang")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iang", "央"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iano", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iaoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iaoi", "㚻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilha", "島"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ilud")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilud", "妄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imov", "移動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inbi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inbi", "硬幣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inch")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inch", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("info")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("info", "報"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("invo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("invo", "召"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irse", "去"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("isca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isca", "餌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ismo", "主義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("isol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isol", "孤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("isso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isso", "那"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("isto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isto", "這個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("item")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("item", "件"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iues", "粵語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jato", "噴出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jaze")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jaze", "爵士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jema", "這麼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jeva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jeva", "爪哇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("joao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joao", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogo", "賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jogo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogo", "遊戲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("john")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("john", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jone")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jone", "莊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("judo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("judo", "杉本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("junt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("junt", "合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("juve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juve", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lado", "方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lado", "旁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ladr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladr", "吠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lago")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lago", "湖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lanc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lanc", "癶投"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lanc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lanc", "擲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("larg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("larg", "堕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("larg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("larg", "离"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lata", "缶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leal", "忠誠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leao", "獅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leoa", "獅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leve", "輕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("liga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("liga", "聯賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limp", "拭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lind")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lind", "美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ling")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ling", "靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("link")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("link", "🖇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lixo", "圾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lobo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lobo", "狼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("logo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("logo", "标志"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("logo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("logo", "趕快"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("loja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loja", "店"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("loli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("loli", "蘿莉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("louv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louv", "讃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lupa", "🔎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("luva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luva", "手套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("luxo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luxo", "贅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maca", "🍎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maca", "苹果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mais", "添"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mama", "媽媽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mama", "母"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mand")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mand", "令"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mapa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mapa", "図"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medi", "医"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medo", "惧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meig", "優"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meio", "半"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mesa", "卓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mian")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mian", "緬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("miau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miau", "喵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mini")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mini", "小"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("moda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moda", "時裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mori")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mori", "森"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morr", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mult")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mult", "乗法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muro", "牆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nada", "不物"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nasc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nasc", "産"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("neto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neto", "孫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("neve")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("neve", "雪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nojo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nojo", "厭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nome")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nome", "名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nota", "記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nova", "新"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nova", "若"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("novo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("novo", "若"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("novo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("novo", "新"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ocup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ocup", "忙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("odio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("odio", "怨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oleo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oleo", "油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "ㅎ"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "👁️"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "眼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "目"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olho", "見我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("onda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onda", "波"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("onda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onda", "🌊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("onde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onde", "何处"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oper", "操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oque", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oque", "啥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("osso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("osso", "骨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("otxi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("otxi", "陥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ouro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouro", "金"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("outr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outr", "也"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pais", "國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pais", "亲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pais", "鄉村"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("para")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("para", "以"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parc", "似"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("part")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("part", "折"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pass", "通"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pass", "過"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peco", "我求"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pegu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pegu", "付"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pela", "为"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pele", "皮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pelo", "毛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pelo", "为"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pena", "ン"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pens", "🤔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pens", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pera")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pera", "梨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perd", "掉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perd", "負"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peru")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peru", "🇵🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peso", "重"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pesq")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pesq", "搜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pess")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pess", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pila")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pila", "屌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pino", "釵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pint")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pint", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pior", "陥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pior", "最坏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pipa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pipa", "琵琶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plan", "計"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("post")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("post", "稿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poup", "貯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("povo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("povo", "民"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ppot")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ppot", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pres", "逮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prev", "預覽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pura", "純"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("puro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("puro", "純"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("puta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("puta", "婊子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("puta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("puta", "賤人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("qual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qual", "哪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("qual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qual", "何"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quei", "凯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quem", "誰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quiu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiu", "究"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rabo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rabo", "臀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("raca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("raca", "人種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("raiz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("raiz", "根"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rato", "鼠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("real")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("real", "實"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("refl")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("refl", "映"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rest")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rest", "剩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("riso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("riso", "笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rock")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rock", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roda", "輪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rolo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rolo", "卷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("romp")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("romp", "破"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rosa", "粉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rosn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rosn", "哮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roub")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roub", "偷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrs", "😂😂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrs", "笑笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sabe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabe", "知彼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saco", "袋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saga", "篇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sair")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sair", "出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sala")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sala", "室"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("salv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salv", "救"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sapo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapo", "蟾蜍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("scit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("scit", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seco", "荒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segu", "辿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("semi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semi", "準"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sent", "坐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sent", "覺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sexi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sexi", "性感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sexo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sexo", "性交"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sexo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sexo", "性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sino", "鈴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("soar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soar", "響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobe", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("solt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solt", "鬆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("soni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soni", "索尼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sono", "睡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sopr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sopr", "吹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ssel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ssel", "素"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("stor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("stor", "先生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suma", "總"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sumo", "汁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suor", "汗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suxi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suxi", "🍣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("taca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taca", "🏆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("taca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taca", "盃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("taro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taro", "芋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("taxa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("taxa", "率"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tche")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tche", "扯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tchi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchi", "吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teia", "網"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teim", "頑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tema", "題"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teta", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ting")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ting", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ting")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ting", "亭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tipo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tipo", "種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tiro", "射"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todo", "全"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todo", "生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tofu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tofu", "豆腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("toma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toma", "苫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("torc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("torc", "絞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("torn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("torn", "旋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trai", "叛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tras", "背"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trem", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tres", "3"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Tres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Tres", "三"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubo", "管"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tudo", "一切"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tudo", "全"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("urso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("urso", "熊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("usar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("usar", "用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("util")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("util", "用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vaca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vaca", "牛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vale")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vale", "谷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vara", "竿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("veio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veio", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("venc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venc", "勝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("venh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venh", "來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verd")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verd", "真"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verg", "恥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viag", "撇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vice")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vice", "副"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vida", "命"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vida", "人生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viol", "提琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viol", "犯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("voar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voar", "飛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("voce")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voce", "您"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xiao", "蕭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xtre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("xtre", "極"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates5() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("abrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abrev", "略"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("abund")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abund", "溢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("abuso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abuso", "虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acada", "毎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acido", "酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acord")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acord", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("activ")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("activ", "啟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adeus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adeus", "至神"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adopt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adopt", "飼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afiar", "研"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afund")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afund", "沉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agarr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarr", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agora", "今"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aguia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguia", "鵰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ainda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ainda", "還"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alarg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alarg", "拡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aleat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleat", "随機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aleat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleat", "随"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alinh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alinh", "列"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alter", "更"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aluno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aluno", "徒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amais", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ambos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ambos", "雙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amiga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amiga", "友"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amigo", "友"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("andar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("andar", "走"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anima", "動画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("antes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antes", "前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apanh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apanh", "堕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apert", "締"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apont")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apont", "指"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apost")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apost", "賭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("areia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("areia", "石尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arroz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arroz", "米"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("artes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artes", "術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aspir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspir", "抽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atomo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atomo", "原子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atras", "遲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atras", "前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("autor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("autor", "著"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aviso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aviso", "注意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aviso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aviso", "提醒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aviso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aviso", "⚠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("axila")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("axila", "脇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bafer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bafer", "緩衝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baile")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baile", "舞會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixo", "下"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixo", "↓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixo", "悄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixo", "低"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bambu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bambu", "竹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banbu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banbu", "竹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banco", "銀行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banda", "頻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banda", "樂隊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banho", "浴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("barco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barco", "艇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("barco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barco", "🚤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bebed")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bebed", "醉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bejes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bejes", "米人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("birus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("birus", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bolha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolha", "泡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bomba")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bomba", "爆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bonit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonit", "麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bosta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bosta", "糞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("braco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("braco", "腕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brilh", "輝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("broli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("broli", "布羅利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bruta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruta", "凶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bruto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruto", "凶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bruxa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bruxa", "萬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("burro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burro", "驢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("burro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burro", "笨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("butao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("butao", "按鈕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("butao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("butao", "釦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cacar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cacar", "狩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cache")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cache", "緩存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("calmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calmo", "穏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("calor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calor", "暑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campo", "場"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canal", "頻道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canto", "唱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carbo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carbo", "炭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carga", "荷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carma", "業"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carne")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carne", "肉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carro", "🚗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carro", "車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("catar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catar", "🇶🇦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cauda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cauda", "尾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caule")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caule", "莖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("causa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("causa", "因"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("certo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("certo", "✅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cesto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cesto", "籃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chave")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chave", "鍵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cheio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheio", "満"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chiis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiis", "🇨🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chiis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiis", "中國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chile")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chile", "🇨🇱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chilr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chilr", "鳴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("china")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("china", "中華"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("china")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("china", "🇨🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("china")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("china", "🇹🇼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chris")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chris", "克里斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chupa", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chuva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chuva", "☔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chuva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chuva", "雨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cienc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cienc", "科"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cinza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cinza", "灰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("clara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clara", "明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("claro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("claro", "明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("clube")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clube", "體育會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobra", "蛇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cocar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cocar", "搔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coisa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coisa", "物"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coisa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coisa", "事"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coiso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coiso", "個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Coiso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Coiso", "事"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colar", "粘貼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comec", "始"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comer", "食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conta", "賬户"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corda", "弦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corda", "繩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coroa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coroa", "👑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corpo", "身"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corpo", "身体"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corpo", "體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corpo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corpo", "体"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corro", "蝕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("costa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("costa", "背中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("costa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("costa", "岸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("couro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("couro", "革"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cover")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cover", "翻唱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cozer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozer", "熟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("credo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("credo", "信條"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("credo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("credo", "教條"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("criar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criar", "作成"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("criar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criar", "制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("criti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criti", "評"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("criti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("criti", "批"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cruel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cruel", "酷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("culpa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culpa", "責"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("curar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curar", "癒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("curso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curso", "講"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dados")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dados", "信息"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("danca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("danca", "舞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("danmu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("danmu", "彈幕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("decol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decol", "脱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("decor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decor", "飾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deita", "伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("delas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delas", "她們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deles", "他們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dente", "牙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desab")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desab", "墜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desde", "以上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desej")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desej", "欲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("destr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destr", "破"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("detet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("detet", "偵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("detet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("detet", "檢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deusa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deusa", "神"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dever")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dever", "應該"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diabo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diabo", "魔鬼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("difer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("difer", "異"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("digam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("digam", "言達吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dizer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dizer", "言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobra", "配音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doido", "狂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("drama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("drama", "劇集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("droga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("droga", "藥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dupla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dupla", "兩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("email")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("email", "電郵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emdir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emdir", "轉播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("engan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("engan", "騙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enrol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enrol", "卷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entao", "就"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entra", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entre", "介"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escal", "登"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escov", "刷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esper", "待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estar", "在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("etern")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("etern", "永"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("etnia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("etnia", "民族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exced")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exced", "越"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("excit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excit", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exist", "存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exort")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exort", "鼓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("expli")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expli", "說明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extre", "極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("facao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("facao", "刀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("falar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falar", "話"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("falha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falha", "失"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("falso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falso", "偽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fara'")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fara'", "請"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("favor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favor", "願"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fazer", "作"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fazer", "做"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("febre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("febre", "病"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feder", "臭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feder", "聯合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feira", "曜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feliz", "歡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fenix")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fenix", "鳳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feroz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feroz", "猛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ferro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferro", "鐵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ferro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferro", "鉄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("filme")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filme", "電影"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("filtr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filtr", "濾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("final")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("final", "決賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("final")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("final", "底"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fluor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluor", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("focar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("focar", "焦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("foder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("foder", "肏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("folha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("folha", "葉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fonia", "樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("forca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forca", "力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("forma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forma", "形"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("forte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forte", "强"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fosse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fosse", "居让"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fosso", "堀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fraco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fraco", "弱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frade", "修士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frase")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frase", "闩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fruta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fruta", "果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fruto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fruto", "果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fuder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fuder", "操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fundo", "深"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ganda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganda", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ganza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganza", "大麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("garfo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garfo", "叉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("garra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garra", "爪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gente", "人人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gequi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gequi", "劇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("geral")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geral", "一般"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("giria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("giria", "俚語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gordo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gordo", "胖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gosto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gosto", "👍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("graus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("graus", "度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grelh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grelh", "炙烤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grupo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grupo", "団"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guerr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerr", "戰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guine", "🇬🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("habit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habit", "慣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hanes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hanes", "韓国語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hanis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hanis", "🇰🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hanis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hanis", "韓國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("heroi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("heroi", "英雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hidro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hidro", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hiper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hiper", "巨大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hoije")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hoije", "今日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("homem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("homem", "男"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hotel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hotel", "宾館"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hunon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hunon", "糊弄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ianos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ianos", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("idade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("idade", "年齡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ideia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ideia", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("igual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igual", "同"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("india")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("india", "🇮🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("india")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("india", "天竺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("indiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("indiv", "個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inger")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inger", "吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inser")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inser", "插"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("insta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("insta", "即"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("intro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intro", "入場曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iquii")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iquii", "愛奇藝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irmao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmao", "兄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irrit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irrit", "煩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("islao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("islao", "回教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("japao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japao", "🇯🇵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("japao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japao", "日本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("joana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joana", "瓢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jogar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogar", "玩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jogar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jogar", "玩耍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jovem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jovem", "子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("justo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("justo", "正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ladra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladra", "賊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lagoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagoa", "池"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lapis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lapis", "✏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lapis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lapis", "筆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("larga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("larga", "広"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("largo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largo", "広"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("latim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("latim", "拉丁字母"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("latim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("latim", "拉丁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lavar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lavar", "洗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leite", "奶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lembr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lembr", "記得"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lenta", "遲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lento", "遲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lento", "慢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leque", "扇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lider")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lider", "首"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limit", "限"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("linha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linha", "緣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("linha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linha", "線"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lirio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lirio", "百合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lista", "一覧表"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("livre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livre", "由"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("livro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livro", "書本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("livro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("livro", "冊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("local")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("local", "所"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("longa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("longa", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("longe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("longe", "遥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lorde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lorde", "主"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lotus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lotus", "蓮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("louco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louco", "狂乱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lugar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lugar", "席"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lugar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lugar", "位"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lutar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lutar", "搏鬥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macau", "🇲🇴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macau", "澳門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macho", "雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macio", "軟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magia", "魔術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magra", "薄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magra", "瘦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magro", "薄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magro", "瘦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maike")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maike", "邁克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maior", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mamas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamas", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manga", "芒果"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manha", "朝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manta", "毯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marca", "票"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marco", "記載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maria", "瑪麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("massa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("massa", "質量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("matar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("matar", "殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("media")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("media", "平均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medio", "平均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("melao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melao", "瓜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("melao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melao", "🍈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menos", "減"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mente", "然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mento", "理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("merda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("merda", "💩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("merda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("merda", "糞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meses", "朋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mesma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mesma", "就"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mesmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mesmo", "就"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("metro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metro", "地鐵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mexer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mexer", "動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("micro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("micro", "微"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("minha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("minha", "我的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mirar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mirar", "狙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("moeda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moeda", "銭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("moren")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moren", "默認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morre", "去死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morte", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morto", "死了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mostr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mostr", "示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("motiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("motiv", "促動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mudar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mudar", "切換"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muito", "大量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muito", "多"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muito", "夥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mundo", "界"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("museu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("museu", "博物館"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mutuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mutuo", "相"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naboa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naboa", "閑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nacao", "邦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nadar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nadar", "泳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("natal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natal", "聖誕節"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("natal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natal", "乡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("navio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("navio", "艦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nebul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nebul", "曇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("negro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("negro", "暗人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nervo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervo", "神經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nevoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nevoa", "霧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ninho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ninho", "巢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("noite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noite", "🌃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("noite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noite", "夜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("noiva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noiva", "嫁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("norma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("norma", "規範"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("norte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("norte", "北"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nosso", "我們的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nuvem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nuvem", "☁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obstr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obstr", "礙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obvio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obvio", "無論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("odeio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("odeio", "憎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oeste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oeste", "西"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olhos", "👀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("omais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omais", "最"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ontem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ontem", "昨天"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("opior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opior", "最低"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ordem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordem", "秩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ordem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordem", "令"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("orgao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orgao", "器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oscil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oscil", "摆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("otaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("otaco", "御宅族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("otodo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("otodo", "生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("outro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outro", "他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ouvir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouvir", "聽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ouvir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouvir", "听"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pagar", "払"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palma", "掌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("panda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panda", "熊貓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parar", "止"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parec", "亡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parte", "分"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parte", "發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passo", "步"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pasta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pasta", "漿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("patio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("patio", "庭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pavao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pavao", "孔雀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pavor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pavor", "戦慄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pedir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedir", "請"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pedra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedra", "石"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peido", "屁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peito", "胸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peixe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peixe", "魚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("peixe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("peixe", "🐟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("penis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("penis", "男根"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pente", "梳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perna")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perna", "腿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perto", "近"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessg", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("piano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("piano", "鋼琴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pilar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pilar", "柱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pilha", "堆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pinta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pinta", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pisar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pisar", "踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plano", "平"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plano", "計画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plano", "片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pobre", "貧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poder", "力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("podre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("podre", "腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponto", "点"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porca", "🐷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porco", "🐷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porco", "豚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porta", "門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porte", "提"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("posta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("posta", "乗到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poste", "柱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pouco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pouco", "少"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("praga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("praga", "疫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prata", "銀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prazo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prazo", "截"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("preco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preco", "價格"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prego", "釘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prend", "逮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("presa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presa", "獵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("press")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("press", "圧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("preto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preto", "黑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("preto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preto", "黑人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proib")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proib", "🚫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prova", "證"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("punho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("punho", "拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quebr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quebr", "斷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("queda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("queda", "落"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quimi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimi", "化"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quion", "阿虛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quiou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiou", "境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quiuu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiuu", "究"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quiuu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiuu", "級"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rampa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rampa", "坂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rapto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapto", "誘拐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("razao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("razao", "理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("receb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("receb", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("regra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("regra", "規"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reino", "王國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relig", "宗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("remar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("remar", "漕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reset")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reset", "重置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resid", "居"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ressu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ressu", "復"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resto", "遺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("retir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retir", "退"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("risos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("risos", "😂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ritmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ritmo", "拍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roque", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roubo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubo", "略奪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roupa", "服"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roupa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roupa", "衣服"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ruque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ruque", "魯克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("russo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("russo", "俄語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sabao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabao", "石鹸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saber")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saber", "知"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sabor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabor", "味"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saite", "網站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saiya")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saiya", "菜野"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("salao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salao", "堂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("salto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salto", "跳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("salvo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salvo", "安"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("santa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("santa", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("santo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("santo", "聖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saude")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saude", "健康"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segur", "持"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seita", "教條"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("senti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("senti", "感"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("serio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serio", "忍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("setsu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("setsu", "說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("siria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("siria", "🇸🇾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sitio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sitio", "站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobra", "餕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobre", "关于"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobre", "關於"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sodio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sodio", "鈉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("solto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solto", "緩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("somos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("somos", "我們是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("somos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("somos", "是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sonho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sonho", "夢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("soque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soque", "但"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sorte", "運"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("subir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subir", "昇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suchi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suchi", "🍣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sugar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sugar", "吸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suica", "🇨🇭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suite", "套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("super")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("super", "超"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sutil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sutil", "微"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tarde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tarde", "晚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tecla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tecla", "鍵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tecno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tecno", "科技"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("telha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("telha", "瓦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tempo", "時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tempo", "天气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tenda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tenda", "⛺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("terra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terra", "🌍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tesao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tesao", "勃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teste", "試"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tetas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tetas", "乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("texto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("texto", "📃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("texto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("texto", "闫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ticia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ticia", "聞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tigre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tigre", "虎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tique", "習慣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tirar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tirar", "取"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tocar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tocar", "触"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todas", "皆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todos", "皆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todos", "全"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("todos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("todos", "大家"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("toque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toque", "提示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("total")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("total", "完"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tradu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tradu", "譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trapo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trapo", "巾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trein")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trein", "鍛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tribo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tribo", "民族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("troca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("troca", "商"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tumor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tumor", "腫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("turno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("turno", "班"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ueder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ueder", "偉瑟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uniao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uniao", "聯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unica", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unico", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unido", "聯合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unido", "联合"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vacuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vacuo", "虛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vaice")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vaice", "罪惡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vamos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vamos", "行們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vapor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vapor", "汽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vasto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vasto", "浩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vasto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vasto", "洋洋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("veado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veado", "鹿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("velha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velha", "舊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("velho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velho", "舊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("veloc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veloc", "速"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vento", "🍃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vento", "風"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verde", "翠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verde", "綠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vezes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vezes", "倍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("video")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("video", "動圖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("video")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("video", "視頻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vigor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vigor", "勢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("virar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virar", "翻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("virar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virar", "向"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("visao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visao", "目光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("visao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visao", "視"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("visit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visit", "訪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vista", "観"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vista", "視圖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("visto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visto", "閲覧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vital", "活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viver", "住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vivid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vivid", "濃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("voces")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voces", "您們"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("volta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("volta", "回"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vomit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vomit", "吐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vosso", "你達的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xtrem")){
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
                if (mComposing.toString().toLowerCase().contains("abanar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abanar", "振"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acabar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acabar", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acabei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acabei", "終了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("actode")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("actode", "辦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acucar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acucar", "糖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acumul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acumul", "溜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adorar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adorar", "大好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adulto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adulto", "大人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("advert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("advert", "警"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afogar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afogar", "溺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afroux")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afroux", "弛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afroux")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afroux", "緩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarra", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agenda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agenda", "議程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aldeia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aldeia", "庄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alegre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alegre", "快樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alemao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alemao", "德文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alface")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alface", "生菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alguem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alguem", "某人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alguns")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alguns", "些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("almoco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("almoco", "昼食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alteza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alteza", "陛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("altura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("altura", "高度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aluzar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aluzar", "暮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amanha", "明日"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amarga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amarga", "苦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amargo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amargo", "苦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ameixa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ameixa", "李"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amolar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amolar", "砥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("angola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("angola", "🇦🇴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("angulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("angulo", "角"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anoite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anoite", "夕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("antiga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antiga", "古代"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("antigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antigo", "古代"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("antigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antigo", "古"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apagar", "消"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apagar", "刪除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apenas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apenas", "只"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apenas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apenas", "僅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apesar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apesar", "虽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apoder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apoder", "攫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apreci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apreci", "贊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aprend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprend", "學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aquela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aquela", "那個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aquele")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aquele", "那個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arranh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arranh", "掻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("artigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artigo", "条"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arvore")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvore", "木"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ascend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ascend", "登"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aspera")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspera", "粗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aspero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aspero", "粗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assist", "帮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assust", "怯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atalho", "近道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ataque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ataque", "擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aument")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aument", "增"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("avolta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avolta", "囲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("avolta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avolta", "辺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("backup")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("backup", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bairro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bairro", "區"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixar", "下載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("balanc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balanc", "振"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("balouc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balouc", "揮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banana", "香蕉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banana", "🍌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("batata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batata", "🥔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("batida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batida", "鼓動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("biblia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("biblia", "聖經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("biruss")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("biruss", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("birusu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("birusu", "比魯斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bonita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonita", "綺麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bonita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonita", "漂亮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bonito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonito", "漂亮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bonito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bonito", "帅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("branca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("branca", "白"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("branco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("branco", "白"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("branco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("branco", "白人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brasil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brasil", "🇧🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brasil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brasil", "巴西"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brutal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brutal", "給力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brutal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brutal", "真棒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brutal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brutal", "厉害"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("buffer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("buffer", "緩衝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("buraco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("buraco", "穴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("burori")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burori", "布羅利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cabeca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabeca", "頭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cabelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabelo", "髪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cabrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabrao", "王八蛋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caixao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caixao", "棺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("calcul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calcul", "算"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campos", "野"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cancao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cancao", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cancro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cancro", "癌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caneca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caneca", "杯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canesa", "韓國人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caneta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caneta", "🖊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canhao", "砲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantar", "歌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cantor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantor", "歌手"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("castig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("castig", "懲罰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("catana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catana", "刀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cavalo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cavalo", "馬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cavalo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cavalo", "🐎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("centro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("centro", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cereja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cereja", "櫻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chamar", "呼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chamar", "📣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chapeu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chapeu", "帽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("charla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("charla", "談"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chegar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chegar", "到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chichi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chichi", "尿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chines")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chines", "中華語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chocar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chocar", "震驚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("choque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("choque", "惊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chorar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chorar", "哭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("church")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("church", "教会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cidade", "市"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("classe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("classe", "類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("clicar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clicar", "選擇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("clique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clique", "点擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cobrir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cobrir", "網羅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coelho", "兔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coelho", "兎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colaps")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colaps", "崩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colect")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colect", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colega")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colega", "同學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colher", "🥄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colher", "勺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comeco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comeco", "開始"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comigo", "和我"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comite", "委"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comose")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comose", "似乎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("compar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compar", "比"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("compil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compil", "輯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("compil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compil", "集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confus", "混亂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("congel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("congel", "凍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conseg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conseg", "可以"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("consol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consol", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("constr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constr", "築"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contar", "数"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conter", "广"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contra", "對"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convid", "誘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cooper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cooper", "協"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("copiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("copiar", "复制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cordel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cordel", "紐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coreia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coreia", "高麗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("correr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correr", "跑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corrig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corrig", "訂正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coruja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coruja", "梟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cozido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozido", "熟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cresci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cresci", "成長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cristo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cristo", "基督"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("culhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("culhao", "㞗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dantes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dantes", "从前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("defend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("defend", "防"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("defesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("defesa", "守備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deitar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitar", "伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deixar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deixar", "交給"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deixar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deixar", "讓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demais", "太"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demoni")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demoni", "妖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("denovo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("denovo", "重新"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentro", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentro", "裡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentro", "内"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("depe10")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depe10", "辛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("depois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depois", "後"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("derrot")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derrot", "倒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("descai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descai", "垂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desejo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desejo", "願望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desenh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenh", "畫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desliz", "摺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("destes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destes", "這些"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("detudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("detudo", "都"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("devast")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devast", "驅逐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diante", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diario", "日記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direto", "直"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direto", "轉播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("discut")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("discut", "論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dispar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dispar", "發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("disper")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disper", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("divert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("divert", "樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobrar", "彎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobrar", "配音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doenca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doenca", "病"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dormir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dormir", "寝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doutor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutor", "博士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doutor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutor", "医者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dragao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dragao", "🐉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dragao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dragao", "龍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("duvida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("duvida", "疑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("efeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("efeito", "效"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ejacul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ejacul", "漏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("elabor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elabor", "講"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eletro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eletro", "電"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("elimin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elimin", "滅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("embora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embora", "虽然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emcada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emcada", "毎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emocao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emocao", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("empate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empate", "平局"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("encerr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encerr", "鎖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("encolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encolh", "縮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("endura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("endura", "忍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("engrax")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("engrax", "磨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enredo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enredo", "脚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ensaio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ensaio", "訓練"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ensino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ensino", "教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entend", "理解"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entend", "解"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enterr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enterr", "埋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enterr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enterr", "葬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entert", "娛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entrar", "入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entreg", "納"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entret")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entret", "娛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("equipa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equipa", "隊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("errado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("errado", "錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escada", "階段"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escada", "升"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escola", "学校"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escolh", "選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escrav")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrav", "隸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrev", "✍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escudo", "盾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escuro", "闇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esforc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esforc", "努"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esfreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esfreg", "擦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esfreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esfreg", "蹭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espaco", "間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espant", "驚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espero", "希望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espert", "賢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espeto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espeto", "串"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espiar", "覗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esporr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esporr", "漏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esposa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esposa", "妻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estava")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estava", "在了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrit", "嚴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("europa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("europa", "歐洲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exacto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exacto", "確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exaust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exaust", "尽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("expalh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("expalh", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("explod")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explod", "爆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("explor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explor", "研"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("export")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("export", "匯出"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extend", "張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extens")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extens", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("falcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("falcao", "隼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fantas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fantas", "奇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("favela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favela", "貧民窟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fedido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fedido", "臭了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fenixa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fenixa", "凰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("figado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("figado", "肝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("finais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("finais", "总决賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fisica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fisica", "物理學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flache")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flache", "閃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flecha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flecha", "箭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("floide")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floide", "佛洛伊德"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fodido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fodido", "苦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fornec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fornec", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fostes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fostes", "行您了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("franca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("franca", "仏國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frango")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frango", "鶏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frecam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frecam", "頻道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frente", "前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fresca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fresca", "鮮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fresco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fresco", "鮮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("funcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("funcao", "作用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("futuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futuro", "未來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gaiola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaiola", "籠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gananc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gananc", "貪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gaviao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gaviao", "鷹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gelado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gelado", "冷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("genero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("genero", "類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gentil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gentil", "優"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gerais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gerais", "一般"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gloria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gloria", "榮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gostar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gostar", "好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grande")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grande", "大"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gratis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gratis", "無償"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grecia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grecia", "🇬🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grossa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grossa", "厚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grosso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grosso", "厚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guerra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerra", "戰爭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("habito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habito", "癖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hancar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hancar", "漢字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("histor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("histor", "史"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hougan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hougan", "霍gan"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("humano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("humano", "人間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("idiota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("idiota", "蠢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("igreja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igreja", "教堂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ilumin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ilumin", "照"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imagem", "圖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imperi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperi", "帝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("implor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implor", "懇願"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inclin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inclin", "傾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("infame")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("infame", "臭名昭著"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inform")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inform", "告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ingles")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ingles", "英語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inicio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inicio", "初"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inseto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inseto", "虫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("instal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instal", "載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("invent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("invent", "开明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irmaza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmaza", "姐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("isolar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("isolar", "孤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("italia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("italia", "🇮🇹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("italia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("italia", "伊國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jantar", "夕食"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jardim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jardim", "廷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("joelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("joelho", "膝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jordao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jordao", "佐敦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("juntos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juntos", "共"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("juntos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juntos", "一起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("labios")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("labios", "唇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ladrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladrao", "賊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lament")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lament", "哀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("largar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largar", "离"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("levant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("levant", "起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("levant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("levant", "舉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("libert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("libert", "放"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limite", "限"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limpar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limpar", "拭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lingua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lingua", "👅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lingua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lingua", "言語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("linque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linque", "📥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("linque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linque", "🖇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("louvar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("louvar", "讃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macaco", "🐒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maique", "邁克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mamilo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamilo", "胸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquin", "機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("margem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("margem", "余裕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marido", "夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mascul")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mascul", "雄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mastig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mastig", "噛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medico", "医者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menina", "女児"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menina", "幼女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menina", "女子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menino", "男児"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("menino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("menino", "幼児"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("merece")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("merece", "該"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mestre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mestre", "師匠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mestre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mestre", "師"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("metodo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metodo", "法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mexico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mexico", "🇲🇽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("miguel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miguel", "米高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("milhao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("milhao", "百万"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mistur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mistur", "混"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("modelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("modelo", "型"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morena")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morena", "栗毛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morrer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morrer", "死"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("morreu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("morreu", "死了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muitas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitas", "很多"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muitos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitos", "很多"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mulher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mulher", "女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mulher")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mulher", "婦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("murcha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murcha", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("murcho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murcho", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("musica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musica", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("musica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musica", "🎶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("musica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musica", "音樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naaodo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naaodo", "沒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naomau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naomau", "不錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("narede")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("narede", "網上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nervos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervos", "神經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("normal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("normal", "普通"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("normal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("normal", "普"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("normal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("normal", "正常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nucleo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nucleo", "核"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("numero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numero", "數"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("objeto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("objeto", "象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oceano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oceano", "洋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oculos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oculos", "眼鏡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("omaior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omaior", "最高"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("opcoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opcoes", "選項"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("opniao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opniao", "意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oposto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oposto", "幸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("orbita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orbita", "軌道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("orelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orelha", "耳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("origem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("origem", "源"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("origem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("origem", "本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("outono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outono", "秋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("outono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outono", "秋季"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ouvido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ouvido", "耳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ovelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ovelha", "羊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pacote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pacote", "包"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pagina", "门"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pagina", "頁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("paixao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paixao", "戀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("panama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panama", "🇵🇦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("panpon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panpon", "盤點"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parece")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parece", "类似"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parede")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parede", "壁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partir", "折"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("patrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("patrao", "お嬢様"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("patrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("patrao", "司"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pendur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pendur", "吊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perceb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perceb", "懂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("percis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("percis", "要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perigo", "危"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("permit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("permit", "允"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perola")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perola", "珠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perseg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perseg", "追"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessei", "人生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoa", "👤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoa", "亻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessoa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoa", "人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("planej")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planej", "企"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("planta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planta", "植"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plasti")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plasti", "塑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poeira", "尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ponder")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponder", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porque", "為啥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porque", "為什麼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porque", "因為"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poster")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poster", "隔人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poupar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poupar", "貯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pralma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pralma", "銀魂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prazer", "愉快"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("precis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("precis", "当"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("premio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("premio", "賞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prepar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prepar", "備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("presas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presas", "獠齒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("privad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("privad", "私"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("procur")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("procur", "探"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("produz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produz", "產"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("produz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produz", "造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("propag")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("propag", "傳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proteg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proteg", "守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("psique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("psique", "靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pulmao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pulmao", "肺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quando")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quando", "何時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quanto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quanto", "幾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quarto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quarto", "屋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quatro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quatro", "4"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("queine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("queine", "肯ne"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quente", "熱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("querer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("querer", "願"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quimoi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimoi", "噁心"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quioco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quioco", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quiocu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quiocu", "曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ranfan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ranfan", "蘭芳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ranque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ranque", "排行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rapido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapido", "快"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rapido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapido", "速"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reacao", "反応"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("recife")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recife", "暗礁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("recolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recolh", "募"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("regiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("regiao", "域"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rejeit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rejeit", "拒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rejeit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rejeit", "悶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relevo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relevo", "显示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("report")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("report", "報"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reprim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reprim", "抑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reprov")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reprov", "斥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reserv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reserv", "訂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resist", "耐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resist")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resist", "抗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resumo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resumo", "概"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("retorc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorc", "捻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("retorn")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorn", "帰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("romano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("romano", "羅馬字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rotina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rotina", "課"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roubar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubar", "盗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrsrs", "笑笑笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("russia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("russia", "🇷🇺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("russia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("russia", "俄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sancao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sancao", "制裁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sangue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sangue", "血"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sapata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapata", "束"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sapato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sapato", "鞋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("satisf")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("satisf", "滿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saudad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saudad", "憧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("secret")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("secret", "泌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seculo", "世紀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seguro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguro", "安"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("semana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semana", "週"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sempre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sempre", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("senhor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("senhor", "氏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sentar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentar", "坐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sessao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sessao", "屆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("severo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("severo", "厳重"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("social")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("social", "社"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sofrer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sofrer", "遭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("solido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solido", "固況"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("solido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("solido", "固"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sonora", "音的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("spamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("spamar", "詐欺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("stoune")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("stoune", "石"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("subita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subita", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("subito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subito", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("subtra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("subtra", "減"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suecia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suecia", "🇸🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suport")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suport", "撐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suprim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suprim", "抑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("susurr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("susurr", "囁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tabaco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tabaco", "煙草"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("talvez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("talvez", "也許"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tambem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tambem", "也是"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tchuan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchuan", "串"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teimos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teimos", "頑固"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("templo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("templo", "殿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("temzem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temzem", "天然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("temzen")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temzen", "天然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tensao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tensao", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teoriz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teoriz", "查"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("termin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("termin", "終"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tibete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tibete", "藏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tingir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tingir", "染"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tomate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomate", "🍅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("topico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("topico", "題"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tornar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tornar", "成"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trancs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trancs", "特蘭克斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tratar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tratar", "治"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("treino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treino", "訓練"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tremer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tremer", "震"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tripla")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tripla", "三分"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("triste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("triste", "悲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trofia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trofia", "肥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tubara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubara", "鮫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vagina", "膣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vagina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vagina", "女陰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vencer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vencer", "勝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vender", "賣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vender", "売"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verifi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verifi", "驗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("versao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("versao", "版"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vestir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vestir", "穿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("viajar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("viajar", "遊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("virgem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virgem", "童貞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("voltar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("voltar", "帰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("volver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("volver", "轉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("xtremo")){
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
                if (mComposing.toString().toLowerCase().contains("abandon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abandon", "弃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("absolut")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("absolut", "絶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("activos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("activos", "資"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adicion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adicion", "添"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adormec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adormec", "眠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aescuta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aescuta", "房"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agarrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agarrar", "握"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agencia", "庁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alcunha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alcunha", "称"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alegrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alegrar", "喜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("algures")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("algures", "某地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alianca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alianca", "聯盟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aliment")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aliment", "餵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alinhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alinhar", "揃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amarelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amarelo", "黃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("america")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("america", "美州"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amostra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amostra", "例示"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("antecip")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("antecip", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apaixon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apaixon", "惚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apelido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apelido", "昵稱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apodrec")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apodrec", "腐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aprovar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprovar", "默認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("armarse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("armarse", "逞强"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("artesao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artesao", "職人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assento", "席"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assinal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinal", "任"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assunto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assunto", "臣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atraido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atraido", "惹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atropel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atropel", "轢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("austria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("austria", "🇦🇹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("avancar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("avancar", "前進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("balanco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balanco", "餘額"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("barriga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barriga", "腹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("batalha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batalha", "鬥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bateria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bateria", "電池"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("beji-ta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beji-ta", "比達"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("belgica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("belgica", "🇧🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bemvind")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bemvind", "歡迎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bolinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolinha", "圓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brigada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brigada", "團"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brincar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brincar", "玩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cadaver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadaver", "屍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cadeado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadeado", "鎖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cadeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cadeira", "椅子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caixote")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caixote", "桶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("camarao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camarao", "蝦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caminho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminho", "道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caminho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caminho", "径"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campeao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeao", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campeos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeos", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canabis")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canabis", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("canhamo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("canhamo", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cansada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cansada", "累"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cansado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cansado", "累了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cansado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cansado", "累"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("capital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("capital", "京"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caralho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caralho", "膣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carbono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carbono", "碳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("castelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("castelo", "城"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("centimo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("centimo", "分錢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("centimo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("centimo", "分€"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("central")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("central", "中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cerebro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cerebro", "腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("certeza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("certeza", "必"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chaomau")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaomau", "坏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chegada", "至"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cheiode")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cheiode", "漫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chiises")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chiises", "中國人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cigarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarra", "蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cigarro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarro", "烟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cintura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cintura", "腰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("circulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circulo", "陣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("circulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circulo", "◯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ciument")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ciument", "睨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cocoras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cocoras", "蹲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coentro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coentro", "香菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comboio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comboio", "列車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comedia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comedia", "喜劇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comemor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comemor", "祝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("complet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("complet", "成"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comprar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comprar", "買"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("compree")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compree", "認識"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("concavo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concavo", "凹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("concord")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concord", "約"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confiar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confiar", "信任"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confund")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confund", "惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("congrat")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("congrat", "拜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conhece")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhece", "会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("consegu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consegu", "出來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("constit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constit", "憲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contact")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contact", "絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contact")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contact", "連絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("continu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continu", "進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("continu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continu", "續"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convers")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convers", "聊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convers")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convers", "談"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convert", "換"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convexo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convexo", "凸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("convite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("convite", "招待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coracao", "♡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coracao", "心"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coragem", "勇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("correct")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correct", "妥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("costuma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("costuma", "曾經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cozinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cozinha", "厨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("crianca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crianca", "孩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("croacia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("croacia", "🇭🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cumprir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cumprir", "遵守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demanda", "需"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demonio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demonio", "惡靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deposit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deposit", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("derrota")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derrota", "敗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desafio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desafio", "挑戰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("descasc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasc", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desejar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desejar", "欲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("despesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("despesa", "経費"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deueine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deueine", "德懷恩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("devagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devagar", "慢慢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("devolta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devolta", "返回"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dificil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dificil", "難"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direcao", "方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direita", "→"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direito", "権"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direito", "權"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diversa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diversa", "雜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobrado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobrado", "配音了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dominar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dominar", "支配"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edepois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edepois", "然後"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("electro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("electro", "電"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emaranh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emaranh", "絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("embrulh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embrulh", "裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emissao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emissao", "播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emprego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprego", "職"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("empresa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empresa", "企業"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emprest")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprest", "貸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("energia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("energia", "能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entanto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entanto", "而"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enticar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enticar", "誘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("equador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equador", "🇪🇨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("erotico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("erotico", "情"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escolha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escolha", "挑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escovar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escovar", "刷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escrita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrita", "文"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrito", "書"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esguich")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esguich", "噴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esmagar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esmagar", "潰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espanha", "🇪🇸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("especie")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especie", "種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espelho", "鏡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esperar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperar", "等待"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esperma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperma", "精"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espinha", "脊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espinho", "刺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espreit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espreit", "傅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estacao", "季"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estadio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estadio", "🏟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estoque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estoque", "株"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrada", "道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrada", "路"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estreit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estreit", "狹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrela", "星"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrela", "☆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exemplo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exemplo", "例"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("existir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("existir", "存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("experma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experma", "精液"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extenso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extenso", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extremo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extremo", "極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fabrica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fabrica", "廠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("familia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("familia", "族"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("feitico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("feitico", "咒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("filosof")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filosof", "哲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("financa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("financa", "金融"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fitness")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fitness", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("floresc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floresc", "咲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flutuar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flutuar", "浮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("formato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formato", "格式"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("formosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formosa", "🇹🇼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("formosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formosa", "台灣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("formula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("formula", "式"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frangan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frangan", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("futebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futebol", "⚽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("futebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futebol", "足球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("galinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("galinha", "雞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ganbare")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ganbare", "加油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("garagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garagem", "輸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gautama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gautama", "釈迦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("geracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("geracao", "世"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gigante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gigante", "巨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gocosta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gocosta", "護岸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gordura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gordura", "脂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("governo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("governo", "政府"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gradual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gradual", "漸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grafica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grafica", "圖形"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gramado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gramado", "芝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grelhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grelhar", "炙烤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("grindar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("grindar", "錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guardar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guardar", "衛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guardar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guardar", "保存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("haneses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("haneses", "韓國人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hermita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hermita", "仙人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("holanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("holanda", "🇳🇱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("holanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("holanda", "荷蘭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("horario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("horario", "日程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hungria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hungria", "🇭🇺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imperio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperio", "帝國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imposto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imposto", "稅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("infeliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("infeliz", "不歡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inimigo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inimigo", "敵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iningue")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iningue", "裏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inscrev")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inscrev", "申"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("insecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("insecto", "虫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inserir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inserir", "插"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("instant")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instant", "瞬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("intelig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intelig", "智"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inverno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inverno", "冬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irlanda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irlanda", "🇮🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irritar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irritar", "慪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jamaica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jamaica", "🇯🇲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("japones")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japones", "日本人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("japones")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("japones", "日本語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jeimess")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jeimess", "詹姆斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Jeimess")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Jeimess", "詹me斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jornada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jornada", "旅程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("jusetsu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("jusetsu", "據說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("juvenil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juvenil", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lagarto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagarto", "蜥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lampada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lampada", "灯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("laranja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("laranja", "橙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("largura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("largura", "寬度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("latente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("latente", "潛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lebrone")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lebrone", "勒邦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lendido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lendido", "彩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("letonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("letonia", "🇱🇻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lheuian")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lheuian", "留言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lheuyan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lheuyan", "留言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ligacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ligacao", "⛓️"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ligeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ligeiro", "温馨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limpeza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limpeza", "掃除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lingjia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lingjia", "凌駕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("liquido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("liquido", "液"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("machado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("machado", "斧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maconha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maconha", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maioria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maioria", "大抵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maisque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maisque", "以上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maneira", "芳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maneira", "樣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maneira", "方法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquilh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquilh", "妆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquina", "機器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquina", "機械"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marcial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marcial", "武"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("masturb")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("masturb", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medicao", "測定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medroso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medroso", "胆小鬼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meiodia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meiodia", "午"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("melodia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melodia", "旋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("memoria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("memoria", "内存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mentira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mentira", "嘘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mescara")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mescara", "臉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mianmar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mianmar", "緬甸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("miseria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("miseria", "蕭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mistura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mistura", "配"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("momento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("momento", "時候"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("monstro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("monstro", "獸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mundial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mundial", "界"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("murchar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("murchar", "枯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("musculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musculo", "筋肉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("musculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("musculo", "筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("natural")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natural", "然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("negativ")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("negativ", "負"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nervosa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervosa", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nervoso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nervoso", "緊張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nigeria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nigeria", "🇳🇬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ninguem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ninguem", "無有人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("noticia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("noticia", "訊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("numerar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numerar", "採"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("numeros")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("numeros", "🔢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("objecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("objecto", "象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obsceno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obsceno", "猥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obscuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obscuro", "玄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oficial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oficial", "公式"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("omelhor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omelhor", "最好"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("onlaine")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onlaine", "網上"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("orgasmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orgasmo", "清"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("orvalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("orvalho", "露"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palacio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palacio", "宮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palavra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavra", "言葉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palavra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavra", "句"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pantano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pantano", "沼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pantazu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pantazu", "沼津"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("paragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paragem", "停"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parente", "亲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partido", "党"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passada", "祖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passado", "祖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passado", "了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passaro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passaro", "鳥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passaro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passaro", "🐤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pequeno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pequeno", "小"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perciso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perciso", "要"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perdoar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perdoar", "許"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pereira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pereira", "梨木"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pergunt")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pergunt", "尋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pescoco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pescoco", "頸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessego")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessego", "桃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessoas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoas", "👥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("picante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("picante", "辣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pimenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pimenta", "椒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pintura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pintura", "画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("planeta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planeta", "球生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plantar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plantar", "植"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("policia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("policia", "警察"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("polonia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("polonia", "🇵🇱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("popular")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("popular", "人气"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porisso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porisso", "所以說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("posicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("posicao", "位"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("postura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("postura", "態"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("praguia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("praguia", "銀鷹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pratica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pratica", "実践"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("preench")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preench", "塗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("present")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("present", "贈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pressao", "壓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primata", "霊長類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("process")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("process", "处"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("produto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produto", "積"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("produto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("produto", "価"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("program")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("program", "程序"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prologo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prologo", "序"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proprio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proprio", "自"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proxima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proxima", "次的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("purpura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("purpura", "紫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("quimica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("quimica", "化學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rafeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rafeiro", "狗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reajust")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reajust", "修"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("receber")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("receber", "受"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("receber")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("receber", "接到"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("registo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("registo", "錄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relacao", "關"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relacao", "関"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("renunci")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("renunci", "辞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reserva")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reserva", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("residuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("residuo", "廢物"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("retorno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retorno", "再臨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reverso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reverso", "🔄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("revisao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revisao", "修正"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("revista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revista", "誌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("risonho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("risonho", "笑容"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saifora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saifora", "干"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saiteji")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saiteji", "網址"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("salpico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("salpico", "潑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("samurai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("samurai", "士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("secreto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("secreto", "密"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segunda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segunda", "第二次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segunda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segunda", "乙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segundo", "乙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("segundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("segundo", "秒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("selecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("selecao", "精選"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("semente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semente", "種"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sentido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentido", "意義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sentido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentido", "義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("serdono")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serdono", "飼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("servico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("servico", "服務"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("simbolo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("simbolo", "符"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sistema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sistema", "系"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("soldado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("soldado", "兵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("somente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("somente", "唯一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sorriso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sorriso", "微笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sortuda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sortuda", "幸運"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sortudo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sortudo", "運幸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sozinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sozinho", "獨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("superar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("superar", "越"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("surpres")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("surpres", "😮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tchihan")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tchihan", "吃飯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teclado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teclado", "⌨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teclado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teclado", "鍵盘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tesouro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tesouro", "宝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tiquete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tiquete", "票"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("toranja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("toranja", "柚子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tortura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tortura", "拷問"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trabalh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trabalh", "働"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("traicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traicao", "叛逆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("traidor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traidor", "奸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trailer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trailer", "予告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trailer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trailer", "預告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("treiler")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treiler", "預告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("treinad")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("treinad", "督"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tubarao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tubarao", "鮫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tunisia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tunisia", "🇹🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ucrania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ucrania", "🇺🇦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("umcerto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umcerto", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unidade", "單"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uruguai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uruguai", "🇺🇾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("usuario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("usuario", "用户"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vacante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vacante", "缺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("valente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("valente", "勇敢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vegetal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vegetal", "菜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verdade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verdade", "真"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("violino")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("violino", "🎻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("virilha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("virilha", "股"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vontade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vontade", "意志"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates8() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("abertura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abertura", "頭曲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("acompanh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("acompanh", "伴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afrouxar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afrouxar", "緩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aindanao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aindanao", "未"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alemanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alemanha", "🇩🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("almofada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("almofada", "枕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("alquemia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("alquemia", "煉金術"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amaldico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amaldico", "呪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ambiente", "容"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ambiente", "環境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("androide")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("androide", "機器人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("animacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("animacao", "動画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aniverso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aniverso", "誕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aparatus")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aparatus", "装置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apertado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apertado", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apresent")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apresent", "紹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("apressar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("apressar", "突進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arvoredo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvoredo", "林"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assuntos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assuntos", "務"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atirador", "癶投者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atributo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atributo", "性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aventura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aventura", "冒險"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bacteria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bacteria", "菌"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixinho", "悄悄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bandeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bandeira", "旗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banheira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banheira", "呂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("banquete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("banquete", "宴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bardoque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bardoque", "巴達克"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("barreira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("barreira", "障碍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("beisebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beisebol", "野球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("beisebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("beisebol", "棒球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bloquear")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bloquear", "塞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("boasorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("boasorte", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bochecha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bochecha", "臉頰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bolafora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolafora", "邪球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bolibert")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolibert", "播放"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bomtempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bomtempo", "涼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bulgaria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bulgaria", "🇧🇬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("camarada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camarada", "同志"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("camaroes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("camaroes", "🇨🇲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeiro", "農夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campeoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeoes", "冠軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("capitulo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("capitulo", "章"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caracter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caracter", "字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carmesim")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carmesim", "茜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carnaval")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carnaval", "狂歡節"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carreira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carreira", "生涯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cigarras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cigarras", "蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("coleccao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("coleccao", "集"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colectar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colectar", "收"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("colombia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("colombia", "🇨🇴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("combater")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("combater", "反对"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comercio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comercio", "貿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comprido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comprido", "長"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conduzir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conduzir", "運転"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conectar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conectar", "係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conflito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conflito", "葛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conforto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conforto", "安慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("construi")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("construi", "作"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contente", "喜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contesto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contesto", "大賽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("continuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continuo", "連"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contribu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contribu", "貢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("controlo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("controlo", "控制"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("correcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("correcao", "正確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("corrente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("corrente", "當前"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cortador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cortador", "斤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("crocante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crocante", "脆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deacordo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deacordo", "依"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deitfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitfora", "棄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demonstr")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demonstr", "表"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("derreter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derreter", "溶"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("descasqu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasqu", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desculpa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desculpa", "抱歉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desistir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desistir", "諦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desporto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desporto", "運動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("destruid")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("destruid", "砕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diametro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diametro", "径"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diarreia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diarreia", "下痢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dinheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dinheiro", "錢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diospiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diospiro", "柿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("discurso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("discurso", "演說"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("distrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distrito", "区"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ditadura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ditadura", "独裁主義"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diversos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diversos", "雜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dizerpeg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dizerpeg", "討"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dobragem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dobragem", "配音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edificio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edificio", "🏢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("edificio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("edificio", "且"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("elefante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("elefante", "象"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eletrico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eletrico", "電"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("embrulha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("embrulha", "裝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("empurrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empurrar", "邁進"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("empurrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("empurrar", "推"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entregar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entregar", "送"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entrelac")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entrelac", "縛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("equivale")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("equivale", "等"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ervanova")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ervanova", "薪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escorreg")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escorreg", "滑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escrever")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escrever", "寫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espanhol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espanhol", "西語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("especial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especial", "特"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espirito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espirito", "靈"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esporrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esporrar", "絶頂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esquecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquecer", "忘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esquerda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquerda", "←"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("essencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("essencia", "精"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estacion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estacion", "停"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estetica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estetica", "美學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estranho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estranho", "怪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estupida")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estupida", "胸大無腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estupido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estupido", "笨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estupido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estupido", "愚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("evolucao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evolucao", "进行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("excitada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excitada", "兴奋女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("excitado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excitado", "兴奋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exclusiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exclusiv", "専"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exercito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exercito", "軍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("explosao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("explosao", "炸裂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extender", "拡張"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fantasma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fantasma", "鬼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fantasma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fantasma", "👻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fascinar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fascinar", "魅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ferencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferencia", "差別"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("festival")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("festival", "節"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ficheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ficheiro", "文件"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("financas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("financas", "金融"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("floresta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("floresta", "森"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fluencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluencia", "流量"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("folclore")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("folclore", "謡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fornecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fornecer", "提供"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gabinete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gabinete", "閣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("garganta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("garganta", "喉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gigantes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gigantes", "巨人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("glandula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("glandula", "腺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gregorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gregorio", "葛瑞格爾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guitarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guitarra", "吉他"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("honduras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("honduras", "🇭🇳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hospital")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hospital", "医院"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("importar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("importar", "輸入"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("impostar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("impostar", "定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imprimir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imprimir", "打印"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("incentiv")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incentiv", "誘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("incepcao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incepcao", "全面啟動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("incrivel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("incrivel", "厉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inferior")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inferior", "底"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inflatar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inflatar", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inspecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspecao", "験"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inspirou")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspirou", "影響了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("instavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instavel", "暴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("intencao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intencao", "意思"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("internet")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("internet", "網絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("investig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("investig", "究"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("investig")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("investig", "調"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("juntocom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("juntocom", "與"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lagrimas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagrimas", "泪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lealdade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lealdade", "忠実"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leiquers")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leiquers", "湖人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lembrete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lembrete", "提醒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("leopardo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("leopardo", "豹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("levantai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("levantai", "起來"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limitada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limitada", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("limitado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("limitado", "窮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("linhagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("linhagem", "系"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lituania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lituania", "🇱🇹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("macarrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("macarrao", "麵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mamifero")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamifero", "哺乳類"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marciais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marciais", "武"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("material")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("material", "材"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medecina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medecina", "医學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mediario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mediario", "媒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medicina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medicina", "医學"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mensagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mensagem", "消息"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("metafora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("metafora", "比喩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ministro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ministro", "臣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("moldavia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moldavia", "🇲🇩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("montanha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("montanha", "山"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naochama")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naochama", "🔇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naofazer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naofazer", "勿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("naogosto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("naogosto", "👎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nascente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nascente", "泉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("natureza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("natureza", "自然"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nutricao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nutricao", "栄養"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obrigado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obrigado", "謝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ocupacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ocupacao", "佔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oferenda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oferenda", "祭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("operacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("operacao", "操作"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("opressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("opressao", "弾圧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ordenado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordenado", "整"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("original")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("original", "原"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("outravez")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("outravez", "又"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("painatal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("painatal", "聖誕老人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palavras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavras", "詞"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("panponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("panponto", "盤點"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("paraguai")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paraguai", "🇵🇾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("parecido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("parecido", "似"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passardo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passardo", "過"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pecuaria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pecuaria", "畜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pendente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pendente", "懸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("percioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("percioso", "尊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("percurso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("percurso", "途"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perguica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perguica", "怠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perpetuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perpetuo", "永世"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pertence")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pertence", "屬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pisadela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pisadela", "踐踏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plastico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plastico", "塑料"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("polegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("polegada", "寸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("politica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("politica", "策"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("politica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("politica", "政"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ponderam")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponderam", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ponderar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponderar", "想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ponteiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ponteiro", "指針"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("porfavor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("porfavor", "請"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("portugal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugal", "葡國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("portugal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugal", "🇵🇹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("portugal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugal", "葡萄牙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("possivel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("possivel", "可"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("precioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("precioso", "尊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("predicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("predicao", "予言"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("presente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presente", "現"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primaver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primaver", "春"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeira", "第一次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primeira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeira", "甲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeiro", "甲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("primeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("primeiro", "第一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("principe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("principe", "王子"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("problema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("problema", "問"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("problema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("problema", "問題"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("projecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("projecto", "志意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prostrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prostrar", "平伏"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("protecao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("protecao", "護"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proteger")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proteger", "守"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("qualquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qualquer", "何"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rapariga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rapariga", "少女"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rebeldia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rebeldia", "叛變"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("recomend")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recomend", "薦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("redecada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redecada", "絡"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("refeicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("refeicao", "餐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reformar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reformar", "職場"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reformar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reformar", "改革"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relacoes", "關係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("relativo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("relativo", "的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("religiao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("religiao", "宗教"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rentavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rentavel", "儲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("respeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("respeito", "敬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("restante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("restante", "余"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrsrsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrsrsrs", "笑笑笑笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sabonete")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabonete", "石鹸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seguidor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguidor", "教徒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seguinte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguinte", "次"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sentenca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentenca", "刑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("serpente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("serpente", "蛇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("servidor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("servidor", "服務器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("silencio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("silencio", "無音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("situacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("situacao", "状"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobrepor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrepor", "疊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sporting")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sporting", "士砵亭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("starwars")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("starwars", "★戰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suastica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suastica", "卍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("submundo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("submundo", "幽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("substitu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("substitu", "代"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suspeito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suspeito", "疑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("susurrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("susurrar", "囁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tardinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tardinha", "昼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tentacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tentacao", "誘惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("terminal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terminal", "端末"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("terrivel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terrivel", "可怕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trabalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trabalho", "工"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("traducao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traducao", "翻譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("traduzir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("traduzir", "翻譯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tranquil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tranquil", "寧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transmit")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transmit", "送"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trascima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trascima", "备份"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trespass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trespass", "伐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trespass")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trespass", "斬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("turquesa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("turquesa", "碧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tutorial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tutorial", "教程"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uebsaite")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uebsaite", "網頁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("uindmill")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("uindmill", "大風車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ultimato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ultimato", "究極"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("umacerta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umacerta", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vermelho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vermelho", "赤"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("veterano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("veterano", "先生"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vinganca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vinganca", "復"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vinganca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vinganca", "復仇"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates9() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("abdominal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abdominal", "腹筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("adormecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("adormecer", "眠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("afundanco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("afundanco", "扣籃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aguaceiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aguaceiro", "雰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aleatorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleatorio", "随機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("amortecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("amortecer", "緩衝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anosatras")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anosatras", "昔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anotacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anotacoes", "記"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aparencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aparencia", "姿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aplicacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aplicacao", "應用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("argentina")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("argentina", "🇦🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("argumento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("argumento", "討"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aromatico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aromatico", "芳香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arquiteto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arquiteto", "建築士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("asescutas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("asescutas", "房"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assinalar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinalar", "📥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aterrador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aterrador", "恐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("atirar-se")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("atirar-se", "撲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("autocarro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("autocarro", "公共汽車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("azulclaro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulclaro", "青"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("batimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("batimento", "臟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bluetooth")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bluetooth", "藍牙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bolasfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bolasfora", "邪球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bom-ponto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bom-ponto", "正論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brilhante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brilhante", "燦爛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("brinquedo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("brinquedo", "玩具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("burocrata")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("burocrata", "僚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cabimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cabimento", "康健"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caboverde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caboverde", "🇨🇻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campainha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campainha", "鐘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("caracheia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("caracheia", "满面"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carafeliz")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carafeliz", "笑顔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carbonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carbonato", "碳酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("casamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("casamento", "婚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cavaleiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cavaleiro", "騎士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chocolate")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chocolate", "🍫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("clavicula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("clavicula", "鎖骨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("companhia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("companhia", "社"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conclusao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conclusao", "結論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confirmar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confirmar", "確認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confortar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confortar", "慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conhecido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhecido", "仲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("consegues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consegues", "挺住"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contrario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contrario", "逆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("crocodilo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("crocodilo", "鱷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("delicioso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delicioso", "好吃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("demasiado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("demasiado", "太"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("depressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("depressao", "鬱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("derepente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("derepente", "突"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("descascar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descascar", "剥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desinstal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desinstal", "卸載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desmascar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desmascar", "揭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diferente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diferente", "別"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("disciplin")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("disciplin", "惩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dispersar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dispersar", "散"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("distancia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("distancia", "距離"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doutorada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutorada", "博士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("doutorado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("doutorado", "博士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("efeitosom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("efeitosom", "音效"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emagrecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emagrecer", "痩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emdirecto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emdirecto", "轉播"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emprestar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprestar", "貸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("encarnado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encarnado", "紅"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("encontrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encontrar", "找"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("encontrar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("encontrar", "会"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("enfrentar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("enfrentar", "面对"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escoteiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escoteiro", "偵察"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escritura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escritura", "經"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eslovenia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eslovenia", "🇸🇮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esmeralda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esmeralda", "翠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("espantado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("espantado", "惑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esperanca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esperanca", "希望"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esquadrao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esquadrao", "團"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrututa")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrututa", "構造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("evacuacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("evacuacao", "撤退"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("excelente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excelente", "优"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("excepcion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("excepcion", "除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("favoravel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("favoravel", "利"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("federacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("federacao", "聯合會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("filipinas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("filipinas", "🇵🇭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fragancia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fragancia", "香"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frequente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frequente", "頻繁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ginastica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ginastica", "體操"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gravidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gravidade", "重力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("guerreiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("guerreiro", "戰士"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hiobsceno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hiobsceno", "卑猥"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("historico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("historico", "歷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imperador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imperador", "皇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("implement")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implement", "實行"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("impressao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("impressao", "打印"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("individuo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("individuo", "個"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("indonesia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("indonesia", "🇮🇩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inspecion")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspecion", "診"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("interesse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("interesse", "趣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irmandade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmandade", "兄弟會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irmazinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmazinha", "妹"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irritante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irritante", "惱人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("labirinto")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("labirinto", "迷宮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ladoalado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ladoalado", "並"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lagueiros")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lagueiros", "湖人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magnetica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magnetica", "磁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("magnetico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("magnetico", "磁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maistarde")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maistarde", "稍後"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mandibula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandibula", "顎"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("marijuana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("marijuana", "麻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("masturbar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("masturbar", "自慰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meditacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meditacao", "瞑想"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("motivacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("motivacao", "促動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("municipio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("municipio", "県"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nascalmas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nascalmas", "福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("navegador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("navegador", "瀏覽器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nicaragua")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nicaragua", "🇳🇮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nostalgia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nostalgia", "懐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obediente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obediente", "従順"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("objectivo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("objectivo", "目標"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("obstaculo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("obstaculo", "障"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ordinario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ordinario", "常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("paquistao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("paquistao", "🇵🇰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("particula")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("particula", "微尘"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partilhar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partilhar", "分享"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partirem2")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partirem2", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partitura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partitura", "譜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pauzinhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pauzinhos", "箸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pesquisar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pesquisar", "搜索"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("populacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("populacao", "人口"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pordelado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pordelado", "除"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("portugues")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugues", "葡語"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pressagio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pressagio", "緣起"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prestigio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prestigio", "譽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("principal")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("principal", "主"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("proclamar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("proclamar", "布告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("professor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("professor", "教師"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("projectar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("projectar", "映"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prudencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prudencia", "謀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("qualidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("qualidade", "質"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("repetidas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("repetidas", "多発"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("republica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("republica", "共和國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reputacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reputacao", "譽"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("retardado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("retardado", "笨蛋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("romantico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("romantico", "浪漫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roqueroll")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roqueroll", "搖滾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sabedoria")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sabedoria", "慧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("seguranca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("seguranca", "安全"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sepultura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sepultura", "墓"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("signature")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("signature", "獨門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("singapura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("singapura", "🇸🇬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sojamolho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sojamolho", "醬油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sucessivo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sucessivo", "連続"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tartaruga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tartaruga", "🐢"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tartaruga")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tartaruga", "龜"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("telemovel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("telemovel", "手機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("televisao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("televisao", "電視"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("temporada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temporada", "期"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tendencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tendencia", "看点"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("terrestre")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("terrestre", "陸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("testemunh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("testemunh", "訴"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tomaconta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomaconta", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("torancusu")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("torancusu", "特蘭克斯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transform")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transform", "化"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("umaooutro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umaooutro", "互"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unificado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unificado", "統一"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("unscertos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("unscertos", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("utensilio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("utensilio", "機器"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("variedade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("variedade", "綜艺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("venezuela")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("venezuela", "🇻🇪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("visitante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("visitante", "亭"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates10() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("abdominais")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("abdominais", "腹筋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("agricultor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("agricultor", "農夫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aplicacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aplicacoes", "應用"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arrepender")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arrepender", "後悔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("artificial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("artificial", "人造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assinatura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assinatura", "獨門"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assustador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assustador", "怖"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("automatico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("automatico", "自動"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("azulescuro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulescuro", "蒼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("balconista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("balconista", "係"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("calendario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("calendario", "曆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("campeonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("campeonato", "冠軍的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cantoneses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cantoneses", "廣東人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("capacidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("capacidade", "設備"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("carregando")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("carregando", "途中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("catastrofe")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("catastrofe", "災"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("cientifica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("cientifica", "科學的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comentario", "評論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("computador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("computador", "電腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("comunidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("comunidade", "社區"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("continente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continente", "州"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("delineador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("delineador", "眼線"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dependente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dependente", "依存"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dependente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dependente", "👫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("descasquei")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("descasquei", "剥了"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dividirem2")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dividirem2", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("esfregarse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("esfregarse", "蹭"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("eslovaquia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("eslovaquia", "🇸🇰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Especifica")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Especifica", "特定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("especifico")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especifico", "特定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("examinacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("examinacao", "診断"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exatamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exatamente", "正確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("existencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("existencia", "存在"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extreminio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extreminio", "虐殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fazercresc")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fazercresc", "養"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("felicidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("felicidade", "幸福"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ferramenta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ferramenta", "具"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fimdalinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fimdalinha", "絶体絶命"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fimdalinha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fimdalinha", "窮途末路"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("habilidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("habilidade", "能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hidrogenio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hidrogenio", "氫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("identidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("identidade", "素性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("igualmente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("igualmente", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imaginacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imaginacao", "想像"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("informacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("informacao", "告闫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inglaterra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inglaterra", "英國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inspiracao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inspiracao", "影響"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("instamorte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("instamorte", "秒殺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("intrometer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("intrometer", "妨礙"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("irmaozinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("irmaozinho", "弟"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("iuserneime")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("iuserneime", "賬號"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("julgamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("julgamento", "審"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("lamentavel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("lamentavel", "可哀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("luxemburgo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("luxemburgo", "🇱🇺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maiorparte")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maiorparte", "大半"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mandarfora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandarfora", "舍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manuscrito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manuscrito", "巻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("masoquismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("masoquismo", "自虐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mauritania")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mauritania", "🇲🇷"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("melancolia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("melancolia", "忧鬱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mensageiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mensageiro", "送言者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mocambique")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mocambique", "🇲🇿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mulherengo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mulherengo", "淫棍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("multi-usos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("multi-usos", "多用途"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("passatempo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("passatempo", "趣味"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pervertido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pervertido", "好色"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pinocabelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pinocabelo", "釵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plataforma")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plataforma", "台"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("prefeitura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("prefeitura", "県"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("presidente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presidente", "总統"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("presidente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("presidente", "總統"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("propaganda")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("propaganda", "宣傳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("provisorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("provisorio", "儚"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("queprovoca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("queprovoca", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("recomendar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("recomendar", "薦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("reconhecer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("reconhecer", "認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("referencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("referencia", "典"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("revelacoes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("revelacoes", "示錄"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roubarloja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roubarloja", "万引"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrsrsrsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrsrsrsrs", "笑笑笑笑笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sentimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sentimento", "感覺"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobrepesca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrepesca", "乱獲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("socialista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("socialista", "社會"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("substituir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("substituir", "替換"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("substituir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("substituir", "変換"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("suficiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("suficiente", "充分"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tempestade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tempestade", "嵐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("temporario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temporario", "暫時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("timorleste")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("timorleste", "🇹🇱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tomarconta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tomarconta", "預"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("Transferir")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("Transferir", "下載"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trapaceiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trapaceiro", "弊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("umascertas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("umascertas", "某"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("velocidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("velocidade", "速度"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("verdadeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("verdadeiro", "本"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vulneravel")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vulneravel", "脆弱"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("ziguezague")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("ziguezague", "蛇行"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates11() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("anderteiker")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anderteiker", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aocontrario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aocontrario", "倒"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("arquitetura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arquitetura", "建築"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("assistencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("assistencia", "收視率"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bandasonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bandasonora", "原聲音樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("basquetebol")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("basquetebol", "籃球"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bicarbonato")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bicarbonato", "氫碳酸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("chaodepeolh")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("chaodepeolh", "境"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("compensacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("compensacao", "賠"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("concentrado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("concentrado", "濃"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("confirmacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("confirmacao", "確認"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("continental")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("continental", "大陸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("curiosidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("curiosidade", "好奇"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dentehumano")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dentehumano", "齿"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desenvolver")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolver", "開發"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desperdicar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desperdicar", "粗末"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("devagarinho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("devagarinho", "慢慢地"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("diariamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("diariamente", "今日的"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emprincipio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprincipio", "從"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("emprogresso")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("emprogresso", "途中"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("escritoamao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("escritoamao", "手寫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estemomento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estemomento", "此時"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estrangeiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estrangeiro", "外國"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exactamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exactamente", "正確"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("experiencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experiencia", "實驗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("experiencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experiencia", "試驗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("flurescente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("flurescente", "蛍光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("gorduralado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("gorduralado", "脂肪"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("hancaracter")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("hancaracter", "漢字"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("implementar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("implementar", "実現"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("imprudencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("imprudencia", "無謀"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inflacionar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inflacionar", "膨"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("localizacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("localizacao", "位置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maisoumenos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maisoumenos", "均"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manhafresca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manhafresca", "🇰🇵"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("manhafresca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("manhafresca", "朝鮮"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maoprojetil")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maoprojetil", "投"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquilhagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquilhagem", "妆"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("maquilhagem")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("maquilhagem", "粧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("medirforcas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("medirforcas", "摔"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("moinhovento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("moinhovento", "大風車"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("molhodesoja")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("molhodesoja", "醬油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muitasvezes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitasvezes", "屡々"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("omnipotente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("omnipotente", "全能"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("onomatopeia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("onomatopeia", "擬音"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("oqueprovoca")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("oqueprovoca", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("originalsom")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("originalsom", "原聲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("portugueses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("portugueses", "葡人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poucoapouco")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poucoapouco", "段段"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("preconceito")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("preconceito", "種差別"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("programacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programacao", "計機"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("programacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programacao", "程序設計"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("programador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("programador", "程序者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("publicidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("publicidade", "広告"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("redefinicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redefinicao", "重置"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("repositorio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("repositorio", "府"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resistencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resistencia", "抵抗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("saidafrente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("saidafrente", "让開吧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("semqualquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("semqualquer", "毫无"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("significado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("significado", "意味"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobrancelha")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrancelha", "眉"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("systemprint")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("systemprint", "💻🎫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("temproblema")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("temproblema", "搞錯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("teracerteza")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("teracerteza", "見定"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trabalhador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trabalhador", "工人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("tradicional")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("tradicional", "繁體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transformar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transformar", "改造"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transmissao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transmissao", "轉播"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates12() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("aindaporcima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aindaporcima", "且"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("aleatoridade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aleatoridade", "随機性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("anderteiquer")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("anderteiquer", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baixotirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baixotirador", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("baterpunheta")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("baterpunheta", "手淫"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("conhecimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("conhecimento", "智"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("constituicao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("constituicao", "憲法"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contraataque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contraataque", "反擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("contraataque")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("contraataque", "回擊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("decumentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("decumentario", "紀錄片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deitaraochao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deitaraochao", "堕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("denteamarelo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("denteamarelo", "歯"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("deslumbrante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("deslumbrante", "眩"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desprevenido")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desprevenido", "油断"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("determinismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("determinismo", "決定論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("documentario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("documentario", "紀錄片"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("durabilidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("durabilidade", "耐久"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("especialista")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("especialista", "名人"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("estatisticas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("estatisticas", "統計"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("experimental")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("experimental", "實驗"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fluorescente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fluorescente", "蛍光"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inteligencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inteligencia", "情報"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("interessante")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("interessante", "有趣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mamasgrandes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamasgrandes", "波霸"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mamasgrandes")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamasgrandes", "巨乳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mandarabaixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mandarabaixo", "辛"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("meioambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("meioambiente", "容"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("necessidades")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("necessidades", "便"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("palavrapasse")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("palavrapasse", "密碼"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("partiremdois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("partiremdois", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("perservativo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("perservativo", "套"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pessoaescura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pessoaescura", "黒坊"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("plaistaichon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("plaistaichon", "遊戲站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("planejamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("planejamento", "企画"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pleisteichon")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pleisteichon", "遊戲站"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("podermilitar")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("podermilitar", "武力"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("poroutrolado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("poroutrolado", "而"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("redefacaocor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("redefacaocor", "絕"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("roupadebaixo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("roupadebaixo", "裾"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("rsrsrsrsrsrs")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("rsrsrsrsrsrs", "笑笑笑笑笑笑"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("simplificado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("simplificado", "簡體"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobrevivente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrevivente", "幸存活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transparente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transparente", "透明"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("trilhasonora")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("trilhasonora", "配樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("vistadeolhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("vistadeolhos", "一覧"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates13() {
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
                if (mComposing.toString().toLowerCase().contains("arabiasaudita")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arabiasaudita", "🇸🇦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("armazenamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("armazenamento", "存儲"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("bompresidente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("bompresidente", "王道"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desenvolvedor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolvedor", "開發者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("dividiremdois")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dividiremdois", "隻"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entardcigarra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entardcigarra", "暮蟬"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("extraodinario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("extraodinario", "非常"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("internacional")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("internacional", "國際"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("malaoambiente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("malaoambiente", "乱開発"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("mausentimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mausentimento", "惡心"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("muitoobrigado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("muitoobrigado", "多謝"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nacionalidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nacionalidade", "國籍"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nomededominio")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nomededominio", "域名"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("nomedeusuario")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("nomedeusuario", "賬號"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("olharparacima")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("olharparacima", "仰"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("piscardeolhos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("piscardeolhos", "瞬間"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("processamento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("processamento", "处理"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("racionalidade")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("racionalidade", "理性"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("sobrevivencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("sobrevivencia", "存活"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("transferencia")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("transferencia", "通信"))), true, true);}
            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates15() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("arvorepolegada")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("arvorepolegada", "村"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("azulesverdeado")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("azulesverdeado", "碧"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("circunstancias")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circunstancias", "状況"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("debaixotirador")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("debaixotirador", "送葬者"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desflorestacao")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desflorestacao", "乱伐"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("entretenimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("entretenimento", "娛樂"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("fisiculturismo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("fisiculturismo", "健美"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("frequentemente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("frequentemente", "頻繁"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("resplandecente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("resplandecente", "光明"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates16() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("circuitovirtual")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("circuitovirtual", "回線"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desenvolvimento")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desenvolvimento", "発展"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("direitoshumanos")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("direitoshumanos", "人権"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("forcanascanelas")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("forcanascanelas", "加油"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("futuropromissor")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("futuropromissor", "前途洋洋"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("localdetrabalho")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("localdetrabalho", "職場"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates17() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("consequentemente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("consequentemente", "因此"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("exatamenteomesmo")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("exatamenteomesmo", "一模一樣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("inexpectadamente")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("inexpectadamente", "不意"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("processojudicial")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("processojudicial", "訴訟"))), true, true);}
            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates18() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("dequalquermaneira")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("dequalquermaneira", "無論"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("declaracaodeguerra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("declaracaodeguerra", "宣戦"))), true, true);}
            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates19() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("aprovadopelogoverno")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("aprovadopelogoverno", "官方"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("desperdicardinheiro")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("desperdicardinheiro", "無駄遣"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("pedrapapeloutesoura")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("pedrapapeloutesoura", "猜拳"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("republicadominicana")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("republicadominicana", "🇩🇴"))), true, true);}

            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }

    private void updateCandidates20() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                if (mComposing.toString().toLowerCase().contains("mamasgrandesmasburra")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("mamasgrandesmasburra", "胸大無腦"))), true, true);}
                else if (mComposing.toString().toLowerCase().contains("namelhordashipoteses")){
                    setSuggestions(new ArrayList<String>(Arrays.asList(mComposing.toString().toLowerCase().replace("namelhordashipoteses", "精々"))), true, true);}
            }
            else {
                setSuggestions(null, false, false);
            }
        }
    }


    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
            try {Log.i(TAG, "setSuggestions: se mcandidateview != nula" + suggestions.toString());}
            catch (Exception NullPointerException) {
                Log.i(TAG, "setSuggestions: Não há sugestões");
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
            updateCandidates13();
            updateCandidates14();
            updateCandidates15();
            updateCandidates16();
            updateCandidates17();
            updateCandidates18();
            updateCandidates19();
            updateCandidates20();
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
            updateCandidates13();
            updateCandidates14();
            updateCandidates15();
            updateCandidates16();
            updateCandidates17();
            updateCandidates18();
            updateCandidates19();
            updateCandidates20();
            Log.i(TAG, "handleBackspace: se length > 0, mcomposing.length:" + mComposing.length());
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }
    private void handleShift() {
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
            updateCandidates13();
            updateCandidates14();
            updateCandidates15();
            updateCandidates16();
            updateCandidates17();
            updateCandidates18();
            updateCandidates19();
            updateCandidates20(); ///este é o primeiro
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
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
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
                updateCandidates13();
                updateCandidates14();
                updateCandidates15();
                updateCandidates16();
                updateCandidates17();
                updateCandidates18();
                updateCandidates19();
                updateCandidates20(); //mcomposing em else
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