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
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
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
    @Override public View onCreateInputView() {
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
    @Override public View onCreateCandidatesView() {
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
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        Log.i(TAG, "onStartInput: ");
        updateCandidates();

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
    @Override public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        Log.i(TAG, "onFinishInput: ");
        updateCandidates();

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

    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
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
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
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
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
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
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);
            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
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
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
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
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
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
            updateCandidates();
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
    private void updateCandidates() {
        Log.i(TAG, "updateCandidates: começou, mcomposing:" + mComposing.toString() );
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                switch (mComposing.toString().toLowerCase()){
                    case "salto":setSuggestions(new ArrayList<String>(Arrays.asList("跳")), true, true);
                        break;
                    case "abanar":setSuggestions(new ArrayList<String>(Arrays.asList("振")), true, true);
                        break;
                    case "abdomina":setSuggestions(new ArrayList<String>(Arrays.asList("腹筋")), true, true);
                        break;
                    case "abdominais":setSuggestions(new ArrayList<String>(Arrays.asList("腹筋")), true, true);
                        break;
                    case "Abdominal":setSuggestions(new ArrayList<String>(Arrays.asList("腹筋")), true, true);
                        break;
                    case "abertura":setSuggestions(new ArrayList<String>(Arrays.asList("頭曲")), true, true);
                        break;
                    case "activ":setSuggestions(new ArrayList<String>(Arrays.asList("啟")), true, true);
                        break;
                    case "absolutamente":setSuggestions(new ArrayList<String>(Arrays.asList("絶対")), true, true);
                        break;
                    case "absolutamentenada":setSuggestions(new ArrayList<String>(Arrays.asList("全然")), true, true);
                        break;
                    case "abuso":setSuggestions(new ArrayList<String>(Arrays.asList("虐待")), true, true);
                        break;
                    case "acabar":setSuggestions(new ArrayList<String>(Arrays.asList("完成", "終")), true, true);
                        break;
                    case "acabei":setSuggestions(new ArrayList<String>(Arrays.asList("終了")), true, true);
                        break;
                    case "acessibilidade":setSuggestions(new ArrayList<String>(Arrays.asList("無障碍")), true, true);
                        break;
                    case "aco":setSuggestions(new ArrayList<String>(Arrays.asList("鋼")), true, true);
                        break;
                    case "acola":setSuggestions(new ArrayList<String>(Arrays.asList("那边")), true, true);
                        break;
                    case "acucar":setSuggestions(new ArrayList<String>(Arrays.asList("糖")), true, true);
                        break;
                    case "acumular":setSuggestions(new ArrayList<String>(Arrays.asList("貯")), true, true);
                        break;
                    case "ad":setSuggestions(new ArrayList<String>(Arrays.asList("增")), true, true);
                        break;
                    case "adi":setSuggestions(new ArrayList<String>(Arrays.asList("統合發発環境")), true, true);
                        break;
                    case "adicion":setSuggestions(new ArrayList<String>(Arrays.asList("新增")), true, true);
                        break;
                    case "adicional":setSuggestions(new ArrayList<String>(Arrays.asList("增")), true, true);
                        break;
                    case "adicionar":setSuggestions(new ArrayList<String>(Arrays.asList("新增")), true, true);
                        break;
                    case "ado":setSuggestions(new ArrayList<String>(Arrays.asList("慕")), true, true);
                        break;
                    case "adoptar":setSuggestions(new ArrayList<String>(Arrays.asList("採用")), true, true);
                        break;
                    case "ador":setSuggestions(new ArrayList<String>(Arrays.asList("慕")), true, true);
                        break;
                    case "adorar":setSuggestions(new ArrayList<String>(Arrays.asList("大好", "慕")), true, true);
                        break;
                    case "adulto":setSuggestions(new ArrayList<String>(Arrays.asList("大人")), true, true);
                        break;
                    case "afog":setSuggestions(new ArrayList<String>(Arrays.asList("溺")), true, true);
                        break;
                    case "afogar":setSuggestions(new ArrayList<String>(Arrays.asList("溺")), true, true);
                        break;
                    case "afroux":setSuggestions(new ArrayList<String>(Arrays.asList("緩")), true, true);
                        break;
                    case "afrouxar":setSuggestions(new ArrayList<String>(Arrays.asList("緩")), true, true);
                        break;
                    case "afundanco":setSuggestions(new ArrayList<String>(Arrays.asList("扣籃")), true, true);
                        break;
                    case "agarr":setSuggestions(new ArrayList<String>(Arrays.asList("握")), true, true);
                        break;
                    case "agarra":setSuggestions(new ArrayList<String>(Arrays.asList("握")), true, true);
                        break;
                    case "agarrar":setSuggestions(new ArrayList<String>(Arrays.asList("握")), true, true);
                        break;
                    case "agencia":setSuggestions(new ArrayList<String>(Arrays.asList("事務所")), true, true);
                        break;
                    case "agora":setSuggestions(new ArrayList<String>(Arrays.asList("今")), true, true);
                        break;
                    case "agri":setSuggestions(new ArrayList<String>(Arrays.asList("農")), true, true);
                        break;
                    case "agricultor":setSuggestions(new ArrayList<String>(Arrays.asList("農夫")), true, true);
                        break;
                    case "agua":setSuggestions(new ArrayList<String>(Arrays.asList("水")), true, true);
                        break;
                    case "aguia":setSuggestions(new ArrayList<String>(Arrays.asList("鷹")), true, true);
                        break;
                    case "ah":setSuggestions(new ArrayList<String>(Arrays.asList("呵")), true, true);
                        break;
                    case "ai":setSuggestions(new ArrayList<String>(Arrays.asList("那里", "艾")), true, true);
                        break;
                    case "ainda":setSuggestions(new ArrayList<String>(Arrays.asList("還")), true, true);
                        break;
                    case "ajudar":setSuggestions(new ArrayList<String>(Arrays.asList("帮")), true, true);
                        break;
                    case "al":setSuggestions(new ArrayList<String>(Arrays.asList("聖")), true, true);
                        break;
                    case "alarg":setSuggestions(new ArrayList<String>(Arrays.asList("拡")), true, true);
                        break;
                    case "alcunha":setSuggestions(new ArrayList<String>(Arrays.asList("称")), true, true);
                        break;
                    case "aleatoridade":setSuggestions(new ArrayList<String>(Arrays.asList("随機性")), true, true);
                        break;
                    case "aleatorio":setSuggestions(new ArrayList<String>(Arrays.asList("随機")), true, true);
                        break;
                    case "alegrar":setSuggestions(new ArrayList<String>(Arrays.asList("喜")), true, true);
                        break;
                    case "alegre":setSuggestions(new ArrayList<String>(Arrays.asList("快樂")), true, true);
                        break;
                    case "alemao":setSuggestions(new ArrayList<String>(Arrays.asList("德文")), true, true);
                        break;
                    case "alface":setSuggestions(new ArrayList<String>(Arrays.asList("生菜")), true, true);
                        break;
                    case "alguem":setSuggestions(new ArrayList<String>(Arrays.asList("某人")), true, true);
                        break;
                    case "alguns":setSuggestions(new ArrayList<String>(Arrays.asList("些")), true, true);
                        break;
                    case "algures":setSuggestions(new ArrayList<String>(Arrays.asList("某地")), true, true);
                        break;
                    case "ali":setSuggestions(new ArrayList<String>(Arrays.asList("那里")), true, true);
                        break;
                    case "alianca":setSuggestions(new ArrayList<String>(Arrays.asList("聯盟")), true, true);
                        break;
                    case "alinh":setSuggestions(new ArrayList<String>(Arrays.asList("揃")), true, true);
                        break;
                    case "alinhar":setSuggestions(new ArrayList<String>(Arrays.asList("並", "揃")), true, true);
                        break;
                    case "alma":setSuggestions(new ArrayList<String>(Arrays.asList("魂")), true, true);
                        break;
                    case "almoco":setSuggestions(new ArrayList<String>(Arrays.asList("昼食")), true, true);
                        break;
                    case "almofada":setSuggestions(new ArrayList<String>(Arrays.asList("枕")), true, true);
                        break;
                    case "alquemia":setSuggestions(new ArrayList<String>(Arrays.asList("煉金術")), true, true);
                        break;
                    case "altura":setSuggestions(new ArrayList<String>(Arrays.asList("高度")), true, true);
                        break;
                    case "amanha":setSuggestions(new ArrayList<String>(Arrays.asList("明日")), true, true);
                        break;
                    case "amar":setSuggestions(new ArrayList<String>(Arrays.asList("愛")), true, true);
                        break;
                    case "amarelo":setSuggestions(new ArrayList<String>(Arrays.asList("黃")), true, true);
                        break;
                    case "ambiente":setSuggestions(new ArrayList<String>(Arrays.asList("環境")), true, true);
                        break;
                    case "ambos":setSuggestions(new ArrayList<String>(Arrays.asList("雙")), true, true);
                        break;
                    case "america":setSuggestions(new ArrayList<String>(Arrays.asList("美州")), true, true);
                        break;
                    case "amolar":setSuggestions(new ArrayList<String>(Arrays.asList("砥")), true, true);
                        break;
                    case "amor":setSuggestions(new ArrayList<String>(Arrays.asList("愛", "愛人")), true, true);
                        break;
                    case "amortecer":setSuggestions(new ArrayList<String>(Arrays.asList("緩衝")), true, true);
                        break;
                    case "amostra":setSuggestions(new ArrayList<String>(Arrays.asList("例示")), true, true);
                        break;
                    case "an":setSuggestions(new ArrayList<String>(Arrays.asList("案")), true, true);
                        break;
                    case "and":setSuggestions(new ArrayList<String>(Arrays.asList("走")), true, true);
                        break;
                    case "anda":setSuggestions(new ArrayList<String>(Arrays.asList("走")), true, true);
                        break;
                    case "andar":setSuggestions(new ArrayList<String>(Arrays.asList("走")), true, true);
                        break;
                    case "anderteiker":setSuggestions(new ArrayList<String>(Arrays.asList("送葬者")), true, true);
                        break;
                    case "anderteiquer":setSuggestions(new ArrayList<String>(Arrays.asList("送葬者")), true, true);
                        break;
                    case "animacao":setSuggestions(new ArrayList<String>(Arrays.asList("動漫")), true, true);
                        break;
                    case "anime":setSuggestions(new ArrayList<String>(Arrays.asList("動漫")), true, true);
                        break;
                    case "anjo":setSuggestions(new ArrayList<String>(Arrays.asList("天使")), true, true);
                        break;
                    case "ano":setSuggestions(new ArrayList<String>(Arrays.asList("年")), true, true);
                        break;
                    case "anosatras":setSuggestions(new ArrayList<String>(Arrays.asList("昔")), true, true);
                        break;
                    case "antes":setSuggestions(new ArrayList<String>(Arrays.asList("前")), true, true);
                        break;
                    case "antiga":setSuggestions(new ArrayList<String>(Arrays.asList("古代")), true, true);
                        break;
                    case "antigo":setSuggestions(new ArrayList<String>(Arrays.asList("古代")), true, true);
                        break;
                    case "ap":setSuggestions(new ArrayList<String>(Arrays.asList("應用")), true, true);
                        break;
                    case "apagar":setSuggestions(new ArrayList<String>(Arrays.asList("刪除", "消")), true, true);
                        break;
                    case "aparatus":setSuggestions(new ArrayList<String>(Arrays.asList("装置")), true, true);
                        break;
                    case "apelido":setSuggestions(new ArrayList<String>(Arrays.asList("昵稱")), true, true);
                        break;
                    case "apenas":setSuggestions(new ArrayList<String>(Arrays.asList("只", "僅僅")), true, true);
                        break;
                    case "apesar":setSuggestions(new ArrayList<String>(Arrays.asList("虽然")), true, true);
                        break;
                    case "apital":setSuggestions(new ArrayList<String>(Arrays.asList("京")), true, true);
                        break;
                    case "aplicacao":setSuggestions(new ArrayList<String>(Arrays.asList("應用")), true, true);
                        break;
                    case "aplicacoes":setSuggestions(new ArrayList<String>(Arrays.asList("應用")), true, true);
                        break;
                    case "apost":setSuggestions(new ArrayList<String>(Arrays.asList("賭")), true, true);
                        break;
                    case "aprendiz":setSuggestions(new ArrayList<String>(Arrays.asList("弟子")), true, true);
                        break;
                    case "apressar":setSuggestions(new ArrayList<String>(Arrays.asList("突進")), true, true);
                        break;
                    case "aprovadopelogoverno":setSuggestions(new ArrayList<String>(Arrays.asList("官方")), true, true);
                        break;
                    case "aprovar":setSuggestions(new ArrayList<String>(Arrays.asList("默認")), true, true);
                        break;
                    case "aquela":setSuggestions(new ArrayList<String>(Arrays.asList("那個")), true, true);
                        break;
                    case "aquele":setSuggestions(new ArrayList<String>(Arrays.asList("那個")), true, true);
                        break;
                    case "aqui":setSuggestions(new ArrayList<String>(Arrays.asList("這裡")), true, true);
                        break;
                    case "ar":setSuggestions(new ArrayList<String>(Arrays.asList("空")), true, true);
                        break;
                    case "arder":setSuggestions(new ArrayList<String>(Arrays.asList("焼")), true, true);
                        break;
                    case "areia":setSuggestions(new ArrayList<String>(Arrays.asList("砂")), true, true);
                        break;
                    case "armarse":setSuggestions(new ArrayList<String>(Arrays.asList("逞强")), true, true);
                        break;
                    case "armazenamento":setSuggestions(new ArrayList<String>(Arrays.asList("存儲")), true, true);
                        break;
                    case "aromatico":setSuggestions(new ArrayList<String>(Arrays.asList("芳香")), true, true);
                        break;
                    case "arquiteto":setSuggestions(new ArrayList<String>(Arrays.asList("建築士")), true, true);
                        break;
                    case "arquitetura":setSuggestions(new ArrayList<String>(Arrays.asList("建築")), true, true);
                        break;
                    case "arrepender":setSuggestions(new ArrayList<String>(Arrays.asList("後悔")), true, true);
                        break;
                    case "beje":setSuggestions(new ArrayList<String>(Arrays.asList("米色")), true, true);
                        break;
                    case "art":setSuggestions(new ArrayList<String>(Arrays.asList("藝")), true, true);
                        break;
                    case "artesao":setSuggestions(new ArrayList<String>(Arrays.asList("職人")), true, true);
                        break;
                    case "artificial":setSuggestions(new ArrayList<String>(Arrays.asList("人造")), true, true);
                        break;
                    case "ascen":setSuggestions(new ArrayList<String>(Arrays.asList("上昇")), true, true);
                        break;
                    case "ass":setSuggestions(new ArrayList<String>(Arrays.asList("組", "烤")), true, true);
                        break;
                    case "assa":setSuggestions(new ArrayList<String>(Arrays.asList("烤")), true, true);
                        break;
                    case "assado":setSuggestions(new ArrayList<String>(Arrays.asList("烤了")), true, true);
                        break;
                    case "assassino":setSuggestions(new ArrayList<String>(Arrays.asList("刺客")), true, true);
                        break;
                    case "assassinos":setSuggestions(new ArrayList<String>(Arrays.asList("刺客")), true, true);
                        break;
                    case "assign":setSuggestions(new ArrayList<String>(Arrays.asList("📥")), true, true);
                        break;
                    case "assim":setSuggestions(new ArrayList<String>(Arrays.asList("那么", "这样")), true, true);
                        break;
                    case "assinalar":setSuggestions(new ArrayList<String>(Arrays.asList("📥")), true, true);
                        break;
                    case "assinatura":setSuggestions(new ArrayList<String>(Arrays.asList("獨門")), true, true);
                        break;
                    case "atalho":setSuggestions(new ArrayList<String>(Arrays.asList("近道")), true, true);
                        break;
                    case "até":setSuggestions(new ArrayList<String>(Arrays.asList("迄", "究")), true, true);
                        break;
                    case "atex":setSuggestions(new ArrayList<String>(Arrays.asList("究極")), true, true);
                        break;
                    case "atir":setSuggestions(new ArrayList<String>(Arrays.asList("投")), true, true);
                        break;
                    case "atirador":setSuggestions(new ArrayList<String>(Arrays.asList("投手")), true, true);
                        break;
                    case "atirar":setSuggestions(new ArrayList<String>(Arrays.asList("投")), true, true);
                        break;
                    case "atraido":setSuggestions(new ArrayList<String>(Arrays.asList("惹")), true, true);
                        break;
                    case "atributos":setSuggestions(new ArrayList<String>(Arrays.asList("屬性")), true, true);
                        break;
                    case "auge":setSuggestions(new ArrayList<String>(Arrays.asList("巔峰")), true, true);
                        break;
                    case "aujo":setSuggestions(new ArrayList<String>(Arrays.asList("巔峰")), true, true);
                        break;
                    case "autocarro":setSuggestions(new ArrayList<String>(Arrays.asList("公共汽車")), true, true);
                        break;
                    case "automatico":setSuggestions(new ArrayList<String>(Arrays.asList("自動")), true, true);
                        break;
                    case "avancar":setSuggestions(new ArrayList<String>(Arrays.asList("前進")), true, true);
                        break;
                    case "ave":setSuggestions(new ArrayList<String>(Arrays.asList("雀")), true, true);
                        break;
                    case "aventura":setSuggestions(new ArrayList<String>(Arrays.asList("冒險")), true, true);
                        break;
                    case "aviso":setSuggestions(new ArrayList<String>(Arrays.asList("注意", "提醒")), true, true);
                        break;
                    case "azul":setSuggestions(new ArrayList<String>(Arrays.asList("藍")), true, true);
                        break;
                    case "azulclaro":setSuggestions(new ArrayList<String>(Arrays.asList("青")), true, true);
                        break;
                    case "azulescuro":setSuggestions(new ArrayList<String>(Arrays.asList("蒼")), true, true);
                        break;
                    case "azulesverdeado":setSuggestions(new ArrayList<String>(Arrays.asList("碧")), true, true);
                        break;
                    case "b":setSuggestions(new ArrayList<String>(Arrays.asList("布")), true, true);
                        break;
                    case "bá":setSuggestions(new ArrayList<String>(Arrays.asList("巴", "吧")), true, true);
                        break;
                    case "backup":setSuggestions(new ArrayList<String>(Arrays.asList("备份")), true, true);
                        break;
                    case "bafer":setSuggestions(new ArrayList<String>(Arrays.asList("緩衝")), true, true);
                        break;
                    case "baile":setSuggestions(new ArrayList<String>(Arrays.asList("舞會")), true, true);
                        break;
                    case "baixar":setSuggestions(new ArrayList<String>(Arrays.asList("下載")), true, true);
                        break;
                    case "baixinho":setSuggestions(new ArrayList<String>(Arrays.asList("悄悄")), true, true);
                        break;
                    case "baixo":setSuggestions(new ArrayList<String>(Arrays.asList("悄")), true, true);
                        break;
                    case "baixotirador":setSuggestions(new ArrayList<String>(Arrays.asList("送葬者")), true, true);
                        break;
                    case "bala":setSuggestions(new ArrayList<String>(Arrays.asList("弾")), true, true);
                        break;
                    case "balanc":setSuggestions(new ArrayList<String>(Arrays.asList("振")), true, true);
                        break;
                    case "balanco":setSuggestions(new ArrayList<String>(Arrays.asList("餘額")), true, true);
                        break;
                    case "pendur":setSuggestions(new ArrayList<String>(Arrays.asList("吊")), true, true);
                        break;
                    case "bambu":setSuggestions(new ArrayList<String>(Arrays.asList("竹")), true, true);
                        break;
                    case "ban":setSuggestions(new ArrayList<String>(Arrays.asList("晚")), true, true);
                        break;
                    case "banana":setSuggestions(new ArrayList<String>(Arrays.asList("香蕉")), true, true);
                        break;
                    case "banbu":setSuggestions(new ArrayList<String>(Arrays.asList("竹")), true, true);
                        break;
                    case "banda":setSuggestions(new ArrayList<String>(Arrays.asList("樂隊")), true, true);
                        break;
                    case "bandasonora":setSuggestions(new ArrayList<String>(Arrays.asList("原聲音樂")), true, true);
                        break;
                    case "bang":setSuggestions(new ArrayList<String>(Arrays.asList("榜")), true, true);
                        break;
                    case "bardoque":setSuggestions(new ArrayList<String>(Arrays.asList("巴達克")), true, true);
                        break;
                    case "barreira":setSuggestions(new ArrayList<String>(Arrays.asList("障碍")), true, true);
                        break;
                    case "barriga":setSuggestions(new ArrayList<String>(Arrays.asList("腹")), true, true);
                        break;
                    case "basquetebol":setSuggestions(new ArrayList<String>(Arrays.asList("籃球")), true, true);
                        break;
                    case "batalha":setSuggestions(new ArrayList<String>(Arrays.asList("戰鬥")), true, true);
                        break;
                    case "bateria":setSuggestions(new ArrayList<String>(Arrays.asList("電池")), true, true);
                        break;
                    case "baterpunheta":setSuggestions(new ArrayList<String>(Arrays.asList("手淫")), true, true);
                        break;
                    case "bebe":setSuggestions(new ArrayList<String>(Arrays.asList("宝宝")), true, true);
                        break;
                    case "beisebol":setSuggestions(new ArrayList<String>(Arrays.asList("棒球", "野球")), true, true);
                        break;
                    case "beji-ta":setSuggestions(new ArrayList<String>(Arrays.asList("比達")), true, true);
                        break;
                    case "bem":setSuggestions(new ArrayList<String>(Arrays.asList("好", "很", "善")), true, true);
                        break;
                    case "bi":setSuggestions(new ArrayList<String>(Arrays.asList("比")), true, true);
                        break;
                    case "biblia":setSuggestions(new ArrayList<String>(Arrays.asList("聖經")), true, true);
                        break;
                    case "birus":setSuggestions(new ArrayList<String>(Arrays.asList("比魯斯")), true, true);
                        break;
                    case "biruss":setSuggestions(new ArrayList<String>(Arrays.asList("比魯斯")), true, true);
                        break;
                    case "birusu":setSuggestions(new ArrayList<String>(Arrays.asList("比魯斯")), true, true);
                        break;
                    case "bloquear":setSuggestions(new ArrayList<String>(Arrays.asList("塞")), true, true);
                        break;
                    case "bluetooth":setSuggestions(new ArrayList<String>(Arrays.asList("藍牙")), true, true);
                        break;
                    case "boa":setSuggestions(new ArrayList<String>(Arrays.asList("良", "優")), true, true);
                        break;
                    case "boca":setSuggestions(new ArrayList<String>(Arrays.asList("嘴")), true, true);
                        break;
                    case "bohana":setSuggestions(new ArrayList<String>(Arrays.asList("播放")), true, true);
                        break;
                    case "bola":setSuggestions(new ArrayList<String>(Arrays.asList("球")), true, true);
                        break;
                    case "bolafora":setSuggestions(new ArrayList<String>(Arrays.asList("邪球")), true, true);
                        break;
                    case "bolasfora":setSuggestions(new ArrayList<String>(Arrays.asList("邪球")), true, true);
                        break;
                    case "bom":setSuggestions(new ArrayList<String>(Arrays.asList("良")), true, true);
                        break;
                    case "bom-ponto":setSuggestions(new ArrayList<String>(Arrays.asList("正論")), true, true);
                        break;
                    case "bompresidente":setSuggestions(new ArrayList<String>(Arrays.asList("王道")), true, true);
                        break;
                    case "bonita":setSuggestions(new ArrayList<String>(Arrays.asList("綺麗", "漂亮")), true, true);
                        break;
                    case "bonito":setSuggestions(new ArrayList<String>(Arrays.asList("漂亮")), true, true);
                        break;
                    case "bora":setSuggestions(new ArrayList<String>(Arrays.asList("上去")), true, true);
                        break;
                    case "bosta":setSuggestions(new ArrayList<String>(Arrays.asList("糞")), true, true);
                        break;
                    case "bou":setSuggestions(new ArrayList<String>(Arrays.asList("某")), true, true);
                        break;
                    case "braco":setSuggestions(new ArrayList<String>(Arrays.asList("腕")), true, true);
                        break;
                    case "braiant":setSuggestions(new ArrayList<String>(Arrays.asList("布萊恩特")), true, true);
                        break;
                    case "Braiant":setSuggestions(new ArrayList<String>(Arrays.asList("B萊ant")), true, true);
                        break;
                    case "braien":setSuggestions(new ArrayList<String>(Arrays.asList("布萊恩")), true, true);
                        break;
                    case "braiente":setSuggestions(new ArrayList<String>(Arrays.asList("布萊恩特")), true, true);
                        break;
                    case "branca":setSuggestions(new ArrayList<String>(Arrays.asList("白")), true, true);
                        break;
                    case "branco":setSuggestions(new ArrayList<String>(Arrays.asList("白", "白人")), true, true);
                        break;
                    case "brinquedo":setSuggestions(new ArrayList<String>(Arrays.asList("玩具")), true, true);
                        break;
                    case "broli":setSuggestions(new ArrayList<String>(Arrays.asList("布羅利")), true, true);
                        break;
                    case "brutal":setSuggestions(new ArrayList<String>(Arrays.asList("真棒", "給力")), true, true);
                        break;
                    case "buffer":setSuggestions(new ArrayList<String>(Arrays.asList("緩衝")), true, true);
                        break;
                    case "burori":setSuggestions(new ArrayList<String>(Arrays.asList("布羅利")), true, true);
                        break;
                    case "burro":setSuggestions(new ArrayList<String>(Arrays.asList("驢", "笨")), true, true);
                        break;
                    case "buscar":setSuggestions(new ArrayList<String>(Arrays.asList("揃")), true, true);
                        break;
                    case "butão":setSuggestions(new ArrayList<String>(Arrays.asList("按鈕", "釦")), true, true);
                        break;
                    case "vs":setSuggestions(new ArrayList<String>(Arrays.asList("對")), true, true);
                        break;
                    case "c":setSuggestions(new ArrayList<String>(Arrays.asList("克")), true, true);
                        break;
                    case "ca":setSuggestions(new ArrayList<String>(Arrays.asList("卡")), true, true);
                        break;
                    case "cab":setSuggestions(new ArrayList<String>(Arrays.asList("合")), true, true);
                        break;
                    case "cabeca":setSuggestions(new ArrayList<String>(Arrays.asList("頭")), true, true);
                        break;
                    case "cabimento":setSuggestions(new ArrayList<String>(Arrays.asList("康健")), true, true);
                        break;
                    case "cabrao":setSuggestions(new ArrayList<String>(Arrays.asList("王八蛋")), true, true);
                        break;
                    case "caçar":setSuggestions(new ArrayList<String>(Arrays.asList("狩")), true, true);
                        break;
                    case "cache":setSuggestions(new ArrayList<String>(Arrays.asList("緩存")), true, true);
                        break;
                    case "cada":setSuggestions(new ArrayList<String>(Arrays.asList("各")), true, true);
                        break;
                    case "cadeira":setSuggestions(new ArrayList<String>(Arrays.asList("椅子")), true, true);
                        break;
                    case "cafe":setSuggestions(new ArrayList<String>(Arrays.asList("☕")), true, true);
                        break;
                    case "cal":setSuggestions(new ArrayList<String>(Arrays.asList("默")), true, true);
                        break;
                    case "calmo":setSuggestions(new ArrayList<String>(Arrays.asList("靜")), true, true);
                        break;
                    case "cama":setSuggestions(new ArrayList<String>(Arrays.asList("床")), true, true);
                        break;
                    case "caminhada":setSuggestions(new ArrayList<String>(Arrays.asList("散步")), true, true);
                        break;
                    case "caminho":setSuggestions(new ArrayList<String>(Arrays.asList("道")), true, true);
                        break;
                    case "campainha":setSuggestions(new ArrayList<String>(Arrays.asList("鐘")), true, true);
                        break;
                    case "campeao":setSuggestions(new ArrayList<String>(Arrays.asList("冠軍")), true, true);
                        break;
                    case "campeiro":setSuggestions(new ArrayList<String>(Arrays.asList("農夫")), true, true);
                        break;
                    case "campeonato":setSuggestions(new ArrayList<String>(Arrays.asList("冠軍的")), true, true);
                        break;
                    case "can":setSuggestions(new ArrayList<String>(Arrays.asList("坎")), true, true);
                        break;
                    case "canal":setSuggestions(new ArrayList<String>(Arrays.asList("頻道")), true, true);
                        break;
                    case "cancao":setSuggestions(new ArrayList<String>(Arrays.asList("歌")), true, true);
                        break;
                    case "caneca":setSuggestions(new ArrayList<String>(Arrays.asList("杯")), true, true);
                        break;
                    case "cano":setSuggestions(new ArrayList<String>(Arrays.asList("韓國")), true, true);
                        break;
                    case "canes":setSuggestions(new ArrayList<String>(Arrays.asList("韓国語")), true, true);
                        break;
                    case "cansada":setSuggestions(new ArrayList<String>(Arrays.asList("累了", "累")), true, true);
                        break;
                    case "cansado":setSuggestions(new ArrayList<String>(Arrays.asList("累")), true, true);
                        break;
                    case "cant":setSuggestions(new ArrayList<String>(Arrays.asList("歌")), true, true);
                        break;
                    case "cantar":setSuggestions(new ArrayList<String>(Arrays.asList("歌")), true, true);
                        break;
                    case "canto":setSuggestions(new ArrayList<String>(Arrays.asList("唱")), true, true);
                        break;
                    case "cantoneses":setSuggestions(new ArrayList<String>(Arrays.asList("廣東人")), true, true);
                        break;
                    case "cantor":setSuggestions(new ArrayList<String>(Arrays.asList("歌手")), true, true);
                        break;
                    case "cão":setSuggestions(new ArrayList<String>(Arrays.asList("狗")), true, true);
                        break;
                    case "cao":setSuggestions(new ArrayList<String>(Arrays.asList("曹")), true, true);
                        break;
                    case "capacidade":setSuggestions(new ArrayList<String>(Arrays.asList("設備")), true, true);
                        break;
                    case "capital":setSuggestions(new ArrayList<String>(Arrays.asList("京")), true, true);
                        break;
                    case "caps":setSuggestions(new ArrayList<String>(Arrays.asList("包")), true, true);
                        break;
                    case "car":setSuggestions(new ArrayList<String>(Arrays.asList("字")), true, true);
                        break;
                    case "cara":setSuggestions(new ArrayList<String>(Arrays.asList("顔")), true, true);
                        break;
                    case "caracheia":setSuggestions(new ArrayList<String>(Arrays.asList("满面")), true, true);
                        break;
                    case "caracter":setSuggestions(new ArrayList<String>(Arrays.asList("字符")), true, true);
                        break;
                    case "caractereshan":setSuggestions(new ArrayList<String>(Arrays.asList("漢字")), true, true);
                        break;
                    case "carafeliz":setSuggestions(new ArrayList<String>(Arrays.asList("笑顔")), true, true);
                        break;
                    case "caralho":setSuggestions(new ArrayList<String>(Arrays.asList("膣")), true, true);
                        break;
                    case "carbo":setSuggestions(new ArrayList<String>(Arrays.asList("炭")), true, true);
                        break;
                    case "caro":setSuggestions(new ArrayList<String>(Arrays.asList("貴")), true, true);
                        break;
                    case "carregando":setSuggestions(new ArrayList<String>(Arrays.asList("途中")), true, true);
                        break;
                    case "carreira":setSuggestions(new ArrayList<String>(Arrays.asList("生涯")), true, true);
                        break;
                    case "carro":setSuggestions(new ArrayList<String>(Arrays.asList("車")), true, true);
                        break;
                    case "casa":setSuggestions(new ArrayList<String>(Arrays.asList("家")), true, true);
                        break;
                    case "catsu":setSuggestions(new ArrayList<String>(Arrays.asList("活")), true, true);
                        break;
                    case "ce":setSuggestions(new ArrayList<String>(Arrays.asList("茲")), true, true);
                        break;
                    case "cele":setSuggestions(new ArrayList<String>(Arrays.asList("賽魯")), true, true);
                        break;
                    case "cem":setSuggestions(new ArrayList<String>(Arrays.asList("百")), true, true);
                        break;
                    case "cemque":setSuggestions(new ArrayList<String>(Arrays.asList("百科")), true, true);
                        break;
                    case "cena":setSuggestions(new ArrayList<String>(Arrays.asList("事", "景", "先拿")), true, true);
                        break;
                    case "centimo":setSuggestions(new ArrayList<String>(Arrays.asList("分錢", "分€")), true, true);
                        break;
                    case "central":setSuggestions(new ArrayList<String>(Arrays.asList("中")), true, true);
                        break;
                    case "centro":setSuggestions(new ArrayList<String>(Arrays.asList("中")), true, true);
                        break;
                    case "cerebro":setSuggestions(new ArrayList<String>(Arrays.asList("腦")), true, true);
                        break;
                    case "cereja":setSuggestions(new ArrayList<String>(Arrays.asList("櫻")), true, true);
                        break;
                    case "certo":setSuggestions(new ArrayList<String>(Arrays.asList("✅")), true, true);
                        break;
                    case "ceu":setSuggestions(new ArrayList<String>(Arrays.asList("天")), true, true);
                        break;
                    case "cha":setSuggestions(new ArrayList<String>(Arrays.asList("茶")), true, true);
                        break;
                    case "chamar":setSuggestions(new ArrayList<String>(Arrays.asList("呼", "📣")), true, true);
                        break;
                    case "chao":setSuggestions(new ArrayList<String>(Arrays.asList("土")), true, true);
                        break;
                    case "chapeu":setSuggestions(new ArrayList<String>(Arrays.asList("帽")), true, true);
                        break;
                    case "chat":setSuggestions(new ArrayList<String>(Arrays.asList("閑聊")), true, true);
                        break;
                    case "chato":setSuggestions(new ArrayList<String>(Arrays.asList("無聊")), true, true);
                        break;
                    case "chave":setSuggestions(new ArrayList<String>(Arrays.asList("鍵")), true, true);
                        break;
                    case "cheg":setSuggestions(new ArrayList<String>(Arrays.asList("到")), true, true);
                        break;
                    case "chegar":setSuggestions(new ArrayList<String>(Arrays.asList("到")), true, true);
                        break;
                    case "chenma":setSuggestions(new ArrayList<String>(Arrays.asList("什麼")), true, true);
                        break;
                    case "chi":setSuggestions(new ArrayList<String>(Arrays.asList("戲", "中")), true, true);
                        break;
                    case "chiao":setSuggestions(new ArrayList<String>(Arrays.asList("蕭")), true, true);
                        break;
                    case "chilr":setSuggestions(new ArrayList<String>(Arrays.asList("鳴")), true, true);
                        break;
                    case "chiis":setSuggestions(new ArrayList<String>(Arrays.asList("中國")), true, true);
                        break;
                    case "chines":setSuggestions(new ArrayList<String>(Arrays.asList("中國語")), true, true);
                        break;
                    case "choc":setSuggestions(new ArrayList<String>(Arrays.asList("震驚")), true, true);
                        break;
                    case "chocar":setSuggestions(new ArrayList<String>(Arrays.asList("震驚")), true, true);
                        break;
                    case "chora":setSuggestions(new ArrayList<String>(Arrays.asList("哭")), true, true);
                        break;
                    case "chorar":setSuggestions(new ArrayList<String>(Arrays.asList("哭")), true, true);
                        break;
                    case "chris":setSuggestions(new ArrayList<String>(Arrays.asList("克里斯")), true, true);
                        break;
                    case "chu":setSuggestions(new ArrayList<String>(Arrays.asList("修")), true, true);
                        break;
                    case "chupa":setSuggestions(new ArrayList<String>(Arrays.asList("吸")), true, true);
                        break;
                    case "church":setSuggestions(new ArrayList<String>(Arrays.asList("教会")), true, true);
                        break;
                    case "chuva":setSuggestions(new ArrayList<String>(Arrays.asList("雨")), true, true);
                        break;
                    case "cidade":setSuggestions(new ArrayList<String>(Arrays.asList("市")), true, true);
                        break;
                    case "cienc":setSuggestions(new ArrayList<String>(Arrays.asList("科")), true, true);
                        break;
                    case "cientifica":setSuggestions(new ArrayList<String>(Arrays.asList("科學的")), true, true);
                        break;
                    case "cigarra":setSuggestions(new ArrayList<String>(Arrays.asList("蟬")), true, true);
                        break;
                    case "cigarras":setSuggestions(new ArrayList<String>(Arrays.asList("蟬")), true, true);
                        break;
                    case "cima":setSuggestions(new ArrayList<String>(Arrays.asList("上")), true, true);
                        break;
                    case "cina":setSuggestions(new ArrayList<String>(Arrays.asList("先拿")), true, true);
                        break;
                    case "cintura":setSuggestions(new ArrayList<String>(Arrays.asList("腰")), true, true);
                        break;
                    case "circuitovirtual":setSuggestions(new ArrayList<String>(Arrays.asList("回線")), true, true);
                        break;
                    case "circunstancias":setSuggestions(new ArrayList<String>(Arrays.asList("状況")), true, true);
                        break;
                    case "classe":setSuggestions(new ArrayList<String>(Arrays.asList("類")), true, true);
                        break;
                    case "clicar":setSuggestions(new ArrayList<String>(Arrays.asList("選擇")), true, true);
                        break;
                    case "cobertura":setSuggestions(new ArrayList<String>(Arrays.asList("翻唱")), true, true);
                        break;
                    case "cobrir":setSuggestions(new ArrayList<String>(Arrays.asList("網羅")), true, true);
                        break;
                    case "cod":setSuggestions(new ArrayList<String>(Arrays.asList("碼")), true, true);
                        break;
                    case "coelho":setSuggestions(new ArrayList<String>(Arrays.asList("兔", "兎")), true, true);
                        break;
                    case "coentro":setSuggestions(new ArrayList<String>(Arrays.asList("香菜")), true, true);
                        break;
                    case "coisa":setSuggestions(new ArrayList<String>(Arrays.asList("物")), true, true);
                        break;
                    case "coiso":setSuggestions(new ArrayList<String>(Arrays.asList("個")), true, true);
                        break;
                    case "Coiso":setSuggestions(new ArrayList<String>(Arrays.asList("事")), true, true);
                        break;
                    case "colaps":setSuggestions(new ArrayList<String>(Arrays.asList("崩")), true, true);
                        break;
                    case "colar":setSuggestions(new ArrayList<String>(Arrays.asList("粘貼")), true, true);
                        break;
                    case "colega":setSuggestions(new ArrayList<String>(Arrays.asList("同學")), true, true);
                        break;
                    case "com":setSuggestions(new ArrayList<String>(Arrays.asList("含", "跟")), true, true);
                        break;
                    case "combater":setSuggestions(new ArrayList<String>(Arrays.asList("反对")), true, true);
                        break;
                    case "comboio":setSuggestions(new ArrayList<String>(Arrays.asList("列車")), true, true);
                        break;
                    case "comec":setSuggestions(new ArrayList<String>(Arrays.asList("始")), true, true);
                        break;
                    case "comedia":setSuggestions(new ArrayList<String>(Arrays.asList("喜劇")), true, true);
                        break;
                    case "comer":setSuggestions(new ArrayList<String>(Arrays.asList("食")), true, true);
                        break;
                    case "comercio":setSuggestions(new ArrayList<String>(Arrays.asList("貿")), true, true);
                        break;
                    case "comigo":setSuggestions(new ArrayList<String>(Arrays.asList("和我")), true, true);
                        break;
                    case "como":setSuggestions(new ArrayList<String>(Arrays.asList("怎", "怎麼")), true, true);
                        break;
                    case "porquê":setSuggestions(new ArrayList<String>(Arrays.asList("因為")), true, true);
                        break;
                    case "comose":setSuggestions(new ArrayList<String>(Arrays.asList("似乎")), true, true);
                        break;
                    case "compr":setSuggestions(new ArrayList<String>(Arrays.asList("買")), true, true);
                        break;
                    case "comp":setSuggestions(new ArrayList<String>(Arrays.asList("集")), true, true);
                        break;
                    case "complet":setSuggestions(new ArrayList<String>(Arrays.asList("成")), true, true);
                        break;
                    case "comprar":setSuggestions(new ArrayList<String>(Arrays.asList("買")), true, true);
                        break;
                    case "compree":setSuggestions(new ArrayList<String>(Arrays.asList("認識")), true, true);
                        break;
                    case "computador":setSuggestions(new ArrayList<String>(Arrays.asList("電腦")), true, true);
                        break;
                    case "comunidade":setSuggestions(new ArrayList<String>(Arrays.asList("社區")), true, true);
                        break;
                    case "contin":setSuggestions(new ArrayList<String>(Arrays.asList("进")), true, true);
                        break;
                    case "cont":setSuggestions(new ArrayList<String>(Arrays.asList("含")), true, true);
                        break;
                    case "cona":setSuggestions(new ArrayList<String>(Arrays.asList("屄")), true, true);
                        break;
                    case "conclusao":setSuggestions(new ArrayList<String>(Arrays.asList("結論")), true, true);
                        break;
                    case "conduzir":setSuggestions(new ArrayList<String>(Arrays.asList("運転")), true, true);
                        break;
                    case "conf":setSuggestions(new ArrayList<String>(Arrays.asList("亂")), true, true);
                        break;
                    case "confiar":setSuggestions(new ArrayList<String>(Arrays.asList("信任")), true, true);
                        break;
                    case "conflito":setSuggestions(new ArrayList<String>(Arrays.asList("抗爭")), true, true);
                        break;
                    case "confortar":setSuggestions(new ArrayList<String>(Arrays.asList("慰")), true, true);
                        break;
                    case "conforto":setSuggestions(new ArrayList<String>(Arrays.asList("安慰")), true, true);
                        break;
                    case "confus":setSuggestions(new ArrayList<String>(Arrays.asList("混亂")), true, true);
                        break;
                    case "congelamento":setSuggestions(new ArrayList<String>(Arrays.asList("凍結")), true, true);
                        break;
                    case "conhece":setSuggestions(new ArrayList<String>(Arrays.asList("会")), true, true);
                        break;
                    case "conhecimento":setSuggestions(new ArrayList<String>(Arrays.asList("認識")), true, true);
                        break;
                    case "consegu":setSuggestions(new ArrayList<String>(Arrays.asList("出來")), true, true);
                        break;
                    case "consegues":setSuggestions(new ArrayList<String>(Arrays.asList("挺住")), true, true);
                        break;
                    case "consequentemente":setSuggestions(new ArrayList<String>(Arrays.asList("因此")), true, true);
                        break;
                    case "constituicao":setSuggestions(new ArrayList<String>(Arrays.asList("憲法")), true, true);
                        break;
                    case "constr":setSuggestions(new ArrayList<String>(Arrays.asList("築")), true, true);
                        break;
                    case "conta":setSuggestions(new ArrayList<String>(Arrays.asList("賬户")), true, true);
                        break;
                    case "contar":setSuggestions(new ArrayList<String>(Arrays.asList("数")), true, true);
                        break;
                    case "contente":setSuggestions(new ArrayList<String>(Arrays.asList("喜")), true, true);
                        break;
                    case "contesto":setSuggestions(new ArrayList<String>(Arrays.asList("大賽")), true, true);
                        break;
                    case "continua":setSuggestions(new ArrayList<String>(Arrays.asList("続")), true, true);
                        break;
                    case "continuar":setSuggestions(new ArrayList<String>(Arrays.asList("保留")), true, true);
                        break;
                    case "contra":setSuggestions(new ArrayList<String>(Arrays.asList("對")), true, true);
                        break;
                    case "controlo":setSuggestions(new ArrayList<String>(Arrays.asList("控制")), true, true);
                        break;
                    case "convers":setSuggestions(new ArrayList<String>(Arrays.asList("談")), true, true);
                        break;
                    case "convite":setSuggestions(new ArrayList<String>(Arrays.asList("招待")), true, true);
                        break;
                    case "cool":setSuggestions(new ArrayList<String>(Arrays.asList("良")), true, true);
                        break;
                    case "copiar":setSuggestions(new ArrayList<String>(Arrays.asList("复制")), true, true);
                        break;
                    case "cor":setSuggestions(new ArrayList<String>(Arrays.asList("色")), true, true);
                        break;
                    case "coracao":setSuggestions(new ArrayList<String>(Arrays.asList("心")), true, true);
                        break;
                    case "cordel":setSuggestions(new ArrayList<String>(Arrays.asList("紐")), true, true);
                        break;
                    case "corpo":setSuggestions(new ArrayList<String>(Arrays.asList("體", "身体")), true, true);
                        break;
                    case "corrente":setSuggestions(new ArrayList<String>(Arrays.asList("當前")), true, true);
                        break;
                    case "correr":setSuggestions(new ArrayList<String>(Arrays.asList("跑")), true, true);
                        break;
                    case "corrigir":setSuggestions(new ArrayList<String>(Arrays.asList("正確")), true, true);
                        break;
                    case "costa":setSuggestions(new ArrayList<String>(Arrays.asList("岸", "背中")), true, true);
                        break;
                    case "costuma":setSuggestions(new ArrayList<String>(Arrays.asList("曾經")), true, true);
                        break;
                    case "cou":setSuggestions(new ArrayList<String>(Arrays.asList("科")), true, true);
                        break;
                    case "coub":setSuggestions(new ArrayList<String>(Arrays.asList("康健")), true, true);
                        break;
                    case "cover":setSuggestions(new ArrayList<String>(Arrays.asList("翻唱")), true, true);
                        break;
                    case "coz":setSuggestions(new ArrayList<String>(Arrays.asList("熟")), true, true);
                        break;
                    case "cozer":setSuggestions(new ArrayList<String>(Arrays.asList("熟")), true, true);
                        break;
                    case "cozinha":setSuggestions(new ArrayList<String>(Arrays.asList("料理")), true, true);
                        break;
                    case "credo":setSuggestions(new ArrayList<String>(Arrays.asList("信條", "教條")), true, true);
                        break;
                    case "crêdo":setSuggestions(new ArrayList<String>(Arrays.asList("信條", "教條")), true, true);
                        break;
                    case "cresci":setSuggestions(new ArrayList<String>(Arrays.asList("成長")), true, true);
                        break;
                    case "criar":setSuggestions(new ArrayList<String>(Arrays.asList("作成", "制")), true, true);
                        break;
                    case "cristo":setSuggestions(new ArrayList<String>(Arrays.asList("基督")), true, true);
                        break;
                    case "crocante":setSuggestions(new ArrayList<String>(Arrays.asList("脆")), true, true);
                        break;
                    case "cu":setSuggestions(new ArrayList<String>(Arrays.asList("克", "族")), true, true);
                        break;
                    case "cubo":setSuggestions(new ArrayList<String>(Arrays.asList("立方")), true, true);
                        break;
                    case "erro":setSuggestions(new ArrayList<String>(Arrays.asList("錯")), true, true);
                        break;
                    case "cumprir":setSuggestions(new ArrayList<String>(Arrays.asList("遵守")), true, true);
                        break;
                    case "cur":setSuggestions(new ArrayList<String>(Arrays.asList("治", "癒")), true, true);
                        break;
                    case "curar":setSuggestions(new ArrayList<String>(Arrays.asList("癒")), true, true);
                        break;
                    case "curiosidade":setSuggestions(new ArrayList<String>(Arrays.asList("好奇")), true, true);
                        break;
                    case "curso":setSuggestions(new ArrayList<String>(Arrays.asList("講座")), true, true);
                        break;
                    case "curv":setSuggestions(new ArrayList<String>(Arrays.asList("彎")), true, true);
                        break;
                    case "curva":setSuggestions(new ArrayList<String>(Arrays.asList("彎")), true, true);
                        break;
                    case "cute":setSuggestions(new ArrayList<String>(Arrays.asList("可愛")), true, true);
                        break;
                    case "d":setSuggestions(new ArrayList<String>(Arrays.asList("德")), true, true);
                        break;
                    case "da":setSuggestions(new ArrayList<String>(Arrays.asList("了")), true, true);
                        break;
                    case "dados":setSuggestions(new ArrayList<String>(Arrays.asList("信息")), true, true);
                        break;
                    case "dai":setSuggestions(new ArrayList<String>(Arrays.asList("呆")), true, true);
                        break;
                    case "dan":setSuggestions(new ArrayList<String>(Arrays.asList("单")), true, true);
                        break;
                    case "danmu":setSuggestions(new ArrayList<String>(Arrays.asList("彈幕")), true, true);
                        break;
                    case "dantes":setSuggestions(new ArrayList<String>(Arrays.asList("从前")), true, true);
                        break;
                    case "pegadoa":setSuggestions(new ArrayList<String>(Arrays.asList("与")), true, true);
                        break;
                    case "dar":setSuggestions(new ArrayList<String>(Arrays.asList("給")), true, true);
                        break;
                    case "data":setSuggestions(new ArrayList<String>(Arrays.asList("数据", "日期")), true, true);
                        break;
                    case "de":setSuggestions(new ArrayList<String>(Arrays.asList("德", "從")), true, true);
                        break;
                    case "deacordo":setSuggestions(new ArrayList<String>(Arrays.asList("依")), true, true);
                        break;
                    case "debaixotirador":setSuggestions(new ArrayList<String>(Arrays.asList("送葬者")), true, true);
                        break;
                    case "declaracaodeguerra":setSuggestions(new ArrayList<String>(Arrays.asList("宣戦")), true, true);
                        break;
                    case "defesa":setSuggestions(new ArrayList<String>(Arrays.asList("守備")), true, true);
                        break;
                    case "deit":setSuggestions(new ArrayList<String>(Arrays.asList("伏")), true, true);
                        break;
                    case "deita":setSuggestions(new ArrayList<String>(Arrays.asList("伏")), true, true);
                        break;
                    case "deitar":setSuggestions(new ArrayList<String>(Arrays.asList("伏")), true, true);
                        break;
                    case "deixar":setSuggestions(new ArrayList<String>(Arrays.asList("讓", "交給")), true, true);
                        break;
                    case "dela":setSuggestions(new ArrayList<String>(Arrays.asList("她的")), true, true);
                        break;
                    case "dele":setSuggestions(new ArrayList<String>(Arrays.asList("他的")), true, true);
                        break;
                    case "delicioso":setSuggestions(new ArrayList<String>(Arrays.asList("好吃")), true, true);
                        break;
                    case "demasiado":setSuggestions(new ArrayList<String>(Arrays.asList("太")), true, true);
                        break;
                    case "dene":setSuggestions(new ArrayList<String>(Arrays.asList("然後")), true, true);
                        break;
                    case "denovo":setSuggestions(new ArrayList<String>(Arrays.asList("重新")), true, true);
                        break;
                    case "dentro":setSuggestions(new ArrayList<String>(Arrays.asList("裡")), true, true);
                        break;
                    case "dependente":setSuggestions(new ArrayList<String>(Arrays.asList("依存")), true, true);
                        break;
                    case "juntos":setSuggestions(new ArrayList<String>(Arrays.asList("👫")), true, true);
                        break;
                    case "deposit":setSuggestions(new ArrayList<String>(Arrays.asList("預")), true, true);
                        break;
                    case "depressao":setSuggestions(new ArrayList<String>(Arrays.asList("抑鬱")), true, true);
                        break;
                    case "dequalquermaneira":setSuggestions(new ArrayList<String>(Arrays.asList("無論")), true, true);
                        break;
                    case "derreter":setSuggestions(new ArrayList<String>(Arrays.asList("溶")), true, true);
                        break;
                    case "derrota":setSuggestions(new ArrayList<String>(Arrays.asList("敗")), true, true);
                        break;
                    case "desafio":setSuggestions(new ArrayList<String>(Arrays.asList("挑戰")), true, true);
                        break;
                    case "deitfora":setSuggestions(new ArrayList<String>(Arrays.asList("棄")), true, true);
                        break;
                    case "descasc":setSuggestions(new ArrayList<String>(Arrays.asList("剥")), true, true);
                        break;
                    case "descascar":setSuggestions(new ArrayList<String>(Arrays.asList("剥")), true, true);
                        break;
                    case "descasqu":setSuggestions(new ArrayList<String>(Arrays.asList("剥")), true, true);
                        break;
                    case "descasquei":setSuggestions(new ArrayList<String>(Arrays.asList("剥了")), true, true);
                        break;
                    case "desculpa":setSuggestions(new ArrayList<String>(Arrays.asList("抱歉")), true, true);
                        break;
                    case "desde":setSuggestions(new ArrayList<String>(Arrays.asList("以上")), true, true);
                        break;
                    case "desej":setSuggestions(new ArrayList<String>(Arrays.asList("欲")), true, true);
                        break;
                    case "desejo":setSuggestions(new ArrayList<String>(Arrays.asList("願望")), true, true);
                        break;
                    case "desenho":setSuggestions(new ArrayList<String>(Arrays.asList("絵")), true, true);
                        break;
                    case "desenvolvedor":setSuggestions(new ArrayList<String>(Arrays.asList("開發者")), true, true);
                        break;
                    case "desenvolver":setSuggestions(new ArrayList<String>(Arrays.asList("開發")), true, true);
                        break;
                    case "desenvolvimento":setSuggestions(new ArrayList<String>(Arrays.asList("発展")), true, true);
                        break;
                    case "desflorestacao":setSuggestions(new ArrayList<String>(Arrays.asList("乱伐")), true, true);
                        break;
                    case "desistir":setSuggestions(new ArrayList<String>(Arrays.asList("諦")), true, true);
                        break;
                    case "deslumbrante":setSuggestions(new ArrayList<String>(Arrays.asList("眩")), true, true);
                        break;
                    case "desperdicar":setSuggestions(new ArrayList<String>(Arrays.asList("粗末")), true, true);
                        break;
                    case "desperdicardinheiro":setSuggestions(new ArrayList<String>(Arrays.asList("無駄遣")), true, true);
                        break;
                    case "desporto":setSuggestions(new ArrayList<String>(Arrays.asList("運動")), true, true);
                        break;
                    case "desprevenido":setSuggestions(new ArrayList<String>(Arrays.asList("油断")), true, true);
                        break;
                    case "destes":setSuggestions(new ArrayList<String>(Arrays.asList("这些")), true, true);
                        break;
                    case "destruicao":setSuggestions(new ArrayList<String>(Arrays.asList("破壊")), true, true);
                        break;
                    case "determinismo":setSuggestions(new ArrayList<String>(Arrays.asList("決定論")), true, true);
                        break;
                    case "deus":setSuggestions(new ArrayList<String>(Arrays.asList("神")), true, true);
                        break;
                    case "deusa":setSuggestions(new ArrayList<String>(Arrays.asList("神")), true, true);
                        break;
                    case "devagar":setSuggestions(new ArrayList<String>(Arrays.asList("慢慢")), true, true);
                        break;
                    case "devagarinho":setSuggestions(new ArrayList<String>(Arrays.asList("慢慢地")), true, true);
                        break;
                    case "devastacao":setSuggestions(new ArrayList<String>(Arrays.asList("駆逐")), true, true);
                        break;
                    case "dever":setSuggestions(new ArrayList<String>(Arrays.asList("當")), true, true);
                        break;
                    case "di":setSuggestions(new ArrayList<String>(Arrays.asList("蒂", "低")), true, true);
                        break;
                    case "dia":setSuggestions(new ArrayList<String>(Arrays.asList("日")), true, true);
                        break;
                    case "diabo":setSuggestions(new ArrayList<String>(Arrays.asList("魔鬼")), true, true);
                        break;
                    case "diante":setSuggestions(new ArrayList<String>(Arrays.asList("起")), true, true);
                        break;
                    case "diariamente":setSuggestions(new ArrayList<String>(Arrays.asList("今日的")), true, true);
                        break;
                    case "diario":setSuggestions(new ArrayList<String>(Arrays.asList("日記")), true, true);
                        break;
                    case "dici":setSuggestions(new ArrayList<String>(Arrays.asList("辞典")), true, true);
                        break;
                    case "dicionario":setSuggestions(new ArrayList<String>(Arrays.asList("辞典")), true, true);
                        break;
                    case "digam":setSuggestions(new ArrayList<String>(Arrays.asList("言達吧")), true, true);
                        break;
                    case "dinheiro":setSuggestions(new ArrayList<String>(Arrays.asList("錢")), true, true);
                        break;
                    case "dir":setSuggestions(new ArrayList<String>(Arrays.asList("方", "電台")), true, true);
                        break;
                    case "transmissao":setSuggestions(new ArrayList<String>(Arrays.asList("轉播")), true, true);
                        break;
                    case "direito":setSuggestions(new ArrayList<String>(Arrays.asList("権")), true, true);
                        break;
                    case "direitoshumanos":setSuggestions(new ArrayList<String>(Arrays.asList("人権")), true, true);
                        break;
                    case "direto":setSuggestions(new ArrayList<String>(Arrays.asList("轉播")), true, true);
                        break;
                    case "dispersar":setSuggestions(new ArrayList<String>(Arrays.asList("散")), true, true);
                        break;
                    case "distancia":setSuggestions(new ArrayList<String>(Arrays.asList("距離")), true, true);
                        break;
                    case "disu":setSuggestions(new ArrayList<String>(Arrays.asList("低俗")), true, true);
                        break;
                    case "ditadura":setSuggestions(new ArrayList<String>(Arrays.asList("独裁主義")), true, true);
                        break;
                    case "divert":setSuggestions(new ArrayList<String>(Arrays.asList("楽")), true, true);
                        break;
                    case "diz":setSuggestions(new ArrayList<String>(Arrays.asList("言吧", "言")), true, true);
                        break;
                    case "dizer":setSuggestions(new ArrayList<String>(Arrays.asList("言")), true, true);
                        break;
                    case "dizes":setSuggestions(new ArrayList<String>(Arrays.asList("言您")), true, true);
                        break;
                    case "dns":setSuggestions(new ArrayList<String>(Arrays.asList("域名")), true, true);
                        break;
                    case "do":setSuggestions(new ArrayList<String>(Arrays.asList("了", "成")), true, true);
                        break;
                    case "dobra":setSuggestions(new ArrayList<String>(Arrays.asList("配音")), true, true);
                        break;
                    case "dobrado":setSuggestions(new ArrayList<String>(Arrays.asList("配音了")), true, true);
                        break;
                    case "dobragem":setSuggestions(new ArrayList<String>(Arrays.asList("配音")), true, true);
                        break;
                    case "dobrar":setSuggestions(new ArrayList<String>(Arrays.asList("配音")), true, true);
                        break;
                    case "doce":setSuggestions(new ArrayList<String>(Arrays.asList("甜")), true, true);
                        break;
                    case "doenca":setSuggestions(new ArrayList<String>(Arrays.asList("病")), true, true);
                        break;
                    case "doer":setSuggestions(new ArrayList<String>(Arrays.asList("痛感")), true, true);
                        break;
                    case "doido":setSuggestions(new ArrayList<String>(Arrays.asList("狂")), true, true);
                        break;
                    case "dominar":setSuggestions(new ArrayList<String>(Arrays.asList("支配")), true, true);
                        break;
                    case "dorm":setSuggestions(new ArrayList<String>(Arrays.asList("眠")), true, true);
                        break;
                    case "dormir":setSuggestions(new ArrayList<String>(Arrays.asList("眠")), true, true);
                        break;
                    case "dqm":setSuggestions(new ArrayList<String>(Arrays.asList("無論")), true, true);
                        break;
                    case "dragao":setSuggestions(new ArrayList<String>(Arrays.asList("龍")), true, true);
                        break;
                    case "drama":setSuggestions(new ArrayList<String>(Arrays.asList("劇集")), true, true);
                        break;
                    case "droga":setSuggestions(new ArrayList<String>(Arrays.asList("藥")), true, true);
                        break;
                    case "dun":setSuggestions(new ArrayList<String>(Arrays.asList("蹲", "盾")), true, true);
                        break;
                    case "dupla":setSuggestions(new ArrayList<String>(Arrays.asList("兩")), true, true);
                        break;
                    case "duvida":setSuggestions(new ArrayList<String>(Arrays.asList("疑")), true, true);
                        break;
                    case "dwaine":setSuggestions(new ArrayList<String>(Arrays.asList("德懷恩")), true, true);
                        break;
                    case "é":setSuggestions(new ArrayList<String>(Arrays.asList("是")), true, true);
                        break;
                    case "e":setSuggestions(new ArrayList<String>(Arrays.asList("和")), true, true);
                        break;
                    case "ecrã":setSuggestions(new ArrayList<String>(Arrays.asList("屏", "屏幕")), true, true);
                        break;
                    case "edepois":setSuggestions(new ArrayList<String>(Arrays.asList("然後")), true, true);
                        break;
                    case "edo":setSuggestions(new ArrayList<String>(Arrays.asList("具")), true, true);
                        break;
                    case "efeito":setSuggestions(new ArrayList<String>(Arrays.asList("效果")), true, true);
                        break;
                    case "efeitosom":setSuggestions(new ArrayList<String>(Arrays.asList("音效")), true, true);
                        break;
                    case "fim":setSuggestions(new ArrayList<String>(Arrays.asList("了")), true, true);
                        break;
                    case "ela":setSuggestions(new ArrayList<String>(Arrays.asList("她")), true, true);
                        break;
                    case "elas":setSuggestions(new ArrayList<String>(Arrays.asList("她們")), true, true);
                        break;
                    case "ele":setSuggestions(new ArrayList<String>(Arrays.asList("他")), true, true);
                        break;
                    case "electro":setSuggestions(new ArrayList<String>(Arrays.asList("電")), true, true);
                        break;
                    case "eles":setSuggestions(new ArrayList<String>(Arrays.asList("他們")), true, true);
                        break;
                    case "eletrico":setSuggestions(new ArrayList<String>(Arrays.asList("電")), true, true);
                        break;
                    case "eletro":setSuggestions(new ArrayList<String>(Arrays.asList("電")), true, true);
                        break;
                    case "em":setSuggestions(new ArrayList<String>(Arrays.asList("於")), true, true);
                        break;
                    case "emagrecer":setSuggestions(new ArrayList<String>(Arrays.asList("痩")), true, true);
                        break;
                    case "email":setSuggestions(new ArrayList<String>(Arrays.asList("電郵")), true, true);
                        break;
                    case "embora":setSuggestions(new ArrayList<String>(Arrays.asList("虽然")), true, true);
                        break;
                    case "embrulh":setSuggestions(new ArrayList<String>(Arrays.asList("裝")), true, true);
                        break;
                    case "embrulha":setSuggestions(new ArrayList<String>(Arrays.asList("裝")), true, true);
                        break;
                    case "emdir":setSuggestions(new ArrayList<String>(Arrays.asList("轉播")), true, true);
                        break;
                    case "emdirecto":setSuggestions(new ArrayList<String>(Arrays.asList("轉播")), true, true);
                        break;
                    case "emp":setSuggestions(new ArrayList<String>(Arrays.asList("推")), true, true);
                        break;
                    case "empate":setSuggestions(new ArrayList<String>(Arrays.asList("平局")), true, true);
                        break;
                    case "emprest":setSuggestions(new ArrayList<String>(Arrays.asList("貸")), true, true);
                        break;
                    case "emprestar":setSuggestions(new ArrayList<String>(Arrays.asList("貸")), true, true);
                        break;
                    case "emprincipio":setSuggestions(new ArrayList<String>(Arrays.asList("一律")), true, true);
                        break;
                    case "emprogresso":setSuggestions(new ArrayList<String>(Arrays.asList("途中")), true, true);
                        break;
                    case "empurrar":setSuggestions(new ArrayList<String>(Arrays.asList("推", "邁進")), true, true);
                        break;
                    case "en":setSuggestions(new ArrayList<String>(Arrays.asList("恩")), true, true);
                        break;
                    case "enc":setSuggestions(new ArrayList<String>(Arrays.asList("終曲")), true, true);
                        break;
                    case "encerramento":setSuggestions(new ArrayList<String>(Arrays.asList("閉鎖")), true, true);
                        break;
                    case "encontrar":setSuggestions(new ArrayList<String>(Arrays.asList("找")), true, true);
                        break;
                    case "endo":setSuggestions(new ArrayList<String>(Arrays.asList("在")), true, true);
                        break;
                    case "endura":setSuggestions(new ArrayList<String>(Arrays.asList("忍")), true, true);
                        break;
                    case "energia":setSuggestions(new ArrayList<String>(Arrays.asList("能")), true, true);
                        break;
                    case "enfrentar":setSuggestions(new ArrayList<String>(Arrays.asList("面对")), true, true);
                        break;
                    case "enredo":setSuggestions(new ArrayList<String>(Arrays.asList("脚")), true, true);
                        break;
                    case "ensino":setSuggestions(new ArrayList<String>(Arrays.asList("教")), true, true);
                        break;
                    case "entao":setSuggestions(new ArrayList<String>(Arrays.asList("所以")), true, true);
                        break;
                    case "entend":setSuggestions(new ArrayList<String>(Arrays.asList("理解")), true, true);
                        break;
                    case "enterr":setSuggestions(new ArrayList<String>(Arrays.asList("葬")), true, true);
                        break;
                    case "enticar":setSuggestions(new ArrayList<String>(Arrays.asList("誘")), true, true);
                        break;
                    case "entr":setSuggestions(new ArrayList<String>(Arrays.asList("入")), true, true);
                        break;
                    case "entra":setSuggestions(new ArrayList<String>(Arrays.asList("入")), true, true);
                        break;
                    case "entrar":setSuggestions(new ArrayList<String>(Arrays.asList("入")), true, true);
                        break;
                    case "entregar":setSuggestions(new ArrayList<String>(Arrays.asList("送")), true, true);
                        break;
                    case "entretenimento":setSuggestions(new ArrayList<String>(Arrays.asList("娛樂")), true, true);
                        break;
                    case "equipa":setSuggestions(new ArrayList<String>(Arrays.asList("隊")), true, true);
                        break;
                    case "era":setSuggestions(new ArrayList<String>(Arrays.asList("存了")), true, true);
                        break;
                    case "err":setSuggestions(new ArrayList<String>(Arrays.asList("誤")), true, true);
                        break;
                    case "erri":setSuggestions(new ArrayList<String>(Arrays.asList("誤解")), true, true);
                        break;
                    case "erva":setSuggestions(new ArrayList<String>(Arrays.asList("大麻", "草", "麻")), true, true);
                        break;
                    case "és":setSuggestions(new ArrayList<String>(Arrays.asList("是")), true, true);
                        break;
                    case "ês":setSuggestions(new ArrayList<String>(Arrays.asList("語")), true, true);
                        break;
                    case "escola":setSuggestions(new ArrayList<String>(Arrays.asList("学校")), true, true);
                        break;
                    case "escov":setSuggestions(new ArrayList<String>(Arrays.asList("刷")), true, true);
                        break;
                    case "escovar":setSuggestions(new ArrayList<String>(Arrays.asList("刷")), true, true);
                        break;
                    case "escrever":setSuggestions(new ArrayList<String>(Arrays.asList("寫")), true, true);
                        break;
                    case "escrito":setSuggestions(new ArrayList<String>(Arrays.asList("書")), true, true);
                        break;
                    case "escritoamao":setSuggestions(new ArrayList<String>(Arrays.asList("手寫")), true, true);
                        break;
                    case "escudo":setSuggestions(new ArrayList<String>(Arrays.asList("盾")), true, true);
                        break;
                    case "escuro":setSuggestions(new ArrayList<String>(Arrays.asList("闇")), true, true);
                        break;
                    case "eses":setSuggestions(new ArrayList<String>(Arrays.asList("人")), true, true);
                        break;
                    case "esmagar":setSuggestions(new ArrayList<String>(Arrays.asList("潰")), true, true);
                        break;
                    case "espanhol":setSuggestions(new ArrayList<String>(Arrays.asList("西語")), true, true);
                        break;
                    case "espantado":setSuggestions(new ArrayList<String>(Arrays.asList("惑")), true, true);
                        break;
                    case "especialista":setSuggestions(new ArrayList<String>(Arrays.asList("名人")), true, true);
                        break;
                    case "Especifica":setSuggestions(new ArrayList<String>(Arrays.asList("特定")), true, true);
                        break;
                    case "especifico":setSuggestions(new ArrayList<String>(Arrays.asList("特定")), true, true);
                        break;
                    case "espelho":setSuggestions(new ArrayList<String>(Arrays.asList("鏡")), true, true);
                        break;
                    case "esperanca":setSuggestions(new ArrayList<String>(Arrays.asList("希望")), true, true);
                        break;
                    case "esperar":setSuggestions(new ArrayList<String>(Arrays.asList("等待")), true, true);
                        break;
                    case "espero":setSuggestions(new ArrayList<String>(Arrays.asList("希望")), true, true);
                        break;
                    case "espi":setSuggestions(new ArrayList<String>(Arrays.asList("覗")), true, true);
                        break;
                    case "espiar":setSuggestions(new ArrayList<String>(Arrays.asList("覗")), true, true);
                        break;
                    case "espirito":setSuggestions(new ArrayList<String>(Arrays.asList("靈")), true, true);
                        break;
                    case "esporra":setSuggestions(new ArrayList<String>(Arrays.asList("打飛機")), true, true);
                        break;
                    case "esporrar":setSuggestions(new ArrayList<String>(Arrays.asList("絶頂")), true, true);
                        break;
                    case "esposa":setSuggestions(new ArrayList<String>(Arrays.asList("妻")), true, true);
                        break;
                    case "esquecer":setSuggestions(new ArrayList<String>(Arrays.asList("忘")), true, true);
                        break;
                    case "essa":setSuggestions(new ArrayList<String>(Arrays.asList("那")), true, true);
                        break;
                    case "esse":setSuggestions(new ArrayList<String>(Arrays.asList("那")), true, true);
                        break;
                    case "esta":setSuggestions(new ArrayList<String>(Arrays.asList("這")), true, true);
                        break;
                    case "estacao":setSuggestions(new ArrayList<String>(Arrays.asList("季")), true, true);
                        break;
                    case "est":setSuggestions(new ArrayList<String>(Arrays.asList("在")), true, true);
                        break;
                    case "estatisticas":setSuggestions(new ArrayList<String>(Arrays.asList("統計")), true, true);
                        break;
                    case "este":setSuggestions(new ArrayList<String>(Arrays.asList("這部")), true, true);
                        break;
                    case "oeste":setSuggestions(new ArrayList<String>(Arrays.asList("西")), true, true);
                        break;
                    case "estemomento":setSuggestions(new ArrayList<String>(Arrays.asList("此時")), true, true);
                        break;
                    case "estetica":setSuggestions(new ArrayList<String>(Arrays.asList("美學")), true, true);
                        break;
                    case "estrangeiro":setSuggestions(new ArrayList<String>(Arrays.asList("外国")), true, true);
                        break;
                    case "estranho":setSuggestions(new ArrayList<String>(Arrays.asList("怪")), true, true);
                        break;
                    case "estrututa":setSuggestions(new ArrayList<String>(Arrays.asList("構造")), true, true);
                        break;
                    case "estupida":setSuggestions(new ArrayList<String>(Arrays.asList("胸大無腦")), true, true);
                        break;
                    case "estupido":setSuggestions(new ArrayList<String>(Arrays.asList("笨", "愚", "愚蠢")), true, true);
                        break;
                    case "etern":setSuggestions(new ArrayList<String>(Arrays.asList("永")), true, true);
                        break;
                    case "etnia":setSuggestions(new ArrayList<String>(Arrays.asList("民族")), true, true);
                        break;
                    case "eu":setSuggestions(new ArrayList<String>(Arrays.asList("我")), true, true);
                        break;
                    case "eua":setSuggestions(new ArrayList<String>(Arrays.asList("合衆國")), true, true);
                        break;
                    case "euro":setSuggestions(new ArrayList<String>(Arrays.asList("€")), true, true);
                        break;
                    case "europa":setSuggestions(new ArrayList<String>(Arrays.asList("歐洲")), true, true);
                        break;
                    case "evacuacao":setSuggestions(new ArrayList<String>(Arrays.asList("撤退")), true, true);
                        break;
                    case "ex":setSuggestions(new ArrayList<String>(Arrays.asList("元")), true, true);
                        break;
                    case "examinacao":setSuggestions(new ArrayList<String>(Arrays.asList("診断")), true, true);
                        break;
                    case "exatamente":setSuggestions(new ArrayList<String>(Arrays.asList("正確")), true, true);
                        break;
                    case "exatamenteomesmo":setSuggestions(new ArrayList<String>(Arrays.asList("一模一樣")), true, true);
                        break;
                    case "excelente":setSuggestions(new ArrayList<String>(Arrays.asList("憂")), true, true);
                        break;
                    case "excepto":setSuggestions(new ArrayList<String>(Arrays.asList("除")), true, true);
                        break;
                    case "excitada":setSuggestions(new ArrayList<String>(Arrays.asList("兴奋女")), true, true);
                        break;
                    case "excitado":setSuggestions(new ArrayList<String>(Arrays.asList("兴奋")), true, true);
                        break;
                    case "exist":setSuggestions(new ArrayList<String>(Arrays.asList("存")), true, true);
                        break;
                    case "existencia":setSuggestions(new ArrayList<String>(Arrays.asList("存在")), true, true);
                        break;
                    case "experiencia":setSuggestions(new ArrayList<String>(Arrays.asList("實驗", "試驗")), true, true);
                        break;
                    case "experimental":setSuggestions(new ArrayList<String>(Arrays.asList("實驗")), true, true);
                        break;
                    case "explode":setSuggestions(new ArrayList<String>(Arrays.asList("爆発")), true, true);
                        break;
                    case "explosao":setSuggestions(new ArrayList<String>(Arrays.asList("炸裂")), true, true);
                        break;
                    case "export":setSuggestions(new ArrayList<String>(Arrays.asList("匯出")), true, true);
                        break;
                    case "extend":setSuggestions(new ArrayList<String>(Arrays.asList("張")), true, true);
                        break;
                    case "extraodinario":setSuggestions(new ArrayList<String>(Arrays.asList("非常")), true, true);
                        break;
                    case "extre":setSuggestions(new ArrayList<String>(Arrays.asList("極")), true, true);
                        break;
                    case "extreminio":setSuggestions(new ArrayList<String>(Arrays.asList("虐殺")), true, true);
                        break;
                    case "extremo":setSuggestions(new ArrayList<String>(Arrays.asList("極")), true, true);
                        break;
                    case "fa":setSuggestions(new ArrayList<String>(Arrays.asList("迷")), true, true);
                        break;
                    case "fal":setSuggestions(new ArrayList<String>(Arrays.asList("話")), true, true);
                        break;
                    case "fala":setSuggestions(new ArrayList<String>(Arrays.asList("話吧")), true, true);
                        break;
                    case "falar":setSuggestions(new ArrayList<String>(Arrays.asList("話")), true, true);
                        break;
                    case "falcao":setSuggestions(new ArrayList<String>(Arrays.asList("鷹")), true, true);
                        break;
                    case "falso":setSuggestions(new ArrayList<String>(Arrays.asList("偽")), true, true);
                        break;
                    case "fan":setSuggestions(new ArrayList<String>(Arrays.asList("芳")), true, true);
                        break;
                    case "fantasma":setSuggestions(new ArrayList<String>(Arrays.asList("鬼")), true, true);
                        break;
                    case "fascinar":setSuggestions(new ArrayList<String>(Arrays.asList("魅")), true, true);
                        break;
                    case "favela":setSuggestions(new ArrayList<String>(Arrays.asList("貧民窟")), true, true);
                        break;
                    case "favor":setSuggestions(new ArrayList<String>(Arrays.asList("願")), true, true);
                        break;
                    case "favoravel":setSuggestions(new ArrayList<String>(Arrays.asList("利")), true, true);
                        break;
                    case "faz":setSuggestions(new ArrayList<String>(Arrays.asList("做", "作", "制")), true, true);
                        break;
                    case "fazer":setSuggestions(new ArrayList<String>(Arrays.asList("做", "制", "作")), true, true);
                        break;
                    case "fazes":setSuggestions(new ArrayList<String>(Arrays.asList("做您")), true, true);
                        break;
                    case "febre":setSuggestions(new ArrayList<String>(Arrays.asList("病")), true, true);
                        break;
                    case "fech":setSuggestions(new ArrayList<String>(Arrays.asList("閉")), true, true);
                        break;
                    case "feder":setSuggestions(new ArrayList<String>(Arrays.asList("臭")), true, true);
                        break;
                    case "fedido":setSuggestions(new ArrayList<String>(Arrays.asList("臭了")), true, true);
                        break;
                    case "fei":setSuggestions(new ArrayList<String>(Arrays.asList("廢")), true, true);
                        break;
                    case "felicidade":setSuggestions(new ArrayList<String>(Arrays.asList("幸福")), true, true);
                        break;
                    case "fortuna":setSuggestions(new ArrayList<String>(Arrays.asList("幸")), true, true);
                        break;
                    case "feroz":setSuggestions(new ArrayList<String>(Arrays.asList("猛")), true, true);
                        break;
                    case "ferro":setSuggestions(new ArrayList<String>(Arrays.asList("鉄")), true, true);
                        break;
                    case "ficar":setSuggestions(new ArrayList<String>(Arrays.asList("居")), true, true);
                        break;
                    case "ficheiro":setSuggestions(new ArrayList<String>(Arrays.asList("文件")), true, true);
                        break;
                    case "filme":setSuggestions(new ArrayList<String>(Arrays.asList("電影")), true, true);
                        break;
                    case "fimdalinha":setSuggestions(new ArrayList<String>(Arrays.asList("絶体絶命", "窮途末路")), true, true);
                        break;
                    case "finais":setSuggestions(new ArrayList<String>(Arrays.asList("总决賽")), true, true);
                        break;
                    case "final":setSuggestions(new ArrayList<String>(Arrays.asList("決賽")), true, true);
                        break;
                    case "fisiculturismo":setSuggestions(new ArrayList<String>(Arrays.asList("健美")), true, true);
                        break;
                    case "fitness":setSuggestions(new ArrayList<String>(Arrays.asList("康健")), true, true);
                        break;
                    case "fito":setSuggestions(new ArrayList<String>(Arrays.asList("康健")), true, true);
                        break;
                    case "fix":setSuggestions(new ArrayList<String>(Arrays.asList("治")), true, true);
                        break;
                    case "fiz":setSuggestions(new ArrayList<String>(Arrays.asList("作了")), true, true);
                        break;
                    case "flache":setSuggestions(new ArrayList<String>(Arrays.asList("閃")), true, true);
                        break;
                    case "fluencia":setSuggestions(new ArrayList<String>(Arrays.asList("流量")), true, true);
                        break;
                    case "fluorescente":setSuggestions(new ArrayList<String>(Arrays.asList("蛍光")), true, true);
                        break;
                    case "flutuar":setSuggestions(new ArrayList<String>(Arrays.asList("浮")), true, true);
                        break;
                    case "fo":setSuggestions(new ArrayList<String>(Arrays.asList("福")), true, true);
                        break;
                    case "foder":setSuggestions(new ArrayList<String>(Arrays.asList("屌")), true, true);
                        break;
                    case "fofa":setSuggestions(new ArrayList<String>(Arrays.asList("可愛")), true, true);
                        break;
                    case "fofo":setSuggestions(new ArrayList<String>(Arrays.asList("可愛", "蓬松", "暄", "柔軟")), true, true);
                        break;
                    case "fogo":setSuggestions(new ArrayList<String>(Arrays.asList("火")), true, true);
                        break;
                    case "fome":setSuggestions(new ArrayList<String>(Arrays.asList("飢")), true, true);
                        break;
                    case "fon":setSuggestions(new ArrayList<String>(Arrays.asList("峰")), true, true);
                        break;
                    case "fong":setSuggestions(new ArrayList<String>(Arrays.asList("鋒")), true, true);
                        break;
                    case "for":setSuggestions(new ArrayList<String>(Arrays.asList("选上")), true, true);
                        break;
                    case "fora":setSuggestions(new ArrayList<String>(Arrays.asList("外")), true, true);
                        break;
                    case "forca":setSuggestions(new ArrayList<String>(Arrays.asList("力")), true, true);
                        break;
                    case "forcanascanelas":setSuggestions(new ArrayList<String>(Arrays.asList("加油")), true, true);
                        break;
                    case "forma":setSuggestions(new ArrayList<String>(Arrays.asList("形")), true, true);
                        break;
                    case "formosa":setSuggestions(new ArrayList<String>(Arrays.asList("台灣")), true, true);
                        break;
                    case "formula":setSuggestions(new ArrayList<String>(Arrays.asList("式")), true, true);
                        break;
                    case "fornec":setSuggestions(new ArrayList<String>(Arrays.asList("提供")), true, true);
                        break;
                    case "fornecer":setSuggestions(new ArrayList<String>(Arrays.asList("提供")), true, true);
                        break;
                    case "fosse":setSuggestions(new ArrayList<String>(Arrays.asList("居让")), true, true);
                        break;
                    case "fostes":setSuggestions(new ArrayList<String>(Arrays.asList("行您了")), true, true);
                        break;
                    case "fra":setSuggestions(new ArrayList<String>(Arrays.asList("香")), true, true);
                        break;
                    case "frade":setSuggestions(new ArrayList<String>(Arrays.asList("修士")), true, true);
                        break;
                    case "fragancia":setSuggestions(new ArrayList<String>(Arrays.asList("香")), true, true);
                        break;
                    case "franca":setSuggestions(new ArrayList<String>(Arrays.asList("仏國")), true, true);
                        break;
                    case "frangan":setSuggestions(new ArrayList<String>(Arrays.asList("香")), true, true);
                        break;
                    case "frecam":setSuggestions(new ArrayList<String>(Arrays.asList("頻道")), true, true);
                        break;
                    case "freq":setSuggestions(new ArrayList<String>(Arrays.asList("頻")), true, true);
                        break;
                    case "frequente":setSuggestions(new ArrayList<String>(Arrays.asList("頻繁")), true, true);
                        break;
                    case "frio":setSuggestions(new ArrayList<String>(Arrays.asList("寒")), true, true);
                        break;
                    case "fresco":setSuggestions(new ArrayList<String>(Arrays.asList("冷")), true, true);
                        break;
                    case "fruta":setSuggestions(new ArrayList<String>(Arrays.asList("果")), true, true);
                        break;
                    case "fu":setSuggestions(new ArrayList<String>(Arrays.asList("佛")), true, true);
                        break;
                    case "fucu":setSuggestions(new ArrayList<String>(Arrays.asList("福")), true, true);
                        break;
                    case "fuder":setSuggestions(new ArrayList<String>(Arrays.asList("操")), true, true);
                        break;
                    case "fum":setSuggestions(new ArrayList<String>(Arrays.asList("抽煙")), true, true);
                        break;
                    case "fumo":setSuggestions(new ArrayList<String>(Arrays.asList("煙")), true, true);
                        break;
                    case "fundo":setSuggestions(new ArrayList<String>(Arrays.asList("深")), true, true);
                        break;
                    case "fi":setSuggestions(new ArrayList<String>(Arrays.asList("菲")), true, true);
                        break;
                    case "futebol":setSuggestions(new ArrayList<String>(Arrays.asList("足球")), true, true);
                        break;
                    case "futuro":setSuggestions(new ArrayList<String>(Arrays.asList("未來")), true, true);
                        break;
                    case "futuropromissor":setSuggestions(new ArrayList<String>(Arrays.asList("前途洋洋")), true, true);
                        break;
                    case "gacu":setSuggestions(new ArrayList<String>(Arrays.asList("學")), true, true);
                        break;
                    case "gai":setSuggestions(new ArrayList<String>(Arrays.asList("同志")), true, true);
                        break;
                    case "gaiola":setSuggestions(new ArrayList<String>(Arrays.asList("籠")), true, true);
                        break;
                    case "galinha":setSuggestions(new ArrayList<String>(Arrays.asList("雞")), true, true);
                        break;
                    case "gan":setSuggestions(new ArrayList<String>(Arrays.asList("梗", "根")), true, true);
                        break;
                    case "ganbare":setSuggestions(new ArrayList<String>(Arrays.asList("加油")), true, true);
                        break;
                    case "ganda":setSuggestions(new ArrayList<String>(Arrays.asList("大")), true, true);
                        break;
                    case "ganza":setSuggestions(new ArrayList<String>(Arrays.asList("大麻")), true, true);
                        break;
                    case "garra":setSuggestions(new ArrayList<String>(Arrays.asList("爪")), true, true);
                        break;
                    case "gato":setSuggestions(new ArrayList<String>(Arrays.asList("貓")), true, true);
                        break;
                    case "gay":setSuggestions(new ArrayList<String>(Arrays.asList("男同志")), true, true);
                        break;
                    case "geni":setSuggestions(new ArrayList<String>(Arrays.asList("天才")), true, true);
                        break;
                    case "gente":setSuggestions(new ArrayList<String>(Arrays.asList("人人")), true, true);
                        break;
                    case "gentil":setSuggestions(new ArrayList<String>(Arrays.asList("優")), true, true);
                        break;
                    case "gequi":setSuggestions(new ArrayList<String>(Arrays.asList("劇")), true, true);
                        break;
                    case "gigante":setSuggestions(new ArrayList<String>(Arrays.asList("巨")), true, true);
                        break;
                    case "ginastica":setSuggestions(new ArrayList<String>(Arrays.asList("體操")), true, true);
                        break;
                    case "gordo":setSuggestions(new ArrayList<String>(Arrays.asList("胖")), true, true);
                        break;
                    case "demais":setSuggestions(new ArrayList<String>(Arrays.asList("太")), true, true);
                        break;
                    case "gost":setSuggestions(new ArrayList<String>(Arrays.asList("好")), true, true);
                        break;
                    case "gostar":setSuggestions(new ArrayList<String>(Arrays.asList("好")), true, true);
                        break;
                    case "gosto":setSuggestions(new ArrayList<String>(Arrays.asList("好", "👍")), true, true);
                        break;
                    case "gou":setSuggestions(new ArrayList<String>(Arrays.asList("夠")), true, true);
                        break;
                    case "governo":setSuggestions(new ArrayList<String>(Arrays.asList("政府")), true, true);
                        break;
                    case "grafica":setSuggestions(new ArrayList<String>(Arrays.asList("圖形")), true, true);
                        break;
                    case "gram":setSuggestions(new ArrayList<String>(Arrays.asList("大")), true, true);
                        break;
                    case "gran":setSuggestions(new ArrayList<String>(Arrays.asList("大")), true, true);
                        break;
                    case "grande":setSuggestions(new ArrayList<String>(Arrays.asList("大")), true, true);
                        break;
                    case "gratis":setSuggestions(new ArrayList<String>(Arrays.asList("無償")), true, true);
                        break;
                    case "grau":setSuggestions(new ArrayList<String>(Arrays.asList("度")), true, true);
                        break;
                    case "graus":setSuggestions(new ArrayList<String>(Arrays.asList("度")), true, true);
                        break;
                    case "gravidade":setSuggestions(new ArrayList<String>(Arrays.asList("重力")), true, true);
                        break;
                    case "greg":setSuggestions(new ArrayList<String>(Arrays.asList("葛瑞格")), true, true);
                        break;
                    case "gregorio":setSuggestions(new ArrayList<String>(Arrays.asList("葛瑞格爾")), true, true);
                        break;
                    case "grelh":setSuggestions(new ArrayList<String>(Arrays.asList("炙烤")), true, true);
                        break;
                    case "grelhar":setSuggestions(new ArrayList<String>(Arrays.asList("炙烤")), true, true);
                        break;
                    case "grindar":setSuggestions(new ArrayList<String>(Arrays.asList("錯")), true, true);
                        break;
                    case "gritar":setSuggestions(new ArrayList<String>(Arrays.asList("叫")), true, true);
                        break;
                    case "grossa":setSuggestions(new ArrayList<String>(Arrays.asList("厚")), true, true);
                        break;
                    case "grosso":setSuggestions(new ArrayList<String>(Arrays.asList("厚")), true, true);
                        break;
                    case "guardar":setSuggestions(new ArrayList<String>(Arrays.asList("保存", "衛")), true, true);
                        break;
                    case "guard":setSuggestions(new ArrayList<String>(Arrays.asList("衛")), true, true);
                        break;
                    case "guerr":setSuggestions(new ArrayList<String>(Arrays.asList("戰")), true, true);
                        break;
                    case "guerra":setSuggestions(new ArrayList<String>(Arrays.asList("戰爭")), true, true);
                        break;
                    case "guerreiro":setSuggestions(new ArrayList<String>(Arrays.asList("戰士")), true, true);
                        break;
                    case "guitarra":setSuggestions(new ArrayList<String>(Arrays.asList("吉他")), true, true);
                        break;
                    case "há":setSuggestions(new ArrayList<String>(Arrays.asList("哈", "有")), true, true);
                        break;
                    case "han":setSuggestions(new ArrayList<String>(Arrays.asList("韓", "汉")), true, true);
                        break;
                    case "hancaractéres":setSuggestions(new ArrayList<String>(Arrays.asList("漢字")), true, true);
                        break;
                    case "hatsu":setSuggestions(new ArrayList<String>(Arrays.asList("發")), true, true);
                        break;
                    case "hermita":setSuggestions(new ArrayList<String>(Arrays.asList("仙人")), true, true);
                        break;
                    case "herói":setSuggestions(new ArrayList<String>(Arrays.asList("英雄")), true, true);
                        break;
                    case "hh":setSuggestions(new ArrayList<String>(Arrays.asList("っ")), true, true);
                        break;
                    case "hiper":setSuggestions(new ArrayList<String>(Arrays.asList("巨大")), true, true);
                        break;
                    case "trofia":setSuggestions(new ArrayList<String>(Arrays.asList("肥")), true, true);
                        break;
                    case "hoje":setSuggestions(new ArrayList<String>(Arrays.asList("今日")), true, true);
                        break;
                    case "holanda":setSuggestions(new ArrayList<String>(Arrays.asList("荷蘭")), true, true);
                        break;
                    case "homem":setSuggestions(new ArrayList<String>(Arrays.asList("男")), true, true);
                        break;
                    case "homo":setSuggestions(new ArrayList<String>(Arrays.asList("同士")), true, true);
                        break;
                    case "hora":setSuggestions(new ArrayList<String>(Arrays.asList("時")), true, true);
                        break;
                    case "hospital":setSuggestions(new ArrayList<String>(Arrays.asList("医院")), true, true);
                        break;
                    case "hotel":setSuggestions(new ArrayList<String>(Arrays.asList("宾館")), true, true);
                        break;
                    case "hou":setSuggestions(new ArrayList<String>(Arrays.asList("方")), true, true);
                        break;
                    case "feliz":setSuggestions(new ArrayList<String>(Arrays.asList("歡")), true, true);
                        break;
                    case "humano":setSuggestions(new ArrayList<String>(Arrays.asList("人間")), true, true);
                        break;
                    case "hunon":setSuggestions(new ArrayList<String>(Arrays.asList("糊弄")), true, true);
                        break;
                    case "i":setSuggestions(new ArrayList<String>(Arrays.asList("工", "伊")), true, true);
                        break;
                    case "iang":setSuggestions(new ArrayList<String>(Arrays.asList("央")), true, true);
                        break;
                    case "idade":setSuggestions(new ArrayList<String>(Arrays.asList("年齡")), true, true);
                        break;
                    case "ideia":setSuggestions(new ArrayList<String>(Arrays.asList("想")), true, true);
                        break;
                    case "identidade":setSuggestions(new ArrayList<String>(Arrays.asList("素性")), true, true);
                        break;
                    case "idiota":setSuggestions(new ArrayList<String>(Arrays.asList("蠢材")), true, true);
                        break;
                    case "iei":setSuggestions(new ArrayList<String>(Arrays.asList("耶")), true, true);
                        break;
                    case "igreja":setSuggestions(new ArrayList<String>(Arrays.asList("教堂")), true, true);
                        break;
                    case "igual":setSuggestions(new ArrayList<String>(Arrays.asList("同")), true, true);
                        break;
                    case "iin":setSuggestions(new ArrayList<String>(Arrays.asList("應")), true, true);
                        break;
                    case "imagem":setSuggestions(new ArrayList<String>(Arrays.asList("圖")), true, true);
                        break;
                    case "imaginação":setSuggestions(new ArrayList<String>(Arrays.asList("想像")), true, true);
                        break;
                    case "imperio":setSuggestions(new ArrayList<String>(Arrays.asList("帝國")), true, true);
                        break;
                    case "implementar":setSuggestions(new ArrayList<String>(Arrays.asList("実現")), true, true);
                        break;
                    case "importar":setSuggestions(new ArrayList<String>(Arrays.asList("輸入")), true, true);
                        break;
                    case "impostar":setSuggestions(new ArrayList<String>(Arrays.asList("定")), true, true);
                        break;
                    case "impressao":setSuggestions(new ArrayList<String>(Arrays.asList("打印")), true, true);
                        break;
                    case "imprimir":setSuggestions(new ArrayList<String>(Arrays.asList("打印")), true, true);
                        break;
                    case "inbi":setSuggestions(new ArrayList<String>(Arrays.asList("硬幣")), true, true);
                        break;
                    case "incrivel":setSuggestions(new ArrayList<String>(Arrays.asList("真棒")), true, true);
                        break;
                    case "indía":setSuggestions(new ArrayList<String>(Arrays.asList("天竺")), true, true);
                        break;
                    case "individuo":setSuggestions(new ArrayList<String>(Arrays.asList("個")), true, true);
                        break;
                    case "inexpectadamente":setSuggestions(new ArrayList<String>(Arrays.asList("不意")), true, true);
                        break;
                    case "infame":setSuggestions(new ArrayList<String>(Arrays.asList("臭名昭著")), true, true);
                        break;
                    case "inflacionar":setSuggestions(new ArrayList<String>(Arrays.asList("膨")), true, true);
                        break;
                    case "inflatar":setSuggestions(new ArrayList<String>(Arrays.asList("膨")), true, true);
                        break;
                    case "informacao":setSuggestions(new ArrayList<String>(Arrays.asList("訊")), true, true);
                        break;
                    case "inglaterra":setSuggestions(new ArrayList<String>(Arrays.asList("英國")), true, true);
                        break;
                    case "ingles":setSuggestions(new ArrayList<String>(Arrays.asList("英語")), true, true);
                        break;
                    case "inicio":setSuggestions(new ArrayList<String>(Arrays.asList("初")), true, true);
                        break;
                    case "iningue":setSuggestions(new ArrayList<String>(Arrays.asList("裏")), true, true);
                        break;
                    case "insecto":setSuggestions(new ArrayList<String>(Arrays.asList("虫")), true, true);
                        break;
                    case "inserir":setSuggestions(new ArrayList<String>(Arrays.asList("插")), true, true);
                        break;
                    case "inspecao":setSuggestions(new ArrayList<String>(Arrays.asList("験")), true, true);
                        break;
                    case "inspiracao":setSuggestions(new ArrayList<String>(Arrays.asList("影響")), true, true);
                        break;
                    case "inspirou":setSuggestions(new ArrayList<String>(Arrays.asList("影響了")), true, true);
                        break;
                    case "instamorte":setSuggestions(new ArrayList<String>(Arrays.asList("秒殺")), true, true);
                        break;
                    case "instantaneo":setSuggestions(new ArrayList<String>(Arrays.asList("即時的", "頓時")), true, true);
                        break;
                    case "instante":setSuggestions(new ArrayList<String>(Arrays.asList("即時", "瞬間")), true, true);
                        break;
                    case "inteligencia":setSuggestions(new ArrayList<String>(Arrays.asList("情報")), true, true);
                        break;
                    case "intencao":setSuggestions(new ArrayList<String>(Arrays.asList("意思")), true, true);
                        break;
                    case "interessante":setSuggestions(new ArrayList<String>(Arrays.asList("有趣")), true, true);
                        break;
                    case "interesse":setSuggestions(new ArrayList<String>(Arrays.asList("趣")), true, true);
                        break;
                    case "internet":setSuggestions(new ArrayList<String>(Arrays.asList("網絡")), true, true);
                        break;
                    case "intrometer":setSuggestions(new ArrayList<String>(Arrays.asList("妨礙")), true, true);
                        break;
                    case "inventar":setSuggestions(new ArrayList<String>(Arrays.asList("発明")), true, true);
                        break;
                    case "investigacao":setSuggestions(new ArrayList<String>(Arrays.asList("檢查")), true, true);
                        break;
                    case "iou":setSuggestions(new ArrayList<String>(Arrays.asList("用")), true, true);
                        break;
                    case "iquii":setSuggestions(new ArrayList<String>(Arrays.asList("愛奇藝")), true, true);
                        break;
                    case "sair":setSuggestions(new ArrayList<String>(Arrays.asList("去")), true, true);
                        break;
                    case "ir":setSuggestions(new ArrayList<String>(Arrays.asList("行","往")), true, true);
                        break;
                    case "irmandade":setSuggestions(new ArrayList<String>(Arrays.asList("兄弟會")), true, true);
                        break;
                    case "irmaozinho":setSuggestions(new ArrayList<String>(Arrays.asList("弟")), true, true);
                        break;
                    case "irmaza":setSuggestions(new ArrayList<String>(Arrays.asList("姐")), true, true);
                        break;
                    case "irritante":setSuggestions(new ArrayList<String>(Arrays.asList("惱人")), true, true);
                        break;
                    case "irritar":setSuggestions(new ArrayList<String>(Arrays.asList("慪")), true, true);
                        break;
                    case "isca":setSuggestions(new ArrayList<String>(Arrays.asList("餌")), true, true);
                        break;
                    case "isol":setSuggestions(new ArrayList<String>(Arrays.asList("孤")), true, true);
                        break;
                    case "isolar":setSuggestions(new ArrayList<String>(Arrays.asList("孤")), true, true);
                        break;
                    case "isso":setSuggestions(new ArrayList<String>(Arrays.asList("那")), true, true);
                        break;
                    case "isto":setSuggestions(new ArrayList<String>(Arrays.asList("這個")), true, true);
                        break;
                    case "iui":setSuggestions(new ArrayList<String>(Arrays.asList("唯")), true, true);
                        break;
                    case "iuserneime":setSuggestions(new ArrayList<String>(Arrays.asList("賬號")), true, true);
                        break;
                    case "ivo":setSuggestions(new ArrayList<String>(Arrays.asList("的")), true, true);
                        break;
                    case "já":setSuggestions(new ArrayList<String>(Arrays.asList("既", "已經")), true, true);
                        break;
                    case "japao":setSuggestions(new ArrayList<String>(Arrays.asList("日本")), true, true);
                        break;
                    case "japones":setSuggestions(new ArrayList<String>(Arrays.asList("日本人", "日本語")), true, true);
                    case "japonês":setSuggestions(new ArrayList<String>(Arrays.asList("日本人", "日本語")), true, true);
                        break;
                    case "jato":setSuggestions(new ArrayList<String>(Arrays.asList("噴出")), true, true);
                        break;
                    case "jaze":setSuggestions(new ArrayList<String>(Arrays.asList("爵士")), true, true);
                        break;
                    case "jeimess":setSuggestions(new ArrayList<String>(Arrays.asList("詹姆斯")), true, true);
                        break;
                    case "Jeimess":setSuggestions(new ArrayList<String>(Arrays.asList("詹me斯")), true, true);
                        break;
                    case "jema":setSuggestions(new ArrayList<String>(Arrays.asList("這麼")), true, true);
                        break;
                    case "jeva":setSuggestions(new ArrayList<String>(Arrays.asList("爪哇")), true, true);
                        break;
                    case "ji":setSuggestions(new ArrayList<String>(Arrays.asList("吉", "姬")), true, true);
                        break;
                    case "jia":setSuggestions(new ArrayList<String>(Arrays.asList("假")), true, true);
                        break;
                    case "jin":setSuggestions(new ArrayList<String>(Arrays.asList("人")), true, true);
                        break;
                    case "so":setSuggestions(new ArrayList<String>(Arrays.asList("就")), true, true);
                        break;
                    case "jo":setSuggestions(new ArrayList<String>(Arrays.asList("舟")), true, true);
                        break;
                    case "joao":setSuggestions(new ArrayList<String>(Arrays.asList("莊")), true, true);
                        break;
                    case "jogar":setSuggestions(new ArrayList<String>(Arrays.asList("玩", "玩耍")), true, true);
                        break;
                    case "jogo":setSuggestions(new ArrayList<String>(Arrays.asList("賽", "遊戲")), true, true);
                        break;
                    case "john":setSuggestions(new ArrayList<String>(Arrays.asList("莊")), true, true);
                        break;
                    case "jone":setSuggestions(new ArrayList<String>(Arrays.asList("莊")), true, true);
                        break;
                    case "jor":setSuggestions(new ArrayList<String>(Arrays.asList("喬")), true, true);
                        break;
                    case "jornada":setSuggestions(new ArrayList<String>(Arrays.asList("旅程")), true, true);
                        break;
                    case "jordão":setSuggestions(new ArrayList<String>(Arrays.asList("佐敦")), true, true);
                        break;
                    case "jou":setSuggestions(new ArrayList<String>(Arrays.asList("常")), true, true);
                        break;
                    case "ju":setSuggestions(new ArrayList<String>(Arrays.asList("舉")), true, true);
                        break;
                    default: setSuggestions(sugestões, true, true);
                }
            }
            else {
                setSuggestions(null, false, false);
                Log.i(TAG, "updateCandidates: se mcomposing.length = 0");
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
            updateCandidates();
            Log.i(TAG, "handleBackspace: se length > 1");
        } else if (length > 0) {
            mComposing.setLength(length - 1); ///(0)
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
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
            updateCandidates(); ///este é o primeiro
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
                updateCandidates(); //mcomposing em else
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