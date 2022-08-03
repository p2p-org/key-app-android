package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.model.CountryCode

interface CountryCodeLocalRepository {
    suspend fun getCountryCodes(): List<CountryCode>
}
