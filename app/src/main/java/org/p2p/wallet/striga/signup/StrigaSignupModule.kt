package org.p2p.wallet.striga.signup

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
import org.p2p.wallet.home.onofframp.OnOffRampCountrySelectionContract
import org.p2p.wallet.home.onofframp.interactor.OnOffRampCountrySelectionInteractor
import org.p2p.wallet.home.onofframp.ui.OnOffRampCountrySelectionPresenter
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.striga.signup.finish.StrigaSignupFinishContract
import org.p2p.wallet.striga.signup.finish.StrigaSignupFinishPresenter
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataSearcher
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataInteractor
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.presetpicker.presenter.StrigaPresetDataPickerPresenter
import org.p2p.wallet.striga.signup.presetpicker.repository.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.signup.presetpicker.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepPresenter
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.steps.second.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.steps.second.StrigaSignUpSecondStepPresenter
import org.p2p.wallet.striga.signup.steps.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.sms.interactor.StrigaOtpConfirmInteractor
import org.p2p.wallet.striga.sms.signup.StrigaSignupSmsApiCaller
import org.p2p.wallet.striga.sms.signup.StrigaSignupSmsInputPresenter
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
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

        factoryOf(::OnOffRampCountrySelectionInteractor)
        factoryOf(::OnOffRampCountrySelectionPresenter) bind OnOffRampCountrySelectionContract.Presenter::class
        factoryOf(::StrigaPresetDataInteractor)
        factoryOf(::StrigaPresetDataSearcher)
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
                strigaPresetDataLocalRepository = get()
            )
        }
        factoryOf(::StrigaUserInteractor)
        factoryOf(::StrigaSignupDataEnsurerInteractor)

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
