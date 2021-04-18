package com.p2p.wallet.auth

import androidx.biometric.BiometricManager
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.interactor.PinCodeInteractor
import com.p2p.wallet.auth.interactor.PinCodeVerificationInteractor
import com.p2p.wallet.auth.interactor.RegFinishInteractor
import com.p2p.wallet.auth.interactor.SecurityKeyInteractor
import com.p2p.wallet.auth.ui.biometric.BiometricContract
import com.p2p.wallet.auth.ui.biometric.BiometricPresenter
import com.p2p.wallet.auth.ui.pin.create.CreatePinContract
import com.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import com.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import com.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import com.p2p.wallet.auth.ui.pincode.viewmodel.PinCodeViewModel
import com.p2p.wallet.auth.ui.security.SecurityKeyContract
import com.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import com.p2p.wallet.restore.ui.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wallet.dashboard.interactor.DetailWalletInteractor
import com.p2p.wallet.dashboard.ui.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wallet.notification.viewmodel.NotificationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { BiometricManager.from(get()) }

        single { AuthInteractor(get(), get(), get(), get()) }
        factory { PinCodeVerificationInteractor(get()) }
        factory { PinCodeInteractor(get()) }
        viewModel { NotificationViewModel(get()) }
        viewModel { RecoveryPhraseViewModel(get()) }
        viewModel { PinCodeViewModel(get(), get()) }
        viewModel { ManualSecretKeyViewModel(get()) }
        factory { SecurityKeyPresenter(get(), get()) } bind SecurityKeyContract.Presenter::class

        factory { BiometricPresenter(get()) } bind BiometricContract.Presenter::class
        factory { CreatePinPresenter(get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get()) } bind SignInPinContract.Presenter::class
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