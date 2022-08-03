package org.p2p.wallet.auth.ui.phone.maskwatcher

import android.text.TextWatcher
import android.text.Editable
import org.p2p.wallet.auth.widget.HintEditText
import java.lang.StringBuilder

class PhoneNumberTextWatcher(
    private val phoneField: HintEditText,
    private val afterTextChanged: (String) -> Unit
) : TextWatcher {
    private var ignoreOnPhoneChange = false
    private var characterAction = -1
    private var actionPosition = 0

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (count == 0 && after == 1) {
            characterAction = 1
        } else if (count == 1 && after == 0) {
            if (s[start] == ' ' && start > 0) {
                characterAction = 3
                actionPosition = start - 1
            } else {
                characterAction = 2
            }
        } else {
            characterAction = -1
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable) {
        if (ignoreOnPhoneChange) {
            return
        }
        var start = phoneField.selectionStart
        val phoneChars = "0123456789"
        var str = phoneField.text.toString()
        if (characterAction == 3) {
            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1)
            start--
        }
        val builder = StringBuilder(str.length)
        for (a in str.indices) {
            val ch = str.substring(a, a + 1)
            if (phoneChars.contains(ch)) {
                builder.append(ch)
            }
        }
        ignoreOnPhoneChange = true
        val hint = phoneField.getHintText()
        if (hint != null) {
            var a = 0
            while (a < builder.length) {
                if (a < hint.length) {
                    if (hint[a] == ' ') {
                        builder.insert(a, ' ')
                        a++
                        if (start == a && characterAction != 2 && characterAction != 3) {
                            start++
                        }
                    }
                } else {
                    builder.insert(a, ' ')
                    if (start == a + 1 && characterAction != 2 && characterAction != 3) {
                        start++
                    }
                    break
                }
                a++
            }
        }
        s.replace(0, s.length, builder)
        if (start >= 0) {
            phoneField.setSelection(start.coerceAtMost(phoneField.length()))
        }
        phoneField.onTextChange()
        ignoreOnPhoneChange = false
        afterTextChanged.invoke(s.toString())
    }
}
