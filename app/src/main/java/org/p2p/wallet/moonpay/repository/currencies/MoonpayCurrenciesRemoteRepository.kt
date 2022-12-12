package org.p2p.wallet.moonpay.repository.currencies

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyAmounts

class MoonpayCurrenciesRemoteRepository(
    private val clientSideApi: MoonpayClientSideApi
) : MoonpayCurrenciesRepository {
    override suspend fun getAllCurrencies(): List<MoonpayCurrency> =
        clientSideApi.getAllCurrencies(BuildConfig.moonpayKey)
            .map { response ->
                response.run {
                    val amounts = MoonpayCurrencyAmounts(
                        minAmount = minAmount,
                        maxAmount = maxAmount,
                        minBuyAmount = minBuyAmount,
                        maxBuyAmount = maxBuyAmount,
                        minSellAmount = minSellAmount,
                        maxSellAmount = maxSellAmount
                    )
                    if (currencyType == "crypto") {
                        MoonpayCurrency.CryptoToken(
                            tokenSymbol = currencySymbol,
                            tokenName = currencyName,
                            currencyId = currencyId,
                            amounts = amounts
                        )
                    } else {
                        MoonpayCurrency.Fiat(
                            fiatCode = currencySymbol,
                            fiatName = currencyName,
                            currencyId = currencyId,
                            amounts = amounts
                        )
                    }
                }
            }
}
