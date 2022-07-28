package org.p2p.wallet.auth.ui.phone.model

import android.text.Editable
import android.text.TextWatcher

class CountryDetectorTextWatcher : TextWatcher {

    private var lastCheckedNumber: String = ""
    private var selectedCountry: CountryCode? = null
    private var countryDetectionBasedOnAreaAllowed = true

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (selectedCountry == null || lastCheckedNumber != s.toString() && countryDetectionBasedOnAreaAllowed) {
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    fun updateSelectedCountry(newCountry: CountryCode) {
        selectedCountry = newCountry
    }
}
