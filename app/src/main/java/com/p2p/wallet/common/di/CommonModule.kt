package com.p2p.wallet.common.di

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.user.UserModule.createLoggingInterceptor
import org.koin.dsl.module

object CommonModule : InjectionModule {

    override fun create() = module {
        single { EnvironmentManager(get(), get(), createLoggingInterceptor("RpcClient")) }
        single { get<EnvironmentManager>().getClient() }
    }
}