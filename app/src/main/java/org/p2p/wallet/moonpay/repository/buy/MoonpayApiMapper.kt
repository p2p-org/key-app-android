package org.p2p.wallet.moonpay.repository.buy

import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.wallet.infrastructure.network.interceptor.MoonpayRequestException
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.utils.emptyString

class MoonpayApiMapper {
    fun fromNetworkToDomain(response: MoonpayBuyCurrencyResponse): BuyCurrency {
        return BuyCurrency(
            receiveAmount = response.quoteCurrencyAmount,
            price = response.quoteCurrencyPrice,
            feeAmount = response.feeAmount,
            extraFeeAmount = response.extraFeeAmount,
            networkFeeAmount = response.networkFeeAmount,
            totalFiatAmount = response.totalAmount,
            baseCurrency = BuyCurrency.Currency(
                code = response.baseCurrency.code.removeUnderscoreSolIfUsdc(),
                minAmount = response.baseCurrency.minBuyAmount,
                maxAmount = response.baseCurrency.maxBuyAmount
            ),
            quoteCurrency = BuyCurrency.Currency(
                code = response.currency.code.removeUnderscoreSolIfUsdc(),
                minAmount = response.currency.minBuyAmount,
                maxAmount = response.currency.maxBuyAmount
            )
        )
    }

    fun fromNetworkErrorToDomainMessage(error: MoonpayRequestException): String {
        val errorMessage = error.message
        return errorMessage.removeUnderscoreSolIfUsdc()
    }

    /**
     * USDC from moonpay comes with _SOL suffix, so remove it
     * @return USDC without _SOL suffix
     */
    private fun String.removeUnderscoreSolIfUsdc(): String {
        return if (contains(USDC_SYMBOL, ignoreCase = true)) {
            replace("_$SOL_SYMBOL", emptyString())
        } else {
            this
        }
    }
}
