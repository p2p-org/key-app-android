package org.p2p.wallet.auth

import androidx.biometric.BiometricManager
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRemoteRepository
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.repository.FileLocalRepository
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UsernameRemoteRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.auth.ui.biometric.BiometricContract
import org.p2p.wallet.auth.ui.biometric.BiometricPresenter
import org.p2p.wallet.auth.ui.pin.create.CreatePinContract
import org.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import org.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import org.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import org.p2p.wallet.auth.ui.security.SecurityKeyContract
import org.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.p2p.wallet.auth.ui.username.ReservingUsernameContract
import org.p2p.wallet.auth.ui.username.ReservingUsernamePresenter
import org.p2p.wallet.auth.ui.username.UsernameContract
import org.p2p.wallet.auth.ui.username.UsernamePresenter
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

object AuthModule {

    const val RESERVING_USERNAME_QUALIFIER = "https://fee-relayer.solana.p2p.org"

    fun create() = module {
        single { BiometricManager.from(get()) }

        factory { AuthInteractor(get(), get(), get(), get(), get()) }
        factory { AuthRemoteRepository() } bind AuthRepository::class
        factory { FileLocalRepository(get()) } bind FileRepository::class
        factory { SecurityKeyPresenter(get()) } bind SecurityKeyContract.Presenter::class
        factory { BiometricPresenter(get()) } bind BiometricContract.Presenter::class
        factory { CreatePinPresenter(get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get()) } bind SignInPinContract.Presenter::class

        // reserving username
        factory { UsernameInteractor(get(), get(), get(), get()) }
        factory { ReservingUsernamePresenter(get(), get()) } bind ReservingUsernameContract.Presenter::class
        factory { UsernamePresenter(get(), get()) } bind UsernameContract.Presenter::class
        single {
            val retrofit = get<Retrofit>(named(RESERVING_USERNAME_QUALIFIER))
            val api = retrofit.create(UsernameApi::class.java)
            UsernameRemoteRepository(api, get(), get())
        } bind UsernameRepository::class
    }
}