package com.p2p.wowlet.auth

import com.wowlet.domain.usecases.AuthInteractor
import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { AuthInteractor(get()) }
    }
}