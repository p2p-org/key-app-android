package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.token.Token
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.repository.RefreshErrorInMemoryRepository
import org.p2p.wallet.home.repository.RefreshErrorRepository
import org.p2p.wallet.home.ui.container.MainContainerContract
import org.p2p.wallet.home.ui.container.MainContainerPresenter
import org.p2p.wallet.home.ui.crypto.MyCryptoContract
import org.p2p.wallet.home.ui.crypto.MyCryptoPresenter
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.home.ui.wallet.WalletContract
import org.p2p.wallet.home.ui.wallet.WalletPresenter
import org.p2p.wallet.home.ui.wallet.WalletPresenterMapper
import org.p2p.wallet.home.ui.wallet.handlers.StrigaBannerClickHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaOnRampClickHandler
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.newsend.interactor.SearchInteractor
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter
import org.p2p.wallet.striga.ui.TopUpWalletContract
import org.p2p.wallet.striga.ui.TopUpWalletPresenter

object HomeModule : InjectionModule {

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factory<HomeLocalRepository> { HomeDatabaseRepository(get()) }
        factoryOf(::RefreshErrorInMemoryRepository) bind RefreshErrorRepository::class
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
    }

    private fun Module.initPresentationLayer() {
        factoryOf(::MainContainerPresenter) bind MainContainerContract.Presenter::class

        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
        // Cached data exists, therefore creating singleton
        factory {
            HomeInteractor(
                userInteractor = get(),
                settingsInteractor = get(),
                usernameInteractor = get(),
                sellInteractor = get(),
                ethereumInteractor = get(),
                strigaUserInteractor = get(),
                strigaOnRampInteractor = get(),
                strigaWalletInteractor = get(),
                tokenKeyProvider = get()
            )
        }
        factoryOf(::HomePresenterMapper)
        factoryOf(::StrigaKycUiBannerMapper)
        factoryOf(::WalletPresenterMapper)
        factoryOf(::StrigaOnRampConfirmedHandler)
        factoryOf(::StrigaOnRampClickHandler)
        factoryOf(::StrigaBannerClickHandler)
        factory<HomeContract.Presenter> {
            // todo: do something with this dependenices!
            // todo: to eliminate all this hell, we could just migrate to hilt
            HomePresenter(
                homeInteractor = get(),
                analytics = get(),
                userInteractor = get(),
                homeMapper = get(),
                newBuyFeatureToggle = get(),
                intercomDeeplinkManager = get(),
                deeplinksManager = get(),
                connectionManager = get(),
                transactionManager = get(),
                context = get(),
                userTokensInteractor = get(),
                updatesManager = get(),
                environmentManager = get(),
                strigaFeatureToggle = get(),
                tokenKeyProvider = get(),
                tokenServiceCoordinator = get(),
                strigaInteractor = get(),
                onRampConfirmedHandler = get()
            )
        }
        factoryOf(::WalletPresenter) bind WalletContract.Presenter::class
        factoryOf(::MyCryptoPresenter) bind MyCryptoContract.Presenter::class

        factoryOf(::TokenListPresenter) bind TokenListContract.Presenter::class

        factoryOf(::TopUpWalletPresenter) bind TopUpWalletContract.Presenter::class
    }
}
