package org.p2p.wallet.striga

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract
import org.p2p.wallet.striga.onboarding.StrigaOnboardingPresenter
import org.p2p.wallet.striga.repository.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.repository.StrigaPresetDataLocalRepository

object StrigaSignupModule {

    fun create() = module {
        factoryOf(::StrigaOnboardingPresenter) bind StrigaOnboardingContract.Presenter::class

        singleOf(::StrigaPresetDataInMemoryRepository) bind StrigaPresetDataLocalRepository::class
    }
}
