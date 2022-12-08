package org.p2p.wallet.moonpay.repository

import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.api.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import java.math.BigDecimal

interface NewMoonpayBuyRepository {

    suspend fun getBuyCurrencyData(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        tokenToBuy: Token,
        baseCurrencyCode: String,
        paymentMethod: String,
    ): MoonpayBuyCurrencyResponse

    suspend fun getCurrencyAskPrice(tokenToGetPrice: Token): BigDecimal

    suspend fun getIpAddressData(): MoonpayIpAddressResponse
}
