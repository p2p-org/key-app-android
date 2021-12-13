package org.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.solanaj.rpc.Environment

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private var onChanged: ((Environment) -> Unit)? = null

    fun getMoonpayUrl(): String {
        val baseUrl = context.getString(R.string.moonpayBaseUrl)
        val apiKey = BuildConfig.moonpayKey

        return Uri.Builder()
            .scheme("https")
            .authority(baseUrl)
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("currencyCode", "eth")
            .appendQueryParameter("baseCurrencyAmount", "100")
            .appendQueryParameter("lockAmount", "false")
            .build()
            .toString()
    }

    fun setOnEnvironmentListener(onChanged: (Environment) -> Unit) {
        this.onChanged = onChanged
    }

    fun loadEnvironment(): Environment {
        val url = sharedPreferences.getString(KEY_BASE_URL, Environment.RPC_POOL.endpoint).orEmpty()
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
        Environment.RPC_POOL.endpoint -> Environment.RPC_POOL
        else -> throw IllegalStateException("Unknown endpoint $url")
    }
}