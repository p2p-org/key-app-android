package com.p2p.wallet.auth

import androidx.biometric.BiometricManager
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.interactor.SecurityKeyInteractor
import com.p2p.wallet.auth.repository.AuthRemoteRepository
import com.p2p.wallet.auth.repository.AuthRepository
import com.p2p.wallet.auth.ui.biometric.BiometricContract
import com.p2p.wallet.auth.ui.biometric.BiometricPresenter
import com.p2p.wallet.auth.ui.pin.create.CreatePinContract
import com.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import com.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import com.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import com.p2p.wallet.auth.ui.security.SecurityKeyContract
import com.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object AuthModule {

    fun create() = module {
        single { BiometricManager.from(get()) }

        single { AuthInteractor(get(), get(), get(), get()) }
        single { AuthRemoteRepository() } bind AuthRepository::class
        factory { SecurityKeyPresenter(get(), get()) } bind SecurityKeyContract.Presenter::class

        factory { BiometricPresenter(get()) } bind BiometricContract.Presenter::class
        factory { CreatePinPresenter(get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get()) } bind SignInPinContract.Presenter::class
        factory { SecurityKeyInteractor(get()) }
    }
}