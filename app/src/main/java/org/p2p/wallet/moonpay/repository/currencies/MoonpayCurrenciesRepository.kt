package org.p2p.wallet.moonpay.repository.currencies

import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency

interface MoonpayCurrenciesRepository {
    suspend fun getAllCurrencies(): List<MoonpayCurrency>
}
