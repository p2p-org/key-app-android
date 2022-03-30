package org.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import org.p2p.solanaj.rpc.Environment
import org.p2p.solanaj.rpc.RpcEnvironment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL

private const val KEY_BASE_URL = "KEY_BASE_URL"
private const val KEY_RPC_BASE_URL = "KEY_RPC_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private var onChanged: ((Environment) -> Unit)? = null

    fun isDevnet(): Boolean = loadEnvironment() == Environment.DEVNET

    fun isMainnet(): Boolean =
        loadEnvironment() in listOf(
            Environment.MAINNET,
            Environment.RPC_POOL,
            Environment.SOLANA
        )

    fun getMoonpayUrl(
        amount: String,
        publicKey: String,
        currencyCode: String
    ): String {
        val baseUrl = context.getString(R.string.moonpayWalletDomain)
        val apiKey = BuildConfig.moonpayKey

        return Uri.Builder()
            .scheme("https")
            .authority(baseUrl)
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("currencyCode", currencyCode)
            .appendQueryParameter("baseCurrencyAmount", amount)
            .appendQueryParameter("baseCurrencyCode", USD_READABLE_SYMBOL.lowercase())
            .appendQueryParameter("lockAmount", "false")
            .appendQueryParameter("walletAddress", publicKey)
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

    fun loadRpcEnvironment(): RpcEnvironment =
        when (loadEnvironment()) {
            Environment.DEVNET -> RpcEnvironment.DEVNET
            else -> RpcEnvironment.MAINNET
        }

    fun saveEnvironment(newEnvironment: Environment) {
        sharedPreferences.edit { putString(KEY_BASE_URL, newEnvironment.endpoint) }

        val newRpcEnvironment = parseRpc(newEnvironment.endpoint)
        sharedPreferences.edit { putString(KEY_RPC_BASE_URL, newRpcEnvironment.endpoint) }
        onChanged?.invoke(newEnvironment)
    }

    private fun parse(url: String): Environment = when (url) {
        Environment.MAINNET.endpoint -> Environment.MAINNET
        Environment.DEVNET.endpoint -> Environment.DEVNET
        Environment.SOLANA.endpoint -> Environment.SOLANA
        Environment.RPC_POOL.endpoint -> Environment.RPC_POOL
        else -> throw IllegalStateException("Unknown endpoint $url")
    }

    private fun parseRpc(url: String): RpcEnvironment = if (Environment.DEVNET.endpoint == url) {
        RpcEnvironment.DEVNET
    } else {
        RpcEnvironment.MAINNET
    }
}
