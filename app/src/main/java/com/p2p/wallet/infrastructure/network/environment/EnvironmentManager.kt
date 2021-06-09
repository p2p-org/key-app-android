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
        client = RpcClient(loadEnvironment(), createOkHttpClient())
    }

    fun loadEnvironment(): String = sharedPreferences.getString(KEY_BASE_URL, Environment.MAINNET.endpoint).orEmpty()

    fun saveEnvironment(environment: String) {
        sharedPreferences.edit { putString(KEY_BASE_URL, environment) }
        client.updateEndpoint(environment)
    }

    fun getClient(): RpcClient = client

    private fun createOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(TIMEOUT_INTERVAL, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(DataHubInterceptor(context))
            .build()
}