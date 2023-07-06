package org.p2p.wallet.moonpay.model

import android.net.Uri
import org.p2p.core.BuildConfig.moonpayKey
import org.p2p.core.utils.Constants
import org.p2p.core.network.environment.MoonpayEnvironment
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.crypto.Base58String

private const val QUERY_API_KEY = "apiKey"
private const val QUERY_CURRENCY_CODE = "currencyCode"
private const val QUERY_BASE_CURRENCY_CODE = "baseCurrencyCode"
private const val QUERY_BASE_CURRENCY_AMOUNT = "baseCurrencyAmount"
private const val QUERY_LOCK_AMOUNT = "lockAmount"
private const val QUERY_WALLET_ADDRESS = "walletAddress"
private const val QUERY_PAYMENT_METHOD = "paymentMethod"
private const val QUERY_REFUND_WALLET_ADDRESS = "refundWalletAddress"
private const val QUERY_EXTERNAL_TRANSACTION_ID = "externalTransactionId"
private const val QUERY_QUOTE_CURRENCY_CODE = "quoteCurrencyCode"
private const val QUERY_EXTERNAL_CUSTOMER_ID = "externalCustomerId"

class MoonpayWidgetUrlBuilder(
    private val networkServicesUrlProvider: NetworkServicesUrlProvider
) {
    private val moonpayNetworkEnvironment: MoonpayEnvironment
        get() = networkServicesUrlProvider.loadMoonpayEnvironment()

    fun buildBuyWidgetUrl(
        amount: String,
        walletAddress: String,
        tokenSymbol: String,
        currencyCode: String,
        paymentMethod: String? = null
    ): String {
        return Uri.parse(networkServicesUrlProvider.loadMoonpayEnvironment().buyWidgetUrl)
            .buildUpon()
            .appendQueryParameter(QUERY_API_KEY, moonpayKey)
            .appendQueryParameter(QUERY_CURRENCY_CODE, tokenSymbol)
            .appendQueryParameter(QUERY_BASE_CURRENCY_AMOUNT, amount)
            .appendQueryParameter(QUERY_BASE_CURRENCY_CODE, currencyCode)
            .appendQueryParameter(QUERY_LOCK_AMOUNT, "false")
            .appendQueryParameter(QUERY_WALLET_ADDRESS, walletAddress)
            .apply { if (paymentMethod != null) appendQueryParameter(QUERY_PAYMENT_METHOD, paymentMethod) }
            .build()
            .toString()
    }

    fun buildSellWidgetUrl(
        tokenSymbol: String,
        userAddress: Base58String,
        externalCustomerId: Base58String,
        fiatSymbol: String,
        tokenAmountToSell: String,
    ): String {
        val validatedTokenSymbol = if (moonpayNetworkEnvironment.isSandboxEnabled) {
            Constants.BTC_SYMBOL // sandbox supports only BTC
        } else {
            tokenSymbol
        }

        return Uri.parse(moonpayNetworkEnvironment.sellWidgetUrl)
            .buildUpon()
            .appendQueryParameter(QUERY_API_KEY, moonpayNetworkEnvironment.moonpayApiKey)
            .appendQueryParameter(QUERY_BASE_CURRENCY_CODE, validatedTokenSymbol.lowercase())
            .appendQueryParameter(QUERY_REFUND_WALLET_ADDRESS, userAddress.base58Value)
            .appendQueryParameter(QUERY_QUOTE_CURRENCY_CODE, fiatSymbol)
            .appendQueryParameter(QUERY_BASE_CURRENCY_AMOUNT, tokenAmountToSell)
            .appendQueryParameter(QUERY_EXTERNAL_CUSTOMER_ID, externalCustomerId.base58Value)
            .build()
            .toString()
    }
}
