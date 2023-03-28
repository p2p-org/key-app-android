package org.p2p.wallet.user

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.repository.UserAccountRemoteRepository
import org.p2p.wallet.user.repository.UserAccountRepository
import org.p2p.wallet.user.repository.UserInMemoryRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRemoteRepository
import org.p2p.wallet.user.repository.UserRepository

object UserModule : InjectionModule {

    override fun create() = module {
        single<SolanaApi> {
            getRetrofit(
                baseUrl = androidContext().getString(R.string.solanaTokensBaseUrl),
                tag = "SolanaApi",
                interceptor = null,
            )
                .create(SolanaApi::class.java)
        }

        factoryOf(::UserRemoteRepository) bind UserRepository::class
        singleOf(::UserAccountRemoteRepository) bind UserAccountRepository::class
        singleOf(::UserInMemoryRepository) bind UserLocalRepository ::class

        factoryOf(::UserInteractor)
    }
}
