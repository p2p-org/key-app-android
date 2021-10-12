package com.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.main.model.Token
import org.p2p.solanaj.rpc.Environment

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private var onChanged: ((Environment) -> Unit)? = null

    fun getTransakUrl(token: Token.Active): String {
        val apiKey = context.getString(R.string.transakApiKey)
        val baseUrl = context.getString(R.string.transakBaseUrl)
        val symbol = if (token.isUSDC) Token.USDC_SYMBOL else Token.SOL_SYMBOL
        val environment = if (BuildConfig.DEBUG) "staging" else "production"
        return Uri.Builder()
            .scheme("https")
            .authority(baseUrl)
            .appendQueryParameter("networks", "mainnet")
            .appendQueryParameter("environment", environment)
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("defaultCryptoCurrency", symbol)
            .appendQueryParameter("walletAddress", token.publicKey)
            .appendQueryParameter("disableWalletAddressForm", "true")
            .appendQueryParameter("hideMenu", "true")
            .build()
            .toString()
    }

    fun setOnEnvironmentListener(onChanged: (Environment) -> Unit) {
        this.onChanged = onChanged
    }

    fun loadEnvironment(): Environment {
        val url = sharedPreferences.getString(KEY_BASE_URL, Environment.SOLANA.endpoint).orEmpty()
        return parse(url)
    }

    fun saveEnvironment(newEnvironment: Environment) {
        sharedPreferences.edit { putString(KEY_BASE_URL, newEnvironment.endpoint) }

        onChanged?.invoke(newEnvironment)
    }

    private fun parse(url: String): Environment = when (url) {
        Environment.MAINNET.endpoint -> Environment.MAINNET
        Environment.DEVNET.endpoint -> Environment.DEVNET
        Environment.SOLANA.endpoint -> Environment.SOLANA
        else -> throw IllegalStateException("Unknown endpoint $url")
    }
}