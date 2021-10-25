package org.p2p.wallet.rpc

import android.content.Context
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.R
import org.p2p.wallet.auth.AuthModule
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.ServerErrorInterceptor
import org.p2p.wallet.rpc.api.FeeRelayerApi
import org.p2p.wallet.rpc.api.RpcApi
import org.p2p.wallet.rpc.repository.FeeRelayerRemoteRepository
import org.p2p.wallet.rpc.repository.FeeRelayerRepository
import org.p2p.wallet.rpc.repository.RpcRemoteRepository
import org.p2p.wallet.rpc.repository.RpcRepository
import retrofit2.Retrofit

object RpcModule : InjectionModule {

    override fun create() = module {
        single {
            val serverErrorInterceptor = ServerErrorInterceptor(get())
            val serum = getRetrofit(Environment.SOLANA.endpoint, interceptor = serverErrorInterceptor)
            val serumRpcApi = serum.create(RpcApi::class.java)

            val mainnet = getRetrofit(Environment.MAINNET.endpoint, interceptor = serverErrorInterceptor)
            val mainnetRpcApi = mainnet.create(RpcApi::class.java)

            /* This string is in gitignore and it's null when CI/CD runs some actions */
            val rpcPoolApiKey = if (R.string.rpcPoolApiKey != null) {
                get<Context>().getString(R.string.rpcPoolApiKey)
            } else ""
            val baseUrl = String.format(Environment.RPC_POOL.endpoint, rpcPoolApiKey)
            val rpcpool = getRetrofit(baseUrl, interceptor = serverErrorInterceptor)
            val rpcpoolRpcApi = rpcpool.create(RpcApi::class.java)

            val testnet = getRetrofit(Environment.DEVNET.endpoint, interceptor = serverErrorInterceptor)
            val testnetRpcApi = testnet.create(RpcApi::class.java)

            RpcRemoteRepository(serumRpcApi, mainnetRpcApi, rpcpoolRpcApi, testnetRpcApi, get())
        } bind RpcRepository::class

        single(named(AuthModule.RESERVING_USERNAME_QUALIFIER)) {
            val baseUrl = get<Context>().getString(R.string.feeRelayerBaseUrl)
            getRetrofit(baseUrl, "FeeRelayer", interceptor = null)
        }

        single {
            val retrofit = get<Retrofit>(named(AuthModule.RESERVING_USERNAME_QUALIFIER))
            val api = retrofit.create(FeeRelayerApi::class.java)
            FeeRelayerRemoteRepository(api)
        } bind FeeRelayerRepository::class
    }
}