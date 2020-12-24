package com.wowlet.domain.di

import com.wowlet.domain.interactors.*
import com.wowlet.domain.usecases.*
import org.koin.dsl.module

val interactorsModule = module {
    single<RegLoginInteractor> { RegLoginUseCase(get(), get()) }
    factory<SecretKeyInteractor> { SecretKeyUseCase(get(), get()) }
    factory<ManualSecretKeyInteractor> { ManualSecretKeyUseCase() }
    factory<EnterPinCodeInteractor> { EnterPinCodeUseCase(get()) }
    single<NotificationInteractor> { NotificationUseCase(get()) }
    single<SendCoinInteractor> { SendCoinUseCase(get(), get()) }
    single<DashboardInteractor> { DashBoardUseCase(get(), get(), get()) }
    single<RegFinishInteractor> { RegFinishUseCase(get()) }
    single<CompleteBackupWalletInteractor> { CompleteBackupWalletUseCase(get()) }
    single<SplashScreenInteractor> { SplashScreenUseCase(get()) }
    single<DetailActivityInteractor> { DetailActivityUseCase(get(), get()) }
    single<FingerPrintInteractor> { FingerPrintUseCase(get()) }
    factory<CreateWalletInteractor> { CreateWalletUseCase(get(), get()) }
    factory<PinCodeVerificationInteractor> { PinCodeVerificationUseCase(get()) }
    factory<PinCodeInteractor> { PinCodeUseCase(get()) }
    factory<QrScannerInteractor> { QrScannerUseCase(get()) }
}
