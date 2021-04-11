package com.p2p.wowlet.auth

import com.p2p.wowlet.auth.interactor.AuthInteractor
import com.p2p.wowlet.auth.interactor.FingerPrintInteractor
import com.p2p.wowlet.auth.interactor.PinCodeInteractor
import com.p2p.wowlet.auth.interactor.PinCodeVerificationInteractor
import com.p2p.wowlet.auth.interactor.RegFinishInteractor
import com.p2p.wowlet.auth.ui.securitykey.interactor.SecurityKeyInteractor
import com.p2p.wowlet.auth.ui.securitykey.ui.SecurityKeyContract
import com.p2p.wowlet.auth.ui.securitykey.ui.SecurityKeyPresenter
import com.p2p.wowlet.auth.ui.fingerprint.viewmodel.FingerPrintViewModel
import com.p2p.wowlet.auth.ui.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wowlet.dashboard.interactor.DetailWalletInteractor
import com.p2p.wowlet.dashboard.ui.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.notification.viewmodel.NotificationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { AuthInteractor(get()) }
        factory { PinCodeVerificationInteractor(get()) }
        factory { PinCodeInteractor(get()) }
        viewModel { FingerPrintViewModel(get()) }
        viewModel { NotificationViewModel(get()) }
        viewModel { RecoveryPhraseViewModel(get()) }
        viewModel { PinCodeViewModel(get(), get(), get()) }
        viewModel { ManualSecretKeyViewModel(get()) }
        factory { SecurityKeyPresenter(get()) } bind SecurityKeyContract.Presenter::class

        single { FingerPrintInteractor(get()) }
        factory {
            SecurityKeyInteractor(
                get(),
                get()
            )
        }
        single { DetailWalletInteractor(get(), get(), get()) }
        single { RegFinishInteractor(get()) }

    }
}