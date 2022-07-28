package org.p2p.wallet.auth.ui.phone.repository

import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.auth.ui.phone.CountryPickerParsingManager
import org.p2p.wallet.auth.ui.phone.model.CountryCode

class CountryCodeInMemoryRepository(private val resources: Resources) : CountryCodeLocalRepository {

    private val countryCodesFlow = MutableStateFlow<List<CountryCode>>(emptyList())

    override fun getCountryCodes(): List<CountryCode> {
        if (countryCodesFlow.value.isEmpty()) {
            countryCodesFlow.value = CountryPickerParsingManager.readCountriesFromXml(resources)
        }
        return countryCodesFlow.value
    }
}
