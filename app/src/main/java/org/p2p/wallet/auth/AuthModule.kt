package org.p2p.wallet.auth

import androidx.biometric.BiometricManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.api.UsernameApi
import org.p2p.wallet.auth.gateway.GatewayServiceModule
import org.p2p.wallet.auth.gateway.parser.CountryCodeHelper
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.SignUpFlowDataCache
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.AuthRemoteRepository
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.UsernameRemoteRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.auth.ui.done.AuthDoneContract
import org.p2p.wallet.auth.ui.done.AuthDonePresenter
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingContract
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingPresenter
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingContract
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingPresenter
import org.p2p.wallet.auth.ui.phone.CountryCodeInteractor
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterContract
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterPresenter
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerContract
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerPresenter
import org.p2p.wallet.auth.ui.pin.biometrics.BiometricsContract
import org.p2p.wallet.auth.ui.pin.biometrics.BiometricsPresenter
import org.p2p.wallet.auth.ui.pin.create.CreatePinContract
import org.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinContract
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinPresenter
import org.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import org.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import org.p2p.wallet.auth.ui.restore.WalletFoundContract
import org.p2p.wallet.auth.ui.restore.WalletFoundPresenter
import org.p2p.wallet.auth.ui.security.SecurityKeyContract
import org.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.p2p.wallet.auth.ui.smsinput.NewAuthSmsInputContract
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputPresenter
import org.p2p.wallet.auth.ui.smsinput.inputblocked.NewAuthSmsInputBlockedContract
import org.p2p.wallet.auth.ui.smsinput.inputblocked.NewSmsInputBlockedPresenter
import org.p2p.wallet.auth.ui.username.ReserveUsernameContract
import org.p2p.wallet.auth.ui.username.ReserveUsernamePresenter
import org.p2p.wallet.auth.ui.username.UsernameContract
import org.p2p.wallet.auth.ui.username.UsernamePresenter
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyContract
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyPresenter
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.auth.web3authsdk.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.Web3AuthApiClient
import org.p2p.wallet.feerelayer.FeeRelayerModule.FEE_RELAYER_QUALIFIER
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.splash.SplashContract
import org.p2p.wallet.splash.SplashPresenter
import retrofit2.Retrofit

object AuthModule {

    fun create() = module {

        onboardingModule()

        single { BiometricManager.from(androidContext()) }

        factory { AuthInteractor(get(), get(), get(), get(), get()) }
        factory { AuthLogoutInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { AuthRemoteRepository() } bind AuthRepository::class
        factory { FileRepository(get(), get()) }
        factory { SecurityKeyPresenter(get(), get(), get(), get()) } bind SecurityKeyContract.Presenter::class
        factory { CreatePinPresenter(get(), get(), get(), get(), get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get(), get(), get(), get(), get()) } bind SignInPinContract.Presenter::class
        factory { SplashPresenter(get()) } bind SplashContract.Presenter::class
        factory { VerifySecurityKeyPresenter(get(), get(), get()) } bind VerifySecurityKeyContract.Presenter::class
        factory { AuthDonePresenter(get(), get(), get()) } bind AuthDoneContract.Presenter::class

        // reserving username
        factory { UsernameInteractor(get(), get(), get(), get()) }
        factory { ReserveUsernamePresenter(get(), get(), get()) } bind ReserveUsernameContract.Presenter::class
        factory { UsernamePresenter(get(), get(), get()) } bind UsernameContract.Presenter::class
        single {
            val retrofit = get<Retrofit>(named(FEE_RELAYER_QUALIFIER))
            val api = retrofit.create(UsernameApi::class.java)
            UsernameRemoteRepository(api)
        } bind UsernameRepository::class

        includes(GatewayServiceModule.create())
    }

    private fun Module.onboardingModule() {
        singleOf(::GoogleSignInHelper)
        singleOf(::UserSignUpDetailsStorage)
        single<Web3AuthApi> {
            Web3AuthApiClient(
                context = androidContext(),
                torusNetwork = get<NetworkServicesUrlProvider>().loadTorusEnvironment(),
                gson = get()
            )
        }
        singleOf(::SignUpFlowDataCache)
        factoryOf(::CreateWalletInteractor)
        factoryOf(::UserSignUpInteractor)

        factoryOf(::NewOnboardingPresenter) bind NewOnboardingContract.Presenter::class
        factoryOf(::ContinueOnboardingPresenter) bind ContinueOnboardingContract.Presenter::class

        factoryOf(::PhoneNumberEnterPresenter) bind PhoneNumberEnterContract.Presenter::class
        factoryOf(::CountryCodePickerPresenter) bind CountryCodePickerContract.Presenter::class
        singleOf(::CountryCodeInMemoryRepository) bind CountryCodeLocalRepository::class
        single { PhoneNumberUtil.createInstance(androidContext()) }
        singleOf(::CountryCodeHelper)
        factoryOf(::CountryCodeInteractor)

        factoryOf(::WalletFoundPresenter) bind WalletFoundContract.Presenter::class

        factoryOf(::NewSmsInputPresenter) bind NewAuthSmsInputContract.Presenter::class
        factoryOf(::NewSmsInputBlockedPresenter) bind NewAuthSmsInputBlockedContract.Presenter::class

        factoryOf(::NewCreatePinPresenter) bind NewCreatePinContract.Presenter::class
        factoryOf(::BiometricsPresenter) bind BiometricsContract.Presenter::class
    }
}
