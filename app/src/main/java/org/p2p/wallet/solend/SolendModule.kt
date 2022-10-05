package org.p2p.wallet.solend

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.data.SolendConfigurationLocalRepository
import org.p2p.wallet.solend.data.SolendConfigurationRepository
import org.p2p.wallet.solend.data.mapper.SolendConfigurationRepositoryMapper
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendDepositsRemoteRepository
import org.p2p.wallet.solend.repository.SolendDepositsRepository
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsContract
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsPresenter
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetContract
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SolendEarnPresenter) bind SolendEarnContract.Presenter::class
        factoryOf(::SolendUserDepositsPresenter) bind SolendUserDepositsContract.Presenter::class
        factory { (deposit: SolendDepositToken) ->
            SolendTopUpBottomSheetPresenter(
                deposit = deposit,
                userInteractor = get()
            )
        } bind SolendTopUpBottomSheetContract.Presenter::class

        factoryOf(::SolendDepositsInteractor)
        singleOf(::SolendDepositsRemoteRepository) bind SolendDepositsRepository::class

        factoryOf(::SolendDepositMapper)

        initDataLayer()
    }

    private fun Module.initDataLayer() {
        factoryOf(::SolendConfigurationRepositoryMapper)
        singleOf(::SolendConfigurationLocalRepository) bind SolendConfigurationRepository::class
    }
}
