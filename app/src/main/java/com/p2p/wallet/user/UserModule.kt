package com.p2p.wallet.user

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.user.repository.UserInMemoryRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import com.p2p.wallet.user.repository.UserRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

object UserModule : InjectionModule {

    override fun create() = module {
        single {
            val compareApi = get<Retrofit>(named("cryptocompare")).create(CompareApi::class.java)
            UserRepositoryImpl(get(), compareApi, get(), get())
        } bind UserRepository::class

        single { UserInMemoryRepository() } bind UserLocalRepository::class
        single { UserInteractor(get(), get(), get(), get()) }
    }
}