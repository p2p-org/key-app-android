package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

class NewMoonpayBuyRemoteRepository(
    private val api: MoonpayApi,
    private val moonpayApiKey: String,
    private val mapper: MoonpayApiMapper
) : NewMoonpayBuyRepository {

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

    override suspend fun getIpAddressData(): MoonpayIpAddressResponse {
        return api.getIpAddress(moonpayApiKey)
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
