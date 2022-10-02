package org.p2p.wallet.solend

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.data.SolendConfigurationLocalRepository
import org.p2p.wallet.solend.data.SolendConfigurationRepository
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SolendEarnPresenter) bind SolendEarnContract.Presenter::class

        initDataLayer()
    }

    private fun Module.initDataLayer() {
        singleOf(::SolendConfigurationLocalRepository) bind SolendConfigurationRepository::class
    }
}
