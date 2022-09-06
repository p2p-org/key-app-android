package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.infrastructure.network.data.ErrorCode
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.moonpay.api.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USDC_SYMBOL
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.isMoreThan

private const val MIN_FIAT_AMOUNT = 30
private val FIAT_CURRENCY_CODES = listOf("eur", "usd", "gbp")

class MoonpayApiMapper {
    fun fromNetworkToDomain(response: MoonpayBuyCurrencyResponse): BuyCurrency {
        return BuyCurrency(
            receiveAmount = response.quoteCurrencyAmount,
            price = response.quoteCurrencyPrice,
            feeAmount = response.feeAmount,
            extraFeeAmount = response.extraFeeAmount,
            networkFeeAmount = response.networkFeeAmount,
            totalAmount = response.totalAmount,
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

    fun fromNetworkErrorToDomainMessage(error: ServerException): String {
        val errorMessage = error.getDirectMessage() ?: error.localizedMessage
        return errorMessage.removeUnderscoreSolIfUsdc()
    }

    fun isMinimumAmountValid(response: MoonpayBuyCurrencyResponse): Boolean {
        val buyCurrency = response.baseCurrency
        val isFiatCurrency = buyCurrency.code in FIAT_CURRENCY_CODES
        val isGreaterThenMin = response.totalAmount.isMoreThan(MIN_FIAT_AMOUNT.toBigDecimal())
        return if (isFiatCurrency) isGreaterThenMin else true
    }

    fun isMinimumAmountException(error: ServerException): Boolean {
        return error.errorCode == ErrorCode.BAD_REQUEST && error.getDirectMessage()
            ?.startsWith("Minimum purchase") == true
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
