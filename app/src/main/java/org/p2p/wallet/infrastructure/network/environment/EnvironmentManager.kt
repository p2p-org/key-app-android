package org.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.utils.Constants.USD_READABLE_SYMBOL

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val FAKE_ETH_ADDRESS = "0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae"
    }

    private var onChanged: ((Environment) -> Unit)? = null

    fun isDevnet(): Boolean = loadEnvironment() == Environment.DEVNET

    fun isMainnet(): Boolean =
        loadEnvironment() in listOf(
            Environment.MAINNET,
            Environment.RPC_POOL,
            Environment.SOLANA
        )

    fun getMoonpayUrl(amount: String): String {
        val baseUrl = context.getString(R.string.moonpayWalletDomain)
        val apiKey = BuildConfig.moonpayKey

        return Uri.Builder()
            .scheme("https")
            .authority(baseUrl)
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("currencyCode", "sol")
            .appendQueryParameter("baseCurrencyAmount", amount)
            .appendQueryParameter("baseCurrencyCode", USD_READABLE_SYMBOL.lowercase())
            .appendQueryParameter("lockAmount", "false")
            // FIXME: fake address will be replaced by real token address, this is for testing
            .appendQueryParameter("walletAddress", FAKE_ETH_ADDRESS)
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