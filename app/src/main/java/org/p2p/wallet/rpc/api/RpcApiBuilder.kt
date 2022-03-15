package org.p2p.wallet.rpc.api

import okhttp3.Interceptor
import org.koin.core.scope.Scope
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit

object RpcApiBuilder {

    var currentEnvironment: Environment? = null

    private var baseUrl: String = ""

    private var interceptor: Interceptor? = null

    private var scope: Scope? = null

    fun with(scope: Scope): RpcApiBuilder {
        this.scope = scope
        return this
    }

    fun set(environment: Environment): RpcApiBuilder {
        currentEnvironment = environment
        onEnvironmentChanged(environment)
        return this
    }

    fun <T> load(api: Class<T>): T {
        val retrofit = scope?.getRetrofit(
            baseUrl,
            interceptor = interceptor
        ) ?: throw IllegalStateException("You must set scope before launch this method")
        return retrofit.create(api)
    }

    fun addInterceptor(interceptor: Interceptor): RpcApiBuilder {
        this.interceptor = interceptor
        return this
    }

    private fun onEnvironmentChanged(environment: Environment) {
        baseUrl = when (environment) {
            Environment.MAINNET -> environment.endpoint
            Environment.SOLANA -> environment.endpoint
            Environment.DEVNET -> environment.endpoint
            Environment.RPC_POOL -> {
                val rpcPoolApiKey = BuildConfig.rpcPoolApiKey
                if (rpcPoolApiKey.isNotBlank()) {
                    "${Environment.RPC_POOL.endpoint}$rpcPoolApiKey/"
                } else {
                    Environment.RPC_POOL.endpoint
                }
            }
        }
    }
}