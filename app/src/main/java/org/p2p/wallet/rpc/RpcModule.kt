package org.p2p.wallet.rpc

import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.interceptor.ServerErrorInterceptor
import org.p2p.wallet.rpc.api.RpcAccountApi
import org.p2p.wallet.rpc.api.RpcAmountApi
import org.p2p.wallet.rpc.api.RpcApiBuilder
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
import org.p2p.wallet.rpc.repository.balance.RpcBalanceInMemoryRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceInteractor
import org.p2p.wallet.rpc.repository.balance.RpcBalanceLocalRepository
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

object RpcModule : InjectionModule {

    override fun create() = module {
        // Rpc account api
        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val accountApi = builder.load(RpcAccountApi::class.java)
            RpcAccountRemoteRepository(accountApi)
        } bind RpcAccountRepository::class

        // Rpc amount api
        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val amountApi = builder.load(RpcAmountApi::class.java)
            RpcAmountRemoteRepository(amountApi)
        } bind RpcAmountRepository::class

        single { RpcAmountInMemoryRepository() } bind RpcAmountLocalRepository::class

        factory { RpcAmountInteractor(get(), get()) }

        // Rpc balance api
        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val balanceApi = builder.load(RpcBalanceApi::class.java)
            RpcBalanceRemoteRepository(balanceApi)
        } bind RpcBalanceRepository::class

        single { RpcBalanceInMemoryRepository() } bind RpcBalanceLocalRepository::class

        factory { RpcBalanceInteractor(get(), get()) }

        // Rpc blockhash api
        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val balanceApi = builder.load(RpcBlockHashApi::class.java)
            RpcBlockHashRemoteRepository(balanceApi)
        } bind RpcBlockHashRepository::class

        // Rpc signature api
        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val signatureApi = builder.load(RpcSignatureApi::class.java)
            RpcSignatureRemoteRepository(signatureApi)
        } bind RpcSignatureRepository::class

        // Rpc token api

        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val tokenApi = builder.load(RpcTokenApi::class.java)
            RpcTokenRemoteRepository(tokenApi)
        } bind RpcTokenRepository::class

        factory {
            val environment = get<EnvironmentManager>().loadEnvironment()
            val interceptor = ServerErrorInterceptor(get())
            val builder = RpcApiBuilder.with(this).set(environment)
                .addInterceptor(interceptor)
            val transactionApi = builder.load(RpcTransactionApi::class.java)
            val poolTransactionApi = builder.set(Environment.RPC_POOL).load(RpcTransactionApi::class.java)
            RpcTransactionRemoteRepository(rpcApi = transactionApi, rpcPoolApi = poolTransactionApi)
        } bind RpcTransactionRepository::class

        factory {
            CloseInteractor(get(), get(), get())
        }

        factory {
            TransactionInteractor(get(), get(), get(), get())
        }
    }
}