package org.p2p.wallet.moonpay.repository.buy

import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.BuildConfig.moonpayKey
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayIpAddressResponse

class NewMoonpayBuyRemoteRepository(
    private val api: MoonpayClientSideApi,
    private val dispatchers: CoroutineDispatchers
) : NewMoonpayBuyRepository {

    private val moonpayApiKey: String = moonpayKey

    override suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String,
        paymentMethod: String,
    ): MoonpayBuyCurrencyResponse {
        return api.getBuyCurrency(
            quoteCurrencyCode = tokenToBuy.tokenSymbolForMoonPay,
            apiKey = moonpayApiKey,
            baseCurrencyAmount = baseCurrencyAmount,
            quoteCurrencyAmount = quoteCurrencyAmount,
            baseCurrencyCode = baseCurrencyCode,
            paymentMethod = paymentMethod
        )
    }

    override suspend fun getCurrencyAskPrice(tokenToGetPrice: Token): BigDecimal {
        val response = api.getCurrencyAskPrice(tokenToGetPrice.tokenSymbolForMoonPay, moonpayApiKey)
        return response.amountInUsd
    }

    override suspend fun getIpAddressData(): MoonpayIpAddressResponse = withContext(dispatchers.io) {
        api.getIpAddress(moonpayApiKey)
    }

    private val Token.tokenSymbolForMoonPay: String
        get() {
            val tokenLowercase = tokenSymbol.lowercase()
            return if (isUSDC) {
                "${tokenLowercase}_${Constants.SOL_SYMBOL.lowercase()}"
            } else {
                tokenLowercase
            }
        }
}
