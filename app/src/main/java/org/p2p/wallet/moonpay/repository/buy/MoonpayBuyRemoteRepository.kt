package org.p2p.wallet.moonpay.repository.buy

import java.math.BigDecimal
import org.p2p.core.BuildConfig.moonpayKey
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayRequestException
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayIpAddressResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyResult

class MoonpayBuyRemoteRepository(
    private val api: MoonpayClientSideApi,
    private val mapper: MoonpayApiMapper
) : MoonpayBuyRepository {

    private val moonpayApiKey: String = moonpayKey

    override suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String,
        paymentMethod: String,
    ): MoonpayBuyResult = try {
        val response = api.getBuyCurrency(
            quoteCurrencyCode = tokenToBuy.tokenSymbolForMoonPay,
            apiKey = moonpayApiKey,
            baseCurrencyAmount = baseCurrencyAmount,
            quoteCurrencyAmount = quoteCurrencyAmount,
            baseCurrencyCode = baseCurrencyCode,
            paymentMethod = paymentMethod
        )
        MoonpayBuyResult.Success(mapper.fromNetworkToDomain(response))
    } catch (error: MoonpayRequestException) {
        if (error.isBadRequest) {
            MoonpayBuyResult.Error(mapper.fromNetworkErrorToDomainMessage(error), error)
        } else {
            throw error
        }
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
                "${tokenLowercase}_${SOL_SYMBOL.lowercase()}"
            } else {
                tokenLowercase
            }
        }
}
