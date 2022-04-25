package org.p2p.wallet.rpc

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.rpc.api.RpcAccountApi
import org.p2p.wallet.rpc.api.RpcAmountApi
import org.p2p.wallet.rpc.api.RpcBalanceApi
import org.p2p.wallet.rpc.api.RpcBlockhashApi
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.rpc.api.RpcSignatureApi
import org.p2p.wallet.rpc.interactor.CloseAccountInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.account.RpcAccountRemoteRepository
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountRemoteRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRemoteRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRemoteRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcHistoryRemoteRepository
import org.p2p.wallet.rpc.repository.history.RpcHistoryRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRemoteRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import retrofit2.Retrofit

object RpcModule : InjectionModule {

    const val RPC_RETROFIT_QUALIFIER = "RPC_RETROFIT_QUALIFIER"

    override fun create(): Module = module {
        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcAccountApi::class.java)
            RpcAccountRemoteRepository(api)
        } bind RpcAccountRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcAmountApi::class.java)
            RpcAmountRemoteRepository(api)
        } bind RpcAmountRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcBalanceApi::class.java)
            RpcBalanceRemoteRepository(api)
        } bind RpcBalanceRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcBlockhashApi::class.java)
            RpcBlockhashRemoteRepository(api)
        } bind RpcBlockhashRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcSignatureApi::class.java)
            RpcSignatureRemoteRepository(api)
        } bind RpcSignatureRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcHistoryApi::class.java)
            RpcHistoryRemoteRepository(api)
        } bind RpcHistoryRepository::class

        factory {
            CloseAccountInteractor(get(), get(), get())
        }
        factory {
            TransactionInteractor(get(), get(), get(), get())
        }
        factory { TokenInteractor(get(), get(), get(), get()) }
    }
}
