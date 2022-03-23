package org.p2p.wallet.common.ui.textwatcher

import android.text.Editable
import android.text.TextWatcher

open class SimpleTextWatcher : TextWatcher {

    override fun afterTextChanged(text: Editable) = Unit

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit
}
