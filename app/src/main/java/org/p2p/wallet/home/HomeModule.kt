package org.p2p.wallet.home

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.token.Token
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.repository.HomeDatabaseRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.home.ui.main.HomeContract
import org.p2p.wallet.home.ui.main.HomeElementItemMapper
import org.p2p.wallet.home.ui.main.HomePresenter
import org.p2p.wallet.home.ui.main.UserTokensPolling
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsContract
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsPresenter
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

object HomeModule : InjectionModule {

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
                dispatchers = get()
            )
        }
        factoryOf(::SearchInteractor)
    }

    private fun Module.initPresentationLayer() {
        factoryOf(::UserTokensPolling)
        /* Cached data exists, therefore creating singleton */
        // todo: do something with this dependenices!
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
                metadataInteractor = get(),
                sellInteractor = get(),
                sellEnabledFeatureToggle = get(),
                intercomDeeplinkManager = get()
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
