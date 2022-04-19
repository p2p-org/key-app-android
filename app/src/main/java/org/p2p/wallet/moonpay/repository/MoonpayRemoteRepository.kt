package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import java.math.BigDecimal

class MoonpayRemoteRepository(
    private val api: MoonpayApi,
    private val moonpayApiKey: String
) : MoonpayRepository {

    override suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String
    ): MoonpayBuyResult =
        try {
            val response = api.getBuyCurrency(
                quoteCurrencyCode = tokenToBuy.tokenSymbolForMoonPay,
                apiKey = moonpayApiKey,
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
        val response = api.getCurrencyAskPrice(tokenToGetPrice.tokenSymbolForMoonPay, moonpayApiKey)
        return response.amountInUsd
    }

    private val Token.tokenSymbolForMoonPay: String
        get() {
            val tokenLowercase = tokenSymbol.lowercase()
            return if (isUSDC) {
                "${tokenLowercase}_${SOL_SYMBOL.lowercase()}"
            } else {
                tokenLowercase
            }
        }
}
