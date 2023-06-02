package org.p2p.wallet.striga.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract
import org.p2p.wallet.striga.onboarding.StrigaOnboardingPresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerContract
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataSearcher
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataInteractor
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
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.repository.StrigaUserRemoteRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepositoryMapper

object StrigaSignupModule : InjectionModule {
    override fun create() = module {
        initDataLayer()

        factoryOf(::StrigaOnboardingInteractor)
        factoryOf(::StrigaPresetDataInteractor)
        factoryOf(::StrigaPresetDataSearcher)
        factoryOf(::StrigaOnboardingPresenter) bind StrigaOnboardingContract.Presenter::class
        factory { (selectedItem: StrigaPresetDataToPick) ->
            StrigaPresetDataPickerPresenter(
                presetDataToPick = selectedItem,
                strigaElementCellMapper = get(),
                strigaPresetDataInteractor = get(),
                dataSearcher = get(),
                dispatchers = get()
            )
        } bind StrigaPresetDataPickerContract.Presenter::class
        factoryOf(::StrigaSignUpFirstStepPresenter) bind StrigaSignUpFirstStepContract.Presenter::class
        factoryOf(::StrigaSignUpSecondStepPresenter) bind StrigaSignUpSecondStepContract.Presenter::class
        factoryOf(::StrigaSignupDataValidator)
        factoryOf(::StrigaSignupInteractor)
    }

    private fun Module.initDataLayer() {
        single<StrigaApi> {
            val url = androidContext().getString(R.string.strigaProxyServiceProdBaseUrl)
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

        factoryOf(::StrigaUserIdProvider)
        factoryOf(::StrigaItemCellMapper)
    }
}
