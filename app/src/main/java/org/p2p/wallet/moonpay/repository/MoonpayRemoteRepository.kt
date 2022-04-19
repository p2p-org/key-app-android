package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

class MoonpayRemoteRepository(
    private val api: MoonpayApi,
    private val apiKey: String
) : MoonpayRepository {

    override suspend fun getCurrency(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String
    ): MoonpayBuyResult =
        try {
            val response = api.getBuyCurrency(
                quoteCurrencyCode = tokenToBuy.tokenSymbolForMoonPay,
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

    override suspend fun getCurrencyAskPrice(tokenToGetPrice: Token): BigDecimal {
        val response = api.getCurrencyAskPrice(tokenToGetPrice.tokenSymbolForMoonPay, apiKey)
        return response.usd
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
