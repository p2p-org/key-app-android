package com.p2p.wallet.infrastructure.network.environment

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.p2p.solanaj.rpc.Environment
import org.p2p.solanaj.rpc.RpcClient
import java.util.concurrent.TimeUnit

private const val KEY_BASE_URL = "KEY_BASE_URL"

class EnvironmentManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val loggingInterceptor: HttpLoggingInterceptor
) {

    companion object {
        private const val TIMEOUT_INTERVAL = 90L
    }

    private val client: RpcClient

    init {
        client = createClient()
    }

    fun loadEnvironment(): String = sharedPreferences.getString(KEY_BASE_URL, Environment.MAINNET.endpoint).orEmpty()

    fun saveEnvironment(environment: String) {
        sharedPreferences.edit { putString(KEY_BASE_URL, environment) }
        client.updateEndpoint(environment)
    }

    fun getClient(): RpcClient = client

    private fun createClient(): RpcClient =
        when (val environment = loadEnvironment()) {
            Environment.DATAHUB.endpoint -> RpcClient(environment, createOkHttpClient(true))
            Environment.SOLANA.endpoint -> RpcClient(environment, createOkHttpClient())
            else -> RpcClient(environment, createOkHttpClient())
        }

    private fun createOkHttpClient(isDataHub: Boolean = false): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(TIMEOUT_INTERVAL, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .apply { if (isDataHub) addInterceptor(DataHubInterceptor(context)) }
            .build()
}