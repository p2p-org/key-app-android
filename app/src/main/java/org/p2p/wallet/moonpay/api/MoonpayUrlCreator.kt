package org.p2p.wallet.moonpay.api

import android.net.Uri
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL

private const val QUERY_API_KEY = "apiKey"
private const val QUERY_CURRENCY_CODE = "currencyCode"
private const val QUERY_BASE_CURRENCY_CODE = "baseCurrencyCode"
private const val QUERY_BASE_CURRENCY_AMOUNT = "baseCurrencyAmount"
private const val QUERY_LOCK_AMOUNT = "lockAmount"
private const val QUERY_WALLET_ADDRESS = "walletAddress"

class MoonpayUrlCreator(private val moonpayWalletDomain: String, private val moonpayApiKey: String) {
    fun create(amount: String, walletAddress: String, currencyCode: String): String {
        return Uri.Builder()
            .scheme("https")
            .authority(moonpayWalletDomain)
            .appendQueryParameter(QUERY_API_KEY, moonpayApiKey)
            .appendQueryParameter(QUERY_CURRENCY_CODE, currencyCode)
            .appendQueryParameter(QUERY_BASE_CURRENCY_AMOUNT, amount)
            .appendQueryParameter(QUERY_BASE_CURRENCY_CODE, USD_READABLE_SYMBOL.lowercase())
            .appendQueryParameter(QUERY_LOCK_AMOUNT, "false")
            .appendQueryParameter(QUERY_WALLET_ADDRESS, walletAddress)
            .build()
            .toString()
    }
}
