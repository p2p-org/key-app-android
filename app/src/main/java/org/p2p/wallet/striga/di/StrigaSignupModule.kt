package org.p2p.wallet.striga.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.striga.finish.StrigaSignupFinishContract
import org.p2p.wallet.striga.finish.StrigaSignupFinishPresenter
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract
import org.p2p.wallet.striga.onboarding.StrigaOnboardingPresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataSearcher
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.presetpicker.presenter.StrigaPresetDataPickerPresenter
import org.p2p.wallet.striga.signup.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepPresenter
import org.p2p.wallet.striga.signup.ui.StrigaSignUpSecondStepPresenter
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.sms.StrigaOtpConfirmInteractor
import org.p2p.wallet.striga.sms.signup.StrigaSignupSmsApiCaller
import org.p2p.wallet.striga.sms.signup.StrigaSignupSmsInputPresenter
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.repository.StrigaUserRemoteRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepositoryMapper
import org.p2p.wallet.striga.user.repository.StrigaUserStatusDestinationMapper
import org.p2p.wallet.striga.user.repository.StrigaUserStatusRepository

object StrigaSignupModule : InjectionModule {

    val SMS_QUALIFIER = SmsInputFactory.Type.StrigaSignup.name

    override fun create() = module {
        initDataLayer()
        initSms()

        factoryOf(::StrigaOnboardingInteractor)
        factoryOf(::StrigaPresetDataInteractor)
        factoryOf(::StrigaPresetDataSearcher)
        factoryOf(::StrigaOnboardingPresenter) bind StrigaOnboardingContract.Presenter::class
        factory { (selectedItem: StrigaPresetDataItem) ->
            StrigaPresetDataPickerPresenter(
                strigaElementCellMapper = get(),
                strigaPresetDataInteractor = get(),
                dataSearcher = get(),
                dispatchers = get(),
                selectedPresetDataItem = selectedItem
            )
        } bind StrigaPresetDataPickerContract.Presenter::class
        factoryOf(::StrigaSignUpFirstStepPresenter) bind StrigaSignUpFirstStepContract.Presenter::class
        factoryOf(::StrigaSignUpSecondStepPresenter) bind StrigaSignUpSecondStepContract.Presenter::class
        factoryOf(::StrigaSignupDataValidator)
        factory {
            StrigaSignupInteractor(
                appScope = get(),
                inAppFeatureFlags = get(),
                validator = get(),
                countryCodeRepository = get(),
                signupDataRepository = get(),
                userInteractor = get(),
                metadataInteractor = get(),
                strigaOtpConfirmInteractor = get(named(SMS_QUALIFIER)),
                strigaUserStatusRepository = get(),
            )
        }
        factoryOf(::StrigaUserInteractor)

        factoryOf(::StrigaSignupFinishPresenter) bind StrigaSignupFinishContract.Presenter::class
    }

    private fun Module.initDataLayer() {
        single<StrigaApi> {
            val url = androidContext().getString(R.string.strigaProxyServiceBaseUrl)
            getRetrofit(
                baseUrl = url,
                tag = "StrigaProxyApi",
                interceptor = new(::StrigaProxyApiInterceptor)
            ).create()
        }

        factoryOf(::StrigaUserRepositoryMapper)
        factoryOf(::StrigaUserRemoteRepository) bind StrigaUserRepository::class

        singleOf(::StrigaPresetDataInMemoryRepository) bind StrigaPresetDataLocalRepository::class
        factoryOf(::StrigaSignupDataDatabaseRepository) bind StrigaSignupDataLocalRepository::class
        factoryOf(::StrigaSignupDataMapper)

        factoryOf(::StrigaItemCellMapper)
        singleOf(::StrigaUserStatusRepository)
        factoryOf(::StrigaUserStatusDestinationMapper)
    }

    private fun Module.initSms() {
        singleOf(::SmsInputTimer) {
            named(SMS_QUALIFIER)
        }

        factoryOf(::StrigaSignupSmsApiCaller) {
            named(SMS_QUALIFIER)
        } bind StrigaSmsApiCaller::class

        factory(named(SMS_QUALIFIER)) {
            StrigaOtpConfirmInteractor(
                strigaSignupDataRepository = get(),
                phoneCodeRepository = get(),
                inAppFeatureFlags = get(),
                smsInputTimer = get(named(SMS_QUALIFIER)),
                strigaStorage = get(),
                smsApiCaller = get(named(SMS_QUALIFIER))
            )
        }

        factory(named(SMS_QUALIFIER)) {
            StrigaSignupSmsInputPresenter(
                interactor = get(named(SMS_QUALIFIER))
            )
        } bind SmsInputContract.Presenter::class
    }
}
