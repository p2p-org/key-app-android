package org.p2p.wallet.main.repository

import org.p2p.wallet.infrastructure.network.ErrorCode
import org.p2p.wallet.infrastructure.network.ServerException
import org.p2p.wallet.main.api.MoonpayApi
import org.p2p.wallet.main.model.BuyCurrency
import org.p2p.wallet.main.model.MoonpayBuyResult
import java.math.BigDecimal

class MoonpayRemoteRepository(
    private val api: MoonpayApi,
    private val apiKey: String
) : MoonpayRepository {

    override suspend fun getCurrency(
        baseCurrencyAmount: String,
        quoteCurrencyCode: String,
        baseCurrencyCode: String
    ): MoonpayBuyResult =
        try {
            val response = api.getBuyCurrency(
                quoteCurrencyCode = quoteCurrencyCode,
                apiKey = apiKey,
                baseCurrencyAmount = baseCurrencyAmount,
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