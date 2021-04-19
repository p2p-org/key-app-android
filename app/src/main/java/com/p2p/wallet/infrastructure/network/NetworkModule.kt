package com.p2p.wallet.infrastructure.network

import com.p2p.wallet.common.di.InjectionModule
import org.koin.dsl.module

object NetworkModule : InjectionModule {

    override fun create() = module {
        single { PublicKeyProvider(get()) }
    }
}