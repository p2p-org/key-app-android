package org.p2p.wallet.auth

import androidx.biometric.BiometricManager
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRemoteRepository
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UsernameRemoteRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.auth.ui.done.AuthDoneContract
import org.p2p.wallet.auth.ui.done.AuthDonePresenter
import org.p2p.wallet.auth.ui.pin.create.CreatePinContract
import org.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import org.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import org.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import org.p2p.wallet.auth.ui.security.SecurityKeyContract
import org.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.p2p.wallet.auth.ui.username.ReserveUsernameContract
import org.p2p.wallet.auth.ui.username.ReserveUsernamePresenter
import org.p2p.wallet.auth.ui.username.UsernameContract
import org.p2p.wallet.auth.ui.username.UsernamePresenter
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyContract
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyPresenter
import org.p2p.wallet.feerelayer.FeeRelayerModule.FEE_RELAYER_QUALIFIER
import retrofit2.Retrofit

object AuthModule {

    fun create() = module {
        single { BiometricManager.from(get()) }

        factory { AuthInteractor(get(), get(), get(), get(), get()) }
        factory { AuthLogoutInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { AuthRemoteRepository() } bind AuthRepository::class
        factory { FileRepository(get()) }
        factory { SecurityKeyPresenter(get(), get(), get(), get()) } bind SecurityKeyContract.Presenter::class
        factory { CreatePinPresenter(get(), get(), get(), get(), get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get(), get(), get(), get(), get()) } bind SignInPinContract.Presenter::class
        factory { VerifySecurityKeyPresenter(get(), get(), get()) } bind VerifySecurityKeyContract.Presenter::class
        factory { AuthDonePresenter(get()) } bind AuthDoneContract.Presenter::class

        // reserving username
        factory { UsernameInteractor(get(), get(), get(), get()) }
        factory { ReserveUsernamePresenter(get(), get(), get()) } bind ReserveUsernameContract.Presenter::class
        factory { UsernamePresenter(get(), get(), get()) } bind UsernameContract.Presenter::class
        single {
            val retrofit = get<Retrofit>(named(FEE_RELAYER_QUALIFIER))
            val api = retrofit.create(UsernameApi::class.java)
            UsernameRemoteRepository(api)
        } bind UsernameRepository::class
    }
}
