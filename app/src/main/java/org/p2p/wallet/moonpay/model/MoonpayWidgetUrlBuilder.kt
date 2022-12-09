package org.p2p.wallet.moonpay.model

import android.net.Uri
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider

private const val QUERY_API_KEY = "apiKey"
private const val QUERY_CURRENCY_CODE = "currencyCode"
private const val QUERY_BASE_CURRENCY_CODE = "baseCurrencyCode"
private const val QUERY_BASE_CURRENCY_AMOUNT = "baseCurrencyAmount"
private const val QUERY_LOCK_AMOUNT = "lockAmount"
private const val QUERY_WALLET_ADDRESS = "walletAddress"
private const val QUERY_PAYMENT_METHOD = "paymentMethod"

class MoonpayWidgetUrlBuilder(
    private val networkServicesUrlProvider: NetworkServicesUrlProvider
) {
    fun buildBuyWidgetUrl(
        amount: String,
        walletAddress: String,
        tokenSymbol: String,
        currencyCode: String,
        paymentMethod: String? = null
    ): String {
        val builder = Uri.parse(networkServicesUrlProvider.loadMoonpayEnvironment().buyWidgetUrl)
            .buildUpon()
            .appendQueryParameter(QUERY_API_KEY, BuildConfig.moonpayKey)
            .appendQueryParameter(QUERY_CURRENCY_CODE, tokenSymbol)
            .appendQueryParameter(QUERY_BASE_CURRENCY_AMOUNT, amount)
            .appendQueryParameter(QUERY_BASE_CURRENCY_CODE, currencyCode)
            .appendQueryParameter(QUERY_LOCK_AMOUNT, "false")
            .appendQueryParameter(QUERY_WALLET_ADDRESS, walletAddress)
            .apply { if (paymentMethod != null) appendQueryParameter(QUERY_PAYMENT_METHOD, paymentMethod) }

        return builder.build().toString()
    }
}
