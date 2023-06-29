package org.p2p.wallet.solend

import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.relay.RelayRemoteRepository
import org.p2p.wallet.relay.RelayRepository
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.interactor.SolendWithdrawInteractor
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.repository.SolendConfigurationLocalRepository
import org.p2p.wallet.solend.repository.SolendConfigurationRepository
import org.p2p.wallet.solend.repository.SolendDepositsRemoteRepository
import org.p2p.wallet.solend.repository.SolendRepository
import org.p2p.wallet.solend.repository.mapper.SolendConfigurationRepositoryMapper
import org.p2p.wallet.solend.storage.SolendStorage
import org.p2p.wallet.solend.storage.SolendStorageContract
import org.p2p.wallet.solend.ui.aboutearn.SolendAboutEarnContract
import org.p2p.wallet.solend.ui.aboutearn.SolendAboutEarnPresenter
import org.p2p.wallet.solend.ui.deposit.SolendDepositContract
import org.p2p.wallet.solend.ui.deposit.SolendDepositPresenter
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsContract
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsPresenter
import org.p2p.wallet.solend.ui.earn.DepositTickerStorage
import org.p2p.wallet.solend.ui.earn.SolendEarnContract
import org.p2p.wallet.solend.ui.earn.SolendEarnPresenter
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetContract
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetPresenter
import org.p2p.wallet.solend.ui.withdraw.SolendWithdrawContract
import org.p2p.wallet.solend.ui.withdraw.SolendWithdrawPresenter

object SolendModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SolendWithdrawPresenter) bind SolendWithdrawContract.Presenter::class
        factoryOf(::SolendEarnPresenter) bind SolendEarnContract.Presenter::class
        factoryOf(::SolendUserDepositsPresenter) bind SolendUserDepositsContract.Presenter::class
        singleOf(::DepositTickerStorage)
        factoryOf(::SolendTopUpBottomSheetPresenter) bind SolendTopUpBottomSheetContract.Presenter::class

        factoryOf(::SolendDepositPresenter) bind SolendDepositContract.Presenter::class

        factoryOf(::SolendDepositInteractor)
        factoryOf(::SolendWithdrawInteractor)
        singleOf(::SolendDepositsRemoteRepository) bind SolendRepository::class
        singleOf(::SolendStorage) { bind<SolendStorageContract>() }

        factoryOf(::SolendDepositMapper)

        factoryOf(::SolendAboutEarnPresenter) { bind<SolendAboutEarnContract.Presenter>() }

        initDataLayer()
    }

    private fun Module.initDataLayer() {
        factoryOf(::SolendConfigurationRepositoryMapper)
        singleOf(::SolendConfigurationLocalRepository) bind SolendConfigurationRepository::class
        singleOf(::RelayRemoteRepository) bind RelayRepository::class
    }
}
