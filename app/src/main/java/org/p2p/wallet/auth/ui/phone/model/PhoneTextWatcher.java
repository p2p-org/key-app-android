package org.p2p.wallet.auth.ui.phone.model;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.p2p.wallet.auth.ui.phone.UiKitTextField;

public class PhoneTextWatcher implements TextWatcher {
    private final Runnable runnable;
    private final UiKitTextField phoneField;
    private boolean ignoreOnPhoneChange = false;

    private int characterAction = -1;
    private int actionPosition;

    public PhoneTextWatcher(UiKitTextField phoneField, Runnable runnable) {
        this.phoneField = phoneField;
        this.runnable = runnable;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (count == 0 && after == 1) {
            characterAction = 1;
        } else if (count == 1 && after == 0) {
            if (s.charAt(start) == ' ' && start > 0) {
                characterAction = 3;
                actionPosition = start - 1;
            } else {
                characterAction = 2;
            }
        } else {
            characterAction = -1;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (ignoreOnPhoneChange) {
            return;
        }
        int start = phoneField.getSelectionStart();
        String phoneChars = "0123456789";
        String str = phoneField.getText().toString();
        if (characterAction == 3) {
            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1);
            start--;
        }
        StringBuilder builder = new StringBuilder(str.length());
        for (int a = 0; a < str.length(); a++) {
            String ch = str.substring(a, a + 1);
            if (phoneChars.contains(ch)) {
                builder.append(ch);
            }
        }
        ignoreOnPhoneChange = true;
        String hint = phoneField.getHint().toString();
        for (int a = 0; a < builder.length(); a++) {
            if (a < hint.length()) {
                if (hint.charAt(a) == ' ') {
                    builder.insert(a, ' ');
                    a++;
                    if (start == a && characterAction != 2 && characterAction != 3) {
                        start++;
                    }
                }
            } else {
                builder.insert(a, ' ');
                if (start == a + 1 && characterAction != 2 && characterAction != 3) {
                    start++;
                }
                break;
            }
        }
        s.replace(0, s.length(), builder);
        if (start >= 0) {
            phoneField.setSelection(Math.min(start, phoneField.length()));
        }
        phoneField.onTextChange();
        ignoreOnPhoneChange = false;
        if (runnable != null) runnable.run();
    }
}