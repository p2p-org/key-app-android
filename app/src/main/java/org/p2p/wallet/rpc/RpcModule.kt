package org.p2p.wallet.rpc

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.kits.transaction.mapper.TransactionDetailsNetworkMapper
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.rpc.api.RpcApi
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.ServerErrorInterceptor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.RpcAmountRemoteRepository
import org.p2p.wallet.rpc.repository.RpcAmountRepository
import org.p2p.wallet.rpc.repository.RpcRemoteRepository
import org.p2p.wallet.rpc.repository.RpcRepository

object RpcModule : InjectionModule {

    override fun create(): Module = module {
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

            RpcRemoteRepository(
                serumApi = serumRpcApi,
                mainnetApi = mainnetRpcApi,
                rpcpoolRpcApi = rpcpoolRpcApi,
                testnetApi = testnetRpcApi,
                transactionDetailsMapper = TransactionDetailsNetworkMapper(),
                environmentManager = get()
            )
        } bind RpcRepository::class

        factory { TokenInteractor(get(), get(), get()) }
        factory { TransactionInteractor(get(), get(), get()) }

        single { RpcAmountRemoteRepository(get()) } bind RpcAmountRepository::class
    }
}