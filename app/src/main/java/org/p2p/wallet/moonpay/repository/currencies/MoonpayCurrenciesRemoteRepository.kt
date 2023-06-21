package org.p2p.wallet.moonpay.repository.currencies

import org.p2p.core.BuildConfig.moonpayKey
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrency
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyAmounts
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyResponse

private const val CURRENCY_TYPE_CRYPTO = "crypto"

class MoonpayCurrenciesRemoteRepository(
    private val clientSideApi: MoonpayClientSideApi
) : MoonpayCurrenciesRepository {

    override suspend fun getAllCurrencies(): List<MoonpayCurrency> =
        clientSideApi.getAllCurrencies(moonpayKey)
            .map(::mapToDomain)

    private fun mapToDomain(response: MoonpayCurrencyResponse): MoonpayCurrency = response.run {
        if (currencyType == CURRENCY_TYPE_CRYPTO) {
            MoonpayCurrency.CryptoToken(
                tokenSymbol = currencySymbol,
                tokenName = currencyName,
                currencyId = currencyId,
                amounts = createAmounts()
            )
        } else {
            MoonpayCurrency.Fiat(
                fiatCode = currencySymbol,
                fiatName = currencyName,
                currencyId = currencyId,
                amounts = createAmounts()
            )
        }
    }

    private fun MoonpayCurrencyResponse.createAmounts(): MoonpayCurrencyAmounts =
        MoonpayCurrencyAmounts(
            minAmount = minAmount.toBigDecimal(),
            maxAmount = maxAmount.toBigDecimal(),
            minBuyAmount = minBuyAmount.toBigDecimal(),
            maxBuyAmount = maxBuyAmount.toBigDecimal(),
            minSellAmount = minSellAmount.toBigDecimal(),
            maxSellAmount = maxSellAmount.toBigDecimal()
        )
}
