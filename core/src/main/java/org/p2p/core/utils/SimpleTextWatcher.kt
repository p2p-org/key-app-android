package org.p2p.core.utils

import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTagTextWatcher(val tag: Any) : TextWatcher {

    override fun afterTextChanged(text: Editable) {
        afterTextChanged(tag, text)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit

    abstract fun afterTextChanged(tag: Any, text: Editable)
}
