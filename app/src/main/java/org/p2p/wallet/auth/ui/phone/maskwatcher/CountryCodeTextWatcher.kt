package org.p2p.wallet.auth.ui.phone.maskwatcher

import android.text.Editable
import android.text.TextWatcher

class CountryCodeTextWatcher(private val afterTextChanged: (String) -> Unit) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        afterTextChanged.invoke(s.toString())
    }
}
