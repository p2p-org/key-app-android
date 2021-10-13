package com.p2p.wallet.rpc

import android.content.Context
import com.p2p.wallet.R
import com.p2p.wallet.auth.AuthModule
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import com.p2p.wallet.infrastructure.network.interceptor.ServerErrorInterceptor
import com.p2p.wallet.rpc.api.FeeRelayerApi
import com.p2p.wallet.rpc.api.RpcApi
import com.p2p.wallet.rpc.repository.FeeRelayerRemoteRepository
import com.p2p.wallet.rpc.repository.FeeRelayerRepository
import com.p2p.wallet.rpc.repository.RpcRemoteRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.rpc.Environment
import retrofit2.Retrofit

object RpcModule : InjectionModule {

    override fun create() = module {
        single {
            val serverErrorInterceptor = ServerErrorInterceptor(get())
            val serum = getRetrofit(Environment.SOLANA.endpoint, interceptor = serverErrorInterceptor)
            val serumRpcApi = serum.create(RpcApi::class.java)

            val mainnet = getRetrofit(Environment.MAINNET.endpoint, interceptor = serverErrorInterceptor)
            val mainnetRpcApi = mainnet.create(RpcApi::class.java)

            val rpcpool = getRetrofit(Environment.RPC_POOL.endpoint, interceptor = serverErrorInterceptor)
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