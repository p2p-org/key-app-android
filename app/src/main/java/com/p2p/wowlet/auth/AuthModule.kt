package com.p2p.wowlet.auth

import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { AuthInteractor(get()) }
    }
}