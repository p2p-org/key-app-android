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
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract
import org.p2p.wallet.striga.repository.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.repository.StrigaUserRemoteRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepositoryMapper

object StrigaSignupModule : InjectionModule {
    override fun create() = module {
        initDataLayer()
        initUiLayer()
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
    }

    private fun Module.initUiLayer() {
        factoryOf(org.p2p.wallet.striga.onboarding.interactor::StrigaOnboardingInteractor)
        factoryOf(org.p2p.wallet.striga.onboarding::StrigaOnboardingPresenter) bind org.p2p.wallet.striga.onboarding.StrigaOnboardingContract.Presenter::class
        factoryOf(org.p2p.wallet.striga.ui.personaldata::StrigaPersonalInfoPresenter) bind org.p2p.wallet.striga.ui.personaldata.StrigaPersonalInfoContract.Presenter::class
    }
}
