package com.p2p.wowlet.auth

import com.p2p.wowlet.auth.interactor.AuthInteractor
import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { AuthInteractor(get()) }
    }
}