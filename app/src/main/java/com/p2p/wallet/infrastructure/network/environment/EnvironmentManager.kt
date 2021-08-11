package com.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import org.p2p.solanaj.rpc.Environment

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val TRANSAK_URL = "https://global.transak.com"
    }

    private var onChanged: ((Environment) -> Unit)? = null

    fun getTransakUrl(): String {
        val apiKey = context.getString(R.string.transakApiKey)
        val environment = if (BuildConfig.DEBUG) "STAGING" else "PRODUCTION"
        val domainName = "https://p2p.org"
        return "$TRANSAK_URL?apiKey=$apiKey&environment=$environment&hostURL=$domainName"
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
        else -> Environment.SOLANA
    }
}