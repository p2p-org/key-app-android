package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import java.math.BigDecimal

interface MoonpayRepository {

    suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String
    ): MoonpayBuyResult

    suspend fun getCurrencyAskPrice(tokenToGetPrice: Token): BigDecimal

    suspend fun getIpAddressData(): MoonpayIpAddressResponse
}
