package org.p2p.wallet.rpc

import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.rpc.api.RpcAccountApi
import org.p2p.wallet.rpc.api.RpcAmountApi
import org.p2p.wallet.rpc.api.RpcBalanceApi
import org.p2p.wallet.rpc.api.RpcBlockHashApi
import org.p2p.wallet.rpc.api.RpcSignatureApi
import org.p2p.wallet.rpc.api.RpcTokenApi
import org.p2p.wallet.rpc.api.RpcTransactionApi
import org.p2p.wallet.rpc.interactor.CloseInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRemoteRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.account.RpcAccountRemoteRepository
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountInMemoryRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountLocalRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRemoteRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockHashRemoteRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockHashRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRemoteRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.rpc.repository.token.RpcTokenRemoteRepository
import org.p2p.wallet.rpc.repository.token.RpcTokenRepository
import org.p2p.wallet.rpc.repository.transaction.RpcTransactionRemoteRepository
import org.p2p.wallet.rpc.repository.transaction.RpcTransactionRepository
import retrofit2.Retrofit

object RpcModule : InjectionModule {

    const val RPC_QUALIFIER = "RPC_QUALIFIER"

    override fun create() = module {
        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcAccountApi::class.java)
            RpcAccountRemoteRepository(api)
        } bind RpcAccountRepository::class

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcAmountApi::class.java)
            RpcAmountRemoteRepository(api)
        } bind RpcAmountRepository::class

        single { RpcAmountInMemoryRepository() } bind RpcAmountLocalRepository::class

        factory { RpcAmountInteractor(get(), get()) }

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcBalanceApi::class.java)
            RpcBalanceRemoteRepository(api)
        } bind RpcBalanceRepository::class

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcBlockHashApi::class.java)
            RpcBlockHashRemoteRepository(api)
        } bind RpcBlockHashRepository::class

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcSignatureApi::class.java)
            RpcSignatureRemoteRepository(api)
        } bind RpcSignatureRepository::class

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcTokenApi::class.java)
            RpcTokenRemoteRepository(api)
        } bind RpcTokenRepository::class

        factory {
            val api = get<Retrofit>(named(RPC_QUALIFIER)).create(RpcTransactionApi::class.java)
            RpcTransactionRemoteRepository(api)
        } bind RpcTransactionRepository::class

        factory {
            CloseInteractor(get(), get(), get())
        }
        factory {
            TransactionInteractor(get(), get(), get(), get())
        }
    }
}