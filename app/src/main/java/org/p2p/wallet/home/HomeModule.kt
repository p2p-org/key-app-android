package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeElementItemMapper
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.main.UserTokensPolling
import org.p2p.wallet.home.ui.new.NewSelectTokenContract
import org.p2p.wallet.home.ui.new.NewSelectTokenPresenter
import org.p2p.wallet.home.ui.select.SelectTokenContract
import org.p2p.wallet.home.ui.select.SelectTokenPresenter
import org.p2p.wallet.newsend.NewSendContract
import org.p2p.wallet.newsend.NewSendPresenter
import org.p2p.wallet.receive.list.TokenListContract
import org.p2p.wallet.receive.list.TokenListPresenter
import org.p2p.wallet.receive.network.ReceiveNetworkTypeContract
import org.p2p.wallet.receive.network.ReceiveNetworkTypePresenter
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcContract
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcPresenter
import org.p2p.wallet.receive.token.ReceiveTokenContract
import org.p2p.wallet.receive.token.ReceiveTokenPresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.SendContract
import org.p2p.wallet.send.ui.main.SendPresenter
import org.p2p.wallet.send.ui.search.NewSearchContract
import org.p2p.wallet.send.ui.search.NewSearchPresenter
import org.p2p.wallet.send.ui.search.SearchContract
import org.p2p.wallet.send.ui.search.SearchPresenter

object HomeModule : InjectionModule {

    const val MOONPAY_QUALIFIER = "api.moonpay.com"

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factory<HomeLocalRepository> { HomeDatabaseRepository(get()) }
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
            )
        }
        factory {
            SearchInteractor(
                usernameRepository = get(),
                userInteractor = get(),
                tokenKeyProvider = get(),
                transactionAddressInteractor = get(),
                resourcesProvider = get()
            )
        }
    }

    private fun Module.initPresentationLayer() {
        factoryOf(::UserTokensPolling)
        /* Cached data exists, therefore creating singleton */
        factory<HomeContract.Presenter> {
            HomePresenter(
                analytics = get(),
                updatesManager = get(),
                userInteractor = get(),
                settingsInteractor = get(),
                usernameInteractor = get(),
                environmentManager = get(),
                tokenKeyProvider = get(),
                homeElementItemMapper = HomeElementItemMapper(),
                resourcesProvider = get(),
                newBuyFeatureToggle = get(),
                networkObserver = get(),
                tokensPolling = get(),
                metadataInteractor = get()
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

        sendModule()
    }

    private fun Module.sendModule() {
        factory<SelectTokenContract.Presenter> { (tokens: List<Token>) ->
            SelectTokenPresenter(tokens)
        }
        factory<SearchContract.Presenter> { (usernames: List<SearchResult>) ->
            SearchPresenter(usernames = usernames, searchInteractor = get(), usernameDomainFeatureToggle = get())
        }
        factory<SendContract.Presenter> {
            SendPresenter(
                sendInteractor = get(),
                addressInteractor = get(),
                userInteractor = get(),
                searchInteractor = get(),
                burnBtcInteractor = get(),
                settingsInteractor = get(),
                tokenKeyProvider = get(),
                browseAnalytics = get(),
                analyticsInteractor = get(),
                sendAnalytics = get(),
                transactionManager = get(),
                resourcesProvider = get(),
                usernameDomainFeatureToggle = get(),
                dispatchers = get()
            )
        }

        factory<NewSearchContract.Presenter> { (usernames: List<SearchResult>) ->
            NewSearchPresenter(
                usernames = usernames,
                searchInteractor = get(),
                usernameDomainFeatureToggle = get()
            )
        }
        factoryOf(::NewSelectTokenPresenter) bind NewSelectTokenContract.Presenter::class
        factoryOf(::NewSendPresenter) bind NewSendContract.Presenter::class
    }
}
