package org.p2p.wallet.rpc

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.rpc.RPC_RETROFIT_QUALIFIER
import org.p2p.solanaj.kits.renBridge.renVM.RenVMRepository
import org.p2p.solanaj.rpc.RenPoolRepository
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.rpc.api.RpcAccountApi
import org.p2p.wallet.rpc.api.RpcAmountApi
import org.p2p.wallet.rpc.api.RpcBalanceApi
import org.p2p.wallet.rpc.api.RpcBlockhashApi
import org.p2p.wallet.rpc.api.RpcSignatureApi
import org.p2p.wallet.rpc.api.RpcTransactionApi
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
import org.p2p.wallet.rpc.repository.history.RpcTransactionRemoteRepository
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.rpc.repository.ren.RenPoolApi
import org.p2p.wallet.rpc.repository.ren.RenPoolRemoteRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRemoteRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.rpc.repository.solana.RpcSolanaApi
import org.p2p.wallet.rpc.repository.solana.RpcSolanaRemoteRepository

object RpcModule : InjectionModule {

    const val REN_POOL_RETROFIT_QUALIFIER = "REN_POOL_RETROFIT_QUALIFIER"

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
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcTransactionApi::class.java)
            RpcTransactionRemoteRepository(api)
        } bind RpcTransactionRepository::class

        single {
            val api = get<Retrofit>(named(RPC_RETROFIT_QUALIFIER)).create(RpcSolanaApi::class.java)
            RpcSolanaRemoteRepository(api, get(), get())
        } bind RpcSolanaRepository::class

        factory {
            CloseAccountInteractor(get(), get(), get())
        }
        factory {
            TransactionInteractor(get(), get(), get(), get())
        }
        factoryOf(::TokenInteractor)

        factory { RpcSolanaInteractor(get(), get<NetworkEnvironmentManager>().loadRpcEnvironment(), get<AppScope>()) }

        factory { RenVMRepository(get()) }

        single {
            val api = get<Retrofit>(named(REN_POOL_RETROFIT_QUALIFIER)).create(RenPoolApi::class.java)
            RenPoolRemoteRepository(api, get())
        } bind RenPoolRepository::class
    }
}
