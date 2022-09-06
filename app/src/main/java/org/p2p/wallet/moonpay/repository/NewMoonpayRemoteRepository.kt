package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

class NewMoonpayRemoteRepository(
    private val api: MoonpayApi,
    private val moonpayApiKey: String,
    private val mapper: MoonpayApiMapper
) : MoonpayRepository {

    override suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String
    ): MoonpayBuyResult = try {
        val response = api.getBuyCurrency(
            quoteCurrencyCode = tokenToBuy.tokenSymbolForMoonPay,
            apiKey = moonpayApiKey,
            baseCurrencyAmount = baseCurrencyAmount,
            quoteCurrencyAmount = quoteCurrencyAmount,
            baseCurrencyCode = baseCurrencyCode
        )
        if (mapper.isMinimumAmountValid(response)) {
            MoonpayBuyResult.Success(mapper.fromNetworkToDomain(response))
        } else {
            MoonpayBuyResult.MinimumAmountError
        }
    } catch (error: ServerException) {
        when {
            mapper.isMinimumAmountException(error) -> {
                MoonpayBuyResult.MinimumAmountError
            }
            error.errorCode == ErrorCode.BAD_REQUEST -> {
                MoonpayBuyResult.Error(mapper.fromNetworkErrorToDomainMessage(error))
            }
            else -> {
                throw error
            }
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
                "${tokenLowercase}_${Constants.SOL_SYMBOL.lowercase()}"
            } else {
                tokenLowercase
            }
        }
}
