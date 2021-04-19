package com.p2p.wallet.user

import com.p2p.wallet.common.di.InjectionModule
import org.koin.dsl.bind
import org.koin.dsl.module

object UserModule : InjectionModule {

    override fun create() = module {
        single { UserRepositoryImpl(get(), get()) } bind UserRepository::class
        single { UserInteractor(get(), get(), get()) }
    }
}