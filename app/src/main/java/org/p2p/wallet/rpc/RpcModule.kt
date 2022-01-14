package org.p2p.wallet.rpc

import org.p2p.wallet.infrastructure.network.feerelayer.FeeRelayerInterceptor
import android.content.Context
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
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

    const val FEE_RELAYER_QUALIFIER = "https://fee-relayer.solana.p2p.org"

    override fun create() = module {
        single {
            val serverErrorInterceptor = ServerErrorInterceptor(get())
            val serum = getRetrofit(Environment.SOLANA.endpoint, interceptor = serverErrorInterceptor)
            val serumRpcApi = serum.create(RpcApi::class.java)

            val mainnet = getRetrofit(Environment.MAINNET.endpoint, interceptor = serverErrorInterceptor)
            val mainnetRpcApi = mainnet.create(RpcApi::class.java)

            /* This string is in gitignore and it's null when CI/CD runs some actions */
            val rpcPoolApiKey = BuildConfig.rpcPoolApiKey
            val baseUrl = if (rpcPoolApiKey.isNotBlank()) {
                "${Environment.RPC_POOL.endpoint}$rpcPoolApiKey/"
            } else {
                Environment.RPC_POOL.endpoint
            }
            val rpcpool = getRetrofit(baseUrl, interceptor = serverErrorInterceptor)
            val rpcpoolRpcApi = rpcpool.create(RpcApi::class.java)

            val testnet = getRetrofit(Environment.DEVNET.endpoint, interceptor = serverErrorInterceptor)
            val testnetRpcApi = testnet.create(RpcApi::class.java)

            RpcRemoteRepository(serumRpcApi, mainnetRpcApi, rpcpoolRpcApi, testnetRpcApi, get())
        } bind RpcRepository::class

        single(named(FEE_RELAYER_QUALIFIER)) {
            val baseUrl = get<Context>().getString(R.string.feeRelayerBaseUrl)
            getRetrofit(baseUrl, "FeeRelayer", interceptor = FeeRelayerInterceptor(get()))
        }

        single {
            val retrofit = get<Retrofit>(named(FEE_RELAYER_QUALIFIER))
            val api = retrofit.create(FeeRelayerApi::class.java)
            FeeRelayerRemoteRepository(api, get())
        } bind FeeRelayerRepository::class
    }
}