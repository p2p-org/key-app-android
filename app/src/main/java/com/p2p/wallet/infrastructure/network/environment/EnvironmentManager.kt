package com.p2p.wallet.infrastructure.network.environment

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.solanaj.rpc.Environment

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val sharedPreferences: SharedPreferences
) {

    private var onChanged: ((Environment) -> Unit)? = null

    fun setOnEnvironmentListener(onChanged: (Environment) -> Unit) {
        this.onChanged = onChanged
    }

    fun loadEnvironment(): Environment {
        val url = sharedPreferences.getString(KEY_BASE_URL, Environment.SOLANA.endpoint).orEmpty()
        return parse(url)
    }

    fun saveEnvironment(newEnvironment: String) {
        sharedPreferences.edit { putString(KEY_BASE_URL, newEnvironment) }

        onChanged?.invoke(parse(newEnvironment))
    }

    private fun parse(url: String): Environment = when (url) {
        Environment.TESTNET.endpoint -> Environment.TESTNET
        Environment.DEVNET.endpoint -> Environment.DEVNET
        Environment.MAINNET.endpoint -> Environment.MAINNET
        else -> Environment.SOLANA
    }
}