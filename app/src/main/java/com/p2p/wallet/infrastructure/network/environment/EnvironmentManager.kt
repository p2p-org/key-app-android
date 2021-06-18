package com.p2p.wallet.infrastructure.network.environment

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.solanaj.rpc.Environment

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val MAINNET = "mainnet"
        const val SERUM = "serum"
        const val DATAHUB = "datahub"
    }

    fun loadEnvironment(): Environment {
        val url = sharedPreferences.getString(KEY_BASE_URL, Environment.MAINNET.endpoint).orEmpty()
        return parse(url)
    }

    fun saveEnvironment(environment: String) {
        sharedPreferences.edit { putString(KEY_BASE_URL, environment) }
    }

    fun getCurrentQualifier(): String = when (loadEnvironment()) {
        Environment.MAINNET -> MAINNET
        Environment.PROJECT_SERUM -> SERUM
        Environment.DATAHUB -> DATAHUB
    }

    private fun parse(url: String): Environment = when (url) {
        Environment.DATAHUB.endpoint -> Environment.DATAHUB
        Environment.PROJECT_SERUM.endpoint -> Environment.PROJECT_SERUM
        else -> Environment.MAINNET
    }
}