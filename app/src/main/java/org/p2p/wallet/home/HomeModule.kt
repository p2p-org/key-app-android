package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
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
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.newsend.interactor.SearchInteractor
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcContract
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcPresenter
import org.p2p.wallet.receive.token.ReceiveTokenContract
import org.p2p.wallet.receive.token.ReceiveTokenPresenter
import org.p2p.wallet.striga.ui.TopUpWalletContract
import org.p2p.wallet.striga.ui.TopUpWalletPresenter
import org.p2p.wallet.updates.subscribe.SolanaAccountUpdateSubscriber
import org.p2p.wallet.updates.subscribe.SplTokenProgramSubscriber

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
                strigaClaimInteractor = get(),
                strigaWalletInteractor = get(),
                tokenKeyProvider = get()
            )
        }
        factoryOf(::HomePresenterMapper)
        factoryOf(::StrigaKycUiBannerMapper)
        factory<HomeContract.Presenter> {
            val subscribers = listOf(
                new(::SplTokenProgramSubscriber),
                new(::SolanaAccountUpdateSubscriber)
            )
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
                appLoader = get(),
                context = get(),
                userTokensInteractor = get()
            )
        }
        factory<ReceiveNetworkTypeContract.Presenter> { (type: NetworkType) ->
            ReceiveNetworkTypePresenter(
                renBtcInteractor = get(),
                userInteractor = get(),
                transactionAmountRepository = get(),
                tokenKeyProvider = get(),
                tokenInteractor = get(),
                receiveAnalytics = get(),
                environmentManager = get(),
                newBuyFeatureToggle = get(),
                networkType = type,
                userSignUpDetailsStorage = get(),
                userTokensInteractor = get(),
                renBtcAnalytics = get()
            )
        }

        factory<ReceiveTokenContract.Presenter> { (token: Token.Active) ->
            ReceiveTokenPresenter(
                defaultToken = token,
                qrCodeInteractor = get(),
                usernameInteractor = get(),
                tokenKeyProvider = get(),
                receiveAnalytics = get()
            )
        }

        factoryOf(::TokenListPresenter) bind TokenListContract.Presenter::class
        factoryOf(::ReceiveRenBtcPresenter) bind ReceiveRenBtcContract.Presenter::class

        factoryOf(::TopUpWalletPresenter) bind TopUpWalletContract.Presenter::class
    }
}
