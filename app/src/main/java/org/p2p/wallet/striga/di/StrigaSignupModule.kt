package org.p2p.wallet.striga.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract
import org.p2p.wallet.striga.onboarding.StrigaOnboardingPresenter
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.repository.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.striga.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.ui.personaldata.StrigaPersonalInfoContract
import org.p2p.wallet.striga.ui.personaldata.StrigaPersonalInfoPresenter

object StrigaSignupModule : InjectionModule {
    override fun create() = module {
        // repo
        singleOf(::StrigaPresetDataInMemoryRepository) bind StrigaPresetDataLocalRepository::class
        factoryOf(::StrigaSignupDataDatabaseRepository) bind StrigaSignupDataLocalRepository::class

        // data & mappers
        factoryOf(::StrigaSignupDataMapper)

        // onboarding
        factoryOf(::StrigaOnboardingInteractor)
        factoryOf(::StrigaOnboardingPresenter) bind StrigaOnboardingContract.Presenter::class

        // user data
        factoryOf(::StrigaPersonalInfoPresenter) bind StrigaPersonalInfoContract.Presenter::class
    }
}
