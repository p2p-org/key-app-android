package org.p2p.wallet.auth

import androidx.biometric.BiometricManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.GatewayServiceModule
import org.p2p.wallet.auth.gateway.parser.CountryCodeHelper
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.FileInteractor
import org.p2p.wallet.auth.interactor.MetadataInteractor
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
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.RestoreUserResultHandler
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
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
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinContract
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinPresenter
import org.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import org.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import org.p2p.wallet.auth.ui.pin.signin.ValidatePinPresenter
import org.p2p.wallet.auth.ui.pin.validate.ValidatePinContract
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameContract
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernamePresenter
import org.p2p.wallet.auth.ui.reserveusername.UsernameValidator
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreContract
import org.p2p.wallet.auth.ui.restore.common.CommonRestorePresenter
import org.p2p.wallet.auth.ui.restore.found.WalletFoundContract
import org.p2p.wallet.auth.ui.restore.found.WalletFoundPresenter
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenContract
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenPresenter
import org.p2p.wallet.auth.ui.security.SecurityKeyContract
import org.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputPresenter
import org.p2p.wallet.auth.ui.smsinput.SmsInputTimer
import org.p2p.wallet.auth.ui.username.UsernameContract
import org.p2p.wallet.auth.ui.username.UsernamePresenter
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyContract
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyPresenter
import org.p2p.wallet.auth.username.di.RegisterUsernameServiceModule
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.Web3AuthApiClient
import org.p2p.wallet.auth.web3authsdk.Web3AuthDurationTracker
import org.p2p.wallet.auth.web3authsdk.mapper.Web3AuthClientMapper
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.settings.ui.recovery.user_seed_phrase.UserSeedPhraseContract
import org.p2p.wallet.settings.ui.recovery.user_seed_phrase.UserSeedPhrasePresenter
import org.p2p.wallet.splash.SplashContract
import org.p2p.wallet.splash.SplashPresenter

object AuthModule {

    fun create() = module {
        single { BiometricManager.from(androidContext()) }

        factoryOf(::AuthInteractor)
        factoryOf(::AuthRemoteRepository) bind AuthRepository::class
        factoryOf(::FileRepository)
        factoryOf(::SecurityKeyPresenter) bind SecurityKeyContract.Presenter::class
        factoryOf(::SignInPinPresenter) bind SignInPinContract.Presenter::class
        factoryOf(::ValidatePinPresenter) bind ValidatePinContract.Presenter::class
        factoryOf(::SplashPresenter) bind SplashContract.Presenter::class
        factory { UserSeedPhrasePresenter(get()) } bind UserSeedPhraseContract.Presenter::class
        factoryOf(::VerifySecurityKeyPresenter) bind VerifySecurityKeyContract.Presenter::class
        factory {
            AuthLogoutInteractor(
                context = get(),
                secureStorage = get(),
                renBtcInteractor = get(),
                sharedPreferences = get(),
                tokenKeyProvider = get(),
                mainLocalRepository = get(),
                recipientsDatabaseRepository = get(),
                updatesManager = get(),
                transactionManager = get(),
                transactionDetailsLocalRepository = get(),
                pushNotificationsInteractor = get(),
                appScope = get(),
                analytics = get(),
            )
        }

        usernameModule()
        onboardingModule()
        includes(RegisterUsernameServiceModule.create(), GatewayServiceModule.create())
    }

    private fun Module.usernameModule() {
        factoryOf(::UsernameInteractor)
        factoryOf(::ReserveUsernamePresenter) { bind<ReserveUsernameContract.Presenter>() }
        factoryOf(::UsernamePresenter) { bind<UsernameContract.Presenter>() }
        factoryOf(::UsernameValidator)
    }

    private fun Module.onboardingModule() {
        singleOf(::GoogleSignInHelper)
        singleOf(::UserSignUpDetailsStorage)
        singleOf(::Web3AuthClientMapper)
        factoryOf(::Web3AuthDurationTracker)
        single<Web3AuthApi> {
            Web3AuthApiClient(
                context = androidContext(),
                torusNetwork = get<NetworkServicesUrlProvider>().loadTorusEnvironment(),
                mapper = get(),
                gson = get(),
                authRepository = get(),
                torusNetworkEnv = androidContext().getString(R.string.torusNetwork),
                torusLogLevel = androidContext().getString(R.string.torusLogLevel),
                durationTracker = get()
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
        factoryOf(::RestoreErrorScreenPresenter) bind RestoreErrorScreenContract.Presenter::class

        singleOf(::SmsInputTimer)
        factoryOf(::NewSmsInputPresenter) bind NewSmsInputContract.Presenter::class

        factoryOf(::RestoreUserResultHandler)

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
        factoryOf(::MetadataInteractor)
        singleOf(::RestoreStateMachine)
    }
}
