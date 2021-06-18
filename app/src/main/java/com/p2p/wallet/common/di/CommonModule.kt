package com.p2p.wallet.common.di

import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.koin.dsl.module

object CommonModule : InjectionModule {

    override fun create() = module {
        single { EnvironmentManager(get()) }
    }
}