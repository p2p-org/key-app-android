package com.p2p.wowlet.domain

import com.p2p.wowlet.auth.interactor.CreateWalletInteractor
import com.p2p.wowlet.auth.interactor.EnterPinCodeInteractor
import com.p2p.wowlet.auth.interactor.FingerPrintInteractor
import com.p2p.wowlet.auth.interactor.PinCodeInteractor
import com.p2p.wowlet.auth.interactor.PinCodeVerificationInteractor
import com.p2p.wowlet.auth.interactor.RegFinishInteractor
import com.p2p.wowlet.domain.usecases.CompleteBackupWalletInteractor
import com.p2p.wowlet.domain.usecases.DashboardInteractor
import com.p2p.wowlet.domain.usecases.DetailWalletInteractor
import com.p2p.wowlet.domain.usecases.ManualSecretKeyInteractor
import com.p2p.wowlet.domain.usecases.NetworksInteractor
import com.p2p.wowlet.domain.usecases.NotificationInteractor
import com.p2p.wowlet.domain.usecases.QrScannerInteractor
import com.p2p.wowlet.domain.usecases.SecretKeyInteractor
import com.p2p.wowlet.domain.usecases.SendCoinInteractor
import com.p2p.wowlet.domain.usecases.SwapInteractor
import org.koin.dsl.module

val interactorsModule = module {
    factory { SecretKeyInteractor(get(), get()) }
    factory { ManualSecretKeyInteractor() }
    factory {
        EnterPinCodeInteractor(
            get()
        )
    }
    single { NotificationInteractor(get()) }
    single { SendCoinInteractor(get(), get()) }
    single { DashboardInteractor(get(), get(), get(), get()) }
    single { RegFinishInteractor(get()) }
    single { CompleteBackupWalletInteractor(get()) }
    single { DetailWalletInteractor(get(), get(), get()) }
    single { FingerPrintInteractor(get()) }
    factory {
        CreateWalletInteractor(
            get(),
            get()
        )
    }
    factory { PinCodeVerificationInteractor(get()) }
    factory { PinCodeInteractor(get()) }
    factory { QrScannerInteractor(get()) }
    factory { NetworksInteractor(get()) }
    factory { SwapInteractor() }
}