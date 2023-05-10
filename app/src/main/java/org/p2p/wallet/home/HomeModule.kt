package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.home.model.HomeMapper
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.repository.RefreshErrorInMemoryRepository
import org.p2p.wallet.home.repository.RefreshErrorRepository
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeElementItemMapper
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.main.UserTokensPolling
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsContract
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsPresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
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
import org.p2p.wallet.updates.subscribe.BalanceUpdateSubscriber
import org.p2p.wallet.updates.subscribe.SplTokenProgramSubscriber

object HomeModule : InjectionModule {

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factory<HomeLocalRepository> { HomeDatabaseRepository(get()) }
        factoryOf(::HomeMapper)
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
        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
        factoryOf(::UserTokensPolling)
        /* Cached data exists, therefore creating singleton */
        // todo: do something with this dependenices!
        // todo: to eliminate all this hell, we could just migrate to hilt
        factory<HomeContract.Presenter> {
            val subscribers = listOf(
                new(::SplTokenProgramSubscriber),
                new(::BalanceUpdateSubscriber)
            )
            HomePresenter(
                analytics = get(),
                claimAnalytics = get(),
                updatesManager = get(),
                userInteractor = get(),
                settingsInteractor = get(),
                usernameInteractor = get(),
                homeMapper = get(),
                environmentManager = get(),
                tokenKeyProvider = get(),
                homeElementItemMapper = HomeElementItemMapper(get()),
                resources = get(),
                newBuyFeatureToggle = get(),
                networkObserver = get(),
                tokensPolling = get(),
                metadataInteractor = get(),
                sellInteractor = get(),
                sellEnabledFeatureToggle = get(),
                intercomDeeplinkManager = get(),
                ethereumInteractor = get(),
                seedPhraseProvider = get(),
                deeplinksManager = get(),
                connectionManager = get(),
                transactionManager = get(),
                ethereumSendRepository = get(),
                updateSubscribers = subscribers
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

        factoryOf(::HomeActionsPresenter) bind HomeActionsContract.Presenter::class
    }
}
