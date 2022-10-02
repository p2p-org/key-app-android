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
import org.p2p.wallet.auth.interactor.FileInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.interactor.restore.CustomShareRestoreInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.interactor.restore.TorusKeyInteractor
import org.p2p.wallet.auth.interactor.restore.UserRestoreInteractor
import org.p2p.wallet.auth.repository.AuthRemoteRepository
import org.p2p.wallet.auth.repository.AuthRepository
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.RestoreUserExceptionHandler
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.repository.UsernameRemoteRepository
import org.p2p.wallet.auth.repository.UsernameRepository
import org.p2p.wallet.auth.ui.done.AuthDoneContract
import org.p2p.wallet.auth.ui.done.AuthDonePresenter
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorPresenter
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerContract
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerPresenter
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingContract
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingPresenter
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingContract
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingPresenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootContract
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootPresenter
import org.p2p.wallet.auth.ui.phone.CountryCodeInteractor
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterContract
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterPresenter
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerContract
import org.p2p.wallet.auth.ui.phone.countrypicker.CountryCodePickerPresenter
import org.p2p.wallet.auth.ui.pin.create.CreatePinContract
import org.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinContract
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinPresenter
import org.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import org.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreContract
import org.p2p.wallet.auth.ui.restore.common.CommonRestorePresenter
import org.p2p.wallet.auth.ui.restore.found.WalletFoundContract
import org.p2p.wallet.auth.ui.restore.found.WalletFoundPresenter
import org.p2p.wallet.auth.ui.security.SecurityKeyContract
import org.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputPresenter
import org.p2p.wallet.auth.ui.smsinput.SmsInputTimer
import org.p2p.wallet.auth.ui.username.ReserveUsernameContract
import org.p2p.wallet.auth.ui.username.ReserveUsernamePresenter
import org.p2p.wallet.auth.ui.username.UsernameContract
import org.p2p.wallet.auth.ui.username.UsernamePresenter
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyContract
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyPresenter
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.Web3AuthApiClient
import org.p2p.wallet.auth.web3authsdk.mapper.Web3AuthClientMapper
import org.p2p.wallet.feerelayer.FeeRelayerModule.FEE_RELAYER_QUALIFIER
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.splash.SplashContract
import org.p2p.wallet.splash.SplashPresenter
import retrofit2.Retrofit

object AuthModule {

    fun create() = module {

        onboardingModule()

        single { BiometricManager.from(androidContext()) }

        factoryOf(::AuthInteractor)
        factory {
            AuthLogoutInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
        }
        factoryOf(::AuthRemoteRepository) bind AuthRepository::class
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
        singleOf(::Web3AuthClientMapper)
        single<Web3AuthApi> {
            Web3AuthApiClient(
                context = androidContext(),
                torusNetwork = get<NetworkServicesUrlProvider>().loadTorusEnvironment(),
                mapper = get(),
                gson = get(),
                authRepository = get()
            )
        }
        singleOf(::SignUpFlowDataLocalRepository)
        factoryOf(::CreateWalletInteractor)
        factoryOf(::UserSignUpInteractor)
        factoryOf(::FileInteractor)
        singleOf(::OnboardingInteractor)

        factoryOf(::OnboardingRootPresenter) bind OnboardingRootContract.Presenter::class

        factoryOf(::NewOnboardingPresenter) bind NewOnboardingContract.Presenter::class
        factoryOf(::ContinueOnboardingPresenter) bind ContinueOnboardingContract.Presenter::class
        factoryOf(::CommonRestorePresenter) bind CommonRestoreContract.Presenter::class

        factoryOf(::PhoneNumberEnterPresenter) bind PhoneNumberEnterContract.Presenter::class
        factoryOf(::CountryCodePickerPresenter) bind CountryCodePickerContract.Presenter::class
        singleOf(::CountryCodeInMemoryRepository) bind CountryCodeLocalRepository::class
        single { PhoneNumberUtil.createInstance(androidContext()) }
        singleOf(::CountryCodeHelper)
        factoryOf(::CountryCodeInteractor)

        factoryOf(::WalletFoundPresenter) bind WalletFoundContract.Presenter::class

        singleOf(::SmsInputTimer)
        factoryOf(::NewSmsInputPresenter) bind NewSmsInputContract.Presenter::class

        factoryOf(::GatewayServiceErrorHandler)
        factoryOf(::RestoreUserExceptionHandler)

        factory { (error: GeneralErrorTimerScreenError, timerLeftTime: Long) ->
            OnboardingGeneralErrorTimerPresenter(
                error = error,
                timerLeftTime = timerLeftTime,
                smsInputTimer = get(),
                fileInteractor = get()
            )
        } bind OnboardingGeneralErrorTimerContract.Presenter::class
        factoryOf(::OnboardingGeneralErrorPresenter) bind OnboardingGeneralErrorContract.Presenter::class
        factoryOf(::RestoreWalletInteractor)
        factoryOf(::NewCreatePinPresenter) bind NewCreatePinContract.Presenter::class

        singleOf(::RestoreFlowDataLocalRepository)
        factoryOf(::CustomShareRestoreInteractor)
        factoryOf(::TorusKeyInteractor)
        factoryOf(::UserRestoreInteractor)
    }
}
