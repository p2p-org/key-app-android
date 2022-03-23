package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import java.math.BigDecimal

class MoonpayRemoteRepository(
    private val api: MoonpayApi,
    private val apiKey: String
) : MoonpayRepository {

    override suspend fun getCurrency(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        quoteCurrencyCode: String,
        baseCurrencyCode: String
    ): MoonpayBuyResult =
        try {
            val response = api.getBuyCurrency(
                quoteCurrencyCode = quoteCurrencyCode,
                apiKey = apiKey,
                baseCurrencyAmount = baseCurrencyAmount,
                quoteCurrencyAmount = quoteCurrencyAmount,
                baseCurrencyCode = baseCurrencyCode
            )
            MoonpayBuyResult.Success(BuyCurrency(response))
        } catch (e: ServerException) {
            if (e.errorCode == ErrorCode.BAD_REQUEST) {
                MoonpayBuyResult.Error(e.getDirectMessage() ?: e.localizedMessage)
            } else {
                throw e
            }
        }

    override suspend fun getCurrencyAskPrice(quoteCurrencyCode: String): BigDecimal {
        val response = api.getCurrencyAskPrice(quoteCurrencyCode, apiKey)
        return response.usd
    }
}
