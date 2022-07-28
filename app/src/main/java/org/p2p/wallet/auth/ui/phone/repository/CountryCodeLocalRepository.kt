package org.p2p.wallet.auth.ui.phone.repository

import org.p2p.wallet.auth.ui.phone.model.CountryCode

interface CountryCodeLocalRepository {
    fun getCountryCodes(): List<CountryCode>
}
