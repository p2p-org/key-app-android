package org.p2p.wallet.moonpay.api

import android.net.Uri

private const val QUERY_API_KEY = "apiKey"
private const val QUERY_CURRENCY_CODE = "currencyCode"
private const val QUERY_BASE_CURRENCY_CODE = "baseCurrencyCode"
private const val QUERY_BASE_CURRENCY_AMOUNT = "baseCurrencyAmount"
private const val QUERY_LOCK_AMOUNT = "lockAmount"
private const val QUERY_WALLET_ADDRESS = "walletAddress"
private const val QUERY_PAYMENT_METHOD = "paymentMethod"

object MoonpayUrlBuilder {
    fun build(
        moonpayWalletDomain: String,
        moonpayApiKey: String,
        amount: String,
        walletAddress: String,
        tokenSymbol: String,
        currencyCode: String,
        paymentMethod: String? = null
    ): String {
        val builder = Uri.Builder()
            .scheme("https")
            .authority(moonpayWalletDomain)
            .appendQueryParameter(QUERY_API_KEY, moonpayApiKey)
            .appendQueryParameter(QUERY_CURRENCY_CODE, tokenSymbol)
            .appendQueryParameter(QUERY_BASE_CURRENCY_AMOUNT, amount)
            .appendQueryParameter(QUERY_BASE_CURRENCY_CODE, currencyCode)
            .appendQueryParameter(QUERY_LOCK_AMOUNT, "false")
            .appendQueryParameter(QUERY_WALLET_ADDRESS, walletAddress)
        paymentMethod?.let {
            builder.appendQueryParameter(QUERY_PAYMENT_METHOD, it)
        }
        return builder
            .build()
            .toString()
    }
}
