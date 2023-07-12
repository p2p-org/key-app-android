package org.p2p.wallet.user

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.user.api.SolanaApi
import org.p2p.wallet.user.interactor.TokenMetadataInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.user.repository.UserAccountRemoteRepository
import org.p2p.wallet.user.repository.UserAccountRepository
import org.p2p.wallet.user.repository.UserInMemoryRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRemoteRepository
import org.p2p.wallet.user.repository.UserRepository
import org.p2p.wallet.user.repository.UserTokensDatabaseRepository
import org.p2p.wallet.user.repository.UserTokensLocalRepository
import org.p2p.wallet.user.repository.UserTokensRemoteRepository
import org.p2p.wallet.user.repository.UserTokensRepository

object UserModule : InjectionModule {

    override fun create() = module {
        single {
            getRetrofit(
                baseUrl = androidContext().getString(R.string.solanaTokensBaseUrl),
                tag = "SolanaApi",
                interceptor = null,
            )
                .create(SolanaApi::class.java)
        }

        factoryOf(::UserRemoteRepository) bind UserRepository::class
        singleOf(::UserAccountRemoteRepository) bind UserAccountRepository::class
        singleOf(::UserInMemoryRepository) bind UserLocalRepository::class

        factory {
            UserInteractor(
                userLocalRepository = get(),
                userTokensRepository = get(),
                homeLocalRepository = get(),
                recipientsLocalRepository = get(),
                rpcRepository = get(),
                sharedPreferences = get(),
                tokenServiceRepository = get()
            )
        }

        singleOf(::UserTokensDatabaseRepository) bind UserTokensLocalRepository::class
        factory { UserTokensInteractor(get(), get(), get(), get()) }
        singleOf(::UserTokensRemoteRepository) bind UserTokensRepository::class
        singleOf(::TokenMetadataInteractor)
    }
}
