package com.wowlet.domain.di

import com.wowlet.domain.interactors.*
import com.wowlet.domain.usecases.*
import org.koin.dsl.module

val interactorsModule = module {
    single<RegLoginInteractor> { RegLoginUseCase(get(),get()) }
    factory <SecretKeyInteractor> { SecretKeyUseCase(get()) }
    single<PinCodeInteractor> { PinCodeUseCase(get()) }
    single<NotificationInteractor> { NotificationUseCase(get()) }
    single<SendCoinInteractor> { SendCoinUseCase(get()) }
    single<DashboardInteractor> { DashBoardUseCase(get(), get()) }
}
