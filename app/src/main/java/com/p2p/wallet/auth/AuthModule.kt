package com.p2p.wallet.auth

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.interactor.FingerPrintInteractor
import com.p2p.wallet.auth.interactor.PinCodeInteractor
import com.p2p.wallet.auth.interactor.PinCodeVerificationInteractor
import com.p2p.wallet.auth.interactor.RegFinishInteractor
import com.p2p.wallet.auth.ui.security.interactor.SecurityKeyInteractor
import com.p2p.wallet.auth.ui.security.ui.SecurityKeyContract
import com.p2p.wallet.auth.ui.security.ui.SecurityKeyPresenter
import com.p2p.wallet.auth.ui.fingerprint.viewmodel.FingerPrintViewModel
import com.p2p.wallet.auth.ui.pincode.viewmodel.PinCodeViewModel
import com.p2p.wallet.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wallet.dashboard.interactor.DetailWalletInteractor
import com.p2p.wallet.dashboard.ui.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wallet.notification.viewmodel.NotificationViewModel
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