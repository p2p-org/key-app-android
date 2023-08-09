package org.p2p.wallet.home.ui.container

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.token.Token
import org.p2p.wallet.home.addmoney.AddMoneyDialogContract
import org.p2p.wallet.home.addmoney.interactor.AddMoneyDialogInteractor
import org.p2p.wallet.home.addmoney.repository.AddMoneyCellsRepository
import org.p2p.wallet.home.addmoney.ui.AddMoneyDialogPresenter
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.home.repository.RefreshErrorInMemoryRepository
import org.p2p.wallet.home.repository.RefreshErrorRepository
import org.p2p.wallet.home.ui.container.mapper.WalletBalanceMapper
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.newsend.interactor.SearchInteractor
import org.p2p.wallet.newsend.interactor.SendInteractor

object MainContainerModule : InjectionModule {

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factoryOf(::RefreshErrorInMemoryRepository) bind RefreshErrorRepository::class
        factoryOf(::AddMoneyCellsRepository)
    }

    private fun Module.initDomainLayer() {
        single {
            SendInteractor(
                addressInteractor = get(),
                feeRelayerInteractor = get(),
                feeRelayerAccountInteractor = get(),
                feeRelayerTopUpInteractor = get(),
                orcaInfoInteractor = get(),
                amountRepository = get(),
                transactionInteractor = get(),
                tokenKeyProvider = get(),
                dispatchers = get()
            )
        }
        factoryOf(::SearchInteractor)
        singleOf(::RefreshErrorInteractor)
        factoryOf(::AddMoneyDialogInteractor)
    }

    private fun Module.initPresentationLayer() {
        factoryOf(::MainContainerPresenter) bind MainContainerContract.Presenter::class

        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
        factory {
            AddMoneyDialogPresenter(
                appFeatureFlags = get(),
                interactor = get(),
                userInteractor = get(),
                strigaSignupFeatureToggle = get(),
                seedPhraseProvider = get(),
                strigaUserInteractor = get(),
                strigaWalletInteractor = get(),
                strigaSignupDataEnsurerInteractor = get(),
            )
        } bind AddMoneyDialogContract.Presenter::class
        factoryOf(::WalletBalanceMapper)
    }
}
