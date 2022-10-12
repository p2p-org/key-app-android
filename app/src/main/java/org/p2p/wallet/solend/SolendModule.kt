package org.p2p.wallet.solend

import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendConfigurationLocalRepository
import org.p2p.wallet.solend.repository.SolendConfigurationRepository
import org.p2p.wallet.solend.repository.SolendDepositsRemoteRepository
import org.p2p.wallet.solend.repository.SolendDepositsRepository
import org.p2p.wallet.solend.repository.mapper.SolendConfigurationRepositoryMapper
import org.p2p.wallet.solend.storage.SolendStorage
import org.p2p.wallet.solend.storage.SolendStorageContract
import org.p2p.wallet.solend.ui.aboutearn.SolendAboutEarnContract
import org.p2p.wallet.solend.ui.aboutearn.SolendAboutEarnPresenter
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsContract
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsPresenter
import org.p2p.wallet.solend.ui.earn.DepositTickerManager
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetContract
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SolendEarnPresenter) bind SolendEarnContract.Presenter::class
        factoryOf(::SolendUserDepositsPresenter) bind SolendUserDepositsContract.Presenter::class
        singleOf(::DepositTickerManager)
        factory { (deposit: SolendDepositToken) ->
            SolendTopUpBottomSheetPresenter(
                deposit = deposit,
                userInteractor = get()
            )
        } bind SolendTopUpBottomSheetContract.Presenter::class

        factoryOf(::SolendDepositsInteractor)
        singleOf(::SolendDepositsRemoteRepository) bind SolendDepositsRepository::class
        singleOf(::SolendStorage) { bind<SolendStorageContract>() }

        factoryOf(::SolendDepositMapper)

        factoryOf(::SolendAboutEarnPresenter) { bind<SolendAboutEarnContract.Presenter>() }

        initDataLayer()
    }

    private fun Module.initDataLayer() {
        factoryOf(::SolendConfigurationRepositoryMapper)
        singleOf(::SolendConfigurationLocalRepository) bind SolendConfigurationRepository::class
    }
}
